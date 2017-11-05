package remm.sharedtrip;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.Profile;

import org.w3c.dom.Text;

import java.io.IOException;
import java.net.URI;
import java.net.URL;

public class ProfileView extends AppCompatActivity {

    private Intent ownIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ownIntent = getIntent();
        setContentView(R.layout.activity_profile);
        Profile currentProfile = Profile.getCurrentProfile();

        ImageView prof_pic = (ImageView) findViewById(R.id.prof_image);
        Uri pic = currentProfile.getProfilePictureUri(300,300);

        try {
            URL imageURL = new URL(pic.toString());
            Bitmap bitmap = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());
            prof_pic.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //TextView gender = (TextView) findViewById(R.id.prof_sex);
        //gender.setText(ownIntent.getStringExtra("gender"));

    }
}