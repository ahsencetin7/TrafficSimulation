package org.example.traf.view;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.example.traf.controller.SimulationButtonManager;
import org.example.traf.controller.SimulationController;
import org.example.traf.controller.TrafficController;
import org.example.traf.controller.VehicleInputHandler;
import org.example.traf.model.TrafficLightSystem;
import org.example.traf.model.VehicleManager;
import org.example.traf.util.Constants;

import java.util.Map;

public class SimulationApp extends Application implements SimulationController {

    // UI bileşenleri
    private Pane root;
    private UIBuilder uiBuilder;
    private Stage primaryStage;

    // Simülasyon bileşenleri
    private VehicleInputHandler vehicleInputHandler;
    private TrafficLightSystem trafficLightSystem;
    private VehicleManager vehicleManager;
    private TrafficController trafficController;
    private SimulationButtonManager buttonManager;

    // Durum değişkenleri
    private Map<String, Integer> currentVehicleCounts;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        try {
            initializeUI(stage);
            initializeComponents();
        } catch (Exception e) {
            handleStartupError();
        }
    }

    private void initializeUI(Stage stage) {
        root = new Pane();
        uiBuilder = new UIBuilder(root);

        Scene scene = new Scene(root, Constants.INTERSECTION_WIDTH, Constants.INTERSECTION_HEIGHT);
        stage.setScene(scene);
        stage.setTitle("Kavşak Simülasyonu");
        stage.setResizable(false);
        stage.setOnCloseRequest(event -> cleanupAndExit());
        stage.show();
    }

    private void initializeComponents() {
        vehicleInputHandler = new VehicleInputHandler(primaryStage, this::onInputResultReceived);
        buttonManager = new SimulationButtonManager(
                uiBuilder.getStartButton(),
                uiBuilder.getStopButton(),
                uiBuilder.getContinueButton(),
                this
        );
    }

    private void handleStartupError() {
        Platform.runLater(() -> {
            try {
                VehicleInputHandler.InputResult defaultResult = vehicleInputHandler.getDefaultValues();
                onInputResultReceived(defaultResult);
            } catch (Exception ignored) {
            }
        });
    }

    private void onInputResultReceived(VehicleInputHandler.InputResult inputResult) {
        try {
            cleanupPreviousSimulation();

            Map<String, Integer> greenDurations = inputResult.greenDurations;
            Map<String, Integer> vehicleCounts = inputResult.vehicleCounts;

            this.currentVehicleCounts = vehicleCounts;

            initializeSimulationComponents(greenDurations, vehicleCounts);
            startSimulation();

            if (buttonManager != null) {
                buttonManager.forceUpdateButtonTexts();
            }
        } catch (Exception ignored) {
        }
    }

    private void cleanupPreviousSimulation() {
        if (trafficController != null) {
            trafficController.stopSimulationCompletely();
            trafficController = null;
        }
        currentVehicleCounts = null;
    }

    private void initializeSimulationComponents(Map<String, Integer> greenDurations,
                                                Map<String, Integer> vehicleCounts) {
        trafficLightSystem = new TrafficLightSystem(
                root,
                Constants.INTERSECTION_WIDTH,
                Constants.INTERSECTION_HEIGHT,
                greenDurations
        );

        vehicleManager = new VehicleManager(root, trafficLightSystem);

        trafficController = new TrafficController(
                trafficLightSystem,
                vehicleManager,
                uiBuilder.getTotalCountdownLabel(),
                greenDurations,
                vehicleCounts
        );
    }

    private void startSimulation() {
        if (trafficController != null) {
            trafficController.startSimulation();
        }
    }

    // ======================== SimulationController Interface Implementasyonları ========================

    @Override
    public void startNewSimulationFlow() {
        try {
            if (trafficController != null && trafficController.isSimulationRunning()) {
                if (trafficController.isSimulationPaused()) {
                    trafficController.resumeSimulation();
                    if (buttonManager != null) {
                        buttonManager.forceUpdateButtonTexts();
                    }
                    return;
                }
                return;
            }

            vehicleInputHandler.showVehicleCountDialog();
        } catch (Exception ignored) {
        }
    }

    @Override
    public void stopCurrentSimulation() {
        try {
            if (trafficController == null || !trafficController.isSimulationRunning()) {
                return;
            }

            trafficController.stopSimulationCompletely();
            trafficController = null;
            currentVehicleCounts = null;

            if (buttonManager != null) {
                buttonManager.forceUpdateButtonTexts();
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void togglePauseResume() {
        try {
            if (trafficController == null || !trafficController.isSimulationRunning()) {
                return;
            }

            if (trafficController.isSimulationPaused()) {
                trafficController.resumeSimulation();
            } else {
                trafficController.pauseSimulation();
            }

            if (buttonManager != null) {
                buttonManager.forceUpdateButtonTexts();
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public boolean isSimulationRunning() {
        return trafficController != null && trafficController.isSimulationRunning();
    }

    @Override
    public boolean isSimulationPaused() {
        return trafficController != null && trafficController.isSimulationPaused();
    }

    public String getSimulationStatus() {
        if (trafficController == null) {
            return "HAZIR";
        }
        return trafficController.getSimulationStatus();
    }

    public Map<String, Integer> getCurrentVehicleCounts() {
        return currentVehicleCounts;
    }

    private void cleanupAndExit() {
        try {
            if (trafficController != null) {
                trafficController.stopSimulationCompletely();
            }
        } catch (Exception ignored) {
        } finally {
            Platform.exit();
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
