package se.deluxerpanda.smssender;
import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
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
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private static final int SMS_PERMISSION_REQUEST_CODE = 1;
    private EditText phoneNumberEditText, messageEditText;

    private static TextView SetTimeText;
    private static TextView SetDateStartText;
    private static TextView SetDateEndsText;
    private LinearLayout pickDateEndsBox;
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
        int minute = c.get(Calendar.MINUTE);
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
            String phoneNumber = phoneNumberEditText.getText().toString();
            String message = messageEditText.getText().toString();
            if (hasSendSmsPermission()) {
                if (!phoneNumber.isEmpty() && !message.isEmpty()) {
                    sendSMS(phoneNumber, message);
                } else {
                    showMessage("Please fill in both phone number and message fields.");
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
                DatePickerFragment datePickerFragment = new DatePickerFragment();
                datePickerFragment.show(getSupportFragmentManager(), "datePicker");
        });
        Button pickDateEndsButton = findViewById(R.id.pickDateEnds);
        pickDateEndsButton.setOnClickListener(view -> {
            DatePickerFragment datePickerFragment = new DatePickerFragment();
            datePickerFragment.show(getSupportFragmentManager(), "datePicker");
        });

        CheckBox pickDateEndsCheckBox = findViewById(R.id.checkBox);
        pickDateEndsCheckBox.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                // CheckBox is checked
                pickDateEndsBox.setVisibility(View.GONE);
            } else {
                // CheckBox is unchecked
                pickDateEndsBox.setVisibility(View.VISIBLE);
            }
        });



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
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it with 24-hour format.
            return new TimePickerDialog(getActivity(), this, hour, minute, true);
        }

        public void onTimeSet(TimePicker view, int hour , int minute) {
            // Do something with the time the user picks.
            String timeText = hour  + ":" + minute;
            SetTimeText.setText(" "+timeText);
        }
    }
//time  Dialog (ends)

//date Dialog Start  (start)
public static class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker.
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it.
        return new DatePickerDialog(requireContext(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        // Do something with the date the user picks.
        String formattedDate = String.format("%04d-%02d-%02d", year, month + 1, day); // Adjust month by +1 since it's 0-based
        SetDateStartText.setText(" " + formattedDate);

        String formattedDate2 = SetDateEndsText.getText().toString();

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date1 = dateFormat.parse(formattedDate);
            Date date2 = dateFormat.parse(formattedDate2);

            if (date1.compareTo(date2) > 0) {
                // date1 is after date2
                SetDateEndsText.setText("aaaa");
            } else if (date1.compareTo(date2) < 0) {
                // date1 is before date2
                SetDateEndsText.setText("bbb");
            } else {
                // date1 and date2 are equal
                SetDateEndsText.setText("hhhhh");
            }
        } catch (ParseException e) {
            // Handle parsing exceptions if the date strings are not in the expected format
            SetDateEndsText.setText("ööööö");
        }

    }
}
// data Dialog start (ends)

//date Dialog ends  (start)
    public static class DatePickerFragment2 extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker.
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it.
            return new DatePickerDialog(requireContext(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date the user picks.
            String formattedDate = String.format("%04d-%02d-%02d", year, month + 1, day); // Adjust month by +1 since it's 0-based
            SetDateStartText.setText(" " + formattedDate);
        }
    }
// data Dialog ends (ends)

    private void sendSMS(String phoneNumber, String message) {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
        showMessage("SMS sent");
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
    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
