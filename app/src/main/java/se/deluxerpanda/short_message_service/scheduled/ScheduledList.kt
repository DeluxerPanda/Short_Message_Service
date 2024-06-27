package se.deluxerpanda.short_message_service.scheduled

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import se.deluxerpanda.short_message_service.R
import se.deluxerpanda.short_message_service.profile.ProfileActivity
import se.deluxerpanda.short_message_service.smssender.MainActivity
import java.text.SimpleDateFormat
import java.util.StringTokenizer

class ScheduledList : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.scheduled_list_layout)
        ScheduledSMSList()
        // back button
        val btnBack = findViewById<ImageView>(R.id.btnToMainSmsSchedulerPage)
        btnBack.setOnClickListener { finish() }
    }

    fun ScheduledSMSList() {
        val parentLayout = findViewById<LinearLayout>(R.id.app_backgrund)
        val linearLayout = parentLayout as LinearLayout
        linearLayout.removeAllViews()

        val alarmList = MainActivity.getAllAlarms(
            this
        )

        if (alarmList.isEmpty()) {
            val AlarmListIsEmptyTextView = TextView(this)


            // Add your dynamic TextView here
            AlarmListIsEmptyTextView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            AlarmListIsEmptyTextView.text =
                resources.getString(R.string.history_info_no_SMS_scheduled)
            AlarmListIsEmptyTextView.textSize = 20f
            AlarmListIsEmptyTextView.typeface =
                Typeface.create("sans-serif-black", Typeface.BOLD_ITALIC)
            AlarmListIsEmptyTextView.gravity = Gravity.CENTER
            linearLayout.addView(AlarmListIsEmptyTextView)
        } else {
            // Now you can use the alarmList as needed
            for (alarmDetails in alarmList) {
                val alarmId = alarmDetails.alarmId

                // long timeInMillis = alarmDetails.getTimeInMillis();
                val sdf = SimpleDateFormat("yyyy-MM-dd")
                val sdf2 = SimpleDateFormat("H:mm")


                // Format the date and print the result
                val formattedDateStart = sdf.format(alarmDetails.timeInMillis)
                val formattedClockTime = sdf2.format(alarmDetails.timeInMillis)
                val getRepeatSmS = alarmDetails.repeatSmS
                val phonenumber = alarmDetails.phonenumber
                val message = alarmDetails.message
                val dynamicTextViewLayout = layoutInflater.inflate(R.layout.history_info, null)

                val dynamicLinearLayout =
                    dynamicTextViewLayout.findViewById<LinearLayout>(R.id.history_info_page)

                val history_info_contact_name_TextView =
                    dynamicTextViewLayout.findViewById<TextView>(R.id.history_info_contact_name)

                var title: String
                val inputString = phonenumber
                var contactName: String? = null
                var photoUri_result: String?
                val concatenatedNames = StringBuilder()
                val permissionCheckContacts =
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                val contactImageView =
                    dynamicTextViewLayout.findViewById<ImageView>(R.id.history_info_contact_profile)
                if (permissionCheckContacts == PackageManager.PERMISSION_GRANTED) {
                    val contentResolver = contentResolver

                    if (phonenumber.contains(",")) {
// Creating a StringTokenizer object with delimiter ","
                        val tokenizer = StringTokenizer(inputString, ",")

                        val tokenCount = tokenizer.countTokens()
                        val stringArray = arrayOfNulls<String>(tokenCount)

                        // Converting each token to array elements
                        for (i in 0 until tokenCount) {
                            stringArray[i] = tokenizer.nextToken()
                        }

                        // Printing the output array
                        for (element in stringArray) {
                            contactName = MainActivity.getContactName(contentResolver, element)
                            concatenatedNames.append(contactName).append(", ")
                        }
                        history_info_contact_name_TextView.text = concatenatedNames
                        title = concatenatedNames.toString()
                    } else {
                        contactName = MainActivity.getContactName(contentResolver, phonenumber)
                        if (contactName != null) {
                            history_info_contact_name_TextView.text = contactName
                            title = contactName.toString()
                        } else {
                            history_info_contact_name_TextView.text = phonenumber
                            title = phonenumber.toString()
                        }
                    }
                } else {
                    history_info_contact_name_TextView.text = phonenumber
                    title = phonenumber.toString()
                }

                val photoUri = getContactPhotoUri(contactName)
                if (photoUri != null) {
                    // Load the contact photo into the ImageView
                    contactImageView.setImageURI(photoUri)
                    // Create a rounded drawable and set it directly to the ImageView
                    val roundedDrawable = RoundedBitmapDrawableFactory.create(
                        resources, (contactImageView.drawable as BitmapDrawable).bitmap
                    )
                    roundedDrawable.isCircular = true // Set to true if you want circular corners
                    contactImageView.setImageDrawable(roundedDrawable)
                    photoUri_result = photoUri.toString()
                } else {
                    contactImageView.setImageResource(R.drawable.ic_baseline_person_24)
                    photoUri_result = null
                }

                val words = phonenumber.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()

                val output = StringBuilder()
                for (word in words) {
                    output.append(word.trim { it <= ' ' }).append("\n")
                }

                val phonenumber_result = output.toString()


                val history_info_message_TextView =
                    dynamicTextViewLayout.findViewById<TextView>(R.id.history_info_message)
                history_info_message_TextView.text = message

                val history_info_date_and_time_TextView =
                    dynamicTextViewLayout.findViewById<TextView>(R.id.history_info_date_and_time)

                val TimeAndDate = "$formattedDateStart | $formattedClockTime"

                history_info_date_and_time_TextView.text =
                    (resources.getString(R.string.history_info_Date_name) + " " + formattedDateStart
                            + ", " + resources.getString(R.string.history_info_Time_name) + " " + formattedClockTime)

                linearLayout.addView(dynamicTextViewLayout)

                var finalPhotoUri_result = photoUri_result

                dynamicLinearLayout.setOnClickListener {
                    val intent = Intent(
                        this@ScheduledList,
                        ProfileActivity::class.java
                    )
                    intent.putExtra("EXTRA_HISTORY_PROFILE_ALARMID", alarmId)
                    intent.putExtra("EXTRA_HISTORY_PROFILE_POTOURL", finalPhotoUri_result)
                    intent.putExtra("EXTRA_HISTORY_PROFILE_TITLE", title)
                    intent.putExtra("EXTRA_HISTORY_PROFILE_TIMEANDDATE", TimeAndDate)
                    intent.putExtra("EXTRA_HISTORY_PROFILE_REPEATS", getRepeatSmS)
                    intent.putExtra("EXTRA_HISTORY_PROFILE_PHONENUMBER", phonenumber_result)
                    intent.putExtra("EXTRA_HISTORY_PROFILE_MESSAGE", message)
                    startActivity(intent)
                }
            }
        }
    }

    fun getContactPhotoUri(contactID: String?): Uri? {
        if (contactID == null) {
            return null
        }
        val contactUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)
        val selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + "=?"
        val selectionArgs = arrayOf(contactID)

        val cursor = contentResolver.query(contactUri, projection, selection, selectionArgs, null)
        var photoUri: Uri? = null

        if (cursor != null && cursor.moveToFirst()) {
            val photoUriString =
                cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.PHOTO_URI))
            if (photoUriString != null) {
                photoUri = Uri.parse(photoUriString)
            }
            cursor.close()
        }
        return photoUri
    }
}