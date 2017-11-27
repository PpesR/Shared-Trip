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

import static remm.sharedtrip.MainActivity.getValueOrNull;

/**
 * Created by Mark on 27.11.2017.
 */

public class UserAccountUtil {

    public interface UserActivityHandle {
        void onUserCheckReady(FbGoogleUserModel model);
        void fillModelFromJson(JSONObject obj);
    }

    public static class UserCheckingTask<Void> extends AsyncTask<Void, Void, Void> {

        private String apiPrefix;
        private UserCheckCallback callback;
        private String gId;
        private String fbId;

        public UserCheckingTask(String apiPrefix, UserCheckCallback callback, String gId, String fbId) {
            this.apiPrefix = apiPrefix;
            this.callback = callback;
            this.gId = gId;
            this.fbId = fbId;
        }

        @SafeVarargs
        @Override
        protected final Void doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();

            String param = fbId != null
                    ? "?fb_id="+fbId
                    : (gId != null
                        ? "?google_id="+gId
                        : ""
            );

            final Request request = new Request.Builder()
                    .url(apiPrefix+"/user/exists"+param)
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
            if (responseBody != null && responseBody.length() > 0) {
                try {
                    JSONObject obj = new JSONObject(responseBody);
                    FbGoogleUserModel model = null;

                    if (!obj.has("error")) {
                        model = new FbGoogleUserModel();
                        model.id = obj.getInt("id");
                        model.fbId = getValueOrNull(obj.getString("fb_id"));
                        model.googleId = getValueOrNull(obj.getString("google_id"));
                        model.name = getValueOrNull(obj.getString("name"));
                        model.description = getValueOrNull(obj.getString("user_desc"));
                        model.imageUri = getValueOrNull(obj.getString("user_pic"));
                        model.gender = getValueOrNull(obj.getString("gender"));
                    }

                    handle.onUserCheckReady(model);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static class UserRegistrationTask<Void> extends AsyncTask<Void, Void, Void> {

        private String apiPrefix;
        private UserRegistrationCallback callback;
        private FbGoogleUserModel model;
        public UserRegistrationTask(String apiPrefix, FbGoogleUserModel model, UserRegistrationCallback callback) {

            this.apiPrefix = apiPrefix;
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
                    .add("gender", model.gender == null? "null" : model.gender);

            if (model.hasFacebook()) {
                formBuilder.add("fb_id", model.fbId);
                Uri uri = Profile
                        .getCurrentProfile()
                        .getProfilePictureUri(300, 300);
                model.imageUri = uri == null ? null : uri.toString();
            }
            if (model.hasGoogle()) formBuilder.add("google_id", model.googleId);

            formBuilder.add("picture", model.imageUri == null ? "null" : model.imageUri);

            final Request request = new Request.Builder()
                    .url(apiPrefix+"/user")
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
