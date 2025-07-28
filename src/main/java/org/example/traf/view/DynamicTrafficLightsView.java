package org.example.traf.view;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import java.util.HashMap;
import java.util.Map;
import org.example.traf.util.Constants;

public class DynamicTrafficLightsView {

    private final Map<String, String> lightStates = new HashMap<>();
    private final Pane parentPane;
    private static final Image redLightImg = new Image(DynamicTrafficLightsView.class.getResourceAsStream("/kirmizi.png"));
    private static final Image greenLightImg = new Image(DynamicTrafficLightsView.class.getResourceAsStream("/yesil.png"));
    private static final Image yellowLightImg = new Image(DynamicTrafficLightsView.class.getResourceAsStream("/sari.png"));
    private final Map<String, ImageView> lightViews = new HashMap<>();
    private final Map<String, Label> countdownLabels = new HashMap<>();

    public DynamicTrafficLightsView(Pane parent, double width, double height) {
        this.parentPane = parent;
        for (String direction : Constants.DIRECTIONS_TURKISH) {
            lightStates.put(direction, "red");
        }
        drawTrafficLights(width, height);
    }

    public boolean isGreen(String direction) {
        return "green".equals(lightStates.get(direction));
    }

    public void setLightColor(String direction, String color) {
        lightStates.put(direction, color.toLowerCase());
        ImageView imageView = lightViews.get(direction);
        if (imageView != null) {
            Image newImage = switch (color.toLowerCase()) {
                case "green" -> greenLightImg;
                case "yellow" -> yellowLightImg;
                case "red" -> redLightImg;
                default -> redLightImg;
            };
            imageView.setImage(newImage);
        }
    }

    public void setAllRed() {
        for (String direction : Constants.DIRECTIONS_TURKISH) {
            setLightColor(direction, "red");
            clearCountdown(direction);
        }
    }

    public void setCountdown(String direction, int duration) {
        Label label = countdownLabels.get(direction);
        if (label != null) {
            label.setText(String.valueOf(duration));
        }
    }

    public void setCountdownColor(String direction, Color color) {
        Label label = countdownLabels.get(direction);
        if (label != null) {
            label.setTextFill(color);
        }
    }

    public void clearCountdown(String direction) {
        Label label = countdownLabels.get(direction);
        if (label != null) {
            label.setText("");
        }
    }

    private void drawTrafficLights(double width, double height) {
        double s = Constants.STREET_WIDTH;
        double cx = width / 2;
        double cy = height / 2;
        double ls = Constants.LIGHT_SIZE;

        double[][] positions = {
                {cx + s - ls, cy - ls / 2},   // Doğu
                {cx - s, cy - ls / 2},        // Batı
                {cx - ls / 2, cy - s - ls},   // Kuzey
                {cx - ls / 2, cy + s}         // Güney
        };

        for (int i = 0; i < Constants.DIRECTIONS_TURKISH.length; i++) {
            String direction = Constants.DIRECTIONS_TURKISH[i];
            double x = positions[i][0];
            double y = positions[i][1];

            ImageView imageView = new ImageView(redLightImg);
            imageView.setFitWidth(ls);
            imageView.setFitHeight(ls);
            imageView.setX(x);
            imageView.setY(y);

            Label countdownLabel = new Label("");
            double labelXOffset = switch (direction) {
                case "Doğu" -> ls + Constants.LIGHT_OFFSET;
                case "Batı" -> -50;
                default -> 0;
            };
            double labelYOffset = switch (direction) {
                case "Kuzey" -> -20;
                case "Güney" -> ls + 20;
                default -> ls / 4;
            };
            double baseLabelX = (direction.equals("Doğu") || direction.equals("Batı")) ? x + labelXOffset : x - 15;
            double baseLabelY = (direction.equals("Kuzey") || direction.equals("Güney")) ? y + labelYOffset : y + labelYOffset;

            countdownLabel.setLayoutX(baseLabelX);
            countdownLabel.setLayoutY(baseLabelY);
            countdownLabel.setStyle("-fx-font-size:" + Constants.COUNTDOWN_LABEL_FONT_SIZE + "px; -fx-font-weight:bold;");

            lightViews.put(direction, imageView);
            countdownLabels.put(direction, countdownLabel);
            parentPane.getChildren().addAll(imageView, countdownLabel);
        }
    }
}
