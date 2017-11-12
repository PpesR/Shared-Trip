package utils;

import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import models.AdminEventModel;
import models.ParticipatorModel;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
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

    public static class ParticipatorRetrievalCallback implements Callback {

        private AdminEventActivity aea;
        private AdminEventModel model;
        private TextView badge;

        public ParticipatorRetrievalCallback(AdminEventActivity aea, AdminEventModel model, TextView badge) {
            this.aea = aea;
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
                    aea.provideParticipators(models, model, badge);
                }

            } catch (JSONException e) { e.printStackTrace();
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    public static class ApprovalTask<Void> extends AsyncTask<Void, Void, Void>{

        private int eventId;
        private int userId;
        private ApprovalCallback callback;

        public ApprovalTask(int eventId, int userId, ApprovalCallback callback) {
            this.eventId = eventId;
            this.userId = userId;
            this.callback = callback;
        }

        @Override
        protected Void doInBackground(Void... voids) {
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
            Call call = client.newCall(request);
            call.enqueue(callback);
            return null;
        }
    }

    public static class ApprovalCallback implements Callback {

        private AdminEventActivity aea;
        private int orderNr;
        private TextView badge;
        AdminEventModel eventModel;

        public ApprovalCallback(AdminEventActivity aea, int orderNr, TextView badge, AdminEventModel eventModel) {
            this.aea = aea;
            this.orderNr = orderNr;
            this.badge = badge;
            this.eventModel = eventModel;
        }

        @Override
        public void onFailure(Call call, IOException e) { }

        @Override
        public void onResponse(Call call, Response response)  {
            try {
                JSONArray array = new JSONArray(response.body().string());
                if(array.getString(0).equals("SUCCESS")) {
                    aea.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            aea.subAdapter.participators.remove(orderNr);
                            aea.subAdapter.notifyDataSetChanged();
                            badge.setText(eventModel.getUsersPending()+"");
                            if (eventModel.getUsersPending()==0) {
                                badge.setVisibility(View.GONE);
                            }
                        }
                    });
                }

            } catch (JSONException e) { e.printStackTrace();
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    public static class DenialTask<Void> extends AsyncTask<Void, Void, Void>{

        private int eventId;
        private int userId;
        private DenialCallback callback;

        public DenialTask(int eventId, int userId, DenialCallback callback) {
            this.eventId = eventId;
            this.userId = userId;
            this.callback = callback;
        }

        @Override
        protected Void doInBackground(Void... voids) {
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
            Call call = client.newCall(request);
            call.enqueue(callback);
            return null;
        }
    }

    public static class DenialCallback implements Callback {

        private AdminEventActivity aea;
        private int orderNr;
        private TextView badge;
        AdminEventModel eventModel;

        public DenialCallback(AdminEventActivity aea, int orderNr, TextView badge, AdminEventModel eventModel) {
            this.aea = aea;
            this.orderNr = orderNr;
            this.badge = badge;
            this.eventModel = eventModel;
        }

        @Override
        public void onFailure(Call call, IOException e) { }

        @Override
        public void onResponse(Call call, Response response)  {
            try {
                JSONArray array = new JSONArray(response.body().string());
                if(array.getString(0).equals("SUCCESS")) {
                    aea.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            aea.subAdapter.participators.remove(orderNr);
                            aea.subAdapter.notifyDataSetChanged();
                            badge.setText(eventModel.getUsersPending()+"");
                            if (eventModel.getUsersPending()==0) {
                                badge.setVisibility(View.GONE);
                            }
                        }
                    });
                }

            } catch (JSONException e) { e.printStackTrace();
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

}
