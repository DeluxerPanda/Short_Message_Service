package se.deluxerpanda.smssender;

import android.telephony.SmsManager;
import android.widget.Toast;

public class SMSservice {
    private void sendSMS(String phoneNumber, String message) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, message, null, null);
    }

}
