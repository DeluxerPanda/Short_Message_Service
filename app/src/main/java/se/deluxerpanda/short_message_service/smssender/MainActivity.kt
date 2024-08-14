package se.deluxerpanda.short_message_service.smssender

import android.Manifest
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.DatePicker
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.google.android.material.snackbar.Snackbar
import se.deluxerpanda.short_message_service.R
import se.deluxerpanda.short_message_service.profile.ContactInfo
import se.deluxerpanda.short_message_service.profile.ProfileActivity
import se.deluxerpanda.short_message_service.ui.theme.AppTheme
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.UUID


class MainActivity : AppCompatActivity() {

    private val SMS_PERMISSION_REQUEST_CODE: Int = 1

    private var ToastContent: String? = null

    private var PhoneNumberFieldText: String? = ""

    private var MessageFieldText: String? = ""

    private val MaxNumbers = 9
    private var CurrentNumber = 0
    private var phoneNumber: String? = null
    private var editedphoneNumber: String? = null

    private var phonenumber_extra: String? = null

    private var DateSet: String? = null

    private var TimeSet: String? = null

    private var repeats: String? = "null"

    private var phoneNumberPattern = Regex("^\\d$")

    private var contactNameAndLast: String? = null

    private var photoUri: Uri? = null

    private fun checkPermissions(): Boolean {
        // Define an array of all the permissions you want to check
        val permissions = arrayOf(
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.POST_NOTIFICATIONS,
        )


        // Check if all permissions are granted
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Return false if any permission is not granted
                return false
            }
        }
        // Return true if all permissions are granted
        return true
    }

     fun checkPermissionsSMS(): Boolean {
        // Define an array of all the permissions you want to check
        val permissions = arrayOf(
            Manifest.permission.SEND_SMS,
        )

        // Check if all permissions are granted
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Return false if any permission is not granted
                return false
            }
        }
        // Return true if all permissions are granted
        return true
    }


    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.SEND_SMS,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.READ_PHONE_STATE
            ),
            SMS_PERMISSION_REQUEST_CODE
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!checkPermissions()) {
            requestPermission()
        }

        val messageResId = intent.getIntExtra("EXTRA_SNACKBAR_MESSAGE", -1)
        if (messageResId != -1) {
            // Show the Snackbar with the message from the resource ID
            Snackbar.make(
                findViewById(android.R.id.content),
                messageResId,
                Snackbar.LENGTH_LONG
            ).show()
        }

        // Use the current date as the default date in the picker.
        val c = Calendar.getInstance()
        var year = c[Calendar.YEAR]
        var month = c[Calendar.MONTH]
        var day = c[Calendar.DAY_OF_MONTH]
        DateSet = String.format("%04d-%02d-%02d", year, month + 1, day)

        var hour: Int
        var minute: Int

        // Add 6 minutes to the current time
        val addTime = c.apply {
            add(Calendar.MINUTE, 6)
        }
        hour = addTime[Calendar.HOUR_OF_DAY]
        minute = addTime[Calendar.MINUTE]

        TimeSet = String.format("%02d:%02d", hour, minute)



        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "MainScreen") {
                    composable("MainScreen") { entry ->
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState()),

                        ) {

                            Box() {
                                /*              IconButton(onClick = {
                                                  intent = Intent(
                                                      this@MainActivity,
                                                      SettingsActivity::class.java
                                                  )
                                                  startActivity(intent)
                                              },
                                                  modifier = Modifier
                                                      .size(34.dp)
                                                      .align(Alignment.TopStart)

                                              ) {
                                                  Icon(
                                                      painter = painterResource(id = R.drawable.baseline_settings),
                                                      contentDescription = "Save button"
                                                  )
                                              }*/

                                Text(
                                    text = stringResource(id = R.string.app_screen_name),
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 24.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }

                                val pInfo: PackageInfo = getPackageManager()
                                    .getPackageInfo(getPackageName(), 0)
                                val version = pInfo.versionName

                                Text(
                                    text = "Version: $version",
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth(),
                                )



                            Spacer(modifier = Modifier.height(16.dp))

                            //  PhoneNumberSection()
                            var number by remember { mutableStateOf(PhoneNumberFieldText) }
                            val launchPhoneList = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.StartActivityForResult()
                            ) { result ->
                                if (result.resultCode == RESULT_OK) {
                                    val phoneNumberData =
                                        result.data?.getStringExtra("PHONE_NUMBER_FROM_CONTACTS")
                                    phoneNumberData?.let { _ ->
                                        number = phoneNumberData
                                    }

                                }
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                                    .clip(RoundedCornerShape(12.dp))
                            ) {

                                number?.let {
                                    TextField(
                                        value = it,
                                        onValueChange = { number = it },
                                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone),
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        label = {Text(
                                            text = stringResource(id = R.string.text_hint_phone_number)
                                        )}
                                    )
                                    PhoneNumberFieldText = number
                                }
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_baseline_import_contacts),
                                    contentDescription = "To contacts",
                                    modifier = Modifier
                                        .size(40.dp)
                                        .padding(end = 8.dp)
                                        .align(Alignment.CenterEnd)
                                        .clickable {
                                            intent = Intent(
                                                this@MainActivity,
                                                PhoneListActivity::class.java
                                            )
                                            launchPhoneList.launch(intent)
                                        }
                                )
                            }

                                 Spacer(modifier = Modifier.height(5.dp))

                            var phonenumber_extra_numbers = 0
                            val phonenumber_number_extra_entry =
                                entry.savedStateHandle.get<Int>("EXTRA_MAINACTIVITY_CURRENT_NUMBER")
                            if (phonenumber_number_extra_entry != null) {
                                phonenumber_extra_numbers = phonenumber_number_extra_entry
                            }

                            val phonenumber_extra_entry =
                                entry.savedStateHandle.get<String>("EXTRA_MAINACTIVITY_FINAL_PHONENUMBER")
                            if (phonenumber_extra_entry != null) {
                                phonenumber_extra = phonenumber_extra_entry
                            }
                            Column(
                                        modifier = Modifier
                                        .align(Alignment.CenterHorizontally),
                            ) {
                                var selected by remember { mutableStateOf(true) }

                                FilterChip(

                                    onClick = {
                                        phoneNumber = phonenumber_extra
                                        navController.navigate("AddMoreNumbersScreen")
                                    },
                                    label = {
                                        Text(
                                            text = stringResource(id = R.string.text_add_phone_number) + " " + phonenumber_extra_numbers.toString() + " / " + MaxNumbers,

                                        )
                                    },
                                    selected = selected,
                                    leadingIcon = if (selected) {
                                        {
                                            Icon(
                                                painter = painterResource(id = R.drawable.baseline_person_add),
                                                contentDescription = "Done icon",
                                                modifier = Modifier
                                                    .size(FilterChipDefaults.IconSize)
                                            )
                                        }
                                    } else {
                                        null
                                    },
                                )
                            }


                               Spacer(modifier = Modifier.height(5.dp))
                            //  MessageSection()
                            var message by remember { mutableStateOf(MessageFieldText) }
                            var showMessageCharactersRetchDialog by remember { mutableStateOf(false) }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                                    .clip(RoundedCornerShape(12.dp)),

                            ) {
                                message?.let {
                                    TextField(
                                        value = it,
                                        onValueChange = {
                                            if (!isSmsTooLong(it)) {
                                                message = it
                                            }else{
                                                showMessageCharactersRetchDialog = true
                                            }
                                            },
                                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                 label = {Text(
                                            text = stringResource(id = R.string.text_hint_message)
                                        )}
                                    )
                                    MessageFieldText = message
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // TimePickerSection()
                            val mContext = LocalContext.current
                            var Time by remember { mutableStateOf(TimeSet) }
                            // Parsing hour and minute from the Time string
                            val mHour = try {
                                Time!!.substringBeforeLast(":").trim().toInt()
                            } catch (e: NumberFormatException) {
                                0
                            }
                            val mMinute = try {
                                Time!!.substringAfterLast(":").trim().toInt()
                            } catch (e: NumberFormatException) {
                                0
                            }

                            // Creating a TimePicker dialog
                            val mTimePickerDialog = TimePickerDialog(
                                mContext,
                                { _, hour: Int, minute: Int ->
                                    val formattedHour = String.format("%02d", hour)
                                    val formattedMinute = String.format("%02d", minute)
                                    val newTime = "$formattedHour:$formattedMinute"
                                    TimeSet = newTime
                                    Time = newTime
                                }, mHour, mMinute, true
                            )
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = stringResource(id = R.string.set_time_button_text),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.Center
                                )
                                Button(
                                    onClick = { mTimePickerDialog.show() },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = TimeSet!!,
                                        fontSize = 20.sp,
                                    )
                                }
                            }

                            //DatePickerSection()
                            var Date by remember { mutableStateOf(DateSet) }
                            // Parsing year, month, and day from the Date string
                            val mYear = try {
                                Date!!.substringBefore("-").trim().toInt()
                            } catch (e: NumberFormatException) {
                                0
                            }
                            val mMonth = try {
                                Date!!.substringAfter("-").substringBefore("-").trim().toInt()
                            } catch (e: NumberFormatException) {
                                0
                            } - 1
                            val mDay = try {
                                Date!!.substringAfterLast("-").substringBefore(" | ").trim().toInt()
                            } catch (e: NumberFormatException) {
                                0
                            }
                            // Creating a DatePickerDialog
                            val mDatePickerDialog = DatePickerDialog(
                                mContext,
                                { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                                    val formattedMonth = String.format("%02d", month + 1)
                                    val formattedDay = String.format("%02d", dayOfMonth)
                                    val newDate = "$year-$formattedMonth-$formattedDay"
                                    DateSet = newDate
                                    Date = newDate
                                }, mYear, mMonth, mDay
                            )
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = stringResource(id = R.string.set_date_start_button_text),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.Center
                                )
                                Button(
                                    onClick = { mDatePickerDialog.show() },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = DateSet!!,
                                        fontSize = 20.sp,
                                    )
                                }
                            }

                            //   SendEverySection()
                            var ShowOptionsDialog by remember { mutableStateOf(false) }
                            var showNoBackInTimeDialog by remember { mutableStateOf(false) }
                            var showNoMainPhoneOrMessageDialog by remember { mutableStateOf(false) }

                            if (repeats == "null") {
                                repeats = stringResource(id = R.string.send_sms_every_year_text)
                            }
                            var repeatsEdited by remember { mutableStateOf(repeats) }
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = stringResource(id = R.string.send_sms_every_text),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.Center
                                )
                                Button(
                                    onClick = { ShowOptionsDialog = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {

                                    Text(
                                        text = repeatsEdited!!,
                                        fontSize = 20.sp,
                                    )
                                }
                                OptionsDialog(
                                    ShowOptionsDialog = ShowOptionsDialog,
                                    onDismiss = { ShowOptionsDialog = false },
                                    onConfirm = { selectedOption ->
                                        repeatsEdited = selectedOption
                                        repeats = repeatsEdited
                                    }
                                )
                                NoBackInTimeDialog(
                                    showNoBackInTimeDialog = showNoBackInTimeDialog,
                                    onSave = {
                                        showNoBackInTimeDialog = false
                                    }
                                )
                                NoMainPhoneOrMessageDialog(
                                    showNoMainPhoneOrMessageDialog = showNoMainPhoneOrMessageDialog,
                                    onSave = {
                                        showNoMainPhoneOrMessageDialog = false
                                    }
                                )
                                MessageCharactersRetchDialog(
                                    showMessageCharactersRetchDialog = showMessageCharactersRetchDialog,
                                    onSave = {
                                        showMessageCharactersRetchDialog = false
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(
                                        0xFF38761D
                                    )
                                ),
                                modifier = Modifier
                                    .fillMaxWidth(),
                                onClick = {
                                    if (!PhoneNumberFieldText!!.isNullOrEmpty() && !message!!.isNullOrEmpty()) {
                                        if (!isSmsTooLong(message!!)) {


                                        val sdf = SimpleDateFormat("yyyy-MM-dd H:m")
                                        val sdfDateTime =
                                            DateTimeFormatter.ofPattern("yyyy-MM-dd H:mm")
                                        val dateTimeString: String = DateSet + " " + TimeSet
                                        val date = sdf.parse(dateTimeString)
                                        val triggerTime = date?.time

                                        val selectedDateTime =
                                            LocalDateTime.parse("$DateSet $TimeSet", sdfDateTime)
                                        val currentDateTime = LocalDateTime.now()

                                        if (selectedDateTime.isAfter(currentDateTime)) {

                                            try {
                                                var PhoneNumberFieldTextEdit: String = ""
                                                if (editedphoneNumber != null && editedphoneNumber!!.isNotEmpty() && PhoneNumberFieldText!!.isNotEmpty()) {
                                                    PhoneNumberFieldTextEdit =
                                                        "$PhoneNumberFieldText,$editedphoneNumber"
                                                } else if (PhoneNumberFieldText!!.isNotEmpty()) {
                                                    PhoneNumberFieldTextEdit =
                                                        "$PhoneNumberFieldText"
                                                }

                                                val alarmId = UUID.randomUUID().hashCode()

                                                val alarmManager =
                                                    getSystemService(ALARM_SERVICE) as AlarmManager

                                                val intent: Intent = Intent(
                                                    this@MainActivity,
                                                    AlarmReceiver::class.java
                                                )

                                                intent.putExtra(
                                                    "EXTRA_PHONE_NUMBER",
                                                    PhoneNumberFieldTextEdit
                                                )
                                                intent.putExtra("EXTRA_MESSAGES", message)
                                                intent.putExtra("EXTRA_ALARMID", alarmId)
                                                intent.putExtra("EXTRA_TRIGGERTIME", triggerTime)
                                                intent.putExtra("EXTRA_REPEATSMS", repeatsEdited)

                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                    startForegroundService(intent)
                                                } else {
                                                    startService(intent)
                                                }

                                                val pendingIntent = PendingIntent.getBroadcast(
                                                    this@MainActivity, alarmId, intent,
                                                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                                                )

                                                alarmManager.setExactAndAllowWhileIdle(
                                                    AlarmManager.RTC_WAKEUP,
                                                    triggerTime!!,
                                                    pendingIntent
                                                )


                                                saveAlarmDetails(
                                                    this@MainActivity,
                                                    alarmId,
                                                    triggerTime,
                                                    repeatsEdited,
                                                    PhoneNumberFieldTextEdit,
                                                    message
                                                )

                                                val BackToStartIntent = Intent(
                                                    this@MainActivity,
                                                    MainActivity::class.java
                                                )
                                                BackToStartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                                BackToStartIntent.putExtra(
                                                    "EXTRA_SNACKBAR_MESSAGE",
                                                    R.string.SnackBar_text_sms_scheduled
                                                )
                                                startActivity(BackToStartIntent)


                                            } catch (e: ParseException) {
                                                e.printStackTrace()
                                            }

                                        } else {
                                            showNoBackInTimeDialog = true
                                        }
                                    } else {
                                            showMessageCharactersRetchDialog = true
                                        }
                                } else {
                                        showNoMainPhoneOrMessageDialog = true
                                    }
                                }
                            ) {
                                Text(
                                    text = stringResource(id = R.string.schedule_sms_button_text),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFe5e9f0),
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = stringResource(id = R.string.history_name),
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )

                            ScheduledSMSListUI()

                            Spacer(modifier = Modifier.height(16.dp))
                        }

                    }
                    composable("AddMoreNumbersScreen") {

                        val scrollBehavior =
                            TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
                        Scaffold(
                            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),

                            topBar = {
                                CenterAlignedTopAppBar(
                                    title = {
                                        Text(
                                            stringResource(id = R.string.text_add_phone_number_add_text),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    navigationIcon = {
                                        IconButton(onClick = {
                                            navController.previousBackStackEntry
                                                ?.savedStateHandle
                                                ?.set(
                                                    "EXTRA_MAINACTIVITY_CURRENT_NUMBER",
                                                    CurrentNumber
                                                )

                                            navController.previousBackStackEntry
                                                ?.savedStateHandle
                                                ?.set(
                                                    "EXTRA_MAINACTIVITY_FINAL_PHONENUMBER",
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
                            var list by remember {
                                mutableStateOf(
                                    phoneNumber?.split(",")?.filter { it.isNotEmpty() } ?: listOf()
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
                                            val phoneNumberData =
                                                result.data?.getStringExtra("PHONE_NUMBER_FROM_CONTACTS")
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

                                     CurrentNumber = list.filter { it.isNotEmpty() }.let { if (it.isEmpty()) 0 else it.size }

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
                                                    intent = Intent(
                                                        this@MainActivity,
                                                        PhoneListActivity::class.java
                                                    )
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
                                                                list.removeAt(index)
                                                                CurrentNumber = list.filter { it.isNotEmpty() }.let { if (it.isEmpty()) 0 else it.size }
                                                                editedphoneNumber =
                                                                    list.joinToString(",")
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
                                            if (list.size < MaxNumbers) {
                                                list.add("")
                                                isPhoneNumberChanged = true
                                            } else {
                                                ToastContent =
                                                    getString(R.string.history_info_Profile_Edit_cannot_have_more_number) +
                                                            " " + MaxNumbers.toString() + " " + getString(
                                                        R.string.More_Then_One_Number_name
                                                    )

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
                }

            }
        }
        }
    }

    @Composable
    fun ScheduledSMSListUI() {
        val alarmList = getAllAlarms(this@MainActivity)
        if (alarmList.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
                Text(
                    text = stringResource(R.string.history_info_no_SMS_scheduled),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
        }
            } else {
                    alarmList.forEach { alarmDetails ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .clip(RoundedCornerShape(12.dp))
                                .padding(vertical = 8.dp, horizontal = 16.dp)
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                   if (alarmDetails.phonenumber!!.contains(",")) {
                                       Icon(
                                           painter = painterResource(id =  R.drawable.baseline_groups),
                                           contentDescription = null,
                                           modifier = Modifier
                                               .size(48.dp)
                                               .padding(end = 8.dp, top = 4.dp, bottom = 4.dp)
                                       )
                                    } else {
                                       contactNameAndLast = ContactInfo.getContactName(contentResolver,alarmDetails.phonenumber,this@MainActivity)
                                       photoUri = ContactInfo.getContactPhotoUri2(this@MainActivity,contactNameAndLast)
                                        if (photoUri != null) {
                                            Image(
                                                painter = rememberAsyncImagePainter(photoUri),
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .padding(end = 8.dp, top = 4.dp, bottom = 4.dp)
                                                    .clip(RoundedCornerShape(50.dp))
                                            )
                                        }else{
                                            Icon(
                                                painter = painterResource(id =  R.drawable.baseline_person),
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .padding(end = 8.dp, top = 4.dp, bottom = 4.dp)
                                                    .padding(end = 8.dp)
                                            )
                                        }

                                    }


                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                ) {
                                    var title = ContactInfo.processPhoneNumbers(alarmDetails.phonenumber, contentResolver, this@MainActivity)
                                    Text(
                                        text = title,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(bottom = 8.dp),
                                        style = TextStyle(textDecoration = TextDecoration.Underline)
                                    )

                                    Text(
                                        text = alarmDetails.message!!,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                    Text(
                                        text = SimpleDateFormat("yyyy-MM-dd | H:mm").format(alarmDetails.timeInMillis) + " | " + alarmDetails.repeatSmS,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier
                                            .align(Alignment.CenterHorizontally)
                                    )
                                }

                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_settings),
                                    contentDescription = stringResource(id = R.string.todo),
                                    modifier = Modifier
                                        .size(34.dp)
                                        .align(Alignment.CenterVertically)
                                        .clickable {
                                            val intent = Intent(
                                                this@MainActivity,
                                                ProfileActivity::class.java
                                            ).apply {
                                                putExtra(
                                                    "EXTRA_HISTORY_PROFILE_ALARMID",
                                                    alarmDetails.alarmId
                                                )
                                                putExtra("EXTRA_HISTORY_PROFILE_POTOURL", photoUri)
                                                putExtra(
                                                    "EXTRA_HISTORY_PROFILE_TIMEANDDATE",
                                                    SimpleDateFormat("yyyy-MM-dd | H:mm").format(
                                                        alarmDetails.timeInMillis
                                                    )
                                                )
                                                putExtra(
                                                    "EXTRA_HISTORY_PROFILE_REPEATS",
                                                    alarmDetails.repeatSmS
                                                )
                                                putExtra(
                                                    "EXTRA_HISTORY_PROFILE_PHONENUMBER",
                                                    alarmDetails.phonenumber
                                                )
                                                putExtra(
                                                    "EXTRA_HISTORY_PROFILE_MESSAGE",
                                                    alarmDetails.message
                                                )
                                            }
                                            startActivity(intent)
                                        }
                                )
                            }
                        }

                    }
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
                title = {
                    Text(
                        text = ("\uD83D\uDE45"),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .width(300.dp),
                    )
                },

                text = {
                    Text(
                        text = ToastContent.toString(),
                        fontWeight = FontWeight.Bold,
                    )
                },

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
                        Text(
                            getString(R.string.text_ok),
                            color = MaterialTheme.colorScheme.secondary
                        )
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
    fun OptionsDialog(
        ShowOptionsDialog: Boolean,
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

        if (ShowOptionsDialog) {
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
                        Text(
                            text = stringResource(R.string.text_ok),
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            onDismiss()
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.text_Cancel),
                        )
                    }
                }
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
    fun NoMainPhoneOrMessageDialog(
        showNoMainPhoneOrMessageDialog: Boolean,
        onSave: () -> Unit,
    ) {
        if (showNoMainPhoneOrMessageDialog) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text(
                    text = getString(R.string.sms_number_or_masage_are_empty_titel),
                    fontWeight = FontWeight.Bold,
                    style = TextStyle(
                        fontSize = 20.sp,
                    ))},

                text = { Text(
                    text = getString(R.string.sms_number_or_masage_are_empty_text),
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

    companion object {

        var CHANNEL_ID: String = UUID.randomUUID().hashCode().toString()

        var CHANNEL_NAME: String = java.lang.String.valueOf(R.string.app_name)

        fun saveAlarmDetails(
            context: Context,
        alarmId: Int,
        triggerTime: Long,
        repeatSmS: String?,
        phonenumber: String?,
        message: String?
    ) {
        val preferences = context.getSharedPreferences("AlarmDetails", MODE_PRIVATE)
        val editor = preferences.edit()

        // Use unique keys for each alarm and each value
        val triggerTimeKey = "triggerTime_$alarmId"
        val getRepeatSmSKey = "getRepeatSmSKey_$alarmId"

        val getPhoneNumberKey = "getPhoneNumberKey_$alarmId"
        val getMessageKey = "getMessageKey_$alarmId"

        // Save the triggerTime and releaseTime using their respective keys
        editor.putLong(triggerTimeKey, triggerTime)
        editor.putString(getRepeatSmSKey, repeatSmS)

        editor.putString(getPhoneNumberKey, phonenumber)
        editor.putString(getMessageKey, message)

        editor.apply()
    }

    class AlarmDetails(
        val alarmId: Int,
        val timeInMillis: Long,
        val repeatSmS: String?,
        val phonenumber: String?,
        val message: String?
    )

    // Retrieve a list of all alarms
    @JvmStatic
    fun getAllAlarms(context: Context): List<AlarmDetails> {
        val alarmList: MutableList<AlarmDetails> = ArrayList()
        val preferences = context.getSharedPreferences("AlarmDetails", MODE_PRIVATE)

        val uniqueAlarmIds: MutableSet<Int> = HashSet()

        // Iterate through all saved alarms and add them to the list
        val allEntries = preferences.all
        for ((key) in allEntries) {
            // Separate keys for triggerTime and releaseTime
            val triggerTimeKey = "triggerTime_" + key.substring(key.lastIndexOf("_") + 1)

            val getRepeatSmSKey = "getRepeatSmSKey_" + key.substring(key.lastIndexOf("_") + 1)

            val getRepeatSmS = preferences.getString(getRepeatSmSKey, 0.toString())

            val getPhonenumberKey = "getPhoneNumberKey_" + key.substring(key.lastIndexOf("_") + 1)
            val getPhonenumber = preferences.getString(getPhonenumberKey, 0.toString())

            val getMessageKey = "getMessageKey_" + key.substring(key.lastIndexOf("_") + 1)
            val getMessage = preferences.getString(getMessageKey, 0.toString())

            val triggerTime = preferences.getLong(triggerTimeKey, 0)

            val alarmId = key.substring(key.lastIndexOf("_") + 1).toInt()
            if (!uniqueAlarmIds.contains(alarmId)) {
                val alarmDetails =
                    AlarmDetails(alarmId, triggerTime, getRepeatSmS, getPhonenumber, getMessage)
                alarmList.add(alarmDetails)

                // Lägg till alarmId i set för att undvika dubbletter
                uniqueAlarmIds.add(alarmId)
            }
        }

        return alarmList
    }


        fun getAlarmById(context: Context, alarmId: Int): AlarmDetails? {
            val allAlarms = getAllAlarms(context)
            for (alarm in allAlarms) {
                if (alarm.alarmId == alarmId) {
                    return alarm
                }
            }
            return null
        }


    fun deleteAlarm(alarmId: Int, context: Context) {
        val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(context, alarmId, intent, PendingIntent.FLAG_MUTABLE)

        // Cancel the alarm
        alarmManager.cancel(pendingIntent)

        // Remove alarm details from shared preferences
        val preferences = context.getSharedPreferences("AlarmDetails", MODE_PRIVATE)
        val editor = preferences.edit()

        // Remove entries for the specified alarmId
        val triggerTimeKey = "triggerTime_$alarmId"
        val releaseTimeKey = "releaseTime_$alarmId"
        val getRepeatSmSKey = "getRepeatSmSKey_$alarmId"
        val getPhonenumber = "getPhoneNumberKey_$alarmId"
        val getMessage = "getMessageKey_$alarmId"


        editor.remove(triggerTimeKey)
        editor.remove(releaseTimeKey)
        editor.remove(getRepeatSmSKey)
        editor.remove(getPhonenumber)
        editor.remove(getMessage)

        editor.apply()


        val intenta = Intent(context, MainActivity::class.java)
        intenta.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intenta)
    }

        fun isSmsTooLong(message: String): Boolean {
            // Define the limits
            val gsmSingleMessageLimit = 160
            val gsmMultipartMessageLimit = 153
            val ucs2SingleMessageLimit = 70
            val ucs2MultipartMessageLimit = 67

            // Check if message contains non-GSM characters
            val containsNonGsmCharacters = message.any { it.code > 127 }

            return if (containsNonGsmCharacters) {
                // UCS-2 encoding
                if (message.length > ucs2SingleMessageLimit) {
                    val segments = (message.length + ucs2MultipartMessageLimit - 1) / ucs2MultipartMessageLimit
                    segments > 1
                } else {
                    false
                }
            } else {
                // GSM 7-bit encoding
                if (message.length > gsmSingleMessageLimit) {
                    val segments = (message.length + gsmMultipartMessageLimit - 1) / gsmMultipartMessageLimit
                    segments > 1
                } else {
                    false
                }
            }
        }
    }

    fun deleteAlarm(alarmId: Int, context: Context) {
return Companion.deleteAlarm(alarmId ,context)
    }



}
