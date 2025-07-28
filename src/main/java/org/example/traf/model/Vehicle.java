package org.example.traf.model;

import javafx.animation.Animation;
import javafx.animation.PathTransition;
import javafx.scene.Node;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.example.traf.util.Constants;
import java.util.Random;

public class Vehicle {
    private final Node carNode;
    private PathTransition currentTransition;
    private double currentProgress;
    private double stopPosition;
    private final String direction;
    private boolean isWaitingAtLight;
    private boolean hasReachedStopPoint;
    private boolean isMovingToSecondPath;
    private boolean hasExitedQueue;
    private boolean hasCheckedTrafficLight;
    private int queuePosition;
    private long queueEntryTime;
    private final Pane rootPane;
    private double pathLength;
    private double currentPixelPosition;

    public Vehicle(Pane rootPane, String direction, int position) {
        this.rootPane = rootPane;
        this.direction = direction;
        this.carNode = createCarNode();
        this.currentProgress = 0.0;
        this.queuePosition = position;
        this.stopPosition = calculateStopPosition(position);
        this.isWaitingAtLight = false;
        this.hasReachedStopPoint = false;
        this.isMovingToSecondPath = false;
        this.hasExitedQueue = false;
        this.hasCheckedTrafficLight = false;
        this.queueEntryTime = 0;
    }

    private Node createCarNode() {
        try {
            Image image = new Image(getClass().getResourceAsStream("/araba.png"));
            if (!image.isError()) {
                ImageView car = new ImageView(image);
                car.setFitWidth(60);
                car.setFitHeight(60);
                ColorAdjust colorAdjust = new ColorAdjust();
                colorAdjust.setHue((Math.random() * 2) - 1);
                colorAdjust.setSaturation((Math.random() * 1.5) - 0.5);
                car.setEffect(colorAdjust);
                return car;
            }
        } catch (Exception e) {
            // silently fallback
        }

        Rectangle car = new Rectangle(60, 30);
        car.setFill(getRandomCarColor());
        car.setStroke(Color.BLACK);
        car.setStrokeWidth(1.5);
        car.setArcWidth(10);
        car.setArcHeight(10);
        return car;
    }

    private Color getRandomCarColor() {
        Random random = new Random();
        String hexColor = Constants.REALISTIC_CAR_COLORS[random.nextInt(Constants.REALISTIC_CAR_COLORS.length)];
        return Color.web(hexColor);
    }

    private double calculateStopPosition(int position) {
        double stopPos;
        if (position == 0) {
            stopPos = 1.0 - Constants.QUEUE_SPACING;
        } else if (position == 1) {
            stopPos = 1.0 - (Constants.QUEUE_SPACING + Constants.SECOND_CAR_EXTRA_DISTANCE + Constants.QUEUE_SPACING);
        } else {
            stopPos = 1.0 - (Constants.QUEUE_SPACING + Constants.SECOND_CAR_EXTRA_DISTANCE + (position * Constants.QUEUE_SPACING));
        }
        return Math.max(0.1, stopPos);
    }

    public void startMovement(Path path, Runnable onFinished) {
        if (currentTransition != null) {
            currentTransition.stop();
        }

        if (!rootPane.getChildren().contains(carNode)) {
            rootPane.getChildren().add(carNode);
        }

        PathTransition transition = new PathTransition(Duration.seconds(Constants.UNIFORM_SPEED), path, carNode);
        transition.setOrientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT);
        transition.setCycleCount(1);
        transition.setAutoReverse(false);

        this.currentTransition = transition;
        this.pathLength = calculatePathLength(path);

        transition.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
            if (transition.getTotalDuration().toMillis() > 0) {
                currentProgress = newTime.toMillis() / transition.getTotalDuration().toMillis();
                currentPixelPosition = currentProgress * pathLength;
            }
        });

        transition.setOnFinished(e -> {
            if (onFinished != null) {
                onFinished.run();
            }
        });

        transition.play();
    }

    public void pauseMovement() {
        if (currentTransition != null && currentTransition.getStatus() == Animation.Status.RUNNING) {
            currentTransition.pause();
            isWaitingAtLight = true;
            queueEntryTime = System.currentTimeMillis();
        }
    }

    private double calculatePathLength(Path path) {
        double width = path.getBoundsInLocal().getWidth();
        double height = path.getBoundsInLocal().getHeight();
        return Math.sqrt(width * width + height * height);
    }

    public void removeFromPane() {
        rootPane.getChildren().remove(carNode);
    }

    public Node getCarNode() {
        return carNode;
    }

    public double getCurrentProgress() {
        return currentProgress;
    }

    public double getStopPosition() {
        return stopPosition;
    }

    public String getDirection() {
        return direction;
    }

    public boolean isWaitingAtLight() {
        return isWaitingAtLight;
    }

    public void setWaitingAtLight(boolean waitingAtLight) {
        isWaitingAtLight = waitingAtLight;
    }

    public boolean hasReachedStopPoint() {
        return hasReachedStopPoint;
    }

    public void setHasReachedStopPoint(boolean hasReachedStopPoint) {
        this.hasReachedStopPoint = hasReachedStopPoint;
    }

    public boolean isMovingToSecondPath() {
        return isMovingToSecondPath;
    }

    public void setMovingToSecondPath(boolean movingToSecondPath) {
        isMovingToSecondPath = movingToSecondPath;
    }

    public boolean hasExitedQueue() {
        return hasExitedQueue;
    }

    public void setHasExitedQueue(boolean hasExitedQueue) {
        this.hasExitedQueue = hasExitedQueue;
    }

    public boolean hasCheckedTrafficLight() {
        return hasCheckedTrafficLight;
    }

    public void setHasCheckedTrafficLight(boolean hasCheckedTrafficLight) {
        this.hasCheckedTrafficLight = hasCheckedTrafficLight;
    }

    public int getQueuePosition() {
        return queuePosition;
    }

    public long getQueueEntryTime() {
        return queueEntryTime;
    }

    public double getCurrentPixelPosition() {
        return currentPixelPosition;
    }

    public PathTransition getCurrentTransition() {
        return currentTransition;
    }

    public void setCurrentTransition(PathTransition transition) {
        this.currentTransition = transition;
    }

    public void setCurrentProgress(double progress) {
        this.currentProgress = progress;
    }

    public void setQueueEntryTime(long time) {
        this.queueEntryTime = time;
    }
}
