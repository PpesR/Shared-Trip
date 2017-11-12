package remm.sharedtrip;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import utils.BottomNavigationViewHelper;

public class FriendsViewActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

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
                                finish();
                                return true;
                            case R.id.bottombaritem_friends:
                                return true;
                            case R.id.bottombaritem_stats:
                                finish();
                                Intent statsViewActivity = new Intent(FriendsViewActivity.this, StatsViewActivity.class);
                                startActivity(statsViewActivity);
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
