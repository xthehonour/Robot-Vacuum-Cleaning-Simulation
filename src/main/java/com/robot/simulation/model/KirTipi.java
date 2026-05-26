package com.robot.simulation.model;

public enum KirTipi {
    TOZ("Toz", 1, 5.0),
    SIVI("Sıvı", 2, 10.0),
    LEKE("Leke", 3, 15.0);

    private final String ad;
    private final int temizlemeSuresi; // saniye (simülasyon adımı)
    private final double pilMaliyeti;

    KirTipi(String ad, int temizlemeSuresi, double pilMaliyeti) {
        this.ad = ad;
        this.temizlemeSuresi = temizlemeSuresi;
        this.pilMaliyeti = pilMaliyeti;
    }

    public String getAd() { return ad; }
    public int getTemizlemeSuresi() { return temizlemeSuresi; }
    public double getPilMaliyeti() { return pilMaliyeti; }
}
