package remm.sharedtrip;

import android.annotation.SuppressLint;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
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
import remm.sharedtrip.MainActivity.FbGoogleUserModel;
import utils.BottomNavigationViewHelper;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class BrowseEvents extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private List<UserEventModel> events;
    private RecyclerView recyclerView;
    private RecyclerView searchRecyclerView;
    private GridLayoutManager searchGridLayout;
    private GridLayoutManager gridLayout;
    private EventAdapter adapter;
    private TextView headerUsername;
    private Intent ownIntent;
    static FbGoogleUserModel userModel;
    private Gson gson = new Gson();
    private SearchView searchView;
    private BottomNavigationView bottomNavigationView;
    private AccessTokenTracker accessTokenTracker;

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
        userModel = gson.fromJson(
                ownIntent.getStringExtra("user")
                , FbGoogleUserModel.class);

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken accessToken, AccessToken accessToken2) {
                if (accessToken2 == null) { finish(); }
            }
        };

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setWindow();
    }

    private void setWindow(){
        setContentView(R.layout.activity_browse_events);

        setUpUserHeader();

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);

        MenuItem profileItem = bottomNavigationView.getMenu().findItem(R.id.bottombaritem_profile);
        profileItem.setTitle(userModel.firstName);

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
                                Intent adminViewActivity = new Intent(BrowseEvents.this, AdminEventActivity.class);
                                startActivity(adminViewActivity);
                                return true;
                        }
                        return true;
                    }
                });


        recyclerView = findViewById(R.id.eventResults);
        events = getEventsfromDB();

        if (events != null && !events.isEmpty()) {
            gridLayout = new GridLayoutManager(this, events.size());
            recyclerView.setLayoutManager(gridLayout);

            adapter = new EventAdapter(this, events);
            adapter.be = this;
            recyclerView.setAdapter(adapter);
        }

        /*recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (gridLayout.findLastCompletelyVisibleItemPosition() == events.size() - 1)
                    events = getEventsfromDB();
            }
        });*/

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add_event_fbutton);
        fab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(BrowseEvents.this, CreateEvent.class);
                BrowseEvents.this.startActivity(myIntent);
            }
        });

        //registers when text is typed on search bar and calls onQuery... methods
        searchView = findViewById(R.id.searchView);
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
        exit.setOnClickListener(new OnClickListener() {
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
                     .url("http://146.185.135.219/requestrouter.php?hdl=event&act=wappr&user="+ userModel.id)
                     .build();
             List<UserEventModel> events = new ArrayList<>();
             try {
                 Response response = client.newCall(request).execute();
                 String bodystring = response.body().string();
                 if (bodystring.equals("")) {
                     events.add(new UserEventModel("temp", "http://clipart-library.com/images/dT4oqE78c.png", "temp"));
                     return events;
                 }

                 JSONArray array = new JSONArray(bodystring);
                 JSONArray resultArray = array.getJSONArray(2);

                 for (int i = 0; i < resultArray.length(); i++) {

                     JSONObject object = resultArray.getJSONObject(i);

                     UserEventModel event = new UserEventModel();

                     event.setName(object.getString("trip_name"));
                     event.setLoc(object.getString("location"));
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

                     if (!object.getString("event_picture").contains("http")) {
                         byte[] bytes = Base64.decode(object.getString("event_picture"), Base64.DEFAULT);
                         event.setBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                     }
                     else {
                         event.setImageLink(object.getString("event_picture"));
                     }
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
        MenuItem myButton = bottomNavigationView.getMenu().findItem(R.id.bottombaritem_events);
        myButton.setChecked(true);
        events = getEventsfromDB();

        if (Profile.getCurrentProfile() == null && GoogleSignIn.getLastSignedInAccount(this) == null) {
            finish();
        }

        if (events!=null && !events.isEmpty()) {
            gridLayout = new GridLayoutManager(this, events.size());
            recyclerView.setLayoutManager(gridLayout);

            adapter = new EventAdapter(this, events);
            adapter.be = this;
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    public void onBackPressed() {
        this.moveTaskToBack(true);
        //do not redirect
    }

    private void setUpUserHeader() {

        headerUsername = findViewById(R.id.user_header_name);
        headerUsername.append("  "+ userModel.name);
        headerUsername.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent profileIntent = new Intent(BrowseEvents.this, ProfileView.class);
                profileIntent.putExtra("user", ownIntent.getStringExtra("user"));
                BrowseEvents.this.startActivity(profileIntent);
            }
        });

        if (userModel.hasFacebook() && !userModel.hasGoogle()) {
            headerUsername.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.com_facebook_button_icon_blue, 0, 0, 0);
        }
        else {
            Drawable googleG = getResources().getDrawable(R.drawable.googleg_color);
            Bitmap b = ((BitmapDrawable)googleG).getBitmap();
            Bitmap bitmapResized = Bitmap.createScaledBitmap(b, 50, 50, false);
            googleG = new BitmapDrawable(getResources(), bitmapResized);
            headerUsername.setCompoundDrawablesWithIntrinsicBounds(googleG, null, null, null);
        }

        LoginButton fbLoginButton = findViewById(R.id.header_logoff_button);
        fbLoginButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

        Button googleLogoutButton = findViewById(R.id.google_logout_btn);
        googleLogoutButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.getGoogleSignInClient().signOut();
                finish();
            }
        });

        if (!userModel.hasGoogle()) {
            googleLogoutButton.setVisibility(GONE);
            fbLoginButton.setVisibility(VISIBLE);
        }

        else if (!userModel.hasFacebook()) {
            fbLoginButton.setVisibility(GONE);
            googleLogoutButton.setVisibility(VISIBLE);
        }
    }
}
