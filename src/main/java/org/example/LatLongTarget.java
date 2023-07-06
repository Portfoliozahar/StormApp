package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LatLongTarget {
    private int id;
    private double latitude;
    private double longitude;

    public LatLongTarget(int id, double latitude, double longitude) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getId() {
        return id;
    }

    public double getLat() {
        return latitude;
    }

    public double getLong() {
        return longitude;
    }
}
