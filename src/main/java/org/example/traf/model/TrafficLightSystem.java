package org.example.traf.model;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.example.traf.view.DynamicTrafficLightsView;
import org.example.traf.util.Constants;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class TrafficLightSystem {
    private final DynamicTrafficLightsView dynamicTrafficLightsView;
    private final Map<String, Integer> greenDurations;
    private Timeline currentPhaseTimeline;
    private Runnable onPhaseCompleteCallback;
    private String currentLightDirection;
    private int currentLightRemainingDuration;

    public TrafficLightSystem(Pane parentPane, double width, double height, Map<String, Integer> greenDurations) {
        this.dynamicTrafficLightsView = new DynamicTrafficLightsView(parentPane, width, height);
        this.greenDurations = greenDurations;
    }

    public synchronized void startLightCycle(String direction, boolean isFirstPhase, Runnable onCompleteCallback) {
        this.onPhaseCompleteCallback = onCompleteCallback;
        this.currentLightDirection = direction;

        if (isFirstPhase) {
            runGreenPhase(direction, greenDurations.getOrDefault(direction, Constants.MIN_GREEN_DURATION));
        } else {
            runYellowPhase(direction, () -> runGreenPhase(direction, greenDurations.getOrDefault(direction, Constants.MIN_GREEN_DURATION)));
        }
    }

    private void runYellowPhase(String direction, Runnable onComplete) {
        stopCurrentPhaseTimeline();
        Platform.runLater(() -> {
            dynamicTrafficLightsView.setAllRed();
            dynamicTrafficLightsView.setLightColor(direction, "yellow");
            dynamicTrafficLightsView.setCountdown(direction, Constants.YELLOW_DURATION);
            dynamicTrafficLightsView.setCountdownColor(direction, Color.ORANGE);
        });

        AtomicInteger countdown = new AtomicInteger(Constants.YELLOW_DURATION);
        currentLightRemainingDuration = Constants.YELLOW_DURATION;

        currentPhaseTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            int remaining = countdown.decrementAndGet();
            currentLightRemainingDuration = remaining;
            Platform.runLater(() -> {
                dynamicTrafficLightsView.setCountdown(direction, remaining);
                if (remaining <= 0) {
                    dynamicTrafficLightsView.clearCountdown(direction);
                }
            });
        }));
        currentPhaseTimeline.setCycleCount(Constants.YELLOW_DURATION);
        currentPhaseTimeline.setOnFinished(e -> {
            if (onComplete != null) {
                Platform.runLater(onComplete);
            }
        });
        currentPhaseTimeline.play();
    }

    private void runGreenPhase(String direction, int duration) {
        stopCurrentPhaseTimeline();
        Platform.runLater(() -> {
            dynamicTrafficLightsView.setAllRed();
            dynamicTrafficLightsView.setLightColor(direction, "green");
            dynamicTrafficLightsView.setCountdown(direction, duration);
            dynamicTrafficLightsView.setCountdownColor(direction, Color.GREEN);
        });

        AtomicInteger countdown = new AtomicInteger(duration);
        currentLightRemainingDuration = duration;

        currentPhaseTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            int remaining = countdown.decrementAndGet();
            currentLightRemainingDuration = remaining;
            Platform.runLater(() -> {
                dynamicTrafficLightsView.setCountdown(direction, remaining);
                if (remaining <= 0) {
                    dynamicTrafficLightsView.clearCountdown(direction);
                }
            });
        }));
        currentPhaseTimeline.setCycleCount(duration);
        currentPhaseTimeline.setOnFinished(e -> {
            if (onPhaseCompleteCallback != null) {
                Platform.runLater(onPhaseCompleteCallback);
            }
        });
        currentPhaseTimeline.play();
    }

    public synchronized void pauseCurrentPhaseTimeline() {
        if (currentPhaseTimeline != null && currentPhaseTimeline.getStatus() == Timeline.Status.RUNNING) {
            currentPhaseTimeline.pause();
        }
    }

    public synchronized void resumeCurrentPhaseTimeline() {
        if (currentPhaseTimeline != null && currentPhaseTimeline.getStatus() == Timeline.Status.PAUSED) {
            currentPhaseTimeline.play();
        } else if (currentPhaseTimeline == null || currentPhaseTimeline.getStatus() == Timeline.Status.STOPPED) {
            if (currentLightDirection != null && currentLightRemainingDuration > 0) {
                String color = dynamicTrafficLightsView.isGreen(currentLightDirection) ? "green" : "yellow";
                AtomicInteger countdown = new AtomicInteger(currentLightRemainingDuration);
                Platform.runLater(() -> {
                    dynamicTrafficLightsView.setCountdown(currentLightDirection, currentLightRemainingDuration);
                    dynamicTrafficLightsView.setCountdownColor(currentLightDirection,
                            color.equals("green") ? Color.GREEN : Color.ORANGE);
                });

                currentPhaseTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
                    int remaining = countdown.decrementAndGet();
                    currentLightRemainingDuration = remaining;
                    Platform.runLater(() -> {
                        dynamicTrafficLightsView.setCountdown(currentLightDirection, remaining);
                        if (remaining <= 0) {
                            dynamicTrafficLightsView.clearCountdown(currentLightDirection);
                        }
                    });
                }));

                currentPhaseTimeline.setCycleCount(currentLightRemainingDuration);
                currentPhaseTimeline.setOnFinished(e -> {
                    if (onPhaseCompleteCallback != null) {
                        Platform.runLater(onPhaseCompleteCallback);
                    }
                });
                currentPhaseTimeline.play();
            }
        }
    }

    public synchronized void stopCurrentPhaseTimeline() {
        if (currentPhaseTimeline != null) {
            currentPhaseTimeline.stop();
            currentPhaseTimeline = null;
            currentLightDirection = null;
            currentLightRemainingDuration = 0;
        }
    }

    public void resetLights() {
        stopCurrentPhaseTimeline();
        dynamicTrafficLightsView.setAllRed();
        for (String direction : Constants.DIRECTIONS_TURKISH) {
            dynamicTrafficLightsView.clearCountdown(direction);
        }
    }

    public boolean isGreen(String direction) {
        return dynamicTrafficLightsView.isGreen(direction);
    }
}
