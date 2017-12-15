package remm.sharedtrip;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import adapters.ChatMessageAdapter;
import interfaces.UserModelHolder;
import models.UserEventModel;
import remm.sharedtrip.MainActivity.FbGoogleUserModel;
import utils.MessageUtil;
import utils.MessageUtil.ChatMessage;
import utils.MessageUtil.HistoryTask;
import static utils.UtilBase.isNull;

/**
 * Created by Mark on 28.11.2017.
 */

public class ChatActivity extends AppCompatActivity implements MessageUtil.ChatMessagesHolder, UserModelHolder {

    private ChatActivity self;
    private FirebaseMessaging fm;
    private EditText messageInput;
    private ImageButton sendButton;
//    private final String SENDER_ID = "631771254653";
    private UserEventModel event;
    private String messageTopic;
    private ChatMessageAdapter adapter;
    private String messageText;
    private FbGoogleUserModel userModel;
    List<ChatMessage> messages = new ArrayList<>();
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    SimpleDateFormat dateFormatUTC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private String serializedUser;
    private int preLastId = -1;
    private int lastId = 0;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        self = this;
        serializedUser = getIntent().getStringExtra("user");
        userModel = new Gson().fromJson(serializedUser, FbGoogleUserModel.class);
        dateFormatUTC.setTimeZone(TimeZone.getTimeZone("UTC"));

        fm = FirebaseMessaging.getInstance();
        LocalBroadcastManager.getInstance(this).registerReceiver((mMessageReceiver),
                new IntentFilter("Message received")
        );

        event = new Gson().fromJson(getIntent().getStringExtra("event"), UserEventModel.class);
        messageTopic = event.getId()+"-"+event.getLoc().toLowerCase().replaceAll("[^a-z]", "");
        fm.subscribeToTopic(messageTopic);

        setContentView(R.layout.activity_chat);
        recyclerView = findViewById(R.id.chat_message_list);
        layoutManager = new LinearLayoutManager(ChatActivity.this);
        recyclerView.setLayoutManager(layoutManager);

        messageInput = findViewById(R.id.chat_message_input);
        messageInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    trySendMessage();
                    handled = true;
                }
                return handled;
            }
        });
        sendButton = findViewById(R.id.chat_send_button);
    }

    private void trySendMessage() {
        messageText = messageInput.getText().toString();
        messageInput.setText("");
        hideSoftKeyboard(ChatActivity.this);
        Date now = new Date();

        MessageUtil.MessageSavingTask<Void> task =
                new MessageUtil.MessageSavingTask<>(
                        messageText,
                        messageTopic,
                        userModel.id+"",
                        now,
                        event.getId());
        task.execute();
    }

    private void addMessage(final ChatMessage m) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messages.add(m);
                adapter.notifyDataSetChanged();
                layoutManager.scrollToPosition(messages.size()-1);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestMessageHistory();
    }

    private void requestMessageHistory() {
        HistoryTask<Void> task;
        if (lastId > 0)
             task = new HistoryTask<>(
                    userModel.id,
                    event.getId(),
                    new MessageUtil.HistoryCallback(this),
                     lastId);
        else
            task = new HistoryTask<>(
                    userModel.id,
                    event.getId(),
                    new MessageUtil.HistoryCallback(this));
        task.execute();
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String json = intent.getStringExtra("messageData");
            try {
                JSONObject obj = new JSONObject(json);
                ChatMessage newMessage = new ChatMessage();
                newMessage.id = Integer.parseInt(obj.getString("message_id"));
                newMessage.sender = Integer.parseInt(obj.getString("sender_id"));
                newMessage.event = Integer.parseInt(obj.getString("event_id"));
                try {
                    newMessage.timeSent = dateFormatUTC.parse(obj.getString("time"));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                newMessage.text = obj.getString("message");
                newMessage.senderName = obj.getString("sender_name");
                newMessage.senderPicture = Uri.parse(obj.getString("sender_picture"));
                ChatActivity.this.addMessage(newMessage);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager broadcaster = LocalBroadcastManager.getInstance(this);
        broadcaster.unregisterReceiver(mMessageReceiver);
        broadcaster.sendBroadcast(new Intent("Switch to notification"));
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }

    @Override
    public void display(final List<ChatMessage> messages) {
        preLastId = lastId;
        if (!messages.isEmpty()) {
            List<ChatMessage> temp = new ArrayList<>();
            temp.addAll(messages);
            temp.addAll(this.messages);
            this.messages = temp;
            lastId = messages.get(messages.size() - 1).id;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isNull(adapter)) {
                        adapter = new ChatMessageAdapter(self.messages, self);
                        recyclerView.setAdapter(adapter);
                        layoutManager.scrollToPosition(messages.size()-1);
                        recyclerView.addOnScrollListener(new OnScrollListener() {

                            @Override
                            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                                super.onScrolled(recyclerView, dx, dy);
                                if (!recyclerView.canScrollVertically(-1) && lastId > 0 && lastId!=preLastId){
                                    requestMessageHistory();
                                }
                            }
                        });
                    }
                    else {
                        adapter.updateMessages(self.messages);
                    }
                }
            });
        }
        sendButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                trySendMessage();
            }
        });
    }

    @Override
    public FbGoogleUserModel getLoggedInUser() {
        return userModel;
    }

    @Override
    public String getSerializedLoggedInUserModel() {
        return serializedUser;
    }

    @Override
    public int getLoggedInUserId() {
        return userModel.id;
    }
}
