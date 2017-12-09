package remm.sharedtrip;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.List;

import adapters.MyEventsAdapter;
import adapters.ParticipatorsAdapter;
import fragments.ParticipatorsFragment;
import models.MyEventModel;
import models.ParticipatorModel;
import remm.sharedtrip.MainActivity.FbGoogleUserModel;
import utils.MyEventsUtils;

/**
 * Created by Mark on 12.11.2017.
 */

public class AdminActivity extends FragmentActivity implements MyEventsAdapter.MyEventsManager {

    private AdminActivity self;
    private MyEventsAdapter adapter;
    public ParticipatorsAdapter subAdapter;
    private RecyclerView recyclerView;
    private RecyclerView subRecyclerView;
    private ParticipatorsFragment frag;
    private FbGoogleUserModel userModel;

    private List<MyEventModel> adminEvents;
    private List<ParticipatorModel> participators;
    private MyEventModel lastClicked;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        self = this;
        userModel = new Gson().fromJson(getIntent().getStringExtra("user"), FbGoogleUserModel.class);
        setContentView(R.layout.activity_user_admin_events);
        recyclerView = findViewById(R.id.admin_event_results);
        getMyAdminEvents();
    }

    private void getMyAdminEvents() {
        /*AdminEventUtils.MyEventsRetrievalTask<Void> task =
                new AdminEventUtils.MyEventsRetrievalTask<>(
                        userModel.id,
                        new AdminEventUtils.MyEventRetrievalCallback(this));
        task.execute();*/
    }

    public void provideEvents(final List<MyEventModel> events) {
        adminEvents = events;
        runOnUiThread(new Runnable() {
            @Override
            public void run () {
                RecyclerView.LayoutManager manager = new LinearLayoutManager(self, 1, false);
                recyclerView.setLayoutManager(manager);

                adapter = new MyEventsAdapter(self, adminEvents, self);
                recyclerView.setAdapter(adapter);

            }
        });

    }

    public void provideParticipators(List<ParticipatorModel> models, final MyEventModel aeModel, final TextView badge) {
        this.participators = models;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                FragmentManager fm = getSupportFragmentManager();
                if (frag!=null) {
                    fm.beginTransaction()
                            .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                            .hide(frag)
                            .commit();
                }
                if (lastClicked != null && lastClicked.equals(aeModel)) {
                    lastClicked = null;
                }
                else {
                    lastClicked = aeModel;
                    frag = new ParticipatorsFragment();
                    frag.setManager(self);
                    frag.eventModel = aeModel;
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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                RecyclerView.LayoutManager manager = new LinearLayoutManager(self, 1, false);
                subRecyclerView.setLayoutManager(manager);
                subAdapter = new ParticipatorsAdapter(self, self, participators, aem, badge);
                subRecyclerView.setAdapter(subAdapter);
            }
        });
    }

    public void eventClicked(int i, TextView badge) {
        MyEventsUtils.ParticipatorRetrievalTask<Void> task =
                new MyEventsUtils.ParticipatorRetrievalTask<>(
                        adminEvents.get(i).getId(),
                        new MyEventsUtils.ParticipatorRetrievalCallback(this, adminEvents.get(i), badge));
        task.execute();
    }


    public void onUserApproved(int participatorPosition, MyEventModel eventModel, TextView badge) {
        MyEventsUtils.ApprovalTask<Void> task =
                new MyEventsUtils.ApprovalTask<>(
                        eventModel.getId(),
                        participators.get(participatorPosition).getId());
        task.execute();
    }

    public void onUserBanned(int participatorPosition, MyEventModel eventModel, TextView badge) {
        MyEventsUtils.DenialTask<Void> task =
                new MyEventsUtils.DenialTask<>(
                        eventModel.getId(),
                        participators.get(participatorPosition).getId());
        task.execute();
    }

    @Override
    public int getLoggedInUSerId() {
        return 0;
    }

    @Override
    public Drawable getDrawableById(int id) {
        return null;
    }
}
