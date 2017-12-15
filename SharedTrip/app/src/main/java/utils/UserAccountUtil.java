package utils;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.facebook.Profile;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import remm.sharedtrip.MainActivity.FbGoogleUserModel;

import static utils.UtilBase.notNull;
import static utils.UtilBase.notNullOrEmpty;
import static utils.UtilBase.toNullSafe;
import static utils.UtilBase.toStringOrNull;
import static utils.UtilBase.valueOrNull;

/**
 * Created by Mark on 27.11.2017.
 */

public class UserAccountUtil extends UtilBase {

    public interface UserActivityHandle {
        void onUserCheckReady(FbGoogleUserModel model);
        void fillModelFromJson(JSONObject obj);
    }

    public static class UserDataTask<Void> extends AsyncTask<Void, Void, FbGoogleUserModel> {

        private int userId;

        public UserDataTask(int userId) {
            this.userId = userId;
        }

        @SafeVarargs
        @Override
        protected final FbGoogleUserModel doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();

            final Request request = new Request.Builder()
                    .url(API_PREFIX+"/user/"+userId)
                    .get()
                    .build();

            FbGoogleUserModel model = new FbGoogleUserModel();
            try {
                Response response = client.newCall(request).execute();
                String bodyString = response.body().string();
                JSONObject obj = new JSONObject(bodyString);

                if (!obj.has("error")) {
                    model.id = userId;
                    model.facebookId = valueOrNull(obj.getString("fb_id"));
                    model.googleId = valueOrNull(obj.getString("google_id"));
                    model.name = valueOrNull(obj.getString("name"));
                    model.firstName = valueOrNull(obj.getString("first_name"));
                    model.description = valueOrNull(obj.getString("user_desc"));
                    model.imageUriString = valueOrNull(obj.getString("user_pic"));
                    model.gender = valueOrNull(obj.getString("gender"));
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return model;
        }
    }

    public static class UserCheckingTask<Void> extends AsyncTask<Void, Void, Void> {

        private UserCheckCallback callback;
        private String gId;
        private String fbId;

        public UserCheckingTask(UserCheckCallback callback, String gId, String fbId) {
            this.callback = callback;
            this.gId = gId;
            this.fbId = fbId;
        }

        @SafeVarargs
        @Override
        protected final Void doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();

            String param =
                    notNull(fbId)
                    ? "?fb_id="+fbId
                    : ( notNull(gId)
                        ? "?google_id="+gId
                        : "" );

            final Request request = new Request.Builder()
                    .url(API_PREFIX+"/user/exists"+param)
                    .get()
                    .build();

            Call call = client.newCall(request);
            call.enqueue(callback);
            return null;
        }
    }

    public static class UserCheckCallback implements Callback {

        private UserActivityHandle handle;
        public UserCheckCallback(UserActivityHandle handle) { this.handle = handle; }

        @Override
        public void onFailure(Call call, IOException e) { handle.onUserCheckReady(null); }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            String responseBody = response.body().string();

            if (notNullOrEmpty(responseBody)) {
                try {
                    JSONObject obj = new JSONObject(responseBody);
                    FbGoogleUserModel model = null;

                    if (!obj.has("error")) {
                        model = new FbGoogleUserModel();
                        model.id = obj.getInt("id");
                        model.facebookId = valueOrNull(obj.getString("fb_id"));
                        model.googleId = valueOrNull(obj.getString("google_id"));
                        model.name = valueOrNull(obj.getString("name"));
                        model.description = valueOrNull(obj.getString("user_desc"));
                        model.imageUriString = valueOrNull(obj.getString("user_pic"));
                        model.gender = valueOrNull(obj.getString("gender"));
                    }
                    handle.onUserCheckReady(model);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static class UserRegistrationTask<Void> extends AsyncTask<Void, Void, Void> {

        private UserRegistrationCallback callback;
        private FbGoogleUserModel model;
        public UserRegistrationTask(FbGoogleUserModel model, UserRegistrationCallback callback) {

            this.callback = callback;
            this.model = model;
        }

        @SafeVarargs
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected final Void doInBackground(final Void... nothings) {
            OkHttpClient client = new OkHttpClient();

            FormBody.Builder formBuilder = new FormBody.Builder()
                    .add("name", model.name)
                    .add("gender", toNullSafe(model.gender));

            if (model.hasFacebook()) {
                formBuilder.add("fb_id", model.facebookId);
                Uri uri = Profile
                        .getCurrentProfile()
                        .getProfilePictureUri(300, 300);
                model.imageUriString = toStringOrNull(uri);
            }
            if (model.hasGoogle()) formBuilder.add("google_id", model.googleId);

            formBuilder.add("picture", toNullSafe(model.imageUriString));

            final Request request = new Request.Builder()
                    .url(API_PREFIX+"/user")
                    .post(formBuilder.build())
                    .build();

            Call call = client.newCall(request);
            call.enqueue(callback);
            return null;
        }
    }

    public static class UserRegistrationCallback implements Callback {

        private UserActivityHandle handle;
        public UserRegistrationCallback(UserActivityHandle handle) { this.handle = handle; }

        @Override
        public void onFailure(Call call, IOException e) { }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            String responseBody = response.body().string();
            if (responseBody != null && responseBody.length() > 0)
            try {
                JSONObject obj = new JSONObject(responseBody);
                handle.fillModelFromJson(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
