package org.example.traf.util;

import java.util.Map;

public final class Constants {

    private Constants() {} // Sabit sınıf, örneklenemez

    // --- Trafik Işığı Süreleri (saniye) ---
    public static final int YELLOW_DURATION = 2;
    public static final int MIN_GREEN_DURATION = 10;
    public static final int MAX_GREEN_DURATION = 60;

    // --- Simülasyon Ayarları ---
    public static final int TOTAL_SIMULATION_TIME = 120;

    // --- Araç Davranış Parametreleri ---
    public static final double SAFE_DISTANCE = 100.0;
    public static final double CAR_LENGTH = 70.0;
    public static final double QUEUE_SPACING = 0.20;
    public static final double UNIFORM_SPEED = 4.5;
    public static final double CAR_SPAWN_DELAY = 2.5;
    public static final double QUEUE_EXIT_DELAY = 1.2;
    public static final double SECOND_CAR_EXTRA_DISTANCE = 0.1;
    public static final double SAFETY_BUFFER = 20.0;
    public static final double MAX_SPEED_DIFF = 0.5;

    // --- Kullanıcı Arayüzü (UI) ---
    public static final double INTERSECTION_WIDTH = 1920;
    public static final double INTERSECTION_HEIGHT = 1080;
    public static final double STREET_WIDTH = 100;
    public static final double ROAD_WIDTH = STREET_WIDTH * 2;
    public static final double LIGHT_SIZE = 40;
    public static final double LIGHT_OFFSET = 5;
    public static final double COUNTDOWN_LABEL_FONT_SIZE = 16;
    public static final double TOTAL_COUNTDOWN_LABEL_FONT_SIZE = 28;

    // --- Araç Renkleri ---
    public static final String[] REALISTIC_CAR_COLORS = {
            "#1E3A8A", "#7C2D12", "#14532D", "#9A3412",
            "#581C87", "#92400E", "#1E40AF", "#BE123C",
            "#374151", "#000000", "#FFFFFF", "#DC2626"
    };

    public static final String[] DIRECTIONS_TURKISH = {"Doğu", "Batı", "Kuzey", "Güney"};

    public static final Map<String, String> DIRECTION_MAP_TURKISH_TO_ENGLISH = Map.of(
            "Doğu", "east",
            "Batı", "west",
            "Kuzey", "north",
            "Güney", "south"
    );
}
