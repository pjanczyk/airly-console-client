package agh.cs.oop.airlyconsoleclient.airlyapi;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Response of "/v1/sensor/measurements" and "/v1/mapPoint/measurements"
 * Deserialized by GSON.
 */
public class AllMeasurements {
    private Measurement currentMeasurements;
    private MeasurementWithTime[] history;

    public Measurement getCurrentMeasurements() {
        return currentMeasurements;
    }

    public List<MeasurementWithTime> getHistory() {
        return Collections.unmodifiableList(Arrays.asList(history));
    }
}
