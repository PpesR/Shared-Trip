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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody.Builder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import remm.sharedtrip.MainActivity.FbGoogleUserModel;

import static utils.DebugUtil.doNothing;

/**
 * Created by Mark on 28.11.2017.
 */

public class MessageUtil extends UtilBase {

    public static class ChatMessage {

        public SimpleDateFormat dateFormatDisplayLong = new SimpleDateFormat("MMM d HH:mm");
        public SimpleDateFormat dateFormatDisplayShort = new SimpleDateFormat("HH:mm");

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ChatMessage message = (ChatMessage) o;

            if (id != message.id) return false;
            if (sender != message.sender) return false;
            if (event != message.event) return false;
            if (!fcmId.equals(message.fcmId)) return false;
            return text != null ? text.equals(message.text) : message.text == null;
        }

        @Override
        public int hashCode() {
            int result = id;
            result = 31 * result + sender;
            result = 31 * result + event;
            result = 31 * result + fcmId.hashCode();
            result = 31 * result + (text != null ? text.hashCode() : 0);
            return result;
        }

        public ChatMessage() {
            dateFormatDisplayLong.setTimeZone(TimeZone.getDefault());
            dateFormatDisplayShort.setTimeZone(TimeZone.getDefault());
        }

        public int id;
        public int sender;
        public int event;
        public String topic;
        public Date timeWritten;
        public Date timeSent;
        public String fcmId;
        public  String text;
        public String senderName;
        public Uri senderPicture;
        public boolean isSeen;
        public boolean isOwn(FbGoogleUserModel model) {
            return sender == model.id;
        }
    }

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

        private String message;
        private String topic;
        private String senderId;
        private Date timeSent;
        private int eventId;

        public SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:dd");

        public MessageSavingTask(String message, String topic, String senderId, Date timeSent, int eventId) {
            this.message = message;
            this.topic = topic;
            this.senderId = senderId;
            this.timeSent = timeSent;
            this.eventId = eventId;
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
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
                    .url(API_PREFIX+"/message")
                    .post(formBodyBuilder.build())
                    .build();
            MessageSaveResponse saveResponse = null;
            try {
                client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
                /*String bodystring = response.body().string();
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
            }*/

            return null;
        }
    }

    public static class HistoryTask<Void> extends AsyncTask<Void, Void, Void> {

        private int userId;
        private int eventId;
        private HistoryCallback callback;
        private int lastMessageId = 0;

        public HistoryTask(int userId, int eventId, HistoryCallback callback, int lastMessageId) {
            this.userId = userId;
            this.eventId = eventId;
            this.callback = callback;
            this.lastMessageId = lastMessageId;
        }

        public HistoryTask(int userId, int eventId, HistoryCallback callback) {
            this.userId = userId;
            this.eventId = eventId;
            this.callback = callback;
        }

        @SafeVarargs
        @Override
        @SuppressLint("SimpleDateFormat")
        protected final Void doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(API_PREFIX+"/user/"+userId+"/conversation?event="+eventId+(
                            lastMessageId > 0 ?
                                    "&from=" + lastMessageId : ""))
                    .build();

            client.newCall(request).enqueue(callback);

            return null;
        }
    }

    public interface ChatMessagesHolder {
        void display(List<ChatMessage> messages);
    }

    public static class HistoryCallback implements Callback {

        private ChatMessagesHolder holder;

        SimpleDateFormat dateFormatUTC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        public HistoryCallback(ChatMessagesHolder holder) {
            this.holder = holder;
            dateFormatUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        @Override
        public void onFailure(Call call, IOException e) {

        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            List<ChatMessage> messages = new ArrayList<>();
            try {
                String bodyString = response.body().string();

                if (bodyString.equals("")) return;

                if (bodyString.substring(0,1).equals("{")){
                    JSONObject obj = new JSONObject(bodyString);
                    if (obj.has("error")){
                        doNothing();
                    }
                }
                else {
                    JSONArray arr = new JSONArray(bodyString);
                    int len = arr.length();

                    // iterating from oldest
                    for (int i = len-1; i >= 0; i--) {
                        JSONObject o = arr.getJSONObject(i);
                        ChatMessage message = new ChatMessage();
                        message.id = o.getInt("id");
                        message.event = o.getInt("event_id");
                        message.sender = o.getInt("sender_id");
                        message.senderName = o.getString("sender_name");
                        message.senderPicture = Uri.parse(o.getString("sender_picture"));
                        message.isSeen = o.getInt("is_seen")==1;
                        message.topic = o.getString("topic");
                        message.timeWritten = dateFormatUTC.parse(o.getString("time_sent_utc"));
                        message.timeSent = dateFormatUTC.parse(o.getString("time_fcm_received_utc"));
                        message.fcmId = o.getString("fcm_id");
                        message.text = o.getString("message");
                        messages.add(message);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            holder.display(messages);
        }
    }
}
