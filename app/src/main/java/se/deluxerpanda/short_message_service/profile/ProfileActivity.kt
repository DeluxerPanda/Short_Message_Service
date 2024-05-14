package se.deluxerpanda.short_message_service.profile

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily.Companion.SansSerif
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import se.deluxerpanda.short_message_service.R
import se.deluxerpanda.short_message_service.smssender.MainActivity
import se.deluxerpanda.short_message_service.ui.theme.AppTheme
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.util.Calendar
import java.util.Date
import java.util.Locale


class ProfileActivity  : ComponentActivity() {

    private var title: String? = null
    private var alarmId = 0

    private var photoUri: Uri? = null

    private var photoUriString: String? = null

    private var timeAndDate: String? = null
    private var editedtimeAndDate: String? = null

    private var phoneNumber: String? = null
    private var phoneNumberNew: String? = null
    private var editedphoneNumber: String? = null
    private var editedphoneNumberNew: String? = null

    private var MessageFieldText: String? = null


    private var  isMessageChanged: Boolean = false
    private var editedMessage: String? = null

    private var contactName: String? = null
    private var contactNameAndLast: String? = null
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("DefaultLocale")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        if (intent != null) {
            title = intent.getStringExtra("EXTRA_HISTORY_PROFILE_TITLE")
            alarmId = intent.getIntExtra("EXTRA_HISTORY_PROFILE_ALARMID", 0)

            photoUriString = intent.getStringExtra("EXTRA_HISTORY_PROFILE_POTOURL")
            if (photoUriString != null){
                photoUri = Uri.parse(photoUriString)
            }


            timeAndDate = intent.getStringExtra("EXTRA_HISTORY_PROFILE_TIMEANDDATE")

            phoneNumber = intent.getStringExtra("EXTRA_HISTORY_PROFILE_PHONENUMBER")
            phoneNumberNew = if (phoneNumber!!.contains(",")) {
                phoneNumber!!.replace(",", "\n")
            } else {
                phoneNumber
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
                        }

                        val MessageFieldTextupdate =
                            entry.savedStateHandle.get<String>("EXTRA_PROFILE_EDITOR_FINAL_MESSAGE")
                        if (MessageFieldTextupdate != null) {
                            MessageFieldText = MessageFieldTextupdate
                        }

                        val PhoneNumberFieldTextupdate =
                            entry.savedStateHandle.get<String>("EXTRA_PROFILE_EDITOR_FINAL_PHONENUMBER")
                        if (PhoneNumberFieldTextupdate != null) {
                            phoneNumber = PhoneNumberFieldTextupdate

                            phoneNumberNew = if (phoneNumber!!.contains(",")) {
                                phoneNumber!!.replace(",", "\n")
                            } else {
                                phoneNumber
                            }
                             photoUri = getContactPhotoUri(contactNameAndLast)
                        }

                        val scrollBehavior =
                            TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
                        Scaffold(
                            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),

                            topBar = {
                                CenterAlignedTopAppBar(
                                    colors = TopAppBarDefaults.topAppBarColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    ),
                                    title = {
                                    },
                                    navigationIcon = {
                                        IconButton(onClick = {
                                            finish()
                                        }
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.baseline_close),
                                                contentDescription = "Save button"
                                            )
                                        }
                                    },

                                    actions = {
                                        IconButton(onClick = {
                                            Toast.makeText(
                                                this@ProfileActivity,
                                                "Coming Soon!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        })
                                        {
                                            Icon(
                                                painter = painterResource(id = R.drawable.baseline_send),
                                                contentDescription = "Close button"
                                            )
                                        }
                                        IconButton(onClick = {
                                            Toast.makeText(
                                                this@ProfileActivity,
                                                "Coming Soon!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        })
                                        {
                                            Icon(
                                                painter = painterResource(id = R.drawable.baseline_pause),
                                                contentDescription = "Close button"
                                            )
                                        }
                                        IconButton(onClick = {
                                            val mainActivity = MainActivity()
                                            mainActivity.deleteAlarm(
                                                alarmId,
                                                this@ProfileActivity
                                            )
                                            finish()
                                        })
                                        {
                                            Icon(
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
                                Text(
                                    text = title!!,
                                    textAlign = TextAlign.Center,
                                    fontSize = 24.sp,
                                    fontFamily = SansSerif,
                                    fontWeight = FontWeight.Bold
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
                                    ) {
                                        if (photoUri != null) {
                                            Image(
                                                painter = rememberAsyncImagePainter(photoUri),

                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(130.dp)
                                                    .align(Alignment.CenterHorizontally)
                                            )
                                        } else {
                                            Image(
                                                painter = painterResource(id = R.drawable.ic_baseline_person_24),
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
                                                text = timeAndDate!!,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                modifier = Modifier.padding(
                                                    top = 10.dp,
                                                    bottom = 10.dp
                                                )
                                            )
                                            Image(
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
                                            Text(
                                                text = phoneNumberNew!!,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                modifier = Modifier.padding(
                                                    top = 10.dp,
                                                    bottom = 10.dp
                                                )
                                            )

                                            Image(
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

                                            Image(
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
                        val scrollBehavior =
                            TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
                        Scaffold(
                            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),

                            topBar = {
                                CenterAlignedTopAppBar(
                                    colors = TopAppBarDefaults.topAppBarColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    ),
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
                                            val timeStr: String = timeAndDate!!.substringAfter("| ")
                                            val dateStr: String = timeAndDate!!.substringBefore(" |")
                                            try {
                                                val selectedDateTime = LocalDateTime.parse("$dateStr $timeStr", sdfDateTime)
                                                val currentDateTime = LocalDateTime.now()

                                                if (selectedDateTime.isAfter(currentDateTime)) {
                                                    navController.previousBackStackEntry
                                                        ?.savedStateHandle
                                                        ?.set(
                                                            "EXTRA_PROFILE_EDITOR_FINAL_TIMEANDDATE",
                                                            editedtimeAndDate
                                                        )
                                                    navController.popBackStack()
                                                } else {
                                                    showNoBackInTimeDialog = true
                                                }
                                            } catch (e: ParseException) {
                                                e.printStackTrace()
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
                                        var showDialog by remember { mutableStateOf(false) }

                                        IconButton(onClick = {

                                            if (editedtimeAndDate == timeAndDate) {
                                                showDialog = true
                                            } else {
                                                showDialog = false
                                                navController.previousBackStackEntry
                                                    ?.savedStateHandle
                                                    ?.set(
                                                        "EXTRA_PROFILE_EDITOR_FINAL_TIMEANDDATE",
                                                        editedtimeAndDate
                                                    )
                                                navController.popBackStack()
                                            }
                                        })
                                        {
                                            Icon(
                                                painter = painterResource(id = R.drawable.baseline_close),
                                                contentDescription = "Close button"
                                            )
                                        }
                                        UnsavedChangesDialog(
                                            showDialog = showDialog,
                                            onDismiss = {
                                                showDialog = false
                                              navController.popBackStack()
                                            },
                                            onSave = {
                                                showDialog = false
                                                val sdfDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd H:mm")
                                                val timeStr: String = timeAndDate!!.substringAfter("| ")
                                                val dateStr: String = timeAndDate!!.substringBefore(" |")
                                                try {
                                                    val selectedDateTime = LocalDateTime.parse("$dateStr $timeStr", sdfDateTime)
                                                    val currentDateTime = LocalDateTime.now()

                                                    if (selectedDateTime.isAfter(currentDateTime)) {
                                                        navController.previousBackStackEntry
                                                            ?.savedStateHandle
                                                            ?.set(
                                                                "EXTRA_PROFILE_EDITOR_FINAL_TIMEANDDATE",
                                                                editedtimeAndDate
                                                            )
                                                    }else{
                                                        showNoBackInTimeDialog = true
                                                    }
                                                } catch (e: ParseException) {
                                                    e.printStackTrace()
                                                }
                                            }
                                        )
                                        NoBackInTimeDialog(
                                            showNoBackInTimeDialog = showNoBackInTimeDialog,
                                            onDismiss = {},
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
                            val mContext = LocalContext.current
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
                                        val newTime = "$hour:$minute"
                                        timeAndDate = timeAndDate!!.replaceAfterLast("| ", newTime)
                                        Time = newTime
                                    }, mHour, mMinute, true
                                )

                                Text(text = "Time")
                                OutlinedButton(onClick = {
                                    mTimePickerDialog.show()
                                }) {
                                    Text(text = Time)

                                }


                                var Date by remember { mutableStateOf(timeAndDate!!.substringBefore("|")) }
                                // Parsing year, month, and day from the Date string
                                val mYear = try { Date.substringBefore("-").trim().toInt() } catch (e: NumberFormatException) { 0 }
                                val mMonth = try { Date.substringAfter("-").substringBefore("-").trim().toInt() } catch (e: NumberFormatException) { 0 } -1
                                val mDay = try { Date.substringAfterLast("-").substringBefore(" | ").trim().toInt() } catch (e: NumberFormatException) { 0 }
                                // Creating a DatePickerDialog
                                val mDatePickerDialog = DatePickerDialog(
                                    mContext,
                                    { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                                        val formattedMonth = String.format("%02d", month + 1)
                                        val newDate = "$year-$formattedMonth-$dayOfMonth"
                                        timeAndDate = timeAndDate!!.replaceBeforeLast(" |", newDate)
                                        Date = newDate

                                    }, mYear, mMonth, mDay
                                )

                                Text(text = "Date")
                                OutlinedButton(onClick = {
                                    mDatePickerDialog.show()
                                }) {
                                    Text(text = Date)
                                }
                            }
                                editedtimeAndDate = timeAndDate
                        }
                    }
                    composable("PhoneNumberField") {
                        val scrollBehavior =
                            TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
                        Scaffold(
                            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),

                            topBar = {
                                CenterAlignedTopAppBar(
                                    colors = TopAppBarDefaults.topAppBarColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    ),
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
                                        var showDialog by remember { mutableStateOf(false) }
                                        IconButton(onClick = {
                                            if (editedphoneNumber != phoneNumber
                                            ) {
                                                showDialog = true
                                            } else {

                                                showDialog = false
                                                it_isPhoneNumberField()
                                                navController.previousBackStackEntry
                                                    ?.savedStateHandle
                                                    ?.set(
                                                        "EXTRA_PROFILE_EDITOR_FINAL_PHONENUMBER",
                                                        editedphoneNumber
                                                    )
                                                navController.popBackStack()
                                            }
                                        })
                                        {
                                            Icon(
                                                painter = painterResource(id = R.drawable.baseline_close),
                                                contentDescription = "Close button"
                                            )
                                        }
                                        UnsavedChangesDialog(
                                            showDialog = showDialog,
                                            onDismiss = {
                                                showDialog = false
                                                navController.popBackStack()
                                            },
                                            onSave = {
                                                showDialog = false
                                                it_isPhoneNumberField()
                                                navController.previousBackStackEntry
                                                    ?.savedStateHandle
                                                    ?.set(
                                                        "EXTRA_PROFILE_EDITOR_FINAL_PHONENUMBER",
                                                        editedphoneNumber
                                                    )
                                            }
                                        )
                                    },

                                    scrollBehavior = scrollBehavior,
                                )
                            },
                        ) { innerPadding ->
                            val keyboardController = LocalSoftwareKeyboardController.current
                            val mContext = LocalContext.current
                            var editedPhoneNumbers by remember {
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
                                editedPhoneNumbers.forEachIndexed { index, phone ->
                                    OutlinedTextField(
                                        value = phone,
                                        onValueChange = {
                                            editedPhoneNumbers =
                                                editedPhoneNumbers.toMutableList().also { list ->
                                                    list[index] = it
                                                }
                                            editedphoneNumber =
                                                editedPhoneNumbers.joinToString(",")
                                            isPhoneNumberChanged = true
                                        },
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Phone,
                                            imeAction = ImeAction.Done,
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onDone = {
                                                keyboardController?.hide()
                                            }
                                        ),
                                        label = {
                                            Text(
                                                text = "Phone number ${index + 1}",
                                                textAlign = TextAlign.Center
                                            )
                                        },
                                        trailingIcon = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                IconButton(onClick = {
                                                    Toast.makeText(
                                                        mContext,
                                                        "Coming Soon! - import contacts!",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }) {
                                                    Icon(
                                                        painter = painterResource(id = R.drawable.ic_baseline_import_contacts),
                                                        contentDescription = "import contacts button"
                                                    )
                                                }
                                                IconButton(onClick = {
                                                    Log.d("editedphoneNumber-1: ", editedphoneNumber!!)
                                                    editedPhoneNumbers =
                                                        editedPhoneNumbers.toMutableList()
                                                            .also { list ->
                                                                if (editedPhoneNumbers.size != 1) {
                                                                        list.removeAt(index)
                                                                } else {
                                                                    Toast.makeText(
                                                                        mContext,
                                                                        R.string.history_info_Profile_Edit_Must_have_one_number,
                                                                        Toast.LENGTH_SHORT
                                                                    ).show()
                                                                }
                                                                editedphoneNumber = editedPhoneNumbers.joinToString(",")
                                                                Log.d("editedphoneNumber-2: ", editedphoneNumber!!)
                                                                isPhoneNumberChanged = true
                                                            }
                                                }) {
                                                    Icon(
                                                        painter = painterResource(id = R.drawable.ic_baseline_delete_outline),
                                                        contentDescription = "delete button"
                                                    )
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                    )
                                }
                                IconButton(onClick = {
                                    editedPhoneNumbers =
                                        editedPhoneNumbers.toMutableList().also { list ->
                                            list.add("")
                                            isPhoneNumberChanged = true
                                        }
                                }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.baseline_add),
                                        contentDescription = "add button"
                                    )
                                }
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
                                    colors = TopAppBarDefaults.topAppBarColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    ),
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
                                        var showDialog by remember { mutableStateOf(false) }
                                        IconButton(onClick = {
                                            if (editedMessage != MessageFieldText
                                            ) {
                                                showDialog = true
                                            } else {
                                                showDialog = false
                                                navController.previousBackStackEntry
                                                    ?.savedStateHandle
                                                    ?.set("EXTRA_PROFILE_EDITOR_FINAL_MESSAGE", editedMessage)
                                                navController.popBackStack()
                                            }
                                        })
                                        {
                                            Icon(
                                                painter = painterResource(id = R.drawable.baseline_close),
                                                contentDescription = "Close button"
                                            )
                                        }
                                        UnsavedChangesDialog(
                                            showDialog = showDialog,
                                            onDismiss = {
                                                showDialog = false
                                                navController.popBackStack()},
                                            onSave = {
                                                showDialog = false
                                                navController.previousBackStackEntry
                                                    ?.savedStateHandle
                                                    ?.set("EXTRA_PROFILE_EDITOR_FINAL_MESSAGE", editedMessage)
                                            }
                                        )
                                    },

                                    scrollBehavior = scrollBehavior,
                                )
                            },
                        ) { innerPadding ->
                            val mContext = LocalContext.current

                            val keyboardController = LocalSoftwareKeyboardController.current

                            var text by remember { mutableStateOf(MessageFieldText) }
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(innerPadding)
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {

                                text?.let {
                                    OutlinedTextField(
                                        value = it,
                                        onValueChange = {
                                            if (it.length <= 159){
                                                text = it
                                                editedMessage = text
                                                isMessageChanged = true
                                            }else{
                                                Toast.makeText(mContext, "max 160 characters", Toast.LENGTH_SHORT).show()
                                                keyboardController?.hide()
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
                contactName = MainActivity.getContactFirstName(contentResolver, number.trim { it <= ' ' })
                contactNameAndLast = MainActivity.getContactName(contentResolver, number.trim { it <= ' ' })
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
            contactName = MainActivity.getContactFirstName(contentResolver, editedphoneNumber)
            contactNameAndLast = MainActivity.getContactName(contentResolver, editedphoneNumber)
            if (contactName != null) {
                title = contactName.toString()
            } else {
                title = title.toString()
            }
        }
    }

    @SuppressLint("Range")
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
                cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI))
            if (photoUriString != null) {
                photoUri = Uri.parse(photoUriString)
            }
            cursor.close()
        }
        return photoUri
    }

    @Composable
    fun UnsavedChangesDialog(
        showDialog: Boolean,
        onDismiss: () -> Unit,
        onSave: () -> Unit
    ) {
        if (showDialog) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("Unsaved Changes") },
                text = { Text("You haven't saved your changes. Do you want to save them?") },
                confirmButton = {
                    TextButton(onClick = {
                        onSave()
                        onDismiss()
                    }) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onDismiss() }) {
                        Text("Don't Save")
                    }
                },
                properties = DialogProperties()
            )
        }
    }
    @Composable
    fun NoBackInTimeDialog(
        showNoBackInTimeDialog: Boolean,
        onDismiss: () -> Unit,
        onSave: () -> Unit
    ) {
        if (showNoBackInTimeDialog) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text(getString(R.string.sms_time_travel_titel))},
                text = { Text(getString(R.string.sms_time_travel_Text))},
                confirmButton = {
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                        onSave()
                    }) {
                        Text(getString(R.string.text_ok))
                    }
                },
                dismissButton = {},
                properties = DialogProperties()
            )
        }
    }
        }



