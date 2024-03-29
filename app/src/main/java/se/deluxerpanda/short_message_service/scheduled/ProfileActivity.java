package se.deluxerpanda.short_message_service.scheduled;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import se.deluxerpanda.short_message_service.smssender.MainActivity;
import se.deluxerpanda.short_message_service.R;

public class ProfileActivity extends AppCompatActivity{

    private int alarmId;
    private String message;
    private String title;
    private String PhoneNumber;
    private String contactName;
    private String timeAndDate;
    private Uri photoUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_porfile);

        Intent intent = getIntent();
        // back button
        ImageView btnBack = findViewById(R.id.btnToMainSmsSchedulerPage);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
          finish();
            }
        });

        // Delete button
        ImageView btnDelete = findViewById(R.id.btnToDelete);
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
                Toast.makeText(ProfileActivity.this, "Coming Soon!", Toast.LENGTH_SHORT).show();
            }
        });

        PhoneNumber = intent.getStringExtra("EXTRA_HISTORY_PROFILE_PHONENUMBER");
        TextView testPhoneNumber = findViewById(R.id.contact_name);
        testPhoneNumber.setText(PhoneNumber);

        ImageView btnPhoneNumber = findViewById(R.id.btn_Edit_Contacts);
        btnPhoneNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ProfileActivity.this, "Coming Soon!", Toast.LENGTH_SHORT).show();
            }
        });



        message = intent.getStringExtra("EXTRA_HISTORY_PROFILE_MESSAGE");
        TextView Message = findViewById(R.id.Profile_History_Message);
        Message.setText(message);

        ImageView btnMessage = findViewById(R.id.btn_Edit_Message);
        btnMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ProfileActivity.this, "Coming Soon!", Toast.LENGTH_SHORT).show();
            }
        });

        }

    }
