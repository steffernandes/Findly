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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class ResetPassword extends AppCompatActivity {
    private FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Integer counter=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        hideActionBar();
    }

    public void hideActionBar() {
        androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }

    public void navigateBack(View v){
        Intent i = new Intent(this, Login.class);
        startActivity(i);
    }

    public void goBack(){
        Intent i = new Intent(this, Login.class);
        startActivity(i);
    }

    public void verifyEmail(View v){
        TextView etEmail = findViewById(R.id.email);
        String emailAddress = etEmail.getText().toString();

        db.collection("users")
                .whereEqualTo("email", emailAddress)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("TAG", document.getId() + " => " + document.getData());
                                counter++;

                            }
                            // Email exists in the db
                            if (counter>0){
                                FirebaseAuth auth = mAuth.getInstance();
                                TextView etEmail = findViewById(R.id.email);
                                String emailAddress = etEmail.getText().toString();
                                auth.sendPasswordResetEmail(emailAddress)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.d("TAG", "Email sent.");
                                                }
                                            }
                                        });
                                Toast.makeText(ResetPassword.this, "Email sent.", Toast.LENGTH_LONG).show();
                                goBack();
                            } else {
                                TextView error= findViewById(R.id.error);
                                error.setText("Invalid email.");
                            }
                        } else {
                            Log.d("TAG", "Error getting documents: ", task.getException());

                        }
                    }
                });




    }


}
