package agh.cs.oop.airlyconsoleclient;

import agh.cs.oop.airlyconsoleclient.airlyapi.AirlyApi;
import agh.cs.oop.airlyconsoleclient.airlyapi.AirlyApiFactory;
import agh.cs.oop.airlyconsoleclient.airlyapi.AllMeasurements;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;

public class Core {
    private final Arguments arguments;

    public Core(Arguments arguments) {
        this.arguments = arguments;
    }

    public void run() {
        Call<AllMeasurements> call = prepareCall();

        try {
            Response<AllMeasurements> response = call.execute();
            if (response.isSuccessful()) {
                AsciiPrinter printer = new AsciiPrinter();
                printer.print(response.body(), arguments);
            } else {
                // TODO: throw exception
                System.out.println(response.errorBody());
            }
        } catch (IOException e) {
            e.printStackTrace(); // TODO: throw exception
        }
    }

    private Call<AllMeasurements> prepareCall() {
        AirlyApi api = new AirlyApiFactory().createAirlyApi();

        if (arguments.sensorId != null) {
            return api.sensorMeasurements(arguments.apiKey, arguments.sensorId);
        } else {
            //noinspection ConstantConditions
            return api.mapPointMeasurements(arguments.apiKey, arguments.latitude, arguments.longitude);
        }
    }
}
