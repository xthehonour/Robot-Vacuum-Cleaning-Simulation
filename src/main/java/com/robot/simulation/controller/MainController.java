package com.robot.simulation.controller;

import com.robot.simulation.model.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class MainController {

    @FXML private Canvas canvas;
    @FXML private Button btnBaslat;
    @FXML private Button btnDurdur;
    @FXML private Button btnSifirla;
    @FXML private ComboBox<Algoritma> comboAlgoritma;
    @FXML private Slider sliderHiz;
    @FXML private ProgressBar progressPil;
    @FXML private Label lblPil;
    @FXML private ListView<String> listLog;
    @FXML private Spinner<Integer> spinGenislik;
    @FXML private Spinner<Integer> spinYukseklik;

    private Izgara izgara;
    private Robot robot;
    private Timeline timeline;
    private final int hucreBoyutu = 30;
    private final Random random = new Random();
    private boolean sarjaGidiyor = false;
    private List<Hucre> aktifYol = new ArrayList<>();

    @FXML
    public void initialize() {
        comboAlgoritma.getItems().setAll(Algoritma.values());
        comboAlgoritma.getSelectionModel().select(Algoritma.RASTGELE);

        spinGenislik.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 50, 20));
        spinYukseklik.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 50, 15));

        sifirla();

        timeline = new Timeline(new KeyFrame(Duration.millis(500), e -> adimAt()));
        timeline.setCycleCount(Timeline.INDEFINITE);

        sliderHiz.valueProperty().addListener((obs, oldVal, newVal) -> {
            timeline.setRate(newVal.doubleValue());
        });
    }

    @FXML
    public void baslat() {
        timeline.play();
        log("Simülasyon başlatıldı.");
    }

    @FXML
    public void durdur() {
        timeline.pause();
        log("Simülasyon durduruldu.");
    }

    @FXML
    public void sifirla() {
        timeline.stop();
        int g = spinGenislik.getValue();
        int y = spinYukseklik.getValue();
        izgara = new Izgara(g, y);
        Hucre sarjIstasyonu = izgara.getHucre(0, 0);
        sarjIstasyonu.setSarjIstasyonu(true);
        izgara.rastgeleOlustur(0.1, 0.2);

        robot = new Robot(0, 0, 100.0);
        sarjaGidiyor = false;
        aktifYol.clear();

        ciz();
        guncelleUI();
        listLog.getItems().clear();
        log("Simülasyon sıfırlandı.");
    }

    private void adimAt() {
        if (robot.getPil() <= 20 && !sarjaGidiyor) {
            log("Pil düşük! Şarj istasyonuna dönülüyor...");
            sarjaGidiyor = true;
            Hucre hedef = izgara.getHucre(0, 0);
            aktifYol = YolBulucu.bfs(izgara, izgara.getHucre(robot.getX(), robot.getY()), hedef);
        }

        if (sarjaGidiyor) {
            sarjAdimi();
        } else {
            temizlikAdimi();
        }

        ciz();
        guncelleUI();
    }

    private void sarjAdimi() {
        if (!aktifYol.isEmpty()) {
            Hucre sonraki = aktifYol.remove(0);
            if (sonraki.getX() == robot.getX() && sonraki.getY() == robot.getY() && !aktifYol.isEmpty()) {
                sonraki = aktifYol.remove(0);
            }
            robot.hareketEt(sonraki.getX(), sonraki.getY());
            if (sonraki.isSarjIstasyonu()) {
                robot.sarjEt();
                sarjaGidiyor = false;
                log("Şarj tamamlandı.");
            }
        }
    }

    private void temizlikAdimi() {
        Hucre mevcutHucre = izgara.getHucre(robot.getX(), robot.getY());
        if (mevcutHucre.isKirli()) {
            KirTipi kir = mevcutHucre.getKir();
            robot.temizle(kir);
            mevcutHucre.setTemizlendi(true);
            log(kir.getAd() + " temizlendi. Kalan Pil: %" + String.format("%.1f", robot.getPil()));
            return;
        }

        Algoritma secili = comboAlgoritma.getValue();
        if (secili == Algoritma.RASTGELE) {
            rastgeleHareket();
        } else if (secili == Algoritma.ZIGZAG) {
            zigzagHareket();
        } else {
            bfsTemizlikHareketi();
        }
    }

    private void rastgeleHareket() {
        int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
        List<int[]> validMoves = new ArrayList<>();
        for (int[] d : directions) {
            Hucre komsu = izgara.getHucre(robot.getX() + d[0], robot.getY() + d[1]);
            if (komsu != null && !komsu.isEngel()) {
                validMoves.add(d);
            }
        }
        if (!validMoves.isEmpty()) {
            int[] move = validMoves.get(random.nextInt(validMoves.size()));
            robot.hareketEt(robot.getX() + move[0], robot.getY() + move[1]);
        }
    }

    private void zigzagHareket() {
        int x = robot.getX();
        int y = robot.getY();

        // Basit bir zigzag mantığı (S-şeklinde tarama)
        if (x % 2 == 0) { // Aşağı doğru git
            if (y + 1 < izgara.getYukseklik() && !izgara.getHucre(x, y + 1).isEngel()) {
                robot.hareketEt(x, y + 1);
            } else { // Sağa geç
                if (x + 1 < izgara.getGenislik() && !izgara.getHucre(x + 1, y).isEngel()) {
                    robot.hareketEt(x + 1, y);
                } else {
                    rastgeleHareket(); // Sıkışırsa rastgele
                }
            }
        } else { // Yukarı doğru git
            if (y - 1 >= 0 && !izgara.getHucre(x, y - 1).isEngel()) {
                robot.hareketEt(x, y - 1);
            } else { // Sağa geç
                if (x + 1 < izgara.getGenislik() && !izgara.getHucre(x + 1, y).isEngel()) {
                    robot.hareketEt(x + 1, y);
                } else {
                    rastgeleHareket();
                }
            }
        }
    }

    private void bfsTemizlikHareketi() {
        if (aktifYol.isEmpty()) {
            // En yakın kirli hücreyi bul
            Hucre hedef = enYakinKirliHucreyiBul();
            if (hedef != null) {
                aktifYol = YolBulucu.bfs(izgara, izgara.getHucre(robot.getX(), robot.getY()), hedef);
            } else {
                rastgeleHareket();
                return;
            }
        }

        if (!aktifYol.isEmpty()) {
            Hucre sonraki = aktifYol.remove(0);
            if (sonraki.getX() == robot.getX() && sonraki.getY() == robot.getY() && !aktifYol.isEmpty()) {
                sonraki = aktifYol.remove(0);
            }
            robot.hareketEt(sonraki.getX(), sonraki.getY());
        }
    }

    private Hucre enYakinKirliHucreyiBul() {
        // Basit BFS ile en yakın kirliyi bulma
        Queue<Hucre> queue = new java.util.LinkedList<>();
        Set<Hucre> visited = new java.util.HashSet<>();
        Hucre start = izgara.getHucre(robot.getX(), robot.getY());

        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            Hucre curr = queue.poll();
            if (curr.isKirli()) return curr;

            int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
            for (int[] d : directions) {
                Hucre komsu = izgara.getHucre(curr.getX() + d[0], curr.getY() + d[1]);
                if (komsu != null && !visited.contains(komsu) && !komsu.isEngel()) {
                    visited.add(komsu);
                    queue.add(komsu);
                }
            }
        }
        return null;
    }

    private void ciz() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        for (int i = 0; i < izgara.getGenislik(); i++) {
            for (int j = 0; j < izgara.getYukseklik(); j++) {
                Hucre hucre = izgara.getHucre(i, j);

                if (hucre.isEngel()) {
                    gc.setFill(Color.BLACK);
                } else if (hucre.isSarjIstasyonu()) {
                    gc.setFill(Color.YELLOW);
                } else if (hucre.isKirli()) {
                    switch (hucre.getKir()) {
                        case TOZ -> gc.setFill(Color.LIGHTPINK);
                        case SIVI -> gc.setFill(Color.CYAN);
                        case LEKE -> gc.setFill(Color.SADDLEBROWN);
                    }
                } else if (hucre.isTemizlendi()) {
                    gc.setFill(Color.LIGHTGREEN);
                } else {
                    gc.setFill(Color.WHITE);
                }

                gc.fillRect(i * hucreBoyutu, j * hucreBoyutu, hucreBoyutu - 1, hucreBoyutu - 1);
                gc.setStroke(Color.LIGHTGRAY);
                gc.strokeRect(i * hucreBoyutu, j * hucreBoyutu, hucreBoyutu, hucreBoyutu);
            }
        }

        // Robotu çiz
        gc.setFill(Color.BLUE);
        gc.fillOval(robot.getX() * hucreBoyutu + 2, robot.getY() * hucreBoyutu + 2, hucreBoyutu - 4, hucreBoyutu - 4);
    }

    private void guncelleUI() {
        double pilYuzde = robot.getPil() / robot.getMaxPil();
        progressPil.setProgress(pilYuzde);
        lblPil.setText("%" + String.format("%.1f", robot.getPil()));

        if (pilYuzde < 0.2) {
            progressPil.setStyle("-fx-accent: red;");
        } else {
            progressPil.setStyle("-fx-accent: green;");
        }
    }

    private void log(String mesaj) {
        String zaman = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        Platform.runLater(() -> {
            listLog.getItems().add(0, "[" + zaman + "] " + mesaj);
        });
    }
}
