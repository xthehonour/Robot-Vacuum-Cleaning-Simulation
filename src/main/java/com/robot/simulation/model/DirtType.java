package com.robot.simulation.model;

public enum DirtType {
    DUST("Toz", "#8B5CF6", 3.0, 1),
    LIQUID("Sivi", "#06B6D4", 5.0, 3),
    STAIN("Leke", "#F97316", 7.0, 5);

    private final String label;
    private final String colorHex;
    private final double batteryCost;
    private final int cleaningTicks;

    DirtType(String label, String colorHex, double batteryCost, int cleaningTicks) {
        this.label = label;
        this.colorHex = colorHex;
        this.batteryCost = batteryCost;
        this.cleaningTicks = cleaningTicks;
    }

    public String getLabel() {
        return label;
    }

    public String getColorHex() {
        return colorHex;
    }

    public double getBatteryCost() {
        return batteryCost;
    }

    public int getCleaningTicks() {
        return cleaningTicks;
    }

    @Override
    public String toString() {
        return label;
    }
}
