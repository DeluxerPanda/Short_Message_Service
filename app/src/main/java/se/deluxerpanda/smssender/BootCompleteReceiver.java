package se.deluxerpanda.smssender;

import static java.sql.DriverManager.println;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class BootCompleteReceiver extends BroadcastReceiver {
    private String day;
    private String week;
    private String month;
    private String year;
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.d("BootCompleteReceiver", "Device booted.");


            List<MainActivity.AlarmDetails> alarmList = getAllAlarms(context);

            for (MainActivity.AlarmDetails alarm : alarmList) {
                if (alarm != null) {
           String phonenumber = alarm.getPhonenumber();
           String message = alarm.getMessage();
           long triggerTime = alarm.getTimeInMillis();
           String repeatSmS = alarm.getRepeatSmS();
               int alarmId = alarm.getAlarmId();
                        scheduleAlarm(phonenumber,message, context, triggerTime, repeatSmS, alarmId);
                }
            }
            }
        }

    private void scheduleAlarm(String phonenumber, String message, Context context, long triggerTime, String repeatSmS, int alarmId){

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

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlarmReceiver.class);

        intent.putExtra("EXTRA_PHONE_NUMBER", phonenumber);
        intent.putExtra("EXTRA_MESSAGES", message);
        intent.putExtra("EXTRA_ALARMID", alarmId);
        intent.putExtra("EXTRA_TRIGGERTIME", triggerTime);
        intent.putExtra("EXTRA_REPEATSMS", repeatSmS);

            context.startForegroundService(intent);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, alarmId, intent,  PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);


        alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
        );

        MainActivity.saveAlarmDetails(context, alarmId, triggerTime,repeatSmS, phonenumber,message);
    }

    private List<MainActivity.AlarmDetails> getAllAlarms(Context context) {
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
