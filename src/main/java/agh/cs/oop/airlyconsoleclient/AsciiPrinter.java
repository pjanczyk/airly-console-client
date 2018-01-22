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

    private static final String ESC_RESET = "\033[0m";

    private static final String[][] BIG_DIGITS = {{
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
            out.println(StringUtils.center("Sensor ID: " + arguments.sensorId, 75));
        } else if (arguments.latitude != null && arguments.longitude != null) {
            out.println(StringUtils.center(String.format(
                    "Lat. %f° %c, Long. %f° %c",
                    Math.abs(arguments.latitude),
                    arguments.latitude >= 0 ? 'N' : 'S',
                    Math.abs(arguments.longitude),
                    arguments.longitude >= 0 ? 'E' : 'W'
            ), 75));
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

        String[][] caqiDigits = { BIG_DIGIT_PLACEHOLDER, BIG_DIGIT_PLACEHOLDER, BIG_DIGIT_PLACEHOLDER };
        if (caqi >= 0 && caqi <= 999) {
            caqiDigits[0] = BIG_DIGITS[caqi % 10];
            if (caqi >= 10) {
                caqiDigits[1] = BIG_DIGITS[caqi / 10 % 10];
            }
            if (caqi >= 100) {
                caqiDigits[2] = BIG_DIGITS[caqi / 100];
            }
        }

        String caqiFg;

        switch (measurement.getPollutionLevel()) {
            case 1:
                caqiFg ="\033[38;5;10m";
                break;
            case 2:
                caqiFg ="\033[38;5;142m";
                break;
            case 3:
                caqiFg ="\033[38;5;214m";
                break;
            case 4:
                caqiFg ="\033[38;5;202m";
                break;
            case 5:
                caqiFg ="\033[38;5;1m";
                break;
            case 6:
                caqiFg ="\033[38;5;52m";
                break;
            default:
                caqiFg = "";
        }

        /*
         * Example:
         * ┌──────────────┬──────────────────────────────────────────────────────────┐
         * │              │  ╷    ╷  ╶────╮  ╭────╮         PM2.5:  999 μg/m³  500%  │
         * │    Monday    │  │    │       │  │    │          PM10:  999 μg/m³  500%  │
         * │ 22 September │  ╰────┤  ╭────╯  ╰────┤   TEMPERATURE:  -50°C            │
         * │   12:00 AM   │       │  │            │      PRESSURE:  1014 hPa         │
         * │              │       ╵  ╰────╴  ╶────╯      HUMIDITY:  100%             │
         * └──────────────┴──────────────────────────────────────────────────────────┘
         */
        out.println(String.format("" +
                        "┌──────────────┬──────────────────────────────────────────────────────────┐\n" +
                        "│              │  %11$s         PM2.5:  %4$s %9$s  │\n" +
                        "│%1$s│  %12$s          PM10:  %5$s %10$s  │\n" +
                        "│%2$s│  %13$s   TEMPERATURE:  %6$s       │\n" +
                        "│%3$s│  %14$s      PRESSURE:  %7$s       │\n" +
                        "│              │  %15$s      HUMIDITY:  %8$s       │\n" +
                        "└──────────────┴──────────────────────────────────────────────────────────┘",
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
                /*11*/ caqiFg + caqiDigits[2][0] + "  " + caqiDigits[1][0] + "  " + caqiDigits[0][0] + ESC_RESET,
                /*12*/ caqiFg + caqiDigits[2][1] + "  " + caqiDigits[1][1] + "  " + caqiDigits[0][1] + ESC_RESET,
                /*13*/ caqiFg + caqiDigits[2][2] + "  " + caqiDigits[1][2] + "  " + caqiDigits[0][2] + ESC_RESET,
                /*14*/ caqiFg + caqiDigits[2][3] + "  " + caqiDigits[1][3] + "  " + caqiDigits[0][3] + ESC_RESET,
                /*15*/ caqiFg + caqiDigits[2][4] + "  " + caqiDigits[1][4] + "  " + caqiDigits[0][4] + ESC_RESET
        ));
    }

}

