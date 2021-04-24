package com.example.g6_findly.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.g6_findly.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Settings extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        hideActionBar();
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
    }

    public void hideActionBar() {
        androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }

    public void deleteAccount(View v){
        new AlertDialog.Builder(this)
                .setTitle("Delete account")
                .setMessage("Are you sure you want to delete your account?")

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton("Delete Account", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Continue with delete operation
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                        user.delete()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d("TAG", "User account deleted.");
                                            Toast.makeText(Settings.this, "Account deleted.", Toast.LENGTH_LONG).show();
                                            mAuth.signOut();
                                            Intent i = new Intent(Settings.this, MainActivity.class);
                                            startActivity(i);
                                        }else{
                                            Toast.makeText(Settings.this, "Error deleting account. Try again later.", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                    }
                })

                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
    public void navigateBack(View v){
      finish();
    }

    public void logOut(View v){
        mAuth.signOut();
        Intent i = new Intent(this, Login.class);
        startActivity(i);
    }

    public void changeDisplayName(View v){
        Intent i = new Intent(this, ChangeDisplayName.class);
        startActivity(i);
    }

    public void changePassword(View v){
        Intent i = new Intent(this, ChangePassword.class);
        startActivity(i);
    }

}
