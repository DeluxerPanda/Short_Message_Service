package se.deluxerpanda.smssender;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AlertCreator {
    private static EditText phoneNumberEditText;
    private static EditText messageEditText;
    public static void showAlertBox_only_ok(Context context, String title, String message) {
        // Create an AlertDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // Set the title and message for the dialog
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Handle the "OK" button click if needed
                        dialog.dismiss(); // Dismiss the dialog
                    }
                });

        // Create and show the dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public static void showAlertBox_for_History_info(Context context, String title, String message, int alarmId) {
        // Create an AlertDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        // Set the title and message for the dialog
        builder.setTitle(title)
                .setMessage(message)
                .setNeutralButton("OKi", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                })
                .setNegativeButton("Edit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(context, "Edit comming soon!", Toast.LENGTH_LONG).show();
                    }
                })



                .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(context, "Deleted: " + alarmId, Toast.LENGTH_LONG).show();
                        MainActivity.deleteAlarm(alarmId, context);
                    }
                });



        AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button DeleteButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                DeleteButton.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));

                Button EditButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
                EditButton.setTextColor(ContextCompat.getColor(context, android.R.color.holo_orange_dark));

                Button OkieButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEUTRAL);
                OkieButton.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
            }
        });

        // Create and show the dialog
        alertDialog.show();
    }
    private static List<MainActivity.AlarmDetails> getAllAlarms(Context context) {
        List<MainActivity.AlarmDetails> alarmList = new ArrayList<>();
        SharedPreferences preferences = context.getSharedPreferences("AlarmDetails", Context.MODE_PRIVATE);

        Set<Integer> uniqueAlarmIds = new HashSet<>();

        // Iterate through all saved alarms and add them to the list
        Map<String, ?> allEntries = preferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String key = entry.getKey();

            // Separate keys for triggerTime and releaseTime
            String triggerTimeKey = "triggerTime_" + key.substring(key.lastIndexOf("_") + 1);

            String getRepeatSmSKey = "getRepeatSmSKey_" + key.substring(key.lastIndexOf("_") + 1);

            String getPhonenumberKey = "getPhoneNumberKey_" + key.substring(key.lastIndexOf("_") + 1);
            String getPhonenumber = preferences.getString(getPhonenumberKey, String.valueOf(0));

            String getMessageKey = "getMessageKey_" + key.substring(key.lastIndexOf("_") + 1);
            String getMessage = preferences.getString(getMessageKey, String.valueOf(0));

            String getRepeatSmS = preferences.getString(getRepeatSmSKey, String.valueOf(0));

            long triggerTime = preferences.getLong(triggerTimeKey, 0);

            int alarmId = Integer.parseInt(key.substring(key.lastIndexOf("_") + 1));
            if (!uniqueAlarmIds.contains(alarmId)) {

                MainActivity.AlarmDetails alarmDetails = new MainActivity.AlarmDetails(alarmId, triggerTime,getRepeatSmS,getPhonenumber, getMessage);
                alarmList.add(alarmDetails);

                // Lägg till alarmId i set för att undvika dubbletter
                uniqueAlarmIds.add(alarmId);
            }
        }

        return alarmList;
    }


}
