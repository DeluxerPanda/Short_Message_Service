package se.deluxerpanda.smssender;
import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final int SMS_PERMISSION_REQUEST_CODE = 1;
    private EditText phoneNumberEditText, messageEditText;

    private static TextView SetTimeText;
    private static TextView SetDateStartText;
    private static TextView SetDateEndsText;
    private LinearLayout pickDateEndsBox;
    public static int timeHourSaved = -1;
    public static int timeMinuteSaved = -1;

    private static String startDate;
    private static String endDate;
    private boolean hasSendSmsPermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);
        return permissionCheck == PackageManager.PERMISSION_GRANTED;
    }
    private void requestSendSmsPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS},
                SMS_PERMISSION_REQUEST_CODE);
    }
    // SetTimeText
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Use the current date as the default date in the picker.
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE)+ 6;
        String formattedDate = String.format("%04d-%02d-%02d", year, month + 1, day); // Adjust month by +1 since it's 0-based

        String timeText = hour + ":" + minute;

        setContentView((R.layout.activity_main));

        phoneNumberEditText = findViewById(R.id.phoneNumberEditText);
        messageEditText = findViewById(R.id.messageEditText);

        pickDateEndsBox = findViewById(R.id.pickDateEndsBox);

        SetTimeText = findViewById(R.id.SetTimeText);

        SetDateStartText = findViewById(R.id.SetDateStartText);

        SetDateEndsText = findViewById(R.id.SetDateEndsText);


        SetTimeText.setText(" "+timeText);
        SetDateStartText.setText(" " + formattedDate);
        SetDateEndsText.setText(" " + formattedDate);

        Button sendButton = findViewById(R.id.sendB);
        sendButton.setOnClickListener(view -> {
            String phonenumber = phoneNumberEditText.getText().toString();
            String message = messageEditText.getText().toString();
            if (hasSendSmsPermission()) {
                if (!phonenumber.isEmpty() && !message.isEmpty()) {
                    scheduleSMS(phonenumber,message);
                    hideKeyboard();
                    History_info();
                } else {
                    Toast.makeText(this,"Please fill in both phone number and message fields.",Toast.LENGTH_SHORT).show();
                }
            } else {
                requestSendSmsPermission();
            }
        });


        // Check and request permission if needed
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS},
                    SMS_PERMISSION_REQUEST_CODE);
        }
        Button pickTimeButton = findViewById(R.id.pickTime);
        pickTimeButton.setOnClickListener(view -> {
            TimePickerFragment timePickerFragment = new TimePickerFragment();
            timePickerFragment.show(getSupportFragmentManager(), "timePicker");
        });


        Button pickDateStartButton = findViewById(R.id.pickDateStarts);
        pickDateStartButton.setOnClickListener(view -> {
            showDatePicker(true);
        });


        Button pickDateEndsButton = findViewById(R.id.pickDateEnds);
        pickDateEndsButton.setOnClickListener(view -> {
                showDatePicker(false);
        });

        CheckBox pickDateEndsCheckBox = findViewById(R.id.checkBox);
        pickDateEndsCheckBox.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                pickDateEndsBox.setVisibility(View.GONE);
            } else {
                pickDateEndsBox.setVisibility(View.VISIBLE);
            }

        });
        History_info();
    }
    public void History_info(){

        // Assuming you want to get the list of alarms when the activity starts
        List<AlarmDetails> alarmList = getAllAlarms(this);

        // Now you can use the alarmList as needed
        for (AlarmDetails alarmDetails : alarmList) {
            int alarmId = alarmDetails.getAlarmId();
            long timeInMillis = alarmDetails.getTimeInMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat sdf2 = new SimpleDateFormat("H:mm");

            // Create a Date object and set it using the time in milliseconds
            Date date = new Date(timeInMillis);

            // Format the date and print the result
            String formattedDateStart = sdf.format(date);
            String formattedClockTime = sdf2.format(date);
            Log.d("AlarmDetails", "" + "Alarm ID: " + alarmId + ", Millis: " + timeInMillis +", Date Start: "+ formattedDateStart +", Time: "+formattedClockTime+", date end");

            LinearLayout parentLayout = findViewById(R.id.HE);

            LinearLayout linearLayout = (LinearLayout) parentLayout;

            // Add your dynamic TextView here
            TextView dynamicTextView = new TextView(this);
            dynamicTextView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));

            // Set dynamic information from the AlarmDetails object to the TextView
            dynamicTextView.setText("Date: " + formattedDateStart + ", Time: " + formattedClockTime);
            dynamicTextView.setTextSize(24);
            dynamicTextView.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD_ITALIC));
            dynamicTextView.setTextColor(getResources().getColor(android.R.color.black));
            dynamicTextView.setGravity(Gravity.CENTER);

            // Add the dynamic TextView to the LinearLayout
            linearLayout.addView(dynamicTextView);
            dynamicTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(MainActivity.this, alarmId+"More comming soon!", Toast.LENGTH_SHORT).show();
                }


            });
        }

    }
    private void showDatePicker(boolean isStartDate) {
        DatePickerFragment datePickerFragment = new DatePickerFragment();
        Bundle args = new Bundle();
        args.putBoolean("isStartDate", isStartDate);
        datePickerFragment.setArguments(args);
        datePickerFragment.show(getSupportFragmentManager(), "datePicker");
    }
    //time Dialog (start)
    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker.
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE)+ 6;

            if (timeHourSaved != -1 && timeMinuteSaved != -1) {
                return new TimePickerDialog(getActivity(), this, timeHourSaved, timeMinuteSaved, true);
            }else {
                // Create a new instance of TimePickerDialog and return it with 24-hour format.
                return new TimePickerDialog(getActivity(), this, hour, minute, true);
            }

        }

        public void onTimeSet(TimePicker view, int hour , int minute) {
            // Do something with the time the user picks.
            timeHourSaved = hour;
            timeMinuteSaved = minute;
            String timeText = hour  + ":" + minute;
            SetTimeText.setText(" "+timeText);
        }
    }
//time  Dialog (ends)
//date  Dialog (starts)
    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a DatePickerDialog and set the minimum date to the current date
            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), this, year, month, day);
            Date date = new Date();
            datePickerDialog.getDatePicker().setMinDate(date.getTime()); // Set minimum date to now

            // If it's the "end" date picker, set the minimum date to the "start" date
            if (!getArguments().getBoolean("isStartDate")) {
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date startDate = dateFormat.parse(SetDateStartText.getText().toString());
                    datePickerDialog.getDatePicker().setMinDate(startDate.getTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            return datePickerDialog;
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            String formattedDate = String.format("%04d-%02d-%02d", year, month + 1, day); // Adjust month by +1 since it's 0-based
            boolean isStartDate = getArguments().getBoolean("isStartDate");

            if (isStartDate) {
                SetDateStartText.setText(" " + formattedDate);
            } else {
                SetDateEndsText.setText(" " + formattedDate);
            }
        }
    }

//date  Dialog (ends)

    private void scheduleSMS(String phonenumber, String message) {
        //String ost = "aLorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient.";
       if (message.length() <= 160) {
           AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

           Intent intent = new Intent(this, AlarmReceiver.class);


           intent.putExtra("EXTRA_PHONE_NUMBER", phonenumber);
           intent.putExtra("EXTRA_MESSAGES", message);

           int alarmId = UUID.randomUUID().hashCode();
           PendingIntent pendingIntent = PendingIntent.getBroadcast(this, alarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

           String DateStart = (String) SetDateStartText.getText();
           String Clock_Time = (String) SetTimeText.getText();
           String dateTimeString = DateStart + " " + Clock_Time;
           SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd H:m");

           try {
               Date date = sdf.parse(dateTimeString);
               long triggerTime = date.getTime();
               Log.d(String.valueOf(this), "Milliseconds since epoch: " + triggerTime);
               alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
               saveAlarmDetails(this, alarmId, triggerTime);
           } catch (ParseException e) {
               e.printStackTrace();
           }


       }else {
           // Inside your activity or fragment
           AlertDialog.Builder builder = new AlertDialog.Builder(this);
           builder.setTitle("\uD83D\uDEA8 Max characters reached \uD83D\uDEA8");
           builder.setMessage("The maximum character limit is 160, not "+message.length());
           builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int which) {
                   messageEditText.requestFocus(); // Set focus to the messageEditText
                   showKeyboard();
               }
           });
           builder.show();

       }

    }

    // Save alarm details in shared preferences
    private void saveAlarmDetails(MainActivity mainActivity, int alarmId, long triggerTime) {
        SharedPreferences preferences = mainActivity.getSharedPreferences("AlarmDetails", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        // Use a unique key for each alarm
        String key = "alarm_" + alarmId;
        editor.putLong(key, triggerTime);
        editor.apply();
    }
    public class AlarmDetails {
        private int alarmId;
        private long triggerTime;


        public AlarmDetails(int alarmId, long triggerTime) {
            this.alarmId = alarmId;
            this.triggerTime = triggerTime;
        }

        public int getAlarmId() {
            return alarmId;
        }

        public long getTimeInMillis() {
            return triggerTime;
        }

    }

    // Retrieve a list of all alarms
    public List<AlarmDetails> getAllAlarms(Context context) {
        List<AlarmDetails> alarmList = new ArrayList<>();
        SharedPreferences preferences = context.getSharedPreferences("AlarmDetails", Context.MODE_PRIVATE);

        // Iterate through all saved alarms and add them to the list
        Map<String, ?> allEntries = preferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String key = entry.getKey();
            long triggerTime = preferences.getLong(key, 0);

            int alarmId = Integer.parseInt(key.substring(key.lastIndexOf("_") + 1));

            AlarmDetails alarmDetails = new AlarmDetails(alarmId, triggerTime);
            alarmList.add(alarmDetails);


        }

        return alarmList;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                showMessage("Grate you have permission");
            } else {
                // Permission denied
                showMessage("Please grant SMS permission to send messages.");
            }
        }
    }
    public void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();

        if (view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    public void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();

        if (view == null) {
            imm.showSoftInput((View) view.getWindowToken(), 1);
        }
    }
}
