package Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;


public class SharedTripDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "SharedTrip.db";

    public SharedTripDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SharedTripContract.SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SharedTripContract.SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public boolean insertEvent (String title, String destination, String description, String start_date, String end_date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("title", title);
        contentValues.put("destination", destination);
        contentValues.put("description", description);
        contentValues.put("start_date", start_date);
        contentValues.put("end_date", end_date);
        db.insert("event_info", null, contentValues);
        return true;
    }
    public int numberOfRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, SharedTripContract.EventInfo.COLUMN_NAME_TITLE);
        return numRows;
    }

    public Cursor getData(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from event_info where id="+id+"", null );
        return res;
    }
    public Integer deleteEvent (Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("event_info",
                "id = ? ",
                new String[] { Integer.toString(id) });
    }

    public boolean updateEvent (Integer id, String title, String destination, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("title", title);
        contentValues.put("destination", destination);
        contentValues.put("description", description);
        db.update("event_info", contentValues, "id = ? ", new String[] { Integer.toString(id) } );
        return true;
    }

    public boolean deleteAllEvents(){
        ArrayList<Integer> array_list = new ArrayList<Integer>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select" + SharedTripContract.EventInfo._ID +" from " + SharedTripContract.EventInfo.TABLE_NAME, null );
        res.moveToFirst();
        while(res.isAfterLast() == false){
            array_list.add(res.getInt(res.getColumnIndex(SharedTripContract.EventInfo._ID)));
            res.moveToNext();
        }
        for (int id : array_list) {
            deleteEvent(id);
        }
        return true;
    }


    public ArrayList<String> getAllEvents() {
        ArrayList<String> array_list = new ArrayList<String>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from " + SharedTripContract.EventInfo.TABLE_NAME, null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            array_list.add(res.getString(res.getColumnIndex(SharedTripContract.EventInfo.COLUMN_NAME_TITLE)));
            res.moveToNext();
        }
        res.close();
        return array_list;
    }

    public Cursor getDatanoid(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "Select * From " + SharedTripContract.EventInfo.TABLE_NAME;
        Cursor data = db.rawQuery(query, null);
        return data;
    }

}
