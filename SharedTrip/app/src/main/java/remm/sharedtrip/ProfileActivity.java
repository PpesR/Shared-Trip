package remm.sharedtrip;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Build;
import android.os.StrictMode;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;

import org.w3c.dom.Text;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import remm.sharedtrip.MainActivity.FbGoogleUserModel;
import utils.RatingUtil;
import utils.RatingUtil.MyRatingsTask;
import utils.RatingUtil.Ratings;

public class ProfileActivity extends AppCompatActivity {

    private Intent ownIntent;
    private TextView hiText;
    private Button editButton;
    private Button saveButton;
    private EditText decription;
    private FbGoogleUserModel userModel;
    private Gson gson = new Gson();
    static ProfileActivity self;
    private boolean isOwnProfile;
    private Ratings ratings;
    private int baseColor;

    TextView thumbsUp;
    TextView thumbsDown;
    TextView heart;
    TextView smile;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // For displaying images
        StrictMode.setThreadPolicy(
                new StrictMode
                        .ThreadPolicy.Builder()
                        .permitAll()
                        .build());

        self = this;
        super.onCreate(savedInstanceState);
        ownIntent = getIntent();
        userModel = gson.fromJson(ownIntent.getStringExtra("user"), FbGoogleUserModel.class);
        setContentView(R.layout.activity_profile);

        baseColor = getResources().getColor(R.color.orangered);

        thumbsUp = findViewById(R.id.profile_thumbs_up);
        thumbsDown = findViewById(R.id.profile_thumbs_down);
        heart = findViewById(R.id.profile_heart);
        smile = findViewById(R.id.profile_smile);

        hiText = findViewById(R.id.profile_hi);
        isOwnProfile = !getIntent().getBooleanExtra("notMine", false);

        ImageView profilePicture = findViewById(R.id.profile_image);

        if (userModel.imageUriString == null) {
            profilePicture.setImageDrawable(getDrawable(R.mipmap.ic_default_user));
        }
        else {
            try {
                URL imageURL = new URL(userModel.imageUriString);
                Bitmap bitmap = BitmapFactory.decodeStream(imageURL.openStream());
                profilePicture.setImageBitmap(bitmap);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        TextView fullname = (TextView) findViewById(R.id.prof_name);
        fullname.setText(fullname.getText()+" "+userModel.name);
        TextView gender = (TextView) findViewById(R.id.prof_gender);
        gender.setText(gender.getText()+" "+userModel.gender);
        TextView bd = (TextView) findViewById(R.id.prof_bd);

        SimpleDateFormat initial;
        if (userModel.birthDate!=null && !userModel.birthDate.equals("null")) {
            if (userModel.birthDate.contains("/"))
                initial = new SimpleDateFormat("MM/dd/yyyy");
            else
                initial = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            SimpleDateFormat correctFormat = new SimpleDateFormat("dd/MM/yyyy");
            Date date = null;
            Calendar calendarBirthday = Calendar.getInstance();

            try {
                date = initial.parse(userModel.birthDate);
                calendarBirthday.setTime(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            Calendar calendarNow = Calendar.getInstance();
            int yearNow = calendarNow.get(Calendar.YEAR);
            int yearBirthday = calendarBirthday.get(Calendar.YEAR);
            int years = yearNow - yearBirthday;

            bd.setText(bd.getText() + "   " + correctFormat.format(date) + "  (" + years + ")");
        }
        else
            bd.setText(bd.getText() + "   " + "hidden");
        decription = findViewById(R.id.description_info);

        if (userModel.description!=null && !userModel.description.equals("null"))
            decription.setText(userModel.description);
        else
            decription.setText("Another amazing traveller");

        saveButton = findViewById(R.id.prof_save);
        editButton = findViewById(R.id.prof_edit);

        if (isOwnProfile) {
            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    decription.setEnabled(true);
                    saveButton.setVisibility(View.VISIBLE);
                    editButton.setVisibility(View.GONE);
                }
            });

            if (userModel.firstName != null)
                hiText.append("  "+userModel.firstName);
            else hiText.append("  "+userModel.name);
        }

        else {
            editButton.setVisibility(View.GONE);
            hiText.setText(userModel.name);
        }

        saveButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String desc_text = decription.getText().toString();
                decription.setEnabled(false);
                saveButton.setVisibility(View.GONE);
                editButton.setVisibility(View.VISIBLE);
                updateDescription(desc_text);
            }
        });

        displayRatings();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void displayRatings() {
        MyRatingsTask<Void> task = new MyRatingsTask<>(
                getResources().getString(R.string.api_address_with_prefix), userModel.id);
        try {
            ratings = task.execute().get();
            if (ratings.thumbsUp>0) {
                thumbsUp.setText(" "+ratings.thumbsUp);
                thumbsUp.setTextColor(baseColor);
                thumbsUp.getCompoundDrawables()[0].setColorFilter(baseColor, PorterDuff.Mode.SRC_ATOP);
            }
            if (ratings.thumbsDown>0) {
                thumbsDown.setText(" "+ratings.thumbsDown);
                thumbsDown.setTextColor(baseColor);
                thumbsDown.getCompoundDrawables()[0].setColorFilter(baseColor, PorterDuff.Mode.SRC_ATOP);
            }
            if (ratings.hearts>0) {
                heart.setText(" "+ratings.hearts);
                heart.setTextColor(baseColor);
                heart.getCompoundDrawables()[0].setColorFilter(baseColor, PorterDuff.Mode.SRC_ATOP);
            }
            if (ratings.smileys>0) {
                smile.setText(" "+ratings.smileys);
                smile.setTextColor(baseColor);
                smile.getCompoundDrawables()[0].setColorFilter(baseColor, PorterDuff.Mode.SRC_ATOP);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void updateDescription(String text) {
        DescChangeTask<Void> task = new DescChangeTask<>(userModel, text);
        task.execute();
    }

    private static class DescChangeTask<Void> extends AsyncTask<Void, Void, Void> {

        private String apiPrefix = self.getString(R.string.api_address_with_prefix);
        private FbGoogleUserModel model;
        private String descriptionText;

        public DescChangeTask(FbGoogleUserModel model, String descriptionText) {
            this.model = model;
            this.descriptionText = descriptionText;
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected final Void doInBackground(final Void... nothings) {

            OkHttpClient client = new OkHttpClient();

            FormBody.Builder formBuilder = new FormBody.Builder()
                    .add("description", descriptionText);

            final Request request = new Request.Builder()
                    .url(apiPrefix+"/user/"+model.id)
                    .put(formBuilder.build())
                    .build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String bodyString = response.body().string();
                        int len = bodyString.length();
                        model.description = descriptionText;

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            return null;
        }
    }
}