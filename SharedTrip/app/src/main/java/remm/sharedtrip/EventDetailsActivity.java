package remm.sharedtrip;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.util.concurrent.ExecutionException;

import models.UserEventModel;
import remm.sharedtrip.MainActivity.FbGoogleUserModel;
import utils.BottomNavigationViewHelper;
import utils.EventDetailsUtils;

import static utils.ValueUtil.notNull;

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
    private FloatingActionButton fab;

    private EventDetailsActivity self;

    private UserEventModel model;
    private LocalBroadcastManager broadcaster;
    private FbGoogleUserModel userModel;
    private String apiPrefix;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        self = this;
        model = new Gson().fromJson(getIntent().getStringExtra("event"), UserEventModel.class);
        userModel = new Gson().fromJson(getIntent().getStringExtra("user"), FbGoogleUserModel.class);
        apiPrefix = getIntent().getStringExtra("prefix");
        broadcaster = LocalBroadcastManager.getInstance(this);

        setContentView(R.layout.activity_event_view);

        joinButton = findViewById(R.id.eventViewRequestButton);
        joinButton.setVisibility(View.GONE);

        fab = findViewById(R.id.enter_chat_fbutton);
        fab.setVisibility(View.GONE);
        fab.setOnClickListener(new OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                Intent chatIntent = new Intent(EventDetailsActivity.this, ChatActivity.class);
                chatIntent.putExtra("user", getIntent().getStringExtra("user"));
                chatIntent.putExtra("event", getIntent().getStringExtra("event"));
                broadcaster.sendBroadcast(new Intent("Switch to immediate"));

                startActivity(chatIntent);
            }
        });

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

        if (notNull(model.getImageLink())){
            Glide
                    .with(this)
                    .load(model.getImageLink())
                    .into(eventPic);
        }
        else {
            EventDetailsUtils.GetImageTask<Void> task = new EventDetailsUtils.GetImageTask<>(model.getId(), apiPrefix);
            try {
                String base64 = task.execute().get();
                model.setBitmap(base64);
                eventPic.setImageBitmap(model.getBitmap());
                return;
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            Toast.makeText(this, "Image download failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void joinEvent() {
        EventDetailsUtils.JoinRequestTask requestTask =
                new EventDetailsUtils.JoinRequestTask<>(
                        model.getId(),
                        userModel.id,
                        new EventDetailsUtils.JoinCallback(this));
        requestTask.execute();
    }

    private void checkApprovalStatus() {
        EventDetailsUtils.ApprovalStatusTask task =
                new EventDetailsUtils.ApprovalStatusTask(
                        model.getId(),
                        userModel.id,
                        new EventDetailsUtils.ApprovalCallback(this, model));
        task.execute();
    }

    public void onJoinSuccess() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onPendingApproval();
            }
        });
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
                    joinButton.setOnClickListener(new OnClickListener() {
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
        fab.setVisibility(View.VISIBLE);
    }

    void onPendingApproval() {
        joinButton.setBackgroundColor(Color.parseColor("#ffdf75"));
        joinButton.setTextColor(Color.parseColor("#d99d2e"));
        joinButton.setText("JOIN REQUEST PENDING");
        joinButton.setVisibility(View.VISIBLE);
        fab.setVisibility(View.GONE);
    }

    void onApproved() {
        joinButton.setBackgroundColor(Color.parseColor("#9ae083"));
        joinButton.setText("YOU ARE PARTICIPATING!");
        joinButton.setTextColor(Color.WHITE);
        joinButton.setVisibility(View.VISIBLE);
        fab.setVisibility(View.VISIBLE);
    }

    void onBanned() {
        joinButton.setBackgroundColor(Color.parseColor("#c75652"));
        joinButton.setText("YOU ARE BANNED FROM THIS EVENT!");
        joinButton.setTextColor(Color.WHITE);
        joinButton.setVisibility(View.VISIBLE);
        fab.setVisibility(View.GONE);
    }
}
