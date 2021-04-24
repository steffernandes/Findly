package com.example.g6_findly.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.g6_findly.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ChangePassword2 extends AppCompatActivity {
    private FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password_2);
        hideActionBar();


        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
    }

    public void hideActionBar() {
        androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }

    public void navigateBack(View v){
        Intent i = new Intent(this, ChangePassword.class);
        startActivity(i);
    }

    public void setPassword(View v){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        EditText et_password = findViewById(R.id.password);
        EditText et_confirm_password = findViewById(R.id.confirmPassword);
        String password = et_password.getText().toString();
        String confirm_password= et_confirm_password.getText().toString();
        Boolean noErrors = checkForErrors(password, confirm_password);


        if (noErrors){
            user.updatePassword(password).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d("TAG", "Password updated");
                        Toast.makeText(ChangePassword2.this, "Password updated!",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d("TAG", "Error password not updated");
                        Toast.makeText(ChangePassword2.this, "Error. Password not updated",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }
    public boolean checkForErrors(String password, String confirm_password) {
        TextView tvError= findViewById(R.id.error);
        if (password.length() < 6) {
            tvError.setText("password must be at least 6 characters long.");
        } else {
            boolean upper = false;
            boolean lower = false;
            boolean number = false;
            boolean notDifferent = false;

            if (password.equals(confirm_password)) {
                notDifferent = true;
            }

            for (char character : password.toCharArray()) {
                if (Character.isUpperCase(character)) {
                    upper = true;
                } else if (Character.isLowerCase(character)) {
                    lower = true;
                } else if (Character.isDigit(character)) {
                    number = true;
                }
            }

            if (!upper) {
                tvError.setText("password must contain at least one uppercase character.");
            } else if (!lower) {
                tvError.setText("password must contain at least one lowercase character.");
            } else if (!number) {
                tvError.setText("password must contain at least one number.");
            } else if (!notDifferent) {
                tvError.setText("passwords don't match.");
            } else {
                tvError.setText("");
                return true;
            }

        }
        return false;
    }
}
