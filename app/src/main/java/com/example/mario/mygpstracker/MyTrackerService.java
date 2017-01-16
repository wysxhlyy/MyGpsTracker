package com.example.mario.mygpstracker;


import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Track service using google api.
 */
public class MyTrackerService extends Service implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,LocationListener {
    public MyTrackerService() {

    }

    public static final int ID = 1;

    private GoogleApiClient mGoogleApiClient;
    private IBinder binder;
    private boolean trackOrNot;
    private String updateTime;
    private LocationRequest locationReq;
    private Location nowLocation;

    private String savedLoc[][];
    private int saveCount;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return binder;
    }

    public void onCreate(){
        super.onCreate();
        binder=new MyBinder();

        savedLoc=new String[20000][5];
        saveCount=0;

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        createLocReq();
        onStart();
    }


    public class MyBinder extends Binder{
        void createLocReq(){
            MyTrackerService.this.createLocReq();
        }

        void updateLocation(){
            MyTrackerService.this.updateLocation();
        }

        void onLocationChanged(Location location){
            MyTrackerService.this.onLocationChanged(location);
        }

        void stopUpdateLocation(){
            MyTrackerService.this.stopUpdateLocation();
        }

        void onStart(){
            MyTrackerService.this.onStart();
        }

        void onStop(){
            MyTrackerService.this.onStop();
        }

        void onPause(){
            MyTrackerService.this.onPause();
        }

        void onResume(){
            MyTrackerService.this.onResume();
        }

        String[][] getSavedLocation(){
            return MyTrackerService.this.getSavedLocation();
        }

        int getSavedCount(){
            return MyTrackerService.this.getSaveCount();
        }

        GoogleApiClient getClient(){
            return MyTrackerService.this.getClient();
        }

        void saveLocation(){
            MyTrackerService.this.saveLocation();
        }

    }


    /**
     * Save the tracked locations into database.
     */
    protected void saveLocation(){
        ContentValues newLocationRecord=new ContentValues();
        for(int i=0;i<saveCount;i++){
            newLocationRecord.put(MyProviderContract.LONGITUDE,savedLoc[i][0]);
            newLocationRecord.put(MyProviderContract.LATITUDE,savedLoc[i][1]);
            newLocationRecord.put(MyProviderContract.DATE,savedLoc[i][2]);
            newLocationRecord.put(MyProviderContract.ALTITUDE,savedLoc[i][3]);
            newLocationRecord.put(MyProviderContract.SPEED,savedLoc[i][4]);

            getContentResolver().insert(MyProviderContract.LOCATION_URI,newLocationRecord);
        }
    }


    /**
     * Create the location request.
     * The location will be recorded every 5-10 s.
     */
    protected void createLocReq(){
        locationReq=new LocationRequest();
        locationReq.setInterval(10000);
        locationReq.setFastestInterval(5000);
        locationReq.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    /**
     * Start updating the location.
     */
    public void updateLocation(){
        try{
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,locationReq, this);
            Log.d("g53mdp","resume succeed");
        }catch (SecurityException e){
            Toast.makeText(MyTrackerService.this,"Failed to update location",Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Stop updating the location.
     */
    public void stopUpdateLocation(){
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
    }

    /**
     * Record the location information when location is changing.
     * The recorded information include latitude,longitude and time.
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {
        SimpleDateFormat sdf=new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        updateTime=sdf.format(new Date());
        savedLoc[saveCount][0]=location.getLongitude()+"";
        savedLoc[saveCount][1]=location.getLatitude()+"";
        savedLoc[saveCount][2]=updateTime;
        savedLoc[saveCount][3]=location.getAltitude()+"";
        savedLoc[saveCount][4]=location.getSpeed()+"";
        saveCount++;
    }


    protected void  onStart(){
        mGoogleApiClient.connect();
        trackOrNot=true;
    }

    protected void onStop(){
        mGoogleApiClient.disconnect();
        stopSelf();
        trackOrNot=false;
    }

    protected void onPause(){
        try{
            stopUpdateLocation();
        }catch (Exception e){
            Toast.makeText(this,"GoogleApiClient is not connected yet,Try Again",Toast.LENGTH_SHORT);
        }
        trackOrNot=false;
    }

    public void onResume(){
        if(mGoogleApiClient.isConnected()){
            updateLocation();
            trackOrNot=true;
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        try{
            nowLocation=LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if(nowLocation!=null){
                Log.d("g53mdp",nowLocation.getLatitude()+"");
                Log.d("g53mdp",nowLocation.getLongitude()+"");
            }
            updateLocation();

        }catch (SecurityException e){
            Toast.makeText(MyTrackerService.this,"Failed to get location",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    protected GoogleApiClient getClient(){
        return mGoogleApiClient;
    }

    protected String[][] getSavedLocation(){
        return savedLoc;
    }

    protected int getSaveCount(){
        return saveCount;
    }




}
