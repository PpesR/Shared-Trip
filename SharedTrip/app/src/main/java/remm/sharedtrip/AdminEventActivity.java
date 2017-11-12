package remm.sharedtrip;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import java.util.List;

import adapters.AdminEventAdapter;
import adapters.ParticipatorsAdapter;
import fragments.ParticipatorsFragment;
import models.AdminEventModel;
import models.ParticipatorModel;
import utils.AdminEventUtils;

/**
 * Created by Mark on 12.11.2017.
 */

public class AdminEventActivity extends FragmentActivity {

    private AdminEventActivity self;
    private AdminEventAdapter adapter;
    public ParticipatorsAdapter subAdapter;
    private RecyclerView recyclerView;
    private RecyclerView subRecyclerView;

    private List<AdminEventModel> adminEvents;
    private List<ParticipatorModel> participators;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        self = this;
        setContentView(R.layout.activity_user_admin_events);
        recyclerView = findViewById(R.id.admin_event_results);
        getMyAdminEvents();
    }

    private void getMyAdminEvents() {
        AdminEventUtils.AdminEventRetrievalTask<Void> task =
                new AdminEventUtils.AdminEventRetrievalTask<>(
                        BrowseEvents.fbUserModel.id,
                        new AdminEventUtils.AndminEventRetrievalCallback(this));
        task.execute();
    }

    public void provideEvents(final List<AdminEventModel> events) {
        adminEvents = events;
        runOnUiThread(new Runnable() {
            @Override
            public void run () {
                RecyclerView.LayoutManager manager = new LinearLayoutManager(self, 1, false);
                recyclerView.setLayoutManager(manager);

                adapter = new AdminEventAdapter(self, adminEvents, self);
                recyclerView.setAdapter(adapter);

            }
        });

    }

    public void provideParticipators(List<ParticipatorModel> models, final AdminEventModel aeModel, final TextView badge) {
        this.participators = models;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ParticipatorsFragment frag = new ParticipatorsFragment();
                frag.setAea(self);
                frag.aem = aeModel;
                frag.badge = badge;
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.fragment_container, frag)
                        .commit();

            }
        });
    }

    public void doAdapterThing(RecyclerView subRecycler, final AdminEventModel aem, final TextView badge) {
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
        AdminEventUtils.ParticipatorRetrievalTask<Void> task =
                new AdminEventUtils.ParticipatorRetrievalTask<>(
                        adminEvents.get(i).getId(),
                        new AdminEventUtils.ParticipatorRetrievalCallback(this, adminEvents.get(i), badge));
        task.execute();
    }


    public void onUserApproved(int participatorPosition, AdminEventModel eventModel, TextView badge) {
        AdminEventUtils.ApprovalTask<Void> task =
                new AdminEventUtils.ApprovalTask<>(
                        eventModel.getId(),
                        participators.get(participatorPosition).getId(),
                        new AdminEventUtils.ApprovalCallback(self, participatorPosition, badge, eventModel));
        task.execute();
    }

    public void onUserBanned(int participatorPosition, AdminEventModel eventModel, TextView badge) {
        AdminEventUtils.DenialTask<Void> task =
                new AdminEventUtils.DenialTask<>(
                        eventModel.getId(),
                        participators.get(participatorPosition).getId(),
                        new AdminEventUtils.DenialCallback(self, participatorPosition, badge, eventModel));
        task.execute();
    }
}
