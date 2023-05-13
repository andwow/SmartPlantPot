package com.timusandrei.smartpot.models;

import java.time.LocalDate;
import java.time.LocalTime;

public final class DeviceData {

    public DeviceData(long moisture, double temperature, LocalDate date, LocalTime time) {
        this.moisture = moisture;
        this.temperature = temperature;
        this.date = date;
        this.time = time;
    }

    public long getMoisture() {
        return moisture;
    }

    public double getTemperature() {
        return temperature;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getTime() {
        return time;
    }

    final long moisture;
    final double temperature;
    final LocalDate date;
    final LocalTime time;
}
