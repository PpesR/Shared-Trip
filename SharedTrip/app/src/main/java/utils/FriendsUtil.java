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

import static utils.EventDetailsUtil.bitmapFromString;
import static utils.UtilBase.valueOrNull;

/**
 * Deals with getting user's friends' events from the server.
 * Created by Mark on 7.12.2017.
 */

public class FriendsUtil extends UtilBase {

    public static class FriendsEventsTask<Void> extends AsyncTask<Void, Void, Void> {
        private FriendsEventsCallback callback;
        private List<String> friendsIds;

        public FriendsEventsTask(FriendsEventsCallback callback, List<String> friendsIds) {
            this.callback = callback;
            this.friendsIds = friendsIds;
        }

        @SafeVarargs
        @Override
        protected final Void doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();

            String idsJSON = new JSONArray(friendsIds).toString();
            String encodedArray = Base64.encodeToString(idsJSON.getBytes(), Base64.DEFAULT);

            Request request = new Request.Builder()
                    .url(API_PREFIX+"/user/friend-events?data="+encodedArray+"&max=10")
                    .build();

            Call call = client.newCall(request);
            call.enqueue(callback);
            return null;
        }
    }

    // The fragment that uses friends' events must implement this interface
    public interface FriendsEventsReceiver {
        void onErrorOccurred();
        void provideFriendsEvents(List<FriendEvent> friendEvents);
    }

    // The data model we'll be using in our Fragment and Adapter
    public static class FriendEvent {

        public int eventId;
        public String eventName;
        public String location;
        public Bitmap eventPicture;

        public int friendId;
        public String friendFacebookId;
        public String friendFullName;
        public String friendFirstName;
        public Uri friendProfilePicture;
        public String eventPictureUriString;
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
            receiverFragment.onErrorOccurred();// should show an error or sth
        }

        @Override
        public void onResponse(Call call, Response response) {
            try {
                String responseBodyString = response.body().string();
                JSONArray allFriendsEvents = new JSONArray(responseBodyString);

                List<FriendEvent> output = new ArrayList<>();

                if (allFriendsEvents.length() > 0) { // this is important, keep it around everything you do.

                    for(int i = 0; i < allFriendsEvents.length(); i++) {
                        JSONObject rawEventData = allFriendsEvents.getJSONObject(i);
                        FriendEvent model = new FriendEvent();

                        model.friendId = rawEventData.getInt("user_id");
                        model.friendFullName = rawEventData.getString("full_name");
                        model.friendFirstName = valueOrNull(rawEventData.getString("first_name"));
                        model.friendFacebookId = valueOrNull(rawEventData.getString("fb_id"));

                        // This one is always a URI, so we use built-in methods
                        model.friendProfilePicture = Uri.parse(rawEventData.getString("user_picture"));

                        model.eventId = rawEventData.getInt("event_id");
                        model.eventName = rawEventData.getString("event_name");
                        model.location = rawEventData.getString("location");

                        // Using my own helper method to decode both URIs and base64's when applicable,
                        // so you don't need to worry about it
                        String pictureString = rawEventData.getString("event_picture");
                        model.eventPicture = bitmapFromString(pictureString);
                        if (pictureString.matches("^https?://.*"))
                            model.eventPictureUriString = pictureString;
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
                        output.add(model);
                    }
                }

                // After everything is done, pass the output (might be empty and it's OK!) back to fragment:
                receiverFragment.provideFriendsEvents(output);

            } catch (JSONException e) {
                receiverFragment.onErrorOccurred();
                e.printStackTrace(); // should show an error or sth
            } catch (IOException e) {
                receiverFragment.onErrorOccurred();
                e.printStackTrace(); //should show an error or sth
            }
        }
    }

    public static class ExtraDetailsTask<Void> extends AsyncTask<Void, Void, JSONObject> {
        private int userId;
        private int eventId;

        public ExtraDetailsTask(int userId, int eventId) {
            this.userId = userId;
            this.eventId = eventId;
        }

        @Override
        protected JSONObject doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(API_PREFIX+"/user/"+userId+"/event/"+eventId+"/extra-details").build();
            try {
                Response response = client.newCall(request).execute();
                String bodyString = response.body().string();
                if (bodyString.length() > 0)
                    return new JSONObject(bodyString);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public interface FriendEventListener {
        void onEventClicked(FriendEvent clickedEvent);
        void onErrorOccurred();
    }
}
