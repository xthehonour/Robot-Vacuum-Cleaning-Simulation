package com.robot.simulation.model;

public class Hucre {
    private final int x;
    private final int y;
    private boolean engel;
    private KirTipi kir;
    private boolean temizlendi;
    private boolean sarjIstasyonu;

    public Hucre(int x, int y) {
        this.x = x;
        this.y = y;
        this.engel = false;
        this.kir = null;
        this.temizlendi = false;
        this.sarjIstasyonu = false;
    }

    // Getters and Setters
    public int getX() { return x; }
    public int getY() { return y; }
    public boolean isEngel() { return engel; }
    public void setEngel(boolean engel) { this.engel = engel; }
    public KirTipi getKir() { return kir; }
    public void setKir(KirTipi kir) { this.kir = kir; }
    public boolean isTemizlendi() { return temizlendi; }
    public void setTemizlendi(boolean temizlendi) { this.temizlendi = temizlendi; }
    public boolean isSarjIstasyonu() { return sarjIstasyonu; }
    public void setSarjIstasyonu(boolean sarjIstasyonu) { this.sarjIstasyonu = sarjIstasyonu; }

    public boolean isKirli() {
        return kir != null && !temizlendi;
    }
}
