package com.robot.simulation.controller;

import com.robot.simulation.model.CleaningAlgorithm;
import com.robot.simulation.model.DirtType;
import com.robot.simulation.model.GridPoint;
import com.robot.simulation.model.RoomGrid;
import com.robot.simulation.model.RoomType;
import com.robot.simulation.model.RobotVacuum;
import com.robot.simulation.model.VacuumSimulation;
import com.robot.simulation.view.CanvasBoardRenderer;
import com.robot.simulation.view.JavaFxStyler;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;

public class MainController {
    private enum EditMode {
        NONE,
        DIRT,
        OBSTACLE,
        ERASER
    }

    private static final List<String> VACUUM_SOUND_CANDIDATES = List.of(
            "/com/robot/simulation/audio/WhatsApp Audio 2026-06-02 at 17.17.49.wav",
            "/com/robot/simulation/audio/vacuum.mp3",
            "/com/robot/simulation/audio/vacuum.wav",
            "/com/robot/simulation/audio/supurge.mp3",
            "/com/robot/simulation/audio/supurge.wav"
    );

    @FXML private BorderPane root;
    @FXML private Canvas canvas;
    @FXML private ComboBox<CleaningAlgorithm> comboAlgorithm;
    @FXML private ComboBox<DirtType> comboDirtType;
    @FXML private Slider speedSlider;
    @FXML private Slider batterySlider;
    @FXML private ProgressBar batteryProgress;
    @FXML private Label batteryValueLabel;
    @FXML private Label batterySliderValueLabel;
    @FXML private Label positionLabel;
    @FXML private Label directionLabel;
    @FXML private Label cleanedAreaLabel;
    @FXML private Label dirtyAreaLabel;
    @FXML private Label elapsedTimeLabel;
    @FXML private Label modeLabel;
    @FXML private Label speedValueLabel;
    @FXML private Label algorithmLabel;
    @FXML private Label activeToolLabel;
    @FXML private Label totalAreaLabel;
    @FXML private Label statusDirtyLabel;
    @FXML private Label statusCleanedLabel;
    @FXML private Label statusRemainingLabel;
    @FXML private Label statusTimeLabel;
    @FXML private Label roomTitleLabel;
    @FXML private CheckBox soundEnabledCheckBox;
    @FXML private Slider volumeSlider;
    @FXML private Label volumeValueLabel;
    @FXML private ListView<String> eventLog;
    @FXML private Button dirtModeButton;
    @FXML private Button obstacleModeButton;
    @FXML private Button eraserModeButton;

    private final EnumMap<RoomType, VacuumSimulation> simulations = new EnumMap<>(RoomType.class);
    private CanvasBoardRenderer boardRenderer;
    private Timeline timeline;
    private MediaPlayer vacuumSoundPlayer;
    private EditMode editMode = EditMode.NONE;
    private RoomType activeRoom = RoomType.BATHROOM;
    private boolean syncingControls;

    @FXML
    public void initialize() {
        initializeRooms();
        boardRenderer = new CanvasBoardRenderer(canvas);
        JavaFxStyler.applyTo(root);
        initializeControls();
        initializeTimeline();
        initializeSound();
        registerEventHandlers();
        syncInitialControlValues();
        refresh();
    }

    @FXML
    private void startSimulation() {
        simulation().start();
        timeline.play();
        syncSoundPlayback();
        refresh();
    }

    @FXML
    private void pauseSimulation() {
        simulation().pause();
        timeline.pause();
        syncSoundPlayback();
        refresh();
    }

    @FXML
    private void resetSimulation() {
        timeline.stop();
        syncSoundPlayback();
        simulation().reset();
        syncRoomControls();
        editMode = EditMode.NONE;
        updateEditButtons();
        refresh();
    }

    @FXML
    private void returnToStation() {
        simulation().sendRobotToChargingStation(false);
        if (!simulation().isRunning()) {
            simulation().start();
        }
        timeline.play();
        refresh();
    }

    @FXML
    private void applyBattery() {
        simulation().applyBatteryLevel(batterySlider.getValue());
        refresh();
    }

    @FXML
    private void previousRoom() {
        changeRoom(activeRoom.previous());
    }

    @FXML
    private void nextRoom() {
        changeRoom(activeRoom.next());
    }

    @FXML
    private void activateDirtMode() {
        editMode = editMode == EditMode.DIRT ? EditMode.NONE : EditMode.DIRT;
        updateEditButtons();
    }

    @FXML
    private void activateObstacleMode() {
        editMode = editMode == EditMode.OBSTACLE ? EditMode.NONE : EditMode.OBSTACLE;
        updateEditButtons();
    }

    @FXML
    private void activateEraserMode() {
        editMode = editMode == EditMode.ERASER ? EditMode.NONE : EditMode.ERASER;
        updateEditButtons();
    }

    private void updateEditButtons() {
        updateModeButton(dirtModeButton, editMode == EditMode.DIRT);
        updateModeButton(obstacleModeButton, editMode == EditMode.OBSTACLE);
        updateModeButton(eraserModeButton, editMode == EditMode.ERASER);
        activeToolLabel.setText("Aktif arac: " + switch (editMode) {
            case DIRT -> "Kir Ekle";
            case OBSTACLE -> "Mobilya Ekle";
            case ERASER -> "Silgi";
            case NONE -> "Secili degil";
        });
    }

    private void updateModeButton(Button button, boolean active) {
        JavaFxStyler.styleToolButton(button, active);
    }

    private void initializeControls() {
        comboAlgorithm.getItems().setAll(CleaningAlgorithm.values());
        comboAlgorithm.setValue(simulation().getAlgorithm());

        comboDirtType.getItems().setAll(DirtType.values());
        comboDirtType.setValue(DirtType.DUST);
    }

    private void initializeRooms() {
        for (RoomType roomType : RoomType.values()) {
            simulations.put(roomType, new VacuumSimulation(roomType));
        }
    }

    private void initializeTimeline() {
        timeline = new Timeline(new KeyFrame(Duration.millis(180), event -> advanceSimulation()));
        timeline.setCycleCount(Timeline.INDEFINITE);
    }

    private void registerEventHandlers() {
        comboAlgorithm.valueProperty().addListener((obs, oldValue, newValue) -> changeAlgorithm(newValue));
        speedSlider.valueProperty().addListener((obs, oldValue, newValue) -> changeSpeed(newValue.doubleValue()));
        batterySlider.valueProperty().addListener((obs, oldValue, newValue) -> updateBatterySliderLabel(newValue.doubleValue()));
        soundEnabledCheckBox.selectedProperty().addListener((obs, oldValue, newValue) -> syncSoundPlayback());
        volumeSlider.valueProperty().addListener((obs, oldValue, newValue) -> changeVolume(newValue.doubleValue()));
        canvas.setOnMouseClicked(event -> editCellAt(event.getX(), event.getY()));
    }

    private void syncInitialControlValues() {
        batterySlider.setValue(simulation().getRobot().getBatteryLevel());
        speedValueLabel.setText(String.format(Locale.US, "%.1fx", speedSlider.getValue()));
    }

    private void advanceSimulation() {
        simulation().tick();
        refresh();
    }

    private void changeAlgorithm(CleaningAlgorithm algorithm) {
        if (algorithm == null || syncingControls) {
            return;
        }
        simulation().setAlgorithm(algorithm);
        refresh();
    }

    private void changeSpeed(double speed) {
        timeline.setRate(speed);
        speedValueLabel.setText(String.format(Locale.US, "%.1fx", speed));
    }

    private void changeVolume(double volume) {
        updateVolumeLabel(volume);
        if (vacuumSoundPlayer != null) {
            vacuumSoundPlayer.setVolume(volume / 100.0);
        }
    }

    private void updateBatterySliderLabel(double batteryLevel) {
        batterySliderValueLabel.setText("%" + String.format(Locale.US, "%.0f", batteryLevel));
    }

    private void updateVolumeLabel(double volume) {
        volumeValueLabel.setText("%" + String.format(Locale.US, "%.0f", volume));
    }

    private void editCellAt(double canvasX, double canvasY) {
        GridPoint point = boardRenderer.pointFromCanvas(simulation().getRoomGrid(), canvasX, canvasY);
        if (point == null) {
            return;
        }
        switch (editMode) {
            case DIRT -> simulation().addDirt(point, comboDirtType.getValue());
            case OBSTACLE -> simulation().addObstacle(point);
            case ERASER -> simulation().eraseAt(point);
            case NONE -> {
                return;
            }
        }
        refresh();
    }

    private void refresh() {
        drawScene();
        syncLabels();
        appendLogs();
        syncSoundPlayback();
    }

    private void syncLabels() {
        VacuumSimulation simulation = simulation();
        RobotVacuum robot = simulation.getRobot();
        RoomGrid grid = simulation.getRoomGrid();

        roomTitleLabel.setText(simulation.getRoomTitle());
        batteryProgress.setProgress(robot.getBatteryLevel() / RobotVacuum.MAX_BATTERY);
        batteryProgress.setStyle(robot.isBatteryLow() ? "-fx-accent: #f59e0b;" : "-fx-accent: #22c55e;");
        batteryValueLabel.setText("%" + String.format(Locale.US, "%.0f", robot.getBatteryLevel()));
        batterySliderValueLabel.setText("%" + String.format(Locale.US, "%.0f", batterySlider.getValue()));
        positionLabel.setText("(" + robot.getPosition().x() + ", " + robot.getPosition().y() + ")");
        directionLabel.setText(robot.getDirection().label());
        cleanedAreaLabel.setText(String.format(Locale.US, "%.0f%%", simulation.getCleanedPercent()));
        dirtyAreaLabel.setText(grid.getDirtyCellCount() + " hucre");
        elapsedTimeLabel.setText(simulation.getElapsedText());
        modeLabel.setText(simulation.getMode().getLabel());
        algorithmLabel.setText(comboAlgorithm.getValue() == null ? "-" : comboAlgorithm.getValue().toString());
        speedValueLabel.setText(String.format(Locale.US, "%.1fx", speedSlider.getValue()));
        totalAreaLabel.setText(grid.getWalkableCount() + " m2");
        statusDirtyLabel.setText(grid.getVisitedCount() + " m2 (" + String.format(Locale.US, "%.0f%%", simulation.getCleanedPercent()) + ")");
        statusCleanedLabel.setText(grid.getDirtyCellCount() + " hucre");
        statusRemainingLabel.setText(String.format(Locale.US, "%.0f%%", simulation.getRemainingDirtyPercent()));
        statusTimeLabel.setText(simulation.getElapsedText());
    }

    private void appendLogs() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        for (String log : simulation().drainLogs()) {
            eventLog.getItems().add(0, "[" + LocalTime.now().format(formatter) + "] " + log);
        }
        if (eventLog.getItems().size() > 200) {
            eventLog.getItems().remove(200, eventLog.getItems().size());
        }
    }

    private void drawScene() {
        boardRenderer.render(simulation());
    }

    private void changeRoom(RoomType roomType) {
        activeRoom = roomType;
        timeline.stop();
        syncRoomControls();
        eventLog.getItems().clear();
        editMode = EditMode.NONE;
        updateEditButtons();
        if (simulation().isRunning()) {
            timeline.play();
        }
        refresh();
    }

    private VacuumSimulation simulation() {
        return simulations.get(activeRoom);
    }

    private void initializeSound() {
        updateVolumeLabel(volumeSlider.getValue());

        URL soundUrl = findVacuumSoundUrl();
        if (soundUrl == null) {
            soundEnabledCheckBox.setSelected(false);
            soundEnabledCheckBox.setDisable(true);
            volumeSlider.setDisable(true);
            volumeValueLabel.setText("Dosya yok");
            return;
        }

        vacuumSoundPlayer = new MediaPlayer(new Media(soundUrl.toExternalForm()));
        vacuumSoundPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        vacuumSoundPlayer.setVolume(volumeSlider.getValue() / 100.0);
    }

    private URL findVacuumSoundUrl() {
        for (String resourcePath : VACUUM_SOUND_CANDIDATES) {
            URL url = MainController.class.getResource(resourcePath);
            if (url != null) {
                return url;
            }
        }
        return null;
    }

    private void syncSoundPlayback() {
        if (vacuumSoundPlayer == null) {
            return;
        }

        boolean shouldPlay = simulation().isRunning() && soundEnabledCheckBox.isSelected();
        if (shouldPlay && vacuumSoundPlayer.getStatus() != MediaPlayer.Status.PLAYING) {
            vacuumSoundPlayer.play();
        } else if (!shouldPlay && vacuumSoundPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            vacuumSoundPlayer.stop();
        }
    }

    private void syncRoomControls() {
        syncingControls = true;
        comboAlgorithm.setValue(simulation().getAlgorithm());
        batterySlider.setValue(simulation().getRobot().getBatteryLevel());
        syncingControls = false;
    }
}
