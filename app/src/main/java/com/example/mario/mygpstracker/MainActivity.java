package com.example.mario.mygpstracker;

import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,LocationListener{

    private GoogleApiClient mGoogleApiClient;
    private Location nowLocation;
    private boolean trackOrNot;
    private Button startLoc;
    private String updateTime;
    private LocationRequest locationReq;


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

        createLocReq();

        trackOrNot=true;



    }


    protected void  onStart(){
        mGoogleApiClient.connect();
        super.onStart();
        trackOrNot=true;
    }

    protected void onStop(){
        mGoogleApiClient.disconnect();
        super.onStop();
        trackOrNot=false;
    }

    protected void onPause(){
        super.onPause();
        stopUpdateLocation();
    }

    public void onResume(){
        super.onResume();
        if(!trackOrNot&&mGoogleApiClient.isConnected()){
            updateLocation();
        }
    }

    public void onConnected(Bundle con){
        try{
            nowLocation=LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if(nowLocation!=null){
                Log.d("g53mdp",nowLocation.getLatitude()+"");
                Log.d("g53mdp",nowLocation.getLongitude()+"");
            }
        }catch (SecurityException e){
            Toast.makeText(MainActivity.this,"Failed to get location",Toast.LENGTH_SHORT).show();
        }

        if(trackOrNot){
            updateLocation();
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
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,locationReq, (LocationListener) this);
        }catch (SecurityException e){
            Toast.makeText(MainActivity.this,"Failed to update location",Toast.LENGTH_SHORT).show();
        }
    }

    public void stopUpdateLocation(){
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, (LocationListener) this);
    }

    public void onLocationChanged(Location location){
        nowLocation=location;
        SimpleDateFormat sdf=new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        updateTime=sdf.format(new Date());
        Log.d("g53mdp",nowLocation.getLatitude()+"");
        Log.d("g53mdp",nowLocation.getLongitude()+"");
        Log.d("g53mdp",updateTime);
    }






    public void onConnectionSuspended(int con){

    }

    public void onConnectionFailed(ConnectionResult cr){
    }
}

