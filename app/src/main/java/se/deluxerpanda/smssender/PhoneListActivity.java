package se.deluxerpanda.smssender;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhoneListActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private ExpandableListView contactListView;
    private int lastExpandedPosition = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_list);

        // back button
        ImageView btnToHamburger = findViewById(R.id.btnToMainSmsSchedulerPage);
        btnToHamburger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
//
        this.contactListView = (ExpandableListView) findViewById(R.id.spinner);

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            loadContacts();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
        }
        contactListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                if (lastExpandedPosition != -1 && groupPosition != lastExpandedPosition) {
                    contactListView.collapseGroup(lastExpandedPosition);
                }
                lastExpandedPosition = groupPosition;
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadContacts();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            }
        }
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

        contactListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                String phoneNumber = ((Map<String, String>) adapter.getChild(groupPosition, childPosition)).get("PHONE");
                    Toast.makeText(PhoneListActivity.this, phoneNumber+ " in Work", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(PhoneListActivity.this, "Phone number: " + phoneNumber, Toast.LENGTH_SHORT).show();
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
    public void setPhoneNumber(String phoneNumber){
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("PHONE_NUMBER_FROM_CONTACTS", phoneNumber);
        startActivity(intent);
    }
}
