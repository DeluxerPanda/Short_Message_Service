package se.deluxerpanda.short_message_service.smssender;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import se.deluxerpanda.short_message_service.R;

public class PhoneListActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private ExpandableListView contactListView;
    private int lastExpandedPosition = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_list);

        TextView TextBox_text = findViewById(R.id.Phone_list_TextBox_text);
        Button TextBox_button = findViewById(R.id.Phone_list_TextBox_button);

        TextBox_text.setVisibility(View.GONE);
        TextBox_button.setVisibility(View.GONE);
        // back button
        ImageView btnToHamburger = findViewById(R.id.btnToMainSmsSchedulerPage);
        btnToHamburger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Initialize the ExpandableListView and permission check
        this.contactListView = (ExpandableListView) findViewById(R.id.Phone_list);
        checkPermissionAndLoadContacts();
    }

    private void checkPermissionAndLoadContacts() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            loadContacts();
        } else {
            showPermissionExplanationDialog();
        }
    }


    private void showPermissionExplanationDialog() {

        ExpandableListView Phone_list = findViewById(R.id.Phone_list);
        TextView TextBox_text = findViewById(R.id.Phone_list_TextBox_text);
        TextView TextBox_text2 = findViewById(R.id.Phone_list_TextBox_text2);
        Button TextBox_button = findViewById(R.id.Phone_list_TextBox_button);

        Phone_list.setVisibility(View.GONE);

        TextBox_text.setVisibility(View.VISIBLE);
        TextBox_text.setText(getResources().getString(R.string.sms_no_permission_contacts_titel));
        TextBox_text.setGravity(Gravity.CENTER);

        TextBox_text2.setVisibility(View.VISIBLE);
        TextBox_text2.setText(getResources().getString(R.string.sms_no_permission_contacts_text));
        TextBox_text2.setGravity(Gravity.CENTER);

        TextBox_button.setVisibility(View.VISIBLE);
        TextBox_button.setText(getResources().getString(R.string.text_ask_give_permission_settings));
        TextBox_button.setGravity(Gravity.CENTER);
        TextBox_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onBackPressed();
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        });

    }

    boolean hasSinglePhoneNumber = false;
    private void loadContacts() {
        List<Map<String, String>> groupData = new ArrayList<>();
        List<List<Map<String, String>>> childData = new ArrayList<>();

        Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()) {
            String contactId = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));


            // Check if the contact has at least one phone number
            if (Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                Map<String, String> curGroupMap = new HashMap<>();
                groupData.add(curGroupMap);
                curGroupMap.put("NAME", name);
                curGroupMap.put("CONTACTID", contactId);
                List<Map<String, String>> children = new ArrayList<>();

                Cursor phoneCursor = getContentResolver().query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        new String[]{contactId}, null);

                if (phoneCursor.getCount() == 1) {
                    if (phoneCursor.moveToFirst()) {
                        // If there's only one phone number, save it
                        String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        curGroupMap.put("PHONE", phoneNumber);
                        hasSinglePhoneNumber = true;
                        curGroupMap.put("SINGLE_PHONE", String.valueOf(hasSinglePhoneNumber));

                    }
                }

                while (phoneCursor.moveToNext()) {
                    String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    Map<String, String> curChildMap = new HashMap<>();
                    children.add(curChildMap);

                    int phoneType = phoneCursor.getInt(phoneCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.TYPE));



                        CharSequence phoneTypeLabel = ContactsContract.CommonDataKinds.Phone.getTypeLabel(
                                getResources(), phoneType, getResources().getString(R.string.Custom_name));

                        String phoneTypeLabelFinl = phoneTypeLabel + ":";
                        curChildMap.put("CATAGORY",phoneTypeLabelFinl);

                    curChildMap.put("PHONE", phoneNumber);
                    hasSinglePhoneNumber = false;
                    curGroupMap.put("SINGLE_PHONE", String.valueOf(hasSinglePhoneNumber));
                }

                childData.add(children);
                phoneCursor.close();
            }
        }

        cursor.close();
        SimpleExpandableListAdapter adapter = new SimpleExpandableListAdapter(
                this,
                groupData,
                R.layout.activity_phone_group_layout, // layout for group
                new String[]{"NAME"},
                new int[]{R.id.group_name},
                childData,
                R.layout.activity_phone_item_layout, // layout for child
                new String[]{"PHONE","CATAGORY"},
                new int[]{R.id.contact_number, R.id.contact_category}
        )
        {
            @Override
            public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
                View view = super.getGroupView(groupPosition, isExpanded, convertView, parent);

                // Get the contact photo URI
                Uri photoUri = getContactPhotoUri(groupData.get(groupPosition).get("CONTACTID"));
                ImageView contactImageView = view.findViewById(R.id.group_image);
                if (photoUri != null) {
                   // Load the contact photo into the ImageView
                    contactImageView.setImageURI(photoUri);
                    // Create a rounded drawable and set it directly to the ImageView
                    RoundedBitmapDrawable roundedDrawable = RoundedBitmapDrawableFactory.create(getResources(), ((BitmapDrawable) contactImageView.getDrawable()).getBitmap());
                    roundedDrawable.setCircular(true); // Set to true if you want circular corners
                    contactImageView.setImageDrawable(roundedDrawable);

                }else {
                    contactImageView.setImageResource(R.drawable.ic_baseline_person_24);
                }

                ImageView arrowImageView1 = view.findViewById(R.id.arrow_icon1);
                ImageView arrowImageView2 = view.findViewById(R.id.arrow_icon2);
                // Check if it's a single phone number (no arrow icon) or multiple phone numbers (with arrow icon)
                if (Boolean.parseBoolean(groupData.get(groupPosition).get("SINGLE_PHONE"))) {
                    arrowImageView1.setVisibility(View.GONE);
                    arrowImageView2.setVisibility(View.GONE);
                } else {
                    arrowImageView1.setVisibility(View.VISIBLE);
                    arrowImageView2.setVisibility(View.GONE);
                if (isExpanded){
                    arrowImageView1.setVisibility(View.GONE);
                    arrowImageView2.setVisibility(View.VISIBLE);
                }
                }
                return view;
            }

        };

        contactListView.setAdapter(adapter);

        if (contactListView.getAdapter().isEmpty()){
            ExpandableListView Phone_list = findViewById(R.id.Phone_list);
            TextView TextBox_text = findViewById(R.id.Phone_list_TextBox_text);
            Button TextBox_button = findViewById(R.id.Phone_list_TextBox_button);

            Phone_list.setVisibility(View.GONE);

            TextBox_text.setVisibility(View.VISIBLE);
            TextBox_text.setText(getResources().getString(R.string.No_contacts_found));
            TextBox_text.setGravity(Gravity.CENTER);

            TextBox_button.setVisibility(View.GONE);
            TextBox_button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    onBackPressed();
                    Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                }
            });

        }

        contactListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                String phoneNumber = ((Map<String, String>) adapter.getChild(groupPosition, childPosition)).get("PHONE");
                setPhoneNumber(phoneNumber);
                return true;
            }
        });

        contactListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            int lastExpandedGroupPosition = -1;

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                ExpandableListAdapter adapter = contactListView.getExpandableListAdapter();
                int childCount = adapter.getChildrenCount(groupPosition);

                if (childCount == 0) {
                    String phoneNumber = ((Map<String, String>) adapter.getGroup(groupPosition)).get("PHONE");
                    if (phoneNumber != null) {
                        setPhoneNumber(phoneNumber);
                    } else {
                        Toast.makeText(PhoneListActivity.this, "No phone number available for this contact", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                } else {

                    if (contactListView.isGroupExpanded(groupPosition)) {
                        contactListView.collapseGroup(groupPosition);
                        lastExpandedGroupPosition = -1;
                    } else {
                        if (lastExpandedGroupPosition != -1) {
                            contactListView.collapseGroup(lastExpandedGroupPosition);
                        }
                        contactListView.expandGroup(groupPosition);
                        lastExpandedGroupPosition = groupPosition;
                    }
                    return true;
                }
            }
        });


    }

    public void setPhoneNumber(String phoneNumber){
        Intent resultIntent = new Intent();
        resultIntent.putExtra("PHONE_NUMBER_FROM_CONTACTS", phoneNumber);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }


    private Uri getContactPhotoUri(String contactID) {
        Uri contactUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = {ContactsContract.CommonDataKinds.Phone.PHOTO_URI};
        String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?";
        String[] selectionArgs = {contactID};

        Cursor cursor = getContentResolver().query(contactUri, projection, selection, selectionArgs, null);
        Uri photoUri = null;

        if (cursor != null && cursor.moveToFirst()) {
            String photoUriString = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
            if (photoUriString != null) {
                photoUri = Uri.parse(photoUriString);
            }
            cursor.close();
        }
        return photoUri;
    }


}
