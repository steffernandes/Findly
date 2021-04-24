package com.example.g6_findly.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.g6_findly.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        hideActionBar();
    }
    public void hideActionBar() {
        androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public void resetPassword(View v){
        Intent i = new Intent(this, ResetPassword.class);
        startActivity(i);
    }

    public void signUp(View v){
        Intent i = new Intent(this, Register.class);
        startActivity(i);
    }
    private void updateUI(FirebaseUser currentUser) {
        if (currentUser!= null){
            if (currentUser.getUid().equals("eQz9oIFDUZR6amNZTMumgcaXUyi1")){
                Intent i = new Intent(this, ListElements.class);
                startActivity(i);
            } else{
                Intent i = new Intent(this, MainActivity.class);
                startActivity(i);
            }
        }
    }
    public void signInWithEmailAndPassword(View v){
        EditText et_email = findViewById(R.id.email);
        EditText et_password = findViewById(R.id.password);
        String email= et_email.getText().toString();
        String password= et_password.getText().toString();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else  {
                            // If sign in fails, display a message to the user.
                            Log.w("LOGIN ERROR", "signInWithEmail:failure", task.getException());
                            TextView tvError= findViewById(R.id.error);
                            tvError.setText("Invalid email or password.");
                            updateUI(null);
                        }
                    }
                });
    }
}




