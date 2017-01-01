package com.example.mario.mygpstracker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


/**
 * Created by mario on 2016/12/31.
 */

public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,int version){
        super(context,name,factory,version);
        Log.d("g54mdp", "DBHelper");
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.d("g53mdp","onCreate DB");

        sqLiteDatabase.execSQL("CREATE TABLE locationRecord(" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "longitude VARCHAR(128) NOT NULL," +
                "latitude VARCHAR(128) NOT NULL," +
                "time timestamp NOT NULL default CURRENT_TIMESTAMP" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS location");
        onCreate(sqLiteDatabase);
    }
}
