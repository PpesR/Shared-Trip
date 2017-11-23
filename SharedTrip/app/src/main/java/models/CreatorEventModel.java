package models;

import android.net.Uri;

import java.io.File;
import java.net.URISyntaxException;

import remm.sharedtrip.CreateEvent;
import utils.CreateEventUtils;

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

    public void setImageFile(Uri imageUri, CreateEvent activity) {
        try {
            this.imageFile = new File(CreateEventUtils.getFilePath(activity, imageUri));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        //this.imageFile = imageFile;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }
}
