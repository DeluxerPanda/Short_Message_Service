package se.deluxerpanda.smssender;

import androidx.appcompat.app.AppCompatActivity;

import android.icu.util.Calendar;
import android.os.Bundle;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private EditText phoneNumberEditText;
    private EditText timeEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        phoneNumberEditText = findViewById(R.id.phoneNumberEditText);
        timeEditText = findViewById(R.id.timeEditText);


    }

    public void sendSMS(View view) {
        String phoneNumber = phoneNumberEditText.getText().toString();
        String time = timeEditText.getText().toString();



        // Parse the time into hours and minutes
        int hours = Integer.parseInt(time.substring(0, 2));
        int minutes = Integer.parseInt(time.substring(3, 5));

        // Schedule the SMS sending
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, SMSSenderReceiver.class);
        intent.putExtra("phoneNumber", phoneNumber);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.set(AlarmManager.RTC_WAKEUP, getTimeInMillis(hours, minutes), pendingIntent);

        Toast.makeText(this, "SMS will be sent at " + time, Toast.LENGTH_SHORT).show();
    }

    private long getTimeInMillis(int hours, int minutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hours);
        calendar.set(Calendar.MINUTE, minutes);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTimeInMillis();
    }
}
