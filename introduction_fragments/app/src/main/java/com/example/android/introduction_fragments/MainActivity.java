package com.example.android.introduction_fragments;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView mBottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mBottomNav = findViewById(R.id.bottom_navigation);
        mBottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.bottombaritem_events:
                        switchToFragment1();
                        break;
                    case R.id.bottombaritem_friends:
                        switchToFragment2();
                        break;

                }
              return false;
        };

    });

}

    public void switchToFragment1() {
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.article_fragment, new ArticleFragment()).commit();
    }
    public void switchToFragment2(){
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.headlines_fragment, new HeadlinesFragment()).commit();
    }
}
