package se.deluxerpanda.short_message_service.smssender

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import se.deluxerpanda.short_message_service.R
import java.text.SimpleDateFormat
import java.util.Date

class BootCompleteReceiver : BroadcastReceiver() {
    private var day: String? = null
    private var week: String? = null
    private var month: String? = null
    private var year: String? = null

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != null && intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootCompleteReceiver", "Device booted.")

            val alarmList = getAllAlarms(context)

            for (alarm in alarmList) {
                val phonenumber = alarm.phonenumber
                val message = alarm.message
                val triggerTime = alarm.timeInMillis
                val repeatSmS = alarm.repeatSmS
                val alarmId = alarm.alarmId
                if (phonenumber != null) {
                    if (message != null) {
                        if (repeatSmS != null) {
                            scheduleAlarm(phonenumber, message, context, triggerTime, repeatSmS, alarmId)
                        }
                    }
                }
            }
        }
    }

    private fun scheduleAlarm(
        phonenumber: String,
        message: String,
        context: Context,
        triggerTime: Long,
        repeatSmS: String,
        alarmId: Int
    ) {
        SimpleDateFormat("yyyy-MM-dd HH:mm")
        Date(triggerTime)

        day = context.getString(R.string.send_sms_every_day_text)
        week = context.getString(R.string.send_sms_every_week_text)
        month = context.getString(R.string.send_sms_every_month_text)
        year = context.getString(R.string.send_sms_every_year_text)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra("EXTRA_PHONE_NUMBER", phonenumber)
        intent.putExtra("EXTRA_MESSAGES", message)
        intent.putExtra("EXTRA_ALARMID", alarmId)
        intent.putExtra("EXTRA_TRIGGERTIME", triggerTime)
        intent.putExtra("EXTRA_REPEATSMS", repeatSmS)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(Intent(context, AlarmReceiver::class.java))
        } else {
            context.startService(Intent(context, AlarmReceiver::class.java))
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )

        MainActivity.saveAlarmDetails(
            context,
            alarmId,
            triggerTime,
            repeatSmS,
            phonenumber,
            message
        )
    }

    private fun getAllAlarms(context: Context): List<MainActivity.Companion.AlarmDetails> {
        val alarmList: MutableList<MainActivity.Companion.AlarmDetails> = ArrayList()
        val preferences = context.getSharedPreferences("AlarmDetails", Context.MODE_PRIVATE)

        val uniqueAlarmIds: MutableSet<Int> = HashSet()

        val allEntries = preferences.all
        for ((key, _) in allEntries) {
            val triggerTimeKey = "triggerTime_" + key.substring(key.lastIndexOf("_") + 1)
            val getRepeatSmSKey = "getRepeatSmSKey_" + key.substring(key.lastIndexOf("_") + 1)
            val getPhonenumberKey = "getPhoneNumberKey_" + key.substring(key.lastIndexOf("_") + 1)
            val getMessageKey = "getMessageKey_" + key.substring(key.lastIndexOf("_") + 1)

            val getPhonenumber = preferences.getString(getPhonenumberKey, null)
            val getMessage = preferences.getString(getMessageKey, null)
            val getRepeatSmS = preferences.getString(getRepeatSmSKey, null)
            val triggerTime = preferences.getLong(triggerTimeKey, 0)

            if (getPhonenumber != null && getMessage != null && getRepeatSmS != null) {
                val alarmId = key.substring(key.lastIndexOf("_") + 1).toInt()
                if (!uniqueAlarmIds.contains(alarmId)) {
                    val alarmDetails =
                        MainActivity.Companion.AlarmDetails(
                            alarmId,
                            triggerTime,
                            getRepeatSmS,
                            getPhonenumber,
                            getMessage
                        )
                    alarmList.add(alarmDetails)
                    uniqueAlarmIds.add(alarmId)
                }
            } else {
                Log.e("BootCompleteReceiver", "Incomplete alarm details for key: $key")
            }
        }

        return alarmList
    }
}
