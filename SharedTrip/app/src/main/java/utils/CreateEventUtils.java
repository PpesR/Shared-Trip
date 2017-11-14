package utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import models.AdminEventModel;
import models.CreatorEventModel;
import models.EventModel;
import models.UserEventModel;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import remm.sharedtrip.CreateEvent;

/**
 * Created by Mark on 14.11.2017.
 */

public class CreateEventUtils {

    public static interface IEventCreatorCaller {
        void onEventCreated();
    }

    public static class EventCreationTask<Void> extends AsyncTask<Void, Void, Void> {

        private CreatorEventModel model;

        public EventCreationTask(CreatorEventModel model) {
            this.model = model;
        }

        @SafeVarargs
        @Override
        protected final Void doInBackground(Void... voids) {

            final MediaType MY_MEDIA_TYPE = MediaType.parse("image/bmp");

            OkHttpClient client = new OkHttpClient();
            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("user", model.getAdminId()+"")
                    .addFormDataPart("location", model.getLoc()+"")
                    .addFormDataPart("name", model.getName()+"")
                    .addFormDataPart("description", model.getDescription()+"")
                    .addFormDataPart("total_cost",model.getCost()+"")
                    .addFormDataPart("spots", model.getSpots()+"")
                    .addFormDataPart("start_date", model.getStartDate()+"")
                    .addFormDataPart("end_date", model.getEndDate()+"")
                    .addFormDataPart("private", model.isPrivate() ? "1" : "0")
                    .addFormDataPart("hdl", "event");

            if (model.getImageFile()!=null) {
                builder.addFormDataPart(
                        "picture",
                        "testFile.png",
                        RequestBody.create(MY_MEDIA_TYPE, model.getImageFile()))
                        .build();
            }

            final Request request = new Request.Builder()
                    .url("http://146.185.135.219/requestrouter.php")
                    .post(builder.build())
                    .build();
            Call call = client.newCall(request);

            call.enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        JSONArray array = new JSONArray(response.body().string());
                        JSONArray actual = array.getJSONArray(2);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            return null;
        }
    }

    public static class ImageUploadTask<Void> extends AsyncTask<Void, Void, Void> {

        public ImageUploadTask(ImageUploadCallback callback, Uri imageUri, CreateEvent caller) {
            this.callback = callback;
            this.imageUri = imageUri;
            this.caller = caller;
        }

        private ImageUploadCallback callback;
        private Uri imageUri;
        private CreateEvent caller;

        @SafeVarargs
        @Override
        protected final Void doInBackground(Void... voids) {

            final MediaType MY_MEDIA_TYPE = MediaType.parse("image/bmp");

            OkHttpClient client = new OkHttpClient();
            MultipartBody multipartBody = null;
            try {
                multipartBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("userid", "8457851245")
                        .addFormDataPart(
                                "file",
                                "testFile.png",
                                RequestBody.create(MY_MEDIA_TYPE, new File(getFilePath(caller, imageUri))))
                        .build();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

            final Request request = new Request.Builder()
                    .url("http://146.185.135.219/api/v1/upload")
                    .post(multipartBody)
                    .build();

            Call call = client.newCall(request);
            call.enqueue(callback);
            return null;
        }
    }

    public static class ImageUploadCallback implements Callback {

        private CreateEvent activity;

        public ImageUploadCallback(CreateEvent activity) {
            this.activity = activity;
        }

        @Override
        public void onFailure(Call call, IOException e) {
            String exe = e.getMessage();
            String s = "";
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            String responseString = response.body().string();
            byte[] bytes = Base64.decode(responseString, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            activity.onImageUploaded(decodedByte);
        }
    }



    @SuppressLint("NewApi")
    public static String getFilePath(Activity activity, Uri uri) throws URISyntaxException {
        String selection = null;
        String[] selectionArgs = null;
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        if (Build.VERSION.SDK_INT >= 19 && DocumentsContract.isDocumentUri(activity, uri)) {//DocumentsContract.isDocumentUri(context.getApplicationContext(), uri))
            if (uri.toString().contains("mnt")) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else if (uri.toString().contains("downloads")) {
                final String id = DocumentsContract.getDocumentId(uri);
                uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            } else if (uri.toString().contains("media")) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("image".equals(type)) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[]{
                        split[1]
                };
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {
                    MediaStore.Images.Media.DATA
            };
            Cursor cursor = null;
            try {
                cursor = activity.getContentResolver()
                        .query(uri, projection, selection, selectionArgs, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }
}
