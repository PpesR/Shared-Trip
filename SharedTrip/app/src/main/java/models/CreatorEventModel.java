package models;

import android.net.Uri;

import java.io.File;
import java.net.URISyntaxException;

import remm.sharedtrip.CreateEvent;
import utils.CreateEventUtils;

import static utils.ValueUtil.notNull;

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
            String filePath = CreateEventUtils.getFilePath(activity, imageUri);
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
