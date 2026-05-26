package com.robot.simulation.controller;

public enum Algoritma {
    RASTGELE("Rastgele Hareket"),
    ZIGZAG("Zigzag (Yılan)"),
    BFS_TEMIZLIK("BFS ile Temizlik");

    private final String ad;

    Algoritma(String ad) {
        this.ad = ad;
    }

    public String getAd() { return ad; }
    @Override
    public String toString() { return ad; }
}
