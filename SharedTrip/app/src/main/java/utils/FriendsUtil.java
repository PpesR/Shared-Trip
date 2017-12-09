package utils;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static utils.DebugUtil.doNothing;
import static utils.EventDetailsUtil.bitmapFromString;

/**
 * Deals with getting user's friends' events from the server.
 * Created by Mark on 7.12.2017.
 */

public class FriendsUtil {

    public static class FriendsEventsTask<Void> extends AsyncTask<Void, Void, Void> {
        private FriendsEventsCallback callback;
        private String apiPrefix;
        private List<String> friendsIds;

        public FriendsEventsTask(FriendsEventsCallback callback, String apiPrefix, List<String> friendsIds) {
            this.callback = callback;
            this.apiPrefix = apiPrefix;
            this.friendsIds = friendsIds;
        }

        @SafeVarargs
        @Override
        protected final Void doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();

            String idsJSON = new JSONArray(friendsIds).toString();
            String encodedArray = Base64.encodeToString(idsJSON.getBytes(), Base64.DEFAULT);

            Request request = new Request.Builder()
                    .url(apiPrefix+"/user/friend-events?data="+encodedArray+"&max=20&after=2")
                    .build();

            Call call = client.newCall(request);
            call.enqueue(callback);
            return null;
        }
    }

    // The fragment that uses friends' events must implement this interface
    public interface FriendsEventsReceiver {
        void provideFriendsEvents(List<FriendEvent> friendEvents);
    }

    // The data model we'll be using in our Fragment and Adapter
    public static class FriendEvent {

        public int eventId;
        public String eventName;
        public String location;
        public Bitmap eventPicture; // is tricky

        public int friendId;
        public String friendFacebookId;
        public String friendFullName;
        public String friendFirstName;
        public Uri friendProfilePicture;
    }

    // A Callback is something that's supposed to react after we get a response from the server
    public static class FriendsEventsCallback implements Callback {

        private FriendsEventsReceiver receiverFragment;

        // When you instantiate this callback in fragment, pass fragment itself aka "this" as argument
        public FriendsEventsCallback(FriendsEventsReceiver receiverFragment) {
            this.receiverFragment = receiverFragment;
        }

        @Override
        public void onFailure(Call call, IOException e) {
            // should show an error or sth
        }

        @Override
        public void onResponse(Call call, Response response) {
            try {
                String responseBodyString = response.body().string();
                JSONArray allFriendsEvents = new JSONArray(responseBodyString);

                // TODO: Need to populate this list with FriendEvents. Create them from allFriendsEvents' data below
                List<FriendEvent> output = new ArrayList<>();

                if (allFriendsEvents.length() > 0) { // this is important, keep it around everything you do.

                    // TODO: instead of index 0, use for-loop to get all FriendsEvents
                    JSONObject firstRawEventData = allFriendsEvents.getJSONObject(0);

                    // TODO: see some examples on how to get the data
                    int exampleId = firstRawEventData.getInt("user_id");
                    String exampleName = firstRawEventData.getString("event_name");

                   /*
                    *  allFriendsEvents is an array of JSONObjects like this:
                    *
                    *  {
                    *      "fb_id": string,
                           "user_id": string (must be cast to int for actual usage),
                           "full_name": string,
                           "first_name": string or null,
                           "user_picture": string (always a URI),
                           "event_id": string (must be cast to int for actual usage),
                           "event_name": string,
                           "location": string,
                           "event_picture": string - a URI if starts with "http" | otherwise base64 encoded file and need to decode
                    *   }
                    */

                   // Using my own helper method to decode both URIs and base64's when applicable, so you don't need to worry about it
                    String pictureString = firstRawEventData.getString("event_picture");
                    Bitmap exampleEventPicture = bitmapFromString(pictureString);

                    // This one is always a URI, so we use built-in methods
                    Uri exampleProfilePicture = Uri.parse(firstRawEventData.getString("user_picture"));

                   // TODO: make a model for every for-loop iteration, set all its values (ids, names...), and then add it to list.
                    FriendEvent exampleEventModel = new FriendEvent();
                    output.add(exampleEventModel);

                    doNothing(); // use as a breakpoint to see what the examples or actual response values are
                }

                // TODO: After everything is done, pass the output (might be empty and it's OK!) back to fragment:
                receiverFragment.provideFriendsEvents(output);

            } catch (JSONException e) {
                e.printStackTrace(); // should show an error or sth
            } catch (IOException e) {
                e.printStackTrace(); //should show an error or sth
            }
        }
    }

    public interface FriendEventListener {
        void onEventClicked(FriendEvent clickedEvent);
    }
}
