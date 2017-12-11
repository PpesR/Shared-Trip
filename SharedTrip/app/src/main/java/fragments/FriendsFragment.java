package fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import adapters.FriendsEventsAdapter;
import models.UserEventModel;
import remm.sharedtrip.ExplorationActivity;
import remm.sharedtrip.MainActivity.FbGoogleUserModel;
import remm.sharedtrip.R;
import utils.FriendsUtil;
import utils.FriendsUtil.FriendsEventsCallback;
import utils.FriendsUtil.FriendsEventsTask;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static utils.ValueUtil.isNull;

public class FriendsFragment extends Fragment implements FriendsUtil.FriendsEventsReceiver, FriendsUtil.FriendEventListener {

    private String apiPrefix;
    private FriendsFragment self;

    private View myView;
    private ExplorationActivity myActivity;

    private FbGoogleUserModel loggedInUserModel;
    private FriendsEventsAdapter adapter;
    private GridLayoutManager layoutManager;

    private RecyclerView recyclerView;
    private ProgressBar spinner;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        self = this;

        myActivity = (ExplorationActivity) getActivity();
        loggedInUserModel = myActivity.getUserModel();
        apiPrefix = myActivity.getApiPrefix();
        myView = inflater.inflate(R.layout.fragment_friends, container, false);

        return myView;
    }

    @Override
    public void onStart() {
        recyclerView = myView.findViewById(R.id.friends_events_recycler);
        spinner = myView.findViewById(R.id.friends_events_spinner);
        super.onStart();
        if (loggedInUserModel.hasFacebook()) {
            spinner.setVisibility(VISIBLE);
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
                apiPrefix,
                new ArrayList<>(loggedInUserModel.facebookFriends));
        task.execute();
    }

    /**
     * Is called from util after the server responds with Friends' event data.
     * @param friendEvents - list of data models that are made based on server response
     */
    @Override
    public void provideFriendsEvents(final List<FriendsUtil.FriendEvent> friendEvents) {
        myActivity.runOnUiThread(new Runnable() {
            @Override
            public void run () {
                spinner.setVisibility(GONE);
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
    public void onEventClicked(FriendsUtil.FriendEvent clickedEvent) {
        // TODO: redirect to the event's detail view
        // Don't forget to include clicked event's complete data (cost, description, etc)!
        // Might need to query separately using eventId value and methods in EventUtils.
        // See BrowseEvents activity for "open details" workflow. Use gson to serialize data.
        FriendsUtil.ExtraDetailsTask<Void> task = new FriendsUtil.ExtraDetailsTask<>(apiPrefix, loggedInUserModel.id, clickedEvent.eventId);
        try {
            JSONObject result = task.execute().get();
            if (!result.has("error")) {
                UserEventModel model = new UserEventModel();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onErrorOccurred() {
        myActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                spinner.setVisibility(GONE);
            }
        });
    }
}
