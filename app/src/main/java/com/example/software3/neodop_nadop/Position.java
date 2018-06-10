package com.example.software3.neodop_nadop;

public class Position {
    private double longitude;
    private double latitude;
    private boolean met;
    private boolean finished;
    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public boolean getMet(){return met;}

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public Position(){

    }
    public Position(double latitude, double longitude,boolean met) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.met = met;
        this.finished = finished;
    }

}
