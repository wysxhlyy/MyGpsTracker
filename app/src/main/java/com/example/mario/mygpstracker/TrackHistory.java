package com.example.mario.mygpstracker;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TrackHistory extends AppCompatActivity {

    private float distance;
    private Cursor cursor;
    private SimpleAdapter dataAdapter;
    private ListView listView;

    private Button export;
    private Button delete;
    private Button show;
    private String dlat5;
    private String dlong5;
    private String filePath;
    private File newFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_history);

        initialize();

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
        List<Map<String,Object>> list=new ArrayList<Map<String, Object>>();
        Map<String,Object> map;

        while (cursor.moveToNext()){
            map=new HashMap<String,Object>();

            formatData();   //format the data
            map.put(MyProviderContract.LATITUDE,dlat5);
            map.put(MyProviderContract.LONGITUDE,dlong5);
            map.put(MyProviderContract.DATE,cursor.getString(cursor.getColumnIndex(MyProviderContract.DATE)));
            list.add(map);
        }

        dataAdapter=new SimpleAdapter(this,list,R.layout.db_item_layout,columnsToDisplay,colRedIds);

        listView.setAdapter(dataAdapter);


        export.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    exportData();       //store the file in the storage.
                } catch (IOException e) {
                    Log.d("g53mdp","hello");
                    e.printStackTrace();
                }
                sendEmail();    //send the record to any Email.
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearData();
            }
        });

        show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(TrackHistory.this,ShowRoute.class);
                startActivity(intent);
            }
        });


    }

    protected void clearData(){
        AlertDialog.Builder builder=new AlertDialog.Builder(TrackHistory.this);
        builder.setTitle("Clear");
        builder.setMessage("Are you sure to clear all the tracking record?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                getContentResolver().delete(MyProviderContract.LOCATION_URI,null,null);
                recreate();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(TrackHistory.this,"Nothing Changed",Toast.LENGTH_SHORT).show();
            }
        });

        AlertDialog dialog=builder.create();
        dialog.show();
    }

    protected void formatData(){
        String latitude=cursor.getString(cursor.getColumnIndex(MyProviderContract.LATITUDE));
        String longitude=cursor.getString(cursor.getColumnIndex(MyProviderContract.LONGITUDE));
        Double dlat=Double.parseDouble(latitude);
        dlat5=new Formatter().format("%.5f",dlat).toString();
        Double dlong=Double.parseDouble(longitude);
        dlong5=new Formatter().format("%.5f",dlong).toString();
    }

    protected void exportData() throws IOException {
        String base=android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        String fileName="TrackRecords.csv";
        filePath=base+ File.separator+fileName;

        Log.d("g53mdp",filePath);

        newFile=new File(filePath);
        Writer writer=new FileWriter(newFile);
        CSVWriter csvWriter= new CSVWriter(writer,',');

        List<String[]> data=new ArrayList<String[]>();
        csvWriter.writeNext(new String[]{"Latitude","Longitude","Time"});

        cursor.moveToFirst();
        while (cursor.moveToNext()){
            formatData();
            csvWriter.writeNext(new String[]{dlat5,dlong5,cursor.getString(cursor.getColumnIndex(MyProviderContract.DATE))});
        }

        csvWriter.close();
    }

    protected void sendEmail(){
        Intent intent=new Intent(Intent.ACTION_SEND);
        intent.setType("text/rfc822");
        intent.putExtra(Intent.EXTRA_SUBJECT,"Tracking Data Records");
        intent.putExtra(Intent.EXTRA_TEXT,"This Email include all your moving records using this app");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(newFile));
        startActivity(Intent.createChooser(intent,"Choose Email Client:"));
    }

    public void initialize(){
        listView=(ListView)findViewById(R.id.lv);
        export=(Button)findViewById(R.id.export);
        delete=(Button)findViewById(R.id.delete);
        show=(Button)findViewById(R.id.show);
    }

    public void onBackPressed(){
        Intent result=new Intent();
        setResult(Activity.RESULT_CANCELED,result);
        TrackHistory.this.finish();
    }
}
