package remm.sharedtrip;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import static android.view.View.VISIBLE;

public class MainActivity extends FragmentActivity {

    CallbackManager callbackManager;
    ProfileTracker profileTracker;
    private MainActivity self;
    private String email;
    private String birthday;
    private String gender;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        self = this;

        /*
         * Mark: Needed for getting pictures from FB/Google/elsewhere.
         * TODO: move to the page that displays the pictures
         * Until then, DO NOT REMOVE
         */
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_main);

        callbackManager = CallbackManager.Factory.create();
        final LoginButton loginButton = findViewById(R.id.fb_login_button);
        loginButton.setReadPermissions(Arrays.asList(
                "public_profile", "email", "user_birthday", "user_friends"));

        /* Progress bar */
        final ProgressBar progressBar = findViewById(R.id.indeterminateBar);
        progressBar.setVisibility(View.GONE);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginButton.setVisibility(View.GONE);
                progressBar.setVisibility(VISIBLE);
            }
        });
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            /*
             * Mark: some default shit. DO NOT REMOVE
             */
            @Override
            public void onSuccess(LoginResult loginResult) {
                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                Log.v("LoginActivity", response.toString());

                                // Application code
                                try {
                                    email = object.getString("email");
                                    birthday = object.getString("birthday");
                                    gender = object.getString("gender");

                                    /*
                                     * Mark: Redirect user who's been already logged in to the event browser.
                                     * Otherwise they'd just see a logout button on the current page.

                                    if (AccessToken.getCurrentAccessToken() != null) redirect();*/
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender,birthday");
                request.setParameters(parameters);
                request.executeAsync();
            }
            @Override
            public void onCancel() {
                loginButton.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError(FacebookException e) {
                loginButton.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }
        });

        profileTracker = new ProfileTracker() {

            /*
             * Mark: Changes the page after the login action returns new user profile data.
             * Could not be done in onSuccess() method above because there we don't have that new profile yet (tested)
             */
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                if (currentProfile != null) redirect();
            }
        };

        if (AccessToken.getCurrentAccessToken() != null)
            redirect();
    }

    /*
     * The transition between Main view and Event Browsing view.
     * Passes your login information like name and picture (only FB for now) to the event browser.
     */
    private void redirect() {

        Intent browseEvents = new Intent(self, BrowseEvents.class); // The thing that performs redirection
        Profile currentProfile = Profile.getCurrentProfile();   // The FB profile data of logged in user

        browseEvents.putExtra("name",currentProfile .getName()); // The data that we want to send to the next view
        browseEvents.putExtra("first_name",currentProfile .getFirstName());
        browseEvents.putExtra("gender", gender);
        // Add more data if needed, e.g:
        // browseEvents.putExtra("imageUrl", currentProfile.getProfilePictureUri(50,50).toString); // 50,50 are image dimensions

        startActivity(browseEvents);
    }

    /*
     * Mark: some default shit. DO NOT REMOVE
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    /*
     * Mark: some default shit. DO NOT REMOVE
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        profileTracker.stopTracking();
    }
}


