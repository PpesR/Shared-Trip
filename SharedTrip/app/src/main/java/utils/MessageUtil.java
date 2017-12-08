package utils;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import okhttp3.FormBody.Builder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static utils.ValueUtil.toNullSafe;

/**
 * Created by Mark on 28.11.2017.
 */

public class MessageUtil {

    public static class MessageSaveResponse {
        public int senderId;
        public int eventId;

        public MessageSaveResponse(int messageId, Date deliverTime) {
            this.messageId = messageId;
            this.deliverTime = deliverTime;
        }

        public int messageId;
        public Date actualSendTime;
        public Date deliverTime;
        public String topic;
        public String message;
        public String FirebaseCloudMessagingId;
        public Uri senderPicture;
        public String senderName;
    }

    public static class MessageSavingTask<Void> extends AsyncTask<Void, Void, MessageSaveResponse> {

        private String apiPrefix;
        private String message;
        private String topic;
        private String senderId;
        private Date timeSent;
        private int eventId;

        public SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm:dd");

        public MessageSavingTask(String apiPrefix, String message, String topic, String senderId, Date timeSent, int eventId) {
            this.apiPrefix = apiPrefix;
            this.message = message;
            this.topic = topic;
            this.senderId = senderId;
            this.timeSent = timeSent;
            this.eventId = eventId;
        }

        @SafeVarargs
        @Override
        @SuppressLint("SimpleDateFormat")
        protected final MessageSaveResponse doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();

            Builder formBodyBuilder = new Builder()
                    .add("message", toNullSafe(message))
                    .add("topic", toNullSafe(topic))
                    .add("sender_id", toNullSafe(senderId))
                    .add("time_sent", dateFormat.format(timeSent))
                    .add("event", eventId+"");

            Request request = new Request.Builder()
                    .url(apiPrefix+"/message")
                    .post(formBodyBuilder.build())
                    .build();
            MessageSaveResponse saveResponse = null;
            try {
                Response response = client.newCall(request).execute();
                String bodystring = response.body().string();
                if (bodystring.equals("")) return null;

                JSONObject obj = new JSONObject(bodystring);

                if (!obj.has("error")){
                    int messageId = obj.getInt("id");
                    String timeString = obj.getString("time_sent_utc");
                    Date timeSent =  dateFormat.parse(timeString);
                    saveResponse = new MessageSaveResponse(messageId, timeSent);
                    saveResponse.senderId = obj.getInt("sender_id");
                    saveResponse.eventId = obj.getInt("event_id");
                    saveResponse.topic = obj.getString("topic");
                    saveResponse.message = obj.getString("message");
                    saveResponse.actualSendTime = dateFormat.parse(obj.getString("time_fcm_received_utc"));
                    saveResponse.FirebaseCloudMessagingId = obj.getString("fcm_id");
                    saveResponse.senderName = obj.getString("sender_name");
                    saveResponse.senderPicture = Uri.parse(obj.getString("sender_picture"));

                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            return saveResponse;
        }
    }

    public static class HistoryRetrievalTask<Void> extends AsyncTask<Void, Void, List<String>> {

        private int userId;
        private int eventId;
        private String apiPrefix;

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm:dd");

        public HistoryRetrievalTask(int userId, int eventId, String apiPrefix) {
            this.userId = userId;
            this.eventId = eventId;
            this.apiPrefix = apiPrefix;
        }

        @SafeVarargs
        @Override
        @SuppressLint("SimpleDateFormat")
        protected final  List<String> doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(apiPrefix+"/user/"+userId+"/conversation?event="+eventId)
                    .build();

            List<String> historyResponse = new ArrayList<>();
            try {
                Response response = client.newCall(request).execute();
                String bodyString = response.body().string();

                if (bodyString.equals("")) return null;

                if (bodyString.substring(0,1).equals("{")){
                    JSONObject obj = new JSONObject(bodyString);
                    if (obj.has("error")){

                    }
                }
                else {
                    JSONArray arr = new JSONArray(bodyString);
                    int len = arr.length();

                    for (int i = len-1; i >= 0; i--) {
                        JSONObject message = arr.getJSONObject(i);
                        String dateStr = message.getString("time_fcm_received_utc");
                        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                        Date date = null;
                        try {
                            date = dateFormat.parse(dateStr);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        TimeZone def = TimeZone.getDefault();
                        dateFormat.setTimeZone(def);
                        String formattedDate = dateFormat.format(date);

                        historyResponse.add(formattedDate+"\r\n"+
                                message.getString("sender_name")+": "+message.getString("message"));
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return historyResponse;
        }
    }
}
