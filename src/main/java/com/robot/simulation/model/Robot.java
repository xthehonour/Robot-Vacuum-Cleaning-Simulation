package com.robot.simulation.model;

public class Robot {
    private int x;
    private int y;
    private double pil;
    private final double maxPil;
    private final double hareketMaliyeti = 1.0;

    public Robot(int x, int y, double baslangicPil) {
        this.x = x;
        this.y = y;
        this.pil = baslangicPil;
        this.maxPil = baslangicPil;
    }

    public void hareketEt(int yeniX, int yeniY) {
        this.x = yeniX;
        this.y = yeniY;
        this.pil -= hareketMaliyeti;
    }

    public void temizle(KirTipi kir) {
        if (kir != null) {
            this.pil -= kir.getPilMaliyeti();
        }
    }

    public void sarjEt() {
        this.pil = maxPil;
    }

    // Getters
    public int getX() { return x; }
    public int getY() { return y; }
    public double getPil() { return pil; }
    public double getMaxPil() { return maxPil; }
}
