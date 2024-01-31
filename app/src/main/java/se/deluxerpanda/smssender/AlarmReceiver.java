package se.deluxerpanda.smssender;

import android.annotation.SuppressLint;

import android.app.AlarmManager;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import kotlin.random.URandomKt;

public class AlarmReceiver extends BroadcastReceiver {

private String number;
private String message;
private String dateStart;
private  String repeatSmS;

    private String day;
    private String week;
    private String month;
    private String year;
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {
        SmsManager smsManager = SmsManager.getDefault();

        number = intent.getStringExtra("EXTRA_PHONE_NUMBER");
        message = intent.getStringExtra("EXTRA_MESSAGES");

     String   alarmId = String.valueOf(intent.getIntExtra("EXTRA_ALARMID", 0));

        dateStart = intent.getStringExtra("EXTRA_DATESTART");

        repeatSmS = intent.getStringExtra("EXTRA_REPEATSMS");
        rescheduleAlarm(context, repeatSmS);

        Log.d("AlarmDetails",
                "ID: " + alarmId
                        + "\n Start: " + dateStart
                        + "\n Repeat evry: "+ repeatSmS);

        smsManager.sendTextMessage(number, null, message, null, null);
        Toast.makeText(context, "SMS sent to " + number + " message: " + message + " Alarmid: " + alarmId, Toast.LENGTH_LONG).show();
        sendNotification(context);
        MainActivity.removeAlarm(context, Integer.parseInt(alarmId));
    }

    private void rescheduleAlarm(Context context, String repeatSmS) {

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlarmReceiver.class);

        long intervalMillis = 0;
        day = context.getString(R.string.send_sms_every_day_text);
        week = context.getString(R.string.send_sms_every_week_text);
        month = context.getString(R.string.send_sms_every_month_text);
        year = context.getString(R.string.send_sms_every_year_text);
        if (repeatSmS.equalsIgnoreCase(day)) {
            intervalMillis = AlarmManager.INTERVAL_DAY;
        } else if (repeatSmS.equalsIgnoreCase(week)) {
            intervalMillis = AlarmManager.INTERVAL_DAY * 7;
        } else if (repeatSmS.equalsIgnoreCase(month)) {
            intervalMillis = AlarmManager.INTERVAL_DAY * 30;
        } else if (repeatSmS.equalsIgnoreCase(year)) {
            intervalMillis = AlarmManager.INTERVAL_DAY * 365;
        }
        int alarmId = UUID.randomUUID().hashCode();
        context.startForegroundService(intent);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, alarmId, intent,  PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd H:m");
            Date date = sdf.parse(dateStart);
            long triggerTime = date.getTime() + intervalMillis;

            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
            );
            Log.d("AlarmDetails",
                    "day: " + triggerTime
                            + "\n Start: " + dateStart
                            + "\n Repeat evry: "+ repeatSmS);
            String phonenumber = number;
            MainActivity.saveAlarmDetails(context, alarmId, triggerTime,repeatSmS,phonenumber,message);
        } catch (ParseException e) {
            e.printStackTrace();
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
                .setContentTitle(context.getResources().getString(R.string.message_sent)+" "+number)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        Random random = new Random();
        int notificationId = random.nextInt();
        // Create an intent for the notification
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_MUTABLE);

        builder.setContentIntent(pendingIntent);

        // Show the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notificationId, builder.build());
    }
}
