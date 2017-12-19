package remm.sharedtrip;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

import adapters.NewAdminChoiceAdapter;
import adapters.NewAdminChoiceAdapter.MiniUserModel;
import models.UserEventModel;
import remm.sharedtrip.MainActivity.FbGoogleUserModel;
import utils.EventDetailsUtil.ApprovalStatusTask;
import utils.EventDetailsUtil.GetImageTask;
import utils.EventDetailsUtil.JoinRequestTask;
import utils.UserAccountUtil.UserDataTask;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static remm.sharedtrip.EventDetailsActivity.ParticipatorStatus.ADMIN;
import static remm.sharedtrip.EventDetailsActivity.ParticipatorStatus.BANNED;
import static remm.sharedtrip.EventDetailsActivity.ParticipatorStatus.JOINED;
import static remm.sharedtrip.EventDetailsActivity.ParticipatorStatus.PENDING;
import static remm.sharedtrip.EventDetailsActivity.ParticipatorStatus.VIEWING;
import static utils.EventDetailsUtil.AdminRightsTask;
import static utils.EventDetailsUtil.ApprovalCallback;
import static utils.EventDetailsUtil.JoinCallback;
import static utils.EventDetailsUtil.LeaveCallback;
import static utils.EventDetailsUtil.LeaveRequestTask;
import static utils.EventDetailsUtil.ParticipatorsTask;
import static utils.UtilBase.notNull;

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

    private SimpleDateFormat dateFormatUTC;
    private SimpleDateFormat dateFormat;

    private Button joinButton;
    private FloatingActionButton fab;

    private EventDetailsActivity self;

    private UserEventModel model;
    private LocalBroadcastManager broadcaster;
    private FbGoogleUserModel userModel;
    private Resources resources;
    private Drawable icon;
    private int statusColor;
    private List<MiniUserModel> participators;
    private NewAdminChoiceAdapter adapter;
    private RecyclerView recyclerView;
    private GridLayoutManager manager;
    private Button cancelButton;
    private ScrollView mainScrollView;
    private ParticipatorStatus myStatus = VIEWING;

    public static final int OPEN_PROFILE = 777;

    public enum ParticipatorStatus {
        VIEWING,
        PENDING,
        JOINED,
        ADMIN,
        BANNED
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        self = this;
        model = new Gson().fromJson(getIntent().getStringExtra("event"), UserEventModel.class);
        userModel = new Gson().fromJson(getIntent().getStringExtra("user"), FbGoogleUserModel.class);
        broadcaster = LocalBroadcastManager.getInstance(this);
        resources = getResources();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormatUTC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormatUTC.setTimeZone(TimeZone.getTimeZone("UTC"));

        participators = getParticipators();

        setContentView(R.layout.activity_event_details);

        recyclerView = findViewById(R.id.my_event_participators);
        manager = new GridLayoutManager(self, 3);
        recyclerView.setLayoutManager(manager);
        pleaseSelect = findViewById(R.id.event_detail_new_admin_label);
        mainScrollView = findViewById(R.id.eventViewScrollView);
        mainScrollView.smoothScrollTo(0,0);
        if (participators.size() > 0) {
            adapter = new NewAdminChoiceAdapter(self, participators, self);
            recyclerView.setAdapter(adapter);
        }
        else {
            pleaseSelect.setVisibility(VISIBLE);
            pleaseSelect.setText(R.string.placeholder_no_participants);
        }

        joinButton = findViewById(R.id.event_detail_main_button);
        joinButton.setVisibility(GONE);

        cancelButton = findViewById(R.id.event_detail_cancel_button);
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
        eventCost = findViewById(R.id.event_detail_cost);
        eventFreeSpots = findViewById(R.id.event_detail_spots);
        eventDescription = findViewById(R.id.event_detail_description);
        eventLocation = findViewById(R.id.event_detail_location);
        status = findViewById(R.id.events_details_status);

        eventName.setText(model.getName());
        eventDescription.setText(model.getDescription());
        eventCost.setText(model.getCost()+"€ "+eventCost.getText());

        int numberOfParticipants = participators.size();
        eventFreeSpots.append(": "+numberOfParticipants+"/"+model.getSpots());
        if (numberOfParticipants > model.getSpots()/3) {
            eventFreeSpots.setTextColor(resources.getColor(R.color.orange));
            if (numberOfParticipants > 2*model.getSpots()/3) {
                eventFreeSpots.setTextColor(resources.getColor(R.color.orangered));
            }
        }

        eventLocation.setText(eventLocation.getText()+": "+model.getLoc());

        if (notNull(model.getImageLink())){
            Glide
                    .with(this)
                    .load(model.getImageLink())
                    .into(eventPic);
        }
        else {
            GetImageTask<Void> task = new GetImageTask<>(model.getId());
            try {
                String base64 = task.execute().get();
                model.setBitmap(base64);
                eventPic.setImageBitmap(model.getBitmap());
            } catch (InterruptedException e) {
                e.printStackTrace();
                Toast.makeText(this, "Image download failed", Toast.LENGTH_SHORT).show();
            } catch (ExecutionException e) {
                e.printStackTrace();
                Toast.makeText(this, "Image download failed", Toast.LENGTH_SHORT).show();
            }
        }

        checkApprovalStatus();
    }

    private List<MiniUserModel> getParticipators() {
        ParticipatorsTask<Void> task = new ParticipatorsTask<>(model.getId());
        List<MiniUserModel> list = new ArrayList<>();
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
        MiniUserModel example = new MiniUserModel();
        example.profilePicture = Uri.parse(userModel.imageUriString);
        example.fullName = userModel.name;
        example.id = userModel.id;
        example.firstName = userModel.firstName;
        participators.add(example);
        adapter.notifyDataSetChanged();
    }

    private void removeSelfFromParticipators() {
        for (int i = 0; i < participators.size(); i++) {
            if (participators.get(i).id == userModel.id) {
                participators.remove(i);
                adapter.notifyDataSetChanged();
                break;
            }
        }
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
                        new ApprovalCallback(this, model));
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
                fab.setVisibility(GONE);
                status.setText(R.string.status_just_viewing);
                if (participators.size() <= model.getSpots()) {
                    joinButton.setText(R.string.button_request_to_join);
                    joinButton.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            joinEvent();
                        }
                    });
                }
                else {
                    onEventFull();
                }
                myStatus = VIEWING;
                removeSelfFromParticipators();
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
                    if (participators.size() < model.getSpots()) {
                        joinButton.setVisibility(VISIBLE);
                        joinButton.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                joinEvent();
                            }
                        });
                    }
                    else onEventFull();
                }

                if (notNull(model.getStartDate()) && notNull(model.getEndDate())) {
                    try {
                        Date
                                endDate = dateFormatUTC.parse(model.getEndDate()),
                                startDate = dateFormatUTC.parse(model.getStartDate()),
                                now = new Date();
                        if (endDate.before(now)) {
                            onEventEnded();
                        } else if (startDate.before(now)) {
                            onEventStarted();
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                else onEventEnded();
            }
        });
    }

    private void onEventFull() {
        joinButton.setVisibility(GONE);
        status.setText("the event is full");
        status.setTextColor(resources.getColor(R.color.orangered));
        status.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
    }

    private void onEventEnded() {
        joinButton.setVisibility(GONE);
        status.setText("the event has ended");
        status.setTextColor(resources.getColor(R.color.orangered));
        status.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
    }

    private void onEventStarted() {
        joinButton.setVisibility(GONE);
        status.setText("the event has already started");
        status.setTextColor(resources.getColor(R.color.orange));
        status.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
    }

    private void onAdmin() {
        myStatus = ADMIN;
        statusColor = resources.getColor(R.color.golden);
        icon = resources.getDrawable(R.drawable.ic_star_black_24dp);
        icon.setColorFilter(statusColor, PorterDuff.Mode.SRC_ATOP);

        status.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
        status.setTextColor(statusColor);
        status.setText(R.string.status_admin);

        if (participators.size() > 0) {
            joinButton.setText(R.string.button_give_admin_rights);
            joinButton.setVisibility(View.VISIBLE);
            joinButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    joinButton.setVisibility(GONE);
                    joinButton.setText(R.string.button_confirm);
                    cancelButton.setVisibility(VISIBLE);
                    pleaseSelect.setVisibility(VISIBLE);
                    adapter.isChoosing();
                    joinButton.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            // To callback
                            AdminRightsTask<Void> task = new AdminRightsTask<>(model.getId(), adapter.selected.userModel.id, userModel.id);
                            try {
                                boolean success = task.execute().get();
                                if (success) {
                                    adapter.notChoosing();
                                    cancelButton.setVisibility(GONE);
                                    pleaseSelect.setVisibility(GONE);
                                    adapter.restoreLayout();
                                    addSelfToParticipators();
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
        }

        fab.setVisibility(VISIBLE);
    }

    void onPendingApproval() {
        myStatus = PENDING;
        statusColor = resources.getColor(R.color.light_gray);
        icon = resources.getDrawable(R.drawable.ic_mail_outline_black_24dp);
        icon.setColorFilter(statusColor, PorterDuff.Mode.SRC_ATOP);

        if (participators.size()<model.getSpots()) {
            status.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
            status.setText(R.string.join_request_sent);
            status.setTextColor(statusColor);
        }
        else {
            status.setText("the event is full");
            status.setTextColor(resources.getColor(R.color.orangered));
        }

        joinButton.setText(R.string.button_cancel_request);
        joinButton.setVisibility(VISIBLE);
        joinButton.setBackgroundColor(resources.getColor(android.R.color.transparent));
        joinButton.setTextColor(resources.getColor(R.color.orangered));
        joinButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                joinButton.setBackgroundColor(resources.getColor(R.color.orangered));
                joinButton.setTextColor(resources.getColor(R.color.white_text));
                status.setText(R.string.just_viewing);
                status.setCompoundDrawablesWithIntrinsicBounds(null,null,null,null);
                leaveEvent();
            }
        });

        fab.setVisibility(GONE);
        
    }

    void onApproved() {
        myStatus = JOINED;
        statusColor = resources.getColor(R.color.light_gray);
        icon = resources.getDrawable(R.drawable.ic_check_black_24dp);
        icon.setColorFilter(statusColor, PorterDuff.Mode.SRC_ATOP);

        status.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
        status.setText(R.string.status_participating);
        status.setTextColor(statusColor);

        joinButton.setText(R.string.button_leave_event);
        joinButton.setTextColor(resources.getColor(R.color.white_text));
        joinButton.setVisibility(VISIBLE);
        joinButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                leaveEvent();
            }
        });

        fab.setVisibility(VISIBLE);
    }


    void onBanned() {
        myStatus = BANNED;
        statusColor = resources.getColor(R.color.calm_red);
        icon = resources.getDrawable(R.drawable.ic_close_black_24px);
        icon.setColorFilter(statusColor, PorterDuff.Mode.SRC_ATOP);

        status.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
        status.setTextColor(statusColor);
        status.setText(R.string.status_banned);

        joinButton.setVisibility(GONE);

        fab.setVisibility(GONE);
    }

    private void leaveEvent(){
        LeaveRequestTask requestTask =
                new LeaveRequestTask<>(
                        model.getId(),
                        userModel.id,
                        new LeaveCallback(this));
        requestTask.execute();
    }

    private void removeFromParticipators(MiniUserModel user) {
        participators.remove(user);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onNewAdminSelectedDone(MiniUserModel newAdmin) {
        myStatus = JOINED;
        mainScrollView.smoothScrollTo(0,0);
        removeFromParticipators(newAdmin);
    }

    public void showConfirm() {
        joinButton.setVisibility(VISIBLE);
    }

    @Override
    public void openUserProfile(int id) {
        UserDataTask<Void> task = new UserDataTask<>(id);
        try {
            final FbGoogleUserModel selectedUser = task.execute().get();
            if (selectedUser != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent profileIntent = new Intent(self, ProfileActivity.class);
                        profileIntent.putExtra("user", new Gson().toJson(selectedUser));
                        profileIntent.putExtra("notMine", selectedUser.id != userModel.id);
                        startActivityForResult(profileIntent, OPEN_PROFILE);
                    }
                });
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent();
        intent.putExtra("status", myStatus.name());
        intent.putExtra("event", model.getId());
        setResult(RESULT_OK, intent);
    }
}
