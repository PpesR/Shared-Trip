package remm.sharedtrip;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import java.io.IOException;
import java.util.ArrayList;
import android.widget.ListAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import Database.SharedTripDbHelper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class BrowseEvents extends AppCompatActivity {

    LinearLayout screen;
    SharedTripDbHelper db;
    private List<EventModel> events;
    private RecyclerView recyclerView;
    private GridLayoutManager gridLayout;
    private EventAdapter adapter;

    private ListView mListView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_browse_events);
        //mListView = (ListView) findViewById(R.id.listview);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        events = new ArrayList<>();
        getEventsfromDB();

        gridLayout = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayout);

        adapter = new EventAdapter(this, events);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                if (gridLayout.findLastCompletelyVisibleItemPosition() == events.size() - 1) {
                    getEventsfromDB();
                }

            }
        });

        //screen = (LinearLayout) findViewById(R.id.screen);
        //db = new SharedTripDbHelper(this);

     //   db.deleteAllEvents();
       /* FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add_event_fbutton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(BrowseEvents.this, CreateEvent.class);
                BrowseEvents.this.startActivity(myIntent);
            }
        });*/


    }
    /*private void populateListView() {
        Cursor data = db.getDatanoid();
        ArrayList<String> listdata = new ArrayList<>();
        while (data.moveToNext()){
            String temp = "Title: "+data.getString(1)+ " Desc: "+data.getString(2)+" Location: " +data.getString(3);
            listdata.add(temp);
        }
        ListAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listdata);
       mListView.setAdapter(adapter);
    }*/


     @Override
     protected void onResume(){
         super.onResume();
         //ArrayList<String> events = db.getAllEvents();
         //populateListView();
//        for (String event_string: events) {
//            TextView tx = new TextView(this);
//            screen.addView(tx);
//            tx.setText(event_string);
//        }
     }

    private void getEventsfromDB() {

        AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {

                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("http://146.185.135.219/sharedtrip.php")
                        .build();
                try {
                    Response response = client.newCall(request).execute();

                    JSONArray array = new JSONArray(response.body().string());

                    for (int i = 0; i < array.length(); i++) {

                        JSONObject object = array.getJSONObject(i);

                        EventModel event = new EventModel(object.getString("trip_name"),
                                object.getString("event_picture"), object.getString("location"));

                        BrowseEvents.this.events.add(event);
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                adapter.notifyDataSetChanged();
            }
        };

        asyncTask.execute();
    }

}
