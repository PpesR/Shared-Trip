package remm.sharedtrip;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SearchView;
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

public class BrowseEvents extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private List<EventModel> events;
    private RecyclerView recyclerView;
    private GridLayoutManager gridLayout;
    private EventAdapter adapter;
    private ProfileTracker profileTracker;
    private TextView t;
    private Intent ownIntent;
    SearchView searchView;

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

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        ownIntent = getIntent();

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_browse_events);

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);

        BottomNavigationViewHelper
                .disableShiftMode(bottomNavigationView);

        MenuItem profileItem = bottomNavigationView.getMenu()
                        .findItem(R.id.bottombaritem_profile);
        profileItem.setTitle(ownIntent.getStringExtra("first_name"));

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.bottombaritem_events:
                                // TODO
                                return true;
                            case R.id.bottombaritem_friends:
                                Intent friendsViewActivity = new Intent(BrowseEvents.this, FriendsViewActivity.class);
                                startActivity(friendsViewActivity);
                                return true;
                            case R.id.bottombaritem_stats:
                                Intent statsViewActivity = new Intent(BrowseEvents.this, StatsViewActivity.class);
                                startActivity(statsViewActivity);
                                return true;
                            case R.id.bottombaritem_profile:
                                // TODO
                                return true;
                        }
                        return true;
                    }
                });


        recyclerView = (RecyclerView) findViewById(R.id.eventResults);
        events = getEventsfromDB();

        gridLayout = new GridLayoutManager(this, events.size());
        recyclerView.setLayoutManager(gridLayout);

        adapter = new EventAdapter(this, events);
        recyclerView.setAdapter(adapter);




        t = (TextView) findViewById(R.id.user_header_name);
        t.append("  "+ownIntent.getStringExtra("name"));
        t.setCompoundDrawablesWithIntrinsicBounds(R.drawable.com_facebook_button_icon_blue, 0, 0, 0);
        t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent profileIntent = new Intent(BrowseEvents.this, ProfileView.class);
                profileIntent.putExtra("gender",ownIntent.getStringExtra("gender"));
                BrowseEvents.this.startActivity(profileIntent);
            }
        });


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


        //registers when text is typed on search bar and calls onQuery... methods
        searchView = (SearchView) findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(this);




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

    @Override
    public boolean onQueryTextSubmit(String s) {

        return true;
    }
    //when called upon filters events by name
    @Override
    public boolean onQueryTextChange(String s) {
        List<EventModel> filteredEvents = new ArrayList<>();
        for(EventModel event : events){
            if(event.getname().toLowerCase().contains(s.toLowerCase())){
                filteredEvents.add(event);
            }
        }
        adapter = new EventAdapter(this, filteredEvents);
        recyclerView.setAdapter(adapter);
        return false;
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

    @Override
    public void onBackPressed() {
        this.moveTaskToBack(true);
        //do not redirect
    }
}
