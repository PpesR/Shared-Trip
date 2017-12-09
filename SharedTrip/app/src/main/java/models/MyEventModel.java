package models;

import android.graphics.Bitmap;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import static utils.EventDetailsUtils.bitmapFromBase64String;
import static utils.ValueUtil.isNull;

/**
 * Created by Mark on 12.11.2017.
 */

public class MyEventModel extends EventModel {

    public MyEventModel(String name, String imageUri, String location) {
        super(name, imageUri, location);
    }

    protected int adminId;
    protected int usersPending;
    private Bitmap bitmap;

    public int getAdminId() { return adminId; }
    public void setAdminId(int adminId) { this.adminId = adminId; }

    public int getUsersPending() { return usersPending; }
    public void setUsersPending(int usersPending) { this.usersPending = usersPending; }

    public void setStartDate(String startDate) {
        if (startDate.contains(":")){
            SimpleDateFormat original = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            SimpleDateFormat correctFormat = new SimpleDateFormat("dd/MM/yyyy");
            try {
                startDate = correctFormat.format(original.parse(startDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        this.startDate = startDate;
    }

    public Bitmap getBitmap() { return bitmap; }
    public void setBitmap(String base64) {
        this.bitmap = isNull(base64) ? null : bitmapFromBase64String(base64);
    }

    public void decreaseUsersPending() {
        usersPending--;
    }
}
