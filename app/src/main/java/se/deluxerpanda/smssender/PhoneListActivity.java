package se.deluxerpanda.smssender;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
        TextBox_button.setText(getResources().getString(R.string.text_ask_give_permission));
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



    private void loadContacts() {
        List<Map<String, String>> groupData = new ArrayList<>();
        List<List<Map<String, String>>> childData = new ArrayList<>();

        Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()) {
            String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

            // Check if the contact has at least one phone number
            if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                Map<String, String> curGroupMap = new HashMap<>();
                groupData.add(curGroupMap);
                curGroupMap.put("NAME", name);

                List<Map<String, String>> children = new ArrayList<>();

                Cursor phoneCursor = getContentResolver().query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        new String[]{contactId}, null);

                if (phoneCursor.getCount() == 1) {
                    if (phoneCursor.moveToFirst()) {
                        // If there's only one phone number, save it
                        String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        curGroupMap.put("PHONE", phoneNumber);
                    }
                }

                while (phoneCursor.moveToNext()) {
                    String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    Map<String, String> curChildMap = new HashMap<>();
                    children.add(curChildMap);
                    curChildMap.put("PHONE", phoneNumber);
                }
                childData.add(children);
                phoneCursor.close();

            }
        }


        cursor.close();

        SimpleExpandableListAdapter adapter = new SimpleExpandableListAdapter(
                this,
                groupData,
                android.R.layout.simple_expandable_list_item_1,
                new String[]{"NAME"},
                new int[]{android.R.id.text1},
                childData,
                android.R.layout.simple_expandable_list_item_2,
                new String[]{"PHONE"},
                new int[]{android.R.id.text1}
        );

        contactListView.setAdapter(adapter);
        if (contactListView.getAdapter().isEmpty()){
            ExpandableListView Phone_list = findViewById(R.id.Phone_list);
            TextView TextBox_text = findViewById(R.id.Phone_list_TextBox_text);
            Button TextBox_button = findViewById(R.id.Phone_list_TextBox_button);

            Phone_list.setVisibility(View.GONE);

            TextBox_text.setVisibility(View.VISIBLE);
            TextBox_text.setText(getResources().getString(R.string.No_contacts_found));
            TextBox_text.setGravity(Gravity.CENTER);

            TextBox_button.setVisibility(View.VISIBLE);
            TextBox_button.setText(getResources().getString(R.string.text_ask_give_permission));
            TextBox_button.setGravity(Gravity.CENTER);
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
                }
                return false;
            }
        });
    }

    private EditText phoneNumberEditText;
    public void setPhoneNumber(String phoneNumber){
        Intent resultIntent = new Intent();
        resultIntent.putExtra("PHONE_NUMBER_FROM_CONTACTS", phoneNumber);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

}
