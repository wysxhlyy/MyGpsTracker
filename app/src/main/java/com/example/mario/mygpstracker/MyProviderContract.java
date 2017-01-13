package com.example.mario.mygpstracker;

import android.net.Uri;


public class MyProviderContract {

    public static final String AUTHORITY="com.example.mario.mygpstracker.MyContentProvider";

    public static final Uri LOCATION_URI=Uri.parse("content://"+AUTHORITY+"/locationRecords");

    public static final String _ID="_id";
    public static final String LONGITUDE="longitude";
    public static final String LATITUDE="latitude";
    public static final String DATE="time";

}
