package agh.cs.oop.airlyconsoleclient.airlyapi;

import java.util.Date;

public class MeasurementWithTime {
    private Date fromDateTime;
    private Date tillDateTime;
    private Measurement measurements;

    public Date getFromDateTime() {
        return fromDateTime;
    }

    public Date getTillDateTime() {
        return tillDateTime;
    }

    public Measurement getMeasurements() {
        return measurements;
    }
}
