package remm.sharedtrip;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import fragments.*;
import fragments.BrowseEvents;
import utils.BottomNavigationViewHelper;

public class FriendsViewActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_friends_view);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        BottomNavigationViewHelper
                .disableShiftMode(bottomNavigationView);

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.bottombaritem_events:
                                fragmentManager = getSupportFragmentManager();
                                fragmentTransaction = fragmentManager.beginTransaction();
                                fragmentTransaction.add(R.id.friends_view_fragment, new BrowseEvents());
                                fragmentTransaction.commit();
                                return true;
                            case R.id.bottombaritem_friends:
                                fragmentManager = getSupportFragmentManager();
                                fragmentTransaction = fragmentManager.beginTransaction();
                                fragmentTransaction.add(R.id.friends_view_fragment, new FriendsView());
                                fragmentTransaction.commit();
                                return true;
                            case R.id.bottombaritem_stats:
                                fragmentManager = getSupportFragmentManager();
                                fragmentTransaction = fragmentManager.beginTransaction();
                                fragmentTransaction.add(R.id.friends_view_fragment, new StatsFragment());
                                fragmentTransaction.commit();
                                return true;
                            case R.id.bottombaritem_profile:
                                finish();
                                Intent adminViewActivity = new Intent(FriendsViewActivity.this, AdminEventActivity.class);
                                startActivity(adminViewActivity);
                                return true;
                        }
                        return true;
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        MenuItem myButton = bottomNavigationView.getMenu()
                .findItem(R.id.bottombaritem_friends);
        myButton.setChecked(true);
    }
}
