package remm.sharedtrip;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.List;
import Database.SharedTripDbHelper;


public class BrowseEvents extends AppCompatActivity {

    LinearLayout screen;
    SharedTripDbHelper db;

    private ListView mListView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_browse_events);
        mListView = (ListView) findViewById(R.id.listview);


        screen = (LinearLayout) findViewById(R.id.screen);
        db = new SharedTripDbHelper(this);

     //   db.deleteAllEvents();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add_event_fbutton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(BrowseEvents.this, CreateEvent.class);
                BrowseEvents.this.startActivity(myIntent);
            }
        });


    }
    private void populateListView() {
        Cursor data = db.getDatanoid();
        ArrayList<String> listdata = new ArrayList<>();
        while (data.moveToNext()){
            listdata.add(data.getString(1));
        }
        ListAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listdata);
       mListView.setAdapter(adapter);
    }


     @Override
     protected void onResume(){
         super.onResume();
         ArrayList<String> events = db.getAllEvents();
         populateListView();
//         for (String event_string: events) {
//             TextView tx = new TextView(this);
//             screen.addView(tx);
//             tx.setText(event_string);
//         }
     }

}
