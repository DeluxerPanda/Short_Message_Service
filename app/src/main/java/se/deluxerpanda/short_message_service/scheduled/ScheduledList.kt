package se.deluxerpanda.short_message_service.scheduled

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import se.deluxerpanda.short_message_service.R
import se.deluxerpanda.short_message_service.profile.ContactInfo
import se.deluxerpanda.short_message_service.profile.ProfileActivity
import se.deluxerpanda.short_message_service.smssender.MainActivity
import java.text.SimpleDateFormat

class ScheduledList : AppCompatActivity() {

    private val requestContactsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted, reload the list
            loadScheduledSMSList()
        } else {
            // Handle the case where the user denied the permission
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScheduledSMSListUI()
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestContactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        } else {
            loadScheduledSMSList()
        }
    }

    private fun loadScheduledSMSList() {
        lifecycleScope.launch {
            // Call your function to load the scheduled SMS list here
        }
    }

    @Composable
    fun ScheduledSMSListUI() {
        // This will contain the compose UI
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Call your composable function to display the list here
            val alarmList = MainActivity.getAllAlarms(this@ScheduledList)
            if (alarmList.isEmpty()) {
                Text(
                    text = stringResource(R.string.history_info_no_SMS_scheduled),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                Column {
                    alarmList.forEach { alarmDetails ->
                        HistoryInfoSection(alarmDetails)
                    }
                }
            }
        }
    }

    @Composable
    fun HistoryInfoSection(alarmDetails: MainActivity.Companion.AlarmDetails) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.baseline_person),
                    contentDescription = stringResource(id = R.string.todo),
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.Top)
                        .padding(end = 8.dp)
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    val title = ContactInfo.processPhoneNumbers(alarmDetails.phonenumber, contentResolver, this@ScheduledList)
                        Text(
                            text = title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(bottom = 8.dp),
                            style = TextStyle(textDecoration = TextDecoration.Underline)
                        )
                    alarmDetails.message?.let {
                        Text(
                            text = it,
                            fontSize = 13.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    Text(
                        text = SimpleDateFormat("yyyy-MM-dd | H:mm").format(alarmDetails.timeInMillis)+" | "+alarmDetails.repeatSmS,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Image(
                    painter = painterResource(id = R.drawable.ic_baseline_info_outline_24),
                    contentDescription = stringResource(id = R.string.todo),
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.CenterVertically)
                        .clickable {
                            val intent = Intent(this@ScheduledList, ProfileActivity::class.java).apply {
                                putExtra("EXTRA_HISTORY_PROFILE_ALARMID", alarmDetails.alarmId)
                                putExtra("EXTRA_HISTORY_PROFILE_POTOURL",
                                    alarmDetails.phonenumber?.let { getContactPhotoUri(it)?.toString() })
                                putExtra("EXTRA_HISTORY_PROFILE_TITLE", alarmDetails.phonenumber) // Replace with actual title
                                putExtra("EXTRA_HISTORY_PROFILE_TIMEANDDATE", SimpleDateFormat("yyyy-MM-dd | H:mm").format(alarmDetails.timeInMillis))
                                putExtra("EXTRA_HISTORY_PROFILE_REPEATS", alarmDetails.repeatSmS)
                                putExtra("EXTRA_HISTORY_PROFILE_PHONENUMBER", alarmDetails.phonenumber)
                                putExtra("EXTRA_HISTORY_PROFILE_MESSAGE", alarmDetails.message)
                            }
                            startActivity(intent)
                        }
                )
            }
        }
    }

    private fun getContactPhotoUri(phoneNumber: String): Uri? {
        val contentResolver = contentResolver
        val contactUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)
        val selection = "${ContactsContract.CommonDataKinds.Phone.NUMBER} = ?"
        val selectionArgs = arrayOf(phoneNumber)

        val cursor = contentResolver.query(contactUri, projection, selection, selectionArgs, null)
        var photoUri: Uri? = null

        cursor?.use {
            if (it.moveToFirst()) {
                val photoUriString = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.PHOTO_URI))
                if (photoUriString != null) {
                    photoUri = Uri.parse(photoUriString)
                }
            }
        }
        return photoUri
    }
}
