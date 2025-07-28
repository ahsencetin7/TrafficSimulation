package org.example.traf.view;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import org.example.traf.util.Constants;

public class UIBuilder {

    private final Pane root;
    private Label totalCountdownLabel;
    private Button startButton;
    private Button stopButton;
    private Button continueButton;

    public UIBuilder(Pane root) {
        this.root = root;
        root.setStyle("-fx-background-color: #D3D3D3;");
        drawIntersection();
        setupCountdownDisplay();
        setupControlButtons();
    }

    private void drawIntersection() {
        double centerX = Constants.INTERSECTION_WIDTH / 2;
        double centerY = Constants.INTERSECTION_HEIGHT / 2;

        Rectangle verticalRoad = new Rectangle(centerX - Constants.STREET_WIDTH, 0, Constants.ROAD_WIDTH, Constants.INTERSECTION_HEIGHT);
        verticalRoad.setFill(Color.DARKGRAY);

        Rectangle horizontalRoad = new Rectangle(0, centerY - Constants.STREET_WIDTH, Constants.INTERSECTION_WIDTH, Constants.ROAD_WIDTH);
        horizontalRoad.setFill(Color.DARKGRAY);

        Line verticalLine = new Line(centerX, 0, centerX, Constants.INTERSECTION_HEIGHT);
        verticalLine.setStroke(Color.WHITE);
        verticalLine.setStrokeWidth(2);
        verticalLine.getStrokeDashArray().addAll(15.0, 10.0);

        Line horizontalLine = new Line(0, centerY, Constants.INTERSECTION_WIDTH, centerY);
        horizontalLine.setStroke(Color.WHITE);
        horizontalLine.setStrokeWidth(2);
        horizontalLine.getStrokeDashArray().addAll(15.0, 10.0);

        root.getChildren().addAll(verticalRoad, horizontalRoad, verticalLine, horizontalLine);
    }

    private void setupCountdownDisplay() {
        totalCountdownLabel = new Label(String.valueOf(Constants.TOTAL_SIMULATION_TIME));
        totalCountdownLabel.setStyle(
                "-fx-font-size: " + Constants.TOTAL_COUNTDOWN_LABEL_FONT_SIZE + "px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-color: white;" +
                        "-fx-border-color: black;" +
                        "-fx-border-width: 2px;" +
                        "-fx-padding: 10px;" +
                        "-fx-background-radius: 5px;"
        );
        totalCountdownLabel.setLayoutX(Constants.INTERSECTION_WIDTH - 150);
        totalCountdownLabel.setLayoutY(15);
        root.getChildren().add(totalCountdownLabel);
    }

    private void setupControlButtons() {
        startButton = new Button("Başlat");
        stopButton = new Button("Durdur");
        continueButton = new Button("Devam Et");

        String buttonStyle = "-fx-font-weight: bold; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10px; -fx-background-radius: 5px;";
        startButton.setStyle(buttonStyle);
        stopButton.setStyle(buttonStyle.replace("#4CAF50", "#f44336"));
        continueButton.setStyle(buttonStyle.replace("#4CAF50", "#2196F3"));

        double buttonX = 150;
        double buttonY = 60;
        double buttonSpacing = 10;

        startButton.setLayoutX(buttonX);
        startButton.setLayoutY(buttonY);

        stopButton.setLayoutX(buttonX + 110);
        stopButton.setLayoutY(buttonY);

        continueButton.setLayoutX(buttonX + 220);
        continueButton.setLayoutY(buttonY);

        startButton.setPrefWidth(100);
        stopButton.setPrefWidth(100);
        continueButton.setPrefWidth(100);

        stopButton.setDisable(true);
        continueButton.setDisable(true);

        root.getChildren().addAll(startButton, stopButton, continueButton);
    }

    public Label getTotalCountdownLabel() {
        return totalCountdownLabel;
    }

    public Button getStartButton() {
        return startButton;
    }

    public Button getStopButton() {
        return stopButton;
    }

    public Button getContinueButton() {
        return continueButton;
    }
}
