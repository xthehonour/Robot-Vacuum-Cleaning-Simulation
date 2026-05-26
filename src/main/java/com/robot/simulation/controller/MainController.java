package com.robot.simulation.controller;

import com.robot.simulation.model.Cell;
import com.robot.simulation.model.CleaningAlgorithm;
import com.robot.simulation.model.DirtType;
import com.robot.simulation.model.GridPoint;
import com.robot.simulation.model.RoomGrid;
import com.robot.simulation.model.RobotVacuum;
import com.robot.simulation.model.VacuumSimulation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class MainController {
    private enum EditMode {
        NONE,
        DIRT,
        OBSTACLE,
        ERASER
    }

    @FXML private Canvas canvas;
    @FXML private ComboBox<CleaningAlgorithm> comboAlgorithm;
    @FXML private ComboBox<DirtType> comboDirtType;
    @FXML private Slider speedSlider;
    @FXML private Slider batterySlider;
    @FXML private ProgressBar batteryProgress;
    @FXML private Label batteryValueLabel;
    @FXML private Label batterySliderValueLabel;
    @FXML private Label positionLabel;
    @FXML private Label directionLabel;
    @FXML private Label cleanedAreaLabel;
    @FXML private Label dirtyAreaLabel;
    @FXML private Label elapsedTimeLabel;
    @FXML private Label modeLabel;
    @FXML private Label speedValueLabel;
    @FXML private Label algorithmLabel;
    @FXML private Label activeToolLabel;
    @FXML private Label totalAreaLabel;
    @FXML private Label statusDirtyLabel;
    @FXML private Label statusCleanedLabel;
    @FXML private Label statusRemainingLabel;
    @FXML private Label statusTimeLabel;
    @FXML private ListView<String> eventLog;
    @FXML private Button dirtModeButton;
    @FXML private Button obstacleModeButton;
    @FXML private Button eraserModeButton;

    private final VacuumSimulation simulation = new VacuumSimulation();
    private Timeline timeline;
    private EditMode editMode = EditMode.NONE;

    private static final double CELL_SIZE = 34.0;
    private static final double PADDING = 34.0;

    @FXML
    public void initialize() {
        comboAlgorithm.getItems().setAll(CleaningAlgorithm.values());
        comboAlgorithm.setValue(simulation.getAlgorithm());
        comboAlgorithm.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                simulation.setAlgorithm(newValue);
                refresh();
            }
        });

        comboDirtType.getItems().setAll(DirtType.values());
        comboDirtType.setValue(DirtType.DUST);

        timeline = new Timeline(new KeyFrame(Duration.millis(180), event -> {
            simulation.tick();
            refresh();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);

        speedSlider.valueProperty().addListener((obs, oldValue, newValue) -> {
            timeline.setRate(newValue.doubleValue());
            speedValueLabel.setText(String.format(Locale.US, "%.1fx", newValue.doubleValue()));
        });
        batterySlider.valueProperty().addListener((obs, oldValue, newValue) ->
                batterySliderValueLabel.setText("%" + String.format(Locale.US, "%.0f", newValue.doubleValue())));

        canvas.setOnMouseClicked(event -> {
            GridPoint point = pointFromCanvas(event.getX(), event.getY());
            if (point == null) {
                return;
            }
            switch (editMode) {
                case DIRT -> simulation.addDirt(point, comboDirtType.getValue());
                case OBSTACLE -> simulation.addObstacle(point);
                case ERASER -> simulation.eraseAt(point);
                default -> {
                    return;
                }
            }
            refresh();
        });

        batterySlider.setValue(simulation.getRobot().getBatteryLevel());
        speedValueLabel.setText(String.format(Locale.US, "%.1fx", speedSlider.getValue()));
        refresh();
    }

    @FXML
    private void startSimulation() {
        simulation.start();
        timeline.play();
        refresh();
    }

    @FXML
    private void pauseSimulation() {
        simulation.pause();
        timeline.pause();
        refresh();
    }

    @FXML
    private void resetSimulation() {
        timeline.stop();
        simulation.reset();
        comboAlgorithm.setValue(simulation.getAlgorithm());
        batterySlider.setValue(simulation.getRobot().getBatteryLevel());
        editMode = EditMode.NONE;
        updateEditButtons();
        refresh();
    }

    @FXML
    private void returnToStation() {
        simulation.sendRobotToChargingStation(false);
        if (!simulation.isRunning()) {
            simulation.start();
        }
        timeline.play();
        refresh();
    }

    @FXML
    private void applyBattery() {
        simulation.applyBatteryLevel(batterySlider.getValue());
        refresh();
    }

    @FXML
    private void activateDirtMode() {
        editMode = editMode == EditMode.DIRT ? EditMode.NONE : EditMode.DIRT;
        updateEditButtons();
    }

    @FXML
    private void activateObstacleMode() {
        editMode = editMode == EditMode.OBSTACLE ? EditMode.NONE : EditMode.OBSTACLE;
        updateEditButtons();
    }

    @FXML
    private void activateEraserMode() {
        editMode = editMode == EditMode.ERASER ? EditMode.NONE : EditMode.ERASER;
        updateEditButtons();
    }

    private void updateEditButtons() {
        updateModeButton(dirtModeButton, editMode == EditMode.DIRT);
        updateModeButton(obstacleModeButton, editMode == EditMode.OBSTACLE);
        updateModeButton(eraserModeButton, editMode == EditMode.ERASER);
        activeToolLabel.setText("Aktif arac: " + switch (editMode) {
            case DIRT -> "Kir Ekle";
            case OBSTACLE -> "Mobilya Ekle";
            case ERASER -> "Silgi";
            case NONE -> "Secili degil";
        });
    }

    private void updateModeButton(Button button, boolean active) {
        button.getStyleClass().remove("tool-button-active");
        if (active) {
            button.getStyleClass().add("tool-button-active");
        }
    }

    private GridPoint pointFromCanvas(double canvasX, double canvasY) {
        int gridX = (int) ((canvasX - PADDING) / CELL_SIZE);
        int gridY = (int) ((canvasY - PADDING) / CELL_SIZE);
        RoomGrid grid = simulation.getRoomGrid();
        if (!grid.inBounds(gridX, gridY)) {
            return null;
        }
        return new GridPoint(gridX, gridY);
    }

    private void refresh() {
        drawScene();
        syncLabels();
        appendLogs();
    }

    private void syncLabels() {
        RobotVacuum robot = simulation.getRobot();
        RoomGrid grid = simulation.getRoomGrid();

        batteryProgress.setProgress(robot.getBatteryLevel() / RobotVacuum.MAX_BATTERY);
        batteryProgress.setStyle(robot.isBatteryLow() ? "-fx-accent: #f59e0b;" : "-fx-accent: #22c55e;");
        batteryValueLabel.setText("%" + String.format(Locale.US, "%.0f", robot.getBatteryLevel()));
        batterySliderValueLabel.setText("%" + String.format(Locale.US, "%.0f", batterySlider.getValue()));
        positionLabel.setText("(" + robot.getPosition().x() + ", " + robot.getPosition().y() + ")");
        directionLabel.setText(robot.getDirection().label());
        cleanedAreaLabel.setText(String.format(Locale.US, "%.0f%%", simulation.getCleanedPercent()));
        dirtyAreaLabel.setText(grid.getDirtyCellCount() + " hucre");
        elapsedTimeLabel.setText(simulation.getElapsedText());
        modeLabel.setText(simulation.getMode().getLabel());
        algorithmLabel.setText(comboAlgorithm.getValue() == null ? "-" : comboAlgorithm.getValue().toString());
        speedValueLabel.setText(String.format(Locale.US, "%.1fx", speedSlider.getValue()));
        totalAreaLabel.setText(grid.getWalkableCount() + " m2");
        statusDirtyLabel.setText(grid.getVisitedCount() + " m2 (" + String.format(Locale.US, "%.0f%%", simulation.getCleanedPercent()) + ")");
        statusCleanedLabel.setText(grid.getDirtyCellCount() + " hucre");
        statusRemainingLabel.setText(String.format(Locale.US, "%.0f%%", simulation.getRemainingDirtyPercent()));
        statusTimeLabel.setText(simulation.getElapsedText());
    }

    private void appendLogs() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        for (String log : simulation.drainLogs()) {
            eventLog.getItems().add(0, "[" + LocalTime.now().format(formatter) + "] " + log);
        }
        if (eventLog.getItems().size() > 200) {
            eventLog.getItems().remove(200, eventLog.getItems().size());
        }
    }

    private void drawScene() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        RoomGrid grid = simulation.getRoomGrid();
        double width = PADDING * 2 + grid.getWidth() * CELL_SIZE;
        double height = PADDING * 2 + grid.getHeight() * CELL_SIZE;
        canvas.setWidth(width);
        canvas.setHeight(height);

        gc.setFill(Color.web("#0b1726"));
        gc.fillRoundRect(0, 0, width, height, 28, 28);

        drawBoard(gc, grid);
        drawPaths(gc, simulation.getTravelTrail(), Color.web("#60a5fa"), false);
        drawPaths(gc, simulation.getActivePath(), Color.web("#22c55e"), true);
        drawGridContents(gc, grid);
        drawRobot(gc, simulation.getRobot());
        drawAxis(gc, grid);
    }

    private void drawBoard(GraphicsContext gc, RoomGrid grid) {
        double boardX = PADDING;
        double boardY = PADDING;
        double boardWidth = grid.getWidth() * CELL_SIZE;
        double boardHeight = grid.getHeight() * CELL_SIZE;

        gc.setFill(Color.web("#d3b185"));
        gc.fillRoundRect(boardX, boardY, boardWidth, boardHeight, 20, 20);
        gc.setFill(Color.web("#e9cda7"));
        gc.fillRoundRect(boardX + 8, boardY + 8, boardWidth - 16, boardHeight - 16, 18, 18);

        gc.setStroke(Color.web("#c7a378"));
        for (int x = 0; x <= grid.getWidth(); x++) {
            double px = boardX + x * CELL_SIZE;
            gc.strokeLine(px, boardY, px, boardY + boardHeight);
        }
        for (int y = 0; y <= grid.getHeight(); y++) {
            double py = boardY + y * CELL_SIZE;
            gc.strokeLine(boardX, py, boardX + boardWidth, py);
        }
    }

    private void drawPaths(GraphicsContext gc, List<GridPoint> points, Color color, boolean dashed) {
        if (points.size() < 2) {
            return;
        }
        gc.setStroke(color);
        gc.setLineWidth(2.5);
        gc.setLineDashes(dashed ? 10 : 0);
        for (int i = 1; i < points.size(); i++) {
            GridPoint previous = points.get(i - 1);
            GridPoint current = points.get(i);
            gc.strokeLine(centerX(previous.x()), centerY(previous.y()), centerX(current.x()), centerY(current.y()));
            drawArrow(gc, previous, current, color);
        }
        gc.setLineDashes(0);
    }

    private void drawArrow(GraphicsContext gc, GridPoint start, GridPoint end, Color color) {
        double x1 = centerX(start.x());
        double y1 = centerY(start.y());
        double x2 = centerX(end.x());
        double y2 = centerY(end.y());
        double angle = Math.atan2(y2 - y1, x2 - x1);
        double length = 7;
        gc.setFill(color);
        gc.fillPolygon(
                new double[]{x2, x2 - length * Math.cos(angle - Math.PI / 6), x2 - length * Math.cos(angle + Math.PI / 6)},
                new double[]{y2, y2 - length * Math.sin(angle - Math.PI / 6), y2 - length * Math.sin(angle + Math.PI / 6)},
                3
        );
    }

    private void drawGridContents(GraphicsContext gc, RoomGrid grid) {
        for (int x = 0; x < grid.getWidth(); x++) {
            for (int y = 0; y < grid.getHeight(); y++) {
                Cell cell = grid.cell(x, y);
                double drawX = PADDING + x * CELL_SIZE;
                double drawY = PADDING + y * CELL_SIZE;

                if (cell.isVisited() && !cell.isObstacle()) {
                    gc.setFill(Color.color(0.85, 0.95, 1.0, 0.35));
                    gc.fillRoundRect(drawX + 3, drawY + 3, CELL_SIZE - 6, CELL_SIZE - 6, 8, 8);
                }

                if (cell.isObstacle()) {
                    gc.setFill(Color.web("#7c4a1f"));
                    gc.fillRoundRect(drawX + 2, drawY + 2, CELL_SIZE - 4, CELL_SIZE - 4, 10, 10);
                    gc.setFill(Color.web("#a1622a"));
                    gc.fillRoundRect(drawX + 4, drawY + 4, CELL_SIZE - 8, CELL_SIZE - 10, 8, 8);
                } else if (cell.isChargingStation()) {
                    gc.setFill(Color.web("#1f2937"));
                    gc.fillRoundRect(drawX + 4, drawY + 4, CELL_SIZE - 8, CELL_SIZE - 8, 10, 10);
                    gc.setFill(Color.web("#22c55e"));
                    gc.fillOval(drawX + 10, drawY + 10, CELL_SIZE - 20, CELL_SIZE - 20);
                } else if (cell.hasDirt()) {
                    gc.setFill(Color.web(cell.getDirtType().getColorHex()));
                    gc.fillOval(drawX + 11, drawY + 11, 6, 6);
                    gc.fillOval(drawX + 18, drawY + 13, 5, 5);
                    gc.fillOval(drawX + 14, drawY + 20, 7, 7);
                    gc.fillOval(drawX + 22, drawY + 20, 4, 4);
                }
            }
        }
    }

    private void drawRobot(GraphicsContext gc, RobotVacuum robot) {
        double x = centerX(robot.getPosition().x());
        double y = centerY(robot.getPosition().y());
        double radius = 13;

        gc.setFill(Color.color(0, 0, 0, 0.25));
        gc.fillOval(x - radius + 3, y - radius + 4, radius * 2, radius * 2);
        gc.setFill(Color.web("#f8fafc"));
        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);
        gc.setStroke(Color.web("#475569"));
        gc.setLineWidth(2);
        gc.strokeOval(x - radius, y - radius, radius * 2, radius * 2);

        gc.setFill(Color.web("#1e293b"));
        gc.fillOval(x - 6, y - 6, 12, 12);

        double dirX = x + robot.getDirection().dx() * 10;
        double dirY = y + robot.getDirection().dy() * 10;
        gc.setStroke(Color.web("#60a5fa"));
        gc.setLineWidth(3);
        gc.strokeLine(x, y, dirX, dirY);

        gc.setFill(Color.web("#f59e0b"));
        gc.fillRoundRect(x - 12, y - 24, 24, 10, 4, 4);
        gc.setFill(Color.web("#0f172a"));
        gc.fillRoundRect(x - 10, y - 22, 20 * (robot.getBatteryLevel() / 100.0), 6, 3, 3);
    }

    private void drawAxis(GraphicsContext gc, RoomGrid grid) {
        gc.setFill(Color.web("#dbeafe"));
        gc.setTextAlign(TextAlignment.CENTER);
        for (int x = 0; x < grid.getWidth(); x++) {
            gc.fillText(String.valueOf(x), centerX(x), 18);
        }
        gc.setTextAlign(TextAlignment.RIGHT);
        for (int y = 0; y < grid.getHeight(); y++) {
            gc.fillText(String.valueOf(y), 18, centerY(y) + 4);
        }
    }

    private double centerX(int gridX) {
        return PADDING + gridX * CELL_SIZE + CELL_SIZE / 2;
    }

    private double centerY(int gridY) {
        return PADDING + gridY * CELL_SIZE + CELL_SIZE / 2;
    }
}
