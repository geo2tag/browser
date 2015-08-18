package com.example.yana.map;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TimePicker;

import com.example.yana.map.util.CustomEditText;
import com.example.yana.map.util.Util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class SettingsActivity extends ActionBarActivity implements View.OnClickListener {
    private final int DATE_FROM = 0;
    private final int DATE_TO = 1;
    private final int TIME_FROM = 2;
    private final int TIME_TO = 3;

    private Context ctx;
    private SeekBar seekBar;
    private EditText tvRadius;
    private EditText tvDateTo, tvDateFrom;
    private EditText etTimeFrom, etTimeTo;

    private int step = 1;
    private int max = 8004;
    private int min = Util.minRadius;
    private int myYear = 1970;
    private int myMonth = 00;
    private int myDay = 01;
    private int myHour = 00;
    private int myMinute = 00;

    private Date curDate = null;
    private int value;
    private int value1;

    Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        tvDateTo = (EditText) findViewById(R.id.tvDateTo);
        tvDateFrom = (EditText) findViewById(R.id.tvDateFrom);

        tvDateTo.setOnClickListener(this);
        tvDateFrom.setOnClickListener(this);

        curDate = new Date();
        tvDateFrom.setText(myYear + "-" + (myMonth + 1) + "-" + myDay);
        tvDateTo.setText(DateFormat.format("dd-MM-yyyy", new Date()));

        etTimeFrom = (EditText) findViewById(R.id.etTimeFrom);
        etTimeTo = (EditText) findViewById(R.id.etTimeTo);

        etTimeFrom.setOnClickListener(this);
        etTimeTo.setOnClickListener(this);

        etTimeFrom.setText("" + myHour + ":" + myMinute);
        etTimeTo.setText("" + myHour + ":" + myMinute);

        seekBar = (SeekBar) findViewById(R.id.seekBar);
        tvRadius = (CustomEditText) findViewById(R.id.tvRadius);

        tvRadius.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    if (!s.toString().equals("")) {
                        value = (Integer.parseInt(s.toString())) / step - min;
                        value++;
                        if (value >= min && value <= (max + 1)) {
                            Util.saveRadius(ctx, value);
                            seekBar.setProgress(value - min);
                        }
                        if (value < min) {
                            Util.saveRadius(ctx, min);
                            seekBar.setProgress(min);
                        }
                        if (value > (max + 1)) {
                            Util.saveRadius(ctx, (max + min));
                            seekBar.setProgress((max + min));
                        }
                    } else {
                        Util.saveRadius(ctx, min);
                        seekBar.setProgress(min);
                    }
                } catch (Exception ex) {
                    Util.saveRadius(ctx, min);
                    ex.printStackTrace();
                }
            }
        });

        seekBar.setMax(max);
        ctx = this;

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                value1 = min + (progress * step);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                tvRadius.setText("" + value1);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        seekBar.setProgress(Util.getRadius(ctx) - 1);

        tvRadius.setText("" + Util.getRadius(ctx));
        tvDateFrom.setText(Util.getDateFrom(ctx));
        etTimeFrom.setText(Util.getTimeFrom(ctx));
        tvDateTo.setText(Util.getDateTo(ctx));
        etTimeTo.setText(Util.getTimeTo(ctx));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    protected Dialog onCreateDialog(int id) {
        DatePickerDialog tpd = null;
        TimePickerDialog td = null;
        calendar = Calendar.getInstance();

        switch (id) {
            case DATE_FROM:
                calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                calendar.setTimeInMillis(Util.getDateTimeFrom(ctx));
                tpd = new DatePickerDialog(this, myCallBackFrom, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));
                tpd.getDatePicker().setMinDate(-62157456000845L);
                return tpd;
            case DATE_TO:
                calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                calendar.setTimeInMillis(Util.getDateTimeTo(ctx));
                tpd = new DatePickerDialog(this, myCallBackTo, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));
                tpd.getDatePicker().setMinDate(-62157456000845L);
                return tpd;
            case TIME_FROM:
                calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                calendar.setTimeInMillis(Util.getDateTimeFrom(ctx));
                if(Util.getDateTimeFrom(ctx) == 0) {
                    calendar.set(Calendar.HOUR, 0);
                }
                td = new TimePickerDialog(this, myCallBackTimeFrom, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
                td.setTitle("Time");
                return td;
            case TIME_TO:
                calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                calendar.setTimeInMillis(Util.getDateTimeTo(ctx));
                td = new TimePickerDialog(this, myCallBackTimeTo, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
                td.setTitle("Time");
                return td;
            default:
                return super.onCreateDialog(id);
        }
    }

    DatePickerDialog.OnDateSetListener myCallBackFrom = new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            myYear = year;
            myMonth = monthOfYear;
            myDay = dayOfMonth;
            String stringDate = "" + myDay + "-" + (myMonth + 1) + "-" + myYear;
            tvDateFrom.setText(stringDate);
            Util.saveDateFrom(ctx, stringDate);
            Util.saveDateTimeFrom(ctx, tvDateFrom.getText().toString(), etTimeFrom.getText().toString());
        }
    };

    TimePickerDialog.OnTimeSetListener myCallBackTimeFrom = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            myHour = hourOfDay;
            myMinute = minute;
            String stringTime = "" + myHour + ":" + myMinute;
            etTimeFrom.setText(stringTime);
            Util.saveTimeFrom(ctx, stringTime);
            Util.saveDateTimeFrom(ctx, tvDateFrom.getText().toString(), etTimeFrom.getText().toString());
        }
    };

    DatePickerDialog.OnDateSetListener myCallBackTo = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            myYear = year;
            myMonth = monthOfYear;
            myDay = dayOfMonth;
            String stringDate = "" + myDay + "-" + (myMonth + 1) + "-" + myYear;
            tvDateTo.setText(stringDate);
            Util.saveDateTo(ctx, stringDate);
            Util.saveDateTimeTo(ctx, tvDateTo.getText().toString(), etTimeTo.getText().toString());
        }
    };

    TimePickerDialog.OnTimeSetListener myCallBackTimeTo = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            myHour = hourOfDay;
            myMinute = minute;
            String stringTime = "" + myHour + ":" + myMinute;
            etTimeTo.setText(stringTime);
            Util.saveTimeTo(ctx, stringTime);
            Util.saveDateTimeTo(ctx, tvDateTo.getText().toString(), etTimeTo.getText().toString());
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvDateFrom:
                showDialog(DATE_FROM);
                break;
            case R.id.tvDateTo:
                showDialog(DATE_TO);
                break;
            case R.id.etTimeFrom:
                showDialog(TIME_FROM);
                break;
            case R.id.etTimeTo:
                showDialog(TIME_TO);
                break;
        }
    }
}