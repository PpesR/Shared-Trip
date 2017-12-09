package fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
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
import remm.sharedtrip.BrowseActivity;
import remm.sharedtrip.MainActivity;
import remm.sharedtrip.R;
import utils.MyEventsUtils;
import utils.MyEventsUtils.ApprovalTask;
import utils.MyEventsUtils.DenialTask;

import static utils.ValueUtil.notNull;

/**
 * Created by Mark on 9.12.2017.
 */

public class MyEventsFragment extends Fragment implements MyEventsManager {
    private BrowseActivity myActivity;
    private MainActivity.FbGoogleUserModel userModel;
    private String apiPrefix;
    private View myView;
    private MyEventsFragment self;
    private MyEventsManager selfManager;
    private RecyclerView recyclerView;
    private List<MyEventModel> myEvents;
    private MyEventsAdapter adapter;
    private List<ParticipatorModel> participators;
    private ParticipatorsFragment frag;
    private RecyclerView subRecyclerView;
    private MyEventModel lastClicked;
    private ParticipatorsAdapter subAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        self = this;
        myActivity = (BrowseActivity) getActivity();
        userModel = myActivity.getUserModel();
        apiPrefix = myActivity.getApiPrefix();

        myView =  inflater.inflate(R.layout.fragment_my_events, container, false);
        recyclerView = myView.findViewById(R.id.my_event_results);
        getMyEvents();

        return myView;
    }

    private void getMyEvents() {
        MyEventsUtils.MyEventsRetrievalTask<Void> task =
                new MyEventsUtils.MyEventsRetrievalTask<>(userModel.id);
        try {
            List<MyEventModel> events = task.execute().get();
            provideEvents(events);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void provideEvents(final List<MyEventModel> events) {
        myEvents = events;

        LayoutManager manager = new LinearLayoutManager(myActivity);
        adapter = new MyEventsAdapter(myActivity, myEvents, this);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
    }

    public void provideParticipators(List<ParticipatorModel> models, final MyEventModel ownerEventModel, final TextView badge) {
        participators = models;
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
                    fm.beginTransaction()
                            .add(R.id.fragment_container, frag)
                            .commit();
                }
            }
        });
    }

    public void setSubAdapter(RecyclerView subRecycler, final MyEventModel aem, final TextView badge) {
        subRecyclerView = subRecycler;
        myActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LayoutManager manager = new LinearLayoutManager(myActivity, 1, false);
                subRecyclerView.setLayoutManager(manager);
                subAdapter = new ParticipatorsAdapter(self, myActivity, participators, aem, badge);
                subRecyclerView.setAdapter(subAdapter);
            }
        });
    }

    public void eventClicked(int i, TextView badge) {
        MyEventsUtils.ParticipatorRetrievalTask<Void> task =
                new MyEventsUtils.ParticipatorRetrievalTask<>(
                        myEvents.get(i).getId(),
                        new MyEventsUtils.ParticipatorRetrievalCallback(this, myEvents.get(i), badge));
        task.execute();
    }


    public void onUserApproved(final int participatorPosition, final MyEventModel eventModel, final TextView badge) {
        ApprovalTask<Void> task =
                new ApprovalTask<>(
                        eventModel.getId(),
                        participators.get(participatorPosition).getId());
        try {
            removeUserOnSuccess(
                    task.execute().get(),
                    participatorPosition,
                    eventModel, badge);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void onUserBanned(int participatorPosition, MyEventModel eventModel, TextView badge) {
        DenialTask<Void> task =
                new DenialTask<>(
                        eventModel.getId(),
                        participators.get(participatorPosition).getId());
        try {
            removeUserOnSuccess(
                    task.execute().get(),
                    participatorPosition,
                    eventModel, badge);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getLoggedInUSerId() {
        return userModel.id;
    }

    @Override
    public Drawable getDrawableById(int id) {
        return getResources().getDrawable(id);
    }

    private void removeUserOnSuccess(
            boolean success,
            final int participatorPosition,
            final MyEventModel eventModel,
            final TextView badge
    ) {
        if (success) {
            myActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    subAdapter.participators.remove(participatorPosition);
                    subAdapter.notifyDataSetChanged();
                    badge.setText(eventModel.getUsersPending() + "");
                    if (eventModel.getUsersPending() == 0) {
                        badge.setVisibility(View.GONE);
                    }
                }});
        }
    }
}
