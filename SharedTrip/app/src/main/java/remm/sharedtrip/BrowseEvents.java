package remm.sharedtrip;

import android.annotation.SuppressLint;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;

import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.widget.LoginButton;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.ExecutionException;

import adapters.EventAdapter;
import models.UserEventModel;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import utils.BottomNavigationViewHelper;

public class BrowseEvents extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private List<UserEventModel> events;
    private RecyclerView recyclerView;
    private RecyclerView searchRecyclerView;
    private GridLayoutManager searchGridLayout;
    private GridLayoutManager gridLayout;
    private EventAdapter adapter;
    private ProfileTracker profileTracker;
    private TextView t;
    private Intent ownIntent;
    static MainActivity.FbUserModel fbUserModel;
    private Gson gson = new Gson();
    private SearchView searchView;
    private BottomNavigationView bottomNavigationView;

    private List<UserEventModel> getEventsfromDB() {

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
        fbUserModel = gson.fromJson(
                ownIntent.getStringExtra("user")
                , MainActivity.FbUserModel.class);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setWindow();

        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                if (currentProfile==null) redirect();
            }
        };
    }

    private void setWindow(){
        setContentView(R.layout.activity_browse_events);

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);

        BottomNavigationViewHelper
                .disableShiftMode(bottomNavigationView);

        MenuItem profileItem = bottomNavigationView.getMenu()
                        .findItem(R.id.bottombaritem_profile);
        profileItem.setTitle(fbUserModel.firstName);

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.bottombaritem_events:
                                // TODO
                                return true;
                            case R.id.bottombaritem_friends:

                                switchToFragmentFriendsView();
//                                Intent friendsViewActivity = new Intent(BrowseEvents.this, FriendsViewActivity.class);
//                                startActivity(friendsViewActivity);
                                break;
                            case R.id.bottombaritem_stats:
                                Intent statsViewActivity = new Intent(BrowseEvents.this, StatsViewActivity.class);
                                startActivity(statsViewActivity);
                                return true;
                            case R.id.bottombaritem_profile:
                                Intent adminViewActivity = new Intent(BrowseEvents.this, AdminEventActivity.class);
                                startActivity(adminViewActivity);
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
        adapter.be = this;
        recyclerView.setAdapter(adapter);

        t = (TextView) findViewById(R.id.user_header_name);
        t.append("  "+fbUserModel.name);
        t.setCompoundDrawablesWithIntrinsicBounds(R.drawable.com_facebook_button_icon_blue, 0, 0, 0);
        t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent profileIntent = new Intent(BrowseEvents.this, ProfileView.class);
                profileIntent.putExtra("user", ownIntent.getStringExtra("user"));
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

    }



    private void redirect() {
        Intent browseEvents = new Intent(this, MainActivity.class);
        startActivity(browseEvents);
    }


    //method not used (searchview), but required by default
    @Override
    public boolean onQueryTextSubmit(String s) {
        search(s);
        return false;
    }
    //when called upon filters events by name
    @Override
    public boolean onQueryTextChange(String s) {

        return false;
    }

    private void search(String filter){
        setContentView(R.layout.search_menu);
        searchRecyclerView = (RecyclerView) findViewById(R.id.searchResults);
        searchView = (SearchView) findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(this);

        Button exit = (Button) findViewById(R.id.exitbutton);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setWindow();
            }
        });

            List<UserEventModel> filteredEvents = new ArrayList<>();
            for (UserEventModel event : events) {
                if (event.getName().toLowerCase().contains(filter.toLowerCase())) {
                    filteredEvents.add(event);
                }
            }
        adapter = new EventAdapter(this, filteredEvents);
        if(filteredEvents.size() < 1){  //quickfix, to be changed later
            searchGridLayout = new GridLayoutManager(this, 1);
        }else{
            searchGridLayout = new GridLayoutManager(this, filteredEvents.size());
        }
        searchRecyclerView.setLayoutManager(searchGridLayout);
        searchRecyclerView.setAdapter(adapter);
    }


    public static class EventRetrievalTask<Void> extends AsyncTask<Void, Void, List<UserEventModel>> {

         @SafeVarargs
         @Override
         protected final List<UserEventModel> doInBackground(Void... voids) {
             OkHttpClient client = new OkHttpClient();
             Request request = new Request.Builder()
                     .url("http://146.185.135.219/requestrouter.php?hdl=event&act=wappr&user="+fbUserModel.id)
                     .build();
             List<UserEventModel> events = new ArrayList<>();
             try {
                 Response response = client.newCall(request).execute();

                 JSONArray array = new JSONArray(response.body().string());
                 JSONArray resultArray = array.getJSONArray(2);

                 for (int i = 0; i < resultArray.length(); i++) {

                     JSONObject object = resultArray.getJSONObject(i);

                     UserEventModel event = new UserEventModel(object.getString("trip_name"),
                             object.getString("event_picture"), object.getString("location"));

                     event.setDescription(object.getString("description"));
                     event.setId(object.getInt("id"));
                     event.setStartDate(object.getString("date_begin"));
                     event.setEndDate(object.getString("date_end"));
                     event.setSpots(object.getInt("spots"));
                     event.setCost(object.getInt("total_cost"));
                     event.setUserApproved(object.getInt("approved")==1);
                     event.setApprovalPending(object.getInt("pending")==1);
                     event.setUserBanned(object.getInt("banned")==1);
                     event.setAdmin(object.getInt("is_admin")==1);

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
    protected void onResume() {
        super.onResume();
        MenuItem myButton = bottomNavigationView.getMenu()
                .findItem(R.id.bottombaritem_events);
        myButton.setChecked(true);
    }

    @Override
    public void onBackPressed() {
        this.moveTaskToBack(true);
        //do not redirect
    }

    public void switchToFragmentFriendsView(){
        FragmentManager manager = getSupportFragmentManager();
       // manager.beginTransaction().replace( new FriendsView()).commit();
    }
}
