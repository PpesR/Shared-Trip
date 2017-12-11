package utils;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import adapters.RateUserAdapter.RateablePerson;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Mark on 11.12.2017.
 */

public class RatingUtil {

    public static class RateablesTask<Void> extends AsyncTask<Void, Void, List<RateablePerson>> {

        private String apiPrefix;
        private int userId;

        public RateablesTask(String apiPrefix, int userId) {
            this.apiPrefix = apiPrefix;
            this.userId = userId;
        }

        @Override
        protected List<RateablePerson> doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(apiPrefix+"/user/"+userId+"/rateables").build();

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

    public static class RatingTask<Void> extends AsyncTask<Void,Void,Boolean> {

        private String apiPrefix;
        private int userId;
        private int eventId;
        private int raterId;
        private int rating;

        public RatingTask(String apiPrefix, int userId, int eventId, int raterId, int rating) {
            this.apiPrefix = apiPrefix;
            this.userId = userId;
            this.eventId = eventId;
            this.raterId = raterId;
            this.rating = rating;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();
            FormBody.Builder formBuilder = formBuilder = new FormBody.Builder()
                    .add("event", eventId+"")
                    .add("rater", raterId+"")
                    .add("value", rating+"");

            Request request = new Request.Builder()
                    .url(apiPrefix+"/user/"+userId+"/rate")
                    .post(formBuilder.build())
                    .build();
            try {
                Response response = client.newCall(request).execute();
                String body = response.body().string();
                if (response.isSuccessful() && body.length()==0){
                    return true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    public static class MyRatingsTask<Void> extends AsyncTask<Void,Void,Ratings> {
        private String apiPrefix;
        private int userId;

        public MyRatingsTask(String apiPrefix, int userId) {
            this.apiPrefix = apiPrefix;
            this.userId = userId;
        }

        @Override
        protected Ratings doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(apiPrefix+"/user/"+userId+"/ratings").build();

            try {
                Response response = client.newCall(request).execute();
                String body = response.body().string();
                if (body.length()>0) {
                    JSONObject o = new JSONObject(body);
                    if (!o.has("error")) {
                        Ratings r = new Ratings();
                        r.hearts = o.getInt("heart");
                        r.smileys = o.getInt("smile");
                        r.thumbsDown = o.getInt("thumbs_down");
                        r.thumbsUp = o.getInt("thumbs_up");
                        return r;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public static class Ratings {
        public Ratings() {}
        public int thumbsUp;
        public int thumbsDown;
        public int hearts;
        public int smileys;
    }
}
