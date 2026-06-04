package com.robot.simulation.model;

public enum CleaningAlgorithm {
    RANDOM("Rastgele"),
    SPIRAL("Spiral"),
    SMART("Akilli BFS");

    private final String label;

    CleaningAlgorithm(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
