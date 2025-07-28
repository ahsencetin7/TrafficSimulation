package org.example.traf.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.util.Duration;
import org.example.traf.model.TrafficLightSystem;
import org.example.traf.model.VehicleManager;
import org.example.traf.util.Constants;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class TrafficController {
    private final TrafficLightSystem trafficLightSystem;
    private final VehicleManager vehicleManager;
    private final Map<String, Integer> greenDurations;
    private final Label totalCountdownLabel;
    private final Map<String, Integer> vehicleCounts;
    private Timeline totalSimulationTimeline;
    private final AtomicBoolean isSimulationRunning = new AtomicBoolean(false);
    private final AtomicBoolean isSimulationPaused = new AtomicBoolean(false);
    private final AtomicBoolean isPhaseTransitioning = new AtomicBoolean(false);
    private final AtomicBoolean isSimulationInitialized = new AtomicBoolean(false);
    private AtomicInteger totalTimeRemaining;
    private Queue<String> directionQueue;
    private String currentDirection = "";
    private Timeline pauseCheckTimeline;

    public TrafficController(TrafficLightSystem trafficLightSystem, VehicleManager vehicleManager,
                             Label totalCountdownLabel, Map<String, Integer> greenDurations, Map<String, Integer> vehicleCounts) {
        this.trafficLightSystem = trafficLightSystem;
        this.vehicleManager = vehicleManager;
        this.totalCountdownLabel = totalCountdownLabel;
        this.greenDurations = greenDurations;
        this.vehicleCounts = vehicleCounts;
    }

    public void startSimulation() {
        Platform.runLater(() -> {
            try {
                if (isSimulationInitialized.get()) {
                    if (isSimulationPaused.get() && isSimulationRunning.get()) {
                        resumeSimulation();
                        return;
                    } else if (isSimulationRunning.get()) {
                        return;
                    }
                }
                initializeSimulation();
            } catch (Exception e) {
                e.printStackTrace();
                isSimulationRunning.set(false);
                isSimulationPaused.set(false);
            }
        });
    }

    private void initializeSimulation() {
        isSimulationRunning.set(true);
        isSimulationPaused.set(false);
        isSimulationInitialized.set(true);
        totalTimeRemaining = new AtomicInteger(Constants.TOTAL_SIMULATION_TIME);
        startTotalCountdownTimer();
        initializeDirectionQueue();
        startVehicleAnimations();
        startTrafficLightCycle();
    }

    private void startTotalCountdownTimer() {
        if (totalSimulationTimeline != null) {
            totalSimulationTimeline.stop();
        }
        totalCountdownLabel.setText(String.valueOf(totalTimeRemaining.get()));
        totalSimulationTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            if (isSimulationPaused.get()) return;
            int remaining = totalTimeRemaining.decrementAndGet();
            Platform.runLater(() -> totalCountdownLabel.setText(String.valueOf(remaining)));
            if (remaining <= 0) {
                Platform.runLater(this::endSimulation);
            }
        }));
        totalSimulationTimeline.setCycleCount(Timeline.INDEFINITE);
        totalSimulationTimeline.play();
    }

    private void initializeDirectionQueue() {
        List<String> shuffledDirections = Arrays.asList(Constants.DIRECTIONS_TURKISH);
        Collections.shuffle(shuffledDirections);
        directionQueue = new LinkedList<>(shuffledDirections);
    }

    private void startVehicleAnimations() {
        for (String direction : Constants.DIRECTIONS_TURKISH) {
            int carCount = vehicleCounts.getOrDefault(direction, 10);
            vehicleManager.spawnVehicles(direction, carCount);
        }
    }

    private void startTrafficLightCycle() {
        processNextDirection(true);
    }

    private synchronized void processNextDirection(boolean isFirstPhase) {
        if (!isSimulationRunning.get() || directionQueue.isEmpty() || totalTimeRemaining.get() <= 0) return;
        if (isSimulationPaused.get()) {
            waitForResume(() -> processNextDirection(isFirstPhase));
            return;
        }
        if (isPhaseTransitioning.get()) return;
        isPhaseTransitioning.set(true);

        String nextDirection = directionQueue.poll();
        directionQueue.offer(nextDirection);
        currentDirection = nextDirection;

        Runnable onPhaseComplete = () -> {
            isPhaseTransitioning.set(false);
            if (isSimulationPaused.get()) {
                waitForResume(() -> {
                    if (trafficLightSystem.isGreen(currentDirection)) {
                        vehicleManager.processQueueMovement(currentDirection);
                    }
                    continueToNextPhase();
                });
                return;
            }
            if (trafficLightSystem.isGreen(currentDirection)) {
                vehicleManager.processQueueMovement(currentDirection);
            }
            continueToNextPhase();
        };

        trafficLightSystem.startLightCycle(currentDirection, isFirstPhase, onPhaseComplete);
    }

    private void waitForResume(Runnable actionAfterResume) {
        if (pauseCheckTimeline != null) {
            pauseCheckTimeline.stop();
        }
        pauseCheckTimeline = new Timeline(new KeyFrame(Duration.millis(100), e -> {
            if (!isSimulationPaused.get() && isSimulationRunning.get()) {
                pauseCheckTimeline.stop();
                pauseCheckTimeline = null;
                Platform.runLater(actionAfterResume);
            } else if (!isSimulationRunning.get()) {
                pauseCheckTimeline.stop();
                pauseCheckTimeline = null;
            }
        }));
        pauseCheckTimeline.setCycleCount(Timeline.INDEFINITE);
        pauseCheckTimeline.play();
    }

    private void continueToNextPhase() {
        Platform.runLater(() -> {
            if (isSimulationRunning.get() && totalTimeRemaining.get() > 0 && !isSimulationPaused.get()) {
                processNextDirection(false);
            }
        });
    }

    public void endSimulation() {
        if (!isSimulationRunning.compareAndSet(true, false)) return;
        isSimulationPaused.set(false);
        isSimulationInitialized.set(false);
        cleanupResources();
        Platform.runLater(() -> {
            trafficLightSystem.resetLights();
            totalCountdownLabel.setText("0");
        });
    }

    private void cleanupResources() {
        if (totalSimulationTimeline != null) {
            totalSimulationTimeline.stop();
            totalSimulationTimeline = null;
        }
        if (pauseCheckTimeline != null) {
            pauseCheckTimeline.stop();
            pauseCheckTimeline = null;
        }
        trafficLightSystem.stopCurrentPhaseTimeline();
        vehicleManager.stopAllVehicles();
        isPhaseTransitioning.set(false);
    }

    public void pauseSimulation() {
        if (!isSimulationRunning.get() || isSimulationPaused.get()) return;
        isSimulationPaused.set(true);
        if (totalSimulationTimeline != null && totalSimulationTimeline.getStatus() == Timeline.Status.RUNNING) {
            totalSimulationTimeline.pause();
        }
        trafficLightSystem.pauseCurrentPhaseTimeline();
        vehicleManager.pauseAllVehiclesControlled();
        vehicleManager.pauseAllBackgroundProcesses();
    }

    public void resumeSimulation() {
        if (!isSimulationRunning.get() || !isSimulationPaused.get()) return;
        isSimulationPaused.set(false);
        if (totalSimulationTimeline != null && totalSimulationTimeline.getStatus() == Timeline.Status.PAUSED) {
            totalSimulationTimeline.play();
        }
        trafficLightSystem.resumeCurrentPhaseTimeline();
        vehicleManager.resumeAllBackgroundProcesses();
        vehicleManager.resumeAllVehiclesControlled();
    }

    public void stopSimulationCompletely() {
        endSimulation();
        Platform.runLater(() -> vehicleManager.clearAllVehiclesFromScreen());
        isSimulationInitialized.set(false);
    }

    public boolean canStart() {
        return !isSimulationRunning.get() || (isSimulationRunning.get() && isSimulationPaused.get());
    }

    public boolean canPause() {
        return isSimulationRunning.get() && !isSimulationPaused.get();
    }

    public boolean canStop() {
        return isSimulationRunning.get();
    }

    public boolean isSimulationRunning() {
        return isSimulationRunning.get();
    }

    public boolean isSimulationPaused() {
        return isSimulationPaused.get();
    }

    public String getSimulationStatus() {
        if (!isSimulationRunning.get()) return "DURDURULDU";
        if (isSimulationPaused.get()) return "DURAKLATILDI";
        return "ÇALIŞIYOR";
    }
}
