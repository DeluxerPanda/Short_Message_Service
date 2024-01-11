package se.deluxerpanda.smssender;

import android.annotation.SuppressLint;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

import kotlin.random.URandomKt;

public class AlarmReceiver extends BroadcastReceiver {

private String number;
private String message;
private String DateStart;
private String DateEnd;

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {
        SmsManager smsManager = SmsManager.getDefault();

        number = intent.getStringExtra("EXTRA_PHONE_NUMBER");
        message = intent.getStringExtra("EXTRA_MESSAGES");

        String alarmId = String.valueOf(intent.getIntExtra("EXTRA_ALARMID", 0));

        DateStart = intent.getStringExtra("EXTRA_DATESTART");
        DateEnd = intent.getStringExtra("EXTRA_DATEEND");

        Date date1;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            date1 = sdf.parse(DateStart);
            Log.d("AlarmDetails-in-TRY",
                    "ID: " + alarmId
                            + "\n Start: " + DateStart
                            + "\n End: " + DateEnd);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

            Log.d("AlarmDetails",
                    "ID: " + alarmId
                            + "\n Start: " + DateStart + "ints? " + date1
                            + "\n End: " + DateEnd);
            Toast.makeText(context, "SMS extends " + DateEnd + " Alarmid: " + alarmId, Toast.LENGTH_LONG).show();

        smsManager.sendTextMessage(number, null, message, null, null);
        Toast.makeText(context, "SMS sent to " + number + " message: " + message + " Alarmid: " + alarmId, Toast.LENGTH_LONG).show();

        sendNotification(context);
        NotificationManager manager2 = context.getSystemService(NotificationManager.class);
        if (manager2.getNotificationChannel(MainActivity.CHANNEL_ID) != null) {
            MainActivity.removeAlarm(context, Integer.parseInt(alarmId));
        }


    }

    private void sendNotification(Context context) {
        // Check if the device is running Android 8.0 (Oreo) or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the notification channel if it doesn't exist
            // Note: Make sure to define your channel ID and name
            NotificationChannel channel = new NotificationChannel(MainActivity.CHANNEL_ID, MainActivity.CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MainActivity.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(R.string.message_sent+" "+number)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        Random random = new Random();
        int notificationId = random.nextInt();
        // Create an intent for the notification
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        builder.setContentIntent(pendingIntent);

        // Show the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notificationId, builder.build());
    }

}
