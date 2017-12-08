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
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static utils.DebugUtil.doNothing;
import static utils.EventDetailsUtils.bitmapFromString;

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

    // The activity that uses friends' events must implement this interface
    public interface FriendsEventsReceiver {
        void provideFriendsEvents(List<FriendEvent> friendEvents);
    }

    // The data model we'll be using in our Activity and Adapter
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

        private FriendsEventsReceiver receiverActivity;

        // When you instantiate this callback in activity, pass activity itself aka "this"
        public FriendsEventsCallback(FriendsEventsReceiver receiverActivity) {
            this.receiverActivity = receiverActivity;
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

                // Need to populate this ArrayList<FriendEvent> from allFriendsEvents above
                List<FriendEvent> output = new ArrayList<>();

                if (allFriendsEvents.length() > 0) {

                    // instead of index 0, use for-loop
                    JSONObject firstFriendEvent = allFriendsEvents.getJSONObject(0);
                    int example1 = firstFriendEvent.getInt("user_id");
                    String example2 = firstFriendEvent.getString("event_name");

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

                   String pictureString = firstFriendEvent.getString("event_picture");

                   Bitmap example3 = bitmapFromString(pictureString); // My own helper method to decode both URIs and base64's

                   Uri example4 = Uri.parse(firstFriendEvent.getString("user_picture"));

                   doNothing(); // use as a breakpoint to see what the examples or actual response values are
                }

                // After everything is done, pass the output (might be empty!) back to activity:
                receiverActivity.provideFriendsEvents(output);

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
