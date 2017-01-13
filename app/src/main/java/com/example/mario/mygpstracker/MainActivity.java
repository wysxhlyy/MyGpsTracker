package com.example.mario.mygpstracker;

import android.content.Intent;
import android.database.Cursor;
import android.icu.text.SimpleDateFormat;
import android.location.Location;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;


import java.text.ParseException;
import java.util.Date;


/**
 * MainActivity here is acted as a controller or an entrance of the app. No actual use.
 * Google map in this activity is only used for showing the user's position.
 * After click start Track button,the track service will start after 3 seconds.
 */

public class MainActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,OnMapReadyCallback{

    static final int ACTIVITY_TRACKER_REQUEST_CODE=1;
    static final int ACTIVITY_HISTORY_REQUEST_CODE=2;


    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;
    private Location nowLocation;

    private Button start;
    private Button history;

    private TextView process;

    private Cursor cursor;
    private String[][] todayLoc;
    private float todayDistance;

    private int countDownInt=4;
    private Handler h;
    private int count;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }


        start=(Button)findViewById(R.id.start);
        history=(Button)findViewById(R.id.history);
        process=(TextView)findViewById(R.id.process);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                countDown();
            }
        });

        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this,TrackHistory.class);
                startActivityForResult(intent,ACTIVITY_HISTORY_REQUEST_CODE);
            }
        });
        try {
            getTodayInfo();                                                                         //Get the user moving distance today.
        } catch (ParseException e) {
            e.printStackTrace();
        }

        MapFragment mapFragment=(MapFragment)getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);          //Add the google map.

        mMap=mapFragment.getMap();
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

    }

    /**
     * Get the user moving distance today.
     * @throws ParseException
     */
    private void getTodayInfo() throws ParseException {
        todayLoc=new String[2000][3];
        todayDistance=0;
        String[] projection=new String[]{
                MyProviderContract._ID,
                MyProviderContract.LONGITUDE,
                MyProviderContract.LATITUDE,
                MyProviderContract.DATE
        };

        count=0;

        cursor=getContentResolver().query(MyProviderContract.LOCATION_URI,projection,null,null,null);

        while (cursor.moveToNext()){
            Log.d("g53mdp",count+":"+cursor.getString(1)+","+cursor.getString(2)+","+cursor.getString(cursor.getColumnIndex(MyProviderContract.DATE)));
            todayLoc[count][0]=cursor.getString(cursor.getColumnIndex(MyProviderContract.LONGITUDE));
            todayLoc[count][1]=cursor.getString(cursor.getColumnIndex(MyProviderContract.LATITUDE));
            todayLoc[count][2]=cursor.getString(cursor.getColumnIndex(MyProviderContract.DATE));

            count++;
        }

        Log.d("g53mdp","cursor count"+cursor.getCount()+"count:"+count);

        calculateDistance();

        process.setText((int)todayDistance+"");
    }

    /**
     * Calculate the distance according to the latitude and longitude.
     * Two locations will be set as the same single track if the difference of their record time is within 10 seconds.
     * The same single track means the two locations share the same start point and end point.
     */
    public void calculateDistance() {
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
            Log.d("g53mdp", i + ":" + distBetweenTwoNodes[0] + "," + "timediff:" + timediff);


            if (Math.abs(timediff) <= 10) {                 //If the record time less than 10 seconds,regarded as the same track.
                todayDistance += distBetweenTwoNodes[0];
            }

        }
    }

    /**
     * Refresh the moving distance.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    protected void onActivityResult(int requestCode,int resultCode,Intent data){

        if(requestCode==ACTIVITY_HISTORY_REQUEST_CODE||requestCode==ACTIVITY_TRACKER_REQUEST_CODE){
            if(resultCode==RESULT_CANCELED){
                recreate();
                Log.d("g53mdp","back");
            }
        }
    }

    /**
     * Set a countdown before start the service
     */
    public void countDown(){
        h=new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                h.post(new Runnable() {
                    @Override
                    public void run() {
                        countDownInt--;
                        if(countDownInt>0){
                            process.setText(countDownInt+"");
                            start.setText("Starting..");
                            start.setEnabled(false);
                            countDown();
                        }else if(countDownInt==0){
                            process.setText("Start!");
                            countDown();
                        }else {
                            Intent intent=new Intent(MainActivity.this,MyTracker.class);
                            Bundle bundle=new Bundle();
                            if(nowLocation!=null){
                                bundle.putString("latitude",nowLocation.getLatitude()+"");
                                bundle.putString("longitude",nowLocation.getLongitude()+"");
                            }
                            intent.putExtras(bundle);
                            startActivityForResult(intent,ACTIVITY_TRACKER_REQUEST_CODE);
                        }
                    }
                });
            }
        }).start();
    }



    /*
        Used to set the google map.
     */
    public void onMapReady(GoogleMap map){
        //map.addMarker(new MarkerOptions().position(new LatLng(nowLocation.getLongitude(),nowLocation.getLatitude())).title("Your position"));
        mMap=map;
        Log.d("g53mdp","map added");
        try{
            map.setMyLocationEnabled(true);     //Enable to find the current location
        }catch (SecurityException e){
            //ask for permission
        }

    }

    protected void  onStart(){
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop(){
        mGoogleApiClient.disconnect();
        super.onStop();
    }


    public void onConnected(Bundle con){
        Log.d("g53mdp","connected");
        try{
            nowLocation=LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if(nowLocation!=null){
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(nowLocation.getLatitude(),nowLocation.getLongitude()),15.0f));
                Log.d("g53mdp",nowLocation.getLatitude()+"");
                Log.d("g53mdp",nowLocation.getLongitude()+"");
            }
        }catch (SecurityException e){
            Toast.makeText(MainActivity.this,"Failed to get location",Toast.LENGTH_SHORT).show();
        }
    }

    public void onConnectionSuspended(int con){

    }

    public void onConnectionFailed(ConnectionResult cr){
    }
}

