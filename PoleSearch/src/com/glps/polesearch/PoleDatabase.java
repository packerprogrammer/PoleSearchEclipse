package com.glps.polesearch;

/**
 * Created by ghensley on 5/22/13.
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteQueryBuilder;


public class PoleDatabase {

    public static final int POLE_NUMBER_TYPE = 2;
    public static final int GRID_NUMBER_TYPE = 1;
    private PoleOpenHelper mDatabaseOH;

    /**
     * Constructor
     * @param context
     */
    public PoleDatabase(Context context){
        mDatabaseOH = new PoleOpenHelper(context);


    }

    public void open() throws SQLException {
        mDatabaseOH.getWritableDatabase();
    }


    public Cursor getPole(String Pole, int SearchType){
        String SearchColumn = "";
        if (SearchType == 1){
            SearchColumn = PoleOpenHelper.GRID_LOC;
        }
        else{
            if (SearchType==2){
                SearchColumn = PoleOpenHelper.POLE_NUM;
            }
        }
        String selection = SearchColumn + " like '" + Pole + "%'";
        String[] columns = new String[] {PoleOpenHelper.X_POS,PoleOpenHelper.Y_POS,PoleOpenHelper.GRID_LOC,PoleOpenHelper.POLE_NUM};
        String[] selectionArgs = new String[] {Pole};
        return query(selection, selectionArgs, columns);
    }

    public GlpsMarker[] getMarkers(String[] poles){

        GlpsMarker[] markers = new GlpsMarker[5];
        //markers[1] = new GlpsMarker();

        String selection = "(" + PoleOpenHelper.GRID_LOC + " = '" + poles[0] + "') " +
                "OR " + "(" + PoleOpenHelper.GRID_LOC + " = '" + poles[1] + "') " +
                "OR " + "(" +PoleOpenHelper.GRID_LOC + " = '" + poles[2] + "') " +
                "OR " + "(" +PoleOpenHelper.GRID_LOC + " = '" + poles[3] + "') " +
                "OR " + "(" +PoleOpenHelper.GRID_LOC + " = '" + poles[4] + "')";
        String[] columns = new String[] {PoleOpenHelper.X_POS,PoleOpenHelper.Y_POS,PoleOpenHelper.GRID_LOC,PoleOpenHelper.POLE_NUM};
        String[] selectionArgs = new String[] {""};
        Cursor cMarkers = query(selection, selectionArgs, columns);
        if (cMarkers == null)
            return null;
        if (cMarkers.moveToFirst()){
            int i = 0;
            do {

                markers[i] = new GlpsMarker();
                markers[i].setPole(cMarkers.getString(3));
                markers[i].setGrid(cMarkers.getString(2));
                markers[i].setCoord((double) cMarkers.getFloat(1), (double) cMarkers.getFloat(0));
                i++;
            }	while (cMarkers.moveToNext());

        }
        return markers;
    }
    public List<String> GetNearby(float lat, float lng){
        float minLat = (float) (lat - .001);
        float maxLat = (float) (lat + .001);
        float minLng = (float) (lng - .001);
        float maxLng = (float) (lng + .001);

        String selection = "(" + PoleOpenHelper.Y_POS + " BETWEEN " + minLat + " AND " + maxLat + ") AND ("+ PoleOpenHelper.X_POS + " BETWEEN " + minLng + " AND " + maxLng + ")";
        String[] columns = new String[] {PoleOpenHelper.X_POS,PoleOpenHelper.Y_POS,PoleOpenHelper.GRID_LOC,PoleOpenHelper.POLE_NUM};
        String[] selectionArgs = new String[] {""};
        Cursor cNearMe = query(selection, selectionArgs, columns);

        if (cNearMe == null)
            return null;
        //List<Float> index = new ArrayList<Float>();
        Map<Float,String> unsortedPoles = new HashMap<Float,String>();
        List<String> listItems;
        if (cNearMe.moveToFirst()){
            do {
                //TODO
                unsortedPoles.put(PoleNearMe.GetDistance(cNearMe.getFloat(0),cNearMe.getFloat(1),lng, lat),cNearMe.getString(2));
            } while  (cNearMe.moveToNext());
            Map<Float,String> sortedPoles = new TreeMap<Float,String>(unsortedPoles);


            listItems = new ArrayList<String>(sortedPoles.values());

        }//if PoleValue
        else{
            cNearMe.close();
            return null;
        }//else
        return listItems;
    }


    private Cursor query(String selection, String[] selectionArgs, String[] columns) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(PoleOpenHelper.TABLE_NAME);

        Cursor cursor = builder.query(mDatabaseOH.getReadableDatabase(), columns, selection, null, null, null, null);

        if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }//else if

        return cursor;
    }
    public long getRows() {
        return mDatabaseOH.rowCount();
    }

}
