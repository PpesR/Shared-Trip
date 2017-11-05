package remm.sharedtrip;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

import Database.SharedTripDbHelper;

public class CreateEvent extends AppCompatActivity {

    EditText title, description, destination, start_date, end_date;
    Button create;
    Button cancel;
    Button addPicture;
    ImageView imageView;
    DatePicker datePickerbegin;
    DatePicker datePickerend;
    EventModel model;

    SharedTripDbHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = new EventModel();
        setContentView(R.layout.activity_create_event);

    //    df = new SimpleDateFormat("dd-MM-yyyy");
        db = new SharedTripDbHelper(this);
        imageView = (ImageView) findViewById(R.id.add_picture_preview);
        title = (EditText) findViewById(R.id.title);
        destination = (EditText) findViewById(R.id.destination);
        description = (EditText) findViewById(R.id.description);
     //   start_date = (EditText) findViewById(R.id.start_date);
      //  end_date = (EditText) findViewById(R.id.end_date);


        cancel = (Button) findViewById(R.id.button3);
        cancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                finish();

            }
        });

        addPicture = (Button) findViewById(R.id.add_picture_button);
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

        create = (Button) findViewById(R.id.button4);
        create.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String title_text, destination_text, description_text;
                String start_date_input, end_date_input;

                //start_date_input = start_date.getText().toString();
                //end_date_input = end_date.getText().toString();

                title_text = title.getText().toString();
                destination_text = description.getText().toString();
                description_text = destination.getText().toString();

                db.insertEvent(title_text, destination_text, description_text);
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
            Uri selectedimg = data.getData();
            try {
                imageView.setImageBitmap(
                        MediaStore.Images.Media.getBitmap(
                                this.getContentResolver(),
                                selectedimg));
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



}
