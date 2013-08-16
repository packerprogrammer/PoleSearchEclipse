package com.glps.polesearch;

/**
 * Created by ghensley on 5/22/13.
 */

//import android.app.SearchManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.content.res.Resources;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.os.Message;

import android.util.Log;


public class PoleOpenHelper extends SQLiteOpenHelper {


    //define the db columns
    public static final String GRID_LOC = "GridLocation";//SearchManager.SUGGEST_COLUMN_TEXT_1;
    public static final String POLE_NUM = "PoleNumber"; //SearchManager.SUGGEST_COLUMN_TEXT_2;
    public static final String X_POS = "Xpos";

    public static final String Y_POS = "Ypos";
    public static final String TABLE_NAME = "tblPoles";
    private static final String TAG = "PoleOpenHelper";

    private static final String DATABASE_NAME = "GLPSPoles";

    private static final int DATABASE_VERSION = 9;


    //private SQLiteDatabase database;
    private final Context mHelperContext;
    private SQLiteDatabase mDatabase;
    private SQLiteDatabase cDatabase;

    private static final String TABLE_CREATE =
            "CREATE VIRTUAL TABLE " + TABLE_NAME +
                    " USING fts3 ("
                    + GRID_LOC + ","
                    + POLE_NUM + ","
                    + X_POS + " REAL,"
                    + Y_POS + " REAL);";

    PoleOpenHelper(Context context) {
        super(context,DATABASE_NAME, null, DATABASE_VERSION);
        mHelperContext = context;
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        mDatabase = db;
        mDatabase.execSQL(TABLE_CREATE);
        loadPoles();


    }
    @Override
    public void onOpen(SQLiteDatabase db) {

        cDatabase = db;

    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);

    }
    private void loadPoles() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    loadValues();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
    private void loadValues() throws IOException {


        Log.d(TAG,"Loading Pole Values...");

        final Resources resources = mHelperContext.getResources();
        InputStream inputStream = resources.openRawResource(R.raw.poles);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        List<String> lines = new ArrayList<String>();
        long startTime = System.currentTimeMillis();
        long id = 0;
        String[] strings = null;
        try {
            String line = null;
            //int i = 1;

            while ((line = reader.readLine()) != null) {
                lines.add(line);
                if (lines.size() >= 5000)
                {
                    id = addPole2(lines);
                    lines.clear();
                }


            }
        } finally {
            id = addPole2(lines);
            lines.clear();
            long endTime = System.currentTimeMillis();
            long elapsedTime = endTime - startTime;

            Message mm = new Message();
            Bundle messageData = new Bundle();
            mm.what = 0;
            messageData.putLong("time", elapsedTime);
            mm.setData(messageData);

            MainActivity.returnHandler().sendMessage(mm);
            //Log.d(TAG, "read took " + elapsedTime);
            reader.close();
            mDatabase.close();
        }
        Log.d(TAG, "DONE loading poles.");
    }


    private long addPole2(List<String>lines)
    {
        mDatabase.beginTransaction();
        try
        {
            String fields = PoleOpenHelper.GRID_LOC + "," + PoleOpenHelper.POLE_NUM + "," + PoleOpenHelper.X_POS + "," + PoleOpenHelper.Y_POS;
            String sql = "INSERT INTO " + PoleOpenHelper.TABLE_NAME + "(" + fields + ") VALUES (?,?,?,?)";
            SQLiteStatement insert = mDatabase.compileStatement(sql);
            String[] values = null;
            for (String s : lines)
            {
                values = s.split(",");
                insert.bindString(1, values[2]);
                insert.bindString(2, values[3]);

                insert.bindDouble(3, Double.parseDouble(values[0]));
                insert.bindDouble(4, Double.parseDouble(values[1]));
                insert.executeInsert();
            }
            mDatabase.setTransactionSuccessful();

        }
        finally
        {
            mDatabase.endTransaction();

        }
        return 1;

    }
    public long rowCount() {
        long count = DatabaseUtils.queryNumEntries(cDatabase, TABLE_NAME);
        //cDatabase.close();
        return count;
    }

    public void closeDB(){
        cDatabase.close();
    }

}