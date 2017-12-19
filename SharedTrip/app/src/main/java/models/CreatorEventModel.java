package models;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;

import java.io.File;
import java.net.URISyntaxException;

import remm.sharedtrip.CreateEventActivity;
import utils.CreateEventUtil;

import static utils.UtilBase.notNull;

/**
 * Created by Mark on 14.11.2017.
 */

public class CreatorEventModel extends EventModel {

    private int adminId;
    private File imageFile;
    private boolean isPrivate;

    public CreatorEventModel(String name, String imageLink, String location, int adminId) {
        super(name, imageLink, location);
        this.adminId = adminId;
    }

    public int getAdminId() {
        return adminId;
    }

    public void setAdminId(int adminId) {
        this.adminId = adminId;
    }

    public File getImageFile() {
        return imageFile;
    }

    public void setImageFile(Uri imageUri, CreateEventActivity activity) {
        try {
            String filePath = CreateEventUtil.getFilePath(activity, imageUri);
            Bitmap bm = BitmapFactory.decodeFile(filePath);
            int width = bm.getWidth();
            int height = bm.getHeight();
            float scaleWidth = ((float) 300) / width;
            float scaleHeight = ((float) 200) / height;

            // Create a matrix for the manipulation
            Matrix matrix = new Matrix();

            // Resize the bit map
            matrix.postScale(scaleWidth, scaleHeight);

            // Recreate the new Bitmap
            Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
            if (notNull(filePath)) this.imageFile = new File(filePath);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }
}
