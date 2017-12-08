package utils;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
        public Date sendTime;
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

        public SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:dd");

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

            /* TODO: Add event id*/
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
                    int messageId = obj.getInt("message_id");
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
}
