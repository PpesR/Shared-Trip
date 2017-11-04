package remm.sharedtrip;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

public class MainActivity extends FragmentActivity {

    CallbackManager callbackManager;
    ProfileTracker profileTracker;
    private MainActivity self;

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
        LoginButton loginButton = findViewById(R.id.fb_login_button);
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            /*
             * Mark: some default shit. DO NOT REMOVE
             */
            @Override
            public void onSuccess(LoginResult loginResult) {}
            @Override
            public void onCancel() {}
            @Override
            public void onError(FacebookException e) {}
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

        /*
         * Mark: Redirect user who's been already logged in to the event browser.
         * Otherwise they'd just see a logout button on the current page.
         */
        if (AccessToken.getCurrentAccessToken() != null) redirect();

    }

    /*
     * The transition between Main view and Event Browsing view.
     * Passes your login information like name and picture (only FB for now) to the event browser.
     */
    private void redirect() {

        Intent browseEvents = new Intent(self, BrowseEvents.class); // The thing that performs redirection
        Profile currentProfile = Profile.getCurrentProfile();   // The FB profile data of logged in user

        browseEvents.putExtra("name",currentProfile .getName()); // The data that we want to send to the next view
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


