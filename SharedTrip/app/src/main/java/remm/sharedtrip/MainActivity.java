package remm.sharedtrip;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

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
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import utils.UserAccountUtil.*;

import static utils.UtilBase.*;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MainActivity extends FragmentActivity implements UserActivityHandle {

    private static final int RC_SIGN_IN = 613;
    CallbackManager callbackManager;
    private static MainActivity self;
    private static FbGoogleUserModel model;
    private Intent explorationActivity;
    private static GoogleSignInClient mGoogleSignInClient;
    private SignInButton googleButton;
    private LoginButton loginButton;
    private ProgressBar progressBar;
    private GoogleSignInAccount googleAccount;
    private AccessToken facebookToken;

    public static Task<Void> signOutOfGoogle() { return mGoogleSignInClient.signOut(); }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        self = this;

        //IMPORTANT FOR DISPLAYING IMAGES! DO NOT REMOVE!
        StrictMode.setThreadPolicy(
                new StrictMode
                        .ThreadPolicy.Builder()
                        .permitAll()
                        .build());

        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.indeterminateBar);
        progressBar.setVisibility(GONE);

        /* Facebook log in */
        callbackManager = CallbackManager.Factory.create();

        loginButton = findViewById(R.id.fb_login_button);
        loginButton.setReadPermissions(Arrays.asList("public_profile", "email", "user_friends"));
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { hideLogInButtons(); }});
        loginButton.registerCallback(callbackManager, facebookCallback);


        /* Google log in */
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getResources().getString(R.string.default_client_id))
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
                hideLogInButtons();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });
    }

    private void tryLogInExistingUser(String googleId, String facebookId) {
        UserCheckingTask<Void> task = new UserCheckingTask<>(
                API_PREFIX,
                new UserCheckCallback(this),
                googleId,
                facebookId);
        task.execute();
    }

    private void postUserToDb() {
        UserRegistrationTask<Void> asyncTask = new UserRegistrationTask<>(
                API_PREFIX,
                model,
                new UserRegistrationCallback(this));
        asyncTask.execute();
    }

    @Override
    public void fillModelFromJson(JSONObject obj) {

        try {
            model.id = obj.getInt("id");
            model.name = valueOrNull(obj.getString("name"));
            model.description = valueOrNull(obj.getString("user_desc"));
            model.imageUriString = valueOrNull(obj.getString("user_pic"));
            model.gender = valueOrNull(obj.getString("gender"));
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

            // The Task returned from this call is always completed
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
        else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            googleAccount = completedTask.getResult(ApiException.class);
            Uri photoUri = googleAccount.getPhotoUrl();

            if (userModelIsSet()) model = new FbGoogleUserModel();

            model.name = googleAccount.getDisplayName();
            model.googleId = googleAccount.getId();
            model.imageUriString = toStringNullSafe(photoUri);

            postUserToDb();

        } catch (ApiException e) { displayAuthError(); }
    }

    @Override
    public void onUserCheckReady(FbGoogleUserModel checkedModel) {
        if (isNull(checkedModel)) { // Logged in user is no longer in database -> force log out

            if (model.hasFacebook()) {
                LoginManager.getInstance().logOut();
                showLogInButtons();
            }

            if (model.hasGoogle()){
                mGoogleSignInClient.signOut()
                    .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            showLogInButtons();
                        }
                });
            }

        } else { // Signed in user is present in database
            model = checkedModel;
            hideLogInButtons();
            updateFriendsAndRedirect();
        }
    }

    private void redirect() {
        Profile currentFbProfile = Profile.getCurrentProfile();

        if (!model.hasFacebook() && !model.hasGoogle() || bothAreNull(currentFbProfile, googleAccount)) {
            showLogInButtons();
            return;
        }

        if (isNull(explorationActivity)) {
            explorationActivity = new Intent(self, ExplorationActivity.class);
        }

        if (model.hasFacebook() && notNull(currentFbProfile)) {
            model.firstName = currentFbProfile.getFirstName();

        } else if (model.hasGoogle() && notNull(googleAccount)) {
            model.firstName = googleAccount.getGivenName();

        } else {
            model.firstName = "You";
        }

        explorationActivity.putExtra("user", new Gson().toJson(model));
        startActivity(explorationActivity);
    }

    public static class FbGoogleUserModel implements Serializable {

        public int id;

        public String facebookId = null;
        public String googleId = null;
        public String name;
        public String gender;
        public String description;
        public String imageUriString;

        public int ageMin = -1;
        public int ageMax = -1;

        final public Set<String> facebookFriends = new HashSet<>();

        public String firstName;
        public String birthDate;

        public boolean hasGoogle() { return googleId != null; }
        public boolean hasFacebook() { return facebookId != null; }
    }

    private FacebookCallback<LoginResult> facebookCallback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            final AccessToken token = loginResult.getAccessToken();
            GraphRequest request = GraphRequest.newMeRequest(
                    token,
                    new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(JSONObject object, GraphResponse response) {
                            try {
                                model = new FbGoogleUserModel();
                                model.facebookId = object.optString("id");
                                model.name = object.getString("name");
                                model.gender = object.getString("gender");
                                JSONObject ageRange = object.getJSONObject("age_range");

                                if (ageRange.has("min")) { model.ageMin = ageRange.getInt("min"); }
                                if (ageRange.has("max")) { model.ageMax = ageRange.getInt("max"); }

                                GraphRequest request = GraphRequest.newMyFriendsRequest(token, onLogInFriendsResponse);
                                Bundle parameters = new Bundle();
                                parameters.putString("limit", "999");
                                request.setParameters(parameters);
                                request.executeAsync();

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

    private GraphRequest.GraphJSONArrayCallback onLogInFriendsResponse =
            new GraphRequest.GraphJSONArrayCallback() {
                @Override
                public void onCompleted(JSONArray array, GraphResponse response) {
                    try {
                        for (int i = 0; i < array.length(); i++) {
                            model.facebookFriends.add(
                                    array.getJSONObject(i).getString("id"));
                        }

                        postUserToDb();

                    } catch (JSONException e) {
                        e.printStackTrace();
                        displayFacebookFriendsError();
                    }

                }
            };

    private GraphRequest.GraphJSONArrayCallback onResumeFriendsResponse =
            new GraphRequest.GraphJSONArrayCallback() {
                @Override
                public void onCompleted(JSONArray array, GraphResponse response) {
                    try {
                        for (int i = 0; i < array.length(); i++) {
                            model.facebookFriends.add(
                                    array.getJSONObject(i).getString("id"));
                        }

                        redirect();

                    } catch (JSONException e) {
                        e.printStackTrace();
                        displayFacebookFriendsError();
                    }

                }
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
        hideLogInButtons();

        facebookToken = AccessToken.getCurrentAccessToken();
        googleAccount = GoogleSignIn.getLastSignedInAccount(this);

        if (userModelIsSet()) { // app instance brought to front and user data is present

            if (bothAreNull(googleAccount, facebookToken)){ // user had been logged out for some reason
                model.facebookId = null;
                model.googleId = null;
                showLogInButtons();
                return;
            }
            else { // user is already logged in, redirect
                updateFriendsAndRedirect();
            }
        }

        else { // new app instance started, no user data yet

            if (notNull(googleAccount)) { // User logged in with Google during last usage
                tryLogInExistingUser(googleAccount.getId(), null);
            }

            else if (notNull(facebookToken)) // User logged in with FB during last udage
                GraphRequest.newMeRequest(facebookToken,
                        new GraphRequest.GraphJSONObjectCallback() {

                            @Override
                            public void onCompleted(JSONObject user, GraphResponse graphResponse) {
                                if (notNull(user))
                                    tryLogInExistingUser(null, user.optString("id"));
                                else {
                                    LoginManager.getInstance().logOut();
                                    showLogInButtons();
                                }
                            }
                        }).executeAsync();
            else { // User hasn't logged in before
                showLogInButtons();
            }
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onDestroy() { super.onDestroy(); }

    private void displayAuthError() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(
                        MainActivity.this,
                        "Authentication failed",
                        Toast.LENGTH_SHORT
                ).show();
                showLogInButtons();
            }
        });
    }

    private void displayFacebookFriendsError() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(
                        MainActivity.this,
                        "Failed to get Facebook friends",
                        Toast.LENGTH_SHORT
                ).show();
                showLogInButtons();
            }
        });
    }

    private boolean userModelIsSet() { return model != null; }

    private void updateFriendsAndRedirect() {
        facebookToken = AccessToken.getCurrentAccessToken();
        if (model.hasFacebook() && notNull(facebookToken) &&  model.facebookFriends.isEmpty()) {

            // FB user doesn't have friends data yet, get it separately
            GraphRequest request = GraphRequest.newMyFriendsRequest(
                            facebookToken,
                            onResumeFriendsResponse);
            Bundle parameters = new Bundle();
            parameters.putString("limit", "999");
            request.setParameters(parameters);
            request.executeAsync();
        }
        else redirect();
    }
}


