package remm.sharedtrip;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import adapters.JoinRequestsAdapter;
import adapters.JoinRequestsAdapter.JoinRequestManager;
import remm.sharedtrip.MainActivity.FbGoogleUserModel;

public class RequestManagementActivity extends AppCompatActivity implements JoinRequestManager {

    private List<JoinRequestsAdapter.RequestUserModel> requesters = new ArrayList<>();
    private RecyclerView recyclerView;
    private JoinRequestsAdapter adapter;
    private LinearLayoutManager layoutManager;
    private FbGoogleUserModel userModel;
    private Gson gson = new Gson();
    private int eventId;
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent myIntent = getIntent();
        userModel = gson.fromJson(myIntent.getStringExtra("user"), FbGoogleUserModel.class);
        eventId = myIntent.getIntExtra("event", 0);
        ArrayList<String> temp = myIntent.getStringArrayListExtra("requesters");
        for (String s : temp ) {
            requesters.add(gson.fromJson(s, JoinRequestsAdapter.RequestUserModel.class));
        }

        adapter = new JoinRequestsAdapter(this, this, requesters);
        layoutManager = new LinearLayoutManager(this);

        setContentView(R.layout.activity_request_management);
        recyclerView = findViewById(R.id.request_management_results);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    public void returnResult() {
        Intent intent = new Intent();
        intent.putExtra("handled", count);
        intent.putExtra("event", eventId);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onBackPressed() { returnResult(); }

    @Override
    public int getAdminId() { return userModel.id; }

    @Override
    public int getEventId() { return eventId; }

    @Override
    public void increaseCount() { count++; }
}
