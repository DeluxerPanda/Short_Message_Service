package se.deluxerpanda.smssender;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final int SMS_PERMISSION_REQUEST_CODE = 1;
    private EditText phoneNumberEditText, messageEditText;

    private static TextView SetTimeText;
    private static TextView SetDateStartText;
    private static TextView selectedOptionText;
    private LinearLayout pickDateEndsBox;
    public static int timeHourSaved = -1;
    public static int timeMinuteSaved = -1;

    private static String startDate;
    private static String endDate;
    boolean checkBoxisChecked = false;
    public static String CHANNEL_ID = String.valueOf(UUID.randomUUID().hashCode());

    public static String CHANNEL_NAME = String.valueOf(R.string.app_name);

    private String day;
    private String week;
    private String month;
    private String year;
    private int selectedOptionIndex;
    private boolean hasSendSmsPermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);
        return permissionCheck == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
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

        SetTimeText = findViewById(R.id.SetTimeText);

        SetDateStartText = findViewById(R.id.SetDateStartText);

        selectedOptionText = findViewById(R.id.selectedOptionText);


        SetTimeText.setText(" "+timeText);
        SetDateStartText.setText(" " + formattedDate);

        Button sendButton = findViewById(R.id.sendB);
        sendButton.setOnClickListener(view -> {

            Calendar currentCalendar = Calendar.getInstance();
            long currentTimeInMillis = currentCalendar.getTimeInMillis();
            SimpleDateFormat sdfDateTime = new SimpleDateFormat("yyyy-MM-dd H:mm", Locale.getDefault());
            String selectedDateString = (String) SetDateStartText.getText();
            String selectedTimeString = (String) SetTimeText.getText();
            Date selectedDateTime = null;
            try {
                selectedDateTime = sdfDateTime.parse(selectedDateString + " " + selectedTimeString);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            String phonenumber = phoneNumberEditText.getText().toString();
            String message = messageEditText.getText().toString();
            String repeatSmS = (String) selectedOptionText.getText();
            if (hasSendSmsPermission()) {
            if (!phonenumber.isEmpty() && !message.isEmpty() || !phonenumber.isEmpty() || !message.isEmpty()) {
            if (message.length() <= 160) {
                if (selectedDateTime != null && selectedDateTime.getTime() > currentTimeInMillis) {
                    if (!repeatSmS.equalsIgnoreCase(getResources().getString(R.string.logding_data))) {
                    scheduleSMS(phonenumber,message);
                    hideKeyboard();
                    History_info();
                    phoneNumberEditText.setText("");
                    messageEditText.setText("");

                } else {
                        // Inside your activity or fragment
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle(getResources().getString(R.string.sms_repeates_titel));
                        builder.setMessage(getResources().getString(R.string.sms_repeates_Text));
                        builder.setPositiveButton(getResources().getString(R.string.text_ok), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        builder.show();
                    }

                }  else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(getResources().getString(R.string.sms_time_travel_titel));
                    builder.setMessage(getResources().getString(R.string.sms_time_travel_Text));
                        builder.setPositiveButton(getResources().getString(R.string.text_ok), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        builder.show();
                }
            } else {
                // Inside your activity or fragment
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getResources().getString(R.string.sms_Max_characters_titel));
                builder.setMessage(getResources().getString(R.string.sms_Max_characters_Text)+ "\n"+
                        getResources().getString(R.string.sms_Max_characters_Text_int)+" "+message.length());
                builder.setPositiveButton(getResources().getString(R.string.text_ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                builder.show();
            }
            } else {
                // Inside your activity or fragment
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("\uD83D\uDEA8 Number and message are required \uD83D\uDEA8");
                builder.setMessage("Please provide both phone number and message.");
                builder.setPositiveButton(getResources().getString(R.string.text_ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.show();
            }
            } else {
                // Inside your activity or fragment
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("\uD83D\uDEA8 The app don't have permission \uD83D\uDEA8");
                builder.setMessage("You must allow the app to send SMS");
                builder.setPositiveButton(getResources().getString(R.string.text_ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermission();
                    }
                });
                builder.show();
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
            showDatePicker();
        });


        Button chooseOptionButton = findViewById(R.id.chooseOptionButton);
        chooseOptionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOptionsDialog();
            }
        });
        History_info();
    }

    public void showOptionsDialog() {
        // Get the layout inflater
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.options_dialog, null);

        String[] choices = {
                getString(R.string.data_CheckBox_every_year_text),
                getString(R.string.data_CheckBox_every_month_text),
                getString(R.string.data_CheckBox_every_week_text),
                getString(R.string.data_CheckBox_every_day_text)
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
                .setTitle(getString(R.string.send_every_text))
                .setPositiveButton(getString(R.string.text_ok), (dialog, which) -> {
                    selectedOptionText.setText(choices[selectedOptionIndex]);
                })
                .setNegativeButton(getString(R.string.text_Cancel), (dialog, which) -> {

                })
                .setSingleChoiceItems(choices, 0, (dialog, which) -> {
                    selectedOptionIndex = which;
                });

        AlertDialog dialog = builder.create();
        dialog.show();

    }



    public void History_info(){
        LinearLayout parentLayout = findViewById(R.id.app_backgrund);
        LinearLayout linearLayout = (LinearLayout) parentLayout;
        linearLayout.destroyDrawingCache();
        linearLayout.removeAllViews();

        List<AlarmDetails> alarmList = getAllAlarms(this);

        if (alarmList.isEmpty()) {
            TextView AlarmListIsEmptyTextView = new TextView(this);

            // Add your dynamic TextView here
            AlarmListIsEmptyTextView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));

            AlarmListIsEmptyTextView.setText("There are no SMS scheduled");
            AlarmListIsEmptyTextView.setTextSize(20);
            AlarmListIsEmptyTextView.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD_ITALIC));
            AlarmListIsEmptyTextView.setGravity(Gravity.CENTER);
            linearLayout.addView(AlarmListIsEmptyTextView);
        } else {
        // Now you can use the alarmList as needed
        for (AlarmDetails alarmDetails : alarmList) {

            int alarmId = alarmDetails.getAlarmId();

           // long timeInMillis = alarmDetails.getTimeInMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat sdf2 = new SimpleDateFormat("H:mm");


            // Format the date and print the result
            String formattedDateStart = sdf.format(alarmDetails.getTimeInMillis());
            String formattedClockTime = sdf2.format(alarmDetails.getTimeInMillis());
            String getRepeatSmS = alarmDetails.getRepeatSmS();

            TextView dynamicTextView = new TextView(this);

            // Add your dynamic TextView here
            dynamicTextView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));

            dynamicTextView.setText("Date: " + formattedDateStart + ", Time: " + formattedClockTime);
            dynamicTextView.setTextSize(20);
            dynamicTextView.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD_ITALIC));
            dynamicTextView.setGravity(Gravity.CENTER);
            dynamicTextView.setHintTextColor(R.color.md_theme_dark_onPrimary);

            linearLayout.addView(dynamicTextView);

            dynamicTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertCreator.showAlertBox_for_History_info(v.getContext(),
                            "ID: " + alarmId,
                            "ID: " + alarmId +
                                    "\n Date: " + formattedDateStart +
                                    "\n Time: " + formattedClockTime +
                                    "\n Repeat every: " + getRepeatSmS +
                                    "\n More coming soon!",
                            alarmId);
                }

            });
        }
    }
    }


    private void showDatePicker() {
        DatePickerFragment datePickerFragment = new DatePickerFragment();

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


                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date startDate = dateFormat.parse(SetDateStartText.getText().toString());
                    datePickerDialog.getDatePicker().setMinDate(startDate.getTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            return datePickerDialog;
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            String formattedDate = String.format("%04d-%02d-%02d", year, month + 1, day); // Adjust month by +1 since it's 0-based

                SetDateStartText.setText(" " + formattedDate);
        }
    }

//date  Dialog (ends)

    private void scheduleSMS(String phonenumber, String message) {
        String DateStart = (String) SetDateStartText.getText();
        String Clock_Time = (String) SetTimeText.getText();
        String dateTimeString = DateStart + " " + Clock_Time;



        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd H:m");

        String repeatSmS = (String) selectedOptionText.getText();

        day = getString(R.string.data_CheckBox_every_day_text);
        week = getString(R.string.data_CheckBox_every_week_text);
        month = getString(R.string.data_CheckBox_every_month_text);
        year = getString(R.string.data_CheckBox_every_year_text);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, AlarmReceiver.class);

        int alarmId = UUID.randomUUID().hashCode();

        intent.putExtra("EXTRA_PHONE_NUMBER", phonenumber);
        intent.putExtra("EXTRA_MESSAGES", message);
        intent.putExtra("EXTRA_ALARMID", alarmId);
        intent.putExtra("EXTRA_DATESTART", dateTimeString);
        intent.putExtra("EXTRA_REPEATSMS", repeatSmS);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, alarmId, intent, PendingIntent.FLAG_MUTABLE);
        try {
            Date date = sdf.parse(dateTimeString);
            long triggerTime = date.getTime();

            long intervalMillis = 0;

            if (repeatSmS.equalsIgnoreCase(day)) {
                intervalMillis = AlarmManager.INTERVAL_DAY;
            } else if (repeatSmS.equalsIgnoreCase(week)) {
                intervalMillis = AlarmManager.INTERVAL_DAY * 7;
            } else if (repeatSmS.equalsIgnoreCase(month)) {
                intervalMillis = AlarmManager.INTERVAL_DAY * 30;
            } else if (repeatSmS.equalsIgnoreCase(year)) {
                intervalMillis = AlarmManager.INTERVAL_DAY * 365;
            }

                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                );

            saveAlarmDetails(this, alarmId, triggerTime,repeatSmS);
            Log.d("AlarmDetails",
                    "ID: " + alarmId
                            + "\n triggerTime: " + triggerTime
                            + "\n intervalMillis: "+ intervalMillis
                            +  "\n pendingIntent"+ pendingIntent
                            + "\n DateStart"+ DateStart
                            + "\n Clock_Time"+ Clock_Time
                            + "\n dateTimeString"+ dateTimeString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    // Save alarm details in shared preferences
    static void saveAlarmDetails(Context mainActivity, int alarmId, long triggerTime, String repeatSmS) {
        SharedPreferences preferences = mainActivity.getSharedPreferences("AlarmDetails", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        // Use unique keys for each alarm and each value
        String triggerTimeKey = "triggerTime_" + alarmId;
        String getRepeatSmSKey = "getRepeatSmSKey_"+ alarmId;

        // Save the triggerTime and releaseTime using their respective keys
        editor.putLong(triggerTimeKey, triggerTime);
        editor.putString(getRepeatSmSKey,repeatSmS);

        editor.apply();
    }

    public class AlarmDetails {
        private int alarmId;
        private long triggerTime;

        private String repeatSmS;


        public AlarmDetails(int alarmId, long triggerTime, String repeatSmS) {
            this.alarmId = alarmId;
            this.triggerTime = triggerTime;
            this.repeatSmS = repeatSmS;
        }

        public int getAlarmId() {
            return alarmId;
        }

        public long getTimeInMillis() {
            return triggerTime;
        }

        public String getRepeatSmS(){return repeatSmS;}

    }

    // Retrieve a list of all alarms
    public List<AlarmDetails> getAllAlarms(Context context) {
        List<AlarmDetails> alarmList = new ArrayList<>();
        SharedPreferences preferences = context.getSharedPreferences("AlarmDetails", Context.MODE_PRIVATE);

        Set<Integer> uniqueAlarmIds = new HashSet<>();

        // Iterate through all saved alarms and add them to the list
        Map<String, ?> allEntries = preferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String key = entry.getKey();

            // Separate keys for triggerTime and releaseTime
            String triggerTimeKey = "triggerTime_" + key.substring(key.lastIndexOf("_") + 1);
            String releaseTimeKey = "releaseTime_" + key.substring(key.lastIndexOf("_") + 1);

            String getRepeatSmSKey = "getRepeatSmSKey_" + key.substring(key.lastIndexOf("_") + 1);

            String getRepeatSmS = preferences.getString(getRepeatSmSKey, String.valueOf(0));

            long triggerTime = preferences.getLong(triggerTimeKey, 0);
            long releaseTime = preferences.getLong(releaseTimeKey, 0);

            int alarmId = Integer.parseInt(key.substring(key.lastIndexOf("_") + 1));
            if (!uniqueAlarmIds.contains(alarmId)) {

                AlarmDetails alarmDetails = new AlarmDetails(alarmId, triggerTime,getRepeatSmS);
                alarmList.add(alarmDetails);

                // Lägg till alarmId i set för att undvika dubbletter
                uniqueAlarmIds.add(alarmId);
            }
        }

        return alarmList;
    }

    public static void removeAlarm(Context context,int alarmId){
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, alarmId, intent, PendingIntent.FLAG_MUTABLE);

        // Cancel the alarm
        alarmManager.cancel(pendingIntent);

        // Remove alarm details from shared preferences
        SharedPreferences preferences = context.getSharedPreferences("AlarmDetails", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        // Remove entries for the specified alarmId
        String triggerTimeKey = "triggerTime_" + alarmId;
        String releaseTimeKey = "releaseTime_" + alarmId;
        String getRepeatSmSKey = "getRepeatSmSKey_" + alarmId;

        editor.remove(triggerTimeKey);
        editor.remove(releaseTimeKey);
        editor.remove(getRepeatSmSKey);

        editor.apply();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Toast.makeText(this, "The app has now permission", Toast.LENGTH_SHORT).show();
            } else {
                // Permission denied
                Toast.makeText(this, "Please grant the app permission to send SMS", Toast.LENGTH_SHORT).show();
            }
        }
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

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}
