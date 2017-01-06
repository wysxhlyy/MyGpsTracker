package com.example.mario.mygpstracker;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MyTrackerService extends Service implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,LocationListener {
    public MyTrackerService() {

    }
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

        savedLoc=new String[2000][3];
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
        notification();
    }

    public void notification(){
        PendingIntent pi=PendingIntent.getActivity(this,0,new Intent(this,MainActivity.class),0);
        Resources r=getResources();
        Notification notification=new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.common_plus_signin_btn_icon_dark_disabled)
                .setContentTitle("Activity Tracker")
                .setContentTitle("Tracking...")
                .setContentIntent(pi)
                .setAutoCancel(false)
                .setDefaults(Notification.DEFAULT_SOUND)
                .build();

        NotificationManager nManager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        nManager.notify(0,notification);
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

    protected void  onStart(){
        mGoogleApiClient.connect();
        trackOrNot=true;
    }

    protected void onStop(){
        mGoogleApiClient.disconnect();
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

    protected void saveLocation(){
        ContentValues newLocationRecord=new ContentValues();
        for(int i=0;i<saveCount;i++){
            newLocationRecord.put(MyProviderContract.LONGITUDE,savedLoc[i][0]);
            newLocationRecord.put(MyProviderContract.LATITUDE,savedLoc[i][1]);
            newLocationRecord.put(MyProviderContract.DATE,savedLoc[i][2]);
            getContentResolver().insert(MyProviderContract.LOCATION_URI,newLocationRecord);
        }
    }


    protected void createLocReq(){
        locationReq=new LocationRequest();
        locationReq.setInterval(10000);
        locationReq.setFastestInterval(5000);
        locationReq.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    public void updateLocation(){
        try{
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,locationReq, this);
            Log.d("g53mdp","resume succeed");
        }catch (SecurityException e){
            Toast.makeText(MyTrackerService.this,"Failed to update location",Toast.LENGTH_SHORT).show();
        }
    }

    public void stopUpdateLocation(){
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
    }

    @Override
    public void onLocationChanged(Location location) {
        SimpleDateFormat sdf=new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        updateTime=sdf.format(new Date());
        savedLoc[saveCount][0]=location.getLongitude()+"";
        savedLoc[saveCount][1]=location.getLatitude()+"";
        savedLoc[saveCount][2]=updateTime;
        saveCount++;
        Log.d("g53mdp",location.getLatitude()+"");
        Log.d("g53mdp",location.getLongitude()+"");
        Log.d("g53mdp",updateTime);
    }



}
