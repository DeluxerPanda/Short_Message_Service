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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import se.deluxerpanda.short_message_service.ui.theme.AppTheme
import se.deluxerpanda.short_message_service.R
import se.deluxerpanda.short_message_service.smssender.MainActivity


private var isTimeAndDateField: Boolean = false
private var isTimeAndDateChanged: Boolean = false
private var timeAndDate: String? = null
private var editedtimeAndDate: String? = null


private var isPhoneNumberField: Boolean = false
private var  isPhoneNumberChanged: Boolean = false
private var phoneNumber: String? = null
private var phoneNumberNew: String? = null
private var editedphoneNumber: String? = null
private var editedphoneNumberNew: String? = null


private var  isMessageField: Boolean = false
private var  isMessageChanged: Boolean = false
private var message: String? = null
private var editedMessage: String? = null

private var contactName: String? = null
private var contactNameAndLast: String? = null


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
            AppTheme {
                val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
                val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
                Scaffold(
                    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),

                    topBar = {
                        CenterAlignedTopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
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
                                        setResult(RESULT_OK, resultIntent)
                                        finish()
                                    }else if (isPhoneNumberField){
                                        it_isPhoneNumberField()
                                    }else if (isMessageField){
                                        val resultIntent = Intent()
                                        resultIntent.putExtra("EXTRA_HISTORY_PROFILE_EDITOR_FINAL", editedMessage)
                                        setResult(RESULT_OK, resultIntent)
                                        finish()
                                    }
                                    onBackPressedDispatcher?.onBackPressed()
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
                                    if (editedtimeAndDate != timeAndDate ||
                                        editedphoneNumber != phoneNumber ||
                                        editedMessage != message
                                    ) {
                                        showDialog = true
                                    } else {
                                        onBackPressedDispatcher?.onBackPressed()
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
                                        onBackPressedDispatcher?.onBackPressed()},
                                    onSave = {
                                        if (isTimeAndDateField){
                                            val resultIntent = Intent()
                                            resultIntent.putExtra("EXTRA_HISTORY_PROFILE_EDITOR_FINAL", editedtimeAndDate)
                                            setResult(RESULT_OK, resultIntent)
                                            finish()
                                        }else if (isPhoneNumberField){
                                            it_isPhoneNumberField()
                                        }else if (isMessageField){
                                            val resultIntent = Intent()
                                            resultIntent.putExtra("EXTRA_HISTORY_PROFILE_EDITOR_FINAL", editedMessage)
                                            setResult(RESULT_OK, resultIntent)
                                            finish()
                                        }
                                        onBackPressedDispatcher?.onBackPressed()
                                        showDialog = false
                                    }
                                )
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

     fun it_isPhoneNumberField(){
        val resultIntent = Intent()

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

        resultIntent.putExtra("EXTRA_HISTORY_PROFILE_EDITOR_FINAL", editedphoneNumberNew)
        resultIntent.putExtra("EXTRA_HISTORY_PROFILE_EDITOR_FINAL_TITLE", title)
        resultIntent.putExtra("EXTRA_HISTORY_PROFILE_EDITOR_FIRST_AND_LAST_NAME", contactNameAndLast)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
    }
@Composable
fun UnsavedChangesDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { onDismiss() },
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
fun TimeAndDateEditBox(innerPadding: PaddingValues) {
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
                timeAndDate = timeAndDate!!.replaceAfterLast("|", newTime)
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

    if (!isTimeAndDateChanged) {
        editedtimeAndDate = timeAndDate
    }
}

@Composable
fun PhoneNumberEditBox(innerPadding: PaddingValues) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val mContext = LocalContext.current
    var editedPhoneNumbers by remember { mutableStateOf(phoneNumber?.split("\n") ?: listOf()) }
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
                    editedPhoneNumbers = editedPhoneNumbers.toMutableList().also { list ->
                        list[index] = it
                    }
                    editedphoneNumber = editedPhoneNumbers.joinToString(", ")
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
                            Toast.makeText(mContext, "Coming Soon! - import contacts!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_baseline_import_contacts),
                                contentDescription = "import contacts button"
                            )
                        }
                        IconButton(onClick = {
                            editedPhoneNumbers = editedPhoneNumbers.toMutableList().also { list ->
                              if  (editedPhoneNumbers.size != 1){
                                  list.removeAt(index)
                              }else{
                                  Toast.makeText(mContext, R.string.history_info_Profile_Edit_Must_have_one_number, Toast.LENGTH_SHORT).show()
                              }
                                editedphoneNumber = editedPhoneNumbers.joinToString(", ")
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
            editedPhoneNumbers = editedPhoneNumbers.toMutableList().also { list ->
                list.add("")
                editedphoneNumber = editedPhoneNumbers.joinToString(", ")
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

@Composable
fun  MessageEditBox(innerPadding: PaddingValues) {
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
                        )
                    }
                )
            }
    }
    if (!isMessageChanged){
        editedMessage = message
    }
}




