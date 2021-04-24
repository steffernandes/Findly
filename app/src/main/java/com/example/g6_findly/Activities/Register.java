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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {
    private FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        hideActionBar();

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
    }

    public void hideActionBar() {
        androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public void registerUser(View v){
        EditText et_first_name = findViewById(R.id.firstName);
        EditText et_last_name = findViewById(R.id.lastName);
        EditText et_email = findViewById(R.id.email);
        EditText et_password = findViewById(R.id.password);
        EditText et_confirm_password = findViewById(R.id.confirmPassword);

        final String first_name= et_first_name.getText().toString();
        final String last_name= et_last_name.getText().toString();
        final String email= et_email.getText().toString();
        String password= et_password.getText().toString();
        String confirm_password= et_confirm_password.getText().toString();

        Boolean noErrors = checkForErrors(password, confirm_password, first_name,last_name,email);

        if (noErrors){

            // create user
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d("TAG", "createUserWithEmail:success");

                                FirebaseUser user = mAuth.getCurrentUser();
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(user.getUid())
                                        .build();

                                user.updateProfile(profileUpdates)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.d("TAG", "User profile updated.");
                                                }
                                            }
                                        });

                                saveUser(first_name,last_name,email);
                            } else {

                                // If sign in fails, display a message to the user.
                                Log.w("TAG", "createUserWithEmail:failure", task.getException());
                                Toast.makeText(Register.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }


    public void saveUser(String first_name, String last_name, String email) {

        Map<String, Object> user_info = new HashMap<>();
        user_info.put("first_name", first_name);
        user_info.put("last_name", last_name);
        user_info.put("email", email);

        // Send email verification
        FirebaseUser user = mAuth.getCurrentUser();

        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("TAG", "Email sent.");
                        } else{
                            Log.d("TAG", "Error sending email.");
                        }
                    }
                });

        String uid = user.getUid();

        // Add user data to Firebase
        db.collection("users").document(uid)
                .set(user_info, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("TAG", "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("TAG", "Error writing document", e);
                    }
                });

        // call the Login activity
        Intent i = new Intent(this, Login.class);
        startActivity(i);
    }

    public boolean checkForErrors(String password, String confirm_password, String first_name, String last_name, String email){

        String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        TextView tvError= findViewById(R.id.error);
        if(first_name.length()<1){
            tvError.setText("all fields are required.");
        }

        else if(last_name.length()<1){
            tvError.setText("all fields are required.");
        }
        else if(!email.matches(regex)){
            tvError.setText("email is not valid.");
        }

        else if (password.length() < 6) {
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
