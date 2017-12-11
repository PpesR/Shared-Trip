package fragments;

import android.annotation.TargetApi;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;
import java.util.concurrent.ExecutionException;

import adapters.MyEventsAdapter;
import adapters.MyEventsAdapter.MyEventsManager;
import adapters.ParticipatorsAdapter;
import models.MyEventModel;
import models.ParticipatorModel;
import remm.sharedtrip.ExplorationActivity;
import remm.sharedtrip.MainActivity.FbGoogleUserModel;
import remm.sharedtrip.R;
import utils.MyEventsUtil;

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
    private MyEventsManager selfManager;
    private RecyclerView recyclerView;
    private List<MyEventModel> myEvents;
    private MyEventsAdapter adapter;
    private ParticipatorsFragment frag;
    private MyEventModel lastClicked;
    private DisplayMetrics displayMetrics;
    private int dpHeight;

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

        final LayoutManager manager = new LinearLayoutManager(myActivity);
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

    public void provideParticipators(List<ParticipatorModel> models, final MyEventModel ownerEventModel, final TextView badge) {
//        participators = models;
        myActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                FragmentManager fm = myActivity.getSupportFragmentManager();
                if (notNull(frag)) {
                    frag.setManager(self);
                    frag.eventModel = ownerEventModel;
                    frag.pendingBadge = badge;
                    fm.beginTransaction()
                            .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                            .hide(frag)
                            .commit();
                }
                if (lastClicked != null && lastClicked.equals(ownerEventModel)) {
                    lastClicked = null;
                }
                else {
                    lastClicked = ownerEventModel;
                    frag = new ParticipatorsFragment();
                    frag.setManager(self);
                    frag.eventModel = ownerEventModel;
                    frag.pendingBadge = badge;
                    recyclerView.getLayoutManager().scrollToPosition(0);
                    /*fm.beginTransaction()
                            .add(R.id.fragment_container, frag)
                            .commit();*/
                }
            }
        });
    }

    public void eventClicked(int i, TextView badge) {

    }


    public void onUserApproved(final int participatorPosition, final MyEventModel eventModel, final TextView badge) {

    }

    public void onUserBanned(int participatorPosition, MyEventModel eventModel, TextView badge) {

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

    private void removeUserOnSuccess(
            boolean success,
            final int participatorPosition,
            final MyEventModel eventModel,
            final TextView badge
    ) {

    }
}
