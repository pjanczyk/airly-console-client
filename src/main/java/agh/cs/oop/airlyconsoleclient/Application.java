package agh.cs.oop.airlyconsoleclient;

import org.apache.commons.cli.*;

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

        String apiKey = cmd.getOptionValue("api-key");
        String sensorId = cmd.getOptionValue("sensor-id");
        String latitude = cmd.getOptionValue("latitude");
        String longitude = cmd.getOptionValue("longitude");
        boolean history = cmd.hasOption("history");

        if (apiKey == null) {
            apiKey = System.getenv("API_KEY");
        }

        if (apiKey == null) {
            System.err.println(MSG_MISSING_API_KEY);
        }

        if (sensorId != null && latitude == null && longitude == null) {

        } else if (sensorId == null && latitude != null && longitude != null) {

        } else {
            System.err.println(MSG_INVALID_ARGS);
        }
    }
}
