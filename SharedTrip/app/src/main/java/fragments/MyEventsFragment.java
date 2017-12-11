package fragments;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import adapters.JoinRequestsAdapter.RequestUserModel;
import adapters.MyEventsAdapter;
import adapters.MyEventsAdapter.MyEventsManager;
import models.MyEventModel;
import remm.sharedtrip.ExplorationActivity;
import remm.sharedtrip.MainActivity.FbGoogleUserModel;
import remm.sharedtrip.R;
import remm.sharedtrip.RequestManagementActivity;
import utils.MyEventsUtil;

import static android.app.Activity.RESULT_OK;
import static utils.ValueUtil.notNull;

/**
 * Created by Mark on 9.12.2017.
 */

public class MyEventsFragment extends Fragment implements MyEventsManager {
    private ExplorationActivity myActivity;
    private FbGoogleUserModel userModel;
    private String apiPrefix;
    private View myView;
    private MyEventsFragment self;
    private RecyclerView recyclerView;
    private List<MyEventModel> myEvents;
    private MyEventsAdapter adapter;
    private DisplayMetrics displayMetrics;
    private int dpHeight;
    private MyEventsAdapter.MyEventViewHolder lastClicked = null;

    private static final int JOIN_REQUEST_MANAGEMENT = 530;
    private LinearLayoutManager manager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        self = this;
        myActivity = (ExplorationActivity) getActivity();
        userModel = myActivity.getUserModel();
        apiPrefix = myActivity.getApiPrefix();

        myView =  inflater.inflate(R.layout.fragment_my_events, container, false);
        recyclerView = myView.findViewById(R.id.my_event_results);
        displayMetrics = myActivity.getResources().getDisplayMetrics();
        dpHeight = (int) Math.floor(displayMetrics.heightPixels / displayMetrics.density * 1.4);
        getMyEvents();

        return myView;
    }

    private void getMyEvents() {
        MyEventsUtil.MyEventsRetrievalTask<Void> task =
                new MyEventsUtil.MyEventsRetrievalTask<>(userModel.id, apiPrefix);
        try {
            List<MyEventModel> events = task.execute().get();
            provideEvents(events);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void provideEvents(final List<MyEventModel> events) {
        myEvents = events;

        manager = new LinearLayoutManager(myActivity);
        adapter = new MyEventsAdapter(myActivity, myEvents, this);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        OnScrollListener mScrollListener = new OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (events.size()*120 >= dpHeight-100 && !recyclerView.canScrollVertically(1)){
                    myActivity.hideNavbar();
                }
                else myActivity.showNavBar();
            }
        };
        recyclerView.addOnScrollListener(mScrollListener);
    }

    @Override
    public int getUserModelId() { return userModel.id; }

    @Override
    public Drawable getDrawableById(int id) {
        return getResources().getDrawable(id);
    }

    @Override
    public String getApiPrefix() { return apiPrefix; }

    @Override
    public FbGoogleUserModel getUserModel() { return userModel; }

    @Override
    public void startDetailsActivity(Intent detailViewIntent) {
        startActivity(detailViewIntent);
    }

    @Override
    public void startRequestManagementActivity(int eventId, List<RequestUserModel> pending, MyEventsAdapter.MyEventViewHolder holder) {
        lastClicked = holder;
        Intent requestManagement = new Intent(myActivity, RequestManagementActivity.class);
        requestManagement.putExtra("prefix", apiPrefix);
        requestManagement.putExtra("event", eventId);
        requestManagement.putExtra("user", myActivity.getIntent().getStringExtra("user"));
        ArrayList<String> serialized = new ArrayList<>();
        Gson gson = new Gson();
        for (RequestUserModel model : pending) {
            serialized.add(gson.toJson(model));
        }
        requestManagement.putExtra("requesters", serialized);
        startActivityForResult(requestManagement, JOIN_REQUEST_MANAGEMENT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==JOIN_REQUEST_MANAGEMENT && resultCode == RESULT_OK) {
            int count = data.getIntExtra("handled", 0);
            if (count > 0 && notNull(lastClicked)) {
                int id = data.getIntExtra("event", -1);
                for (int i = 0; i < myEvents.size(); i++) {
                    MyEventModel model = myEvents.get(i);
                    if (model.getId() == id) {
                        model.setUsersPending(model.getUsersPending() - count);

                        if (model.getUsersPending() < 0)
                            model.setUsersPending(0);

                        lastClicked.amount.setText(model.getUsersPending()+"");

                        if (model.getUsersPending() == 0)
                            lastClicked.badge.setVisibility(View.GONE);
                        break;
                    }
                }
            }

        }
    }
}
