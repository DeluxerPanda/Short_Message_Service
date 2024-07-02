package se.deluxerpanda.short_message_service.smssender


import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import se.deluxerpanda.short_message_service.R
import se.deluxerpanda.short_message_service.scheduled.ScheduledList
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Random

class AlarmReceiver : BroadcastReceiver() {
    private var phonenumber: String? = null
    private var Phonenumber_Multi: String? = null
    private var message: String? = null
    private var triggerTime: Long = 0
    private var repeatSmS: String? = null
    private var alarmId = 0

    private var week: String? = null
    private var day: String? = null
    private var month: String? = null
    private var year: String? = null


    override fun onReceive(context: Context, intent: Intent) {
        phonenumber = intent.getStringExtra("EXTRA_PHONE_NUMBER")

        message = intent.getStringExtra("EXTRA_MESSAGES")

        triggerTime = intent.getLongExtra("EXTRA_TRIGGERTIME", 0)

        repeatSmS = intent.getStringExtra("EXTRA_REPEATSMS")

        alarmId = intent.getIntExtra("EXTRA_ALARMID", 0).toString().toInt()

        val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)  {
            context.getSystemService(SmsManager::class.java)
        } else {
            SmsManager.getDefault()
        }

        val isMultiNumber = phonenumber!!.contains(",")
        if (isMultiNumber) {
            phonenumber!!.split(",\\s*".toRegex()).forEach { number ->
                val parts = smsManager.divideMessage(message)
                smsManager.sendMultipartTextMessage(number, null, parts, null, null)
            }
        } else {
            Log.d("Hmmmm ", " phoneNumber: $phonenumber message: $message")
            smsManager.sendTextMessage(phonenumber, null, message, null, null)
        }

        rescheduleAlarm(
            phonenumber,
            Phonenumber_Multi,
            message,
            context,
            triggerTime,
            repeatSmS,
            alarmId
        )
        sendNotification(context)
    }

    private fun rescheduleAlarm(
        phonenumber: String?,
        Phonenumber_Multi: String?,
        message: String?,
        context: Context,
        triggerTime: Long,
        repeatSmS: String?,
        alarmId: Int
    ) {
        SimpleDateFormat("yyyy-MM-dd HH:mm")
        val date = Date(triggerTime)

        day = context.getString(R.string.send_sms_every_day_text)
        week = context.getString(R.string.send_sms_every_week_text)
        month = context.getString(R.string.send_sms_every_month_text)
        year = context.getString(R.string.send_sms_every_year_text)

        var intervalMillis: Long = 0

        if (repeatSmS.equals(day, ignoreCase = true)) {
            intervalMillis = AlarmManager.INTERVAL_DAY
        } else if (repeatSmS.equals(week, ignoreCase = true)) {
            intervalMillis = AlarmManager.INTERVAL_DAY * 7
        } else if (repeatSmS.equals(month, ignoreCase = true)) {
            intervalMillis = AlarmManager.INTERVAL_DAY * 30
        } else if (repeatSmS.equals(year, ignoreCase = true)) {
            intervalMillis = AlarmManager.INTERVAL_DAY * 365
        }

        val newtriggerTime = date.time + intervalMillis

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java)

        if (Phonenumber_Multi !== "false") {
            intent.putExtra("EXTRA_PHONE_NUMBER", phonenumber)
        } else {
            intent.putExtra("EXTRA_PHONE_NUMBER", phonenumber)
        }
        intent.putExtra("EXTRA_MESSAGES", message)
        intent.putExtra("EXTRA_ALARMID", alarmId)
        intent.putExtra("EXTRA_TRIGGERTIME", newtriggerTime)
        intent.putExtra("EXTRA_REPEATSMS", repeatSmS)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            newtriggerTime,
            pendingIntent
        )

        MainActivity.saveAlarmDetails(
            context,
            alarmId,
            newtriggerTime,
            repeatSmS,
            phonenumber,
            message
        )
    }

    fun sendNow(context: Context, intent: Intent) {

        phonenumber = intent.getStringExtra("EXTRA_PHONE_NUMBER")

        message = intent.getStringExtra("EXTRA_MESSAGES")

        triggerTime = intent.getLongExtra("EXTRA_TRIGGERTIME", 0)

        repeatSmS = intent.getStringExtra("EXTRA_REPEATSMS")

        alarmId = intent.getIntExtra("EXTRA_ALARMID", 0).toString().toInt()

        val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(SmsManager::class.java)
        } else {
            SmsManager.getDefault()
        }

        val isMultiNumber = phonenumber!!.contains(",")
        if (isMultiNumber) {
            phonenumber!!.split(",\\s*".toRegex()).forEach { number ->
                val parts = smsManager.divideMessage(message)
                smsManager.sendMultipartTextMessage(number, null, parts, null, null)
            }
        } else {
            Log.d("Hmmmm ", " phoneNumber: $phonenumber message: $message")
            smsManager.sendTextMessage(phonenumber, null, message, null, null)
        }

        rescheduleAlarm(
            phonenumber,
            Phonenumber_Multi,
            message,
            context,
            triggerTime,
            repeatSmS,
            alarmId
        )
        val intenta = Intent(context, MainActivity::class.java)
        intenta.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intenta)
        sendNotification(context)
    }
    
    private fun sendNotification(context: Context) {
        var channel: NotificationChannel? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = NotificationChannel(
                MainActivity.CHANNEL_ID,
                MainActivity.CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
        }
        val manager = context.getSystemService(
            NotificationManager::class.java
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(channel!!)
        }

        // Build the notification
        val builder = NotificationCompat.Builder(context, MainActivity.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    context.resources,
                    R.mipmap.ic_launcher_foreground
                )
            )
            .setContentTitle(context.resources.getString(R.string.sms_notify_message_sent_number_text) + " " + phonenumber)
            .setContentText(context.resources.getString(R.string.sms_notify_message_sent_message_text) + " " + message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        val random = Random()
        val notificationId = random.nextInt()

        // Create an intent for the notification
        val notificationIntent = Intent(
            context,
            ScheduledList::class.java
        )



        val pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_MUTABLE)

        builder.setContentIntent(pendingIntent)

        // Show the notification
        val notificationManager = NotificationManagerCompat.from(context)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        notificationManager.notify(notificationId, builder.build())
    }
}