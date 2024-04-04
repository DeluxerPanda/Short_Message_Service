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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import se.deluxerpanda.short_message_service.R
import se.deluxerpanda.short_message_service.ui.theme.Short_Message_ServiceTheme

private var message: String? = null
private var editedMessage: String? = null
private var  IsMessageField: Boolean = true
private var  IsDateField: Boolean = false
class ProfileEditorActivity  : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
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
                                Text(
                                    stringResource(id = R.string.history_info_Profile_Edit_Message_name),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            navigationIcon = {
                                IconButton(onClick = {
                                    if (IsMessageField){
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
                    if (IsMessageField){
                        MessageEditBox(innerPadding,intent)
                    }else if (IsDateField){
                        //       DateField(innerPadding)
                    }

                }
            }
        }
    }

}
@Composable
fun MessageEditBox(innerPadding: PaddingValues,intent: Intent?) {
    val mContext = LocalContext.current
//    val alarmList: List<MainActivity.AlarmDetails> = MainActivity.getAllAlarms(mContext)
    val keyboardController = LocalSoftwareKeyboardController.current
    if (intent != null) {
        message = intent.getStringExtra("EXTRA_HISTORY_PROFILE_EDITOR_MESSAGE")
    }else{
        message = ""
    }
    var text by remember { mutableStateOf(message) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(innerPadding)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
    //    for (alarmDetails: MainActivity.AlarmDetails in alarmList) {
            text?.let {
                OutlinedTextField(
                    value = it,
                    onValueChange = {
                        if (it.length <= 159){
                            text = it
                            editedMessage = text
                        }else{
                            Toast.makeText(mContext, "Cannot be more than 5 Characters", Toast.LENGTH_SHORT).show()
                            keyboardController?.hide()
                        }
                         },
                    label = {
                        Text(
                            stringResource(id = R.string.history_info_Message_name),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                        )
                    }
                )
            }
     //   }
    }
}








