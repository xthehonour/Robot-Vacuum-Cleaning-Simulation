package com.robot.simulation.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RoomGrid {
    private final int width;
    private final int height;
    private final Cell[][] cells;
    private final List<FurnitureItem> furnitureItems = new ArrayList<>();

    public RoomGrid(int width, int height) {
        this.width = width;
        this.height = height;
        this.cells = new Cell[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                cells[x][y] = new Cell(x, y);
            }
        }
    }

    public static RoomGrid createDefaultLayout() {
        return createLayout(RoomType.KITCHEN);
    }

    public static RoomGrid createLayout(RoomType roomType) {
        return switch (roomType) {
            case BATHROOM -> createBathroomLayout();
            case KITCHEN -> createKitchenLayout();
            case CHILD_ROOM -> createChildRoomLayout();
        };
    }

    private static RoomGrid createBathroomLayout() {
        RoomGrid grid = createBaseGrid();
        grid.addFurnitureRect("Dus Kabini", 15, 1, 4, 4, "#67e8f9");
        grid.addFurnitureRect("Kuvet", 1, 1, 5, 2, "#bae6fd");
        grid.addFurnitureRect("Lavabo", 8, 1, 2, 2, "#e0f2fe");
        grid.addFurnitureRect("Klozet", 12, 1, 2, 2, "#f8fafc");
        grid.addFurnitureRect("Dolap", 3, 8, 3, 2, "#94a3b8");
        grid.addFurnitureRect("Sepet", 16, 8, 2, 2, "#c084fc");

        grid.addDirt(9, 3, DirtType.LIQUID);
        grid.addDirt(16, 5, DirtType.LIQUID);
        grid.addDirt(17, 5, DirtType.LIQUID);
        grid.addDirt(2, 4, DirtType.DUST);
        grid.addDirt(6, 9, DirtType.DUST);
        grid.addDirt(13, 5, DirtType.STAIN);
        return grid;
    }

    private static RoomGrid createKitchenLayout() {
        RoomGrid grid = createBaseGrid();
        grid.addFurnitureRect("Tezgah", 4, 1, 6, 1, "#475569");
        grid.addFurnitureRect("Evye", 10, 1, 3, 1, "#38bdf8");
        grid.addFurnitureRect("Ocak", 13, 1, 3, 1, "#1f2937");
        grid.addFurnitureRect("Buzdolabi", 17, 1, 2, 3, "#cbd5e1");
        grid.addFurnitureRect("Masa", 8, 5, 4, 3, "#a16207");
        grid.addFurnitureRect("Sandalye", 6, 6, 1, 1, "#92400e");
        grid.addFurnitureRect("Sandalye", 13, 6, 1, 1, "#92400e");
        grid.addFurnitureRect("Kiler Dolabi", 2, 8, 2, 3, "#78350f");

        grid.addDirt(11, 2, DirtType.LIQUID);
        grid.addDirt(14, 2, DirtType.STAIN);
        grid.addDirt(7, 7, DirtType.DUST);
        grid.addDirt(12, 8, DirtType.DUST);
        grid.addDirt(4, 10, DirtType.STAIN);
        grid.addDirt(16, 6, DirtType.DUST);
        return grid;
    }

    private static RoomGrid createChildRoomLayout() {
        RoomGrid grid = createBaseGrid();
        grid.addFurnitureRect("Yatak", 1, 1, 5, 3, "#60a5fa");
        grid.addFurnitureRect("Gardrop", 15, 1, 4, 2, "#7c3aed");
        grid.addFurnitureRect("Calisma Masasi", 12, 6, 4, 2, "#0f766e");
        grid.addFurnitureRect("Oyuncak Kutusu", 2, 8, 3, 2, "#f97316");
        grid.addFurnitureRect("Kitaplik", 17, 7, 2, 4, "#16a34a");
        grid.addFurnitureRect("Komodin", 6, 2, 1, 1, "#8b5a2b");

        grid.addDirt(8, 3, DirtType.DUST);
        grid.addDirt(10, 5, DirtType.DUST);
        grid.addDirt(5, 8, DirtType.STAIN);
        grid.addDirt(11, 9, DirtType.DUST);
        grid.addDirt(16, 11, DirtType.DUST);
        grid.addDirt(3, 11, DirtType.STAIN);
        return grid;
    }

    private static RoomGrid createBaseGrid() {
        RoomGrid grid = new RoomGrid(20, 14);
        grid.cell(0, 12).setChargingStation(true);
        grid.addObstacleRect(0, 11, 2, 1);
        grid.addObstacleRect(0, 13, 10, 1);
        return grid;
    }

    public void addObstacleRect(int startX, int startY, int rectWidth, int rectHeight) {
        for (int x = startX; x < startX + rectWidth; x++) {
            for (int y = startY; y < startY + rectHeight; y++) {
                if (inBounds(x, y) && !cell(x, y).isChargingStation()) {
                    cell(x, y).setObstacle(true);
                }
            }
        }
    }

    public void addFurnitureRect(String name, int startX, int startY, int rectWidth, int rectHeight, String colorHex) {
        for (int x = startX; x < startX + rectWidth; x++) {
            for (int y = startY; y < startY + rectHeight; y++) {
                if (inBounds(x, y) && !cell(x, y).isChargingStation()) {
                    cell(x, y).clearDirt();
                    cell(x, y).setObstacle(true);
                }
            }
        }
        furnitureItems.add(new FurnitureItem(name, startX, startY, rectWidth, rectHeight, colorHex));
    }

    public Cell cell(int x, int y) {
        return inBounds(x, y) ? cells[x][y] : null;
    }

    public boolean inBounds(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public boolean isWalkable(GridPoint point) {
        Cell cell = cell(point.x(), point.y());
        return cell != null && !cell.isObstacle();
    }

    public List<GridPoint> neighbors(GridPoint point) {
        List<GridPoint> result = new ArrayList<>();
        int[][] directions = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};
        for (int[] direction : directions) {
            int nx = point.x() + direction[0];
            int ny = point.y() + direction[1];
            if (inBounds(nx, ny) && !cell(nx, ny).isObstacle()) {
                result.add(new GridPoint(nx, ny));
            }
        }
        return result;
    }

    public void addDirt(int x, int y, DirtType type) {
        Cell cell = cell(x, y);
        if (cell != null && !cell.isObstacle() && !cell.isChargingStation()) {
            cell.setDirtType(type);
        }
    }

    public void clearCell(int x, int y) {
        Cell cell = cell(x, y);
        if (cell != null && !cell.isChargingStation()) {
            Optional<FurnitureItem> furniture = furnitureAt(x, y);
            if (furniture.isPresent()) {
                clearFurniture(furniture.get());
                return;
            }
            cell.setObstacle(false);
            cell.clearDirt();
        }
    }

    private void clearFurniture(FurnitureItem furniture) {
        furnitureItems.remove(furniture);
        for (int x = furniture.x(); x < furniture.x() + furniture.width(); x++) {
            for (int y = furniture.y(); y < furniture.y() + furniture.height(); y++) {
                Cell cell = cell(x, y);
                if (cell != null && !cell.isChargingStation()) {
                    cell.setObstacle(false);
                    cell.clearDirt();
                }
            }
        }
    }

    public Optional<FurnitureItem> furnitureAt(int x, int y) {
        return furnitureItems.stream()
                .filter(item -> item.contains(x, y))
                .findFirst();
    }

    public boolean isFurnitureCell(int x, int y) {
        return furnitureAt(x, y).isPresent();
    }

    public List<FurnitureItem> getFurnitureItems() {
        return List.copyOf(furnitureItems);
    }

    public GridPoint getChargingStation() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (cells[x][y].isChargingStation()) {
                    return cells[x][y].getPoint();
                }
            }
        }
        return new GridPoint(0, 0);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getWalkableCount() {
        int total = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (!cells[x][y].isObstacle()) {
                    total++;
                }
            }
        }
        return total;
    }

    public int getVisitedCount() {
        int total = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (!cells[x][y].isObstacle() && cells[x][y].isVisited()) {
                    total++;
                }
            }
        }
        return total;
    }

    public int getDirtyCellCount() {
        int total = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (cells[x][y].hasDirt()) {
                    total++;
                }
            }
        }
        return total;
    }
}
