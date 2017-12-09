package utils;

import android.os.AsyncTask;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import adapters.MyEventsAdapter.MyEventsManager;
import models.MyEventModel;
import models.ParticipatorModel;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static utils.ValueUtil.isNull;

/**
 * Created by Mark on 12.11.2017.
 */

public class MyEventsUtil {

    public static class MyEventsRetrievalTask<Void> extends AsyncTask<Void, Void, List<MyEventModel>>{

        private int adminId;
        public MyEventsRetrievalTask(int adminId) {
            this.adminId = adminId;
        }

        @Override
        protected List<MyEventModel> doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();
            final Request request = new Request.Builder()
                    .url("http://146.185.135.219/requestrouter.php?hdl=admin&user="+adminId)
                    .build();

            List<MyEventModel> models = new ArrayList<>();
            try {
                Response response = client.newCall(request).execute();
                String rstring = response.body().string();
                JSONArray array = new JSONArray(rstring);

                if(array.getString(0).equals("SUCCESS")) {
                    JSONArray actualResults = array.getJSONArray(2);
                    for (int i = 0; i<actualResults.length(); i++) {
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

            } catch (JSONException e) { e.printStackTrace();
            } catch (IOException e) { e.printStackTrace(); }
            return models;
        }
    }

    public static class ParticipatorRetrievalTask<Void> extends AsyncTask<Void, Void, Void>{

        private int eventId;
        private ParticipatorRetrievalCallback callback;

        public ParticipatorRetrievalTask(int eventId, ParticipatorRetrievalCallback callback) {
            this.eventId = eventId;
            this.callback = callback;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();
            final Request request = new Request.Builder()
                    .url("http://146.185.135.219/requestrouter.php?hdl=admin&act=pnd&event="+eventId)
                    .build();
            Call call = client.newCall(request);
            call.enqueue(callback);
            return null;
        }
    }

    public static class ParticipatorRetrievalCallback implements Callback {

        private MyEventsManager manager;
        private MyEventModel model;
        private TextView badge;

        public ParticipatorRetrievalCallback(MyEventsManager manager, MyEventModel model, TextView badge) {
            this.manager = manager;
            this.model = model;
            this.badge = badge;
        }

        @Override
        public void onFailure(Call call, IOException e) { }

        @Override
        public void onResponse(Call call, Response response)  {
            try {
                JSONArray array = new JSONArray(response.body().string());
                if(array.getString(0).equals("SUCCESS")) {
                    List<ParticipatorModel> models = new ArrayList<>();
                    JSONArray actualResults = array.getJSONArray(2);

                    for (int i = 0; i<actualResults.length(); i++) {
                        JSONObject obj = actualResults.getJSONObject(i);
                        ParticipatorModel model = new ParticipatorModel();
                        model.setFbId(obj.getString("fb_id"));
                        model.setId(obj.getInt("id"));
                        model.setName(obj.getString("name"));
                        model.setImageUri(obj.getString("user_pic"));
                        models.add(model);
                    }
                    manager.provideParticipators(models, model, badge);
                }

            } catch (JSONException e) { e.printStackTrace();
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    public static class ApprovalTask<Void> extends AsyncTask<Void, Void, Boolean>{

        private int eventId;
        private int userId;

        public ApprovalTask(int eventId, int userId) {
            this.eventId = eventId;
            this.userId = userId;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();

            FormBody.Builder formBuilder = null;
            formBuilder = new FormBody.Builder()
                    .add("hdl", "admin")
                    .add("act","apr")
                    .add("event", eventId+"")
                    .add("participator", userId+"");

            final Request request = new Request.Builder()
                    .url("http://146.185.135.219/requestrouter.php")
                    .post(formBuilder.build())
                    .build();
            try {
                Response response = client.newCall(request).execute();
                JSONArray array = new JSONArray(response.body().string());
                if(array.getString(0).equals("SUCCESS"))
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

        public DenialTask(int eventId, int userId) {
            this.eventId = eventId;
            this.userId = userId;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();

            FormBody.Builder formBuilder = null;
            formBuilder = new FormBody.Builder()
                    .add("hdl", "admin")
                    .add("act","rej")
                    .add("event", eventId+"")
                    .add("participator", userId+"");

            final Request request = new Request.Builder()
                    .url("http://146.185.135.219/requestrouter.php")
                    .post(formBuilder.build())
                    .build();
            try {
                Response response = client.newCall(request).execute();
                JSONArray array = new JSONArray(response.body().string());
                if(array.getString(0).equals("SUCCESS"))
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
