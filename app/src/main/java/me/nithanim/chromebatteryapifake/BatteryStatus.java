package me.nithanim.chromebatteryapifake;

public class BatteryStatus {
    private boolean charging;
    private double chargingTime;
    private double dischargingTime;
    private double level;

    public BatteryStatus(boolean charging, double chargingTime, double dischargingTime, double level) {
        this.charging = charging;
        this.chargingTime = chargingTime;
        this.dischargingTime = dischargingTime;
        this.level = level;
    }

    public boolean isCharging() {
        return charging;
    }

    public double getChargingTime() {
        return chargingTime;
    }

    public double getDischargingTime() {
        return dischargingTime;
    }

    public double getLevel() {
        return level;
    }
}
