package com.robot.simulation.model;

public enum RoomType {
    BATHROOM("Banyo"),
    KITCHEN("Mutfak"),
    CHILD_ROOM("Cocuk Odasi");

    private final String title;

    RoomType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public RoomType previous() {
        RoomType[] values = values();
        return values[(ordinal() - 1 + values.length) % values.length];
    }

    public RoomType next() {
        RoomType[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
