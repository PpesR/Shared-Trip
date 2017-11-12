package models;

/**
 * Created by Mark on 12.11.2017.
 */

public class UserEventModel extends EventModel {

    protected boolean userApproved;
    protected boolean approvalPending;
    protected boolean userBanned;

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
}
