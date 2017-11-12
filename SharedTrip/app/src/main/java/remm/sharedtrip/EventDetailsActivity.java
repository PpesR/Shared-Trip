package remm.sharedtrip;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import models.UserEventModel;
import utils.BottomNavigationViewHelper;
import utils.EventDetailsUtils;

/**
 * Created by Mark on 12.11.2017.
 */

public class EventDetailsActivity extends FragmentActivity {

    private ImageView eventPic;
    private TextView eventName;
    private TextView eventLocation;
    private TextView eventCost;
    private TextView eventFreeSpots;
    private TextView eventDescription;

    private Button joinButton;
    private BottomNavigationView bottomNavigationView;
    private EventDetailsActivity self;

    private UserEventModel model;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        self = this;
        model = new Gson().fromJson(getIntent().getStringExtra("event"), UserEventModel.class);

        setContentView(R.layout.activity_event_view);
        setUpNavbar();

        joinButton = findViewById(R.id.eventViewRequestButton);
        joinButton.setVisibility(View.GONE);

        checkApprovalStatus();

        eventPic = findViewById(R.id.eventViewPicture);
        eventName = findViewById(R.id.eventViewLocation);
        eventCost = findViewById(R.id.eventViewCostPerNight);
        eventFreeSpots = findViewById(R.id.eventViewFreeSpots);
        eventDescription = findViewById(R.id.eventViewDescription);
        eventLocation = findViewById(R.id.eventViewLocationGPS);

        eventName.setText(model.getName());
        eventDescription.setText(model.getDescription());
        eventCost.setText(model.getCost()+"€ "+eventCost.getText());
        eventFreeSpots.setText(eventFreeSpots.getText()+": "+model.getSpots());
        eventLocation.setText(eventLocation.getText()+": "+model.getLoc());

        Glide
            .with(this)
            .load(model.getImageLink())
            .into(eventPic);
    }

    private void joinEvent() {
        EventDetailsUtils.JoinRequestTask requestTask =
                new EventDetailsUtils.JoinRequestTask<>(
                        model.getId(),
                        BrowseEvents.fbUserModel.id,
                        new EventDetailsUtils.JoinCallback(this));
        requestTask.execute();
    }

    private void checkApprovalStatus() {
        EventDetailsUtils.ApprovalStatusTask task =
                new EventDetailsUtils.ApprovalStatusTask(
                        model.getId(),
                        BrowseEvents.fbUserModel.id,
                        new EventDetailsUtils.ApprovalCallback(this, model));
        task.execute();
    }

    public void onJoinSuccess() {
        onPendingApproval();
    }

    public void onApprovalStatusReady() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (model.isAdmin()) {
                    onAdmin();
                } else if (model.isUserApproved()) {
                    onApproved();
                } else if (model.isApprovalPending()) {
                    onPendingApproval();
                } else if (model.isUserBanned()) {
                    onBanned();
                } else {
                    joinButton.setVisibility(View.VISIBLE);
                    joinButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            joinEvent();
                        }
                    });
                }
            }
        });
    }

    private void onAdmin() {
        joinButton.setBackgroundColor(Color.TRANSPARENT);
        joinButton.setTextColor(Color.parseColor("#8ad073"));
        joinButton.setTextSize(24);
        joinButton.setText("YOU ARE THE ADMIN!");
        joinButton.setVisibility(View.VISIBLE);
    }

    void onPendingApproval() {
        joinButton.setBackgroundColor(Color.parseColor("#ffdf75"));
        joinButton.setTextColor(Color.parseColor("#d99d2e"));
        joinButton.setText("JOIN REQUEST PENDING");
        joinButton.setVisibility(View.VISIBLE);
    }

    void onApproved() {
        joinButton.setBackgroundColor(Color.parseColor("#9ae083"));
        joinButton.setText("YOU ARE PARTICIPATING!");
        joinButton.setTextColor(Color.WHITE);
        joinButton.setVisibility(View.VISIBLE);
    }

    void onBanned() {
        joinButton.setBackgroundColor(Color.parseColor("#c75652"));
        joinButton.setText("YOU ARE BANNED FROM THIS EVENT!");
        joinButton.setTextColor(Color.WHITE);
        joinButton.setVisibility(View.VISIBLE);
    }

    private void setUpNavbar() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        BottomNavigationViewHelper
                .disableShiftMode(bottomNavigationView);

        MenuItem profileItem = bottomNavigationView.getMenu()
                .findItem(R.id.bottombaritem_profile);
        profileItem.setTitle(BrowseEvents.fbUserModel.firstName);

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.bottombaritem_events:
                                finish();
                                return true;
                            case R.id.bottombaritem_friends:
                                Intent friendsViewActivity = new Intent(self, FriendsViewActivity.class);
                                startActivity(friendsViewActivity);
                                return true;
                            case R.id.bottombaritem_stats:
                                Intent statsViewActivity = new Intent(self, StatsViewActivity.class);
                                startActivity(statsViewActivity);
                                return true;
                            case R.id.bottombaritem_profile:
                                // TODO
                                return true;
                        }
                        return true;
                    }
                });
    }
}
