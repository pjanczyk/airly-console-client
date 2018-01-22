package agh.cs.oop.airlyconsoleclient;

import agh.cs.oop.airlyconsoleclient.airlyapi.AirlyApi;
import agh.cs.oop.airlyconsoleclient.airlyapi.AirlyApiFactory;
import agh.cs.oop.airlyconsoleclient.airlyapi.AllMeasurements;
import org.apache.commons.cli.*;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.Optional;

import static java.lang.System.*;

public class Application {

    private static final String MSG_INVALID_ARGS = "Invalid format of arguments";
    private static final String MSG_MISSING_API_KEY =
            "Airly API key must be provided either as '--api-key' parameter or as 'API_KEY' environment variable.";
    private static final String MSG_MISSING_SENSOR_ID_OR_COORDINATES =
            "Either '--sensor-id' or '--latitude' and '--longitude' must be specified.";

    public static void main(String[] args) {
        Options options = new Options();
        options.addOption(null, "api-key", true, "Airly API key");
        options.addOption(null, "sensor-id", true, "Sensor ID");
        options.addOption(null, "latitude", true, "Latitude coordinate of an area");
        options.addOption(null, "longitude", true, "Longitude coordinate of an area.");
        options.addOption(null, "history", false, "Displays history of measurement");

        if (args.length == 0) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar airly-console-client.jar", options);
            out.println();
            out.println(MSG_MISSING_SENSOR_ID_OR_COORDINATES);
            out.println(MSG_MISSING_API_KEY);
            return;
        }

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            err.println(MSG_INVALID_ARGS);
            return;
        }

        String apiKey = Optional.ofNullable(cmd.getOptionValue("api-key"))
                .or(() -> Optional.ofNullable(System.getenv("API_KEY")))
                .orElse(null);
        String sensorIdText = cmd.getOptionValue("sensor-id");
        String latitudeText = cmd.getOptionValue("latitude");
        String longitudeText = cmd.getOptionValue("longitude");
        boolean history = cmd.hasOption("history");

        Integer sensorId = null;
        Double latitude = null;
        Double longitude = null;

        try {
            if (sensorIdText != null)
                sensorId = Integer.parseUnsignedInt(sensorIdText);
            if (latitudeText != null)
                latitude = Double.parseDouble(latitudeText);
            if (longitudeText != null)
                longitude = Double.parseDouble(longitudeText);
        } catch (NumberFormatException e) {
            err.println(MSG_INVALID_ARGS);
            return;
        }

        if (apiKey == null) {
            err.println(MSG_MISSING_API_KEY);
            return;
        }

        Arguments arguments;

        if (sensorId != null && latitude == null && longitude == null) {
            arguments = new Arguments(apiKey, sensorId, history);
        } else if (sensorId == null && latitude != null && longitude != null) {
            arguments = new Arguments(apiKey, latitude, longitude, history);
        } else {
            err.println(MSG_MISSING_SENSOR_ID_OR_COORDINATES);
            return;
        }

        Call<AllMeasurements> call = prepareCall(arguments);

        Response<AllMeasurements> response;
        try {
            response = call.execute();
        } catch (IOException e) {
            err.println("Failed to connect to the server: " + e.getMessage());
            return;
        } catch (Exception e) {
            err.println("An unexpected error occurred during creating the request or decoding the response");
            return;
        }

        if (response.code() == 401) {
            err.println("Invalid API Key (server returned 401 Unauthorized)");
            return;
        }

        if (!response.isSuccessful()) {
            err.println(String.format("Server returned: %d %s", response.code(), response.message()));
            return;
        }

        AsciiPrinter printer = new AsciiPrinter();
        printer.print(response.body(), arguments);
    }

    private static Call<AllMeasurements> prepareCall(Arguments arguments) {
        AirlyApi api = new AirlyApiFactory().createAirlyApi();

        if (arguments.sensorId != null) {
            return api.sensorMeasurements(arguments.apiKey, arguments.sensorId);
        } else {
            //noinspection ConstantConditions
            return api.mapPointMeasurements(arguments.apiKey, arguments.latitude, arguments.longitude);
        }
    }

}
