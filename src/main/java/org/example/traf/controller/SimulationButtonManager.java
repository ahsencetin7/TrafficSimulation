package org.example.traf.controller;

import javafx.scene.control.Button;

public class SimulationButtonManager {
    private final Button startButton;
    private final Button stopButton;
    private final Button continueButton;
    private final SimulationController controller;

    public SimulationButtonManager(Button startButton, Button stopButton, Button continueButton,
                                   SimulationController controller) {
        this.startButton = startButton;
        this.stopButton = stopButton;
        this.continueButton = continueButton;
        this.controller = controller;

        attachButtonActions();
        updateButtonTextsAndStates();
    }

    private void attachButtonActions() {
        startButton.setOnAction(e -> handleStartButtonClick());

        stopButton.setOnAction(e -> {
            controller.stopCurrentSimulation();
            updateButtonTextsAndStates();
        });

        continueButton.setOnAction(e -> {
            controller.togglePauseResume();
            updateButtonTextsAndStates();
        });
    }

    private void handleStartButtonClick() {
        if (controller.isSimulationRunning() && controller.isSimulationPaused()) {
            controller.togglePauseResume();
        } else if (!controller.isSimulationRunning()) {
            controller.startNewSimulationFlow();
        }
        updateButtonTextsAndStates();
    }

    public void updateButtonTextsAndStates() {
        boolean isRunning = controller.isSimulationRunning();
        boolean isPaused = controller.isSimulationPaused();

        startButton.setDisable(false);
        stopButton.setDisable(false);
        continueButton.setDisable(false);

        updateButtonTexts(isRunning, isPaused);
    }

    private void updateButtonTexts(boolean isRunning, boolean isPaused) {
        if (isRunning && isPaused) {
            startButton.setText("▶ Devam Et");
        } else {
            startButton.setText("🚀 Yeni Simülasyon");
        }

        stopButton.setText("🔄 Resetle");

        if (isRunning) {
            if (isPaused) {
                continueButton.setText("▶ Devam Et");
            } else {
                continueButton.setText("⏸ Durdur");
            }
        } else {
            continueButton.setText("⏸ Durdur");
        }
    }

    public void forceUpdateButtonTexts() {
        updateButtonTextsAndStates();
    }
}
