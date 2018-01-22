package agh.cs.oop.airlyconsoleclient.airlyapi;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AirlyApiFactory {
    public AirlyApi createAirlyApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://airapi.airly.eu/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(AirlyApi.class);
    }
}
