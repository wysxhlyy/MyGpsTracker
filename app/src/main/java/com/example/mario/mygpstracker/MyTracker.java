package com.example.mario.mygpstracker;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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


/**
 * This activity used to control the track.
 * User could choose to pause the track and resume the track.
 * Only the records that user wants to save will be saved into database.
 */
public class MyTracker extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,OnMapReadyCallback,View.OnClickListener,BatteryReceiver.BroadcastData {

    static final int ACTIVITY_TRACKER_REQUEST_CODE=1;

    private GoogleApiClient mGoogleApiClient;
    private MapFragment mapFragment;
    private GoogleMap mMap;
    private Location nowLocation;


    private Button pause;
    private Button save;
    private TextView process;
    private TextView battery;

    private MyTrackerService.MyBinder trackService;
    private Intent intent;

    private boolean tracking;
    private BatteryReceiver batteryReceiver;


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
        startService(intent);                                                                       //start the service.
        MyTracker.this.bindService(intent,serviceConnection, Context.BIND_AUTO_CREATE);
        tracking=true;
        save.setEnabled(false);                                                                     //The user could save the track records when the track is paused.
        process.setText("Tracking");

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }



        mapFragment.getMapAsync(this);                                                              //Add the google map.
        mMap=mapFragment.getMap();
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);


        Bundle bundle=getIntent().getExtras();
        if(bundle!=null){
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(bundle.getString("latitude")),Double.parseDouble(bundle.getString("longitude"))),13.0f));
        }                                                                                           //animate the camera to let user see his position immediately.

        pause.setOnClickListener(this);
        save.setOnClickListener(this);


        batteryReceiver=new BatteryReceiver();
        IntentFilter intentFilter=new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryReceiver,intentFilter);
        batteryReceiver.setBroadcastData(this);

    }

    /**
     * Initial the components.
     */
    public void initialize(){
        pause=(Button)findViewById(R.id.pause);
        save=(Button)findViewById(R.id.save);
        process=(TextView)findViewById(R.id.process);
        mapFragment=(MapFragment)getFragmentManager().findFragmentById(R.id.map);
        battery=(TextView)findViewById(R.id.battery);

    }


    /**
     * Buttons handler.
     * @param view
     */
    public void onClick(View view){
        switch (view.getId()){
            case R.id.pause:
                if(pause.getText().equals("pause")){
                    trackService.onPause();
                    pause.setText("resume");
                    save.setEnabled(true);
                    process.setText("Paused");
                    tracking=false;
                }else if(pause.getText().equals("resume")){
                    trackService.onResume();
                    pause.setText("pause");
                    save.setEnabled(false);
                    process.setText("Tracking");
                    tracking=true;
                }
                break;
            case R.id.save:
                trackService.saveLocation();                                                        //save the records to database.
                save.setEnabled(false);
                break;
        }
    }

    /**
     * Set the battery.
     * The battery information is from BatteryReceiver.
     * @param content
     */
    @Override
    public void setBattery(String content) {
        if(content!=null){
            battery.setText(content);
            Toast.makeText(this,"Your current battery level is "+content,Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handle the back button.
     * If the service is paused, user could back directly, user will lose the track records if press back without click the "save" button.
     * If the track service is running,a dialog will be shown to alert the user.
     * Track service will be stopped after user leave this activity.
     */
    public void onBackPressed(){
        if(tracking){
            AlertDialog.Builder builder=new AlertDialog.Builder(MyTracker.this);
            builder.setTitle("Back");
            builder.setMessage("Please stop tracking and save the track records before return to the main page.");

            builder.setNegativeButton("Keep Tracking", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Toast.makeText(MyTracker.this,"Still Tracking",Toast.LENGTH_SHORT).show();
                }
            });

            builder.setPositiveButton("Sure", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    trackService.onStop();
                    unregisterReceiver(batteryReceiver);
                    Toast.makeText(MyTracker.this,"Track Service Stopped",Toast.LENGTH_SHORT).show();

                    Intent result=new Intent(MyTracker.this,MainActivity.class);
                    setResult(Activity.RESULT_CANCELED,result);
                    startActivity(result);
                    finish();
                }
            });

            AlertDialog dialog=builder.create();
            dialog.show();
        }else {
            trackService.onStop();
            unregisterReceiver(batteryReceiver);
            Toast.makeText(MyTracker.this,"Track Service Stopped",Toast.LENGTH_SHORT).show();

            Intent result=new Intent(MyTracker.this,MainActivity.class);
            setResult(Activity.RESULT_CANCELED,result);
            startActivity(result);
            finish();
        }

    }


    /**
     * Set the google map.
     * @param map
     */
    public void onMapReady(GoogleMap map){
        mMap=map;
        try{
            map.setMyLocationEnabled(true);                                                         //Enable to find the current location
        }catch (SecurityException e){
            e.printStackTrace();
        }

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
            Toast.makeText(MyTracker.this,"Failed to get location",Toast.LENGTH_SHORT).show();
        }
    }

    protected void onDestroy(){
        Log.d("g54mdp","Activity Destroyed");
        if(serviceConnection!=null){
            unbindService(serviceConnection);
            serviceConnection=null;
            super.onDestroy();
        }

    }

    protected void onSaveInstanceState(Bundle storeInfo){
        super.onSaveInstanceState(storeInfo);
        storeInfo.putString("trackOrNot",process.getText().toString());
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState!=null){
            process.setText(savedInstanceState.getString("trackOrNot"));
        }
    }


    public void onConnectionSuspended(int con){

    }

    public void onConnectionFailed(ConnectionResult cr){
    }





}
