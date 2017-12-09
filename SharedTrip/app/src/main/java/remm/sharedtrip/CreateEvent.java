package remm.sharedtrip;
import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import models.CreatorEventModel;
import remm.sharedtrip.MainActivity.FbGoogleUserModel;
import utils.CreateEventUtils;
import utils.CreateEventUtils.EventCreator;
import utils.DatePickerFragment;

import static utils.DebugUtil.doNothing;
import static utils.ValueUtil.notNullOrWhitespace;

public class CreateEvent extends AppCompatActivity implements EventCreator {

    private static final int READ_EXTERNAL_STORAGE_PERMISSION_REQUEST = 120;
    private static final int IMAGE_REQUEST = 700;
    private static final int PIC_CROP = 868;

    EditText title, description, destination, cost, spots;
    Button create;
    Button cancel;
    Button addPicture;
    ImageView imageView;
    CheckBox private_event_state;
    static Boolean private_event;
    static CreatorEventModel model;
    CreateEvent self;
    private FbGoogleUserModel userModel;
    private String apiPrefix;


    private void postEventsToDb() {
        CreateEventUtils.EventCreationTask<String> asyncTask = new CreateEventUtils.EventCreationTask<>(model, apiPrefix, this);
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
        userModel = new Gson().fromJson(getIntent().getStringExtra("user"), FbGoogleUserModel.class);
        apiPrefix = getIntent().getStringExtra("prefix");

        model = new CreatorEventModel("","","", userModel.id);
        setContentView(R.layout.activity_create_event);

        getIntent().setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        imageView = findViewById(R.id.add_picture_preview);
        title = findViewById(R.id.title);
        destination = findViewById(R.id.destination);
        description = findViewById(R.id.description);
        cost = findViewById(R.id.cost);
        spots = findViewById(R.id.spots);
        private_event_state = findViewById(R.id.checkBox3);
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
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View view) {
                tryShowImagePreview();
            }
        });

        create = findViewById(R.id.button4);
        create.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String costString = cost.getText().toString();
                String spotsString = spots.getText().toString();

                model.setName(title.getText().toString());
                model.setDescription(description.getText().toString());
                model.setLoc(destination.getText().toString());
                model.setCost(notNullOrWhitespace(costString) ? Integer.parseInt(costString) : 0);
                model.setSpots(notNullOrWhitespace(spotsString) ? Integer.parseInt(spotsString) : 0);
                model.setPrivate(private_event_state.isChecked());
                postEventsToDb();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        doNothing();
        switch (requestCode) {
            case IMAGE_REQUEST:
                if (resultCode == RESULT_CANCELED) { displayError("Action cancelled"); }
                if (resultCode == RESULT_OK) {
                    final Uri selectedImgUri = data.getData();
                        model.setImageLink(selectedImgUri != null ? selectedImgUri.toString() : null);
                        model.setImageFile(selectedImgUri, this);

                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                                self.getContentResolver(),
                                selectedImgUri);
                        imageView.setImageBitmap(bitmap);
                        if (bitmap.getHeight() > 600) {
                            imageView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    performCrop(selectedImgUri);
                                }
                            });
                            displayMessage("Click on picture to crop (scroll up)");
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                        displayError("Image upload failed");
                    }
                }

                break;
            case PIC_CROP:
                if (data != null) {
                    Uri selectedImgUri = data.getData();
                    imageView.setImageURI(selectedImgUri);
                    model.setImageLink(selectedImgUri != null ? selectedImgUri.toString() : null);
                    model.setImageFile(selectedImgUri, this);
                }
        }
    }

    private void performCrop(Uri picUri) {
        try {
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            // indicate image type and Uri
            cropIntent.setDataAndType(picUri, "image/*");
            // set crop properties here
            cropIntent.putExtra("crop", true);
            // indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            // indicate output X and Y
            cropIntent.putExtra("outputX", 1600);
            cropIntent.putExtra("outputY", 1600);
            // retrieve data on return
//            cropIntent.putExtra("return-data", true);
            // start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, PIC_CROP);
        }
        // respond to users whose devices do not support the crop action
        catch (ActivityNotFoundException anfe) {
            // display an error message
            String errorMessage = "Whoops - your device doesn't support the crop action!";
            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
            toast.show();
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

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void tryShowImagePreview() {

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                        new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                    READ_EXTERNAL_STORAGE_PERMISSION_REQUEST);
        }
        else {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(
                    Intent.createChooser(intent, "Choose Picture"),
                    IMAGE_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case READ_EXTERNAL_STORAGE_PERMISSION_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(
                            Intent.createChooser(intent, "Choose Picture"),
                            1);
                } else {
                    displayError("You denied access");
                }
                return;
            }
        }
    }

    private void displayError(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(
                        CreateEvent.this,
                        message,
                        Toast.LENGTH_SHORT
                ).show();;
            }
        });
    }

    private void displayMessage(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(
                        CreateEvent.this,
                        message,
                        Toast.LENGTH_LONG
                ).show();;
            }
        });
    }

    @Override
    public void onEventCreated() {
        finish();
    }
}
