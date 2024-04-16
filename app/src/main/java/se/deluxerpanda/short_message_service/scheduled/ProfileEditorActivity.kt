package se.deluxerpanda.short_message_service.scheduled

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import se.deluxerpanda.short_message_service.R
import se.deluxerpanda.short_message_service.ui.theme.Short_Message_ServiceTheme
import java.util.Calendar
import java.util.Date

private var isTimeAndDateField: Boolean = false
private var isTimeAndDateChanged: Boolean = false
private var timeAndDate: String? = null
private var editedtimeAndDate: String? = null


public var isPhoneNumberField: Boolean = false
private var  isPhoneNumberChanged: Boolean = false
private var phoneNumber: String? = null
private var editedphoneNumber: String? = null
private var editedphoneNumberNew: String? = null


public var  isMessageField: Boolean = false
private var  isMessageChanged: Boolean = false
private var message: String? = null
private var editedMessage: String? = null



class ProfileEditorActivity  : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        if (intent != null) {
            timeAndDate = intent.getStringExtra("EXTRA_HISTORY_PROFILE_TIMEANDDATE")
            phoneNumber = intent.getStringExtra("EXTRA_HISTORY_PROFILE_EDITOR_PHONENUMBER")
            message = intent.getStringExtra("EXTRA_HISTORY_PROFILE_EDITOR_MESSAGE")

            if (timeAndDate != null){
                editedtimeAndDate = timeAndDate
                isPhoneNumberField = false
                isTimeAndDateField = true
                isMessageField = false

            }else if (phoneNumber != null){
                editedphoneNumber = phoneNumber
                editedphoneNumberNew = phoneNumber
                isPhoneNumberField = true
                isTimeAndDateField = false
                isMessageField = false

            }else if (message != null){
                editedMessage = message
                isPhoneNumberField = false
                isTimeAndDateField = false
                isMessageField = true
                }
        }

        setContent {
            Short_Message_ServiceTheme {
                val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
                val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
                Scaffold(
                    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),

                    topBar = {
                        CenterAlignedTopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.primary,
                            ),
                            title = {
                                if (isTimeAndDateField){
                                    Text(
                                        stringResource(id = R.string.history_info_Profile_Edit_TimeAndDate_name),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }else if (isPhoneNumberField){
                                    Text(
                                        stringResource(id = R.string.history_info_Profile_Edit_Phone_number_name),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }else if (isMessageField){
                                        Text(
                                            stringResource(id = R.string.history_info_Profile_Edit_Message_name),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }


                            },
                            navigationIcon = {
                                IconButton(onClick = {
                                    if (isTimeAndDateField){
                                        val resultIntent = Intent()
                                        resultIntent.putExtra("EXTRA_HISTORY_PROFILE_EDITOR_FINAL", editedtimeAndDate)
                                        setResult(Activity.RESULT_OK, resultIntent)
                                        finish()
                                    }else if (isPhoneNumberField){
                                        val resultIntent = Intent()
                                        if (editedphoneNumber!!.contains(",")) {
                                            editedphoneNumberNew = editedphoneNumber!!.replace(",", "\n")
                                        } else {
                                            editedphoneNumberNew = editedphoneNumber
                                        }
                                        resultIntent.putExtra("EXTRA_HISTORY_PROFILE_EDITOR_FINAL", editedphoneNumberNew)
                                        setResult(Activity.RESULT_OK, resultIntent)
                                        finish()
                                    }else if (isMessageField){
                                        val resultIntent = Intent()
                                        resultIntent.putExtra("EXTRA_HISTORY_PROFILE_EDITOR_FINAL", editedMessage)
                                        setResult(Activity.RESULT_OK, resultIntent)
                                        finish()
                                    }
                                    onBackPressedDispatcher?.onBackPressed()
                                }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_arrow_back),
                                        contentDescription = "Localized description"
                                    )
                                }
                            },
                            scrollBehavior = scrollBehavior,
                        )
                    },
                ) { innerPadding ->
                    if (isTimeAndDateField){
                        TimeAndDateEditBox(innerPadding)
                    }else if (isPhoneNumberField){
                        PhoneNumberEditBox(innerPadding)
                    }else if (isMessageField){
                        MessageEditBox(innerPadding)
                    }
                    }
                }
            }
        }
    }
@Composable
fun TimeAndDateEditBox(innerPadding: PaddingValues) {
    // Fetching local context
    val mContext = LocalContext.current
    val Time = timeAndDate!!.substringAfterLast("|")

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

        // Value for storing time as a string
        val mTime = remember { mutableStateOf("") }

        // Creating a TimePicker dialog
        val mTimePickerDialog = TimePickerDialog(
            mContext,
            { _, hour: Int, minute: Int ->
                mTime.value = "$hour:$minute"
            }, mHour, mMinute, true
        )

        Text(text = "Time")
        OutlinedButton(onClick = {
            mTimePickerDialog.show()
        }) {
            Text(text = Time)
        }

        // Initializing a Calendar
        val mCalendar = Calendar.getInstance()

        // Fetching current year, month, and day
        val mYear = mCalendar.get(Calendar.YEAR)
        val mMonth = mCalendar.get(Calendar.MONTH)
        val mDay = mCalendar.get(Calendar.DAY_OF_MONTH)

        // Value for storing date as a string
        val mDate = remember { mutableStateOf("") }

        // Creating a DatePickerDialog
        val mDatePickerDialog = DatePickerDialog(
            mContext,
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                mDate.value = "$dayOfMonth/${month + 1}/$year"
            }, mYear, mMonth, mDay
        )

        Text(text = "Date")
        OutlinedButton(onClick = {
            mDatePickerDialog.show()
        }) {
            Text(text = timeAndDate!!.substringBeforeLast("|"))
        }
    }

    if (!isTimeAndDateChanged) {
        editedtimeAndDate = timeAndDate
    }
}


@Composable
fun PhoneNumberEditBox(innerPadding: PaddingValues) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var text by remember { mutableStateOf(phoneNumber) }

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
                    text = it
                    editedphoneNumber = text
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
                        stringResource(id = R.string.text_hint_phone_number),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                    )
                }
            )
        }
    }
    if (!isPhoneNumberChanged){
        editedphoneNumber = phoneNumber
    }
}

@Composable
fun MessageEditBox(innerPadding: PaddingValues) {
    val mContext = LocalContext.current

    val keyboardController = LocalSoftwareKeyboardController.current

    var text by remember { mutableStateOf(message) }
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
                            modifier = Modifier
                        )
                    }
                )
            }
    }
    if (!isMessageChanged){
        editedMessage = message
    }
}








