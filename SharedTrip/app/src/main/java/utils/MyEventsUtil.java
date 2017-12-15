package utils;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import adapters.JoinRequestsAdapter;
import adapters.MyEventsAdapter.MyEventsManager;
import models.MyEventModel;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static utils.UtilBase.API_PREFIX;
import static utils.UtilBase.isNull;
import static utils.UtilBase.valueOrNull;

/**
 * Created by Mark on 12.11.2017.
 */

public class MyEventsUtil {

    public static class MyEventsRetrievalTask<Void> extends AsyncTask<Void, Void, Void>{

        private int userId;
        private MyEventsCallback callback;

        public MyEventsRetrievalTask(int userId, MyEventsCallback callback) {
            this.userId = userId;
            this.callback = callback;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();
            final Request request = new Request.Builder()
                    .url(API_PREFIX + "/user/" + userId + "/my-events")
                    .build();
            client.newCall(request).enqueue(callback);
            return null;
        }
    }

    public static class MyEventsCallback implements Callback {

        private MyEventsManager manager;

        public MyEventsCallback(MyEventsManager manager) {
            this.manager = manager;
        }

        @Override
        public void onFailure(Call call, IOException e) {

        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            List<MyEventModel> models = new ArrayList<>();
            try {
                String bodyString = response.body().string();
                if (!bodyString.substring(0,1).equals("{")) { // otherwise it's an { "error": "blah blah" } type of JSON object
                    JSONArray jsonResults = new JSONArray(bodyString);

                    for (int i = 0; i < jsonResults.length(); i++) {
                        JSONObject obj = jsonResults.getJSONObject(i);
                        MyEventModel adminEventModel = new MyEventModel(
                                obj.getString("trip_name"),
                                obj.getString("event_picture").matches("^https?://.*") ? obj.getString("event_picture") : null,
                                obj.getString("location"));

                        adminEventModel.setId(obj.getInt("id"));
                        adminEventModel.setAdminId(obj.getInt("admin_id"));
                        adminEventModel.setSpots(obj.getInt("spots"));
                        adminEventModel.setCost(obj.getInt("total_cost"));
                        adminEventModel.setUsersPending(obj.getInt("users_pending"));

                        adminEventModel.setEndDate(obj.getString("date_end"));
                        adminEventModel.setStartDate(obj.getString("date_begin"));
                        adminEventModel.setDescription(obj.getString("description"));

                        adminEventModel.setAdmin(obj.getInt("is_admin") == 1);
                        adminEventModel.setApproved(obj.getInt("is_approved") == 1);
                        adminEventModel.setBanned(obj.getInt("is_banned") == 1);

                        if (isNull(adminEventModel.getImageLink()))
                            adminEventModel.setBitmap(obj.getString("event_picture"));

                        models.add(adminEventModel);
                    }
                }

            } catch (JSONException e) { e.printStackTrace();
            } catch (IOException e) { e.printStackTrace(); }

            manager.provideEvents(models);
        }
    }

    public static class PendingRequestsTask<Void> extends AsyncTask<Void, Void, List<JoinRequestsAdapter.RequestUserModel>> {

        private int eventId;
        private String apiPrefix;
        private MyEventsManager manager;

        public PendingRequestsTask(int eventId, String apiPrefix, MyEventsManager manager) {
            this.eventId = eventId;
            this.apiPrefix = apiPrefix;
            this.manager = manager;
        }

        @Override
        protected List<JoinRequestsAdapter.RequestUserModel> doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();
            final Request request = new Request.Builder()
                    .url(apiPrefix + "/admin/" + manager.getUserModel().id + "/pending?event=" + eventId)
                    .build();

            List<JoinRequestsAdapter.RequestUserModel> models = new ArrayList<>();
            try {
                Response response = client.newCall(request).execute();
                String bodyString = response.body().string();
                JSONArray array = new JSONArray(bodyString);
                if (bodyString.length() > 0 && bodyString.charAt(0) != '}') {

                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        JoinRequestsAdapter.RequestUserModel model = new JoinRequestsAdapter.RequestUserModel();
                        model.id = obj.getInt("id");
                        model.name = obj.getString("name");
                        model.firstName = valueOrNull(obj.getString("first_name"));
                        model.imageUriString = obj.getString("user_pic");
                        model.facebookId = valueOrNull(obj.getString("fb_id"));
                        model.googleId = valueOrNull(obj.getString("google_id"));
                        model.gender = valueOrNull(obj.getString("gender"));
                        model.description = valueOrNull(obj.getString("user_desc"));
                        model.birthDate = valueOrNull(obj.getString("birthdate"));
                        models.add(model);
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return models;
        }
    }

    public static class ApprovalTask<Void> extends AsyncTask<Void, Void, Boolean>{

        private int eventId;
        private int userId;
        private int adminId;
        String apiPerfix;

        public ApprovalTask(int eventId, int userId, int adminId, String apiPrefix) {
            this.eventId = eventId;
            this.userId = userId;
            this.adminId = adminId;
            this.apiPerfix = apiPrefix;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();

            FormBody.Builder formBuilder = null;
            formBuilder = new FormBody.Builder()
                    .add("event", eventId+"")
                    .add("user", userId+"");

            final Request request = new Request.Builder()
                    .url(apiPerfix+"/admin/"+adminId+"/approve")
                    .put(formBuilder.build())
                    .build();
            try {
                Response response = client.newCall(request).execute();
                String bodyString = response.body().string();
                if (bodyString.length()==0 || !new JSONObject(bodyString).has("error"))
                    return true;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    public static class DenialTask<Void> extends AsyncTask<Void, Void, Boolean>{

        private int eventId;
        private int userId;
        private int adminId;
        String apiPerfix;

        public DenialTask(int eventId, int userId, int adminId, String apiPrefix) {
            this.eventId = eventId;
            this.userId = userId;
            this.adminId = adminId;
            this.apiPerfix = apiPrefix;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();

            FormBody.Builder formBuilder = null;
            formBuilder = new FormBody.Builder()
                    .add("event", eventId+"")
                    .add("user", userId+"");

            final Request request = new Request.Builder()
                    .url(apiPerfix+"/admin/"+adminId+"/reject")
                    .put(formBuilder.build())
                    .build();
            try {
                Response response = client.newCall(request).execute();
                String bodyString = response.body().string();
                if (bodyString.length()==0 || !new JSONObject(bodyString).has("error"))
                    return true;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return false;
        }
    }

}
