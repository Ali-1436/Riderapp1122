package com.example.riderpanel.Model;
import com.firebase.geofire.GeoLocation;

public class DriverGeoModel {
    private String Key;
    private GeoLocation geolocation;
    private DriverInfoModel driverinfoModel;



    public String getKey() {
        return Key;
    }

    public void setKey(String key) {
        Key = key;
    }

    public GeoLocation getGeolocation() {
        return geolocation;
    }

    public void setGeolocation(GeoLocation geolocation) {
        this.geolocation = geolocation;
    }

    public DriverGeoModel() {

    }


    public DriverGeoModel(String key, GeoLocation geolocation) {
        Key = key;
        this.geolocation = geolocation;

    }

    public <T> void setDriverInfoModel(DriverInfoModel driverinfoModel) {
        this.driverinfoModel = driverinfoModel;
    }


    public void getDriverInfoModel() {
   return ;
    }

}
