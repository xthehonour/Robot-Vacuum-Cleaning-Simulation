package com.robot.simulation.view;

import com.robot.simulation.model.Cell;
import com.robot.simulation.model.FurnitureItem;
import com.robot.simulation.model.GridPoint;
import com.robot.simulation.model.RobotVacuum;
import com.robot.simulation.model.RoomGrid;
import com.robot.simulation.model.VacuumSimulation;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.List;

public class CanvasBoardRenderer {
    public static final double CELL_SIZE = 34.0;
    public static final double PADDING = 34.0;

    private final Canvas canvas;

    public CanvasBoardRenderer(Canvas canvas) {
        this.canvas = canvas;
    }

    public void render(VacuumSimulation simulation) {
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
        drawFurniture(gc, grid);
        drawGridContents(gc, grid);
        drawRobot(gc, simulation.getRobot());
        drawAxis(gc, grid);
    }

    public GridPoint pointFromCanvas(RoomGrid grid, double canvasX, double canvasY) {
        int gridX = (int) ((canvasX - PADDING) / CELL_SIZE);
        int gridY = (int) ((canvasY - PADDING) / CELL_SIZE);
        return grid.inBounds(gridX, gridY) ? new GridPoint(gridX, gridY) : null;
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
                drawCell(gc, grid, grid.cell(x, y));
            }
        }
    }

    private void drawCell(GraphicsContext gc, RoomGrid grid, Cell cell) {
        double drawX = PADDING + cell.getX() * CELL_SIZE;
        double drawY = PADDING + cell.getY() * CELL_SIZE;

        if (cell.isVisited() && !cell.isObstacle()) {
            gc.setFill(Color.color(0.85, 0.95, 1.0, 0.35));
            gc.fillRoundRect(drawX + 3, drawY + 3, CELL_SIZE - 6, CELL_SIZE - 6, 8, 8);
        }

        if (cell.isObstacle() && !grid.isFurnitureCell(cell.getX(), cell.getY())) {
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

    private void drawFurniture(GraphicsContext gc, RoomGrid grid) {
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("System", FontWeight.BOLD, 10));
        for (FurnitureItem item : grid.getFurnitureItems()) {
            double drawX = PADDING + item.x() * CELL_SIZE + 2;
            double drawY = PADDING + item.y() * CELL_SIZE + 2;
            double width = item.width() * CELL_SIZE - 4;
            double height = item.height() * CELL_SIZE - 4;

            gc.setFill(Color.web(item.colorHex()));
            gc.fillRoundRect(drawX, drawY, width, height, 10, 10);
            gc.setStroke(Color.color(0.02, 0.05, 0.10, 0.35));
            gc.setLineWidth(1.5);
            gc.strokeRoundRect(drawX, drawY, width, height, 10, 10);

            if (item.width() * item.height() > 1) {
                gc.setFill(Color.web("#0f172a"));
                gc.fillText(item.name(), drawX + width / 2, drawY + height / 2 + 4, Math.max(20, width - 8));
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
        gc.fillRoundRect(x - 10, y - 22, 20 * (robot.getBatteryLevel() / RobotVacuum.MAX_BATTERY), 6, 3, 3);
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
