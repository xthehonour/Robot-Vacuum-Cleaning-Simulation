package com.robot.simulation.model;

public class Cell {
    private final GridPoint point;
    private boolean obstacle;
    private boolean chargingStation;
    private DirtType dirtType;
    private boolean visited;

    public Cell(int x, int y) {
        this.point = new GridPoint(x, y);
    }

    public GridPoint getPoint() {
        return point;
    }

    public int getX() {
        return point.x();
    }

    public int getY() {
        return point.y();
    }

    public boolean isObstacle() {
        return obstacle;
    }

    public void setObstacle(boolean obstacle) {
        this.obstacle = obstacle;
        if (obstacle) {
            this.dirtType = null;
        }
    }

    public boolean isChargingStation() {
        return chargingStation;
    }

    public void setChargingStation(boolean chargingStation) {
        this.chargingStation = chargingStation;
    }

    public DirtType getDirtType() {
        return dirtType;
    }

    public boolean hasDirt() {
        return dirtType != null;
    }

    public void setDirtType(DirtType dirtType) {
        this.dirtType = dirtType;
        if (dirtType != null) {
            this.obstacle = false;
        }
    }

    public void clearDirt() {
        this.dirtType = null;
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }
}
