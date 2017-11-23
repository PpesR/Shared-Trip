package remm.sharedtrip;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import models.CreatorEventModel;
import models.EventModel;
import models.UserEventModel;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import utils.CreateEventUtils;
import utils.DatePickerFragment;

public class CreateEvent extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 120;

    static EditText title, description, destination, cost, spots;
    String test;
    Button create;
    Button cancel;
    Button addPicture;
    static ImageView imageView;
    static int creator_id;
    CheckBox private_event_state;
    static Boolean private_event;
    static CreatorEventModel model;
    static CreateEvent self;

    private Uri imageUri;


    private void postEventsToDb() {
        CreateEventUtils.EventCreationTask<String> asyncTask = new CreateEventUtils.EventCreationTask<>(model);
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
        model = new CreatorEventModel("","","", BrowseEvents.fbUserModel.id);
        setContentView(R.layout.activity_create_event);

        getIntent().setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        creator_id = BrowseEvents.fbUserModel.id;
        imageView = findViewById(R.id.add_picture_preview);
        title = findViewById(R.id.title);
        destination = findViewById(R.id.destination);
        description = findViewById(R.id.description);
        cost = findViewById(R.id.cost);
        spots = findViewById(R.id.spots);
        private_event_state =  findViewById(R.id.checkBox3);
        private_event =  private_event_state.isChecked();



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
                model.setName(title.getText().toString());
                model.setDescription(description.getText().toString());
                model.setLoc(destination.getText().toString());
                model.setCost(Integer.parseInt(cost.getText().toString()));
                model.setSpots(Integer.parseInt(spots.getText().toString()));
                model.setPrivate(private_event_state.isChecked());
                postEventsToDb();
                finish();
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
            Uri selectedImgUri = data.getData();
            try {
                imageView.setImageBitmap(
                        MediaStore.Images.Media.getBitmap(
                                this.getContentResolver(),
                                selectedImgUri));
            model.setImageLink(selectedImgUri.toString());
            model.setImageFile(selectedImgUri, this);
            this.imageUri = selectedImgUri;
//            trySendImage();
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

    private void trySendImage() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            }
        }
        else {
            CreateEventUtils.ImageUploadTask<Void> task =
                    new CreateEventUtils.ImageUploadTask<>(
                            new CreateEventUtils.ImageUploadCallback(this),
                            imageUri,
                            this);
            task.execute();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay!
                    CreateEventUtils.ImageUploadTask<Void> task =
                            new CreateEventUtils.ImageUploadTask<>(
                                    new CreateEventUtils.ImageUploadCallback(this),
                                    imageUri,
                                    this);
                    task.execute();

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other permissions this app might request
        }
    }

    public void onImageUploaded(final Bitmap newImage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageView.setImageBitmap(newImage);
            }
        });
    }

}
