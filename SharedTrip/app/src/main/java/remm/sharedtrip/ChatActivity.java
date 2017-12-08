package remm.sharedtrip;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

import models.UserEventModel;
import remm.sharedtrip.MainActivity.FbGoogleUserModel;
import utils.MessageUtil;
import utils.MessageUtil.HistoryRetrievalTask;
import utils.MessageUtil.MessageSaveResponse;

import static utils.ValueUtil.notNull;

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
    private List<String> messages;
    private ArrayAdapter<String> adapter;
    private String messageText;
    private FbGoogleUserModel userModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userModel = new Gson().fromJson(getIntent().getStringExtra("user"), FbGoogleUserModel.class);

        fm = FirebaseMessaging.getInstance();
        LocalBroadcastManager.getInstance(this).registerReceiver((mMessageReceiver),
                new IntentFilter("Message received")
        );

        event = new Gson().fromJson(getIntent().getStringExtra("event"), UserEventModel.class);
        messageTopic = event.getId()+"-"+event.getLoc().toLowerCase().replaceAll("[^a-z]", "");
        fm.subscribeToTopic(messageTopic);
        fm.subscribeToTopic("0-test-topic");

        setContentView(R.layout.temp_chat_layout);
        messages = new ArrayList<>();
        adapter = new ArrayAdapter<>(ChatActivity.this, R.layout.single_chat_message, messages);
        listView = findViewById(R.id.chat_message_list);
        listView.setAdapter(adapter);
        getMessageHistory();

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
        sendButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                trySendMessage();
            }
        });
    }

    private void trySendMessage() {
        messageText = messageInput.getText().toString();
        messageInput.setText("");
        hideSoftKeyboard(ChatActivity.this);
        Date now = new Date();

        MessageUtil.MessageSavingTask<Void> task =
                new MessageUtil.MessageSavingTask<>(
                        getResources().getString(R.string.api_address_with_prefix),
                        messageText,
                        messageTopic,
                        userModel.id+"",
                        now,
                        event.getId());
        try {
            MessageSaveResponse response = task.execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void addMessage(final String m) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.add(m);
                adapter.notifyDataSetChanged();
                listView.setSelection(adapter.getCount() - 1);
            }
        });
    }

    private void getMessageHistory() {
        HistoryRetrievalTask<Void> task = new HistoryRetrievalTask<>(
                userModel.id, event.getId(), getResources().getString(R.string.api_address_with_prefix));
        try {
            final List<String> history = task.execute().get();
            if (notNull(history)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.addAll(history);
                        adapter.notifyDataSetChanged();
                        listView.setSelection(adapter.getCount() - 1);
                    }
                });

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private ListView listView;
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String json = intent.getStringExtra("messageData");
            try {
                JSONObject obj = new JSONObject(json);
                String dateStr = obj.getString("time");
                ChatActivity.this.addMessage(dateStr+"\r\n"+obj.getString("sender_name")+": "+obj.getString("message"));

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
}
