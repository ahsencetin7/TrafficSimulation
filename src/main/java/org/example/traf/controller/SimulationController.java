package org.example.traf.controller;

/**
 * Simülasyon kontrol butonları tarafından çağrılacak temel simülasyon eylemlerini tanımlayan arayüz.
 */
public interface SimulationController {

    void startNewSimulationFlow();

    void stopCurrentSimulation();

    void togglePauseResume();

    boolean isSimulationRunning();

    boolean isSimulationPaused();

    default boolean canStart() {
        return !isSimulationRunning() || (isSimulationRunning() && isSimulationPaused());
    }

    default boolean canPause() {
        return isSimulationRunning() && !isSimulationPaused();
    }

    default boolean canStop() {
        return isSimulationRunning();
    }

    default String getSimulationStatus() {
        if (!isSimulationRunning()) {
            return "DURDURULDU";
        } else if (isSimulationPaused()) {
            return "DURAKLATILDI";
        } else {
            return "ÇALIŞIYOR";
        }
    }
}
