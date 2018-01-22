package agh.cs.oop.airlyconsoleclient.airlyapi;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface AirlyApi {
    @GET("v1/sensor/measurements")
    Call<AllMeasurements> sensorMeasurements(@Header("apikey") String apiKey,
                                             @Query("sensorId") int sensorId);

    @GET("v1/mapPoint/measurements")
    Call<AllMeasurements> mapPointMeasurements(@Header("apikey") String apiKey,
                                               @Query("latitude") double latitude,
                                               @Query("longitude") double longitude);
}
