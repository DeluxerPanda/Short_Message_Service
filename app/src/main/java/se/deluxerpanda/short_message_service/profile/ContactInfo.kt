package se.deluxerpanda.short_message_service.profile

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.content.ContextCompat

class ContactInfo {


    companion object {
        fun getContactName(
            contentResolver: ContentResolver,
            phoneNumber: String?,
            context: Context
        ): String? {
            val permissionCheck =
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                return null
            }
            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNumber)
            )
            val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)

            contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    // Contact exists, return the name
                    val contactName =
                        cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME))
                    return contactName
                }
            }
            // Contact doesn't exist
            return null
        }

        fun getContactFirstName(
            contentResolver: ContentResolver,
            phoneNumber: String?,
            context: Context
        ): String? {
            val permissionCheck =
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                return null
            }
            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNumber)
            )
            val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)

            contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    // Contact exists, extract the first name from the display name
                    val contactName =
                        cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME))
                    val parts =
                        contactName.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray() // Split by whitespace
                    return parts[0] // Return the first part
                }
            }
            // Contact doesn't exist
            return null
        }
        fun processPhoneNumbers(detailsNumber: String?, contentResolver: ContentResolver, context: Context): String {
            val phoneNumbers = detailsNumber?.split(",") ?: emptyList()
            val titleBuilder = StringBuilder()
            for (number in phoneNumbers) {

                val trimmedNumber = number.trim()
                val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    titleBuilder.append(trimmedNumber).append(", ")
                }
                val contactName = getContactFirstName(contentResolver, trimmedNumber, context)
                if (contactName != null) {
                    titleBuilder.append(contactName).append(", ")
                } else {
                    titleBuilder.append(trimmedNumber).append(", ")
                }
            }

            return titleBuilder.toString().removeSuffix(", ")
        }

         fun getContactPhotoUri(contactID: String?, contentResolver: ContentResolver, context: Context): Uri? {
             val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
             if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                 return null
             }
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

        @SuppressLint("Range")
        fun getContactPhotoUri2(context: Context, contactID: String?): Uri? {
            val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                return null
            }
            if (contactID == null) {
                return null
            }
            val contactUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
            val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)
            val selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + "=?"
            val selectionArgs = arrayOf(contactID)

            val cursor = context.contentResolver.query(contactUri, projection, selection, selectionArgs, null)
            var photoUri: Uri? = null

            cursor?.use {
                if (it.moveToFirst()) {
                    val photoUriString = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI))
                    if (photoUriString != null) {
                        photoUri = Uri.parse(photoUriString)
                    }
                }
            }
            return photoUri
        }


    }

}