package com.rakshith.mapsPlaces;



public class Favourite {

    public static final String TABLE_NAME = "location";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_ADDRESS = "address";

    public double latitude;
    public double longitude;
    public  int id;
    public String locationAddress;


    // Create table SQL query
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_LATITUDE + " TEXT,"  + COLUMN_ADDRESS + " TEXT,"
                    + COLUMN_LONGITUDE + " TEXT"
                    + ")";


    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLocationAddress() {
        return locationAddress;
    }

    public void setLocationAddress(String locationAddress) {
        this.locationAddress = locationAddress;
    }

    @Override
    public String toString() {
        return "Favourite{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", id=" + id +
                ", locationAddress='" + locationAddress + '\'' +
                '}';
    }
}
