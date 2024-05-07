package se.deluxerpanda.short_message_service.scheduled;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.StringTokenizer;

import se.deluxerpanda.short_message_service.profile.ProfileActivity;
import se.deluxerpanda.short_message_service.smssender.MainActivity;
import se.deluxerpanda.short_message_service.R;

public class ScheduledList extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scheduled_list_layout);
        ScheduledSMSList(ScheduledList.this);
        Intent intent = getIntent();
        // back button
        ImageView btnBack = findViewById(R.id.btnToMainSmsSchedulerPage);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    public void ScheduledSMSList(Context context){
        LinearLayout parentLayout = findViewById(R.id.app_backgrund);
        LinearLayout linearLayout = (LinearLayout) parentLayout;
        linearLayout.destroyDrawingCache();
        linearLayout.removeAllViews();

        List<MainActivity.AlarmDetails> alarmList = MainActivity.getAllAlarms(this);

        if (alarmList.isEmpty()) {
            TextView AlarmListIsEmptyTextView = new TextView(this);


            // Add your dynamic TextView here
            AlarmListIsEmptyTextView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));

            AlarmListIsEmptyTextView.setText(getResources().getString(R.string.history_info_no_SMS_scheduled));
            AlarmListIsEmptyTextView.setTextSize(20);
            AlarmListIsEmptyTextView.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD_ITALIC));
            AlarmListIsEmptyTextView.setGravity(Gravity.CENTER);
            linearLayout.addView(AlarmListIsEmptyTextView);
        } else {
            // Now you can use the alarmList as needed
            for (MainActivity.AlarmDetails alarmDetails : alarmList) {

                int alarmId = alarmDetails.getAlarmId();

                // long timeInMillis = alarmDetails.getTimeInMillis();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat sdf2 = new SimpleDateFormat("H:mm");


                // Format the date and print the result
                String formattedDateStart = sdf.format(alarmDetails.getTimeInMillis());
                String formattedClockTime = sdf2.format(alarmDetails.getTimeInMillis());
                String getRepeatSmS = alarmDetails.getRepeatSmS();
                String phonenumber = alarmDetails.getPhonenumber();
                String message = alarmDetails.getMessage();
                View dynamicTextViewLayout = getLayoutInflater().inflate(R.layout.history_info, null);

                LinearLayout dynamicLinearLayout = dynamicTextViewLayout.findViewById(R.id.history_info_page);

                TextView history_info_contact_name_TextView = dynamicTextViewLayout.findViewById(R.id.history_info_contact_name);

                String title;
                String inputString = phonenumber;
                String contactName = null;
                String photoUri_result = null;
                StringBuilder concatenatedNames = new StringBuilder();
                int permissionCheckContacts = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
                ImageView contactImageView = dynamicTextViewLayout.findViewById(R.id.history_info_contact_profile);
                if (permissionCheckContacts == PackageManager.PERMISSION_GRANTED){
                    ContentResolver contentResolver = getContentResolver();

                    if (phonenumber.contains(",")){
// Creating a StringTokenizer object with delimiter ","
                        StringTokenizer tokenizer = new StringTokenizer(inputString, ",");

                        int tokenCount = tokenizer.countTokens();
                        String[] stringArray = new String[tokenCount];

// Converting each token to array elements
                        for (int i = 0; i < tokenCount; i++) {
                            stringArray[i] = tokenizer.nextToken();
                        }

// Printing the output array

                        for (String element : stringArray) {
                            contactName = MainActivity.getContactName(contentResolver, element);
                            concatenatedNames.append(contactName).append(", ");
                        }
                        history_info_contact_name_TextView.setText(concatenatedNames);
                        title = String.valueOf(concatenatedNames);
                    }else {


                        contactName = MainActivity.getContactName(contentResolver, phonenumber);
                        if (contactName != null) {
                            history_info_contact_name_TextView.setText(contactName);
                            title = String.valueOf(contactName);
                        } else {
                            history_info_contact_name_TextView.setText(phonenumber);
                            title = String.valueOf(phonenumber);
                        }
                    }

                } else {
                    history_info_contact_name_TextView.setText(phonenumber);
                    title = String.valueOf(phonenumber);
                }

                Uri photoUri = getContactPhotoUri(contactName);
                if (photoUri != null) {
                    // Load the contact photo into the ImageView
                    contactImageView.setImageURI(photoUri);
                    // Create a rounded drawable and set it directly to the ImageView
                    RoundedBitmapDrawable roundedDrawable = RoundedBitmapDrawableFactory.create(getResources(), ((BitmapDrawable) contactImageView.getDrawable()).getBitmap());
                    roundedDrawable.setCircular(true); // Set to true if you want circular corners
                    contactImageView.setImageDrawable(roundedDrawable);
                    photoUri_result = photoUri.toString();

                }else {
                    contactImageView.setImageResource(R.drawable.ic_baseline_person_24);
                    photoUri_result = null;
                }

                String[] words = phonenumber.split(",");

                StringBuilder output = new StringBuilder();
                for (String word : words) {
                    output.append(word.trim()).append("\n");
                }

                String phonenumber_result = output.toString();





                TextView history_info_message_TextView = dynamicTextViewLayout.findViewById(R.id.history_info_message);
                history_info_message_TextView.setText(message);

                TextView history_info_date_and_time_TextView = dynamicTextViewLayout.findViewById(R.id.history_info_date_and_time);

                String TimeAndDate = formattedDateStart +" | "+ formattedClockTime;

                history_info_date_and_time_TextView.setText(getResources().getString(R.string.history_info_Date_name) +" "+ formattedDateStart
                        + ", "+getResources().getString(R.string.history_info_Time_name)  +" "+ formattedClockTime);

                linearLayout.addView(dynamicTextViewLayout);
                String finalPhotoUri_result;

                if (photoUri_result == null){
                    finalPhotoUri_result = null;
                }else {
                    finalPhotoUri_result = photoUri_result;
                }

                dynamicLinearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent(ScheduledList.this, ProfileActivity.class);
                        intent.putExtra("EXTRA_HISTORY_PROFILE_ALARMID", alarmId);
                        intent.putExtra("EXTRA_HISTORY_PROFILE_POTOURL", finalPhotoUri_result);
                        intent.putExtra("EXTRA_HISTORY_PROFILE_TITLE", title);
                        intent.putExtra("EXTRA_HISTORY_PROFILE_TIMEANDDATE", TimeAndDate);
                        intent.putExtra("EXTRA_HISTORY_PROFILE_PHONENUMBER", phonenumber_result);
                        intent.putExtra("EXTRA_HISTORY_PROFILE_MESSAGE", message);

                        startActivity(intent);
                    }

                });
            }
        }
    }
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
            String photoUriString = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
            if (photoUriString != null) {
                photoUri = Uri.parse(photoUriString);
            }
            cursor.close();
        }
        return photoUri;
    }
}
