package remm.sharedtrip;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import Database.SharedTripDbHelper;


public class BrowseEvents extends AppCompatActivity {

    LinearLayout screen;
    SharedTripDbHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_browse_events);

        screen = (LinearLayout) findViewById(R.id.screen);
        db = new SharedTripDbHelper(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add_event_fbutton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(BrowseEvents.this, CreateEvent.class);
                BrowseEvents.this.startActivity(myIntent);
            }
        });


    }

     @Override
     protected void onResume(){
         super.onResume();
         ArrayList<String> events = db.getAllEvents();

         for (String event_string: events) {
             TextView tx = new TextView(this);
             screen.addView(tx);
             tx.setText(event_string);
         }
     }

}
