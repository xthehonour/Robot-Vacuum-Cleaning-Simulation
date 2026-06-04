package com.robot.simulation.model;

public record FurnitureItem(String name, int x, int y, int width, int height, String colorHex) {
    public boolean contains(int gridX, int gridY) {
        return gridX >= x && gridX < x + width && gridY >= y && gridY < y + height;
    }
}
