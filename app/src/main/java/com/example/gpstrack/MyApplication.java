package com.example.gpstrack;
import android.app.Application;
import android.location.Location;

import java.util.ArrayList;
import java.util.List;


public class MyApplication extends Application {

    private static MyApplication singleton;

    public List<Location> getMyLocations() {
        return myLocations;
    }

    public void setMyLocations(List<Location> myLocation) {
        this.myLocations = myLocation;
    }

    private List<Location> myLocations;

    public MyApplication getInstance() {
        return singleton;
    }

    public void onCreate() {
        super.onCreate();
        singleton = this;
        myLocations = new ArrayList<>();
    }
}
