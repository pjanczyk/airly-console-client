package agh.cs.oop.airlyconsoleclient;

import org.jetbrains.annotations.Nullable;

public class Arguments {
    public final String apiKey;
    public final @Nullable Integer sensorId;
    public final @Nullable Double latitude;
    public final @Nullable Double longitude;
    public final boolean history;

    public Arguments(String apiKey, int sensorId, boolean history) {
        this.apiKey = apiKey;
        this.sensorId = sensorId;
        this.latitude = null;
        this.longitude = null;
        this.history = history;
    }

    public Arguments(String apiKey, double latitude, double longitude, boolean history) {
        this.apiKey = apiKey;
        this.sensorId = null;
        this.latitude = latitude;
        this.longitude = longitude;
        this.history = history;
    }
}
