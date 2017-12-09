package remm.sharedtrip;

import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import adapters.FriendsEventsAdapter;

import static remm.sharedtrip.MainActivity.*;
import static utils.DebugUtil.doNothing;
import static utils.FriendsUtil.*;
import static utils.ValueUtil.isNull;

public class FriendsActivity extends AppCompatActivity implements FriendsEventsReceiver, FriendEventListener {

    private Gson gson;
    private String apiPrefix;
    private FriendsActivity self;

    private BottomNavigationView bottomNavigationView;
    private FbGoogleUserModel loggedInUserModel;
    private FriendsEventsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        self = this;

        gson = new Gson();
        loggedInUserModel = gson.fromJson(getIntent().getStringExtra("user"), FbGoogleUserModel.class);
        apiPrefix = getIntent().getStringExtra("prefix");

        setContentView(R.layout.activity_friends_view);

        // data exchange starts here. Read method descriptions for more info!
        requestFriendEventsFromDb();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MenuItem myButton = bottomNavigationView.getMenu()
                .findItem(R.id.bottombaritem_friends);
        myButton.setChecked(true);
    }

    /**
     * Only initiates the request, gets nothing back.
     * The provide...() method is where we actually get things back.
     */
    private void requestFriendEventsFromDb() {
        FriendsEventsCallback callback = new FriendsEventsCallback(this);
        FriendsEventsTask<Void> task = new FriendsEventsTask<>(callback, apiPrefix, new ArrayList<>(loggedInUserModel.facebookFriends));
        task.execute();
    }

    /**
     * Is called from util after the server responds with Friends' event data.
     * @param friendEvents - list of data models that are made based on server response
     */
    @Override
    public void provideFriendsEvents(List<FriendEvent> friendEvents) {

        doNothing(); // use as a breakpoint for testing

        if (isNull(adapter)) {
            adapter = new FriendsEventsAdapter(this, this, friendEvents);
            // TODO: attach the adapter to our RecyclerView. See AdminEventActivity for reference.
        }

        else {
            // TODO: update the adapter if it already exists
        }
    }

    @Override
    public void onEventClicked(FriendEvent clickedEvent) {
        // TODO: redirect to the event's detail view
        // Don't forget to include clicked event's complete data (cost, description, etc)!
        // Might need to query separately using eventId value and methods in EventUtils.
        // See BrowseEvents activity for "open details" workflow. Use gson to serialize data.
    }
}
