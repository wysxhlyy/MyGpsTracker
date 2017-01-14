package com.example.mario.mygpstracker;

import android.database.Cursor;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.ParseException;
import java.util.Date;


/**
 * Used to visually display the track record for a specified day.
 * Choose the data using datePicker and the Google map below will show the track record of that day.
 */
public class ShowRoute extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,OnMapReadyCallback {

    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;
    private Cursor cursor;

    private Double dlat;
    private Double dlong;

    private DatePicker datePicker;
    private Button chooseDate;
    private String chosenDate = " ";
    private boolean historyExist = false;

    private String[][] todayLoc;
    private float todayDistance;
    private int count;
    private PolylineOptions route;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_route);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);                                                              //Add the google map.

        mMap = mapFragment.getMap();
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        datePicker = (DatePicker) findViewById(R.id.datePicker);
        chooseDate = (Button) findViewById(R.id.chooseDate);

        chooseDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.clear();                                                                       //When choose a new date,the map should be clear first.
                count = 0;
                todayLoc = null;
                int month = datePicker.getMonth() + 1;                                                  //get the day,month and year information, set them as "int" to avoid the judge problem like "01-01-2017"!="1-1-2017".
                int day = datePicker.getDayOfMonth();
                int year = datePicker.getYear();
                drawRoute(day, month, year);
            }
        });

        getData();                                                                                  //Get data from database.

    }

    /**
     * Get data from database.
     */
    protected void getData() {
        String[] projection = new String[]{
                MyProviderContract._ID,
                MyProviderContract.LONGITUDE,
                MyProviderContract.LATITUDE,
                MyProviderContract.DATE
        };

        String[] columnsToDisplay = new String[]{
                MyProviderContract.LATITUDE,
                MyProviderContract.LONGITUDE,
                MyProviderContract.DATE
        };

        int[] colRedIds = new int[]{
                R.id.value1,
                R.id.value2,
                R.id.value3
        };

        cursor = getContentResolver().query(MyProviderContract.LOCATION_URI, projection, null, null, null);
    }

    /**
     * Draw the route on google map for a specified day.
     * The day could set by day,month and year,and in real situation,the user could choose the day.
     *
     * @param day
     * @param month
     * @param year
     */
    protected void drawRoute(int day, int month, int year) {
        route = new PolylineOptions();
        todayLoc = new String[20000][3];                                                               //todayLoc stores all the location information of that day.

        while (cursor.moveToNext()) {
            String getDateData = cursor.getString(cursor.getColumnIndex(MyProviderContract.DATE));
            String[] dateInDatabase = getDateData.split(" ")[0].split("-");
            int dayInDatabase = Integer.parseInt(dateInDatabase[0]);
            int monthInDatabase = Integer.parseInt(dateInDatabase[1]);
            int yearInDatabase = Integer.parseInt(dateInDatabase[2]);

            if (day == dayInDatabase && month == monthInDatabase && year == yearInDatabase) {
                formatData();
                route.add(new LatLng(dlat, dlong));
                historyExist = true;
                todayLoc[count][0] = cursor.getString(cursor.getColumnIndex(MyProviderContract.LONGITUDE));
                todayLoc[count][1] = cursor.getString(cursor.getColumnIndex(MyProviderContract.LATITUDE));
                todayLoc[count][2] = cursor.getString(cursor.getColumnIndex(MyProviderContract.DATE));
                count++;
            }
        }
        cursor.moveToFirst();

        if (historyExist) {
            calculateDistance();
            changeColor();
            mMap.addPolyline(route);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(dlat, dlong), 12.0f));
            historyExist = false;
            Toast.makeText(ShowRoute.this, "Your move " + (int) todayDistance + " meters in that day", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(ShowRoute.this, "Cannot Find Any Record", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Change the color of displayed route according to the moving distance to indicate
     * that user had experienced a easy day or tough day.
     */
    public void changeColor() {
        route.width(15);
        if (todayDistance > 5000) {
            route.color(Color.RED);
        } else if (todayDistance < 1000) {
            route.color(Color.GREEN);
        } else {
            route.color(Color.BLUE);
        }
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
            }
        }
    }

    /**
     * Transfer data from String to double.
     */
    protected void formatData() {
        String latitude = cursor.getString(cursor.getColumnIndex(MyProviderContract.LATITUDE));
        String longitude = cursor.getString(cursor.getColumnIndex(MyProviderContract.LONGITUDE));
        dlat = Double.parseDouble(latitude);
        dlong = Double.parseDouble(longitude);
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    /**
     * Set the Google map.
     *
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Log.d("g53mdp", "map added");
        try {
            googleMap.setMyLocationEnabled(true);                                                   //Enable to find the current location
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}

