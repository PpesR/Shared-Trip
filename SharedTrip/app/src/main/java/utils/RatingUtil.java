package utils;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import adapters.RateUserAdapter.RateablePerson;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Mark on 11.12.2017.
 */

public class RatingUtil extends UtilBase {

    public static class RateablesTask<Void> extends AsyncTask<Void, Void, List<RateablePerson>> {

        private int userId;

        public RateablesTask(int userId) {
            this.userId = userId;
        }

        @Override
        protected List<RateablePerson> doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(API_PREFIX + "/user/" + userId + "/rateables").build();

            List<RateablePerson> people = new ArrayList<>();
            try {
                Response response = client.newCall(request).execute();
                String body = response.body().string();

                if (body.length() > 0 && body.charAt(0)!='{') {
                    JSONArray array = new JSONArray(body);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject o = array.getJSONObject(i);
                        RateablePerson person = new RateablePerson();
                        person.id = o.getInt("user_id");
                        person.name = o.getString("full_name");
                        person.profilePictureUri = o.getString("user_pic");
                        person.eventId = o.getInt("event_id");
                        person.eventName = o.getString("trip_name");
                        person.isAdmin = o.getInt("is_admin") == 1;
                        people.add(person);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return people;
        }
    }

    public static class RateTask<Void> extends AsyncTask<Void,Void,Void> {

        private int userId;
        private int eventId;
        private int raterId;
        private int rating;

        public RateTask(int userId, int eventId, int raterId, int rating) {
            this.userId = userId;
            this.eventId = eventId;
            this.raterId = raterId;
            this.rating = rating;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();
            FormBody.Builder formBuilder = new FormBody.Builder()
                    .add("event", eventId+"")
                    .add("rater", raterId+"")
                    .add("value", rating+"");

            Request request = new Request.Builder()
                    .url(API_PREFIX + "/user/" + userId + "/rate")
                    .post(formBuilder.build())
                    .build();
            try {
                client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public static class MyRatingsTask<Void> extends AsyncTask<Void,Void,Ratings> {
        private int userId;
        private MyRatingsCallback callback;

        public MyRatingsTask(int userId, MyRatingsCallback callback) {
            this.userId = userId;
            this.callback = callback;
        }

        @Override
        protected Ratings doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(API_PREFIX+"/user/"+userId+"/ratings").build();
            client.newCall(request).enqueue(callback);
            return null;
        }
    }

    public static class MyRatingsCallback implements Callback {

        private UserRatingHolder holder;

        public MyRatingsCallback(UserRatingHolder holder) {
            this.holder = holder;
        }

        @Override
        public void onFailure(Call call, IOException e) {

        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            try {
                String body = response.body().string();
                if (body.length()>0) {
                    JSONObject o = new JSONObject(body);
                    if (!o.has("error")) {
                        Ratings r = new Ratings();
                        r.hearts = o.getInt("heart");
                        r.smileys = o.getInt("smile");
                        r.thumbsDown = o.getInt("thumbs_down");
                        r.thumbsUp = o.getInt("thumbs_up");
                        holder.ShowOwnRatings(r);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public interface UserRatingHolder {
        void ShowOwnRatings(Ratings ratings);
    }

    public static class Ratings {
        public int thumbsUp;
        public int thumbsDown;
        public int hearts;
        public int smileys;
    }
}
