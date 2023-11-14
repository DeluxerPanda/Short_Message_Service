package se.deluxerpanda.smssender;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        SmsManager smsManager = SmsManager.getDefault();

        String phonenumber = intent.getStringExtra("EXTRA_PHONE_NUMBER");
        String message = intent.getStringExtra("EXTRA_MESSAGES");


          smsManager.sendTextMessage(phonenumber, null, message, null, null);
         Toast.makeText(context, "SMS sent to "+phonenumber+"message: "+message, Toast.LENGTH_LONG).show();

        AlderCreator.showAlertBox_only_ok(context,"test", "This is the alert message.");


    }
}
