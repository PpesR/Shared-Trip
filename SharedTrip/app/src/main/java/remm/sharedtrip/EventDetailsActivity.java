package remm.sharedtrip;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import adapters.NewAdminChoiceAdapter;
import adapters.NewAdminChoiceAdapter.PotentialAdminModel;
import models.UserEventModel;
import remm.sharedtrip.MainActivity.FbGoogleUserModel;
import utils.EventDetailsUtil.ApprovalStatusTask;
import utils.EventDetailsUtil.GetImageTask;
import utils.EventDetailsUtil.JoinRequestTask;
import utils.UserAccountUtil;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static utils.EventDetailsUtil.*;
import static utils.ValueUtil.notNull;

/**
 * Created by Mark on 12.11.2017.
 */

public class EventDetailsActivity extends FragmentActivity implements NewAdminChoiceAdapter.DetailsManager {

    private ImageView eventPic;
    private TextView eventName;
    private TextView eventLocation;
    private TextView eventCost;
    private TextView eventFreeSpots;
    private TextView eventDescription;
    private TextView status;
    private TextView pleaseSelect;

    private Button joinButton;
    private FloatingActionButton fab;

    private EventDetailsActivity self;

    private UserEventModel model;
    private LocalBroadcastManager broadcaster;
    private FbGoogleUserModel userModel;
    private String apiPrefix;
    private Resources resources;
    private Drawable icon;
    private int statusColor;
    private List<PotentialAdminModel> participators;
    private NewAdminChoiceAdapter adapter;
    private RecyclerView recyclerView;
    private GridLayoutManager manager;
    private Button cancelButton;
    private ScrollView mainScrollView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        self = this;
        model = new Gson().fromJson(getIntent().getStringExtra("event"), UserEventModel.class);
        userModel = new Gson().fromJson(getIntent().getStringExtra("user"), FbGoogleUserModel.class);
        apiPrefix = getIntent().getStringExtra("prefix");
        broadcaster = LocalBroadcastManager.getInstance(this);
        resources = getResources();

        participators = getParticipators();


        setContentView(R.layout.activity_event_details);

        recyclerView = findViewById(R.id.my_event_participators);
        manager = new GridLayoutManager(self, 3);
        recyclerView.setLayoutManager(manager);
        pleaseSelect = findViewById(R.id.event_detail_new_admin_label);
        mainScrollView = findViewById(R.id.eventViewScrollView);

        adapter = new NewAdminChoiceAdapter(self, participators, self);
        recyclerView.setAdapter(adapter);

        joinButton = findViewById(R.id.eventViewRequestButton);
        joinButton.setVisibility(GONE);

        cancelButton = findViewById(R.id.event_detail_cancel_admin);
        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.notChoosing();
                cancelButton.setVisibility(GONE);
                pleaseSelect.setVisibility(GONE);
                adapter.restoreLayout();
                onAdmin();
            }
        });

        fab = findViewById(R.id.enter_chat_fbutton);
        fab.setVisibility(GONE);
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

        eventPic = findViewById(R.id.eventViewPicture);
        eventName = findViewById(R.id.event_detail_name);
        eventCost = findViewById(R.id.eventViewCostPerNight);
        eventFreeSpots = findViewById(R.id.eventViewFreeSpots);
        eventDescription = findViewById(R.id.eventViewDescription);
        eventLocation = findViewById(R.id.eventViewLocationGPS);
        status = findViewById(R.id.events_details_status);

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
            GetImageTask<Void> task = new GetImageTask<>(model.getId(), apiPrefix);
            try {
                String base64 = task.execute().get();
                model.setBitmap(base64);
//                eventPic.setImageBitmap(model.getBitmap());
                return;
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            Toast.makeText(this, "Image download failed", Toast.LENGTH_SHORT).show();
        }

        checkApprovalStatus();
    }

    private List<PotentialAdminModel> getParticipators() {
        // tegelt peaks selle listi requestiga saama
        ParticipatorsTask<Void> task = new ParticipatorsTask<>(model.getId(), apiPrefix);
        List<PotentialAdminModel> list = new ArrayList<>();
        try {
            list = task.execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return list;
    }

    private void addSelfToParticipators() {
        PotentialAdminModel example = new PotentialAdminModel();
        example.profilePicture = Uri.parse(userModel.imageUriString);
        example.fullName = userModel.name;
        example.id = userModel.id;
        example.firstName = userModel.firstName;
        participators.add(example);
        adapter.notifyDataSetChanged();
    }

    private void joinEvent() {
        JoinRequestTask requestTask =
                new JoinRequestTask<>(
                        model.getId(),
                        userModel.id,
                        new JoinCallback(this));
        requestTask.execute();
    }

    private void checkApprovalStatus() {
        ApprovalStatusTask task =
                new ApprovalStatusTask(
                        model.getId(),
                        userModel.id,
                        new ApprovalCallback(this, model), apiPrefix);
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

    public void onLeaveSuccess(){
        runOnUiThread(new Runnable(){
            @Override
            public void run(){
                joinButton.setVisibility(VISIBLE);
                joinButton.setText("REQUEST TO JOIN");
                joinButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        joinEvent();
                    }
                });
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
                    joinButton.setVisibility(VISIBLE);
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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void onAdmin() {
        statusColor = resources.getColor(R.color.golden);
        icon = resources.getDrawable(R.drawable.ic_star_black_24dp);
        icon.setTint(statusColor);

        status.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
        status.setTextColor(statusColor);
        status.setText("  admin");

        joinButton.setText("GIVE AWAY ADMIN RIGHTS");
        joinButton.setVisibility(View.VISIBLE);
        joinButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                joinButton.setVisibility(GONE);
                joinButton.setText("CONFIRM");
                cancelButton.setVisibility(VISIBLE);
                pleaseSelect.setVisibility(VISIBLE);
                adapter.isChoosing();
                joinButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        adapter.notChoosing();
                        cancelButton.setVisibility(GONE);
                        pleaseSelect.setVisibility(GONE);
                        adapter.restoreLayout();
                        addSelfToParticipators();

                        // To callback
                        AdminRightsTask<Void> task = new AdminRightsTask<>(model.getId(), adapter.selected.userModel.id, userModel.id, apiPrefix);
                        try {
                            boolean success = task.execute().get();
                            if (success) {
                                onNewAdminSelectedDone(adapter.selected.userModel);
                                onApproved();
                                return;
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                        cancelButton.callOnClick();

                    }
                });
            }
        });

        fab.setVisibility(VISIBLE);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    void onPendingApproval() {
        statusColor = resources.getColor(R.color.light_gray);
        icon = resources.getDrawable(R.drawable.ic_mail_outline_black_24dp);
        icon.setTint(statusColor);

        status.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
        status.setText("  join request sent");
        status.setTextColor(statusColor);

        joinButton.setText("CANCEL REQUEST");
        joinButton.setVisibility(VISIBLE);
        joinButton.setOnClickListener(new OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                leaveEvent();
            }
        });

        fab.setVisibility(GONE);
        
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    void onApproved() {
        statusColor = resources.getColor(R.color.light_gray);
        icon = resources.getDrawable(R.drawable.ic_check_black_24dp);
        icon.setTint(statusColor);

        status.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
        status.setText("  participating");
        status.setTextColor(statusColor);

        joinButton.setText("LEAVE EVENT");
        joinButton.setTextColor(resources.getColor(R.color.white_text));
        joinButton.setVisibility(VISIBLE);
        joinButton.setOnClickListener(new OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                leaveEvent();
            }
        });

        fab.setVisibility(VISIBLE);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    void onBanned() {
        statusColor = resources.getColor(R.color.calm_red);
        icon = resources.getDrawable(R.drawable.ic_close_black_24px);
        icon.setTint(statusColor);

        status.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
        status.setTextColor(statusColor);
        status.setText("  BANNED");

        joinButton.setVisibility(GONE);

        fab.setVisibility(GONE);
    }

    private void leaveEvent(){
        LeaveRequestTask requestTask =
                new LeaveRequestTask<>(
                        model.getId(),
                        userModel.id,
                        new LeaveCallback(this),
                        apiPrefix);
        requestTask.execute();
    }

    @Override
    public void onNewAdminSelectedDone(PotentialAdminModel newAdmin) {
        mainScrollView.fullScroll(ScrollView.FOCUS_UP);
        // send admin change request
    }

    public void showConfirm() {
        joinButton.setVisibility(VISIBLE);
    }

    @Override
    public void openUserProfile(int id) {
        UserAccountUtil.UserDataTask<Void> task = new UserAccountUtil.UserDataTask<>(apiPrefix, id);
        try {
            FbGoogleUserModel selectedUser = task.execute().get();
            Intent profileIntent = new Intent(this, ProfileActivity.class);
            profileIntent.putExtra("user", new Gson().toJson(selectedUser));
            profileIntent.putExtra("notMine", selectedUser.id!=userModel.id);
            startActivity(profileIntent);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
