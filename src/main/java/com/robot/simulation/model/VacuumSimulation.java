package com.robot.simulation.model;

import com.robot.simulation.controller.PathFinder;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;

public class VacuumSimulation {
    public enum Mode {
        CLEANING("Temizlik"),
        RETURNING("Sarj Istasyonuna Donus"),
        CHARGING("Sarj"),
        IDLE("Beklemede"),
        FINISHED("Tamamlandi");

        private final String label;

        Mode(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    private static final double MOVE_COST = 1.1;
    private static final double CHARGE_PER_TICK = 6.5;
    private static final int TICK_MILLIS = 180;

    private final Random random = new Random();
    private final List<String> pendingLogs = new ArrayList<>();
    private final List<GridPoint> travelTrail = new ArrayList<>();
    private final Deque<GridPoint> activePath = new ArrayDeque<>();

    private final RoomType roomType;
    private RoomGrid roomGrid;
    private RobotVacuum robot;
    private CleaningAlgorithm algorithm;
    private Mode mode;
    private boolean running;
    private long elapsedMillis;
    private int cleaningTicksRemaining;
    private DirtType activeCleaningType;
    private boolean unreachableLogged;

    public VacuumSimulation() {
        this(RoomType.KITCHEN);
    }

    public VacuumSimulation(RoomType roomType) {
        this.roomType = roomType;
        reset();
    }

    public void reset() {
        roomGrid = RoomGrid.createLayout(roomType);
        robot = new RobotVacuum(roomGrid.getChargingStation());
        algorithm = CleaningAlgorithm.SMART;
        mode = Mode.IDLE;
        running = false;
        elapsedMillis = 0L;
        cleaningTicksRemaining = 0;
        activeCleaningType = null;
        travelTrail.clear();
        travelTrail.add(robot.getPosition());
        activePath.clear();
        unreachableLogged = false;
        roomGrid.cell(robot.getPosition().x(), robot.getPosition().y()).setVisited(true);
        pendingLogs.clear();
        log(roomType.getTitle() + " simulasyonu sifirlandi.");
    }

    public void tick() {
        if (!running) {
            return;
        }

        elapsedMillis += TICK_MILLIS;

        if (mode == Mode.FINISHED) {
            return;
        }

        if (cleaningTicksRemaining > 0) {
            cleaningTicksRemaining--;
            if (cleaningTicksRemaining == 0) {
                finishCleaning();
            }
            return;
        }

        if (robot.getBatteryLevel() <= 0) {
            running = false;
            mode = Mode.IDLE;
            log("Pil bitti. Simulasyon durduruldu.");
            return;
        }

        if (mode != Mode.RETURNING && mode != Mode.CHARGING && robot.isBatteryLow()) {
            sendRobotToChargingStation(true);
        }

        if (mode == Mode.RETURNING) {
            handleReturn();
            return;
        }

        if (mode == Mode.CHARGING) {
            handleCharging();
            return;
        }

        mode = Mode.CLEANING;
        Cell currentCell = roomGrid.cell(robot.getPosition().x(), robot.getPosition().y());
        if (currentCell.hasDirt()) {
            startCleaning(currentCell);
            return;
        }

        GridPoint nextMove = selectNextMove();
        if (nextMove == null) {
            if (roomGrid.getDirtyCellCount() == 0) {
                if (robot.getPosition().equals(roomGrid.getChargingStation())) {
                    mode = Mode.FINISHED;
                    running = false;
                    log("Tum kirler temizlendi.");
                } else {
                    log("Tum kirler temizlendi. Robot istasyona donuyor.");
                    sendRobotToChargingStation(false);
                }
            } else if (!unreachableLogged) {
                unreachableLogged = true;
                log("Bazi kirli alanlara ulasilamiyor.");
                running = false;
                mode = Mode.IDLE;
            }
            return;
        }

        moveRobot(nextMove);
    }

    private void handleReturn() {
        if (robot.getPosition().equals(roomGrid.getChargingStation())) {
            mode = Mode.CHARGING;
            log("Robot sarj istasyonuna ulasti.");
            return;
        }

        ensurePathTo(roomGrid.getChargingStation());
        followCurrentPath();
    }

    private void handleCharging() {
        robot.charge(CHARGE_PER_TICK);
        if (robot.getBatteryLevel() >= RobotVacuum.MAX_BATTERY) {
            robot.setBatteryLevel(RobotVacuum.MAX_BATTERY);
            activePath.clear();
            if (roomGrid.getDirtyCellCount() == 0) {
                mode = Mode.FINISHED;
                running = false;
                log("Sarj tamamlandi. Temizlik gorevi bitti.");
            } else {
                mode = Mode.CLEANING;
                log("Sarj tamamlandi. Temizlik devam ediyor.");
            }
        }
    }

    private void startCleaning(Cell cell) {
        activeCleaningType = cell.getDirtType();
        cleaningTicksRemaining = activeCleaningType.getCleaningTicks();
        robot.consume(activeCleaningType.getBatteryCost());
        log(activeCleaningType.getLabel() + " temizleniyor...");
    }

    private void finishCleaning() {
        Cell cell = roomGrid.cell(robot.getPosition().x(), robot.getPosition().y());
        if (cell != null && cell.hasDirt()) {
            String dirtLabel = cell.getDirtType().getLabel();
            cell.clearDirt();
            log(dirtLabel + " temizlendi.");
        }
        activeCleaningType = null;
    }

    private GridPoint selectNextMove() {
        if (!activePath.isEmpty()) {
            return popNextPathPoint();
        }

        return switch (algorithm) {
            case RANDOM -> chooseRandomMove();
            case SPIRAL -> chooseSpiralMove();
            case SMART -> chooseSmartMove();
        };
    }

    private GridPoint chooseRandomMove() {
        List<GridPoint> neighbors = roomGrid.neighbors(robot.getPosition());
        if (neighbors.isEmpty()) {
            return null;
        }
        neighbors.sort(Comparator.comparingInt(point -> roomGrid.cell(point.x(), point.y()).isVisited() ? 1 : 0));
        int limit = Math.min(2, neighbors.size());
        return neighbors.get(random.nextInt(limit));
    }

    private GridPoint chooseSpiralMove() {
        GridPoint target = findSpiralTarget();
        if (target == null) {
            return chooseSmartMove();
        }
        List<GridPoint> path = PathFinder.shortestPath(roomGrid, robot.getPosition(), target);
        queuePath(path);
        return popNextPathPoint();
    }

    private GridPoint chooseSmartMove() {
        GridPoint targetDirt = findNearestTarget(true);
        if (targetDirt != null) {
            List<GridPoint> path = PathFinder.shortestPath(roomGrid, robot.getPosition(), targetDirt);
            queuePath(path);
            return popNextPathPoint();
        }

        GridPoint unexplored = findNearestTarget(false);
        if (unexplored != null) {
            List<GridPoint> path = PathFinder.shortestPath(roomGrid, robot.getPosition(), unexplored);
            queuePath(path);
            return popNextPathPoint();
        }
        return null;
    }

    private GridPoint findNearestTarget(boolean dirtOnly) {
        GridPoint best = null;
        int bestDistance = Integer.MAX_VALUE;
        for (int x = 0; x < roomGrid.getWidth(); x++) {
            for (int y = 0; y < roomGrid.getHeight(); y++) {
                Cell cell = roomGrid.cell(x, y);
                if (cell.isObstacle()) {
                    continue;
                }
                if (dirtOnly && !cell.hasDirt()) {
                    continue;
                }
                if (!dirtOnly && cell.isVisited()) {
                    continue;
                }
                List<GridPoint> path = PathFinder.shortestPath(roomGrid, robot.getPosition(), cell.getPoint());
                if (path.isEmpty()) {
                    continue;
                }
                int distance = path.size();
                if (distance < bestDistance) {
                    bestDistance = distance;
                    best = cell.getPoint();
                }
            }
        }
        return best;
    }

    private GridPoint findSpiralTarget() {
        int left = 0;
        int right = roomGrid.getWidth() - 1;
        int top = 0;
        int bottom = roomGrid.getHeight() - 1;
        List<GridPoint> spiralOrder = new ArrayList<>();

        while (left <= right && top <= bottom) {
            for (int x = left; x <= right; x++) {
                spiralOrder.add(new GridPoint(x, top));
            }
            top++;
            for (int y = top; y <= bottom; y++) {
                spiralOrder.add(new GridPoint(right, y));
            }
            right--;
            if (top <= bottom) {
                for (int x = right; x >= left; x--) {
                    spiralOrder.add(new GridPoint(x, bottom));
                }
                bottom--;
            }
            if (left <= right) {
                for (int y = bottom; y >= top; y--) {
                    spiralOrder.add(new GridPoint(left, y));
                }
                left++;
            }
        }

        Optional<GridPoint> preferred = spiralOrder.stream()
                .filter(roomGrid::isWalkable)
                .filter(point -> !roomGrid.cell(point.x(), point.y()).isVisited() || roomGrid.cell(point.x(), point.y()).hasDirt())
                .findFirst();
        return preferred.orElse(null);
    }

    private void moveRobot(GridPoint nextPoint) {
        robot.moveTo(nextPoint);
        robot.consume(MOVE_COST);
        roomGrid.cell(nextPoint.x(), nextPoint.y()).setVisited(true);
        travelTrail.add(nextPoint);
    }

    private void ensurePathTo(GridPoint target) {
        if (!activePath.isEmpty()) {
            return;
        }
        List<GridPoint> path = PathFinder.shortestPath(roomGrid, robot.getPosition(), target);
        queuePath(path);
    }

    private void queuePath(List<GridPoint> path) {
        activePath.clear();
        if (path == null || path.isEmpty()) {
            return;
        }
        for (GridPoint point : path) {
            if (!point.equals(robot.getPosition())) {
                activePath.addLast(point);
            }
        }
    }

    private GridPoint popNextPathPoint() {
        return activePath.pollFirst();
    }

    public void start() {
        running = true;
        if (mode == Mode.IDLE) {
            mode = Mode.CLEANING;
        }
        log("Simulasyon baslatildi.");
    }

    public void pause() {
        running = false;
        log("Simulasyon duraklatildi.");
    }

    public void sendRobotToChargingStation(boolean automatic) {
        List<GridPoint> path = PathFinder.shortestPath(roomGrid, robot.getPosition(), roomGrid.getChargingStation());
        queuePath(path);
        if (path.isEmpty() && !robot.getPosition().equals(roomGrid.getChargingStation())) {
            log("Sarj istasyonuna gidis yolu bulunamadi.");
            return;
        }
        mode = robot.getPosition().equals(roomGrid.getChargingStation()) ? Mode.CHARGING : Mode.RETURNING;
        log(automatic ? "Pil dusuk. Robot sarja donuyor." : "Robot sarj istasyonuna yonlendirildi.");
    }

    public void setAlgorithm(CleaningAlgorithm algorithm) {
        this.algorithm = algorithm;
        activePath.clear();
        log("Temizlik algoritmasi: " + algorithm);
    }

    public CleaningAlgorithm getAlgorithm() {
        return algorithm;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public String getRoomTitle() {
        return roomType.getTitle();
    }

    public RoomGrid getRoomGrid() {
        return roomGrid;
    }

    public RobotVacuum getRobot() {
        return robot;
    }

    public Mode getMode() {
        return mode;
    }

    public boolean isRunning() {
        return running;
    }

    public long getElapsedMillis() {
        return elapsedMillis;
    }

    public String getElapsedText() {
        long totalSeconds = elapsedMillis / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format(Locale.US, "%02d:%02d", minutes, seconds);
    }

    public List<GridPoint> getTravelTrail() {
        return List.copyOf(travelTrail);
    }

    public List<GridPoint> getActivePath() {
        return List.copyOf(activePath);
    }

    public double getCleanedPercent() {
        return roomGrid.getVisitedCount() * 100.0 / roomGrid.getWalkableCount();
    }

    public double getRemainingDirtyPercent() {
        int totalWalkable = roomGrid.getWalkableCount();
        return roomGrid.getDirtyCellCount() * 100.0 / totalWalkable;
    }

    public void applyBatteryLevel(double batteryLevel) {
        robot.setBatteryLevel(batteryLevel);
        log("Pil seviyesi manuel olarak %" + String.format(Locale.US, "%.0f", batteryLevel) + " yapildi.");
        if (robot.isBatteryLow() && !robot.getPosition().equals(roomGrid.getChargingStation())) {
            sendRobotToChargingStation(true);
        }
    }

    public void addObstacle(GridPoint point) {
        if (point.equals(robot.getPosition()) || point.equals(roomGrid.getChargingStation())) {
            return;
        }
        roomGrid.clearCell(point.x(), point.y());
        roomGrid.addFurnitureRect("Mobilya", point.x(), point.y(), 1, 1, "#7c4a1f");
        activePath.clear();
        log("Mobilya eklendi: (" + point.x() + ", " + point.y() + ")");
    }

    public void addDirt(GridPoint point, DirtType dirtType) {
        if (point.equals(robot.getPosition()) || point.equals(roomGrid.getChargingStation())) {
            return;
        }
        roomGrid.addDirt(point.x(), point.y(), dirtType);
        log(dirtType.getLabel() + " eklendi: (" + point.x() + ", " + point.y() + ")");
    }

    public void eraseAt(GridPoint point) {
        if (point.equals(roomGrid.getChargingStation())) {
            return;
        }
        roomGrid.clearCell(point.x(), point.y());
        log("Hucre temizlendi: (" + point.x() + ", " + point.y() + ")");
    }

    public List<String> drainLogs() {
        List<String> copy = List.copyOf(pendingLogs);
        pendingLogs.clear();
        return copy;
    }

    private void followCurrentPath() {
        GridPoint next = popNextPathPoint();
        if (next != null) {
            moveRobot(next);
        }
    }

    private void log(String message) {
        pendingLogs.add(message);
    }
}
