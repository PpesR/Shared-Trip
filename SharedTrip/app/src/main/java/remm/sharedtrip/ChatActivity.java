package remm.sharedtrip;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import models.UserEventModel;
import utils.MessageUtil;

import static remm.sharedtrip.BrowseEvents.userModel;

/**
 * Created by Mark on 28.11.2017.
 */

public class ChatActivity extends AppCompatActivity {

    private FirebaseMessaging fm;
    private EditText messageInput;
    private ImageButton sendButton;
    private final String SENDER_ID = "631771254653";
    private UserEventModel event;
    private String messageTopic;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fm = FirebaseMessaging.getInstance();

        event = new Gson().fromJson(getIntent().getStringExtra("event"), UserEventModel.class);
        messageTopic = event.getId()+"-"+event.getLoc().toLowerCase().replaceAll("[^a-z]", "");
        fm.subscribeToTopic(messageTopic);

        setContentView(R.layout.temp_chat_layout);

        messageInput = findViewById(R.id.chat_message_input);

        sendButton = findViewById(R.id.chat_send_button);
        sendButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String messageText = messageInput.getText().toString();
                Date now = new Date();

                MessageUtil.MessageSavingTask<Void> task =
                        new MessageUtil.MessageSavingTask<>(
                                getResources().getString(R.string.api_address_with_prefix),
                                messageText,
                                messageTopic,
                                userModel.id+"",
                                now
                        );
                try {
                    MessageUtil.MessageSaveResponse response = task.execute().get();
                    fm.send(new RemoteMessage.Builder(SENDER_ID + "@gcm.googleapis.com")
                            .setMessageId(Integer.toString(response.messageId))
                            .addData("sender_id", userModel.id+"")
                            .addData("message", "Hello World")
                            .addData("topic", messageTopic)
                            .addData("time_sent", new SimpleDateFormat("yyyy-MM-dd hh:mm:dd").format(now))
                            .build());

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
