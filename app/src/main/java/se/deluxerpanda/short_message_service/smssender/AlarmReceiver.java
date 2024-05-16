package se.deluxerpanda.short_message_service.smssender;


import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import se.deluxerpanda.short_message_service.R;
import se.deluxerpanda.short_message_service.profile.ProfileActivity;
import se.deluxerpanda.short_message_service.scheduled.ScheduledList;

public class AlarmReceiver extends BroadcastReceiver {

    private String phonenumber;
    private String Phonenumber_Multi;
    private String message;
    private long triggerTime;
    private String repeatSmS;
    private int alarmId;

    private String week;
    private String day;
    private String month;
    private String year;


    @Override
    public void onReceive(Context context, Intent intent) {

        phonenumber = intent.getStringExtra("EXTRA_PHONE_NUMBER");

        message = intent.getStringExtra("EXTRA_MESSAGES");

        triggerTime = intent.getLongExtra("EXTRA_TRIGGERTIME", 0);

        repeatSmS = intent.getStringExtra("EXTRA_REPEATSMS");

        alarmId = Integer.parseInt(String.valueOf(intent.getIntExtra("EXTRA_ALARMID", 0)));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            SmsManager smsManager = context.getSystemService(SmsManager.class);
            if (phonenumber.contains(",")) {
                Phonenumber_Multi = phonenumber;
                String[] phoneNumbersArray = phonenumber.split(",\\s*");
                for (String phoneNumber : phoneNumbersArray) {
                    ArrayList<String> parts = smsManager.divideMessage(message);
                    smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null);
                }
            } else {
                Phonenumber_Multi = "false";
                Log.d("Hmmmm ", " phonenumber: " + phonenumber + " message: " + message);
                smsManager.sendTextMessage(phonenumber, null, message, null, null);
            }
            rescheduleAlarm(phonenumber, Phonenumber_Multi, message, context, triggerTime, repeatSmS, alarmId);
            sendNotification(context);
        } else {
            SmsManager smsManager = SmsManager.getDefault();
            if (phonenumber.contains(",")) {
                Phonenumber_Multi = phonenumber;
                String[] phoneNumbersArray = phonenumber.split(",\\s*");
                for (String phoneNumber : phoneNumbersArray) {
                    ArrayList<String> parts = smsManager.divideMessage(message);
                    smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null);
                }
            } else {
                Phonenumber_Multi = "false";
                Log.d("Hmmmm ", " phonenumber: " + phonenumber + " message: " + message);
                smsManager.sendTextMessage(phonenumber, null, message, null, null);
            }
            rescheduleAlarm(phonenumber, Phonenumber_Multi, message, context, triggerTime, repeatSmS, alarmId);
            sendNotification(context);
        }

    }

    private void rescheduleAlarm(String phonenumber, String Phonenumber_Multi, String message, Context context, long triggerTime, String repeatSmS, int alarmId) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date date = new Date(triggerTime);

        day = context.getString(R.string.send_sms_every_day_text);
        week = context.getString(R.string.send_sms_every_week_text);
        month = context.getString(R.string.send_sms_every_month_text);
        year = context.getString(R.string.send_sms_every_year_text);

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

        long newtriggerTime = date.getTime() + intervalMillis;

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlarmReceiver.class);

        if (Phonenumber_Multi != "false") {
            intent.putExtra("EXTRA_PHONE_NUMBER", phonenumber);
        } else {
            intent.putExtra("EXTRA_PHONE_NUMBER", phonenumber);
        }
        intent.putExtra("EXTRA_MESSAGES", message);
        intent.putExtra("EXTRA_ALARMID", alarmId);
        intent.putExtra("EXTRA_TRIGGERTIME", newtriggerTime);
        intent.putExtra("EXTRA_REPEATSMS", repeatSmS);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        }else {
            context.startService(intent);
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, alarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

        alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                newtriggerTime,
                pendingIntent
        );

        MainActivity.saveAlarmDetails(context, alarmId, newtriggerTime, repeatSmS, phonenumber, message);
    }

    private void sendNotification(Context context) {
        NotificationChannel channel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(MainActivity.CHANNEL_ID, MainActivity.CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        }
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(channel);
        }

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MainActivity.CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher_foreground))
                .setContentTitle(context.getResources().getString(R.string.sms_notify_message_sent_number_text) + " " + phonenumber)
                .setContentText(context.getResources().getString(R.string.sms_notify_message_sent_message_text) + " " + message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        Random random = new Random();
        int notificationId = random.nextInt();

        // Create an intent for the notification
        Intent notificationIntent = new Intent(context, ScheduledList.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_MUTABLE);

        builder.setContentIntent(pendingIntent);

        // Show the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(notificationId, builder.build());
    }
}