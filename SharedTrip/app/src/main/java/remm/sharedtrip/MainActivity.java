package remm.sharedtrip;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import utils.UserAccountUtil.*;

import static utils.ValueUtil.*;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MainActivity extends FragmentActivity implements UserActivityHandle {

    private static final int RC_SIGN_IN = 613;
    CallbackManager callbackManager;
    private static MainActivity self;
    private static FbGoogleUserModel model;
    private Intent browseEvents;
    private static GoogleSignInClient mGoogleSignInClient;
    private SignInButton googleButton;
    private LoginButton loginButton;
    private ProgressBar progressBar;
    private GoogleSignInAccount account;
    private String apiPrefix;
    private FirebaseAuth mAuth;
    private FirebaseUser currentFirebaseUser;

    public static GoogleSignInClient getGoogleSignInClient() { return mGoogleSignInClient; }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        self = this;

        mAuth = FirebaseAuth.getInstance();
        apiPrefix = getResources().getString(R.string.api_address_with_prefix);

        /*
         * Mark: Needed for getting pictures from FB/Google/elsewhere.
         * TODO: move to the page that displays the pictures
         * Until then, DO NOT REMOVE
         */
        /*StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);*/
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


        /* Handling existing users*/
        if (notNull(AccessToken.getCurrentAccessToken())) { // Facebook
            GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {

                @Override
                public void onCompleted(JSONObject user, GraphResponse graphResponse) {
                    tryLogInExistingUser(null, user.optString("id"));
                }

            }).executeAsync();
        }
        else { // Google
            account = GoogleSignIn.getLastSignedInAccount(this);
            if (notNull(account)) { tryLogInExistingUser(account.getId(), null); }
        }
    }

    private void tryLogInExistingUser(String googleId, String facebookId) {
        UserCheckingTask<Void> task = new UserCheckingTask<>(
                apiPrefix,
                new UserCheckCallback(this),
                googleId,
                facebookId);
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

        try {
            model.id = obj.getInt("id");
            model.name = valueOrNull(obj.getString("name"));
            model.description = valueOrNull(obj.getString("user_desc"));
            model.imageUriString = valueOrNull(obj.getString("user_pic"));
            model.gender = valueOrNull(obj.getString("gender"));

            /*if (notNull(currentFirebaseUser)) {
                if (model.hasGoogle()) firebaseAuthWithGoogle(account);
                if (model.hasFacebook()) handleFacebookAccessToken(AccessToken.getCurrentAccessToken());
            }
            else*/
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
            account = completedTask.getResult(ApiException.class);
            Uri photoUri = account.getPhotoUrl();

            if (modelIsSet()) model = new FbGoogleUserModel();

            model.name = account.getDisplayName();
            model.googleId = account.getId();
            model.imageUriString = toStringNullSafe(photoUri);

//            Set<Scope> scopes = account.getGrantedScopes();
            postUserToDb();

        } catch (ApiException e) { displayAuthError(); }
    }

    @Override
    public void onUserCheckReady(FbGoogleUserModel checkedModel) {
        if (isNull(checkedModel)) { // Logged in user is no longer in database -> force log out

//            if (notNull(currentFirebaseUser)) FirebaseAuth.getInstance().signOut();

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
            updateFacebookFriendsAndRedirect();
            /*currentFirebaseUser = mAuth.getCurrentUser();
            if (notNull(currentFirebaseUser)) {
                updateFacebookFriendsAndRedirect();
            /*}
            else {
                if (model.hasGoogle()) { firebaseAuthWithGoogle(account); }
                if (model.hasFacebook()) { handleFacebookAccessToken(AccessToken.getCurrentAccessToken()); }
            }*/
        }
    }

    private void redirect() {
        if (!model.hasFacebook() && !model.hasGoogle()
                || bothAreNull(Profile.getCurrentProfile(), account)) {
            showLogInButtons();
            return;
        }

        if (isNull(browseEvents)) browseEvents = new Intent(self, BrowseActivity.class);

        model.firstName = model.hasFacebook()
                ? Profile.getCurrentProfile().getFirstName()
                : (notNull(account) ? account.getGivenName() : "You");

        browseEvents.putExtra("user", new Gson().toJson(model));
        browseEvents.putExtra("prefix", apiPrefix);
        startActivity(browseEvents);
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

        String firstName;
        String birthDate;

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

        GoogleSignInAccount googleAccount = GoogleSignIn.getLastSignedInAccount(this);
        AccessToken facebookToken = AccessToken.getCurrentAccessToken();

        if (notNull(model)) {
            hideLogInButtons();

            if (bothAreNull(googleAccount, facebookToken)){
                model.facebookId = null;
                model.googleId = null;
                showLogInButtons();
            }
            else {
                updateFacebookFriendsAndRedirect();
            }
        }

        else {
            if (notNull(googleAccount))
                tryLogInExistingUser(googleAccount.getId(), null);
            else if (notNull(facebookToken))
                tryLogInExistingUser(null, facebookToken.getUserId());
            else {
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

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        String accountToken = acct.getIdToken();
        AuthCredential credential = GoogleAuthProvider.getCredential(accountToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) { // Sign in success, update UI with the signed-in user's information
                            currentFirebaseUser = mAuth.getCurrentUser();
                            redirect();

                        } else { displayAuthError(); }
                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken token) {

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        if (isNull(mAuth.getCurrentUser())) {
            mAuth.signInWithCredential(credential)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                currentFirebaseUser = mAuth.getCurrentUser();
                                updateFacebookFriendsAndRedirect();

                            } else {
                                displayAuthError();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    displayAuthError();
                }
            });
        }
        else
            redirect();
    }

    private void displayAuthError() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(
                        MainActivity.this,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT
                ).show();
                showLogInButtons();
            }
        });
    }

    private boolean modelNotSet() { return model == null; }
    private boolean modelIsSet() { return model != null; }

    private void updateFacebookFriendsAndRedirect () {
        AccessToken facebookToken = AccessToken.getCurrentAccessToken();
        if (notNull(facebookToken) && model.hasFacebook() && model.facebookFriends.isEmpty()) {
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


