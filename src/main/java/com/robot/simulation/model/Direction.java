package com.robot.simulation.model;

public enum Direction {
    NORTH(0, -1, "Kuzey"),
    EAST(1, 0, "Dogu"),
    SOUTH(0, 1, "Guney"),
    WEST(-1, 0, "Bati");

    private final int dx;
    private final int dy;
    private final String label;

    Direction(int dx, int dy, String label) {
        this.dx = dx;
        this.dy = dy;
        this.label = label;
    }

    public int dx() {
        return dx;
    }

    public int dy() {
        return dy;
    }

    public String label() {
        return label;
    }

    public static Direction fromDelta(int dx, int dy) {
        for (Direction direction : values()) {
            if (direction.dx == Integer.signum(dx) && direction.dy == Integer.signum(dy)) {
                return direction;
            }
        }
        return EAST;
    }
}
