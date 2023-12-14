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

        String number = intent.getStringExtra("EXTRA_PHONE_NUMBER");
        String message = intent.getStringExtra("EXTRA_MESSAGES");


          smsManager.sendTextMessage(number, null, message, null, null);
         Toast.makeText(context, "SMS sent to "+number+"message: "+message, Toast.LENGTH_LONG).show();

        //AlderCreator.showAlertBox_only_ok( context,"test", "This is the alert message.");



    }
}
