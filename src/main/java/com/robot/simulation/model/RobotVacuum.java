package com.robot.simulation.model;

public class RobotVacuum {
    public static final double MAX_BATTERY = 100.0;

    private GridPoint position;
    private Direction direction;
    private double batteryLevel;

    public RobotVacuum(GridPoint startPosition) {
        this.position = startPosition;
        this.direction = Direction.EAST;
        this.batteryLevel = MAX_BATTERY;
    }

    public GridPoint getPosition() {
        return position;
    }

    public void moveTo(GridPoint nextPoint) {
        direction = Direction.fromDelta(nextPoint.x() - position.x(), nextPoint.y() - position.y());
        position = nextPoint;
    }

    public Direction getDirection() {
        return direction;
    }

    public double getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(double batteryLevel) {
        this.batteryLevel = clampBattery(batteryLevel);
    }

    public void consume(double amount) {
        setBatteryLevel(batteryLevel - amount);
    }

    public void charge(double amount) {
        setBatteryLevel(batteryLevel + amount);
    }

    public boolean isBatteryLow() {
        return batteryLevel <= 22.0;
    }

    private double clampBattery(double value) {
        return Math.max(0.0, Math.min(MAX_BATTERY, value));
    }
}
