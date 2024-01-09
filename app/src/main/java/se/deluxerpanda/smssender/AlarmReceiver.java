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
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class AlarmReceiver extends BroadcastReceiver {
public static String CHANNEL_ID = "dusofjuoedf";
private String Str_number;
private String Str_message;
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {
        SmsManager smsManager = SmsManager.getDefault();

        String number = intent.getStringExtra("EXTRA_PHONE_NUMBER");
        String message = intent.getStringExtra("EXTRA_MESSAGES");
        String alarmId = String.valueOf(intent.getIntExtra("EXTRA_ALARMID",0));
      //  Str_number = number;
       // Str_message = message;
        smsManager.sendTextMessage(number, null, message, null, null);
        Toast.makeText(context, "SMS sent to " + number + " message: " + message +" Alarmid: "+alarmId, Toast.LENGTH_LONG).show();


        sendNotification(context);
        NotificationManager manager2 = context.getSystemService(NotificationManager.class);
        if (manager2.getNotificationChannel(CHANNEL_ID) != null) {
            MainActivity.removeAlarm(context,Integer.parseInt(alarmId));
        }

    }
    private String CHANNEL_NAME = String.valueOf(R.string.app_name);
    private void sendNotification(Context context) {
        // Check if the device is running Android 8.0 (Oreo) or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the notification channel if it doesn't exist
            // Note: Make sure to define your channel ID and name
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(Str_number)
                .setContentText(Str_message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        int notificationId = 123;
        // Create an intent for the notification
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        builder.setContentIntent(pendingIntent);

        // Show the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notificationId, builder.build());
    }

}
