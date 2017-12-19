package models;

import android.graphics.Bitmap;

import static utils.EventDetailsUtil.bitmapFromBase64String;
import static utils.UtilBase.isNull;

/**
 * Created by Mark on 12.11.2017.
 */

public class UserEventModel extends EventModel {

    protected boolean userApproved;
    protected boolean approvalPending;
    protected boolean userBanned;
    protected boolean isAdmin;
    private Bitmap bitmap;
    private String topic;

    public UserEventModel() {
        super();
    }

    public UserEventModel(String trip_name, String event_picture, String location) {
        super(trip_name, event_picture, location);
    }

    public boolean isUserApproved() { return userApproved; }
    public void setUserApproved(boolean userApproved) { this.userApproved = userApproved; }

    public boolean isApprovalPending() { return approvalPending; }
    public void setApprovalPending(boolean approvalPending) { this.approvalPending = approvalPending; }

    public boolean isUserBanned() { return userBanned; }
    public void setUserBanned(boolean userBanned) { this.userBanned = userBanned; }

    public boolean isAdmin() { return isAdmin; }
    public void setAdmin(boolean admin) { isAdmin = admin; }

    public Bitmap getBitmap() { return bitmap; }
    public void setBitmap(String base64) {
        this.bitmap = isNull(base64) ? null : bitmapFromBase64String(base64);
    }

    public UserEventModel copyWithoutBitmap() {
        UserEventModel copy = new UserEventModel();
        copy.setImageLink(imageLink);
        copy.setLoc(loc);
        copy.setName(name);
        copy.setUserBanned(userBanned);
        copy.setUserApproved(userApproved);
        copy.setAdmin(isAdmin);
        copy.setApprovalPending(approvalPending);
        copy.setCost(cost);
        copy.setDescription(description);
        copy.setEndDate(endDate);
        copy.setStartDate(startDate);
        copy.setSpots(spots);
        copy.setId(id);
        copy.setTopic(topic);
        return copy;
    }

    public void setTopic(String topic) { this.topic = topic; }

    public String getTopic() { return topic; }
}
