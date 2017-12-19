package utils;

import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import models.UserEventModel;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Mark on 9.12.2017.
 */

public class BrowseUtil extends UtilBase {

    public interface EventExplorer {
        void DisplayNearbyEvents(List<UserEventModel> events);
        void DisplayNewEvents(List<UserEventModel> events);
        void DisplaySearchResults(List<UserEventModel> events);
    }

    public static class EventRetrievalTask<Void> extends AsyncTask<Void, Void, Void> {

        private int userId;
        private String searchText;
        private boolean areNew;
        private EventRetrievalCallback callback;

        public EventRetrievalTask(int userId, boolean areNew, EventRetrievalCallback callback) {
            this.userId = userId;
            this.areNew = areNew;
            this.callback = callback;
        }

        public EventRetrievalTask(int userId, String searchText, EventRetrievalCallback callback) {
            this.userId = userId;
            this.searchText = searchText;
            this.callback = callback;
        }

        @SafeVarargs
        @Override
        protected final Void doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();
            Request request;
            if(searchText != null) {
                request = new Request.Builder()
                        .url(API_PREFIX + "/user/" + userId + "/search?name=" + searchText)
                        .build();
            } else if (areNew) {
                request = new Request.Builder()
                        .url(API_PREFIX + "/user/" + userId + "/browse-new")
                        .build();

            } else {
                request = new Request.Builder()
                        .url(API_PREFIX + "/user/" + userId + "/browse")
                        .build();
            }
            Call call = client.newCall(request);
            call.enqueue(callback);
            return null;
        }
    }

    public static class EventRetrievalCallback implements Callback {

        private EventExplorer explorer;
        private EventsPurpose purpose;

        public EventRetrievalCallback(EventExplorer explorer, EventsPurpose purpose) {
            this.explorer = explorer;
            this.purpose = purpose;
        }

        @Override
        public void onFailure(Call call, IOException e) {

        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            List<UserEventModel> events = new ArrayList<>();
            try {
                String bodyString = response.body().string();

                if (bodyString.length() > 0 && bodyString.charAt(0) == '[') {

                    JSONArray resultArray = new JSONArray(bodyString);
                    for (int i = 0; i < resultArray.length(); i++) {

                        JSONObject object = resultArray.getJSONObject(i);

                        UserEventModel event = new UserEventModel();

                        event.setTopic(object.getString("chat_topic"));
                        event.setName(object.getString("trip_name"));
                        event.setLoc(object.getString("location"));
                        event.setDescription(object.getString("description"));
                        event.setId(object.getInt("id"));
                        event.setStartDate(object.getString("date_begin"));
                        event.setEndDate(object.getString("date_end"));
                        event.setSpots(object.getInt("spots"));
                        event.setCost(object.getInt("total_cost"));
                        event.setUserApproved(object.getInt("approved") == 1);
                        event.setApprovalPending(object.getInt("pending") == 1);
                        event.setUserBanned(object.getInt("banned") == 1);
                        event.setAdmin(object.getInt("is_admin") == 1);

                        String pictureString = object.getString("event_picture");
                        if (pictureString.matches("^http(s?)://.*")) {
                            event.setImageLink(pictureString);
                        } else {
                            event.setBitmap(pictureString);
                        }
                        events.add(event);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            switch (purpose) {

                case NEAR:
                    explorer.DisplayNearbyEvents(events);
                    break;
                case NEW:
                    explorer.DisplayNewEvents(events);
                    break;
                case SEARCH:
                    explorer.DisplaySearchResults(events);
                    break;
            }
        }
    }

    public static class SubscriptionTask<Void> extends AsyncTask<Void, Void, Void> {

        private int userId;

        public SubscriptionTask(int userId) {
            this.userId = userId;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(API_PREFIX + "/user/" + userId + "/topics")
                    .build();
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) { }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String body = response.body().string();
                    if (!body.isEmpty() && body.charAt(0) == '[') {
                        try {
                            JSONArray arr = new JSONArray(body);
                            FirebaseMessaging fm = FirebaseMessaging.getInstance();
                            for(int i = 0; i < arr.length(); i++) {
                                fm.subscribeToTopic(arr.getString(i));
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            return null;
        }
    }

    public enum EventsPurpose {
        NEAR,
        NEW,
        SEARCH
    }
}
