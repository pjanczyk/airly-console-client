package agh.cs.oop.airlyconsoleclient;

import org.apache.commons.cli.*;

import java.util.Optional;

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
            System.out.println();
            System.out.println(MSG_MISSING_SENSOR_ID_OR_COORDINATES);
            System.out.println(MSG_MISSING_API_KEY);
            return;
        }

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println(MSG_INVALID_ARGS);
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
            System.err.println(MSG_INVALID_ARGS);
            return;
        }

        if (apiKey == null) {
            System.err.println(MSG_MISSING_API_KEY);
            return;
        }

        Arguments arguments;

        if (sensorId != null && latitude == null && longitude == null) {
            arguments = new Arguments(apiKey, sensorId, history);
        } else if (sensorId == null && latitude != null && longitude != null) {
            arguments = new Arguments(apiKey, latitude, longitude, history);
        } else {
            System.err.println(MSG_MISSING_SENSOR_ID_OR_COORDINATES);
            return;
        }

        Core core = new Core(arguments);
        core.run();
    }

}
