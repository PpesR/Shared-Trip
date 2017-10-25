package Database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

public class SharedTripContract {

    private SharedTripContract(){}

    public static class EventInfo implements BaseColumns{
        public static final String TABLE_NAME = "event_info";
        public static final String COLUMN_NAME_DESTINATION = "destination";
        public static final String COLUMN_NAME_STARTDATE = "start_date";
        public static final String COLUMN_NAME_ENDDATE = "end_date";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_DESCRIPTION = "description";
    }

    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + EventInfo.TABLE_NAME + " (" +
                    EventInfo._ID + " INTEGER PRIMARY KEY," +
                    EventInfo.COLUMN_NAME_TITLE+ " TEXT," +
                    EventInfo.COLUMN_NAME_DESTINATION + " TEXT," +
                    EventInfo.COLUMN_NAME_DESCRIPTION + " TEXT)";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + EventInfo.TABLE_NAME;
}