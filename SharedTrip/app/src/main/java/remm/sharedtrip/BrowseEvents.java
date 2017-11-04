package remm.sharedtrip;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.widget.LoginButton;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.ExecutionException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BrowseEvents extends AppCompatActivity {

    private List<EventModel> events;
    private RecyclerView recyclerView;
    private GridLayoutManager gridLayout;
    private EventAdapter adapter;
    private ProfileTracker profileTracker;

    private BottomNavigationView bottomNavigationView;

    private List<EventModel> getEventsfromDB() {

        EventRetrievalTask<Void> asyncTask = new EventRetrievalTask<>();
        try {
            return asyncTask.execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_browse_events);

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);

//        BottomNavigationViewHelper
//                .disableShiftMode(bottomNavigationView);

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.bottombaritem_events:
                                // TODO
                                return true;
                            case R.id.bottombaritem_friends:
                                // TODO
                                return true;
                            case R.id.bottombaritem_profile:
                                // TODO
                                return true;
                        }
                        return false;
                    }
                });


        recyclerView = (RecyclerView) findViewById(R.id.eventResults);
        events = getEventsfromDB();

        gridLayout = new GridLayoutManager(this, events.size());
        recyclerView.setLayoutManager(gridLayout);

        adapter = new EventAdapter(this, events);
        recyclerView.setAdapter(adapter);

        TextView t = findViewById(R.id.user_header_name);
        t.append("  "+getIntent().getStringExtra("name"));
        t.setCompoundDrawablesWithIntrinsicBounds(R.drawable.com_facebook_button_icon_blue, 0, 0, 0);

        /*recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (gridLayout.findLastCompletelyVisibleItemPosition() == events.size() - 1)
                    events = getEventsfromDB();
            }
        });*/

        LoginButton loginButton = findViewById(R.id.header_logoff_button);
        loginButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add_event_fbutton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(BrowseEvents.this, CreateEvent.class);
                BrowseEvents.this.startActivity(myIntent);
            }
        });

        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                if (currentProfile==null) redirect();
            }
        };
    }

    private void redirect() {
        Intent browseEvents = new Intent(this, MainActivity.class);
        startActivity(browseEvents);
    }

     private static class EventRetrievalTask<Void> extends AsyncTask<Void, Void, List<EventModel>> {

         @SafeVarargs
         @Override
         protected final List<EventModel> doInBackground(Void... voids) {
             OkHttpClient client = new OkHttpClient();
             Request request = new Request.Builder()
                     .url("http://146.185.135.219/sharedtrip.php")
                     .build();
             List<EventModel> events = new ArrayList<>();
             try {
                 Response response = client.newCall(request).execute();

                 JSONArray array = new JSONArray(response.body().string());

                 for (int i = 0; i < array.length(); i++) {

                     JSONObject object = array.getJSONObject(i);

                     EventModel event = new EventModel(object.getString("trip_name"),
                             object.getString("event_picture"), object.getString("location"));

                     events.add(event);
                 }


             } catch (IOException e) {
                 e.printStackTrace();
             } catch (JSONException e) {
                 e.printStackTrace();
             }
             return events;
         }
     }
}
