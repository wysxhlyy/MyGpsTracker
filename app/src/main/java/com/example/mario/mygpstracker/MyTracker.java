package com.example.mario.mygpstracker;

import android.content.Intent;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

import java.text.SimpleDateFormat;
import java.util.Date;

public class MyTracker extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,LocationListener,OnMapReadyCallback,View.OnClickListener {

    private GoogleApiClient mGoogleApiClient;
    private Location nowLocation;
    private boolean trackOrNot;
    private String updateTime;
    private LocationRequest locationReq;
    private MapFragment mapFragment;

    private Button pause;
    private Button save;
    private Button cancel;

    private String[][] savedLoc;
    private int savedCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_tracker);
        updateValue(savedInstanceState);
        initialize();


        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        createLocReq();

        trackOrNot=true;

        mapFragment.getMapAsync(this);          //Add the google map.

        cancel.setOnClickListener(this);
        pause.setOnClickListener(this);
        save.setOnClickListener(this);

    }

    private void updateValue(Bundle bundle){
        if(bundle!=null){
            if(bundle.keySet().contains("requstLocationUpdate")){
                trackOrNot=bundle.getBoolean("requestLocationUpdate");
            }

            if(bundle.keySet().contains("nowLocation")){
                nowLocation=bundle.getParcelable("nowLocation");
            }

            if(bundle.keySet().contains("lastUpdateTime")){
                updateTime=bundle.getString("lastUpdateTime");
            }
        }
    }

    public void initialize(){
        cancel=(Button)findViewById(R.id.cancel);
        pause=(Button)findViewById(R.id.pause);
        save=(Button)findViewById(R.id.save);
        mapFragment=(MapFragment)getFragmentManager().findFragmentById(R.id.map);
        savedLoc=new String[2000][3];
        savedCount=0;

    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.cancel:
                Intent intent=new Intent(MyTracker.this,MainActivity.class);
                startActivity(intent);
                //stop the track
                break;
            case R.id.pause:
                if(pause.getText().equals("pause")){
                    onPause();
                    pause.setText("resume");
                }else if(pause.getText().equals("resume")){
                    onStart();
                    onResume();
                    pause.setText("pause");
                }
                break;
            case R.id.save:
                for(int i=0;i<savedCount;i++){
                    Log.d("g53mdp",savedLoc[i][0]+","+savedLoc[i][1]+","+savedLoc[i][2]);
                }

        }
    }


    public void onMapReady(GoogleMap map){
        //map.addMarker(new MarkerOptions().position(new LatLng(nowLocation.getLongitude(),nowLocation.getLatitude())).title("Your position"));
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        try{
            map.setMyLocationEnabled(true);     //Enable to find the current location
        }catch (SecurityException e){
            //ask for permission
        }

    }

    protected void createLocReq(){
        locationReq=new LocationRequest();
        locationReq.setInterval(5000);
        locationReq.setFastestInterval(1000);
        locationReq.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void onLocationChanged(Location location){   //update the location
        nowLocation=location;
        SimpleDateFormat sdf=new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        updateTime=sdf.format(new Date());
        savedLoc[savedCount][0]=nowLocation.getLongitude()+"";
        savedLoc[savedCount][1]=nowLocation.getLatitude()+"";
        savedLoc[savedCount][2]=updateTime;
        savedCount++;

        Log.d("g53mdp",nowLocation.getLatitude()+"");
        Log.d("g53mdp",nowLocation.getLongitude()+"");
        Log.d("g53mdp",updateTime);
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
        trackOrNot=false;
    }

    public void onResume(){
        super.onResume();
        if(mGoogleApiClient.isConnected()){
            updateLocation();
            trackOrNot=true;
        }
    }

    public void updateLocation(){
        try{
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,locationReq, this);
            Log.d("g53mdp","resume succeed");
        }catch (SecurityException e){
            Toast.makeText(MyTracker.this,"Failed to update location",Toast.LENGTH_SHORT).show();
        }
    }

    public void stopUpdateLocation(){
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
    }


    public void onConnected(Bundle con){
        try{
            nowLocation=LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if(nowLocation!=null){
                Log.d("g53mdp",nowLocation.getLatitude()+"");
                Log.d("g53mdp",nowLocation.getLongitude()+"");
            }
        }catch (SecurityException e){
            Toast.makeText(MyTracker.this,"Failed to get location",Toast.LENGTH_SHORT).show();
        }

        if(trackOrNot){
            updateLocation();
        }
    }

    public void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putBoolean("requestLocationUpdate",trackOrNot);
        savedInstanceState.putParcelable("nowLocation",nowLocation);
        savedInstanceState.putString("lastUpdateTime",updateTime);
        super.onSaveInstanceState(savedInstanceState);
    }




    public void onConnectionSuspended(int con){

    }

    public void onConnectionFailed(ConnectionResult cr){
    }
}
