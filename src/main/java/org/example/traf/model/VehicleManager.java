package org.example.traf.model;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.PathTransition;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Path;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.example.traf.util.Constants;

public class VehicleManager {
    private final Pane rootPane;
    private final TrafficLightSystem trafficLightSystem;

    private final Map<String, List<Vehicle>> activeVehicles = new ConcurrentHashMap<>();
    private final Map<String, List<Vehicle>> waitingQueues = new ConcurrentHashMap<>();

    private final List<PauseTransition> backgroundTransitions = new ArrayList<>();
    private final List<Timeline> backgroundTimelines = new ArrayList<>();
    private volatile boolean backgroundProcessesPaused = false;

    public VehicleManager(Pane rootPane, TrafficLightSystem trafficLightSystem) {
        this.rootPane = rootPane;
        this.trafficLightSystem = trafficLightSystem;
        initializeVehicleLists();
    }

    private void initializeVehicleLists() {
        for (String direction : Constants.DIRECTIONS_TURKISH) {
            activeVehicles.put(direction, new CopyOnWriteArrayList<>());
            waitingQueues.put(direction, new CopyOnWriteArrayList<>());
        }
    }

    public void spawnVehicles(String direction, int carCount) {
        List<Path> toLightPaths = PathService.getPathsToLight(Constants.DIRECTION_MAP_TURKISH_TO_ENGLISH.get(direction));
        List<Path> afterLightPaths = PathService.getPathsAfterLight(Constants.DIRECTION_MAP_TURKISH_TO_ENGLISH.get(direction));

        if (toLightPaths.isEmpty() || afterLightPaths.isEmpty()) {
            return;
        }

        for (int i = 0; i < carCount; i++) {
            final int carIndex = i;
            Path toLightPath = toLightPaths.get(i % toLightPaths.size());
            Path afterLightPath = afterLightPaths.get(i % afterLightPaths.size());

            Vehicle vehicle = new Vehicle(rootPane, direction, carIndex);
            activeVehicles.get(direction).add(vehicle);

            double delay = carIndex * Constants.CAR_SPAWN_DELAY;
            PauseTransition startDelay = new PauseTransition(Duration.seconds(delay));
            backgroundTransitions.add(startDelay);

            startDelay.setOnFinished(event -> {
                backgroundTransitions.remove(startDelay);

                if (backgroundProcessesPaused) return;

                vehicle.startMovement(toLightPath, () -> {
                    if (!vehicle.isWaitingAtLight() && vehicle.hasCheckedTrafficLight()) {
                        continueToAfterLight(vehicle, afterLightPath);
                    }
                });

                vehicle.getCurrentTransition().currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                    if (vehicle.getCurrentTransition().getTotalDuration().toMillis() > 0) {
                        vehicle.setCurrentProgress(newTime.toMillis() / vehicle.getCurrentTransition().getTotalDuration().toMillis());

                        if (vehicle.getCurrentProgress() >= vehicle.getStopPosition() && !vehicle.hasCheckedTrafficLight()) {
                            vehicle.setHasCheckedTrafficLight(true);
                            vehicle.setHasReachedStopPoint(true);
                            checkTrafficLightAndAct(vehicle, toLightPath, afterLightPath);
                        }
                    }
                });
            });
            startDelay.play();
        }
    }

    private void checkTrafficLightAndAct(Vehicle vehicle, Path toLightPath, Path afterLightPath) {
        boolean isGreen = trafficLightSystem.isGreen(vehicle.getDirection());

        if (!isGreen) {
            vehicle.pauseMovement();
            vehicle.setWaitingAtLight(true);
            vehicle.setQueueEntryTime(System.currentTimeMillis());

            List<Vehicle> queue = waitingQueues.get(vehicle.getDirection());
            if (queue != null) {
                queue.add(vehicle);
            }

            monitorTrafficLightForVehicle(vehicle, afterLightPath);
        }
    }

    private void monitorTrafficLightForVehicle(Vehicle vehicle, Path afterLightPath) {
        Timeline waitTimeline = new Timeline(
                new KeyFrame(Duration.millis(100), waitEvent -> {
                    if (backgroundProcessesPaused) return;

                    if (trafficLightSystem.isGreen(vehicle.getDirection())) {
                        processQueueMovement(vehicle.getDirection());

                        Timeline currentWaitTimeline = (Timeline) waitEvent.getSource();
                        currentWaitTimeline.stop();
                        backgroundTimelines.remove(currentWaitTimeline);
                    }
                })
        );
        waitTimeline.setCycleCount(Timeline.INDEFINITE);
        backgroundTimelines.add(waitTimeline);
        waitTimeline.play();
    }

    public void processQueueMovement(String direction) {
        List<Vehicle> queue = waitingQueues.get(direction);
        if (queue == null || queue.isEmpty()) return;

        List<Vehicle> queueCopy = new ArrayList<>(queue);
        for (int i = 0; i < queueCopy.size(); i++) {
            Vehicle vehicle = queueCopy.get(i);
            if (vehicle.hasExitedQueue() || !vehicle.isWaitingAtLight()) continue;

            double exitDelay = i * Constants.QUEUE_EXIT_DELAY;
            PauseTransition queueExitDelay = new PauseTransition(Duration.seconds(exitDelay));
            backgroundTransitions.add(queueExitDelay);

            queueExitDelay.setOnFinished(exitEvent -> {
                backgroundTransitions.remove(queueExitDelay);

                if (backgroundProcessesPaused) return;

                if (!vehicle.hasExitedQueue() && vehicle.isWaitingAtLight()) {
                    vehicle.setHasExitedQueue(true);
                    vehicle.setWaitingAtLight(false);
                    resumeCarMovement(vehicle);
                }
            });
            queueExitDelay.play();
        }
        queue.clear();
    }

    private void resumeCarMovement(Vehicle vehicle) {
        double remainingProgress = 1.0 - vehicle.getCurrentProgress();
        double remainingTime = Constants.UNIFORM_SPEED * remainingProgress;

        if (remainingTime > 0.1) {
            PathTransition transition = vehicle.getCurrentTransition();
            if (transition != null) {
                transition.setDuration(Duration.seconds(remainingTime));
                transition.setOnFinished(e -> moveToSecondPath(vehicle));
                transition.play();
            }
        } else {
            moveToSecondPath(vehicle);
        }
    }

    private void moveToSecondPath(Vehicle vehicle) {
        vehicle.setMovingToSecondPath(true);
        List<Path> afterLightPaths = PathService.getPathsAfterLight(Constants.DIRECTION_MAP_TURKISH_TO_ENGLISH.get(vehicle.getDirection()));

        if (afterLightPaths != null && !afterLightPaths.isEmpty()) {
            Path afterLightPath = afterLightPaths.get(vehicle.getQueuePosition() % afterLightPaths.size());
            continueToAfterLight(vehicle, afterLightPath);
        }
    }

    private void continueToAfterLight(Vehicle vehicle, Path afterLightPath) {
        if (vehicle.getCurrentTransition() != null) {
            vehicle.getCurrentTransition().stop();
        }

        PathTransition afterLightTransition = new PathTransition(
                Duration.seconds(Constants.UNIFORM_SPEED), afterLightPath, vehicle.getCarNode()
        );
        afterLightTransition.setOrientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT);
        afterLightTransition.setOnFinished(e -> {
            vehicle.removeFromPane();
            activeVehicles.get(vehicle.getDirection()).remove(vehicle);
        });

        vehicle.setCurrentTransition(afterLightTransition);
        afterLightTransition.play();
    }

    public void pauseAllVehiclesControlled() {
        for (String direction : Constants.DIRECTIONS_TURKISH) {
            List<Vehicle> vehicles = activeVehicles.get(direction);
            if (vehicles != null) {
                for (Vehicle vehicle : vehicles) {
                    if (vehicle.getCurrentTransition() != null &&
                            vehicle.getCurrentTransition().getStatus() == Animation.Status.RUNNING &&
                            !vehicle.isWaitingAtLight()) {
                        vehicle.getCurrentTransition().pause();
                    }
                }
            }
        }
    }

    public void resumeAllVehiclesControlled() {
        for (String direction : Constants.DIRECTIONS_TURKISH) {
            List<Vehicle> vehicles = activeVehicles.get(direction);
            if (vehicles != null) {
                for (Vehicle vehicle : vehicles) {
                    if (vehicle.getCurrentTransition() != null &&
                            vehicle.getCurrentTransition().getStatus() == Animation.Status.PAUSED &&
                            !vehicle.isWaitingAtLight()) {
                        vehicle.getCurrentTransition().play();
                    }
                }
            }
        }
    }

    public void pauseAllBackgroundProcesses() {
        backgroundProcessesPaused = true;

        for (PauseTransition transition : new ArrayList<>(backgroundTransitions)) {
            if (transition != null && transition.getStatus() == Animation.Status.RUNNING) {
                try {
                    transition.pause();
                } catch (Exception ignored) {}
            }
        }

        for (Timeline timeline : new ArrayList<>(backgroundTimelines)) {
            if (timeline != null && timeline.getStatus() == Timeline.Status.RUNNING) {
                try {
                    timeline.pause();
                } catch (Exception ignored) {}
            }
        }
    }

    public void resumeAllBackgroundProcesses() {
        backgroundProcessesPaused = false;

        for (PauseTransition transition : new ArrayList<>(backgroundTransitions)) {
            if (transition != null && transition.getStatus() == Animation.Status.PAUSED) {
                try {
                    transition.play();
                } catch (Exception ignored) {}
            }
        }

        for (Timeline timeline : new ArrayList<>(backgroundTimelines)) {
            if (timeline != null && timeline.getStatus() == Timeline.Status.PAUSED) {
                try {
                    timeline.play();
                } catch (Exception ignored) {}
            }
        }
    }

    public void stopAllVehicles() {
        for (String direction : Constants.DIRECTIONS_TURKISH) {
            List<Vehicle> vehicles = activeVehicles.get(direction);
            if (vehicles != null) {
                for (Vehicle vehicle : vehicles) {
                    if (vehicle.getCurrentTransition() != null) {
                        vehicle.getCurrentTransition().stop();
                    }
                }
            }
            List<Vehicle> waiting = waitingQueues.get(direction);
            if (waiting != null) {
                waiting.clear();
            }
        }
        for (PauseTransition pt : new ArrayList<>(backgroundTransitions)) {
            pt.stop();
        }
        backgroundTransitions.clear();
        for (Timeline tl : new ArrayList<>(backgroundTimelines)) {
            tl.stop();
        }
        backgroundTimelines.clear();
    }

    public void clearAllVehiclesFromScreen() {
        for (String direction : Constants.DIRECTIONS_TURKISH) {
            List<Vehicle> vehicles = activeVehicles.get(direction);
            if (vehicles != null) {
                for (Vehicle vehicle : new ArrayList<>(vehicles)) {
                    vehicle.removeFromPane();
                    vehicles.remove(vehicle);
                }
            }
            List<Vehicle> waiting = waitingQueues.get(direction);
            if (waiting != null) {
                waiting.clear();
            }
        }
        backgroundTransitions.clear();
        backgroundTimelines.clear();
        backgroundProcessesPaused = false;
    }
}
