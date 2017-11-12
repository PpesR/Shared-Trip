package remm.sharedtrip;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import models.UserEventModel;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Mark on 12.11.2017.
 */

public class EventDetailsUtils {

    static class JoinRequestTask<Void> extends AsyncTask<Void, Void, Void> {
        private int eventId;
        private int participatorId;
        private JoinCallback callback;

        JoinRequestTask(int eventId, int participatorId, JoinCallback callback) {
            this.eventId = eventId;
            this.participatorId = participatorId;
            this.callback = callback;
        }

        @SafeVarargs
        @Override
        protected final Void doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();

            FormBody.Builder formBuilder = null;
            formBuilder = new FormBody.Builder()
                    .add("hdl", "event")
                    .add("act","join")
                    .add("event", eventId+"")
                    .add("participator", participatorId +"");

            final Request request = new Request.Builder()
                    .url("http://146.185.135.219/requestrouter.php")
                    .post(formBuilder.build())
                    .build();
            Call call = client.newCall(request);
            call.enqueue(callback);
            return null;
        }
    }

    static class JoinCallback implements Callback {

        private EventDetailsActivity eda;
        JoinCallback(EventDetailsActivity eda) { this.eda = eda; }
        @Override
        public void onFailure(Call call, IOException e) { }
        @Override
        public void onResponse(Call call, Response response) {
            try {
                JSONArray array = new JSONArray(response.body().string());
                if(array.getString(0).equals("SUCCESS")) {
                    eda.onJoinSuccess();
                }

            } catch (JSONException e) { e.printStackTrace();
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    static class ApprovalStatusTask<Void> extends AsyncTask<Void, Void, Void> {
        private int eventId;
        private int participatorId;
        private ApprovalCallback callback;

        ApprovalStatusTask(int eventId, int participatorId, ApprovalCallback callback) {
            this.eventId = eventId;
            this.participatorId = participatorId;
            this.callback = callback;
        }

        @SafeVarargs
        @Override
        protected final Void doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();

            final Request request = new Request.Builder()
                    .url("http://146.185.135.219/requestrouter.php?hdl=event&act=apst&event="+eventId+"&user="+participatorId)
                    .build();
            Call call = client.newCall(request);
            call.enqueue(callback);
            return null;
        }
    }

    static class ApprovalCallback implements Callback {

        private EventDetailsActivity eda;
        private UserEventModel model;
        ApprovalCallback(EventDetailsActivity eda, UserEventModel model) { this.eda = eda; this.model=model; }
        @Override
        public void onFailure(Call call, IOException e) { }
        @Override
        public void onResponse(Call call, Response response) {
            try {
                JSONArray array = new JSONArray(response.body().string());
                if(array.getString(0).equals("SUCCESS")) {
                    JSONArray actualResult = array.getJSONArray(2);
                    if(actualResult.length()==1) {
                        JSONObject obj = actualResult.getJSONObject(0);
                        if (obj.getInt("is_admin")==1)
                            model.setAdmin(true);
                        else if (obj.getInt("approved")==1)
                            model.setUserApproved(true);
                        else if (obj.getInt("banned")==1) {
                            model.setUserBanned(true);
                        }
                    }
                    eda.onApprovalStatusReady();
                }

            } catch (JSONException e) { e.printStackTrace();
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

}
