package com.example.mario.mygpstracker;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShowRoute extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,OnMapReadyCallback {

    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;
    private Cursor cursor;

    private Double dlat;
    private Double dlong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_route);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        MapFragment mapFragment=(MapFragment)getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);          //Add the google map.

        mMap=mapFragment.getMap();
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        String[] projection=new String[]{
                MyProviderContract._ID,
                MyProviderContract.LONGITUDE,
                MyProviderContract.LATITUDE,
                MyProviderContract.DATE
        };

        String[] columnsToDisplay=new String[]{
                MyProviderContract.LATITUDE,
                MyProviderContract.LONGITUDE,
                MyProviderContract.DATE
        };

        int[] colRedIds=new int[]{
                R.id.value1,
                R.id.value2,
                R.id.value3
        };

        cursor=getContentResolver().query(MyProviderContract.LOCATION_URI,projection,null,null,null);
        PolylineOptions route=new PolylineOptions();

        while (cursor.moveToNext()){
            formatData();   //format the data
            route.add(new LatLng(dlat,dlong));
        }

        Polyline polyline=mMap.addPolyline(route);

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(dlat,dlong),12.0f));

    }

    protected void formatData(){
        String latitude=cursor.getString(cursor.getColumnIndex(MyProviderContract.LATITUDE));
        String longitude=cursor.getString(cursor.getColumnIndex(MyProviderContract.LONGITUDE));
        dlat=Double.parseDouble(latitude);
        dlong=Double.parseDouble(longitude);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    protected void  onStart(){
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop(){
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap=googleMap;
        Log.d("g53mdp","map added");
        try{
            googleMap.setMyLocationEnabled(true);     //Enable to find the current location
        }catch (SecurityException e){
            //ask for permission
        }
    }
}
