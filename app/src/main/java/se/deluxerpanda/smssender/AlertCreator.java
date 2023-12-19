package se.deluxerpanda.smssender;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AlertCreator {
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
                        Toast.makeText(context, "Oki", Toast.LENGTH_LONG).show();
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
                        Toast.makeText(context, "(Not working) Deleted"+alarmId, Toast.LENGTH_LONG).show();
                     //   List<MainActivity.AlarmDetails> alarmList = new ArrayList<>();
                      //  SharedPreferences preferences = context.getSharedPreferences("AlarmDetails", Context.MODE_PRIVATE);
                      //  Set<Integer> uniqueAlarmIds = new HashSet<>();
                      //  uniqueAlarmIds.remove(String.valueOf(alarmId));
                      //  Intent intent = new Intent(context, MainActivity.class); // Replace YourAlarmReceiver with the actual class handling the alarm
                      //  PendingIntent pendingIntent = PendingIntent.getBroadcast(context, alarmId, intent, 0);

                       // alarmManager.cancel(pendingIntent);

                        //pendingIntent.cancel();
                    }
                });

        // Set the color of the "DELETE" button to red
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

}
