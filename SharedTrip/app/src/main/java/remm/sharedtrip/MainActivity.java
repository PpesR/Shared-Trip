package remm.sharedtrip;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ProgressBar;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Set;

import utils.UserAccountUtil.UserActivityHandle;
import utils.UserAccountUtil.UserCheckCallback;
import utils.UserAccountUtil.UserCheckingTask;
import utils.UserAccountUtil.UserRegistrationCallback;
import utils.UserAccountUtil.UserRegistrationTask;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MainActivity extends FragmentActivity implements UserActivityHandle {

    private static final int RC_SIGN_IN = 613;
    CallbackManager callbackManager;
    private static MainActivity self;
    private static FbGoogleUserModel model;
    private Intent browseEvents;
    private static GoogleSignInClient mGoogleSignInClient;
    private GoogleApiClient mGoogleApiClient;
    private SignInButton googleButton;
    private LoginButton loginButton;
    private ProgressBar progressBar;
    private GoogleSignInAccount account;
    private String apiPrefix;

    public static GoogleSignInClient getGoogleSignInClient() { return mGoogleSignInClient; }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        self = this;

        apiPrefix = getResources().getString(R.string.api_address_with_prefix);

        /*
         * Mark: Needed for getting pictures from FB/Google/elsewhere.
         * TODO: move to the page that displays the pictures
         * Until then, DO NOT REMOVE
         */
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.indeterminateBar);
        progressBar.setVisibility(GONE);

        /* Facebook log in */
        callbackManager = CallbackManager.Factory.create();

        loginButton = findViewById(R.id.fb_login_button);
        loginButton.setReadPermissions(Arrays.asList("public_profile", "email", "user_birthday", "user_friends"));

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { hideLogInButtons(); }});

        loginButton.registerCallback(callbackManager, facebookCallback);


        /* Google log in */
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(Scopes.PLUS_ME))
                .requestEmail()
                .requestProfile()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        googleButton = findViewById(R.id.google_sign_in_button);
        googleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });


        /* Handling existing users*/

        // Facebook user token exists (already logged in)
        if (AccessToken.getCurrentAccessToken() != null) {
            GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {

                @Override
                public void onCompleted(JSONObject user, GraphResponse graphResponse) {
                    tryLogInExistingUser(null, user.optString("id"));
                }

            }).executeAsync();
        }
        else {
            account = GoogleSignIn.getLastSignedInAccount(this);

            // Google user has already logged in
            if (account != null) {
                tryLogInExistingUser(account.getId(), null);
            }
        }
    }

    private void tryLogInExistingUser(String gId, String fbId) {
        UserCheckingTask<Void> task = new UserCheckingTask<>(apiPrefix, new UserCheckCallback(this), gId, fbId);
        task.execute();
    }

    private void postUserToDb() {
        UserRegistrationTask<Void> asyncTask = new UserRegistrationTask<>(
                apiPrefix,
                model,
                new UserRegistrationCallback(this));
        asyncTask.execute();
    }

    @Override
    public void fillModelFromJson(JSONObject obj) {

        Profile current = Profile.getCurrentProfile();
        try {
            model.id = obj.getInt("id");
            model.name = getNullSafe(obj.getString("name"));
            model.description = getNullSafe(obj.getString("user_desc"));
            model.imageUri = getNullSafe(obj.getString("user_pic"));
            model.gender = getNullSafe(obj.getString("gender"));
            redirect();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {

            // The Task returned from this call is always completed, no need to attach a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
        else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            account = completedTask.getResult(ApiException.class);
            Uri photoUri = account.getPhotoUrl();

            if (model == null) model = new FbGoogleUserModel();

            model.name = account.getDisplayName();
            model.googleId = account.getId();
            model.imageUri = photoUri == null ? "null" : photoUri.toString();

            Set<Scope> scopes = account.getGrantedScopes();
            postUserToDb();

        } catch (ApiException e) {

            // Google log in failed
            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(this);
            }
            builder.setTitle("Log In Failed")
                    .setMessage("Something went wrong... Make sure you are logged in to Google on your device!")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

    @Override
    public void onUserCheckReady(FbGoogleUserModel checkedModel) {
        if (checkedModel == null) {
            if (model.fbId != null) {
                LoginManager.getInstance().logOut();
                showLogInButtons();
            }

            if (model.googleId != null){
                mGoogleSignInClient.signOut()
                    .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            showLogInButtons();
                        }
                });
            }

        } else {
            model = checkedModel;
            hideLogInButtons();
            redirect();
        }
    }

    private void redirect() {
        if (browseEvents==null) browseEvents = new Intent(self, BrowseEvents.class);

        model.firstName = model.hasFacebook() ? Profile.getCurrentProfile().getFirstName() : account.getGivenName();
        browseEvents.putExtra("user", new Gson().toJson(model));
        startActivity(browseEvents);
    }

    public static class FbGoogleUserModel implements Serializable {

        public String fbId = null;
        public String googleId = null;
        public int id;
        public String name;
        public String gender;
        public String description;
        public String imageUri;
        String firstName;

        String birthDate;
        public boolean hasGoogle() { return googleId != null; }

        public boolean hasFacebook() { return fbId != null; }
    }

    private FacebookCallback<LoginResult> facebookCallback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            GraphRequest request = GraphRequest.newMeRequest(
                    loginResult.getAccessToken(),
                    new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(JSONObject object, GraphResponse response) {
                            try {
                                model = new FbGoogleUserModel();
                                model.fbId = object.optString("id");
                                model.name = object.getString("name");
                                model.gender = object.getString("gender");
                                postUserToDb();

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,name,email,gender,age_range");
            request.setParameters(parameters);
            request.executeAsync();
        }
        @Override
        public void onCancel() { showLogInButtons(); }

        @Override
        public void onError(FacebookException e) { showLogInButtons(); }
    };

    private void showLogInButtons() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(GONE);
                googleButton.setVisibility(VISIBLE);
                loginButton.setVisibility(VISIBLE);
            }
        });
    }

    private void hideLogInButtons() {
        runOnUiThread(new Runnable() {
              @Override
              public void run() {
                  progressBar.setVisibility(VISIBLE);
                  googleButton.setVisibility(GONE);
                  loginButton.setVisibility(GONE);
              }
        });
    }

    @Override
    protected void onResume() {

        super.onResume();
        showLogInButtons();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onDestroy() { super.onDestroy(); }

    public static String getNullSafe(String in) {
        return in.equals("null") || in.equals("") ? null : in;
    }
}


