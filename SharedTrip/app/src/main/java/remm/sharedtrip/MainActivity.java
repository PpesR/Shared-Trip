package remm.sharedtrip;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.StrictMode;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphRequestAsyncTask;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.view.View.VISIBLE;

public class MainActivity extends FragmentActivity {

    CallbackManager callbackManager;
    ProfileTracker profileTracker;
    private static MainActivity self;
    private static FbUserModel model;
    private Intent browseEvents;

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
//                loginButton.setVisibility(View.GONE);
                progressBar.setVisibility(VISIBLE);
            }
        });

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                try {
                                    model = new FbUserModel(
                                            object.optString("id"),
                                            object.getString("name"),
                                            object.getString("gender"),
                                            object.has("birthday") ? object.getString("birthday") : null );
                                    postUserToDb();

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

        /*profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                if (oldProfile==null && currentProfile != null && !userSent) {
                    userSent = true;

                }
            }
        };*/

        if (AccessToken.getCurrentAccessToken() != null) {
            //loginButton.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            getUserInfoFromDb();
        }
    }

    /*
     * The transition between Main view and Event Browsing view.
     * Passes your login information like name and picture (only FB for now) to the event browser.
     */
    private void redirect() {

        if (browseEvents==null)
            browseEvents = new Intent(self, BrowseEvents.class); // The thing that performs redirection
        browseEvents.putExtra("user", new Gson().toJson(model));
        startActivity(browseEvents);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        /*profileTracker.stopTracking();*/
    }

    private static class UserRegistrationTask<Void> extends AsyncTask<Void, Void, Void> {

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected final Void doInBackground(final Void... nothings) {
            OkHttpClient client = new OkHttpClient();

            java.lang.String formattedBirthDate = "";
            if (model.birthDate != null) {
                SimpleDateFormat fromUser = new SimpleDateFormat("MM/dd/yyyy");
                SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    formattedBirthDate = myFormat.format(fromUser.parse(model.birthDate));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            FormBody.Builder formBuilder = null;
            formBuilder = new FormBody.Builder()
                    .add("hdl", "user")
                    .add("fb_id", model.fbId+"")
                    .add("name", model.name)
                    .add("gender", model.gender)
                    .add("birth_date", model.birthDate==null ? "null" : formattedBirthDate)
                    .add("picture", Profile.getCurrentProfile().getProfilePictureUri(300,300).toString());

            final Request request = new Request.Builder()
                    .url("http://146.185.135.219/requestrouter.php")
                    .post(formBuilder.build())
                    .build();
            Call call = client.newCall(request);

            call.enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        JSONArray array = new JSONArray(response.body().string());
                        Profile current = Profile.getCurrentProfile();
                        if (array.getString(0).equals("SUCCESS") && current != null) {
                            int userId = array.getJSONArray(2).getInt(0);
                            model.id = userId;
                            model.description = array.getJSONArray(2).getString(1);
                            model.imageUri = Profile.getCurrentProfile().getProfilePictureUri(300,300).toString();
                            model.firstName = current.getFirstName();
                            self.redirect();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            return null;
        }
    }

    private static class UserInfoTask<String> extends AsyncTask<FbUserModel, Void, String> {

        private String input;

        public UserInfoTask(String input) {
            this.input = input;
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @SafeVarargs
        @Override
        protected final String doInBackground(FbUserModel... users) {

            OkHttpClient client = new OkHttpClient();
            final Request request = new Request.Builder()
                    .url("http://146.185.135.219/requestrouter.php?hdl=user&act=fb&user="+input)
                    .build();
            Call call = client.newCall(request);
            call.enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) { }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        JSONArray array = new JSONArray(response.body().string());
                        if (array.getString(0).equals("SUCCESS")) {
                            JSONArray userData = array.getJSONArray(2);
                            model = new FbUserModel(input+"", userData.getString(1), userData.getString(2), userData.getString(3));
                            model.id = userData.getInt(0);
                            model.description = userData.getString(4);
                            model.imageUri = userData.getString(5);
                        }
                        Profile current = Profile.getCurrentProfile();
                        /*if (current != null) {
                            model.firstName = current.getFirstName();
                            self.redirect();
                        }*/

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            return null;
        }
    }

    private void postUserToDb() {
        UserRegistrationTask<Void> asyncTask = new UserRegistrationTask<>();
        asyncTask.execute();
    }

    private void getUserInfoFromDb() {
        GraphRequestAsyncTask request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject user, GraphResponse graphResponse) {
                try {
                    UserInfoTask<String> asyncTask = new UserInfoTask<>(user.optString("id"));
                    String s = asyncTask.execute().get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }).executeAsync();
    }

    public static class FbUserModel implements Serializable {
        public String fbId;
        public int id;
        public String firstName;
        public String name;
        public String gender;
        public String birthDate;
        public String description;
        public String imageUri;

        public FbUserModel(String fid, String name, String gender, String birthDate) {
            this.fbId = fid;
            this.name = name;
            this.gender = gender;
            this.birthDate = birthDate;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}


