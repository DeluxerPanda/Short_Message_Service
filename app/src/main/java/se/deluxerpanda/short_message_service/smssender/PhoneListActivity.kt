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
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ExpandableListView
import android.widget.ExpandableListView.OnGroupClickListener
import android.widget.ImageView
import android.widget.SimpleExpandableListAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import se.deluxerpanda.short_message_service.R

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

    private fun checkPermissionAndLoadContacts() {
        val permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            loadContacts()
        } else {
            showPermissionExplanationDialog()
        }
    }

    private fun showPermissionExplanationDialog() {
        val phoneList: ExpandableListView = findViewById(R.id.Phone_list)
        val textBoxText: TextView = findViewById(R.id.Phone_list_TextBox_text)
        val textBoxText2: TextView = findViewById(R.id.Phone_list_TextBox_text2)
        val textBoxButton: Button = findViewById(R.id.Phone_list_TextBox_button)

        phoneList.visibility = View.GONE

        textBoxText.visibility = View.VISIBLE
        textBoxText.text = getString(R.string.sms_no_permission_contacts_titel)
        textBoxText.gravity = Gravity.CENTER

        textBoxText2.visibility = View.VISIBLE
        textBoxText2.text = getString(R.string.sms_no_permission_contacts_text)
        textBoxText2.gravity = Gravity.CENTER

        textBoxButton.visibility = View.VISIBLE
        textBoxButton.text = getString(R.string.text_ask_give_permission_settings)
        textBoxButton.gravity = Gravity.CENTER
        textBoxButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
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
                val photoUri = getContactPhotoUri(groupData[groupPosition]["CONTACTID"])
                val contactImageView: ImageView = view.findViewById(R.id.group_image)
                if (photoUri != null) {
                    contactImageView.setImageURI(photoUri)

                    val drawable = contactImageView.drawable
                    if (drawable != null && drawable is BitmapDrawable) {
                        val roundedDrawable = RoundedBitmapDrawableFactory.create(resources, drawable.bitmap)
                        roundedDrawable.isCircular = true
                        contactImageView.setImageDrawable(roundedDrawable)
                    } else {
                        contactImageView.setImageResource(R.drawable.ic_baseline_person_24)
                    }
                } else {
                    contactImageView.setImageResource(R.drawable.ic_baseline_person_24)
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

    private fun getContactPhotoUri(contactID: String?): Uri? {
        val contactUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)
        val selection = "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID}=?"
        val selectionArgs = arrayOf(contactID)

        var photoUri: Uri? = null
        val cursor = contentResolver.query(contactUri, projection, selection, selectionArgs, null)
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
