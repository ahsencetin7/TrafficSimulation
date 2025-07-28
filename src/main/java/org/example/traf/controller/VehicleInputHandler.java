package org.example.traf.controller;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

import org.example.traf.util.Constants;

public class VehicleInputHandler {

    private final Stage primaryStage;
    private final Consumer<InputResult> onInputProcessed;

    public static class InputResult {
        public final Map<String, Integer> greenDurations;
        public final Map<String, Integer> vehicleCounts;

        public InputResult(Map<String, Integer> greenDurations, Map<String, Integer> vehicleCounts) {
            this.greenDurations = greenDurations;
            this.vehicleCounts = vehicleCounts;
        }
    }

    public VehicleInputHandler(Stage primaryStage, Consumer<InputResult> onInputProcessed) {
        this.primaryStage = primaryStage;
        this.onInputProcessed = onInputProcessed;
    }

    public void showVehicleCountDialog() {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initOwner(primaryStage);
        dialogStage.setTitle("Araç Sayısı Ayarları");

        Pane dialogPane = createVehicleInputDialog(dialogStage);
        Scene dialogScene = new Scene(dialogPane, 350, 280);
        dialogStage.setScene(dialogScene);
        dialogStage.setResizable(false);
        dialogStage.show();
    }

    private Pane createVehicleInputDialog(Stage dialogStage) {
        Pane dialogPane = new Pane();
        dialogPane.setPrefSize(400, 320);

        Map<String, TextField> inputs = new LinkedHashMap<>();
        double yStart = 30;

        Label titleLabel = new Label("Her yön için araç sayısını giriniz:");
        titleLabel.setLayoutX(20);
        titleLabel.setLayoutY(10);
        titleLabel.setStyle("-fx-font-weight: bold;");
        dialogPane.getChildren().add(titleLabel);

        for (int i = 0; i < Constants.DIRECTIONS_TURKISH.length; i++) {
            String direction = Constants.DIRECTIONS_TURKISH[i];
            Label label = new Label(direction + " yönü:");
            label.setLayoutX(30);
            label.setLayoutY(yStart + i * 35);

            TextField textField = new TextField("10");
            textField.setLayoutX(120);
            textField.setLayoutY(yStart + i * 35 - 3);
            textField.setPrefWidth(80);

            textField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal.matches("\\d*")) {
                    textField.setText(newVal.replaceAll("[^\\d]", ""));
                }
                if (!newVal.isEmpty()) {
                    try {
                        int value = Integer.parseInt(newVal);
                        if (value > 50) {
                            textField.setText("50");
                        }
                    } catch (NumberFormatException e) {
                        textField.setText("");
                    }
                }
            });

            dialogPane.getChildren().addAll(label, textField);
            inputs.put(direction, textField);
        }

        double buttonY = yStart + Constants.DIRECTIONS_TURKISH.length * 35 + 20;
        double buttonWidth = 160;
        double spacing = 10;

        Button randomButton = new Button("Rastgele Araç Sayıları");
        randomButton.setLayoutX(30);
        randomButton.setLayoutY(buttonY);
        randomButton.setPrefWidth(buttonWidth);
        randomButton.setStyle("-fx-font-weight: bold; -fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 5px;");
        randomButton.setOnAction(e -> {
            Random random = new Random();
            for (TextField textField : inputs.values()) {
                textField.setText(String.valueOf(random.nextInt(30) + 5));
            }
        });

        Button startSimulationButton = new Button("Simülasyonu Başlat");
        startSimulationButton.setLayoutX(30 + buttonWidth + spacing);
        startSimulationButton.setLayoutY(buttonY);
        startSimulationButton.setPrefWidth(buttonWidth);
        startSimulationButton.setStyle("-fx-font-weight: bold; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 5px;");
        startSimulationButton.setOnAction(e -> {
            processAndReturnResults(inputs, dialogStage);
        });

        dialogPane.getChildren().addAll(startSimulationButton, randomButton);
        return dialogPane;
    }

    private void processAndReturnResults(Map<String, TextField> inputs, Stage dialogStage) {
        Map<String, Integer> greenDurations = new LinkedHashMap<>();
        Map<String, Integer> vehicleCounts = new HashMap<>();
        int totalVehicles = 0;

        for (String direction : Constants.DIRECTIONS_TURKISH) {
            TextField textField = inputs.get(direction);
            int count = 0;
            try {
                String text = textField.getText();
                if (!text.isEmpty()) {
                    count = Math.max(1, Math.min(50, Integer.parseInt(text)));
                }
            } catch (NumberFormatException e) {
                count = 10;
            }
            vehicleCounts.put(direction, count);
            totalVehicles += count;
        }

        if (totalVehicles == 0) totalVehicles = 40;

        int totalYellowTime = (Constants.DIRECTIONS_TURKISH.length - 1) * Constants.YELLOW_DURATION;
        int availableGreenTime = Constants.TOTAL_SIMULATION_TIME - totalYellowTime;

        for (String direction : Constants.DIRECTIONS_TURKISH) {
            int vehicleCount = vehicleCounts.get(direction);
            double ratio = (double) vehicleCount / totalVehicles;
            int greenTime = (int) Math.round(ratio * availableGreenTime);
            greenTime = Math.max(Constants.MIN_GREEN_DURATION, Math.min(Constants.MAX_GREEN_DURATION, greenTime));
            greenDurations.put(direction, greenTime);
        }

        InputResult result = new InputResult(greenDurations, vehicleCounts);
        dialogStage.close();
        onInputProcessed.accept(result);
    }

    public InputResult getDefaultValues() {
        Map<String, Integer> defaultGreenDurations = new LinkedHashMap<>();
        Map<String, Integer> defaultVehicleCounts = new LinkedHashMap<>();

        for (String direction : Constants.DIRECTIONS_TURKISH) {
            defaultGreenDurations.put(direction, 15);
            defaultVehicleCounts.put(direction, 10);
        }

        return new InputResult(defaultGreenDurations, defaultVehicleCounts);
    }
}
