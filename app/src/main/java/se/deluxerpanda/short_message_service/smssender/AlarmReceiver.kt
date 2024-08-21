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
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import se.deluxerpanda.short_message_service.profile.ProfileActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED) {
            sendNotificationSMSNoPermission(context)
        } else {
        phonenumber = intent.getStringExtra("EXTRA_PHONE_NUMBER")

        message = intent.getStringExtra("EXTRA_MESSAGES")

        triggerTime = intent.getLongExtra("EXTRA_TRIGGERTIME", 0)

        repeatSmS = intent.getStringExtra("EXTRA_REPEATSMS")

        alarmId = intent.getIntExtra("EXTRA_ALARMID", 0).toString().toInt()

        val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(SmsManager::class.java)
        } else {
            @Suppress("DEPRECATION")
            SmsManager.getDefault()
        }

        val isMultiNumber = phonenumber?.contains(",") ?: false

        if (isMultiNumber) {
            phonenumber!!.split(",\\s*".toRegex()).forEach { number ->
                val parts = smsManager.divideMessage(message)
                smsManager.sendMultipartTextMessage(number, null, parts, null, null)
            }
        } else {
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
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val date = Date(triggerTime)

        day = context.getString(se.deluxerpanda.short_message_service.R.string.send_sms_every_day_text)
        week = context.getString(se.deluxerpanda.short_message_service.R.string.send_sms_every_week_text)
        month = context.getString(se.deluxerpanda.short_message_service.R.string.send_sms_every_month_text)
        year = context.getString(se.deluxerpanda.short_message_service.R.string.send_sms_every_year_text)

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


        context.startForegroundService(intent)

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
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED) {
            sendNotificationSMSNoPermission(context)
        } else {
            phonenumber = intent.getStringExtra("EXTRA_PHONE_NUMBER")

            message = intent.getStringExtra("EXTRA_MESSAGES")

            triggerTime = intent.getLongExtra("EXTRA_TRIGGERTIME", 0)

            repeatSmS = intent.getStringExtra("EXTRA_REPEATSMS")

            alarmId = intent.getIntExtra("EXTRA_ALARMID", 0).toString().toInt()

            val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }

            val isMultiNumber = phonenumber!!.contains(",")
            if (isMultiNumber) {
                phonenumber!!.split(",\\s*".toRegex()).forEach { number ->
                    val parts = smsManager.divideMessage(message)
                    smsManager.sendMultipartTextMessage(number, null, parts, null, null)
                }
            } else {
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
    }

    private fun sendNotification(context: Context) {
        // Create notification channel for Android O and above
        val channel = NotificationChannel(
            MainActivity.CHANNEL_ID,
            MainActivity.CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        // Create a unique request code for the PendingIntent
        val requestCode = Random().nextInt()

        // Retrieve alarm details
        val alarmDetails = MainActivity.getAlarmById(context, alarmId)
        if (alarmDetails == null) {
            Log.e("AlarmReceiver", "Alarm not found for ID: $alarmId")
            return
        }

        // Create an intent for ProfileActivity
        val notificationIntent =  Intent(context, ProfileActivity::class.java).apply {
            putExtra("EXTRA_HISTORY_PROFILE_ALARMID", alarmDetails.alarmId)
            putExtra("EXTRA_HISTORY_PROFILE_TITLE", alarmDetails.phonenumber)
            putExtra("EXTRA_HISTORY_PROFILE_TIMEANDDATE", SimpleDateFormat("yyyy-MM-dd | H:mm", Locale.getDefault()).format(alarmDetails.timeInMillis))
            putExtra("EXTRA_HISTORY_PROFILE_REPEATS", alarmDetails.repeatSmS)
            putExtra("EXTRA_HISTORY_PROFILE_PHONENUMBER", alarmDetails.phonenumber)
            putExtra("EXTRA_HISTORY_PROFILE_MESSAGE", alarmDetails.message)}

        // Create a PendingIntent for the notification
        val pendingIntent =   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    PendingIntent.getActivity(context, requestCode, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
}else{
    PendingIntent.getActivity(context, requestCode, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
}
        // Build the notification
        val builder = NotificationCompat.Builder(context, MainActivity.CHANNEL_ID)
            .setSmallIcon(se.deluxerpanda.short_message_service.R.mipmap.ic_launcher)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, se.deluxerpanda.short_message_service.R.mipmap.ic_launcher))
            .setContentTitle(context.resources.getString(se.deluxerpanda.short_message_service.R.string.sms_notify_message_sent_number_text) + " " + alarmDetails.phonenumber)
            .setContentText(context.resources.getString(se.deluxerpanda.short_message_service.R.string.sms_notify_message_sent_message_text) + " " + alarmDetails.message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent) // Set the PendingIntent to be triggered when the notification is clicked
            .setAutoCancel(true) // Automatically remove the notification when it is clicked

        // Show the notification
        val notificationManager = NotificationManagerCompat.from(context)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        notificationManager.notify(requestCode, builder.build())
    }


    private fun sendNotificationSMSNoPermission(context: Context) {
        // Create notification channel for Android O and above
        val channel = NotificationChannel(
            MainActivity.CHANNEL_ID,
            MainActivity.CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        // Create a unique request code for the PendingIntent
        val requestCode = Random().nextInt()

        val notificationIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }

        // Create a PendingIntent for the notification
        val pendingIntent =   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PendingIntent.getActivity(context, requestCode, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }else{
            PendingIntent.getActivity(context, requestCode, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

            // Build the notification
            val builder = NotificationCompat.Builder(context, MainActivity.CHANNEL_ID)
                .setSmallIcon(se.deluxerpanda.short_message_service.R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(context.resources, se.deluxerpanda.short_message_service.R.mipmap.ic_launcher))
                .setContentTitle(context.resources.getString(se.deluxerpanda.short_message_service.R.string.sms_no_permission_sms_titel) )
                .setContentText(context.resources.getString(se.deluxerpanda.short_message_service.R.string.sms_no_permission_sms_text))
                .addAction(se.deluxerpanda.short_message_service.R.drawable.baseline_settings, context.resources.getString(se.deluxerpanda.short_message_service.R.string.text_ask_give_permission_settings), pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)

        // Show the notification
        val notificationManager = NotificationManagerCompat.from(context)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        notificationManager.notify(requestCode, builder.build())
    }

}