package se.deluxerpanda.short_message_service.smssender

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Settings
import android.util.Log
import android.widget.ExpandableListView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import se.deluxerpanda.short_message_service.R
import se.deluxerpanda.short_message_service.profile.ContactInfo
import se.deluxerpanda.short_message_service.ui.theme.AppTheme


@OptIn(ExperimentalMaterial3Api::class)
class PhoneListActivity : ComponentActivity() {
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                loadContacts()
            } else {
                setContent {
                    AppTheme {
                        Scaffold(
                            topBar = {
                                CenterAlignedTopAppBar(
                                    title = { Text(stringResource(R.string.app_contacts_name)) },
                                    navigationIcon = {
                                        IconButton(onClick = { finish() }) {
                                            Icon(painter = painterResource(id = R.drawable.ic_arrow_back), contentDescription = "back button")
                                        }
                                    }
                                )
                            }
                        ) { innerPadding ->
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.sms_no_permission_contacts_titel),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = stringResource(R.string.sms_no_permission_contacts_text),
                                    fontSize = 15.sp,
                                    textAlign = TextAlign.Center,
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = {
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.fromParts("package", packageName, null)
                                    }
                                    startActivity(intent)
                                }) {
                                    Text(
                                        text = stringResource(R.string.text_ask_give_permission_settings),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)) {
            PackageManager.PERMISSION_GRANTED -> loadContacts()
            else -> requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    private fun loadContacts() {
        setContent {
            AppTheme {
                ContactListScreen(fetchContacts())
            }
        }
    }

    private fun fetchContacts(): List<ContactData> {
        val contacts = mutableListOf<ContactData>()
        contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)?.use { cursor ->
            while (cursor.moveToNext()) {
                val contactId = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID))

                val name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))
                val photoUri = ContactInfo.getContactPhotoUri(contactId, contentResolver, this)
                val phoneNumbers = mutableListOf<PhoneNumber>()
                if (cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                        "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?", arrayOf(contactId), null
                    )?.use { phoneCursor ->
                        while (phoneCursor.moveToNext()) {
                            val number = phoneCursor.getString(phoneCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                            val type = ContactsContract.CommonDataKinds.Phone.getTypeLabel(
                                resources,
                                phoneCursor.getInt(phoneCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.TYPE)),
                                getString(R.string.Custom_name)
                            ).toString()
                            phoneNumbers.add(PhoneNumber(type, number))
                        }
                    }
                }
                contacts.add(ContactData(contactId, name, photoUri, phoneNumbers))
            }
        }
        return contacts
    }


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactListScreen(contacts: List<ContactData>) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.app_contacts_name)) },
                navigationIcon = {
                    IconButton(onClick = { finish() }) {
                        Icon(painter = painterResource(id = R.drawable.ic_arrow_back), contentDescription = "back button")
                    }
                }
            )
        }

    ) { innerPadding ->
        if (contacts.isNotEmpty()) {

        LazyColumn(
            contentPadding = innerPadding,
            modifier = Modifier.fillMaxSize()
        ) {

            items(contacts) { contact ->
                ContactItem(contact)
            }
            }
        }else{
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.No_contacts_found),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

    @Composable
    fun ContactItem(contact: ContactData) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)) {

            val uniquePhoneNumbers = contact.phoneNumbers
                .distinctBy { Pair(it.number, it.type) }
                .sortedBy { it.type }

            val isSinglePhoneNumber = uniquePhoneNumbers.size == 1

            Column(
                    modifier = Modifier
                        .clickable(enabled = isSinglePhoneNumber) {
                            // Handle column click if there's only one phone number
                            uniquePhoneNumbers
                                .firstOrNull()
                                ?.let { phone ->
                                    val resultIntent = Intent().apply {
                                        putExtra("PHONE_NUMBER_FROM_CONTACTS", phone.number)
                                    }
                                    setResult(Activity.RESULT_OK, resultIntent)
                                    finish()
                                }
                        }
                        .fillMaxWidth()
                ) {
                    var expanded by remember { mutableStateOf(false) }
                    if (!isSinglePhoneNumber) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier
                                .clickable { expanded = !expanded }
                                .fillMaxWidth()

                        ){
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (contact.photoUri != null) {
                                        Image(
                                            painter = rememberAsyncImagePainter(contact.photoUri),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(48.dp)
                                                .padding(end = 8.dp, top = 4.dp, bottom = 4.dp)
                                                .clip(RoundedCornerShape(50.dp))
                                        )
                                    } else {
                                        Image(
                                            painter = painterResource(id = R.drawable.baseline_person),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(48.dp)
                                                .padding(end = 8.dp, top = 4.dp, bottom = 4.dp)
                                                .clip(RoundedCornerShape(50.dp))
                                        )
                                    }
                                }
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(contact.name+ contact.phoneNumbers.size + isSinglePhoneNumber , fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier
                                            .fillMaxWidth())
                                }

                                if (expanded) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_baseline_keyboard_arrow_up_24),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(34.dp)
                                    ) }else{
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_baseline_keyboard_arrow_down_24),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(34.dp)
                                    )
                                }
                        }
                        if (expanded) {
                            uniquePhoneNumbers.forEach { phone ->
                                Column(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .clickable {
                                                val resultIntent = Intent().apply {
                                                    putExtra("PHONE_NUMBER_FROM_CONTACTS", phone.number)
                                                }
                                                setResult(Activity.RESULT_OK, resultIntent)
                                                finish()
                                            }
                                            .fillMaxWidth(),

                                    ) {
                                        Text(
                                            text = phone.type,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        Text(
                                            text = phone.number,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                }
                            }
                    }
                    }else{
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (contact.photoUri != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(contact.photoUri),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .padding(end = 8.dp, top = 4.dp, bottom = 4.dp)
                                        .clip(RoundedCornerShape(50.dp))
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.baseline_person),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .padding(end = 8.dp, top = 4.dp, bottom = 4.dp)
                                        .clip(RoundedCornerShape(50.dp))
                                )
                            }
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp),
                            ) {
                                Text(contact.name + contact.phoneNumbers.size + isSinglePhoneNumber, fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .fillMaxWidth())
                            }
                    }
                    }
                }
            }
        }

    data class ContactData(
    val id: String,
    val name: String,
    val photoUri: Uri?,
    val phoneNumbers: List<PhoneNumber>
)

data class PhoneNumber(
    val type: String,
    val number: String
)
}