package fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import java.util.List;
import java.util.concurrent.ExecutionException;

import adapters.EventAdapter;
import models.UserEventModel;
import remm.sharedtrip.BrowseActivity;
import remm.sharedtrip.CreateEvent;
import remm.sharedtrip.MainActivity.FbGoogleUserModel;
import remm.sharedtrip.R;
import utils.BrowseUtil;


public class BrowseEvents extends Fragment {

    private List<UserEventModel> events;
    private RecyclerView recyclerView;
    private GridLayoutManager gridLayout;
    private EventAdapter adapter;
    private FbGoogleUserModel userModel;
    private SearchView searchView;
    private String apiPrefix;
    private BrowseActivity myActivity;
    private View myView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        myActivity = (BrowseActivity) getActivity();
        userModel = myActivity.getUserModel();
        apiPrefix = myActivity.getApiPrefix();

        myView =  inflater.inflate(R.layout.fragment_browse_events, container, false);

        recyclerView = myView.findViewById(R.id.eventResults);
        events = getEventsfromDB();
        gridLayout = new GridLayoutManager(myActivity, events.size());
        recyclerView.setLayoutManager(gridLayout);

        adapter = new EventAdapter(myActivity, events);
        adapter.browseActivity = myActivity;
        recyclerView.setAdapter(adapter);


        FloatingActionButton fab = myView.findViewById(R.id.add_event_fbutton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(myActivity, CreateEvent.class);
                myIntent.putExtra("user", myActivity.getIntent().getStringExtra("user"));
                myIntent.putExtra("prefix", apiPrefix);
                myActivity.startActivity(myIntent);
            }
        });

        return myView;
    }

    public void passBrowseActivity(BrowseActivity ba) {
        /*adapter = new EventAdapter(myActivity, events);
        adapter.be = ba;
        recyclerView.setAdapter(adapter);*/
    }

    private List<UserEventModel> getEventsfromDB() {

        BrowseUtil.EventRetrievalTask<Void> asyncTask = new BrowseUtil.EventRetrievalTask<>(userModel.id, apiPrefix);
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
