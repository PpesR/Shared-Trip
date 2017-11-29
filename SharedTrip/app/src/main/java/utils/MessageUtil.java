package utils;

import android.annotation.SuppressLint;
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

import static remm.sharedtrip.MainActivity.getNullSafeValue;

/**
 * Created by Mark on 28.11.2017.
 */

public class MessageUtil {

    public static class MessageSaveResponse {
        public MessageSaveResponse(int messageId, Date sendTime) {
            this.messageId = messageId;
            this.sendTime = sendTime;
        }

        public int messageId;
        public Date sendTime;
    }

    public static class MessageSavingTask<Void> extends AsyncTask<Void, Void, MessageSaveResponse> {

        private String apiPrefix;
        private String message;
        private String topic;
        private String senderId;
        private Date timeSent;

        public MessageSavingTask(String apiPrefix, String message, String topic, String senderId, Date timeSent) {
            this.apiPrefix = apiPrefix;
            this.message = message;
            this.topic = topic;
            this.senderId = senderId;
            this.timeSent = timeSent;
        }

        @SafeVarargs
        @Override
        @SuppressLint("SimpleDateFormat")
        protected final MessageSaveResponse doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();

            Builder formBodyBuilder = new Builder()
                    .add("message", getNullSafeValue(message))
                    .add("topic", getNullSafeValue(topic))
                    .add("sender_id", getNullSafeValue(senderId))
                    .add("time_sent", new SimpleDateFormat("yyyy-MM-dd hh:mm:dd").format(timeSent));

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
                    Date timeSent =  new SimpleDateFormat("yyyy-MM-dd hh:mm:dd").parse(timeString);
                    saveResponse = new MessageSaveResponse(messageId, timeSent);
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
