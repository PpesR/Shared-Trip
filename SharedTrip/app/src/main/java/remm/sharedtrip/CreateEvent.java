package remm.sharedtrip;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.XmlRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import Database.SharedTripDbHelper;


public class CreateEvent extends AppCompatActivity {

    SharedTripDbHelper db;

    EditText title;
    EditText description, destination;
    Button create;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);


        db = new SharedTripDbHelper(this);

        title = (EditText) findViewById(R.id.title);
        destination = (EditText) findViewById(R.id.destination);
        description = (EditText) findViewById(R.id.description);

        create = (Button) findViewById(R.id.button4);
        create.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String title_text, destination_text, description_text;

                title_text = title.getText().toString();
                destination_text = description.getText().toString();
                description_text = destination.getText().toString();

                db.insertEvent(title_text, destination_text, description_text);
                finish();

            }
        });

    }

}
