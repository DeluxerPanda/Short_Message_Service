package se.deluxerpanda.smssender;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhoneListActivity extends AppCompatActivity {

    private LinearLayout contactContainer;
    private Spinner categorySpinner;

    private Map<String, List<String>> contactsMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_list);

        contactContainer = findViewById(R.id.contactContainer);
        categorySpinner = findViewById(R.id.categorySpinner);

        // Initialize data structures
        contactsMap = new HashMap<>();

        // Fetch contacts
        fetchContacts();

        // Populate category spinner
        populateCategorySpinner();

        // Show contacts for the default category
        showContactsForCategory(getDefaultCategory());
    }

    private void fetchContacts() {
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                List<String> contactNumbers = getContactNumbers(contactId);

                contactsMap.put(contactName, contactNumbers);
            }
            cursor.close();
        }
    }

    private List<String> getContactNumbers(String contactId) {
        List<String> numbers = new ArrayList<>();
        Cursor cursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                new String[]{contactId},
                null
        );

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                numbers.add(number);
            }
            cursor.close();
        }

        return numbers;
    }

    private void populateCategorySpinner() {
        List<String> categories = new ArrayList<>(contactsMap.keySet());
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(spinnerAdapter);

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedCategory = parentView.getItemAtPosition(position).toString();
                showContactsForCategory(selectedCategory);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing here
            }
        });
    }

    private void showContactsForCategory(String category) {
        contactContainer.removeAllViews();

        List<String> numbers = contactsMap.get(category);
        if (numbers != null) {
            for (String number : numbers) {
                // You can customize the layout as per your requirements
                View contactView = getLayoutInflater().inflate(R.layout.contact_item, null);

                TextView contactNameTextView = contactView.findViewById(R.id.contactNameTextView);
                TextView contactNumberTextView = contactView.findViewById(R.id.contactNumbersSpinner);
                ImageView contactPhotoImageView = contactView.findViewById(R.id.contactPhotoImageView);

                contactNameTextView.setText(category);
                contactNumberTextView.setText(number);
                // Set photo if available

                contactContainer.addView(contactView);
            }
        }
    }

    private String getDefaultCategory() {
        // You can modify this logic based on your requirements
        return contactsMap.keySet().iterator().next();
    }
}

