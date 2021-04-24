package com.example.g6_findly.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.g6_findly.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

public class TvShowDelete extends AppCompatActivity  {
    
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String tv_show_name;
    String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tv_show_delete);
        hideActionBar();
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        if (extras != null){
            id = extras.getString("id");
            getTvShowDetails(id);
        }
    }

    public void hideActionBar() {
        androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }

    public void deleteTvShow(View v){
        new AlertDialog.Builder(this)
                .setTitle("Delete Tv Show")
                .setMessage("Are you sure you want to delete "+ tv_show_name + " ?")

                .setPositiveButton("Delete Tv Show", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        db.collection("tv_shows_details").document(id)
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("TAG", "DocumentSnapshot successfully added!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w("TAG", "Error adding document", e);
                                    }
                                });
                        Toast.makeText(TvShowDelete.this , tv_show_name+ " was deleted.", Toast.LENGTH_SHORT);

                        finish();

                    }
                })

                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public void NavigateBack (View v){
        finish();
    }

    public void getTvShowDetails(String id){
        final ImageView image = findViewById(R.id.image);
        final TextView tvName = findViewById(R.id.movie_title);
        final TextView tvReleaseDate = findViewById(R.id.release_date);
        final TextView tvRating = findViewById(R.id.rating);
        final TextView tvDescription = findViewById(R.id.description);
        final TextView tvStatus = findViewById(R.id.status);

        db.collection("tv_shows_details")
                .whereEqualTo("id", id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Picasso.get().load(document.getString("poster_path")).into(image);
                                tvName.setText(document.getString("name"));
                                tv_show_name = document.getString("name");
                                String string =document.getString("release_date");
                                String[] year = string.split("-");
                                String release_date = year[0];
                                tvReleaseDate.setText(release_date);
                                tvRating.setText(document.getString("rating")+ " / 10");
                                tvDescription.setText(document.getString("overview"));
                                tvStatus.setText(document.getString("status"));
                            }
                            findViewById(R.id.info).setVisibility(View.VISIBLE);
                        } else {
                            Log.d("TAG", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}