package utils;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import models.AdminEventModel;
import models.EventModel;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import remm.sharedtrip.AdminEventActivity;

/**
 * Created by Mark on 12.11.2017.
 */

public class AdminEventUtils {

    public static class AdminEventRetrievalTask<Void> extends AsyncTask<Void, Void, Void>{

        private int adminId;

        public AdminEventRetrievalTask(int adminId, AndminEventRetrievalCallback callback) {
            this.adminId = adminId;
            this.callback = callback;
        }

        private AndminEventRetrievalCallback callback;

        @Override
        protected Void doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();
            final Request request = new Request.Builder()
                    .url("http://146.185.135.219/requestrouter.php?hdl=admin&user="+adminId)
                    .build();
            Call call = client.newCall(request);
            call.enqueue(callback);
            return null;
        }
    }

    public static class AndminEventRetrievalCallback implements Callback {

        private AdminEventActivity aea;

        public AndminEventRetrievalCallback(AdminEventActivity aea) {
            this.aea = aea;
        }

        @Override
        public void onFailure(Call call, IOException e) { }

        @Override
        public void onResponse(Call call, Response response)  {
            try {
                JSONArray array = new JSONArray(response.body().string());
                List<AdminEventModel> models = new ArrayList<>();
                if(array.getString(0).equals("SUCCESS")) {
                    JSONArray actualResults = array.getJSONArray(2);
                    for (int i = 0; i<actualResults.length(); i++) {
                        JSONObject obj = actualResults.getJSONObject(i);
                        AdminEventModel adminEventModel = new AdminEventModel(
                                obj.getString("trip_name"),
                                obj.getString("event_picture"),
                                obj.getString("location"));

                        adminEventModel.setId(obj.getInt("id"));
                        adminEventModel.setStartDate(obj.getString("date_begin"));
                        adminEventModel.setAdminId(obj.getInt("admin_id"));
                        adminEventModel.setUsersPending(obj.getInt("users_pending"));
                        models.add(adminEventModel);
                    }
                    aea.provideEvents(models);
                }

            } catch (JSONException e) { e.printStackTrace();
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

}
