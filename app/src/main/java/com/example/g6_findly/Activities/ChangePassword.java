package com.example.g6_findly.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.g6_findly.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePassword extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        mAuth = FirebaseAuth.getInstance();
        hideActionBar();
    }

    public void hideActionBar() {
        androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }

    public void navigateBack(View v){
        Intent i = new Intent(this, Settings.class);
        startActivity(i);
    }

    public void checkPassword(View v){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        EditText tvPassword = findViewById(R.id.currentPassword);
        String password = tvPassword.getText().toString();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            stepTwo();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "signInWithEmail:failure", task.getException());
                            TextView tvError= findViewById(R.id.error);
                            tvError.setText("Wrong Password.");
                        }

                    }
                });
    }

    public void stepTwo(){
        Intent i = new Intent(this, ChangePassword2.class );
        startActivity(i);
    }
}
