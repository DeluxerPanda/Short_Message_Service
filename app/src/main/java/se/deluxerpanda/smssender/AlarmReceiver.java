package se.deluxerpanda.smssender;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        // Perform the task you want to do when the alarm is triggered
        SmsManager smsManager = SmsManager.getDefault();
        String phone = intent.getStringExtra("EXTRA_PHONE_NUMBER");
        String text = intent.getStringExtra("EXTRA_MESSAGE");

          smsManager.sendTextMessage(text, null, phone, null, null);
         Toast.makeText(context, "SMS sent!", Toast.LENGTH_SHORT).show();


        Toast.makeText(context, phone + "" + text, Toast.LENGTH_SHORT).show();
}
}
