package com.example.mario.mygpstracker;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.IBinder;
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

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MyTracker extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,OnMapReadyCallback,View.OnClickListener {

    private GoogleApiClient mGoogleApiClient;
    private MapFragment mapFragment;


    private Button pause;
    private Button save;
    private Button cancel;

    private MyTrackerService.MyBinder trackService;
    private Intent intent;


    private ServiceConnection serviceConnection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d("g54mdp", "MyTracker onServiceConnected");
            trackService=(MyTrackerService.MyBinder) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d("g54mdp", "MyTracker onServiceDisconnected");
            trackService=null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_tracker);
        initialize();


        intent=new Intent(MyTracker.this,MyTrackerService.class);
        startService(intent);
        this.bindService(intent,serviceConnection, Context.BIND_AUTO_CREATE);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        mapFragment.getMapAsync(this);          //Add the google map.

        cancel.setOnClickListener(this);
        pause.setOnClickListener(this);
        save.setOnClickListener(this);


    }

    public void initialize(){
        cancel=(Button)findViewById(R.id.cancel);
        pause=(Button)findViewById(R.id.pause);
        save=(Button)findViewById(R.id.save);
        mapFragment=(MapFragment)getFragmentManager().findFragmentById(R.id.map);


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
                    trackService.onPause();
                    pause.setText("resume");
                    save.setEnabled(true);
                }else if(pause.getText().equals("resume")){
                    trackService.onResume();
                    pause.setText("pause");
                    save.setEnabled(false);
                }
                break;
            case R.id.save:
                trackService.saveLocation();
                break;
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

    public void onConnected(Bundle con){

    }

    public void onConnectionSuspended(int con){

    }

    public void onConnectionFailed(ConnectionResult cr){
    }


    protected void onDestroy(){
        Log.d("g54mdp","Activity Destroyed");
        if(serviceConnection!=null){
            unbindService(serviceConnection);
            serviceConnection=null;
        }
        super.onDestroy();

    }

/*



    public void onLocationChanged(Location location){   //update the location
        nowLocation=location;
        SimpleDateFormat sdf=new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        updateTime=sdf.format(new Date());
        savedLoc[saveCount][0]=nowLocation.getLongitude()+"";
        savedLoc[saveCount][1]=nowLocation.getLatitude()+"";
        savedLoc[saveCount][2]=updateTime;
        saveCount++;
        Log.d("g53mdp",nowLocation.getLatitude()+"");
        Log.d("g53mdp",nowLocation.getLongitude()+"");
        Log.d("g53mdp",updateTime);
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






 */
}
