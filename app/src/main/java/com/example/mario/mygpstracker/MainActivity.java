package com.example.mario.mygpstracker;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
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
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;


public class MainActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,LocationListener,OnMapReadyCallback{

    private GoogleApiClient mGoogleApiClient;
    private Location nowLocation;
    private boolean trackOrNot;
    private Button startLoc;
    private String updateTime;
    private LocationRequest locationReq;

    private MapFragment map;


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

       /* map=MapFragment.newInstance();
        FragmentTransaction fragmentTransaction=getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.map,map);
        fragmentTransaction.commit();*/

        MapFragment mapFragment=(MapFragment)getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);          //Add the google map.




    }

    /*
        Used to set the google map.
     */
    public void onMapReady(GoogleMap map){
        //map.addMarker(new MarkerOptions().position(new LatLng(nowLocation.getLongitude(),nowLocation.getLatitude())).title("Your position"));
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        try{
            map.setMyLocationEnabled(true);     //Enable to find the current location
        }catch (SecurityException e){
            //ask for permission
        }

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
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,locationReq, this);
        }catch (SecurityException e){
            Toast.makeText(MainActivity.this,"Failed to update location",Toast.LENGTH_SHORT).show();
        }
    }

    public void stopUpdateLocation(){
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
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

