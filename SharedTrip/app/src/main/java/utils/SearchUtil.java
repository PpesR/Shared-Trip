package utils;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import models.MyEventModel;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static utils.UtilBase.API_PREFIX;
import static utils.UtilBase.isNull;

/**
 * Created by Mark on 12.11.2017.
 */

public class SearchUtil {
    public static class EventRetrievalTask<Void> extends AsyncTask<Void, Void, List<MyEventModel>> {

        private final int userId;
        private final String filter;

        public EventRetrievalTask(int userId, String filter) {
            this.userId = userId;
            this.filter = filter;
        }

        @Override
        protected List<MyEventModel> doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();
            final Request request = new Request.Builder()
                    .url(API_PREFIX + "/user/" + userId + "/search?name=" + filter)
                    .build();

            List<MyEventModel> models = new ArrayList<>();
            try {
                Response response = client.newCall(request).execute();
                String rstring = response.body().string();
                JSONArray array = new JSONArray(rstring);

                if (array.getString(0).equals("SUCCESS")) {
                    JSONArray actualResults = array.getJSONArray(2);
                    for (int i = 0; i < actualResults.length(); i++) {
                        JSONObject obj = actualResults.getJSONObject(i);
                        MyEventModel adminEventModel = new MyEventModel(
                                obj.getString("trip_name"),
                                obj.getString("event_picture").contains("http") ? obj.getString("event_picture") : null,
                                obj.getString("location"));

                        adminEventModel.setId(obj.getInt("id"));
                        adminEventModel.setStartDate(obj.getString("date_begin"));
                        adminEventModel.setAdminId(obj.getInt("admin_id"));
                        adminEventModel.setUsersPending(obj.getInt("users_pending"));

                        if (isNull(adminEventModel.getImageLink()))
                            adminEventModel.setBitmap(obj.getString("event_picture"));

                        models.add(adminEventModel);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return models;
        }
    }

}