package agh.cs.oop.airlyconsoleclient.airlyapi;

public class Measurement {
    private double airQualityIndex;
    private double pm1;
    private double pm25;
    private double pm10;
    private double pressure;
    private double humidity;
    private double temperature;
    private int polutionLevel;

    public double getAirQualityIndex() {
        return airQualityIndex;
    }

    public double getPm1() {
        return pm1;
    }

    public double getPm25() {
        return pm25;
    }

    public double getPm10() {
        return pm10;
    }

    public double getPressure() {
        return pressure;
    }

    public double getHumidity() {
        return humidity;
    }

    public double getTemperature() {
        return temperature;
    }

    public int getPolutionLevel() {
        return polutionLevel;
    }
}
