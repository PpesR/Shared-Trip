package utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import adapters.NewAdminChoiceAdapter.MiniUserModel;
import models.UserEventModel;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import remm.sharedtrip.EventDetailsActivity;

import static utils.UtilBase.valueOrNull;

/**
 * Created by Mark on 12.11.2017.
 */

public class EventDetailsUtil extends UtilBase {

    public static class LeaveRequestTask<Void> extends AsyncTask<Void, Void, Void> {
        private int eventId;
        private int participatorId;
        private LeaveCallback leaveCallBack;

        public LeaveRequestTask(int eventId, int participatorId, LeaveCallback callback) {
            this.eventId = eventId;
            this.participatorId = participatorId;
            this.leaveCallBack = callback;
        }

        @SafeVarargs
        @Override
        protected final Void doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();
            final Request request = new Request.Builder()
                    .url(API_PREFIX + "/event/" + eventId + "/participator/" + participatorId)
                    .delete()
                    .build();
            Call call = client.newCall(request);
            call.enqueue(leaveCallBack);
            return null;
        }
    }

    public static class LeaveCallback implements Callback {

        private EventDetailsActivity eda;
        public LeaveCallback(EventDetailsActivity eda) { this.eda = eda; }
        @Override
        public void onFailure(Call call, IOException e) { }
        @Override
        public void onResponse(Call call, Response response) {
            try {
                String bodystring = response.body().string();
                JSONObject obj = new JSONObject(bodystring);
                if(!obj.has("error")){
                    eda.onLeaveSuccess();
                }
            } catch (IOException e) { e.printStackTrace(); } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static class JoinRequestTask<Void> extends AsyncTask<Void, Void, Void> {
        private int eventId;
        private int participatorId;
        private JoinCallback callback;

        public JoinRequestTask(int eventId, int participatorId, JoinCallback callback) {
            this.eventId = eventId;
            this.participatorId = participatorId;
            this.callback = callback;
        }

        @SafeVarargs
        @Override
        protected final Void doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();

            FormBody.Builder formBuilder = new FormBody.Builder()
                    .add("user", participatorId +"");

            final Request request = new Request.Builder()
                    .url(API_PREFIX+"/event/"+eventId+"/join")
                    .post(formBuilder.build())
                    .build();
            Call call = client.newCall(request);
            call.enqueue(callback);
            return null;
        }
    }

    public static class JoinCallback implements Callback {

        private EventDetailsActivity eda;
        public JoinCallback(EventDetailsActivity eda) { this.eda = eda; }
        @Override
        public void onFailure(Call call, IOException e) { }
        @Override
        public void onResponse(Call call, Response response) {
            try {
                String body = response.body().string();
                if (body.isEmpty()) {
                    eda.onJoinSuccess();
                }
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    public static class ParticipatorsTask<Void> extends AsyncTask<Void, Void, List<MiniUserModel>> {
        private int eventId;

        public ParticipatorsTask(int eventId) {
            this.eventId = eventId;
        }

        @SafeVarargs
        @Override
        protected final List<MiniUserModel> doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();

            final Request request = new Request.Builder()
                    .url(API_PREFIX+"/event/"+eventId+"/participators")
                    .build();
            List<MiniUserModel> models = new ArrayList<>();
            try {
                Response response = client.newCall(request).execute();
                String bodyString = response.body().string();

                if (bodyString.length()>0 && !bodyString.substring(0,1).equals("{")) {
                    JSONArray array = new JSONArray(bodyString);
                    for(int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        MiniUserModel model = new MiniUserModel();
                        model.firstName = valueOrNull(obj.getString("first_name"));
                        model.fullName = obj.getString("full_name");
                        model.id = obj.getInt("user_id");
                        model.profilePicture = Uri.parse(obj.getString("picture_uri"));
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

    public static class AdminRightsTask<Void> extends AsyncTask<Void, Void, Boolean> {
        private int adminId;
        private int eventId;
        private int participatorId;

        public AdminRightsTask(int eventId, int participatorId, int adminId) {
            this.eventId = eventId;
            this.participatorId = participatorId;
            this.adminId = adminId;
        }

        @SafeVarargs
        @Override
        protected final Boolean doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();

            FormBody.Builder formBuilder = new FormBody.Builder()
                    .add("event", eventId+"")
                    .add("user", participatorId +"");

            final Request request = new Request.Builder()
                    .url(API_PREFIX+"/admin/"+adminId+"/pass-rights")
                    .put(formBuilder.build())
                    .build();
            try {
                Response response = client.newCall(request).execute();
                String bodyString = response.body().string();
                if (bodyString.length()==0 || !new JSONObject(bodyString).has("error")) return true;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    public static class ApprovalStatusTask<Void> extends AsyncTask<Void, Void, Void> {
        private int eventId;
        private int participatorId;
        private ApprovalCallback callback;

        public ApprovalStatusTask(int eventId, int participatorId, ApprovalCallback callback) {
            this.eventId = eventId;
            this.participatorId = participatorId;
            this.callback = callback;
        }

        @SafeVarargs
        @Override
        protected final Void doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();

            final Request request = new Request.Builder()
                    .url(API_PREFIX+"/user/"+participatorId+"/event/"+eventId+"/status")
                    .build();
            Call call = client.newCall(request);
            call.enqueue(callback);
            return null;
        }
    }

    public static class ApprovalCallback implements Callback {

        private EventDetailsActivity eda;
        private UserEventModel model;
        public ApprovalCallback(EventDetailsActivity eda, UserEventModel model) { this.eda = eda; this.model=model; }
        @Override
        public void onFailure(Call call, IOException e) { }
        @Override
        public void onResponse(Call call, Response response) {
            try {
                String bodyString = response.body().string();
                JSONObject obj = new JSONObject(bodyString);
                if(!obj.has("error")) {
                        if (obj.getInt("is_admin")==1)
                            model.setAdmin(true);
                        else if (obj.getInt("approved")==1)
                            model.setUserApproved(true);
                        else if (obj.getInt("banned")==1) {
                            model.setUserBanned(true);
                        }
                    }
                    eda.onApprovalStatusReady();

            } catch (JSONException e) { e.printStackTrace();
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    public static Bitmap bitmapFromBase64String(String encodedString) {
        byte[] bytes = Base64.decode(encodedString, Base64.DEFAULT);
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        options.inSampleSize = calculateInSampleSize(options, 300, 200);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap bitmapFromUriString(String uriString) {
        try {
            URL url = new URL(uriString);
            return BitmapFactory.decodeStream(url.openStream());

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap bitmapFromString(String imageString) {
        if (imageString.matches("^https?://.*"))
            return bitmapFromUriString(imageString);

        return bitmapFromBase64String(imageString);
    }

    public static class GetImageTask<Void> extends AsyncTask<Void, Void, String> {
        private int eventId;

        public GetImageTask(int eventId) {
            this.eventId = eventId;
        }

        @SafeVarargs
        @Override
        protected final String doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();
            final Request request = new Request.Builder()
                    .url(API_PREFIX+"/event/"+eventId+"/image")
                    .build();
            try {
                Response response = client.newCall(request).execute();
                String bodyString = response.body().string();

                if (bodyString.length() > 0) {
                    return new JSONObject(bodyString).getString("event_picture");
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

}
