package com.uni.swansea.happyplant;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.sql.Date;
import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by michaelwaterworth on 09/03/15.
 */
public class PlantDatabaseHandler extends SQLiteOpenHelper {
    private static PlantDatabaseHandler instance;

    public static synchronized PlantDatabaseHandler getHelper(Context context)
    {
        if (instance == null)
            instance = new PlantDatabaseHandler(context);

        return instance;
    }

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    DateFormat format = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "plants.db";


    // PlantStatusData table
    private static final String TABLE_SENSORVALUES = "sensorvalues";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_SENSORTYPE = "sensortype";
    public static final String COLUMN_SENSORVALUE = "sensorvalue";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    // PlantDataRange table
    private static final String TABLE_SENSORRANGES = "sensorrange";
    public static final String COLUMN_MINVALUE = "minvalue";
    public static final String COLUMN_MAXVALUE = "maxvalue";

    public PlantDatabaseHandler(Context context, String name,
                       SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    /**
     * constructor should be private to prevent direct instantiation.
     * make call to static factory method "getInstance()" instead.
     */
    private PlantDatabaseHandler(Context ctx) {
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createValuesTable = "CREATE TABLE " +
                TABLE_SENSORVALUES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_SENSORTYPE + " INTEGER,"
                + COLUMN_SENSORVALUE + " INTEGER,"
                + COLUMN_TIMESTAMP + " TEXT)";

        String createRangeTable = "CREATE TABLE " +
                TABLE_SENSORRANGES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_SENSORTYPE + " INTEGER,"
                + COLUMN_MINVALUE + " INTEGER,"
                + COLUMN_MAXVALUE + " INTEGER)";

        db.execSQL(createValuesTable);
        db.execSQL(createRangeTable);

        createSensorRangeDefaults();
        createValuesDefaults();
    }

    public void createSensorRangeDefaults(){
        this.addRangeValues(new PlantDataRange(0,0,1,10));
        this.addRangeValues(new PlantDataRange(1, 1, 10, 20));
        this.addRangeValues(new PlantDataRange(2, 2, 20, 30));
    }

    public void createValuesDefaults(){
        Date oldDate = new Date(System.currentTimeMillis() - 5400 * 1000);
        this.addStatusData(new PlantStatusData(0, 0, oldDate));
        this.addStatusData(new PlantStatusData(1, 0, oldDate));
        this.addStatusData(new PlantStatusData(2, 0, oldDate));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SENSORVALUES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SENSORRANGES);
        onCreate(db);
    }

    public void clearData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_SENSORVALUES);
        db.execSQL("DELETE FROM " + TABLE_SENSORRANGES);
        createSensorRangeDefaults();
        createValuesDefaults();
    }


    public void addStatusData(PlantStatusData statusData) {

        ContentValues values = new ContentValues();
        values.put(COLUMN_SENSORTYPE, statusData.getType());
        values.put(COLUMN_SENSORVALUE, statusData.getValue());

        Format formatter = new SimpleDateFormat(DATE_FORMAT);
        values.put(COLUMN_TIMESTAMP, formatter.format(statusData.getTimeStamp()));

        SQLiteDatabase db = this.getWritableDatabase();

        db.insert(TABLE_SENSORVALUES, null, values);
        db.close();
    }


    public List<PlantStatusData> findByType(int statusType) {
        PlantStatusData statusData;
        String query = "select strftime('%Y-%m-%d %H:00:00', " + COLUMN_TIMESTAMP  + ")," + COLUMN_SENSORVALUE + " from " + TABLE_SENSORVALUES + " where " + COLUMN_SENSORTYPE + " = " + statusType;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        List<PlantStatusData> statusDataList = new ArrayList<>();

        while (cursor.moveToNext()) {
            try {
                statusData = new PlantStatusData();
                statusData.setType(statusType);
                statusData.setTimeStamp(format.parse(cursor.getString(0)));
                statusData.setValue(Math.round(cursor.getFloat(1)));
                statusDataList.add(statusData);
            } catch (ParseException e){
                System.out.println("Failed to parse Date from DB");
            }
        }
        cursor.close();
        db.close();
        return statusDataList;
    }

    public PlantStatusData findLastByType(int statusType) {
        PlantStatusData statusData = null;
        String query = "select " + COLUMN_SENSORVALUE + " from " + TABLE_SENSORVALUES + " where " + COLUMN_SENSORTYPE + " = " + statusType + " order by " + COLUMN_TIMESTAMP  + " DESC limit 1";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        while (cursor.moveToNext()) {
            statusData = new PlantStatusData();
            statusData.setValue(Math.round(cursor.getInt(0)));
        }
        cursor.close();
        db.close();
        return statusData;
    }

    public List<PlantStatusData> findByTypeGroupedHour(int statusType) {
        //"select strftime('%Y-%m-%dT%00:00:00.000', date_time),line, count() from entry group by strftime('%Y-%m-%dT%00:00:00.000', date_time)";//Day
        PlantStatusData statusData;
        String query = "select strftime('%Y-%m-%d %H:00:00', " + COLUMN_TIMESTAMP + "), avg(" + COLUMN_SENSORVALUE + ") from " + TABLE_SENSORVALUES + " where " + COLUMN_SENSORTYPE + " = " + statusType + " group by strftime('%Y-%m-%d %H:00:00', " + COLUMN_TIMESTAMP + ")";

        //String query = "Select * FROM " + TABLE_PRODUCTS + " WHERE " + COLUMN_PRODUCTNAME + " =  \"" + productname + "\"";

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(query, null);

        List<PlantStatusData> statusDataList = new ArrayList<>();

        while (cursor.moveToNext()) {
            try {
                statusData = new PlantStatusData();
                statusData.setType(statusType);
                statusData.setTimeStamp(format.parse(cursor.getString(0)));
                statusData.setValue(Math.round(cursor.getInt(1)));
                statusDataList.add(statusData);
            } catch (ParseException e){
                System.out.println("Failed to parse Date from DB");
            }
        }
        cursor.close();
        db.close();
        return statusDataList;
    }

    public void addRangeValues(PlantDataRange plantDataRange){
        ContentValues values = new ContentValues();
        values.put(COLUMN_SENSORTYPE, plantDataRange.getType());
        values.put(COLUMN_MINVALUE, plantDataRange.getMinValue());
        values.put(COLUMN_MAXVALUE, plantDataRange.getMaxValue());


        SQLiteDatabase db = this.getWritableDatabase();

         db.insert(TABLE_SENSORRANGES, null, values);
        db.close();
    }

    public PlantDataRange getRange(int statusType) {
        PlantDataRange plantDataRange = new PlantDataRange();
        String query = "select " + COLUMN_MINVALUE + ", " + COLUMN_MAXVALUE + " from " + TABLE_SENSORRANGES + " where " + COLUMN_SENSORTYPE + " = " + statusType;

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(query, null);


        while (cursor.moveToNext()) {
            plantDataRange.setType(statusType);
            plantDataRange.setMinValue(Math.round(cursor.getFloat(0)));
            plantDataRange.setMaxValue(Math.round(cursor.getFloat(1)));

        }
        cursor.close();
        db.close();
        return plantDataRange;
    }


    public PlantCurrentStatus getUpdatedPlantCurrentStatus() {
        PlantStatusData[] generalPlantStatusData =
                {
                        findLastByType(0),
                        findLastByType(1),
                        findLastByType(2)
                };

        PlantDataRange[] generalPlantDataRange =
                {
                        getRange(0),
                        getRange(1),
                        getRange(2)
                };

        return new PlantCurrentStatus(generalPlantStatusData, generalPlantDataRange);
    }

}