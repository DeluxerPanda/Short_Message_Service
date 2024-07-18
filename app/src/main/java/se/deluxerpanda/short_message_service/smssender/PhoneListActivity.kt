package se.deluxerpanda.short_message_service.smssender

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ExpandableListView
import android.widget.ImageView
import android.widget.SimpleExpandableListAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import se.deluxerpanda.short_message_service.R
import se.deluxerpanda.short_message_service.profile.ContactInfo
import se.deluxerpanda.short_message_service.ui.theme.AppTheme

class PhoneListActivity : AppCompatActivity() {
    private lateinit var contactListView: ExpandableListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_list)

        val textBoxText: TextView = findViewById(R.id.Phone_list_TextBox_text)
        val textBoxButton: Button = findViewById(R.id.Phone_list_TextBox_button)

        textBoxText.visibility = View.GONE
        textBoxButton.visibility = View.GONE

        // back button
        val btnToHamburger: ImageView = findViewById(R.id.btnToMainSmsSchedulerPage)
        btnToHamburger.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // Initialize the ExpandableListView and permission check
        contactListView = findViewById(R.id.Phone_list)

            checkPermissionAndLoadContacts()

    }


    @OptIn(ExperimentalMaterial3Api::class)
    private fun checkPermissionAndLoadContacts() {
        val permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            loadContacts()
        } else {
            setContent {
                AppTheme {
                    val scrollBehavior =
                        TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
                    Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(
                                    stringResource(id = R.string.app_contacts_name),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            navigationIcon = {
                                IconButton(onClick = {
                                    finish()
                                }
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_arrow_back),
                                        contentDescription = "back button"
                                    )
                                }
                            },
                            scrollBehavior = scrollBehavior,
                        )
                    },
                    ) { innerPadding ->
                        showPermissionExplanationDialog(innerPadding)
                }

                }
            }

        }
    }

    @Composable
    private fun showPermissionExplanationDialog(innerPadding: PaddingValues) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
                .padding(horizontal = 15.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,

        ){
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

            Button(
                onClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                },

            ) {
                Text(
                    text = stringResource(R.string.text_ask_give_permission_settings),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
    }
    }

    private fun loadContacts() {
        val groupData = mutableListOf<Map<String, String>>()
        val childData = mutableListOf<List<Map<String, String>>>()

        val cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)
        cursor?.use {
            while (it.moveToNext()) {
                val contactId = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                val name = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))

                if (it.getInt(it.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    val curGroupMap = mutableMapOf<String, String>()
                    groupData.add(curGroupMap)
                    curGroupMap["NAME"] = name
                    curGroupMap["CONTACTID"] = contactId
                    val children = mutableListOf<Map<String, String>>()

                    val phoneCursor = contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                        "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                        arrayOf(contactId), null
                    )

                    phoneCursor?.use { pc ->
                        if (pc.count == 1) {
                            if (pc.moveToFirst()) {
                                val phoneNumber = pc.getString(pc.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                                curGroupMap["PHONE"] = phoneNumber
                                curGroupMap["SINGLE_PHONE"] = true.toString()
                            }
                        }

                        while (pc.moveToNext()) {
                            val phoneNumber = pc.getString(pc.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                            val curChildMap = mutableMapOf<String, String>()
                            children.add(curChildMap)

                            val phoneType = pc.getInt(pc.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.TYPE))
                            val phoneTypeLabel = ContactsContract.CommonDataKinds.Phone.getTypeLabel(resources, phoneType, getString(R.string.Custom_name))
                            val phoneTypeLabelFinal = "$phoneTypeLabel:"
                            curChildMap["CATAGORY"] = phoneTypeLabelFinal
                            curChildMap["PHONE"] = phoneNumber
                            curGroupMap["SINGLE_PHONE"] = false.toString()
                        }
                    }
                    childData.add(children)
                }
            }
        }

        val adapter = object : SimpleExpandableListAdapter(
            this,
            groupData,
            R.layout.activity_phone_group_layout, // layout for group
            arrayOf("NAME"),
            intArrayOf(R.id.group_name),
            childData,
            R.layout.activity_phone_item_layout, // layout for child
            arrayOf("PHONE", "CATAGORY"),
            intArrayOf(R.id.contact_number, R.id.contact_category)
        ) {
            override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup): View {
                val view = super.getGroupView(groupPosition, isExpanded, convertView, parent)
                val photoUri = ContactInfo.getContactPhotoUri(groupData[groupPosition]["CONTACTID"],contentResolver, this@PhoneListActivity)
                val contactImageView: ImageView = view.findViewById(R.id.group_image)
                if (photoUri != null) {
                    contactImageView.setImageURI(photoUri)

                    val drawable = contactImageView.drawable
                    if (drawable != null && drawable is BitmapDrawable) {
                        val roundedDrawable = RoundedBitmapDrawableFactory.create(resources, drawable.bitmap)
                        roundedDrawable.isCircular = true
                        contactImageView.setImageDrawable(roundedDrawable)
                    } else {
                        contactImageView.setImageResource(R.drawable.baseline_person)
                    }
                } else {
                    contactImageView.setImageResource(R.drawable.baseline_person)
                }


                val arrowImageView1: ImageView = view.findViewById(R.id.arrow_icon1)
                val arrowImageView2: ImageView = view.findViewById(R.id.arrow_icon2)
                if (groupData[groupPosition]["SINGLE_PHONE"].toBoolean()) {
                    arrowImageView1.visibility = View.GONE
                    arrowImageView2.visibility = View.GONE
                } else {
                    arrowImageView1.visibility = View.VISIBLE
                    arrowImageView2.visibility = View.GONE
                    if (isExpanded) {
                        arrowImageView1.visibility = View.GONE
                        arrowImageView2.visibility = View.VISIBLE
                    }
                }
                return view
            }
        }

        contactListView.setAdapter(adapter)

        if (adapter.isEmpty) {
            val phoneList: ExpandableListView = findViewById(R.id.Phone_list)
            val textBoxText: TextView = findViewById(R.id.Phone_list_TextBox_text)
            val textBoxButton: Button = findViewById(R.id.Phone_list_TextBox_button)

            phoneList.visibility = View.GONE

            textBoxText.visibility = View.VISIBLE
            textBoxText.text = getString(R.string.No_contacts_found)
            textBoxText.gravity = Gravity.CENTER

            textBoxButton.visibility = View.GONE
            textBoxButton.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
                val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
        }

        contactListView.setOnChildClickListener { _, _, groupPosition, childPosition, _ ->
            val phoneNumber = (adapter.getChild(groupPosition, childPosition) as Map<*, *>)["PHONE"]
            setPhoneNumber(phoneNumber.toString())
            true
        }

        contactListView.setOnGroupClickListener(object : ExpandableListView.OnGroupClickListener {
            var lastExpandedGroupPosition = -1

            override fun onGroupClick(parent: ExpandableListView, v: View, groupPosition: Int, id: Long): Boolean {
                val childCount = contactListView.expandableListAdapter.getChildrenCount(groupPosition)
                if (childCount == 0) {
                    val phoneNumber = (adapter.getGroup(groupPosition) as? Map<*, *>)?.get("PHONE")

                    if (phoneNumber != null) {
                        setPhoneNumber(phoneNumber.toString())
                    } else {
                        Toast.makeText(this@PhoneListActivity, "No phone number available for this contact", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    if (contactListView.isGroupExpanded(groupPosition)) {
                        contactListView.collapseGroup(groupPosition)
                        lastExpandedGroupPosition = -1
                    } else {
                        if (lastExpandedGroupPosition != -1) {
                            contactListView.collapseGroup(lastExpandedGroupPosition)
                        }
                        contactListView.expandGroup(groupPosition)
                        lastExpandedGroupPosition = groupPosition
                    }
                }
                return true
            }
        })
    }

    private fun setPhoneNumber(phoneNumber: String?) {
        val resultIntent = Intent().apply {
            putExtra("PHONE_NUMBER_FROM_CONTACTS", phoneNumber)
        }
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }


}
