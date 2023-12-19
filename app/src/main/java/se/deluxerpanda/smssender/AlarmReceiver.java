package se.deluxerpanda.smssender;

import static androidx.core.content.ContextCompat.getSystemService;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.SmsManager;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;


public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SmsManager smsManager = SmsManager.getDefault();

        String number = intent.getStringExtra("EXTRA_PHONE_NUMBER");
        String message = intent.getStringExtra("EXTRA_MESSAGES");
        String alarmid = intent.getStringExtra("EXTRA_ALARMID");

        smsManager.sendTextMessage(number, null, message, null, null);
        Toast.makeText(context, "SMS sent to " + number + " message: " + message +"Alarmid "+alarmid, Toast.LENGTH_LONG).show();

        //AlderCreator.showAlertBox_only_ok( context,"test", "This is the alert message.");

        try {
            List<MainActivity.AlarmDetails> alarmList = new ArrayList<>();
            SharedPreferences preferences = context.getSharedPreferences("AlarmDetails", Context.MODE_PRIVATE);
            Map<String, ?> allEntries = preferences.getAll();
            Set<Integer> uniqueAlarmIds = new HashSet<>();
            uniqueAlarmIds.remove(alarmid);
           // MainActivity instance = new MainActivity();
           // instance.History_info();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

}
