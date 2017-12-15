package fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.TimePicker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import models.CreatorEventModel;
import remm.sharedtrip.CreateEventActivity;

import static utils.DebugUtil.doNothing;
import static utils.UtilBase.notNull;

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {
    String myday;
    String mymonth;
    String myyear;
    String date;
    CreatorEventModel model;
    char whichDate;
    CreateEventActivity creationView;

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat dateFormatUTC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private DatePickerDialog datePickerDialog;

    public void setModel(CreatorEventModel model, char whichDate, CreateEventActivity handle) {
        this.model = model;
        this.whichDate = whichDate;
        this.creationView = handle;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker

        dateFormatUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
        dateFormat.setTimeZone(TimeZone.getDefault());

        final Calendar c = Calendar.getInstance();
        c.setTime(new Date());

        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        datePickerDialog = new DatePickerDialog(getActivity(), this, year, month, day);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        return datePickerDialog;
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        myday = (day < 10 ? "0" : "") + day;
        mymonth = (month < 10 ? "0" : "") + (month + 1);
        myyear = year + "";
        date = myyear + "-" + mymonth + "-" + myday + " 00:00:00";

        if (whichDate == 's') {
            model.setStartDate(date);
        } else {
            model.setEndDate(date);
        }

        final Calendar c = Calendar.getInstance();
        c.setTime(new Date());

        Date temp = null;
        try {
            dateFormatUTC.parse(model.getStartDate());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        int minHr;
        if (whichDate=='s'){
            minHr = c.get(Calendar.HOUR_OF_DAY);
        }
        else {
            if (notNull(temp)) {
                Calendar c1 = Calendar.getInstance();
                c1.setTimeZone(TimeZone.getTimeZone("UTC"));
                c1.setTime(temp);
                c1.setTimeZone(TimeZone.getDefault());
                minHr = c1.get(Calendar.HOUR_OF_DAY);
                c1.setTimeZone(TimeZone.getTimeZone("UTC"));
                c1.setTime(new Date());
            }
            else minHr = (c.get(Calendar.HOUR_OF_DAY)+1)%24;
        }

        TimePickerDialog timePicker = new TimePickerDialog(this.getActivity(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                try {
                    if (whichDate == 's') {
                        if (model.getStartDate() != null) {
                            Date d = dateFormatUTC.parse(model.getStartDate());
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(d);
                            cal.setTimeZone(TimeZone.getDefault());
                            cal.set(Calendar.HOUR_OF_DAY, i);
                            cal.set(Calendar.MINUTE, i1);
                            d = cal.getTime();
                            model.setStartDate(dateFormatUTC.format(d));
                            creationView.onModeDatelChanged('s');

                        }
                    } else {
                        if (model.getEndDate() != null) {
                            Date d = dateFormatUTC.parse(model.getEndDate());
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(d);
                            cal.setTimeZone(TimeZone.getDefault());
                            cal.set(Calendar.HOUR_OF_DAY, i);
                            cal.set(Calendar.MINUTE, i1);
                            d = cal.getTime();
                            model.setEndDate(dateFormatUTC.format(d));
                            creationView.onModeDatelChanged('e');
                        }
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }, (minHr+1)%24, 0, true);
        timePicker.show();
    }
}