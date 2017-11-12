package utils;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.widget.DatePicker;

import java.util.Calendar;

import models.EventModel;
import models.UserEventModel;

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {
    String myday;
    String mymonth;
    String myyear;
    String date;
    UserEventModel model;
    char whichDate;
    CreateEvent creationView;

    public void setModel(UserEventModel model, char whichDate, CreateEvent handle) {
        this.model = model;
        this.whichDate = whichDate;
        this.creationView = handle;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();

        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);


        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onDateSet(DatePicker view, int year, int month, int day) {
        myday = (day < 10 ? "0" : "")+day;
        mymonth = (month < 10 ? "0" :"")+month;
        myyear = year+"";
        date = String.join("-",myyear,mymonth,myday);
        if (whichDate=='s') {
            model.setStartDate(date);
            creationView.onModelChanged();
        }
        else {
            model.setEndDate(date);
            creationView.onModelChanged();
        }
    }




}