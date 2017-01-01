package com.example.mario.mygpstracker;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by mario on 2016/12/31.
 */

public class MyContentProvider extends ContentProvider {

    private DBHelper dbHelper;
    private static final UriMatcher uriMatcher;


    static final int LOCATIONS=1;
    static final int LOCATION_ID=2;
    static final String TABLE_NAME="locationRecord";
    static final String DB_NAME="locationDB";

    static {
        uriMatcher=new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(MyProviderContract.AUTHORITY,"locationRecord",LOCATIONS);
        uriMatcher.addURI(MyProviderContract.AUTHORITY,"locationRecord/#",LOCATION_ID);
    }

    @Override
    public boolean onCreate() {
        Log.d("g53mdp","Contentprovider oncreate");
        this.dbHelper=new DBHelper(this.getContext(),DB_NAME,null,7);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        return db.query(TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        if(uri.getLastPathSegment()==null){
            return "vnd.android.cursor.dir/MyProvider.data.text";
        }else{
            return "vnd.android.cursor.item/MyProvider.data.text";
        }    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }
}
