package remm.sharedtrip;

import android.annotation.SuppressLint;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.SearchView.OnQueryTextListener;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;

import java.util.List;
import java.util.concurrent.ExecutionException;

import adapters.RateUserAdapter;
import fragments.*;
import fragments.BrowseEventsFragment;
import interfaces.UserModelHolder;
import remm.sharedtrip.MainActivity.FbGoogleUserModel;
import services.SharedTripFirebaseMessagingService;
import utils.BottomNavigationViewHelper;
import utils.RatingUtil;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static utils.EventDetailsUtil.bitmapFromUriString;
import static utils.UtilBase.isNull;

public class ExplorationActivity extends AppCompatActivity implements OnQueryTextListener, UserModelHolder {

    private static final int OPEN_OWN_PROFILE = 596;

    private Intent ownIntent;
    private FbGoogleUserModel userModel;
    private Gson gson = new Gson();
    private BottomNavigationView bottomNavigationView;
    private Intent messagingService;
    private String serializedUser;

    List<RateUserAdapter.RateablePerson> people;
    RecyclerView rateablesRecycler;
    LinearLayoutManager ratingrateablesLayout;
    AlertDialog ratingDialog;
    RateUserAdapter rateUserAdapter;
    public LayoutInflater inflater;

    private AlertDialog.Builder dialogBuilder;
    private View ratingDialogView;
    private Toolbar toolbar;
    public ProgressBar spinner;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ownIntent = getIntent();
        serializedUser = ownIntent.getStringExtra("user");
        userModel = gson.fromJson(serializedUser, FbGoogleUserModel.class);

        new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken previousToken, AccessToken newToken) {
                if (newToken == null) {
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

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        setWindow(savedInstanceState);
    }

    private void setWindow(Bundle savedInstanceState){
        setContentView(R.layout.activity_exploration);

        setUpUserHeader();

        spinner = findViewById(R.id.exploration_spinner);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);

        MenuItem myEventsItem = bottomNavigationView.getMenu().findItem(R.id.bottombaritem_my_events);
        myEventsItem.setTitle(userModel.firstName);

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.bottombaritem_browse:
                                toolbar.setSubtitle("Browse");
                                switchToFragmentBrowseEvents();
                                return true;
                            case R.id.bottombaritem_friends:
                                toolbar.setSubtitle("Friends' events");
                                switchToFragmentFriendsView();
                                break;
                            case R.id.bottombaritem_stats:
                                toolbar.setSubtitle("My stats");
                                switchToFragmentStats();
                                return true;
                            case R.id.bottombaritem_my_events:
                                toolbar.setSubtitle("My events");
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

    private void search(String keyword){
        Intent searchIntent = new Intent(ExplorationActivity.this, SearchActivity.class);
        searchIntent.putExtra("user", serializedUser);
        searchIntent.putExtra("keyword", keyword);
        ExplorationActivity.this.startActivity(searchIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        toolbar.setSubtitle(null);

        if (isNull(messagingService)) {
            messagingService = new Intent(this, SharedTripFirebaseMessagingService.class);
            startService(messagingService);
        }

        if (Profile.getCurrentProfile() == null && GoogleSignIn.getLastSignedInAccount(this) == null) {
            finish();
        }
    }

    public void getRateables() {
        RatingUtil.RateablesTask<Void> task = new RatingUtil.RateablesTask<>(userModel.id);
        try {
            people = task.execute().get();
            if (!people.isEmpty()) {
                rateUserAdapter = new RateUserAdapter(this, people);
                ratingrateablesLayout = new LinearLayoutManager(this);
                rateablesRecycler.setLayoutManager(ratingrateablesLayout);
                rateablesRecycler.setAdapter(rateUserAdapter);
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
        //do not redirect
        this.moveTaskToBack(true);
    }

    private void setUpUserHeader() {

        toolbar = findViewById(R.id.toolbar);
        toolbar.setSubtitle("Browse events");
        setSupportActionBar(toolbar);

        inflater = getLayoutInflater();
        ratingDialogView = inflater.inflate(R.layout.dialog_rate_users, null);
        rateablesRecycler = ratingDialogView.findViewById(R.id.rating_recycler);

        dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder
                .setTitle("Rate other travellers")
                .setView(ratingDialogView)
                .setPositiveButton("confirm", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        RatingUtil.RateTask<Void> task;
                        for (RateUserAdapter.RateablePerson p : people) {
                            if (p.rating > -1) {
                                task = new RatingUtil.RateTask<>(p.id, p.eventId, userModel.id, p.rating);
                                task.execute();
                            }
                        }
                    }
                })

                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ratingDialog.hide();
                    }
                });

        ratingDialog = dialogBuilder.create();

        /*
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
        */
    }

    public FbGoogleUserModel getUserModel() {
        return userModel;
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_profile:
                Intent profileIntent = new Intent(this, ProfileActivity.class);
                profileIntent.putExtra("user", serializedUser);
                startActivityForResult(profileIntent, OPEN_OWN_PROFILE);
                break;

            case R.id.action_rate:
                getRateables();
                break;

            case R.id.action_log_out:
                if (userModel.hasFacebook()) {
                    LoginManager.getInstance().logOut();
                }

                if (userModel.hasGoogle()){
                    MainActivity.signOutOfGoogle()
                        .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                finish();
                            }
                        });
                }
                else finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);

        RoundedBitmapDrawable rounded = RoundedBitmapDrawableFactory.create(
                getResources(),
                bitmapFromUriString(userModel.imageUriString));
        rounded.setCornerRadius(50);

        Drawable star = getResources().getDrawable(R.drawable.ic_star_black_24dp, null);
        star.setColorFilter(getResources().getColor(R.color.white_text), PorterDuff.Mode.SRC_ATOP);

        menu.findItem(R.id.action_profile).setIcon(rounded);
        menu.findItem(R.id.action_rate).setIcon(star);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OPEN_OWN_PROFILE && resultCode == RESULT_OK) {
            if (data.hasExtra("description")) {
                userModel.description = data.getStringExtra("description");
                serializedUser = gson.toJson(userModel);
            }
        }
    }

    @Override
    public FbGoogleUserModel getLoggedInUser() {
        return userModel;
    }

    @Override
    public String getSerializedLoggedInUserModel() {
        return serializedUser;
    }

    @Override
    public int getLoggedInUserId() {
        return userModel.id;
    }
}
