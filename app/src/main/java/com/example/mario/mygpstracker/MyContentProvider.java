package com.example.mario.mygpstracker;

import android.content.ContentProvider;
import android.content.ContentUris;
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
    static final String TABLE_NAME="locationRecords";
    static final String DB_NAME="locationDB2";

    static {
        uriMatcher=new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(MyProviderContract.AUTHORITY,"locationRecords",LOCATIONS);
        uriMatcher.addURI(MyProviderContract.AUTHORITY,"locationRecords/#",LOCATION_ID);
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
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        long id=db.insert(TABLE_NAME,null,contentValues);
        db.close();
        if(id>0){
            Uri nu= ContentUris.withAppendedId(uri,id);
            Log.d("g53mdp",nu.toString());
            getContext().getContentResolver().notifyChange(nu,null);
            return nu;
        }
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
