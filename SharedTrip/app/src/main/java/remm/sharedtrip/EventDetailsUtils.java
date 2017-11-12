package remm.sharedtrip;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;

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
}
