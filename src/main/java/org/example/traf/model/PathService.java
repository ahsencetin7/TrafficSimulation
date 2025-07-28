package org.example.traf.model;

import javafx.scene.shape.ArcTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import java.util.List;

public class PathService {

    private PathService() {}

    public static List<Path> getPathsToLight(String direction) {
        return switch (direction.toLowerCase()) {
            case "east" -> List.of(createEastPathToLight());
            case "west" -> List.of(createWestPathToLight());
            case "north" -> List.of(createNorthPathToLight());
            case "south" -> List.of(createSouthPathToLight());
            default -> throw new IllegalArgumentException("Invalid direction: " + direction);
        };
    }

    public static List<Path> getPathsAfterLight(String direction) {
        return switch (direction.toLowerCase()) {
            case "east" -> List.of(createEastPathAfterLight1(), createEastPathAfterLight2(), createEastPathAfterLight3());
            case "west" -> List.of(createWestPathAfterLight1(), createWestPathAfterLight2(), createWestPathAfterLight3());
            case "north" -> List.of(createNorthPathAfterLight1(), createNorthPathAfterLight2(), createNorthPathAfterLight3());
            case "south" -> List.of(createSouthPathAfterLight1(), createSouthPathAfterLight2(), createSouthPathAfterLight3());
            default -> throw new IllegalArgumentException("Invalid direction: " + direction);
        };
    }

    // --- Dogu ---
    private static Path createEastPathToLight() {
        Path path = new Path();
        path.getElements().add(new MoveTo(1979, 490));
        path.getElements().add(new LineTo(1160, 490));
        return path;
    }

    private static Path createEastPathAfterLight1() {
        Path path = new Path();
        path.getElements().add(new MoveTo(1160, 490));
        path.getElements().add(new LineTo(1060, 490));
        path.getElements().add(new ArcTo(300, 300, 0, 910, 640, false, false));
        path.getElements().add(new LineTo(910, 1139));
        return path;
    }

    private static Path createEastPathAfterLight2() {
        Path path = new Path();
        path.getElements().add(new MoveTo(1160, 490));
        path.getElements().add(new LineTo(-60, 490));
        return path;
    }

    private static Path createEastPathAfterLight3() {
        Path path = new Path();
        path.getElements().add(new MoveTo(1160, 490));
        path.getElements().add(new LineTo(1060, 490));
        path.getElements().add(new ArcTo(90, 90, 0, 1010, 440, false, true));
        path.getElements().add(new LineTo(1010, -60));
        return path;
    }

    // ---Batı ---
    private static Path createWestPathToLight() {
        Path path = new Path();
        path.getElements().add(new MoveTo(-60, 590));
        path.getElements().add(new LineTo(760, 590));
        return path;
    }

    private static Path createWestPathAfterLight1() {
        Path path = new Path();
        path.getElements().add(new MoveTo(760, 590));
        path.getElements().add(new LineTo(860, 590));
        path.getElements().add(new ArcTo(90, 90, 0, 910, 640, false, true));
        path.getElements().add(new LineTo(910, 1139));
        return path;
    }

    private static Path createWestPathAfterLight2() {
        Path path = new Path();
        path.getElements().add(new MoveTo(760, 590));
        path.getElements().add(new LineTo(1979, 590));
        return path;
    }

    private static Path createWestPathAfterLight3() {
        Path path = new Path();
        path.getElements().add(new MoveTo(760, 590));
        path.getElements().add(new LineTo(860, 590));
        path.getElements().add(new ArcTo(300, 300, 0, 1010, 440, false, false));
        path.getElements().add(new LineTo(1010, -60));
        return path;
    }

    // --- Kuzey ---
    private static Path createNorthPathToLight() {
        Path path = new Path();
        path.getElements().add(new MoveTo(910, -60));
        path.getElements().add(new LineTo(910, 340));
        return path;
    }

    private static Path createNorthPathAfterLight1() {
        Path path = new Path();
        path.getElements().add(new MoveTo(910, 340));
        path.getElements().add(new LineTo(910, 440));
        path.getElements().add(new ArcTo(90, 90, 0, 860, 490, false, true));
        path.getElements().add(new LineTo(-60, 490));
        return path;
    }

    private static Path createNorthPathAfterLight2() {
        Path path = new Path();
        path.getElements().add(new MoveTo(910, 340));
        path.getElements().add(new LineTo(910, 1139));
        return path;
    }

    private static Path createNorthPathAfterLight3() {
        Path path = new Path();
        path.getElements().add(new MoveTo(910, 340));
        path.getElements().add(new LineTo(910, 440));
        path.getElements().add(new ArcTo(300, 300, 0, 1060, 590, false, false));
        path.getElements().add(new LineTo(1979, 590));
        return path;
    }

    // --- Guney ---
    private static Path createSouthPathToLight() {
        Path path = new Path();
        path.getElements().add(new MoveTo(1010, 1139));
        path.getElements().add(new LineTo(1010, 790));
        return path;
    }

    private static Path createSouthPathAfterLight1() {
        Path path = new Path();
        path.getElements().add(new MoveTo(1010, 790));
        path.getElements().add(new LineTo(1010, 690));
        path.getElements().add(new ArcTo(300, 300, 0, 860, 490, false, false));
        path.getElements().add(new LineTo(-60, 490));
        return path;
    }

    private static Path createSouthPathAfterLight2() {
        Path path = new Path();
        path.getElements().add(new MoveTo(1010, 790));
        path.getElements().add(new LineTo(1010, -600));
        return path;
    }

    private static Path createSouthPathAfterLight3() {
        Path path = new Path();
        path.getElements().add(new MoveTo(1010, 790));
        path.getElements().add(new LineTo(1010, 690));
        path.getElements().add(new ArcTo(90, 90, 0, 1060, 590, false, true));
        path.getElements().add(new LineTo(1979, 590));
        return path;
    }
}
