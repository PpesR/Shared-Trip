package remm.sharedtrip;
import android.Manifest;
import android.content.ActivityNotFoundException;
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
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

import models.CreatorEventModel;
import remm.sharedtrip.MainActivity.FbGoogleUserModel;
import utils.CreateEventUtil.EventCreationTask;
import utils.CreateEventUtil.EventCreator;
import fragments.DatePickerFragment;

import static utils.DebugUtil.doNothing;
import static utils.UtilBase.notNullOrWhitespace;

public class CreateEventActivity extends AppCompatActivity implements EventCreator {

    private static final int READ_EXTERNAL_STORAGE_PERMISSION_REQUEST = 120;
    private static final int IMAGE_REQUEST = 700;
    private static final int PIC_CROP = 868;

    private TextView startDateInput;
    private TextView endDateInput;

    EditText title, description, destination, cost, spots;
    Button create;
    Button cancel;
    Button addPicture;
    ImageView imageView;
    CheckBox privateEvent;
    static CreatorEventModel model;
    CreateEventActivity self;
    private FbGoogleUserModel userModel;

    SimpleDateFormat dateFormatUTC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat displayFormat = new SimpleDateFormat("d MMM HH:mm");

    private void postEventsToDb() {
        EventCreationTask<String> asyncTask = new EventCreationTask<>(model, this);
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
        dateFormatUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
        displayFormat.setTimeZone(TimeZone.getDefault());
        userModel = new Gson().fromJson(getIntent().getStringExtra("user"), FbGoogleUserModel.class);

        model = new CreatorEventModel("","","", userModel.id);
        setContentView(R.layout.activity_create_event);

        getIntent().setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startDateInput = findViewById(R.id.start_date_input);
        startDateInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) showStartDatePickerDialog(view);
            }
        });
        endDateInput = findViewById(R.id.end_date_input);
        endDateInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) showEndDatePickerDialog(view);
            }
        });

        imageView = findViewById(R.id.add_picture_preview);
        title = findViewById(R.id.title);
        destination = findViewById(R.id.destination);
        description = findViewById(R.id.description);
        cost = findViewById(R.id.cost);
        spots = findViewById(R.id.spots);
        privateEvent = findViewById(R.id.checkBox3);

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
                String
                    titleString = title.getText().toString().trim(),
                    costString = cost.getText().toString().trim(),
                    spotsString = spots.getText().toString().trim(),
                    descString = description.getText().toString().trim(),
                    locString = destination.getText().toString().trim();

                if (!notNullOrWhitespace(locString)) {
                    destination.setError("Location is required!");
                }

                else if (!notNullOrWhitespace(model.getStartDate())) {
                    Toast.makeText(self, "Start time is required!", Toast.LENGTH_SHORT).show();
                }
                else if (!notNullOrWhitespace(model.getEndDate())) {
                    Toast.makeText(self, "End time is required!", Toast.LENGTH_SHORT).show();
                }
                else try {
                        if (dateFormatUTC.parse(model.getEndDate()).before(dateFormatUTC.parse(model.getStartDate()))){
                            Toast.makeText(self, "End time must be after start time!", Toast.LENGTH_SHORT).show();
                        }
                        else if (!notNullOrWhitespace(titleString)) {
                            title.setError("Trip Title is required!");
                        }
                        else if (!notNullOrWhitespace(descString)) {
                            description.setError("Trip description is required!");
                        }
                        else if (!notNullOrWhitespace(costString)) {
                            cost.setError("Total cost is required!");
                        }
                        else if (!notNullOrWhitespace(spotsString) || Integer.parseInt(spotsString) < 1) {
                            spots.setError("The amount of free spots is required!");
                        }
                        else {
                            model.setName(titleString);
                            model.setDescription(descString);
                            model.setLoc(locString);
                            model.setCost(Integer.parseInt(costString));
                            model.setSpots(Integer.parseInt(spotsString));
                            model.setPrivate(privateEvent.isChecked());
                            postEventsToDb();
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
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
                        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            imageView.setImageTintList(null);
                        }
                        imageView.setImageBitmap(bitmap);
                        if (bitmap.getHeight() > 600) {
                            imageView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    performCrop(selectedImgUri);
                                }
                            });
                            displayMessage("Your image is quite large. You can click on it to crop!");
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
            cropIntent.putExtra("aspectX", 3);
            cropIntent.putExtra("aspectY", 2);
            // indicate output X and Y
            cropIntent.putExtra("outputX", 900);
            cropIntent.putExtra("outputY", 900);
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

    public void onModeDatelChanged(final char whichDate) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (whichDate=='s' && model.getStartDate() != null) {
                        Date actualDate = dateFormatUTC.parse(model.getStartDate());
                        startDateInput.setText(displayFormat.format(actualDate));
                        startDateInput.clearFocus();
                    }
                    if (whichDate=='e' && model.getEndDate() != null) {
                        Date actualDate = dateFormatUTC.parse(model.getEndDate());
                        endDateInput.setText(displayFormat.format(actualDate));
                        endDateInput.clearFocus();
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
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
                        CreateEventActivity.this,
                        message,
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void displayMessage(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(
                        CreateEventActivity.this,
                        message,
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    @Override
    public void onEventCreated() {
        Intent data = new Intent();
        data.putExtra("success", true);
        setResult(RESULT_OK, data);
        finish();
    }
}
