package com.example.mario.mygpstracker;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
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
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;



public class MyTracker extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,OnMapReadyCallback,View.OnClickListener,BatteryReceiver.BroadcastData {

    private GoogleApiClient mGoogleApiClient;
    private MapFragment mapFragment;


    private Button pause;
    private Button save;
    private Button cancel;
    private TextView process;
    private TextView battery;

    private MyTrackerService.MyBinder trackService;
    private Intent intent;
    private int countDownInt=4;
    private Handler h;

    private boolean tracking;


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

        countDown();

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


        BatteryReceiver br=new BatteryReceiver();
        IntentFilter intentFilter=new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(br,intentFilter);
        br.setBroadcastData(this);


    }

    public void initialize(){
        cancel=(Button)findViewById(R.id.cancel);
        pause=(Button)findViewById(R.id.pause);
        save=(Button)findViewById(R.id.save);
        process=(TextView)findViewById(R.id.process);
        mapFragment=(MapFragment)getFragmentManager().findFragmentById(R.id.map);
        battery=(TextView)findViewById(R.id.battery);

    }

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
                            countDown();
                        }else if(countDownInt==0){
                            process.setText("Start!");
                            countDown();
                        }else {
                            process.setText("Tracking");
                            intent=new Intent(MyTracker.this,MyTrackerService.class);
                            startService(intent);
                            MyTracker.this.bindService(intent,serviceConnection, Context.BIND_AUTO_CREATE);
                            tracking=true;
                            pause.setVisibility(View.VISIBLE);
                            save.setVisibility(View.VISIBLE);
                            cancel.setVisibility(View.VISIBLE);

                        }
                    }
                });
            }
        }).start();
    }


    public void onClick(View view){
        switch (view.getId()){
            case R.id.cancel:
                if (!tracking){
                    Intent intent=new Intent(MyTracker.this,MainActivity.class);
                    startActivity(intent);
                }else {
                    backWarn();
                }

                //stop the track
                break;
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
                trackService.saveLocation();
                save.setEnabled(false);
                break;
        }
    }

    @Override
    public void setBattery(String content) {
        if(content!=null){
            battery.setText(content);
            //Toast.makeText(this,"Your current battery level is "+content,Toast.LENGTH_SHORT).show();
        }
    }

    public void backWarn(){
        AlertDialog.Builder builder=new AlertDialog.Builder(MyTracker.this);
        builder.setTitle("Back");
        builder.setMessage("Please stop tracking before return to the main page.");

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
                Intent intent=new Intent(MyTracker.this,MainActivity.class);
                startActivity(intent);
            }
        });

        AlertDialog dialog=builder.create();
        dialog.show();
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



}
