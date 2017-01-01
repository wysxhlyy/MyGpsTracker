package com.example.mario.mygpstracker;


import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
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
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;

import static java.sql.Types.DOUBLE;


public class MainActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,OnMapReadyCallback{

    private GoogleApiClient mGoogleApiClient;
    private Location nowLocation;

    private Button start;
    private Button history;

    private TextView process;
    private String todayInfo=" ";

    private Cursor cursor;
    private String[][] todayLoc;
    private float todayDistance;


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

        MapFragment mapFragment=(MapFragment)getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);          //Add the google map.

        start=(Button)findViewById(R.id.start);
        history=(Button)findViewById(R.id.history);
        process=(TextView)findViewById(R.id.process);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this,MyTracker.class);
                startActivity(intent);
            }
        });

        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this,TrackHistory.class);
                startActivity(intent);
            }
        });
        getTodayInfo();

    }

    //Get the information of today.
    private void getTodayInfo() {
        todayLoc=new String[2000][3];
        todayDistance=0;
        String[] projection=new String[]{
                MyProviderContract._ID,
                MyProviderContract.LONGITUDE,
                MyProviderContract.LATITUDE,
                MyProviderContract.DATE
        };

        int count=0;

        cursor=getContentResolver().query(MyProviderContract.LOCATION_URI,projection,null,null,null);

        while (cursor.moveToNext()){
            Log.d("g53mdp",count+":"+cursor.getString(1)+","+cursor.getString(2)+","+cursor.getString(cursor.getColumnIndex(MyProviderContract.DATE)));
            todayLoc[count][0]=cursor.getString(cursor.getColumnIndex(MyProviderContract.LONGITUDE));
            todayLoc[count][1]=cursor.getString(cursor.getColumnIndex(MyProviderContract.LATITUDE));

            count++;
        }

        Log.d("g53mdp","cursor count"+cursor.getCount());

        for(int i=0;i<count-1;i++){
            double long1=Double.parseDouble(todayLoc[i][0]);
            double lat1=Double.parseDouble(todayLoc[i][1]);
            double long2=Double.parseDouble(todayLoc[i+1][0]);
            double lat2=Double.parseDouble(todayLoc[i+1][1]);
            float[] distBetweenTwoNodes=new float[1];
            Location.distanceBetween(lat1,long1,lat2,long2,distBetweenTwoNodes);
            Log.d("g53mdp",i+":"+distBetweenTwoNodes[0]);
            if(distBetweenTwoNodes[0]<300){
                todayDistance+=distBetweenTwoNodes[0];
            }
        }

        process.setText((int)todayDistance+"");
    }

    /*
        Used to set the google map.
     */
    public void onMapReady(GoogleMap map){
        //map.addMarker(new MarkerOptions().position(new LatLng(nowLocation.getLongitude(),nowLocation.getLatitude())).title("Your position"));
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
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
                Log.d("g53mdp",nowLocation.getLatitude()+"");
                Log.d("g53mdp",nowLocation.getLongitude()+"");
                //todayInfo+="Your Current Position:\n longitude "+nowLocation.getLongitude()+", latitude "+nowLocation.getLatitude();
                //process.setText(todayInfo);

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

