package services;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import remm.sharedtrip.R;

import static utils.ValueUtil.notNull;

/**
 * Created by Mark on 28.11.2017.
 */

public class SharedTripFirebaseMessagingService extends FirebaseMessagingService {

    private NotificationManager mNotificationManager;
    private static int mode = 0;

    public static final int NOTIFICATION = 0;
    public static final int IMMEDIATE = 1;
    private LocalBroadcastManager broadcaster;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // ...

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            String channelId = "my_channel_01";
            Map<String, String> messageData = remoteMessage.getData();
            /*if (Integer.parseInt(messageData.get("sender_id")) == BrowseEvents.userModel.id) {

            }
            else {*/
                String message = messageData.get("message");
                if (message.length() > 20) {
                    message = message.substring(0, 20)+"...";
                }

                if (mode == IMMEDIATE) {
                    Intent intent = new Intent("Message received");
                    intent.putExtra(
                            "messageData",
                            new JSONObject( remoteMessage.getData() ).toString()
                    );
                    broadcaster.sendBroadcast(intent);
                }
                else {
                    int mNotificationId = Integer.parseInt(messageData.get("message_id"));
                    NotificationCompat.Builder mBuilder = null;
                    try {
                        mBuilder = new NotificationCompat.Builder(this, channelId)
                                .setSmallIcon(R.drawable.ic_chat_black_24dp)
                                .setLargeIcon(BitmapFactory.decodeStream(new URL(messageData.get("sender_picture")).openStream()))
                                .setContentTitle("New message in " + messageData.get("event_name"))
                                .setContentText(messageData.get("sender_name") + " said \"" + message + "\"")
                                .setColor(getResources().getColor(R.color.orangered))
                                .setPriority(2)
                                .setVibrate((new long[]{1000, 1000, 1000}));
                        mNotificationManager.notify(mNotificationId, mBuilder.build());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        }
    }

    @Override
    public void onMessageSent(String s) {
        super.onMessageSent(s);
    }

    @Override
    public void onSendError(String s, Exception e) {
        super.onSendError(s, e);
    }

    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        broadcaster = LocalBroadcastManager.getInstance(this);
        broadcaster.registerReceiver(mMessageReceiver, new IntentFilter("Switch to immediate"));
        broadcaster.registerReceiver(mMessageReceiver, new IntentFilter("Switch to notification"));
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (notNull(action)) {
                switch (action) {
                    case "Switch to immediate":
                        mode = IMMEDIATE;
                        break;
                    case "Switch to notification":
                        mode = NOTIFICATION;
                        break;
                }
            }
        }
    };
}
