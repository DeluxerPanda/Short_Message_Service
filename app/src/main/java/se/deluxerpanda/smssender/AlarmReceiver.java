package se.deluxerpanda.smssender;

import android.annotation.SuppressLint;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {
        SmsManager smsManager = SmsManager.getDefault();

        String number = intent.getStringExtra("EXTRA_PHONE_NUMBER");
        String message = intent.getStringExtra("EXTRA_MESSAGES");
        String alarmId = String.valueOf(intent.getIntExtra("EXTRA_ALARMID",0));

        smsManager.sendTextMessage(number, null, message, null, null);
        Toast.makeText(context, "SMS sent to " + number + " message: " + message +" Alarmid: "+alarmId, Toast.LENGTH_LONG).show();


          MainActivity.removeAlarm(context,Integer.parseInt(alarmId));
    }

}
