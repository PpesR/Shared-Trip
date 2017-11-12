package remm.sharedtrip;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

import com.facebook.Profile;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import models.EventModel;
import models.UserEventModel;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import utils.DatePickerFragment;

public class CreateEvent extends AppCompatActivity {

    static EditText title, description, destination, cost, spots;
    Button create;
    Button cancel;
    Button addPicture;
    static ImageView imageView;
    static String creator_id;
    CheckBox private_event_state;
    static Boolean private_event;
    static UserEventModel model;
    static CreateEvent self;


    private void postEventsToDb() {
        EventCreationTask<String> asyncTask = new EventCreationTask<>();
        try {
            String s = asyncTask.execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        self = this;
        model = new UserEventModel();
        setContentView(R.layout.activity_create_event);

        Profile currentProfile = Profile.getCurrentProfile();

        creator_id = currentProfile.getId();
        imageView = findViewById(R.id.add_picture_preview);
        title = findViewById(R.id.title);
        destination = findViewById(R.id.destination);
        description = findViewById(R.id.description);
        cost = findViewById(R.id.cost);
        spots = findViewById(R.id.spots);
        private_event_state =  findViewById(R.id.checkBox3);
        private_event =  private_event_state.isEnabled();



        cancel = findViewById(R.id.button3);
        cancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                finish();

            }
        });

        addPicture = findViewById(R.id.add_picture_button);
        addPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(
                        Intent.createChooser(intent, "Choose Picture"),
                        1);
            }
        });

        create = findViewById(R.id.button4);
        create.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                postEventsToDb();
                //finish();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(resultCode==RESULT_CANCELED)
        {
            // action cancelled
        }
        if(resultCode==RESULT_OK)
        {
            Uri selectedimg = data.getData();
            try {
                imageView.setImageBitmap(
                        MediaStore.Images.Media.getBitmap(
                                this.getContentResolver(),
                                selectedimg));
                model.setImageLink(selectedimg.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void showStartDatePickerDialog(View v) {
        DatePickerFragment newFragment = new DatePickerFragment();
        newFragment.setModel(model,'s', this);
        newFragment.show(getFragmentManager(), "datePicker");
    }

    public void showEndDatePickerDialog(View v) {
        DatePickerFragment newFragment = new DatePickerFragment();
        newFragment.setModel(model,'e', this);
        newFragment.show(getFragmentManager(), "datePicker");
    }

    public void onModelChanged () {
        if (model.getStartDate()!=null) {
            ((Button) findViewById(R.id.start_date)).setHint(model.getStartDate());
        }
        if (model.getEndDate()!=null) {
            ((Button) findViewById(R.id.end_date)).setHint(model.getEndDate());
        }
    }

    private static class EventCreationTask<String> extends AsyncTask<EventModel, Void, String> {

        @SafeVarargs
        @Override
        protected final String doInBackground(EventModel... events) {
            OkHttpClient client = new OkHttpClient();
            FormBody.Builder formBuilder = new FormBody.Builder()
                    .add("user", creator_id)
                    .add("location", destination.getText().toString())
                    .add("name", title.getText().toString())
                    .add("description", description.getText().toString())
                    .add("total_cost", cost.getText().toString())
                    .add("spots", spots.getText().toString())
                    .add("start_date", model.getStartDate())
                    .add("end_date", model.getEndDate())
                    .add("private", private_event ? "1" : "0")
                    .add("picture", model.getImageLink());

            final Request request = new Request.Builder()
                    .url("http://146.185.135.219/requestrouter.php?hdl=event")
                    .post(formBuilder.build())
                    .build();
            Call call = client.newCall(request);

            call.enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    self.tempDisplay(e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        JSONArray array = new JSONArray(response.body().string());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });

            return null;
        }
    }

    private void tempDisplay(String s) {
        String s2;
    }



}
