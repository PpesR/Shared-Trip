package remm.sharedtrip;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.List;

import models.AdminEventModel;
import models.EventModel;

/**
 * Created by Mark on 12.11.2017.
 */

public class AdminEventActivity extends Activity {

    private AdminEventActivity self;
    private AdminEventAdapter adapter;
    private RecyclerView recyclerView;

    private List<AdminEventModel> adminEvents;

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

    void provideEvents(List<AdminEventModel> events) {
        adminEvents = events;
        runOnUiThread(new Runnable() {
            @Override
            public void run () {
                GridLayoutManager manager = new GridLayoutManager(self, adminEvents.size());
                recyclerView.setLayoutManager(manager);

                adapter = new AdminEventAdapter(self, adminEvents);
                recyclerView.setAdapter(adapter);

            }
        });

    }


}
