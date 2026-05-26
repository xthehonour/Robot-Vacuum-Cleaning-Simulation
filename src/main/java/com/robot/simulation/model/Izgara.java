package com.robot.simulation.model;

import java.util.Random;

public class Izgara {
    private final int genislik;
    private final int yukseklik;
    private final Hucre[][] hucreler;

    public Izgara(int genislik, int yukseklik) {
        this.genislik = genislik;
        this.yukseklik = yukseklik;
        this.hucreler = new Hucre[genislik][yukseklik];

        for (int i = 0; i < genislik; i++) {
            for (int j = 0; j < yukseklik; j++) {
                hucreler[i][j] = new Hucre(i, j);
            }
        }
    }

    public void rastgeleOlustur(double engelOrani, double kirOrani) {
        Random random = new Random();
        for (int i = 0; i < genislik; i++) {
            for (int j = 0; j < yukseklik; j++) {
                Hucre hucre = hucreler[i][j];
                if (hucre.isSarjIstasyonu()) continue;

                if (random.nextDouble() < engelOrani) {
                    hucre.setEngel(true);
                } else if (random.nextDouble() < kirOrani) {
                    KirTipi[] tipler = KirTipi.values();
                    hucre.setKir(tipler[random.nextInt(tipler.length)]);
                }
            }
        }
    }

    public Hucre getHucre(int x, int y) {
        if (x >= 0 && x < genislik && y >= 0 && y < yukseklik) {
            return hucreler[x][y];
        }
        return null;
    }

    public int getGenislik() { return genislik; }
    public int getYukseklik() { return yukseklik; }
}
