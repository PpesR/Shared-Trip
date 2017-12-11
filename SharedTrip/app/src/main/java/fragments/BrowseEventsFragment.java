package fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import java.util.List;
import java.util.concurrent.ExecutionException;

import adapters.EventAdapter;
import models.UserEventModel;
import remm.sharedtrip.CreateEventActivity;
import remm.sharedtrip.ExplorationActivity;
import remm.sharedtrip.MainActivity.FbGoogleUserModel;
import remm.sharedtrip.R;
import remm.sharedtrip.SearchActivity;
import utils.BrowseUtil;
import utils.BrowseUtil.EventRetrievalTask;

import static android.support.v7.widget.LinearLayoutManager.HORIZONTAL;


public class BrowseEventsFragment extends Fragment {

    private List<UserEventModel> events;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private EventAdapter adapter;
    private FbGoogleUserModel userModel;
    private SearchView searchView;
    private String apiPrefix;
    private ExplorationActivity myActivity;

    private List<UserEventModel> eventsNew;
    private RecyclerView recyclerViewNew;
    private LinearLayoutManager layoutManagerNew;
    private EventAdapter adapterNew;

    private View myView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        myActivity = (ExplorationActivity) getActivity();
        userModel = myActivity.getUserModel();
        apiPrefix = myActivity.getApiPrefix();

        myView =  inflater.inflate(R.layout.fragment_browse_events, container, false);

        /* Events near you */
        recyclerView = myView.findViewById(R.id.browse_near_you_events);
        events = getEventsfromDB();
        layoutManager = new LinearLayoutManager(myActivity);
        layoutManager.setOrientation(HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new EventAdapter(myActivity, events);
        adapter.browseActivity = myActivity;
        recyclerView.setAdapter(adapter);

        /* New events */
        recyclerViewNew = myView.findViewById(R.id.browse_new_events);
        eventsNew = getNewEventsfromDB();
        layoutManagerNew = new LinearLayoutManager(myActivity);
        layoutManagerNew.setOrientation(HORIZONTAL);
        recyclerViewNew.setLayoutManager(layoutManagerNew);

        adapterNew = new EventAdapter(myActivity, events);
        adapterNew.browseActivity = myActivity;
        recyclerViewNew.setAdapter(adapterNew);

        searchView = myView.findViewById(R.id.browse_search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()  {
            @Override
            public boolean onQueryTextSubmit(String filter) {
                Intent myIntent = new Intent(myActivity, SearchActivity.class);
                myIntent.putExtra("filter", filter);
                myIntent.putExtra("prefix", apiPrefix);
                myIntent.putExtra("userid", String.valueOf(userModel.id));
                myIntent.putExtra("user",myActivity.getIntent().getStringExtra("user"));
                myActivity.startActivity(myIntent);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String filter) {
                return false;
            }
        });


        FloatingActionButton fab = myView.findViewById(R.id.add_event_fbutton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(myActivity, CreateEventActivity.class);
                myIntent.putExtra("user",myActivity.getIntent().getStringExtra("user"));
                myIntent.putExtra("prefix", apiPrefix);
                myActivity.startActivity(myIntent);
            }
        });


        return myView;
    }

    private List<UserEventModel> getNewEventsfromDB() {
        EventRetrievalTask<Void> asyncTask = new EventRetrievalTask<>(userModel.id, apiPrefix, true);

        try {
            return asyncTask.execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }


    private List<UserEventModel> getEventsfromDB() {
        EventRetrievalTask<Void> asyncTask = new EventRetrievalTask<>(userModel.id, apiPrefix, false);

        try {
            return asyncTask.execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }
}
