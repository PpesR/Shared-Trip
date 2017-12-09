package fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import adapters.FriendsEventsAdapter;
import remm.sharedtrip.ExplorationActivity;
import remm.sharedtrip.MainActivity.FbGoogleUserModel;
import remm.sharedtrip.R;
import utils.FriendsUtil;

import static utils.DebugUtil.doNothing;
import static utils.ValueUtil.isNull;

public class FriendsFragment extends Fragment implements FriendsUtil.FriendsEventsReceiver, FriendsUtil.FriendEventListener {

    private String apiPrefix;
    private FriendsFragment self;

    private View myView;
    private ExplorationActivity myActivity;

    private FbGoogleUserModel loggedInUserModel;
    private FriendsEventsAdapter adapter;

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
        super.onStart();

        // TODO: Uncomment when done with the rest. Try moving to onCreateView() if this doesn't work.
        //requestFriendEventsFromDb()
    }

    /**
     * Only initiates the request, gets nothing back.
     * The provide...() method is where we actually get things back.
     */
    private void requestFriendEventsFromDb() {
        FriendsUtil.FriendsEventsCallback callback = new FriendsUtil.FriendsEventsCallback(this);
        FriendsUtil.FriendsEventsTask<Void> task = new FriendsUtil.FriendsEventsTask<>(callback, apiPrefix, new ArrayList<>(loggedInUserModel.facebookFriends));
        task.execute();
    }

    /**
     * Is called from util after the server responds with Friends' event data.
     * @param friendEvents - list of data models that are made based on server response
     */
    @Override
    public void provideFriendsEvents(List<FriendsUtil.FriendEvent> friendEvents) {

        doNothing(); // use as a breakpoint for testing

        if (isNull(adapter)) {
            adapter = new FriendsEventsAdapter(myActivity, this, friendEvents);
            // TODO: attach the adapter to our RecyclerView. See AdminEventFragment for reference.
        }

        else {
            // TODO: update the adapter if it already exists
        }
    }

    @Override
    public void onEventClicked(FriendsUtil.FriendEvent clickedEvent) {
        // TODO: redirect to the event's detail view
        // Don't forget to include clicked event's complete data (cost, description, etc)!
        // Might need to query separately using eventId value and methods in EventUtils.
        // See BrowseEvents activity for "open details" workflow. Use gson to serialize data.
    }
}
