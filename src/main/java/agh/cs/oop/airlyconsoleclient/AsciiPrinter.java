package agh.cs.oop.airlyconsoleclient;

import agh.cs.oop.airlyconsoleclient.airlyapi.AllMeasurements;
import agh.cs.oop.airlyconsoleclient.airlyapi.Measurement;
import agh.cs.oop.airlyconsoleclient.airlyapi.MeasurementWithTime;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.stream.DoubleStream;

import static java.lang.System.*;
import static java.util.stream.Collectors.*;

public class AsciiPrinter {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm", Locale.UK);
    private static final DateTimeFormatter DAY_OF_WEEK_FORMATTER = DateTimeFormatter.ofPattern("EEEE", Locale.UK);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMMM", Locale.UK);

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
            "   ╷  ",
            "   │  ",
            "   │  ",
            "   │  ",
            "   ╵  "
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
            out.println("Sensor ID: " + arguments.sensorId);
        } else if (arguments.latitude != null && arguments.longitude != null) {
            out.println(String.format(
                    "Lat. %f° %c, Long. %f° %c",
                    Math.abs(arguments.latitude),
                    arguments.latitude >= 0 ? 'N' : 'S',
                    Math.abs(arguments.longitude),
                    arguments.longitude >= 0 ? 'E' : 'W'
            ));
        }

        if (!arguments.history) {
            printMeasurement(allMeasurements.getCurrentMeasurements(), LocalDateTime.now());
        } else {
            if (allMeasurements.getHistory() == null || allMeasurements.getHistory().size() == 0) return;

            LocalDateTime historyStartDate = allMeasurements.getHistory()
                    .get(0)
                    .getFromDateTime()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();

            LocalDateTime historyEndDate = allMeasurements.getHistory()
                    .get(allMeasurements.getHistory().size() - 1)
                    .getFromDateTime()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
                    .plusHours(1);

            List<Measurement> history = allMeasurements.getHistory().stream()
                    .map(MeasurementWithTime::getMeasurements)
                    .collect(toList());
            history.add(allMeasurements.getCurrentMeasurements());

            // CAQI chart
            double[] values = history.stream()
                    .mapToDouble(Measurement::getAirQualityIndex)
                    .toArray();
            double max = DoubleStream.of(values).max().orElse(100);
            Integer[] colors = history.stream()
                    .mapToInt(Measurement::getPollutionLevel)
                    .mapToObj(this::getPollutionLevelColor)
                    .toArray(Integer[]::new);

            printChartTitle("CAQI");
            printChart(values, 0.0, max, 10.0, colors);
            printChartTimeline(historyStartDate, historyEndDate);
            out.println();

            // PM2.5 chart
            values = history.stream()
                    .mapToDouble(Measurement::getPm25)
                    .toArray();
            max = DoubleStream.of(values).max().orElse(200);

            printChartTitle("PM2.5 [μg/m³]");
            printChart(values, 0.0, max, 10.0, null);
            printChartTimeline(historyStartDate, historyEndDate);
            out.println();

            // PM10 chart
            values = history.stream()
                    .mapToDouble(Measurement::getPm10)
                    .toArray();
            max = DoubleStream.of(values).max().orElse(100);

            printChartTitle("PM10 [μg/m³]");
            printChart(values, 0.0, max, 20.0, null);
            printChartTimeline(historyStartDate, historyEndDate);
        }
    }

    private void printMeasurement(Measurement measurement, LocalDateTime dateTime) {
        String dayOfWeek = DAY_OF_WEEK_FORMATTER.format(dateTime);
        String date = DATE_FORMATTER.format(dateTime);
        String time = TIME_FORMATTER.format(dateTime);

        int caqi = (int) Math.round(measurement.getAirQualityIndex());
        int pm25 = (int) Math.round(measurement.getPm25());
        int pm10 = (int) Math.round(measurement.getPm10());
        int temperature = (int) Math.round(measurement.getTemperature());
        int pressure = (int) Math.round(measurement.getPressure() / 100.0);
        int humidity = (int) Math.round(measurement.getHumidity());

        int pm25Percentage = (int) Math.round(measurement.getPm25() / PM25_NORM * 100);
        int pm10Percentage = (int) Math.round(measurement.getPm10() / PM10_NORM * 100);

        String[][] caqiDigits = {BIG_DIGIT_PLACEHOLDER, BIG_DIGIT_PLACEHOLDER, BIG_DIGIT_PLACEHOLDER};
        if (caqi >= 0 && caqi <= 999) {
            caqiDigits[0] = BIG_DIGITS[caqi % 10];
            if (caqi >= 10) {
                caqiDigits[1] = BIG_DIGITS[caqi / 10 % 10];
            }
            if (caqi >= 100) {
                caqiDigits[2] = BIG_DIGITS[caqi / 100];
            }
        }

        Integer pollutionLevelColor = getPollutionLevelColor(measurement.getPollutionLevel());
        String caqiFg = pollutionLevelColor != null
                ? "\033[38;5;" + pollutionLevelColor + "m"
                : "";

        /*
         * Example:
         * ┌──────────────┬──────────────────────────────────────────────────────────┐
         * │              │  ╷    ╷  ╶────╮  ╭────╮         PM2.5:  999 μg/m³  500%  │
         * │    Monday    │  │    │       │  │    │          PM10:  999 μg/m³  500%  │
         * │ 22 September │  ╰────┤  ╭────╯  ╰────┤   TEMPERATURE:  -50°C            │
         * │   12:00 AM   │       │  │            │      PRESSURE:  1014 hPa         │
         * │              │       ╵  ╰────╴  ╶────╯      HUMIDITY:  100%             │
         * └──────────────┴──────────────────────────────────────────────────────────┘
         * █▄
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

    private void printChartTitle(String title) {
        out.println("\033[1m" + StringUtils.center(title, 56) + ESC_RESET);
    }

    private void printChartTimeline(LocalDateTime startDate, LocalDateTime endDate) {
        LocalDateTime midDate = startDate.plus(ChronoUnit.MINUTES.between(startDate, endDate) / 2, ChronoUnit.MINUTES);
        out.println("    "
                + StringUtils.rightPad(TIME_FORMATTER.format(startDate), 17)
                + StringUtils.center(TIME_FORMATTER.format(midDate), 18)
                + StringUtils.leftPad("NOW", 17));
    }

    private void printChart(double[] values, double min, double max, double step, @Nullable Integer[] colors) {
        final String ESC_GRID_FG = "\033[38;5;238m";

        int steps = (int) Math.ceil((max - min) / step) + 1;

        for (int r = 0; r < steps; r++) {
            double rval = (steps - r - 1) * step;
            out.print(StringUtils.leftPad(Integer.toString((int) Math.round(rval)), 3));

            if (r == 0)
                out.print(ESC_GRID_FG + " ┼─" + ESC_RESET);
            else if (r == steps - 1)
                out.print(ESC_GRID_FG + " ┴─" + ESC_RESET);
            else
                out.print(ESC_GRID_FG + " ┼─" + ESC_RESET);

            for (int c = 0; c < values.length; c++) {
                double normalized = values[c] - min; // normalized is in range [0, 1]

                final String ESC_BAR_FG = colors != null && colors[c] != null
                        ? "\033[38;5;" + colors[c] + "m"
                        : "";

                if (r == steps - 1) {
                    if (normalized >= step / 2.0)
                        out.print(ESC_BAR_FG + "▀" + ESC_GRID_FG + "─" + ESC_RESET);
                    else
                        out.print(ESC_GRID_FG + "──" + ESC_RESET);
                } else {
                    if (normalized >= rval + step / 2.0)
                        out.print(ESC_BAR_FG + "█" + ESC_GRID_FG + "─" + ESC_RESET);
                    else if (normalized >= rval)
                        out.print(ESC_BAR_FG + "▄" + ESC_GRID_FG + "─" + ESC_RESET);
                    else
                        out.print(ESC_GRID_FG + "──" + ESC_RESET);
                }
            }

            out.println();
        }
    }

    private Integer getPollutionLevelColor(int pollutionLevel) {
        switch (pollutionLevel) {
            case 1:
                return 10;
            case 2:
                return 142;
            case 3:
                return 214;
            case 4:
                return 202;
            case 5:
                return 1;
            case 6:
                return 52;
            default:
                return null;
        }
    }

}

