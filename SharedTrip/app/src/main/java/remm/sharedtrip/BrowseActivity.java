package remm.sharedtrip;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import fragments.*;
import fragments.BrowseEvents;
import models.UserEventModel;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import remm.sharedtrip.MainActivity.FbGoogleUserModel;
import utils.BottomNavigationViewHelper;


public class BrowseActivity extends AppCompatActivity {


    static FbGoogleUserModel fbUserModel;

    public FbGoogleUserModel getFbUserModel() {
        return gson.fromJson(
                getIntent().getStringExtra("user")
                , FbGoogleUserModel.class);
    }

    private BottomNavigationView bottomNavigationView;
    private Gson gson = new Gson();
    private Intent ownIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        ownIntent = getIntent();
        fbUserModel = gson.fromJson(
                getIntent().getStringExtra("user")
                , FbGoogleUserModel.class);

        setContentView(R.layout.activity_browse);
        if (savedInstanceState == null) {
            BrowseEvents fragment = new BrowseEvents();
            fragment.passBrowseActivity(this);
            getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
        }

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
                                switchToFragmentBrowseEvents();
                                return true;
                            case R.id.bottombaritem_friends:
                                switchToFragmentFriendsView();
                                break;
                            case R.id.bottombaritem_stats:
                                switchToFragmentStats();
                                return true;
                            case R.id.bottombaritem_profile:
                                switchToFragmentProfile();
                                return true;
                        }
                        return true;
                    }
                });
    }

    private void switchToFragmentProfile() {
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.container, new FriendsView()).commit();

    }

    private void switchToFragmentStats() {
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.container, new StatsFragment()).commit();

    }

    private void switchToFragmentBrowseEvents() {
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.container, new BrowseEvents()).commit();

    }

    public void switchToFragmentFriendsView(){
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.container, new FriendsView()).commit();

    }

    public List<UserEventModel> getEventsfromDB() {

        EventRetrievalTask<Object> asyncTask = new EventRetrievalTask<>();
        try {
            return asyncTask.execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static class EventRetrievalTask<Void> extends AsyncTask<Void, Void, List<UserEventModel>> {

        @SafeVarargs
        @Override
        protected final List<UserEventModel> doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("http://146.185.135.219/requestrouter.php?hdl=event&act=wappr&user="+ fbUserModel.id)
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

    public FbGoogleUserModel getUserModel() {
        return fbUserModel;
    }
}
