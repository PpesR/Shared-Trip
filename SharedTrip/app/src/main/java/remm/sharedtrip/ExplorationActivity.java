package remm.sharedtrip;

import android.annotation.SuppressLint;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.Profile;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;

import java.util.ArrayList;

import java.util.List;
import java.util.concurrent.ExecutionException;

import adapters.EventAdapter;
import adapters.RateUserAdapter;
import fragments.*;
import fragments.BrowseEventsFragment;
import models.UserEventModel;
import remm.sharedtrip.MainActivity.FbGoogleUserModel;
import services.SharedTripFirebaseMessagingService;
import utils.BottomNavigationViewHelper;
import utils.RatingUtil;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static utils.ValueUtil.isNull;

public class ExplorationActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {


    private TextView headerUsername;
    private Intent ownIntent;
    private FbGoogleUserModel userModel;
    private Gson gson = new Gson();
    private BottomNavigationView bottomNavigationView;
    private AccessTokenTracker accessTokenTracker;
    private Intent messagingService;
    private LoginButton fbLoginButton;
    private String apiPrefix;


    private Button rateButton;
    List<RateUserAdapter.RateablePerson> people;
    RecyclerView recyclerView;
    LinearLayoutManager ratingManager;
    AlertDialog ratingDialog;
    RateUserAdapter rateUserAdapter;

    private static ExplorationActivity self;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        self = this;
        ownIntent = getIntent();
        userModel = gson.fromJson(
                ownIntent.getStringExtra("user")
                , FbGoogleUserModel.class);

        apiPrefix = ownIntent.getStringExtra("prefix");

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken previousToken, AccessToken newToken) {
                if (newToken == null) {
                    fbLoginButton.setVisibility(GONE);
                    finish();
                }
            }
        };

        if (isNull(messagingService)) {
            messagingService = new Intent(this, SharedTripFirebaseMessagingService.class);
            startService(messagingService);
        }

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setWindow(savedInstanceState);

    }

    private void setWindow(Bundle savedInstanceState){
        setContentView(R.layout.activity_exploration);

        setUpUserHeader();

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);

        rateButton = findViewById(R.id.header_rate_button);
        rateButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(self);
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.dialog_rate_users, null);
                builder.setView(dialogView);
                recyclerView = dialogView.findViewById(R.id.rating_recycler);
                builder.setTitle("Rate other travellers");
                builder.setPositiveButton("confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        RatingUtil.RatingTask<Void> task;
                        for (RateUserAdapter.RateablePerson p : people) {
                            if (p.rating > -1) {
                                task = new RatingUtil.RatingTask<>(apiPrefix, p.id, p.eventId, userModel.id, p.rating);
                                task.execute();
                            }
                        }
                        ratingDialog.hide();
                    }
                });
                builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ratingDialog.hide();
                    }
                });
                ratingDialog = builder.create();
                getRateables();
            }
        });

        MenuItem profileItem = bottomNavigationView.getMenu().findItem(R.id.bottombaritem_profile);
        profileItem.setTitle(userModel.firstName);

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.bottombaritem_events:
                                switchToFragmentBrowseEvents();
                                return true;
                            case R.id.bottombaritem_friends:
                                switchToFragmentFriendsView();
                                break;
                            case R.id.bottombaritem_stats:
                                switchToFragmentStats();
                                return true;
                            case R.id.bottombaritem_profile:
                                switchToFragmentMyEvents();
                                return true;
                        }
                        return true;
                    }
                });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.container, new BrowseEventsFragment()).commit();
        }
    }

    //method not used (searchview), but required by default
    @Override
    public boolean onQueryTextSubmit(String s) {
        search(s);
        return false;
    }
    //when called upon filters events by name
    @Override
    public boolean onQueryTextChange(String s) {
        return false;
    }

    private void search(String filter){
        Intent searchIntent = new Intent(ExplorationActivity.this, SearchActivity.class);
        searchIntent.putExtra("user", ownIntent.getStringExtra("user"));
        ExplorationActivity.this.startActivity(searchIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MenuItem myButton = bottomNavigationView.getMenu().findItem(R.id.bottombaritem_events);
        myButton.setChecked(true);

        if (isNull(messagingService)) {
            messagingService = new Intent(this, SharedTripFirebaseMessagingService.class);
            startService(messagingService);
        }

        if (Profile.getCurrentProfile() == null && GoogleSignIn.getLastSignedInAccount(this) == null) {
            finish();
        }
    }

    public void getRateables() {
        RatingUtil.RateablesTask<Void> task = new RatingUtil.RateablesTask<>(apiPrefix, userModel.id);
        try {
            people = task.execute().get();
            if (!people.isEmpty()) {
                rateUserAdapter = new RateUserAdapter(this, people);
                ratingManager = new LinearLayoutManager(this);
                recyclerView.setLayoutManager(ratingManager);
                recyclerView.setAdapter(rateUserAdapter);
                ratingDialog.show();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        this.moveTaskToBack(true);
        //do not redirect
    }

    private void setUpUserHeader() {

        headerUsername = findViewById(R.id.user_header_name);
        headerUsername.append("  "+ userModel.name);
        headerUsername.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent profileIntent = new Intent(ExplorationActivity.this, ProfileActivity.class);
                profileIntent.putExtra("user", ownIntent.getStringExtra("user"));
                ExplorationActivity.this.startActivity(profileIntent);
            }
        });

        if (userModel.hasFacebook() && !userModel.hasGoogle()) {
            headerUsername.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.com_facebook_button_icon_blue, 0, 0, 0);
        }
        else {
            Drawable googleG = getResources().getDrawable(R.drawable.googleg_color);
            Bitmap b = ((BitmapDrawable)googleG).getBitmap();
            Bitmap bitmapResized = Bitmap.createScaledBitmap(b, 50, 50, false);
            googleG = new BitmapDrawable(getResources(), bitmapResized);
            headerUsername.setCompoundDrawablesWithIntrinsicBounds(googleG, null, null, null);
        }

        fbLoginButton = findViewById(R.id.header_logoff_button);
        fbLoginButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

        final Button googleLogoutButton = findViewById(R.id.google_logout_btn);
        googleLogoutButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.getGoogleSignInClient().signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        googleLogoutButton.setVisibility(GONE);
                        finish();
                    }
                });
            }
        });

        if (!userModel.hasGoogle()) {
            googleLogoutButton.setVisibility(GONE);
            fbLoginButton.setVisibility(VISIBLE);
        }

        else if (!userModel.hasFacebook()) {
            fbLoginButton.setVisibility(GONE);
            googleLogoutButton.setVisibility(VISIBLE);
        }
    }

    public FbGoogleUserModel getUserModel() {
        return userModel;
    }

    public String getApiPrefix() {
        return apiPrefix;
    }

    private void switchToFragmentMyEvents() {
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.container, new MyEventsFragment()).commit();

    }

    private void switchToFragmentStats() {
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.container, new StatsFragment()).commit();

    }

    private void switchToFragmentBrowseEvents() {
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.container, new BrowseEventsFragment()).commit();

    }

    public void switchToFragmentFriendsView(){
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.container, new FriendsFragment()).commit();
    }

    public void showNavBar() {
        bottomNavigationView.setVisibility(VISIBLE);
    }

    public void hideNavbar() {
        bottomNavigationView.setVisibility(GONE);
    }
}
