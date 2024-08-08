package se.deluxerpanda.short_message_service.profile

import android.Manifest
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.net.ParseException
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily.Companion.SansSerif
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import se.deluxerpanda.short_message_service.ui.theme.AppTheme
import se.deluxerpanda.short_message_service.R
import se.deluxerpanda.short_message_service.smssender.AlarmReceiver
import se.deluxerpanda.short_message_service.smssender.MainActivity
import se.deluxerpanda.short_message_service.smssender.MainActivity.Companion.isSmsTooLong
import se.deluxerpanda.short_message_service.smssender.MainActivity.Companion.saveAlarmDetails
import se.deluxerpanda.short_message_service.smssender.PhoneListActivity
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.UUID


class ProfileActivity  : ComponentActivity() {



    private var title: String? = null
    private var alarmId = 0

    private val MaxNumbers = 10

    private var photoUri: Uri? = null

    private var photoUriString: String? = null

    private var timeAndDate: String? = null
    private var editedtimeAndDate: String? = null
    private var  isTimeAndDateChanged: Boolean = false

    private  var repeats: String? = null

    private var phoneNumber: String? = null
    private var phoneNumberNew: String? = null
    private var editedphoneNumber: String? = null
    private var editedphoneNumberNew: String? = null

    private var phoneNumberID: String? = null
    private var phoneNumberData: String? = null

    private var MessageFieldText: String? = null

    private var  isMessageChanged: Boolean = false
    private var editedMessage: String? = null

    private var contactName: String? = null
    private var contactNameAndLast: String? = null

    private var UpdateSceduluedSmS: Boolean = false

    private var ToastContent: String? = null

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var intent = intent
        if (intent != null) {
            title = intent.getStringExtra("EXTRA_HISTORY_PROFILE_TITLE")
            alarmId = intent.getIntExtra("EXTRA_HISTORY_PROFILE_ALARMID", 0)

            photoUriString = intent.getStringExtra("EXTRA_HISTORY_PROFILE_POTOURL")
            if (photoUriString != null){
                photoUri = Uri.parse(photoUriString)
            }

            timeAndDate = intent.getStringExtra("EXTRA_HISTORY_PROFILE_TIMEANDDATE")

            repeats = intent.getStringExtra("EXTRA_HISTORY_PROFILE_REPEATS")

            phoneNumber = intent.getStringExtra("EXTRA_HISTORY_PROFILE_PHONENUMBER")
            phoneNumberNew = if (phoneNumber!!.contains(",")) {
                phoneNumber!!.replace(",", "\n")
            } else {
                phoneNumber
            }
            if (!phoneNumber!!.contains(",")) {
                contactNameAndLast = ContactInfo.getContactName(contentResolver,phoneNumber,this)
                photoUri = ContactInfo.getContactPhotoUri2(this@ProfileActivity,contactNameAndLast)
            }


            MessageFieldText = intent.getStringExtra("EXTRA_HISTORY_PROFILE_MESSAGE")
        }
        setContent {
            AppTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "screen1") {
                    composable("screen1") { entry ->

                        val TimeAndDateFieldTextupdate =
                            entry.savedStateHandle.get<String>("EXTRA_PROFILE_EDITOR_FINAL_TIMEANDDATE")
                        if (TimeAndDateFieldTextupdate != null) {
                            timeAndDate = TimeAndDateFieldTextupdate
                            UpdateSceduluedSmS = true
                        }

                        val RepeatFieldTextupdate =
                            entry.savedStateHandle.get<String>("EXTRA_PROFILE_EDITOR_FINAL_REPEAT")
                        if (RepeatFieldTextupdate != null) {
                            repeats = RepeatFieldTextupdate
                            UpdateSceduluedSmS = true
                        }

                        val MessageFieldTextupdate =
                            entry.savedStateHandle.get<String>("EXTRA_PROFILE_EDITOR_FINAL_MESSAGE")
                        if (MessageFieldTextupdate != null) {
                            MessageFieldText = MessageFieldTextupdate
                            UpdateSceduluedSmS = true
                        }

                        val PhoneNumberFieldTextupdate =
                            entry.savedStateHandle.get<String>("EXTRA_PROFILE_EDITOR_FINAL_PHONENUMBER")
                        if (PhoneNumberFieldTextupdate != null) {
                            phoneNumber = PhoneNumberFieldTextupdate
                            UpdateSceduluedSmS = true
                            phoneNumberNew = if (phoneNumber!!.contains(",")) {
                                phoneNumber!!.replace(",", "\n")
                            } else {
                                phoneNumber
                            }
                            if (!phoneNumber!!.contains(",")) {
                                photoUri = ContactInfo.getContactPhotoUri2(this@ProfileActivity,contactNameAndLast)
                            }

                        }

                        val scrollBehavior =
                            TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
                        Scaffold(
                            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),

                            topBar = {
                                CenterAlignedTopAppBar(
                                    title = {
                                    },
                                    navigationIcon = {
                                        var showReSceduledOrNotDialog by remember { mutableStateOf(false) }
                                        ReSceduledOrNotDialog(
                                            showReSceduledOrNotDialog = showReSceduledOrNotDialog,
                                            onSave = {
                                                val sdf = SimpleDateFormat("yyyy-MM-dd | HH:mm") // Corrected the format to match the input string

                                                try {
                                                    val date: Date? = timeAndDate.let { sdf.parse(it.toString()) }
                                                    val triggerTime: Long? = date?.time


                                                    var intent2: Intent = Intent(
                                                        this@ProfileActivity,
                                                        AlarmReceiver::class.java
                                                    )

                                                    val newalarmId = UUID.randomUUID().hashCode()

                                                    intent2.putExtra("EXTRA_PHONE_NUMBER", phoneNumber)
                                                    intent2.putExtra("EXTRA_MESSAGES", MessageFieldText)
                                                    intent2.putExtra("EXTRA_ALARMID", newalarmId)
                                                    intent2.putExtra("EXTRA_TRIGGERTIME", triggerTime)
                                                    intent2.putExtra("EXTRA_REPEATSMS", repeats)


                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                        startForegroundService(intent2)
                                                    } else {
                                                        startService(intent2)
                                                    }


                                                    val pendingIntent = PendingIntent.getBroadcast(
                                                        this@ProfileActivity,
                                                        newalarmId,
                                                        intent2,
                                                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                                                    )

                                                    val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager


                                                    alarmManager.setExactAndAllowWhileIdle(
                                                        AlarmManager.RTC_WAKEUP,
                                                        triggerTime!!,
                                                        pendingIntent
                                                    )

                                                    saveAlarmDetails(
                                                        this@ProfileActivity,
                                                        newalarmId,
                                                        triggerTime,
                                                        repeats,
                                                        phoneNumber,
                                                        MessageFieldText
                                                    )

                                                        val mainActivity = MainActivity()
                                                        mainActivity.deleteAlarm(
                                                            alarmId,
                                                            this@ProfileActivity
                                                        )

                                                } catch (e: ParseException) {
                                                    e.printStackTrace()
                                                }
                                                showReSceduledOrNotDialog = false
                                                finish()
                                            },
                                            onDismiss = {
                                                showReSceduledOrNotDialog = false
                                                finish()
                                            }
                                        )
                                        IconButton(onClick = {
                                            if (UpdateSceduluedSmS == true){
                                                showReSceduledOrNotDialog = true
                                            }else{
                                                finish()
                                            }

                                        }
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.baseline_close),
                                                contentDescription = "Save button"
                                            )
                                        }
                                    },

                                    actions = {
                                        var showSendNowDialog by remember { mutableStateOf(false) }
                                        SendNowDialog(
                                            showSendNowDialog = showSendNowDialog,
                                            onSave = {
                                                val alarmreceiver = AlarmReceiver()
                                                var intentAlarmReceiver: Intent = Intent()
                                                val sdf = SimpleDateFormat("yyyy-MM-dd | HH:mm")
                                                val date: Date? = timeAndDate.let { sdf.parse(it.toString()) }
                                                val triggerTime: Long? = date?.time
                                                intentAlarmReceiver.putExtra("EXTRA_PHONE_NUMBER", phoneNumber)
                                                intentAlarmReceiver.putExtra("EXTRA_MESSAGES", MessageFieldText)
                                                intentAlarmReceiver.putExtra("EXTRA_ALARMID", alarmId)
                                                intentAlarmReceiver.putExtra("EXTRA_TRIGGERTIME", triggerTime)
                                                intentAlarmReceiver.putExtra("EXTRA_REPEATSMS", repeats)
                                                alarmreceiver.sendNow(
                                                    this@ProfileActivity,
                                                    intentAlarmReceiver
                                                )
                                                showSendNowDialog = false
                                            },
                                            onDismiss = {
                                                showSendNowDialog = false
                                            }
                                        )
                                        IconButton(onClick = {
                                            showSendNowDialog = true
                                        })
                                        {
                                            Icon(
                                                painter = painterResource(id = R.drawable.baseline_send),
                                                contentDescription = "Send now button"
                                            )
                                        }
                                        var showDeleteDialog by remember { mutableStateOf(false) }
                                        DeleteDialog(
                                            showDeleteDialog = showDeleteDialog,
                                            onSave = {
                                                showDeleteDialog = false
                                                val mainActivity = MainActivity()
                                                mainActivity.deleteAlarm(
                                                    alarmId,
                                                    this@ProfileActivity
                                                )
                                                finish()
                                            },
                                            onDismiss = {
                                                showDeleteDialog = false
                                            }
                                        )
                                        IconButton(onClick = {
                                            showDeleteDialog = true
                                        })
                                        {
                                            Image(
                                                painter = painterResource(id = R.drawable.ic_baseline_delete_outline),
                                                contentDescription = "Delete button"
                                            )
                                        }
                                    },

                                    scrollBehavior = scrollBehavior,
                                )
                            },
                        ) { innerPadding ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(innerPadding)
                                    .padding(horizontal = 15.dp)
                                    .verticalScroll(rememberScrollState()),
                                horizontalAlignment = Alignment.CenterHorizontally
                            )
                            {

                                var title = ContactInfo.processPhoneNumbers(phoneNumber, contentResolver, this@ProfileActivity)
                                Text(
                                    text = title,
                                    textAlign = TextAlign.Center,
                                    fontSize = 24.sp,
                                    fontFamily = SansSerif,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    text = "ID: $alarmId",
                                    textAlign = TextAlign.Center,
                                    fontSize = 14.sp,
                                    fontFamily = SansSerif,
                                    fontWeight = FontWeight.Bold
                                )

                                Card(
                                    modifier = Modifier
                                        .wrapContentSize()
                                        .padding(top = 10.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(15.dp),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {  if (phoneNumber!!.contains(",")) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.baseline_groups),
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(130.dp)
                                                    .align(Alignment.CenterHorizontally)
                                            )
                                        }else if (photoUri != null) {
                                            Image(
                                                painter = rememberAsyncImagePainter(photoUri),

                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(130.dp)
                                                    .align(Alignment.CenterHorizontally)
                                            )
                                        }else{
                                            Icon(
                                                painter = painterResource(id = R.drawable.baseline_person),
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(130.dp)
                                                    .align(Alignment.CenterHorizontally)
                                            )
                                        }


                                    }
                                }

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentSize()
                                        .padding(top = 10.dp),
                                ) {

                                    Column(
                                        modifier = Modifier.padding(15.dp),
                                        verticalArrangement = Arrangement.Top
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth(),
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = timeAndDate!! +"\n"+getString(R.string.history_info_Repeat_name)+" "+repeats,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                modifier = Modifier.padding(
                                                    top = 10.dp,
                                                    bottom = 10.dp
                                                )
                                            )
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_baseline_edit_24),
                                                contentDescription = "To Edit",
                                                modifier = Modifier
                                                    .align(Alignment.End)
                                                    .clickable {
                                                        navController.navigate("TimeAndDateField")
                                                    }
                                                    .padding(bottom = 10.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.padding(10.dp))
                                Text(
                                    text = getString(R.string.history_info_Profile_Send_to),
                                    textAlign = TextAlign.Center,
                                    fontSize = 24.sp,
                                    fontFamily = SansSerif,
                                    fontWeight = FontWeight.Bold
                                )
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentSize()
                                        .padding(top = 10.dp),
                                ) {
                                    Column(
                                        modifier = Modifier.padding(15.dp),
                                        verticalArrangement = Arrangement.Top
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                        ) {
                                            // Display the phone number
                                            Text(
                                                text = phoneNumberNew!!,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                modifier = Modifier.padding(
                                                    top = 5.dp,
                                                    bottom = 10.dp
                                                )
                                            )
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_baseline_edit_24),
                                                contentDescription = "To Edit Phone Number",
                                                modifier = Modifier
                                                    .align(Alignment.End)
                                                    .clickable {
                                                        navController.navigate("PhoneNumberField")
                                                    }
                                                    .padding(bottom = 10.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.padding(10.dp))

                                Text(
                                    text = getString(R.string.history_info_Message_name),
                                    textAlign = TextAlign.Center,
                                    fontSize = 24.sp,
                                    fontFamily = SansSerif,
                                    fontWeight = FontWeight.Bold
                                )
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentSize()
                                        .padding(top = 10.dp),
                                ) {
                                    Column(
                                        modifier = Modifier.padding(15.dp),
                                        verticalArrangement = Arrangement.Top
                                    ) {

                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                        ) {
                                            Text(
                                                text = MessageFieldText!!,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = SansSerif,

                                                modifier = Modifier
                                                    .padding(top = 10.dp, bottom = 10.dp)
                                            )

                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_baseline_edit_24),
                                                contentDescription = "To Edit",
                                                modifier = Modifier
                                                    .align(Alignment.End)
                                                    .clickable {
                                                        navController.navigate("MessageField")
                                                    }
                                                    .padding(bottom = 10.dp)
                                            )
                                        }
                                    }
                                }

                            }
                        }
                    }
                    composable("TimeAndDateField") {
                        var showNoBackInTimeDialog by remember { mutableStateOf(false) }
                        var repeatsEdited by remember { mutableStateOf(repeats)}
                        var timeAndDateEdited by remember { mutableStateOf(timeAndDate)}
                        val mContext = LocalContext.current
                        val scrollBehavior =
                            TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
                        Scaffold(
                            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),

                            topBar = {
                                CenterAlignedTopAppBar(
                                    title = {
                                        Text(
                                            stringResource(id = R.string.history_info_Profile_Edit_TimeAndDate_name),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    navigationIcon = {
                                        IconButton(onClick = {
                                            val sdfDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd H:mm")
                                            val timeStr: String = timeAndDateEdited!!.substringAfter("| ")
                                            val dateStr: String = timeAndDateEdited!!.substringBefore(" |")
                                            try {
                                                val selectedDateTime = LocalDateTime.parse("$dateStr $timeStr", sdfDateTime)
                                                val currentDateTime = LocalDateTime.now()

                                                if (selectedDateTime.isAfter(currentDateTime)) {
                                                    navController.previousBackStackEntry
                                                        ?.savedStateHandle
                                                        ?.set(
                                                            "EXTRA_PROFILE_EDITOR_FINAL_TIMEANDDATE",
                                                            timeAndDateEdited
                                                        )
                                                    navController.previousBackStackEntry
                                                        ?.savedStateHandle
                                                        ?.set(
                                                            "EXTRA_PROFILE_EDITOR_FINAL_REPEAT",
                                                            repeatsEdited
                                                        )
                                                    navController.popBackStack()
                                                } else {
                                                    showNoBackInTimeDialog = true
                                                }
                                            } catch (e: ParseException) {
                                                e.printStackTrace()

                                                Toast.makeText(
                                                    mContext,
                                                    e.printStackTrace().toString(),
                                                    Toast.LENGTH_SHORT
                                                ).show()

                                            }
                                        }
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.baseline_save),
                                                contentDescription = "Save button"
                                            )
                                        }
                                    },

                                    actions = {
                                        IconButton(onClick = {
                                            navController.popBackStack()
                                        })
                                        {
                                            Icon(
                                                painter = painterResource(id = R.drawable.baseline_close),
                                                contentDescription = "Close button"
                                            )
                                        }
                                        NoBackInTimeDialog(
                                            showNoBackInTimeDialog = showNoBackInTimeDialog,
                                            onSave = {
                                                showNoBackInTimeDialog = false
                                            }
                                        )
                                    },

                                    scrollBehavior = scrollBehavior,
                                )
                            },
                        ) { innerPadding ->
                            // Fetching local context
                   
                            var Time by remember { mutableStateOf(timeAndDate!!.substringAfterLast("|")) }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(innerPadding)
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {

                                // Parsing hour and minute from the Time string
                                val mHour = try { Time.substringBeforeLast(":").trim().toInt() } catch (e: NumberFormatException) { 0 }
                                val mMinute = try { Time.substringAfterLast(":").trim().toInt() } catch (e: NumberFormatException) { 0 }

                                // Creating a TimePicker dialog
                                val mTimePickerDialog = TimePickerDialog(
                                    mContext,
                                    { _, hour: Int, minute: Int ->
                                        val formattedHour = String.format("%02d", hour)
                                        val formattedMinute = String.format("%02d", minute)
                                        val newTime = "$formattedHour:$formattedMinute"
                                        timeAndDateEdited = timeAndDateEdited!!.replaceAfterLast("| ", newTime)
                                        Time = newTime
                                        isTimeAndDateChanged = true

                                    }, mHour, mMinute, true
                                )

                                Text(text = getString(R.string.history_info_Time_name))
                                Button(onClick = {
                                    mTimePickerDialog.show()
                                }) {
                                    Text(text = Time)

                                }


                                var Date by remember { mutableStateOf(timeAndDateEdited!!.substringBefore("|")) }
                                // Parsing year, month, and day from the Date string
                                val mYear = try { Date.substringBefore("-").trim().toInt() } catch (e: NumberFormatException) { 0 }
                                val mMonth = try { Date.substringAfter("-").substringBefore("-").trim().toInt() } catch (e: NumberFormatException) { 0 } -1
                                val mDay = try { Date.substringAfterLast("-").substringBefore(" | ").trim().toInt() } catch (e: NumberFormatException) { 0 }
                                // Creating a DatePickerDialog
                                val mDatePickerDialog = DatePickerDialog(
                                    mContext,
                                    { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                                        val formattedMonth = String.format("%02d", month + 1)
                                        val formattedDay = String.format("%02d", dayOfMonth)
                                        val newDate = "$year-$formattedMonth-$formattedDay"
                                        timeAndDateEdited = timeAndDateEdited!!.replaceBeforeLast(" |", newDate)
                                        Date = newDate
                                    }, mYear, mMonth, mDay
                                )


                                Text(text = getString(R.string.history_info_Date_name))
                                Button(onClick = {
                                    mDatePickerDialog.show()
                                }) {
                                    Text(text = Date)
                                }
                                var showDialog by remember { mutableStateOf(false) }
                                Text(text = getString(R.string.send_sms_every_text))
                                Button(onClick = {
                                    showDialog = true
                                }) {
                                    Text(text = repeatsEdited.toString())

                                }
                                ShowOptionsDialog(
                                    showDialog = showDialog,
                                    onDismiss = { showDialog = false },
                                    onConfirm = { selectedOption ->
                                        repeatsEdited = selectedOption
                                        isTimeAndDateChanged = true
                                    }
                                )
                            }

                        }

                    }
                    composable("PhoneNumberField") {
                        val scrollBehavior =
                            TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
                        Scaffold(
                            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),

                            topBar = {
                                CenterAlignedTopAppBar(
                                    title = {
                                        Text(
                                            stringResource(id = R.string.history_info_Profile_Edit_Phone_number_name),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    navigationIcon = {
                                        IconButton(onClick = {
                                            it_isPhoneNumberField()
                                            navController.previousBackStackEntry
                                                ?.savedStateHandle
                                                ?.set(
                                                    "EXTRA_PROFILE_EDITOR_FINAL_PHONENUMBER",
                                                    editedphoneNumber
                                                )
                                            navController.popBackStack()
                                        }
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.baseline_save),
                                                contentDescription = "Save button"
                                            )
                                        }
                                    },

                                    actions = {
                                        IconButton(onClick = {
                                            navController.popBackStack()
                                        })
                                        {
                                            Icon(
                                                painter = painterResource(id = R.drawable.baseline_close),
                                                contentDescription = "Close button"
                                            )
                                        }
                                    },

                                    scrollBehavior = scrollBehavior,
                                )
                            },
                        ) { innerPadding ->
                            val keyboardController = LocalSoftwareKeyboardController.current
                            val mContext = LocalContext.current
                            var list by remember {
                                mutableStateOf(
                                    phoneNumber?.split(",") ?: listOf()
                                )
                            }
                            var isPhoneNumberChanged by remember { mutableStateOf(false) }



                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(innerPadding)
                                    .verticalScroll(rememberScrollState())
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                list.forEachIndexed { index, phone ->
                                    val launchPhoneList = rememberLauncherForActivityResult(
                                        contract = ActivityResultContracts.StartActivityForResult()
                                    ) { result ->
                                        if (result.resultCode == RESULT_OK) {
                                            val phoneNumberData = result.data?.getStringExtra("PHONE_NUMBER_FROM_CONTACTS")
                                            // Handle the phone number data received
                                            list = list.toMutableList().also { list ->
                                                if (phoneNumberData != null) {
                                                    list[index] = phoneNumberData.toString()
                                                    editedphoneNumber =
                                                        list.joinToString(",")
                                                    isPhoneNumberChanged = true
                                                }
                                            }
                                        }
                                    }
                                    TextField(
                                        value = phone,
                                        onValueChange = {
                                            list =
                                                list.toMutableList().also { list ->
                                                    list[index] = it
                                                }
                                            editedphoneNumber =
                                                list.joinToString(",")
                                            isPhoneNumberChanged = true
                                        },
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Number,
                                            imeAction = ImeAction.Done,
                                            autoCorrect = false
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onDone = {
                                                keyboardController?.hide()
                                            }
                                        ),
                                        label = {
                                            Text(
                                                text = "${getString(R.string.history_info_PhoneNumber_name)} ${index + 1}",
                                                textAlign = TextAlign.Center
                                            )
                                        },
                                        trailingIcon = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                IconButton(onClick = {
                                                 intent = Intent(this@ProfileActivity, PhoneListActivity::class.java)
                                                    launchPhoneList.launch(intent)
                                                }) {
                                                    Icon(
                                                        painter = painterResource(id = R.drawable.ic_baseline_import_contacts),
                                                        contentDescription = "import contacts button"
                                                    )
                                                }
                                                IconButton(onClick = {
                                                    list =
                                                        list.toMutableList()
                                                            .also { list ->
                                                                if (list.size != 1) {
                                                                    list.removeAt(index)
                                                                } else {
                                                                    Toast.makeText(
                                                                        mContext,
                                                                        R.string.history_info_Profile_Edit_Must_have_one_number,
                                                                        Toast.LENGTH_SHORT
                                                                    ).show()
                                                                }
                                                                editedphoneNumber = list.joinToString(",")
                                                                isPhoneNumberChanged = true
                                                            }
                                                }) {
                                                    Icon(
                                                        painter = painterResource(id = R.drawable.ic_baseline_delete_outline),
                                                        contentDescription = "delete button",
                                                        tint = MaterialTheme.colorScheme.error
                                                    )
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                    )
                                }
                                var ToastBool by remember { mutableStateOf(false) }
                                IconButton(onClick = {
                                    list =
                                        list.toMutableList().also { list ->
                                            if (list.size < MaxNumbers){
                                            list.add("")
                                            isPhoneNumberChanged = true
                                            }else{
                                                   ToastContent = getString(R.string.history_info_Profile_Edit_cannot_have_more_number)+
                                                           " "+MaxNumbers.toString()+" "+getString(R.string.More_Then_One_Number_name)

                                                ToastBool = true
                                            }
                                        }
                                }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.baseline_person_add),
                                        contentDescription = "add button"
                                    )
                                }
                                ToastDialog(
                                    ToastBool = ToastBool,
                                    onSave = {
                                        ToastBool = false
                                    }
                                )
                                // Update phoneNumber if it's not changed
                                if (!isPhoneNumberChanged) {
                                    editedphoneNumber = phoneNumber
                                }
                            }
                        }
                    }
                    composable("MessageField"){
                        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
                        Scaffold(
                            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),

                            topBar = {
                                CenterAlignedTopAppBar(
                                    title = {
                                            Text(
                                                stringResource(id = R.string.history_info_Profile_Edit_Message_name),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                    },
                                    navigationIcon = {
                                        IconButton(onClick = {
                                            navController.previousBackStackEntry
                                                ?.savedStateHandle
                                                ?.set("EXTRA_PROFILE_EDITOR_FINAL_MESSAGE", editedMessage)
                                                navController.popBackStack()
                                        }
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.baseline_save),
                                                contentDescription = "Save button"
                                            )
                                        }
                                    },

                                    actions = {
                                        IconButton(onClick = {
                                                navController.popBackStack()
                                        })
                                        {
                                            Icon(
                                                painter = painterResource(id = R.drawable.baseline_close),
                                                contentDescription = "Close button"
                                            )
                                        }
                                    },

                                    scrollBehavior = scrollBehavior,
                                )
                            },
                        ) { innerPadding ->

                            val keyboardController = LocalSoftwareKeyboardController.current

                            var text by remember { mutableStateOf(MessageFieldText)}
                            var ToastBool by remember { mutableStateOf(false) }
                            var showMessageCharactersRetchDialog by remember { mutableStateOf(false) }
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(innerPadding)
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {

                                text?.let {
                                    TextField(
                                        value = it,
                                        onValueChange = {
                                            if (!isSmsTooLong(it)) {
                                                text = it
                                            }else{
                                                showMessageCharactersRetchDialog = true
                                            }
                                        },
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Text,
                                            imeAction = ImeAction.Done
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onDone = {
                                                keyboardController?.hide()
                                            }
                                        ),
                                        label = {
                                            Text(
                                                stringResource(id = R.string.history_info_Message_name),
                                                textAlign = TextAlign.Center,
                                            )
                                        }
                                    )
                                }
                            }
                            MessageCharactersRetchDialog(
                                showMessageCharactersRetchDialog = showMessageCharactersRetchDialog,
                                onSave = {
                                    showMessageCharactersRetchDialog = false
                                }
                            )
                            ToastDialog(
                                ToastBool = ToastBool,
                                onSave = {
                                    ToastBool = false
                                }
                            )
                            if (!isMessageChanged){
                                editedMessage = MessageFieldText
                            }
                        }
                    }
                }
                    }
                }
            }

    fun it_isPhoneNumberField(){
        if (editedphoneNumber!!.contains(",")) {
            editedphoneNumberNew = editedphoneNumber!!.replace(",", "\n")

            val phoneNumbers: Array<String> =
                editedphoneNumber!!.split(",".toRegex())
                    .dropLastWhile { it.isEmpty() }
                    .toTypedArray()

            val titleBuilder = StringBuilder()

            for (number in phoneNumbers) {
                contactName = ContactInfo.getContactFirstName(contentResolver, number.trim { it <= ' ' },this)
                contactNameAndLast = ContactInfo.getContactName(contentResolver, number.trim { it <= ' ' },this)
                if (contactName != null) {
                    titleBuilder.append(contactName)
                        .append(", ")
                } else {
                    titleBuilder.append(number).append(", ")
                }
            }

            title = titleBuilder.toString()
                .replace(", $".toRegex(), "")

            title = title.toString()

        } else {
            editedphoneNumberNew = editedphoneNumber
                contactName = ContactInfo.getContactFirstName(contentResolver, editedphoneNumber,this)
                contactNameAndLast = ContactInfo.getContactName(contentResolver, editedphoneNumber,this)

            if (contactName != null) {
                title = contactName.toString()
            } else {
                title = title.toString()
            }
        }
    }


    @Composable
    fun MessageCharactersRetchDialog(
        showMessageCharactersRetchDialog: Boolean,
        onSave: () -> Unit,
    ) {
        if (showMessageCharactersRetchDialog) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text(
                    text = getString(R.string.sms_max_characters_titel),
                    fontWeight = FontWeight.Bold,
                    style = TextStyle(
                        fontSize = 20.sp,
                    ))},

                text = { Text(
                    text = getString(R.string.sms_max_characters_Text),
                    fontWeight = FontWeight.Bold,
                )},

                confirmButton = {
                    TextButton(
                        modifier = Modifier
                            .shadow(4.dp, shape = RoundedCornerShape(14.dp))
                            .width(300.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                MaterialTheme.colorScheme.surface,
                            ),
                        onClick = {
                            onSave()
                        }) {
                        Text(getString(R.string.text_ok),
                            color = MaterialTheme.colorScheme.secondary)
                    }
                },

                dismissButton = {},
                properties = DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false,
                ),
            )
        }
    }

    @Composable
    fun NoBackInTimeDialog(
        showNoBackInTimeDialog: Boolean,
        onSave: () -> Unit,
    ) {
        if (showNoBackInTimeDialog) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text(
                    text = getString(R.string.sms_time_travel_titel),
                    fontWeight = FontWeight.Bold,
                    style = TextStyle(
                        fontSize = 20.sp,
                    ))},

                text = { Text(
                    text = getString(R.string.sms_time_travel_Text),
                    fontWeight = FontWeight.Bold,
                )},

                confirmButton = {
                    TextButton(
                        modifier = Modifier
                            .shadow(4.dp, shape = RoundedCornerShape(14.dp))
                            .width(300.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                MaterialTheme.colorScheme.surface,
                            ),
                        onClick = {
                        onSave()
                    }) {
                        Text(getString(R.string.text_ok))
                    }
                },

                dismissButton = {},
                properties = DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false,
                ),
            )
        }
    }

    @Composable
    fun SendNowDialog(
        showSendNowDialog: Boolean,
        onSave: () -> Unit,
        onDismiss: () -> Unit
    ) {
        if (showSendNowDialog) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text(
                    text = getString(R.string.send_now_Titel),
                    fontWeight = FontWeight.Bold,
                    style = TextStyle(
                        fontSize = 20.sp,
                    ))},

                text = { Text(
                    text = getString(R.string.send_now_Description),
                    fontWeight = FontWeight.Bold,
                    )},

                confirmButton = {
                    TextButton(
                        modifier = Modifier
                            .shadow(4.dp, shape = RoundedCornerShape(14.dp))
                            .width(130.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                MaterialTheme.colorScheme.surface,
                            ),
                        onClick = {
                            onSave()
                            onDismiss()
                        }) {
                        Text(getString(R.string.text_ok))
                    }
                },

                dismissButton = {
                    TextButton(
                        modifier = Modifier
                            .shadow(4.dp, shape = RoundedCornerShape(14.dp))
                            .width(130.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                MaterialTheme.colorScheme.surface,
                            ),
                        onClick = { onDismiss() }) {
                        Text(getString(R.string.text_Cancel))
                    }
                },
                properties = DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false,
                ),
            )
        }
    }

    @Composable
    fun DeleteDialog(
        showDeleteDialog: Boolean,
        onSave: () -> Unit,
        onDismiss: () -> Unit
    ) {
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text(
                    text = getString(R.string.DeleteScheduleMessage),
                    fontWeight = FontWeight.Bold,
                    style = TextStyle(
                        fontSize = 20.sp,
                    ))},
                text = { Text(
                    text = getString(R.string.DeleteScheduleMessage),
                    fontWeight = FontWeight.Bold,
                ) },
                confirmButton = {
                    TextButton(
                        modifier = Modifier
                            .shadow(4.dp, shape = RoundedCornerShape(14.dp))
                            .width(130.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                MaterialTheme.colorScheme.error,
                            ),
                        onClick = {
                        onDismiss()
                        onSave()
                    }) {
                        Text(getString(R.string.delete_name),
                            color = MaterialTheme.colorScheme.onError,
                        fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        modifier = Modifier
                            .shadow(4.dp, shape = RoundedCornerShape(14.dp))
                            .width(130.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                MaterialTheme.colorScheme.surface,
                            ),
                        onClick = { onDismiss() }) {
                        Text(getString(R.string.text_Cancel),
                                fontWeight = FontWeight.Bold)
                    }
                },
                properties = DialogProperties(),
            )
        }
    }

    @Composable
    fun ReSceduledOrNotDialog(
        showReSceduledOrNotDialog: Boolean,
        onSave: () -> Unit,
        onDismiss: () -> Unit
    ) {
        if (showReSceduledOrNotDialog) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text(
                    text = getString(R.string.ReSceduleOrNotDialog_title),
                    fontWeight = FontWeight.Bold,
                    style = TextStyle(
                        fontSize = 20.sp,
                    ))},
                text = { Text(
                    text = getString(R.string.ReSceduleOrNotDialog_text)
                            + "\n" + timeAndDate
                            + "\n" + getString(R.string.history_info_Repeat_name) + " " + repeats
                            + "\n" + phoneNumber
                            + "\n" + MessageFieldText,
                    fontWeight = FontWeight.Bold,
                )},
                confirmButton = {
                    TextButton(
                        modifier = Modifier
                            .shadow(4.dp, shape = RoundedCornerShape(14.dp))
                            .width(130.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                MaterialTheme.colorScheme.surface,
                            ),
                        onClick = {
                        onDismiss()
                        onSave()
                    }) {
                        Text(getString(R.string.text_ok))
                    }
                },
                dismissButton = {
                    TextButton(
                        modifier = Modifier
                            .shadow(4.dp, shape = RoundedCornerShape(14.dp))
                            .width(130.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                MaterialTheme.colorScheme.surface,
                            ),
                        onClick = { onDismiss() }) {
                        Text(getString(R.string.text_Cancel))
                    }
                },
                properties = DialogProperties(),
            )
        }
    }

    @Composable
    fun ShowOptionsDialog(
        showDialog: Boolean,
        onDismiss: () -> Unit,
        onConfirm: (String) -> Unit
    ) {
        val choices = listOf(
            stringResource(R.string.send_sms_every_year_text),
            stringResource(R.string.send_sms_every_month_text),
            stringResource(R.string.send_sms_every_week_text),
            stringResource(R.string.send_sms_every_day_text)/*,
            stringResource(R.string.send_sms_every_now_text)*/
        )
        var selectedOptionIndex by remember { mutableStateOf(0) }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = {
                    onDismiss()
                },
                title = {
                    Text(text = stringResource(R.string.history_info_Repeat_name))
                },
                text = {
                    Column {
                        choices.forEachIndexed { index, choice ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedOptionIndex = index
                                    }
                            ) {
                                RadioButton(
                                    selected = (selectedOptionIndex == index),
                                    onClick = {
                                        selectedOptionIndex = index
                                    }
                                )
                                Text(text = choice)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onConfirm(choices[selectedOptionIndex])
                            onDismiss()
                        }
                    ) {
                        Text(text = stringResource(R.string.text_ok))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            onDismiss()
                        }
                    ) {
                        Text(text = stringResource(R.string.text_Cancel))
                    }
                }
            )
        }
    }
    @Composable
    fun ToastDialog(
         ToastBool: Boolean,
        onSave: () -> Unit,
    ) {
        if (ToastBool) {
            AlertDialog(

                onDismissRequest = {},
                title = { Text(
                    text = ("\uD83D\uDE45"),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .width(300.dp),
                )},

                text = { Text(
                    text = ToastContent.toString(),
                    fontWeight = FontWeight.Bold,
                )},

                confirmButton = {
                    TextButton(
                        modifier = Modifier
                            .shadow(4.dp, shape = RoundedCornerShape(14.dp))
                            .width(300.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                MaterialTheme.colorScheme.surface,
                            ),
                        onClick = {
                            onSave()
                        }) {
                        Text(getString(R.string.text_ok))
                    }
                },

                dismissButton = {},
                properties = DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false,
                ),

                )
        }
    }
}










