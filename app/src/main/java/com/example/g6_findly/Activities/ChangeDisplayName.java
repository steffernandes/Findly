package com.example.g6_findly.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.g6_findly.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class ChangeDisplayName extends AppCompatActivity {
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_display_name);
        hideActionBar();
    }

    public void hideActionBar() {
        androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }


    public void changeDisplayName(View v){
        TextView tvDisplayName= findViewById(R.id.displayName);
        String displayName= tvDisplayName.getText().toString();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("TAG", "User profile updated.");
                            Toast.makeText(ChangeDisplayName.this, "Display name changed", Toast.LENGTH_LONG).show();

                        } else {
                            Log.d("TAG", "Error updating user profile.");
                            Toast.makeText(ChangeDisplayName.this, "Error changing display name. Try again.", Toast.LENGTH_LONG).show();

                        }
                    }
                });
        Intent i = new Intent(this, Settings.class);
        startActivity(i);

    }

    public void navigateBack(View v){
        Intent i = new Intent(this, Settings.class);
        startActivity(i);
    }
}
