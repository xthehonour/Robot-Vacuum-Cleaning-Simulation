package com.robot.simulation.view;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;

public final class JavaFxStyler {
    private static final String BUTTON_BASE_STYLE = """
            -fx-background-radius: 10;
            -fx-text-fill: white;
            -fx-font-weight: 700;
            -fx-padding: 10 12 10 12;
            -fx-cursor: hand;
            -fx-border-width: 1;
            -fx-border-color: rgba(255,255,255,0.08);
            """;
    private static final String ACTIVE_BUTTON_STYLE = """
            -fx-border-color: #f8fafc;
            -fx-border-width: 2;
            -fx-effect: dropshadow(gaussian, rgba(248,250,252,0.25), 12, 0.2, 0, 0);
            -fx-scale-x: 1.01;
            -fx-scale-y: 1.01;
            """;

    private JavaFxStyler() {
    }

    public static void applyTo(Node root) {
        styleNode(root);
    }

    public static void styleToolButton(Button button, boolean active) {
        button.setStyle(buttonStyle(button) + (active ? ACTIVE_BUTTON_STYLE : ""));
    }

    private static void styleNode(Node node) {
        if (node == null) {
            return;
        }

        String classes = String.join(" ", node.getStyleClass());
        applyClassStyle(node, classes);
        applyControlStyle(node, classes);

        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                styleNode(child);
            }
        }
    }

    private static void applyClassStyle(Node node, String classes) {
        if (classes.contains("app-root")) {
            node.setStyle("-fx-background-color: linear-gradient(to bottom right, #07111f, #102339);");
        } else if (classes.contains("panel-card") || classes.contains("board-card") || classes.contains("status-strip")) {
            node.setStyle(cardStyle(classes));
        } else if (classes.contains("active-hint")) {
            node.setStyle("-fx-background-color: rgba(37, 99, 235, 0.18); -fx-background-radius: 999; -fx-padding: 6 10 6 10; -fx-text-fill: #bfdbfe; -fx-font-size: 11px; -fx-font-weight: 600;");
        } else if (classes.contains("logo-label")) {
            node.setStyle("-fx-text-fill: #dbeafe; -fx-font-size: 28px;");
        } else if (classes.contains("title-label")) {
            node.setStyle("-fx-text-fill: white; -fx-font-size: 28px; -fx-font-weight: 700;");
        } else if (classes.contains("subtitle-label")) {
            node.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");
        } else if (classes.contains("header-badge")) {
            node.setStyle("-fx-background-color: rgba(148, 163, 184, 0.14); -fx-background-radius: 999; -fx-padding: 6 12 6 12; -fx-text-fill: #dbeafe; -fx-font-size: 11px;");
        } else if (classes.contains("card-title")) {
            node.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: 700;");
        } else if (classes.contains("field-label") || classes.contains("status-title")) {
            node.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");
        } else if (classes.contains("field-value")) {
            node.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: " + (classes.contains("compact") ? "12px" : "14px") + "; -fx-font-weight: 600;");
        } else if (classes.contains("status-value")) {
            node.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: 700;");
        } else if (classes.contains("status-card")) {
            node.setStyle("-fx-padding: 0 8 0 8; -fx-min-width: 140;");
        }
    }

    private static void applyControlStyle(Node node, String classes) {
        if (node instanceof Button button && classes.contains("tool-button")) {
            button.setStyle(buttonStyle(button));
        } else if (node instanceof ComboBox<?>) {
            node.setStyle("-fx-background-radius: 10; -fx-background-color: rgba(30, 41, 59, 0.85);");
        } else if (node instanceof ListView<?>) {
            node.setStyle("-fx-background-color: rgba(2, 6, 23, 0.45); -fx-control-inner-background: rgba(2, 6, 23, 0.65); -fx-background-insets: 0; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: rgba(148, 163, 184, 0.12);");
        } else if (node instanceof ProgressBar) {
            node.setStyle("-fx-accent: #22c55e;");
        } else if (node instanceof ScrollPane) {
            node.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        } else if (node instanceof Control) {
            node.setStyle(node.getStyle() + " -fx-background-radius: 10;");
        }
    }

    private static String cardStyle(String classes) {
        String padding = "";
        if (classes.contains("panel-card")) {
            padding = "-fx-padding: 14;";
        } else if (classes.contains("board-card")) {
            padding = "-fx-padding: 12;";
        } else if (classes.contains("status-strip")) {
            padding = "-fx-padding: 14;";
        }
        return "-fx-background-color: rgba(15, 23, 42, 0.78); -fx-background-radius: 16; -fx-border-color: rgba(148, 163, 184, 0.16); -fx-border-radius: 16; " + padding;
    }

    private static String buttonStyle(Button button) {
        String classes = String.join(" ", button.getStyleClass());
        String background = "linear-gradient(to bottom, #2563eb, #1d4ed8)";
        if (classes.contains("green")) {
            background = "linear-gradient(to bottom, #22c55e, #16a34a)";
        } else if (classes.contains("red")) {
            background = "linear-gradient(to bottom, #ef4444, #dc2626)";
        } else if (classes.contains("secondary")) {
            background = "linear-gradient(to bottom, #475569, #334155)";
        } else if (classes.contains("blue")) {
            background = "linear-gradient(to bottom, #0ea5e9, #0284c7)";
        }
        return BUTTON_BASE_STYLE + "-fx-background-color: " + background + ";";
    }
}
