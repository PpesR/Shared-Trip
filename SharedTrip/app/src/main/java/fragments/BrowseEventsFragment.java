package fragments;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import adapters.EventAdapter;
import interfaces.UserModelHolder;
import models.UserEventModel;
import remm.sharedtrip.CreateEventActivity;
import remm.sharedtrip.ExplorationActivity;
import remm.sharedtrip.MainActivity.FbGoogleUserModel;
import remm.sharedtrip.R;
import remm.sharedtrip.SearchActivity;
import utils.BrowseUtil.EventExplorer;
import utils.BrowseUtil.EventRetrievalCallback;
import utils.BrowseUtil.EventRetrievalTask;

import static android.app.Activity.RESULT_OK;
import static android.support.v7.widget.LinearLayoutManager.HORIZONTAL;
import static utils.BrowseUtil.EventsPurpose.NEAR;
import static utils.BrowseUtil.EventsPurpose.NEW;


public class BrowseEventsFragment extends Fragment implements EventExplorer, UserModelHolder {

    private static final int CREATE_EVENT = 664;

    private RecyclerView recyclerViewNear;
    private LinearLayoutManager layoutManagerNear;
    private EventAdapter adapterNear;
    private FbGoogleUserModel userModel;
    private SearchView searchView;
    private ExplorationActivity myActivity;

    private TextView nearLabel;
    private TextView newLabel;

    private RecyclerView recyclerViewNew;
    private LinearLayoutManager layoutManagerNew;
    private EventAdapter adapterNew;

    private View myView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        myActivity = (ExplorationActivity) getActivity();
        userModel = myActivity.getUserModel();

        myView =  inflater.inflate(R.layout.fragment_browse_events, container, false);

        /* Events near you */
        recyclerViewNear = myView.findViewById(R.id.browse_near_you_events);
        layoutManagerNear = new LinearLayoutManager(myActivity);
        layoutManagerNear.setOrientation(HORIZONTAL);
        nearLabel = myView.findViewById(R.id.browse_near_you_label);


        /* New events */
        recyclerViewNew = myView.findViewById(R.id.browse_new_events);
        layoutManagerNew = new LinearLayoutManager(myActivity);
        layoutManagerNew.setOrientation(HORIZONTAL);
        newLabel = myView.findViewById(R.id.browse_new_label);

        searchView = myView.findViewById(R.id.browse_search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()  {
            @Override
            public boolean onQueryTextSubmit(String keyword) {
                Intent myIntent = new Intent(myActivity, SearchActivity.class);
                myIntent.putExtra("keyword", keyword);
                myIntent.putExtra("user", myActivity.getSerializedLoggedInUserModel());
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
                Intent createEvent = new Intent(myActivity, CreateEventActivity.class);
                createEvent.putExtra("user", myActivity.getSerializedLoggedInUserModel());
                myActivity.startActivityForResult(createEvent, CREATE_EVENT);
            }
        });

        return myView;
    }

    @Override
    public void onStart() {
        super.onStart();
        myActivity.spinner.setVisibility(View.VISIBLE);
        nearLabel.setVisibility(View.GONE);
        newLabel.setVisibility(View.GONE);
        recyclerViewNew.setVisibility(View.GONE);
        recyclerViewNear.setVisibility(View.GONE);
        requestNearbyEvents();
        requestNewEvents();
    }

    private void requestNearbyEvents() {
        EventRetrievalTask<Void> asyncTask = new EventRetrievalTask<>(
                userModel.id,
                false,
                new EventRetrievalCallback(this, NEAR));
        asyncTask.execute();
    }

    private void requestNewEvents() {
        EventRetrievalTask<Void> asyncTask =
                new EventRetrievalTask<>(
                        userModel.id,
                        true,
                        new EventRetrievalCallback(this, NEW));
        asyncTask.execute();
    }

    @Override
    public void DisplayNearbyEvents(List<UserEventModel> events) {
        if (!events.isEmpty()) {
            adapterNear = new EventAdapter(myActivity, events);
            myActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    myActivity.spinner.setVisibility(View.GONE);
                    nearLabel.setVisibility(View.VISIBLE);
                    recyclerViewNear.setLayoutManager(layoutManagerNear);
                    recyclerViewNear.setAdapter(adapterNear);
                    recyclerViewNear.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    @Override
    public void DisplayNewEvents(List<UserEventModel> events) {
        if (!events.isEmpty()) {
            adapterNew = new EventAdapter(myActivity, events);
            myActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    myActivity.spinner.setVisibility(View.GONE);
                    newLabel.setVisibility(View.VISIBLE);
                    recyclerViewNew.setLayoutManager(layoutManagerNew);
                    recyclerViewNew.setAdapter(adapterNew);
                    recyclerViewNew.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    @Override
    public void DisplaySearchResults(List<UserEventModel> events) {
        // do nothing (not meant for this fragment)
    }

    @Override
    public FbGoogleUserModel getLoggedInUser() {
        return userModel;
    }

    @Override
    public String getSerializedLoggedInUserModel() {
        return myActivity.getSerializedLoggedInUserModel();
    }

    @Override
    public int getLoggedInUserId() {
        return userModel.id;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==CREATE_EVENT && resultCode==RESULT_OK) {
            Toast.makeText(myActivity, "Event Created!", Toast.LENGTH_SHORT).show();
        }
    }
}
