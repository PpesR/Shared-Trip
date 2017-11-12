package models;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Created by Mark on 12.11.2017.
 */

public class AdminEventModel extends EventModel {

    public AdminEventModel(String name, String imageUri, String location) {
        super(name, imageUri, location);
    }

    protected int adminId;
    protected int usersPending;

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

}
