package remm.sharedtrip;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.SearchView;

import com.google.gson.Gson;

import java.util.List;

import adapters.SearchResultAdapter;
import interfaces.UserModelHolder;
import models.UserEventModel;
import utils.BrowseUtil;
import utils.BrowseUtil.EventExplorer;

import static utils.BrowseUtil.EventsPurpose.SEARCH;
import static utils.UtilBase.isNull;
import static utils.UtilBase.notNullOrWhitespace;

/**
 * Created by MihkelV on 10.12.2017.
 */

public class SearchActivity extends AppCompatActivity implements EventExplorer, UserModelHolder {

    private RecyclerView recyclerView;
    private GridLayoutManager gridLayout;
    private SearchResultAdapter adapter;
    private MainActivity.FbGoogleUserModel userModel;
    private SearchView searchView;
    private String keyword;
    private String serializedUser;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent ownIntent = getIntent();
        serializedUser = ownIntent.getStringExtra("user");

        userModel = new Gson().fromJson(
                serializedUser,
                MainActivity.FbGoogleUserModel.class);

        keyword = ownIntent.getStringExtra("keyword");

        setContentView(R.layout.activity_search);

        gridLayout = new GridLayoutManager(this, 2);
        recyclerView = findViewById(R.id.eventSearchResults);
        recyclerView.setLayoutManager(gridLayout);

        searchView = findViewById(R.id.searchActivityView); //can search new events after old search
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()  {
            @Override
            public boolean onQueryTextSubmit(String keyword) {
                requestSearchResults(keyword);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String filter) {
                return false;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (notNullOrWhitespace(keyword)) {
            requestSearchResults(keyword);
        }
    }

    private void requestSearchResults(String keyword) {
        BrowseUtil.EventRetrievalTask<Void> asyncTask =
                new BrowseUtil.EventRetrievalTask<>(
                        userModel.id,
                        keyword,
                        new BrowseUtil.EventRetrievalCallback(this, SEARCH));
        asyncTask.execute();
    }

    public MainActivity.FbGoogleUserModel getUserModel() {
        return userModel;
    }

    @Override
    public void DisplayNearbyEvents(List<UserEventModel> events) { }

    @Override
    public void DisplayNewEvents(List<UserEventModel> events) { }

    @Override
    public void DisplaySearchResults(final List<UserEventModel> events) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isNull(adapter)) {
                    adapter = new SearchResultAdapter(SearchActivity.this, events);
                    recyclerView.setAdapter(adapter);

                } else {
                    adapter.replaceResults(events);
                }
            }
        });
    }

    @Override
    public MainActivity.FbGoogleUserModel getLoggedInUser() {
        return userModel;
    }

    @Override
    public String getSerializedLoggedInUserModel() {
        return serializedUser;
    }

    @Override
    public int getLoggedInUserId() {
        return userModel.id;
    }
}
