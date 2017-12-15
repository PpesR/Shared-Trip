package fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import adapters.FriendsEventsAdapter;
import models.UserEventModel;
import remm.sharedtrip.EventDetailsActivity;
import remm.sharedtrip.ExplorationActivity;
import remm.sharedtrip.MainActivity.FbGoogleUserModel;
import remm.sharedtrip.ProfileActivity;
import remm.sharedtrip.R;
import utils.FriendsUtil;
import utils.FriendsUtil.FriendEvent;
import utils.FriendsUtil.FriendsEventsCallback;
import utils.FriendsUtil.FriendsEventsTask;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static utils.UtilBase.API_PREFIX;
import static utils.UtilBase.isNull;
import static utils.UtilBase.valueOrNull;

public class FriendsFragment extends Fragment implements FriendsUtil.FriendsEventsReceiver, FriendsUtil.FriendEventListener {

    private static final int FRIENDS_EVENT = 564;
    private FriendsFragment self;

    private View myView;
    private ExplorationActivity myActivity;

    private FbGoogleUserModel loggedInUserModel;
    private FriendsEventsAdapter adapter;
    private GridLayoutManager layoutManager;

    private RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        self = this;

        myActivity = (ExplorationActivity) getActivity();
        loggedInUserModel = myActivity.getUserModel();
        myView = inflater.inflate(R.layout.fragment_friends, container, false);

        return myView;
    }

    @Override
    public void onStart() {
        recyclerView = myView.findViewById(R.id.friends_events_recycler);
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (loggedInUserModel.hasFacebook()) {
            myActivity.spinner.setVisibility(VISIBLE);
            requestFacebookFriendEvents();
        }
    }

    /**
     * Only initiates the request, gets nothing back.
     * The provide...() method is where we actually get things back.
     */
    private void requestFacebookFriendEvents() {
        FriendsEventsCallback callback =
                new FriendsEventsCallback(this);
        FriendsEventsTask<Void> task;
        task = new FriendsEventsTask<>(
                callback,
                API_PREFIX,
                new ArrayList<>(loggedInUserModel.facebookFriends));
        task.execute();
    }

    /**
     * Is called from util after the server responds with Friends' event data.
     * @param friendEvents - list of data models that are made based on server response
     */
    @Override
    public void provideFriendsEvents(final List<FriendEvent> friendEvents) {
        myActivity.runOnUiThread(new Runnable() {
            @Override
            public void run () {
                myActivity.spinner.setVisibility(GONE);
                if (!friendEvents.isEmpty()) {
                    if (isNull(adapter)) {
                        adapter = new FriendsEventsAdapter(myActivity, self, friendEvents);
                        layoutManager = new GridLayoutManager(myActivity, 2);
                        recyclerView.setLayoutManager(layoutManager);
                        recyclerView.setAdapter(adapter);

                    } else {
                        // Update the adapter if it already exists
                        adapter.friendEvents = friendEvents;
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    @Override
    public void onEventClicked(FriendEvent clickedEvent) {
        FriendsUtil.ExtraDetailsTask<Void> task = new FriendsUtil.ExtraDetailsTask<>(API_PREFIX, loggedInUserModel.id, clickedEvent.eventId);
        try {
            JSONObject result = task.execute().get();
            if (!result.has("error")) {
                UserEventModel model = new UserEventModel();
                model.setSpots(result.getInt("spots"));
                model.setCost(result.getInt("total_cost"));
                model.setDescription(valueOrNull(result.getString("description")));
                model.setStartDate(valueOrNull(result.getString("date_begin")));
                model.setEndDate(valueOrNull(result.getString("date_end")));
                model.setUserApproved(result.getInt("approved")==1);
                model.setUserBanned(result.getInt("banned")==1);
                model.setAdmin(result.getInt("is_admin")==1);
                model.setApprovalPending(result.getInt("pending")==1);
                model.setName(clickedEvent.eventName);
                model.setLoc(clickedEvent.location);
                model.setId(clickedEvent.eventId);
                model.setImageLink(clickedEvent.eventPictureUriString);

                Intent detailsIntent = new Intent(myActivity, EventDetailsActivity.class);
                detailsIntent.putExtra("prefix", API_PREFIX);
                detailsIntent.putExtra("user", myActivity.getSerializedLoggedInUserModel());
                detailsIntent.putExtra("event", new Gson().toJson(model));
                detailsIntent.putExtra("friend", true);
                myActivity.startActivityForResult(detailsIntent, FRIENDS_EVENT);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onErrorOccurred() {
        myActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                myActivity.spinner.setVisibility(GONE);
            }
        });
    }
}
