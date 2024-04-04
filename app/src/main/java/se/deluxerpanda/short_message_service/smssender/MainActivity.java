package se.deluxerpanda.short_message_service.smssender;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.snackbar.Snackbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;
import se.deluxerpanda.short_message_service.scheduled.ScheduledList;
import se.deluxerpanda.short_message_service.R;
import se.deluxerpanda.short_message_service.scheduled.ProfileActivity;
import se.deluxerpanda.short_message_service.smssender.AlarmReceiver;
import se.deluxerpanda.short_message_service.smssender.PhoneListActivity;

public class MainActivity extends AppCompatActivity {
    private static final int SMS_PERMISSION_REQUEST_CODE = 1;
    private EditText phoneNumberEditText, messageEditText;

    private static TextView SetTimeText;
    private static TextView SetDateStartText;
    private static TextView selectedOptionText;

    private static TextView addNumbers;
    private LinearLayout pickDateEndsBox;
    public static int timeHourSaved = -1;
    public static int timeMinuteSaved = -1;

    private static String startDate;
    private static String endDate;
    public static String CHANNEL_ID = String.valueOf(UUID.randomUUID().hashCode());

    public static String CHANNEL_NAME = String.valueOf(R.string.app_name);

    private String day;
    private String week;
    private String month;
    private String year;
    private static int hour;
    private static int minute;
    private int selectedOptionIndex;
    private  int permissionCheck;

    private static int[] counterLeft = {0};
    private static int counterMax = 29;
    private int phoneNumberEditTextID;
    static HashMap<Integer, EditText> editTextMap = new HashMap<>();
    private boolean hasSendSmsPermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }


    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.SEND_SMS, Manifest.permission.READ_CONTACTS, Manifest.permission.POST_NOTIFICATIONS},
                SMS_PERMISSION_REQUEST_CODE);
    }
    // SetTimeText
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!hasSendSmsPermission()) {
            requestPermission();
        }
        setContentView((R.layout.activity_main));
        counterLeft = new int[]{0};
        // Use the current date as the default date in the picker.
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        hour = c.get(Calendar.HOUR_OF_DAY);
        minute = c.get(Calendar.MINUTE)+ 6;
        String formattedDate = String.format("%04d-%02d-%02d", year, month + 1, day); // Adjust month by +1 since it's 0-based
        String timeText = String.format("%02d:%02d", hour, minute);

        phoneNumberEditText = findViewById(R.id.phoneNumberEditText);

        messageEditText = findViewById(R.id.messageEditText);

        SetTimeText = findViewById(R.id.pickTime);

        SetDateStartText = findViewById(R.id.SetDateStartText);

        selectedOptionText = findViewById(R.id.selectedSendEvery);


        SetTimeText.setText(" "+timeText);
        SetDateStartText.setText(" " + formattedDate);

        ImageView btnToHamburger = findViewById(R.id.btnToHamburger);
        btnToHamburger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                Toast.makeText(MainActivity.this, "Coming Soon!", Toast.LENGTH_SHORT).show();
            }
        });

        ImageView btnToContacts = findViewById(R.id.btnToContacts);
        btnToContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                phoneNumberEditTextID = phoneNumberEditText.getId();
                Intent intent = new Intent(MainActivity.this, PhoneListActivity.class);
                PhoneListActivityLauncher.launch(intent);
            }
        });


        TextView addNumbers = findViewById(R.id.addNumbers);
        addNumbers.setText(getResources().getString(R.string.text_add_phone_number)+" "+ counterLeft[0] +" / "+counterMax + " (BETA)");
        addNumbers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addMoreNumbers();
            }
        });

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
                if (!phonenumber.isEmpty() && !message.isEmpty()) {
                    if (message.length() <= 160) {
                        if (selectedDateTime != null && selectedDateTime.getTime() > currentTimeInMillis) {
                            scheduleSMS(phonenumber,message);
                            hideKeyboard();
                            History_info();

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
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle(getResources().getString(R.string.sms_max_characters_titel));
                        builder.setMessage(getResources().getString(R.string.sms_max_characters_Text)+ "\n"+
                                getResources().getString(R.string.sms_max_characters_Text_int)+" "+message.length());
                        builder.setPositiveButton(getResources().getString(R.string.text_ok), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });

                        builder.show();
                    }
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(getResources().getString(R.string.sms_number_or_masage_are_empty_titel));
                    builder.setMessage(getResources().getString(R.string.sms_number_or_masage_are_empty_text));
                    builder.setPositiveButton(getResources().getString(R.string.text_ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.show();
                }
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getResources().getString(R.string.sms_no_permission_sms_titel));
                builder.setMessage(getResources().getString(R.string.sms_no_permission_sms_text));
                builder.setPositiveButton(getResources().getString(R.string.text_ask_give_permission), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        onBackPressed();
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);

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


        Button pickDateStartButton = findViewById(R.id.SetDateStartText);
        pickDateStartButton.setOnClickListener(view -> {
            showDatePicker();
        });

        Button chooseOptionButton = findViewById(R.id.selectedSendEvery);
        chooseOptionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOptionsDialog();
            }
        });
        History_info();
    }

    private ActivityResultLauncher<Intent> PhoneListActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        TextView textView = findViewById(phoneNumberEditTextID);

                        if (textView != null && textView instanceof TextView) {
                            Intent data = result.getData();
                            String phoneNumber = data.getStringExtra("PHONE_NUMBER_FROM_CONTACTS");
                            textView.setText(phoneNumber);
                        }
                    }
                }
            });
    public void showOptionsDialog() {
        // Get the layout inflater
        LayoutInflater inflater = getLayoutInflater();

        String[] choices = {
                getString(R.string.send_sms_every_year_text),
                getString(R.string.send_sms_every_month_text),
                getString(R.string.send_sms_every_week_text),
                getString(R.string.send_sms_every_day_text)
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
                .setTitle(getString(R.string.send_sms_every_text))
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

    public void addMoreNumbers(){
        if (counterLeft[0] != counterMax) {
            TextView addNumbers = findViewById(R.id.addNumbers);
            counterLeft[0]++;
            addNumbers.setText(getResources().getString(R.string.text_add_phone_number) + " " + counterLeft[0] + " / " + counterMax + " (BETA)");
            LinearLayout parentLayout = findViewById(R.id.numbersContainer);
            hideKeyboard();

            View dynamicTextViewLayout = getLayoutInflater().inflate(R.layout.add_number_layout, null);

            // Generate a unique ID for the TextView
            int dynamicTextViewId = View.generateViewId();

            EditText dynamicEditText = dynamicTextViewLayout.findViewById(R.id.phoneNumberEditText);

           //     dynamicEditText.setText("+" + counterLeft[0] +"123");

            dynamicEditText.setId(dynamicTextViewId);
            editTextMap.put(dynamicTextViewId, dynamicEditText);
            ImageView contactButton = dynamicTextViewLayout.findViewById(R.id.btnToContacts);
            contactButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideKeyboard();
                    phoneNumberEditTextID = dynamicEditText.getId();
                    Intent intent = new Intent(MainActivity.this, PhoneListActivity.class);
                    PhoneListActivityLauncher.launch(intent);
                }
            });

            ImageView deleteButton = dynamicTextViewLayout.findViewById(R.id.btnToDeleteNumber);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    parentLayout.removeView(dynamicTextViewLayout);
                    counterLeft[0]--;
                    addNumbers.setText(getResources().getString(R.string.text_add_phone_number) + " " + counterLeft[0] + " / " + counterMax + " (BETA)");
                }
            });

            // Add the dynamic TextView layout to the parent layout
            parentLayout.addView(dynamicTextViewLayout);
        }
    }
    private void showDatePicker() {
        DatePickerFragment datePickerFragment = new DatePickerFragment();

        datePickerFragment.show(getSupportFragmentManager(), "datePicker");
    }


    //time Dialog (start)
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
            AlarmListIsEmptyTextView.setText(getResources().getString(R.string.history_info_no_SMS_scheduled));
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
                String phonenumber = alarmDetails.getPhonenumber();
                String message = alarmDetails.getMessage();
                View dynamicTextViewLayout = getLayoutInflater().inflate(R.layout.history_info, null);
                LinearLayout dynamicLinearLayout = dynamicTextViewLayout.findViewById(R.id.history_info_page);
                TextView history_info_contact_name_TextView = dynamicTextViewLayout.findViewById(R.id.history_info_contact_name);
                String title;
                String inputString = phonenumber;
                String contactName = null;
                String photoUri_result = null;
                StringBuilder concatenatedNames = new StringBuilder();
                int permissionCheckContacts = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
                ImageView contactImageView = dynamicTextViewLayout.findViewById(R.id.history_info_contact_profile);
                if (permissionCheckContacts == PackageManager.PERMISSION_GRANTED){
                    ContentResolver contentResolver = getContentResolver();
                    if (phonenumber.contains(",")){
// Creating a StringTokenizer object with delimiter ","
                        StringTokenizer tokenizer = new StringTokenizer(inputString, ",");
                        int tokenCount = tokenizer.countTokens();
                        String[] stringArray = new String[tokenCount];
// Converting each token to array elements
                        for (int i = 0; i < tokenCount; i++) {
                            stringArray[i] = tokenizer.nextToken();
                        }
// Printing the output array
                        for (String element : stringArray) {
                            contactName = getContactLastName(contentResolver, element);
                            concatenatedNames.append(contactName).append(", ");
                        }
                        history_info_contact_name_TextView.setText(concatenatedNames);
                        title = String.valueOf(concatenatedNames);
                    }else {
                        contactName = getContactName(contentResolver, phonenumber);
                        if (contactName != null) {
                            history_info_contact_name_TextView.setText(contactName);
                            title = String.valueOf(contactName);
                        } else {
                            history_info_contact_name_TextView.setText(phonenumber);
                            title = String.valueOf(phonenumber);
                        }
                    }
                } else {
                    history_info_contact_name_TextView.setText(phonenumber);
                    title = String.valueOf(phonenumber);
                }
                Uri photoUri = getContactPhotoUri(contactName);
                if (photoUri != null) {
                    // Load the contact photo into the ImageView
                    contactImageView.setImageURI(photoUri);
                    // Create a rounded drawable and set it directly to the ImageView
                    RoundedBitmapDrawable roundedDrawable = RoundedBitmapDrawableFactory.create(getResources(), ((BitmapDrawable) contactImageView.getDrawable()).getBitmap());
                    roundedDrawable.setCircular(true); // Set to true if you want circular corners
                    contactImageView.setImageDrawable(roundedDrawable);
                    photoUri_result = photoUri.toString();
                }else {
                    contactImageView.setImageResource(R.drawable.ic_baseline_person_24);
                    photoUri_result = null;
                }
                String[] words = phonenumber.split(",");
                StringBuilder output = new StringBuilder();
                for (String word : words) {
                    output.append(word.trim()).append("\n");
                }
                String phonenumber_result = output.toString();
                TextView history_info_message_TextView = dynamicTextViewLayout.findViewById(R.id.history_info_message);
                history_info_message_TextView.setText(message);
                TextView history_info_date_and_time_TextView = dynamicTextViewLayout.findViewById(R.id.history_info_date_and_time);
                String TimeAndDate = formattedDateStart +" | "+ formattedClockTime;
                history_info_date_and_time_TextView.setText(getResources().getString(R.string.history_info_Date_name) +" "+ formattedDateStart
                        + ", "+getResources().getString(R.string.history_info_Time_name)  +" "+ formattedClockTime);
                linearLayout.addView(dynamicTextViewLayout);
                String finalPhotoUri_result;
                if (photoUri_result == null){
                    finalPhotoUri_result = null;
                }else {
                    finalPhotoUri_result = photoUri_result;
                }
                dynamicLinearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                        intent.putExtra("EXTRA_HISTORY_PROFILE_ALARMID", alarmId);
                        intent.putExtra("EXTRA_HISTORY_PROFILE_POTOURL", finalPhotoUri_result);
                        intent.putExtra("EXTRA_HISTORY_PROFILE_TITLE", title);
                        intent.putExtra("EXTRA_HISTORY_PROFILE_TIMEANDDATE", TimeAndDate);
                        intent.putExtra("EXTRA_HISTORY_PROFILE_PHONENUMBER", phonenumber_result);
                        intent.putExtra("EXTRA_HISTORY_PROFILE_MESSAGE", message);
                        startActivity(intent);
                    }
                });
            }
        }
    }
    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

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
            String timeText = String.format("%02d:%02d", hour, minute);
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
            int weekOfyear = c.get(Calendar.WEEK_OF_YEAR);
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
        phonenumber = phonenumber.replaceAll("[/N.,'*;#]", "");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd H:m");
        try {
            Date date = sdf.parse(dateTimeString);
            long triggerTime = date.getTime();

            String repeatSmS = (String) selectedOptionText.getText();

            day = getString(R.string.send_sms_every_day_text);
            week = getString(R.string.send_sms_every_week_text);
            month = getString(R.string.send_sms_every_month_text);
            year = getString(R.string.send_sms_every_year_text);

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

            Intent intent = new Intent(this, AlarmReceiver.class);

            int alarmId = UUID.randomUUID().hashCode();

            if (counterLeft[0] > 0){
                StringBuilder allText = new StringBuilder();

                for (Map.Entry<Integer, EditText> entry : editTextMap.entrySet()) {
                    EditText editText = entry.getValue();
                    allText.append(editText.getText()).append(",");
                }
                String strnum = allText.toString() + phonenumber;
                intent.putExtra("EXTRA_PHONE_NUMBER", strnum);
                phonenumber = strnum;
            }else {
                intent.putExtra("EXTRA_PHONE_NUMBER", phonenumber);
            }
            intent.putExtra("EXTRA_MESSAGES", message);
            intent.putExtra("EXTRA_ALARMID", alarmId);
            intent.putExtra("EXTRA_TRIGGERTIME", triggerTime);
            intent.putExtra("EXTRA_REPEATSMS", repeatSmS);


            startForegroundService(intent);


            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, alarmId, intent,  PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);


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

            saveAlarmDetails(this, alarmId, triggerTime,repeatSmS,phonenumber,message);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    // Save alarm details in shared preferences
    public static void saveAlarmDetails(Context mainActivity, int alarmId, long triggerTime, String repeatSmS, String phonenumber, String message) {
        SharedPreferences preferences = mainActivity.getSharedPreferences("AlarmDetails", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        // Use unique keys for each alarm and each value
        String triggerTimeKey = "triggerTime_" + alarmId;
        String getRepeatSmSKey = "getRepeatSmSKey_"+ alarmId;

        String getPhoneNumberKey = "getPhoneNumberKey_"+ alarmId;
        String getMessageKey = "getMessageKey_"+ alarmId;

        // Save the triggerTime and releaseTime using their respective keys
        editor.putLong(triggerTimeKey, triggerTime);
        editor.putString(getRepeatSmSKey,repeatSmS);

        editor.putString(getPhoneNumberKey,phonenumber);
        editor.putString(getMessageKey,message);

        editor.apply();
    }

    public static class AlarmDetails {
        private int alarmId;
        private long triggerTime;

        private String repeatSmS;
        private String phonenumber;

        private String message;

        public AlarmDetails(int alarmId, long triggerTime, String repeatSmS, String phonenumber, String message) {
            this.alarmId = alarmId;
            this.triggerTime = triggerTime;
            this.repeatSmS = repeatSmS;
            this.phonenumber = phonenumber;
            this.message = message;
        }

        public int getAlarmId() {
            return alarmId;
        }

        public long getTimeInMillis() {
            return triggerTime;
        }

        public String getRepeatSmS(){return repeatSmS;}

        public String getPhonenumber(){return phonenumber;}
        public  String getMessage(){return message;}

    }

    // Retrieve a list of all alarms
    public static List<AlarmDetails> getAllAlarms(Context context) {
        List<AlarmDetails> alarmList = new ArrayList<>();
        SharedPreferences preferences = context.getSharedPreferences("AlarmDetails", Context.MODE_PRIVATE);

        Set<Integer> uniqueAlarmIds = new HashSet<>();

        // Iterate through all saved alarms and add them to the list
        Map<String, ?> allEntries = preferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String key = entry.getKey();

            // Separate keys for triggerTime and releaseTime
            String triggerTimeKey = "triggerTime_" + key.substring(key.lastIndexOf("_") + 1);

            String getRepeatSmSKey = "getRepeatSmSKey_" + key.substring(key.lastIndexOf("_") + 1);

            String getRepeatSmS = preferences.getString(getRepeatSmSKey, String.valueOf(0));

            String getPhonenumberKey = "getPhoneNumberKey_" + key.substring(key.lastIndexOf("_") + 1);
            String getPhonenumber = preferences.getString(getPhonenumberKey, String.valueOf(0));

            String getMessageKey = "getMessageKey_" + key.substring(key.lastIndexOf("_") + 1);
            String getMessage = preferences.getString(getMessageKey, String.valueOf(0));

            long triggerTime = preferences.getLong(triggerTimeKey, 0);

            int alarmId = Integer.parseInt(key.substring(key.lastIndexOf("_") + 1));
            if (!uniqueAlarmIds.contains(alarmId)) {

                AlarmDetails alarmDetails = new AlarmDetails(alarmId, triggerTime,getRepeatSmS,getPhonenumber, getMessage);
                alarmList.add(alarmDetails);

                // Lägg till alarmId i set för att undvika dubbletter
                uniqueAlarmIds.add(alarmId);
            }
        }

        return alarmList;
    }
    public  void deleteAlarm(int alarmId, Context context) {
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
        String getPhonenumber = "getPhoneNumberKey_" + alarmId;
        String getMessage = "getMessageKey_" + alarmId;


        editor.remove(triggerTimeKey);
        editor.remove(releaseTimeKey);
        editor.remove(getRepeatSmSKey);
        editor.remove(getPhonenumber);
        editor.remove(getMessage);

        editor.apply();


        Intent intenta = new Intent(context, MainActivity.class);
        intenta.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intenta);

    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();

        if (view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    public static String getContactLastName(ContentResolver contentResolver, String phoneNumber) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        String[] projection = {ContactsContract.PhoneLookup.DISPLAY_NAME};

        Cursor cursor = contentResolver.query(uri, projection, null, null, null);

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    // Contact exists, extract the first name from the display name
                    String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                    String[] parts = contactName.split("\\s+"); // Split by whitespace
                    return parts[0]; // Return the first part
                }
            } finally {
                cursor.close();
            }
        }
        // Contact doesn't exist
        return null;
    }

    public static String getContactName(ContentResolver contentResolver, String phoneNumber) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        String[] projection = {ContactsContract.PhoneLookup.DISPLAY_NAME};

        Cursor cursor = contentResolver.query(uri, projection, null, null, null);

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    // Contact exists, return the name
                    String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                    return contactName;
                }
            } finally {
                cursor.close();
            }
        }
        // Contact doesn't exist
        return null;
    }

    public Uri getContactPhotoUri(String contactID) {
        if (contactID == null) {
            return null;
        }
        Uri contactUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = {ContactsContract.CommonDataKinds.Phone.PHOTO_URI};
        String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + "=?";
        String[] selectionArgs = {contactID};

        Cursor cursor = getContentResolver().query(contactUri, projection, selection, selectionArgs, null);
        Uri photoUri = null;

        if (cursor != null && cursor.moveToFirst()) {
            String photoUriString = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
            if (photoUriString != null) {
                photoUri = Uri.parse(photoUriString);
            }
            cursor.close();
        }
        return photoUri;
    }

}
