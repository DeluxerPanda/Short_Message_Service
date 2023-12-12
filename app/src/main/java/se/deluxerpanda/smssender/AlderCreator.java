package se.deluxerpanda.smssender;
import android.content.Context;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;

public class AlderCreator {
    public static void showAlertBox_only_ok(Context context, String title, String message) {
        // Create an AlertDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // Set the title and message for the dialog
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Handle the "OK" button click if needed
                        dialog.dismiss(); // Dismiss the dialog
                    }
                });

        // Create and show the dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

}
