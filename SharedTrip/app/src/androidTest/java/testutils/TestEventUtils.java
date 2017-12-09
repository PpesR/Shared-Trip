package testutils;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Mark on 13.11.2017.
 */

public class TestEventUtils {

    public JSONArray DeleteEventFromDb(int eventId) {
        EventRemovalTask<Void> task = new EventRemovalTask<Void>(eventId);
        try {
            return task.execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONArray RemoveEventParticipation(int eventId, int userId) {
        ParticipationRemovalTask<Void> task = new ParticipationRemovalTask<Void>(eventId, userId);
        try {
            return task.execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }


    private static class EventRemovalTask<Void> extends AsyncTask<Void, Void, JSONArray> {

        private int eventId;
        public EventRemovalTask(int eventId) { this.eventId = eventId; }

        @Override
        protected JSONArray doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();

            FormBody.Builder formBuilder = null;
            formBuilder = new FormBody.Builder()
                    .add("hdl", "event")
                    .add("act","testdel")
                    .add("event", eventId+"");

            final Request request = new Request.Builder()
                    .url("http://146.185.135.219/requestrouter.php")
                    .post(formBuilder.build())
                    .build();
            Call call = client.newCall(request);
            try {
                Response resp = call.execute();
                return new JSONArray(resp.body().toString());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private static class ParticipationRemovalTask<Void> extends AsyncTask<Void, Void, JSONArray> {

        private int eventId;
        private int userId;
        public ParticipationRemovalTask(int eventId, int userId) {
            this.eventId = eventId;
            this.userId = userId;
        }

        @Override
        protected JSONArray doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();

            FormBody.Builder formBuilder = null;
            formBuilder = new FormBody.Builder()
                    .add("hdl", "event")
                    .add("act","testleave")
                    .add("event", eventId+"")
                    .add("user", userId+"");

            final Request request = new Request.Builder()
                    .url("http://146.185.135.219/requestrouter.php")
                    .post(formBuilder.build())
                    .build();
            Call call = client.newCall(request);
            try {
                Response resp = call.execute();
                return new JSONArray(resp.body().toString());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
