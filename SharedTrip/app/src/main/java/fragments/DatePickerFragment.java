package fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.widget.DatePicker;

import java.util.Calendar;

import models.CreatorEventModel;
import remm.sharedtrip.CreateEventActivity;

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {
    String myday;
    String mymonth;
    String myyear;
    String date;
    CreatorEventModel model;
    char whichDate;
    CreateEventActivity creationView;

    public void setModel(CreatorEventModel model, char whichDate, CreateEventActivity handle) {
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
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), this, year, month, day);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        return datePickerDialog;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onDateSet(DatePicker view, int year, int month, int day) {
        myday = (day < 10 ? "0" : "") + day;
        mymonth = (month < 10 ? "0" : "") + month+1;
        myyear = year + "";
        date = myyear + "-" + mymonth + "-" + myday;
        if (whichDate == 's') {
            model.setStartDate(date);
            creationView.onModelChanged();
        } else {
            model.setEndDate(date);
            creationView.onModelChanged();
        }
    }


}