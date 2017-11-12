package remm.sharedtrip;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

/**
 * Created by Mark on 12.11.2017.
 */

public class EventDetailsActivity extends Activity {

    private ImageView eventPic;
    private TextView eventName;
    private TextView eventLocation;
    private TextView eventCost;
    private TextView eventFreeSpots;
    private TextView eventDescription;
    private EventModel model;
    private Button joinButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        model = new Gson().fromJson(getIntent().getStringExtra("event"), EventModel.class);

        setContentView(R.layout.activity_event_view);

        joinButton = findViewById(R.id.eventViewRequestButton);
        eventPic = findViewById(R.id.eventViewPicture);
        eventName = findViewById(R.id.eventViewLocation);
        eventCost = findViewById(R.id.eventViewCostPerNight);
        eventFreeSpots = findViewById(R.id.eventViewFreeSpots);
        eventDescription = findViewById(R.id.eventViewDescription);
        eventLocation = findViewById(R.id.eventViewLocationGPS);

        eventName.setText(model.getname());
        eventDescription.setText(model.getDescription());
        eventCost.setText(model.getCost()+"€ "+eventCost.getText());
        eventFreeSpots.setText(eventFreeSpots.getText()+": "+model.getSpots());
        eventLocation.setText(eventLocation.getText()+": "+model.getLoc());

        Glide
            .with(this)
            .load(model.getImageLink())
            .into(eventPic);

//        joinButton.setOnClickListener();
    }
}
