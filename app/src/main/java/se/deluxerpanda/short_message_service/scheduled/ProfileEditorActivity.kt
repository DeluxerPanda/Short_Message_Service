package se.deluxerpanda.short_message_service.scheduled

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import se.deluxerpanda.short_message_service.R
import se.deluxerpanda.short_message_service.smssender.MainActivity
import se.deluxerpanda.short_message_service.ui.theme.Short_Message_ServiceTheme

private var message: String? = null
private var editedMessage: String? = null

private var phoneNumber: String? = null

private var isTimeAndDateField: Boolean = false
private var timeAndDate: String? = null

public var isPhoneNumberField: Boolean = false
private var  isPhoneNumberChanged: Boolean = false

public var  isMessageField: Boolean = false
private var  isMessageChanged: Boolean = false



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
                isPhoneNumberField = false
                isTimeAndDateField = true
                isMessageField = false

            }else if (phoneNumber != null){
                isPhoneNumberField = true
                isTimeAndDateField = false
                isMessageField = false

            }else if (message != null){
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
                                    if (isMessageField){
                                        val resultIntent = Intent()
                                        resultIntent.putExtra("EXTRA_HISTORY_PROFILE_EDITOR_MESSAGE_FINAL", editedMessage)
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
        val keyboardController = LocalSoftwareKeyboardController.current

        var text by remember { mutableStateOf(phoneNumber) }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {}
        if (!isMessageChanged){
            editedMessage = message
        }
    }

@Composable
fun PhoneNumberEditBox(innerPadding: PaddingValues) {
    val mContext = LocalContext.current
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
                    editedMessage = text
                    isPhoneNumberChanged = true
                },

                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
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
                            Toast.makeText(mContext, "Cannot be more than 5 Characters", Toast.LENGTH_SHORT).show()
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








