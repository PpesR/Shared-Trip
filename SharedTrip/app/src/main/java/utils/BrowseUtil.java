package utils;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import models.UserEventModel;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Mark on 9.12.2017.
 */

public class BrowseUtil {

    public static class EventRetrievalTask<Void> extends AsyncTask<Void, Void, List<UserEventModel>> {

        private int userId;
        private String apiPrefix;

        public EventRetrievalTask(int userId, String apiPrefix) {
            this.userId = userId;
            this.apiPrefix = apiPrefix;
        }

        @SafeVarargs
        @Override
        protected final List<UserEventModel> doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(apiPrefix+"/user/"+userId+"/browse")
                    .build();
            List<UserEventModel> events = new ArrayList<>();
            try {
                Response response = client.newCall(request).execute();
                String bodyString = response.body().string();

                if (bodyString.length() > 0) {

                    JSONArray resultArray = new JSONArray(bodyString);
                    for (int i = 0; i < resultArray.length(); i++) {

                        JSONObject object = resultArray.getJSONObject(i);

                        UserEventModel event = new UserEventModel();

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
            return events;
        }
    }
}
