package remm.sharedtrip;
//CIworks
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.support.design.widget.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
      /*  Intent intent = new Intent(this, SignInActivity.class);
        startActivity(intent); */
        Intent intent = new Intent(this, BrowseEvents.class);
        startActivity(intent);
    }
}


