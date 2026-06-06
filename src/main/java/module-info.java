module com.robot.simulation {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

    opens com.robot.simulation to javafx.fxml;
    opens com.robot.simulation.controller to javafx.fxml;
    exports com.robot.simulation;
    exports com.robot.simulation.model;
    exports com.robot.simulation.controller;
}
