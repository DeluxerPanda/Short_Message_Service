package se.deluxerpanda.short_message_service.scheduled;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import se.deluxerpanda.short_message_service.smssender.MainActivity;
import se.deluxerpanda.short_message_service.R;

public class ProfileActivity extends AppCompatActivity{

    private int alarmId;
    private String message;
    private String title;
    private String phoneNumber;
    private String phoneNumberNew;
    private String contactName;
    private String timeAndDate;
    private Uri photoUri;
    private int EditTextID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_porfile);

        Intent intent = getIntent();
        // back button
        ImageView btnToMainSmsSchedulerPage = findViewById(R.id.btnToMainSmsSchedulerPage);
        btnToMainSmsSchedulerPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
          finish();
            }
        });

        //Send now button
        ImageView btnToSendScheduledMessageNow = findViewById(R.id.btnToSendScheduledMessageNow);
        btnToSendScheduledMessageNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ProfileActivity.this, "Coming Soon!", Toast.LENGTH_SHORT).show();
            }
        });

        //Delay button
        ImageView btnToDelayScheduledMessage = findViewById(R.id.btnToDelayScheduledMessage);
        btnToDelayScheduledMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ProfileActivity.this, "Coming Soon!", Toast.LENGTH_SHORT).show();
            }
        });

        // Delete button
        ImageView btnDelete = findViewById(R.id.btnToDeleteScheduledMessage);
        MainActivity mainActivity = new MainActivity();
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.deleteAlarm(alarmId, ProfileActivity.this);
                finish();
            }
        });




        String photoUriString = intent.getStringExtra("EXTRA_HISTORY_PROFILE_POTOURL");
        ImageView contactImageView = findViewById(R.id.Profile_History_group_image);
        if (photoUriString != null) {
            photoUri = Uri.parse(photoUriString);
            contactImageView.setImageURI(photoUri);
        }else {
            contactImageView.setImageResource(R.drawable.ic_baseline_person_24);
        }

        alarmId = intent.getIntExtra("EXTRA_HISTORY_PROFILE_ALARMID", 0);
        TextView testID = findViewById(R.id.Profile_History_ID_Title);
        testID.setText(String.valueOf("ID: "+alarmId));

        title = intent.getStringExtra("EXTRA_HISTORY_PROFILE_TITLE");
        TextView testTitle = findViewById(R.id.Profile_History_group_name);
        testTitle.setText(title);

        timeAndDate = intent.getStringExtra("EXTRA_HISTORY_PROFILE_TIMEANDDATE");
        TextView TimeAndDate = findViewById(R.id.history_info_date_and_time);
        TimeAndDate.setText(timeAndDate);

        ImageView btnTimeAndDate = findViewById(R.id.btn_Edit_Date);
        btnTimeAndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditTextID = TimeAndDate.getId();
                Intent intent = new Intent(ProfileActivity.this, ProfileEditorActivity.class);
                intent.putExtra("EXTRA_HISTORY_PROFILE_TIMEANDDATE", TimeAndDate.getText());
                ProfileEditorActivityLauncher.launch(intent);
            }
        });

        phoneNumber = intent.getStringExtra("EXTRA_HISTORY_PROFILE_PHONENUMBER");
        TextView PhoneNumber = findViewById(R.id.contact_name);
        if (phoneNumber.contains(",")) {
            phoneNumberNew = phoneNumber.replace(",", "\n");
        }else {
            phoneNumberNew = phoneNumber;
        }

        PhoneNumber.setText(phoneNumberNew);
        ImageView btnPhoneNumber = findViewById(R.id.btn_Edit_Contacts);
        btnPhoneNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditTextID = PhoneNumber.getId();
                Intent intent = new Intent(ProfileActivity.this, ProfileEditorActivity.class);
                intent.putExtra("EXTRA_HISTORY_PROFILE_EDITOR_PHONENUMBER", PhoneNumber.getText());
                ProfileEditorActivityLauncher.launch(intent);
            }
        });

        message = intent.getStringExtra("EXTRA_HISTORY_PROFILE_MESSAGE");
        TextView Message = findViewById(R.id.Profile_History_Message);
        Message.setText(message);

        ImageView btnMessage = findViewById(R.id.btn_Edit_Message);
        btnMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditTextID = Message.getId();
                Intent intent = new Intent(ProfileActivity.this, ProfileEditorActivity.class);
                intent.putExtra("EXTRA_HISTORY_PROFILE_EDITOR_MESSAGE", Message.getText());
                ProfileEditorActivityLauncher.launch(intent);
            }
        });
        }




    private ActivityResultLauncher<Intent> ProfileEditorActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        TextView textView = findViewById(EditTextID);

                        if (textView != null && textView instanceof TextView) {
                            Intent resultIntent = result.getData();
                            String data = resultIntent.getStringExtra("EXTRA_HISTORY_PROFILE_EDITOR_FINAL");
                            textView.setText(data);

                            String dataTitel = resultIntent.getStringExtra("EXTRA_HISTORY_PROFILE_EDITOR_FINAL_TITLE");
                            String data_contactNameAndLast = resultIntent.getStringExtra("EXTRA_HISTORY_PROFILE_EDITOR_FIRST_AND_LAST_NAME");

                            if (dataTitel != null){
                                TextView textTitle = findViewById(R.id.Profile_History_group_name);
                                textTitle.setText(dataTitel);
                            }
                            String photoUri_result = null;
                            Uri photoUri = getContactPhotoUri(data_contactNameAndLast);

                            ImageView contactImageView = findViewById(R.id.Profile_History_group_image);
                            if (photoUri != null) {
                                // Load the contact photo into the ImageView
                                contactImageView.setImageURI(photoUri);
                                photoUri_result = photoUri.toString();
                            } else {
                                contactImageView.setImageResource(R.drawable.ic_baseline_person_24);
                                photoUri_result = null;
                            }
                        }
                    }
                }
            });
    public Uri getContactPhotoUri(String contactID) {
        if (contactID == null) {
            return null;
        }
        Uri contactUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = {ContactsContract.CommonDataKinds.Phone.PHOTO_URI};
        String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + "=?";
        String[] selectionArgs = {contactID};

        Cursor cursor = getContentResolver().query(contactUri, projection, selection, selectionArgs, null);
        Uri photoUri = null;

        if (cursor != null && cursor.moveToFirst()) {
            String photoUriString = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
            if (photoUriString != null) {
                photoUri = Uri.parse(photoUriString);
            }
            cursor.close();
        }
        return photoUri;
    }
}

