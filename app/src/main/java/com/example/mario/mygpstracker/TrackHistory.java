package com.example.mario.mygpstracker;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Used to show the information stored in database.
 * The tracking information includes the latitude,longitude and record time.
 * The user could use this activity to export the data by Email or delete all the data.
 */
public class TrackHistory extends AppCompatActivity {

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

    private String[] columnsToDisplay;
    private int[] colRedIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_history);

        initialize();                                                                               //initialize the components.

        getData();                                                                                  //get Data from database.

        setDataInListview();                                                                        //Put the selected data into listview.

        export.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    exportData();                                                                   //store the file in the storage.
                } catch (IOException e) {
                    e.printStackTrace();
                }
                sendEmail();                                                                        //send the record to an Email address using email app like Gmail.
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearData();                                                                        //delete all the data
            }
        });

        show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(TrackHistory.this,ShowRoute.class);
                startActivity(intent);                                                              //Visually display track record for a specified day.
            }
        });
    }

    /**
     * Get data from database.
     */
    public void getData(){
        String[] projection=new String[]{
                MyProviderContract._ID,
                MyProviderContract.LONGITUDE,
                MyProviderContract.LATITUDE,
                MyProviderContract.DATE
        };

        columnsToDisplay=new String[]{
                MyProviderContract.LATITUDE,
                MyProviderContract.LONGITUDE,
                MyProviderContract.DATE
        };

        colRedIds=new int[]{
                R.id.value1,
                R.id.value2,
                R.id.value3
        };

        cursor=getContentResolver().query(MyProviderContract.LOCATION_URI,projection,null,null,null);
    }

    /**
     * Put the track record got from database into listview.
     */
    public void setDataInListview(){
        List<Map<String,Object>> list=new ArrayList<Map<String, Object>>();
        Map<String,Object> map;
        map=new HashMap<String,Object>();

        map.put(MyProviderContract.LATITUDE,"Latitude");
        map.put(MyProviderContract.LONGITUDE,"Longitude");
        map.put(MyProviderContract.DATE,"Record Time      ");
        list.add(map);

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
    }

    /**
     * Delete all the track record.
     * Show a dialog to let user comfirm the deletion.
     */
    protected void clearData(){
        AlertDialog.Builder builder=new AlertDialog.Builder(TrackHistory.this);
        builder.setTitle("Clear");
        builder.setMessage("Are you sure to clear all the track record?");
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


    /**
     * Format data to transfer it from String to Double.
     */
    protected void formatData(){
        String latitude=cursor.getString(cursor.getColumnIndex(MyProviderContract.LATITUDE));
        String longitude=cursor.getString(cursor.getColumnIndex(MyProviderContract.LONGITUDE));
        Double dlat=Double.parseDouble(latitude);
        dlat5=new Formatter().format("%.5f",dlat).toString();
        Double dlong=Double.parseDouble(longitude);
        dlong5=new Formatter().format("%.5f",dlong).toString();
    }


    /**
     * Export data and store the file in the device.
     * Exported data includes latitude,longitude and record time.
     * @throws IOException
     */
    protected void exportData() throws IOException {
        String base=android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        String fileName="TrackRecords.csv";
        filePath=base+ File.separator+fileName;

        newFile=new File(filePath);
        Writer writer=new FileWriter(newFile);
        CSVWriter csvWriter= new CSVWriter(writer,',');

        csvWriter.writeNext(new String[]{"Latitude","Longitude","Time"});

        cursor.moveToFirst();
        while (cursor.moveToNext()){
            formatData();
            csvWriter.writeNext(new String[]{dlat5,dlong5,cursor.getString(cursor.getColumnIndex(MyProviderContract.DATE))});
        }
        csvWriter.close();
    }

    /**
     * Allow the user to export data by Email because it is hard for user to get exported
     * data in the external storage of device.
     * This requires an Email app downloaded in the device. e.g.,Gmail App.
     */
    protected void sendEmail(){
        Intent intent=new Intent(Intent.ACTION_SEND);
        intent.setType("text/rfc822");
        intent.putExtra(Intent.EXTRA_SUBJECT,"Tracking Data Records");
        intent.putExtra(Intent.EXTRA_TEXT,"This Email include all your moving records using this app");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(newFile));
        startActivity(Intent.createChooser(intent,"Choose Email Client:"));
    }

    /**
     * Initial the components.
     */
    public void initialize(){
        listView=(ListView)findViewById(R.id.lv);
        export=(Button)findViewById(R.id.export);
        delete=(Button)findViewById(R.id.delete);
        show=(Button)findViewById(R.id.show);
    }


    /**
     * Handle the back button.
     */
    public void onBackPressed(){
        Intent result=new Intent();
        setResult(Activity.RESULT_CANCELED,result);
        TrackHistory.this.finish();
    }
}
