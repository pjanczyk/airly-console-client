package agh.cs.oop.airlyconsoleclient;

import agh.cs.oop.airlyconsoleclient.airlyapi.AllMeasurements;
import agh.cs.oop.airlyconsoleclient.airlyapi.Measurement;
import agh.cs.oop.airlyconsoleclient.airlyapi.MeasurementWithTime;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static java.lang.System.*;

public class AsciiPrinter {

    private static final double PM25_NORM = 25.0;
    private static final double PM10_NORM = 50.0;

    private static String ESC_RESET = "\033[0m";

    private static String[][] BIG_DIGITS = {{
            "╭────╮",
            "│    │",
            "│    │",
            "│    │",
            "╰────╯"
    }, {
            "     ╷",
            "     │",
            "     │",
            "     │",
            "     ╵"
    }, {

            "╶────╮",
            "     │",
            "╭────╯",
            "│     ",
            "╰────╴"
    }, {
            "╶────╮",
            "     │",
            " ────┤",
            "     │",
            "╶────╯"
    }, {
            "╷    ╷",
            "│    │",
            "╰────┤",
            "     │",
            "     ╵"
    }, {
            "╭────╴",
            "│     ",
            "╰────╮",
            "     │",
            "╶────╯"
    }, {
            "╭────╴",
            "│     ",
            "├────╮",
            "│    │",
            "╰────╯"
    }, {
            "╶────╮",
            "     │",
            "     │",
            "     │",
            "     ╵"
    }, {
            "╭────╮",
            "│    │",
            "├────┤",
            "│    │",
            "╰────╯"
    }, {
            "╭────╮",
            "│    │",
            "╰────┤",
            "     │",
            "╶────╯"
    }};

    private static final String[] BIG_DIGIT_PLACEHOLDER = {
            "      ",
            "      ",
            "      ",
            "      ",
            "      "
    };

    public void print(AllMeasurements allMeasurements, Arguments arguments) {
        if (arguments.sensorId != null) {
            out.println(StringUtils.center("Sensor ID: " + arguments.sensorId, 67));
        } else if (arguments.latitude != null && arguments.longitude != null) {
            out.println(StringUtils.center(String.format(
                    "Lat. %f° %c, Long. %f° %c",
                    Math.abs(arguments.latitude),
                    arguments.latitude >= 0 ? 'N' : 'S',
                    Math.abs(arguments.longitude),
                    arguments.longitude >= 0 ? 'E' : 'W'
            ), 67));
        }

        if (!arguments.history) {
            printMeasurement(allMeasurements.getCurrentMeasurements(), LocalDateTime.now());
        } else {
            for (MeasurementWithTime m : allMeasurements.getHistory()) {
                LocalDateTime dateTime = m.getFromDateTime()
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();
                printMeasurement(m.getMeasurements(), dateTime);
            }
        }
    }

    private void printMeasurement(Measurement measurement, LocalDateTime dateTime) {
        String dayOfWeek = DateTimeFormatter.ofPattern("EEEE", Locale.US).format(dateTime);
        String date = DateTimeFormatter.ofPattern("d MMMM", Locale.US).format(dateTime);
        String time = DateTimeFormatter.ofPattern("hh:mm a", Locale.US).format(dateTime);

        int caqi = (int) Math.round(measurement.getAirQualityIndex());
        int pm25 = (int) Math.round(measurement.getPm25());
        int pm10 = (int) Math.round(measurement.getPm10());
        int temperature = (int) Math.round(measurement.getTemperature());
        int pressure = (int) Math.round(measurement.getPressure() / 100.0);
        int humidity = (int) Math.round(measurement.getHumidity());

        int pm25Percentage = (int) Math.round(measurement.getPm25() / PM25_NORM * 100);
        int pm10Percentage = (int) Math.round(measurement.getPm10() / PM10_NORM * 100);

        String[] caqiDigit1;
        String[] caqiDigit2;
        if (caqi >= 0 && caqi <= 9) {
            caqiDigit1 = BIG_DIGIT_PLACEHOLDER;
            caqiDigit2 = BIG_DIGITS[caqi];
        } else if (caqi >= 10 && caqi <= 99) {
            caqiDigit1 = BIG_DIGITS[caqi / 10];
            caqiDigit2 = BIG_DIGITS[caqi % 10];
        } else {
            caqiDigit1 = BIG_DIGIT_PLACEHOLDER;
            caqiDigit2 = BIG_DIGIT_PLACEHOLDER;
        }

        String caqiFg;

        if (caqi > 100) {
            caqiFg = "\033[38;5;1m";
        } else if (caqi > 75) {
            caqiFg = "\033[38;5;202m";
        } else if (caqi > 50) {
            caqiFg = "\033[38;5;220m";
        } else if (caqi > 25) {
            caqiFg = "\033[38;5;112m";
        } else {
            caqiFg = "\033[38;5;40m";
        }

        /*
         * Example:
         * ┌──────────────┬──────────────────────────────────────────────────┐
         * │              │  ╶────╮  ╭────╮         PM2.5:  999 μg/m³  500%  │
         * │    Monday    │       │  │    │          PM10:  999 μg/m³  500%  │
         * │ 22 September │  ╭────╯  ╰────┤   TEMPERATURE:  -50°C            │
         * │   12:00 AM   │  │            │      PRESSURE:  1014 hPa         │
         * │              │  ╰────╴  ╶────╯      HUMIDITY:  100%             │
         * └──────────────┴──────────────────────────────────────────────────┘
         */
        out.println(String.format("" +
                        "┌──────────────┬──────────────────────────────────────────────────┐\n" +
                        "│              │  %11$s         PM2.5:  %4$s %9$s  │\n" +
                        "│%1$s│  %12$s          PM10:  %5$s %10$s  │\n" +
                        "│%2$s│  %13$s   TEMPERATURE:  %6$s       │\n" +
                        "│%3$s│  %14$s      PRESSURE:  %7$s       │\n" +
                        "│              │  %15$s      HUMIDITY:  %8$s       │\n" +
                        "└──────────────┴──────────────────────────────────────────────────┘",
                /* 1*/ StringUtils.center(dayOfWeek, 14),
                /* 2*/ StringUtils.center(date, 14),
                /* 3*/ StringUtils.center(time, 14),
                /* 4*/ StringUtils.rightPad(pm25 + " μg/m³", 10),
                /* 5*/ StringUtils.rightPad(pm10 + " μg/m³", 10),
                /* 6*/ StringUtils.rightPad(temperature + "°C", 10),
                /* 7*/ StringUtils.rightPad(pressure + " hPa", 10),
                /* 8*/ StringUtils.rightPad(humidity + "%", 10),
                /* 9*/ StringUtils.leftPad(pm10Percentage + "%", 4),
                /*10*/ StringUtils.leftPad(pm25Percentage + "%", 4),
                /*11*/ caqiFg + caqiDigit1[0] + "  " + caqiDigit2[0] + ESC_RESET,
                /*12*/ caqiFg + caqiDigit1[1] + "  " + caqiDigit2[1] + ESC_RESET,
                /*13*/ caqiFg + caqiDigit1[2] + "  " + caqiDigit2[2] + ESC_RESET,
                /*14*/ caqiFg + caqiDigit1[3] + "  " + caqiDigit2[3] + ESC_RESET,
                /*15*/ caqiFg + caqiDigit1[4] + "  " + caqiDigit2[4] + ESC_RESET
        ));
    }

}

