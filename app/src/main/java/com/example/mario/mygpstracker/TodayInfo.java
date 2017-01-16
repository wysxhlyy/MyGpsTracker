package com.example.mario.mygpstracker;

import android.content.Intent;
import android.database.Cursor;
import android.icu.text.SimpleDateFormat;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;


/**
 * This activity is used to show the information of current day.
 */
public class TodayInfo extends AppCompatActivity {

    private Cursor cursor;
    private String[][] todayLoc;
    private int count;
    private int todayDistance;
    private int altitudeDiff;
    private double maxSpeed;
    private int tracks;

    private TextView moveDistance;
    private TextView altitude;
    private TextView speed;
    private TextView trackTime;

    private Button history;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today_info);

        initialize();
        getData();                                                                                  //get Data from database.
        getTodayInfo();                                                                             //get the information of current day.

        moveDistance.setText(todayDistance+"");                                                     //set the moving distance of current day.

        if(altitudeDiff==0){                                                                        //If the difference of altitude is 0, it means the altitude data did not recorded.
            altitude.setText("No Available Record");
            altitude.setTextSize(20);
        }else {
            altitude.setText(altitudeDiff+"");
        }

        if(maxSpeed==0){                                                                            //If the max speed of a day is 0, it means the speed information did not recorded.
            speed.setText("No Available Record");
            speed.setTextSize(20);
        }else {
            speed.setText(maxSpeed+"");
        }

        if(tracks==0){                                                                              //tracks indicates that how many times the user use the app to track his location in a day,if the track number is zero,it means user didn't use app that day.
            trackTime.setText("No Track Record Today");
            moveDistance.setText("No Track Record Today");
            altitude.setText("No Track Record Today");
            speed.setText("No Track Record Today");
            altitude.setTextSize(20);
            moveDistance.setTextSize(20);
            trackTime.setTextSize(20);
            speed.setTextSize(20);
        }else {
            trackTime.setText(tracks+"");
        }

        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(TodayInfo.this,TrackHistory.class);
                startActivity(intent);
            }
        });

    }


    /**
     * Initial the components.
     */
    public void initialize(){
        moveDistance=(TextView)findViewById(R.id.moveDistance);
        altitude=(TextView)findViewById(R.id.altitude);
        speed=(TextView)findViewById(R.id.speed);
        trackTime=(TextView)findViewById(R.id.trackTime);
        history=(Button)findViewById(R.id.history);
        tracks=0;
    }

    /**
     * Get data from database.
     */
    protected void getData() {
        String[] projection = new String[]{
                MyProviderContract._ID,
                MyProviderContract.LONGITUDE,
                MyProviderContract.LATITUDE,
                MyProviderContract.DATE,
                MyProviderContract.ALTITUDE,
                MyProviderContract.SPEED
        };

        cursor = getContentResolver().query(MyProviderContract.LOCATION_URI, projection, null, null, null);
    }

    /**
     * Get the information of current date.
     */
    protected void getTodayInfo() {
        Calendar calendar=Calendar.getInstance();
        int year=calendar.get(Calendar.YEAR);
        int month=calendar.get(Calendar.MONTH)+1;
        int day=calendar.get(Calendar.DAY_OF_MONTH);
        todayLoc = new String[20000][5];
        count=0;

        while (cursor.moveToNext()) {
            String getDateData = cursor.getString(cursor.getColumnIndex(MyProviderContract.DATE));
            String[] dateInDatabase = getDateData.split(" ")[0].split("-");
            int dayInDatabase = Integer.parseInt(dateInDatabase[0]);
            int monthInDatabase = Integer.parseInt(dateInDatabase[1]);
            int yearInDatabase = Integer.parseInt(dateInDatabase[2]);

            if (day == dayInDatabase && month == monthInDatabase && year == yearInDatabase) {
                todayLoc[count][0] = cursor.getString(cursor.getColumnIndex(MyProviderContract.LONGITUDE));
                todayLoc[count][1] = cursor.getString(cursor.getColumnIndex(MyProviderContract.LATITUDE));
                todayLoc[count][2] = cursor.getString(cursor.getColumnIndex(MyProviderContract.DATE));
                todayLoc[count][3] = cursor.getString(cursor.getColumnIndex(MyProviderContract.ALTITUDE));
                todayLoc[count][4] = cursor.getString(cursor.getColumnIndex(MyProviderContract.SPEED));
                count++;
                tracks=1;
            }
        }
            calculateDistance();
            calculateAltitudeAndSpeed();

    }


    /**
     * Calculate the distance according to the latitude and longitude.
     * Two locations will be set as the same single track if the difference of their record time is within 10 seconds.
     * The same single track means the two locations share the same start point and end point.
     */
    public void calculateDistance() {
        todayDistance = 0;

        for (int i = 0; i < count - 1; i++) {
            double long1 = Double.parseDouble(todayLoc[i][0]);
            double lat1 = Double.parseDouble(todayLoc[i][1]);
            double long2 = Double.parseDouble(todayLoc[i + 1][0]);
            double lat2 = Double.parseDouble(todayLoc[i + 1][1]);
            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");

            Date time1 = null;
            Date time2 = null;
            try {
                time1 = format.parse(todayLoc[i][2]);
                time2 = format.parse(todayLoc[i + 1][2]);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            float[] distBetweenTwoNodes = new float[1];
            Location.distanceBetween(lat1, long1, lat2, long2, distBetweenTwoNodes);

            long timediff = time2.getTime() - time1.getTime();
            timediff = timediff / 1000;


            if (Math.abs(timediff) <= 10) {                                                         //If the difference of record time less than 10 seconds,regarded as the same track.
                todayDistance += distBetweenTwoNodes[0];
            }else {
                tracks++;
            }
        }
    }

    /**
     * Calculate the altitude drop and max speed in a day.
     */
    public void calculateAltitudeAndSpeed(){
        altitudeDiff=0;
        maxSpeed=0;
        double maxAlt=0;
        double minAlt=10000;

        for (int i = 0; i < count - 1; i++) {
            Double getAltitude=Double.parseDouble(todayLoc[i][3]);
            Double getSpeed=Double.parseDouble(todayLoc[i][4]);
            Log.d("g53mdp",getAltitude+","+getSpeed);
            if(getAltitude>=maxAlt){
                maxAlt=getAltitude;
            }
            if(getAltitude<=minAlt){
                minAlt=getAltitude;
            }
            if(getSpeed>maxSpeed){
                maxSpeed=getSpeed;
            }
        }
        altitudeDiff=(int)maxAlt-(int)minAlt;

    }

}
