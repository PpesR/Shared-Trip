package remm.sharedtrip;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import remm.sharedtrip.MainActivity.FbGoogleUserModel;
import utils.BottomNavigationViewHelper;

public class ProfileView extends AppCompatActivity {

    private Intent ownIntent;
    private TextView hi_text;
    private Button edit;
    private Button save;
    private EditText desc_field;
    private BottomNavigationView bottomNavigationView;
    private FbGoogleUserModel userModel;
    private Gson gson = new Gson();
    static  ProfileView self;

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

        hi_text = (TextView) findViewById(R.id.profile_hi);
        hi_text.append("  "+userModel.firstName);
        ImageView prof_pic = (ImageView) findViewById(R.id.profile_image);

        if (userModel.imageUriString == null) {
            prof_pic.setImageDrawable(getDrawable(R.mipmap.ic_default_user));
        }
        else {
            try {
                URL imageURL = new URL(userModel.imageUriString);
                Bitmap bitmap = BitmapFactory.decodeStream(imageURL.openStream());
                prof_pic.setImageBitmap(bitmap);
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
        desc_field = (EditText) findViewById(R.id.description_info);

        if (userModel.description!=null && !userModel.description.equals("null"))
            desc_field.setText(userModel.description);
        else
            desc_field.setText("Another amazing traveller");

        save = (Button) findViewById(R.id.prof_save);
        edit = (Button) findViewById(R.id.prof_edit);
        edit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                desc_field.setEnabled(true);
                save.setVisibility(View.VISIBLE);
                edit.setVisibility(View.GONE);
            }
        });
        save.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String desc_text = desc_field.getText().toString();
                desc_field.setEnabled(false);
                save.setVisibility(View.GONE);
                edit.setVisibility(View.VISIBLE);
                updateDescription(desc_text);
            }
        });

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);

        BottomNavigationViewHelper
                .disableShiftMode(bottomNavigationView);

        MenuItem profileItem = bottomNavigationView.getMenu()
                .findItem(R.id.bottombaritem_profile);
        profileItem.setTitle(userModel.firstName);
        profileItem.setChecked(false);

        MenuItem eventItem = bottomNavigationView.getMenu()
                .findItem(R.id.bottombaritem_profile);
        eventItem.setChecked(false);

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.bottombaritem_events:
                                finish();
                                return true;
                            case R.id.bottombaritem_friends:
                                finish();
                                Intent friendsViewActivity = new Intent(ProfileView.this, FriendsViewActivity.class);
                                startActivity(friendsViewActivity);
                                return true;
                            case R.id.bottombaritem_stats:
                                finish();
                                Intent statsViewActivity = new Intent(ProfileView.this, StatsViewActivity.class);
                                startActivity(statsViewActivity);
                                return true;
                            case R.id.bottombaritem_profile:
                                finish();
                                Intent adminViewActivity = new Intent(ProfileView.this, AdminEventActivity.class);
                                startActivity(adminViewActivity);
                                return true;
                        }
                        return true;
                    }
                });

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