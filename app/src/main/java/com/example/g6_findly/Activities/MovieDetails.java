package com.example.g6_findly.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.g6_findly.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipDrawable;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class MovieDetails extends AppCompatActivity  {
    
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser user;
    String movie_name;
    String poster_path;
    ImageView watchlist;
    String movie_id;
    Boolean addedToWatchlist= false;
    String document_reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        hideActionBar();
        watchlist = findViewById(R.id.bookmark);
        user = FirebaseAuth.getInstance().getCurrentUser();

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        if (extras != null){
            movie_id = extras.getString("movie_id");
            getMovieDetails(movie_id);
            isSelected(movie_id);

        }


        watchlist.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // movie is in user's watchlist. Delete the movie from watchlist
                if (addedToWatchlist){
                    addedToWatchlist = false;
                    watchlist.setImageResource(R.drawable.bookmark_outlined);
                    Log.d("TAG", "movie deleted: ");
                    deleteMovieFromWatchlist();
                }
                // movie is not in user's watchlist. Add the movie to watchlist
                else{
                    addedToWatchlist = true;
                    watchlist.setImageResource(R.drawable.bookmark_filled);
                    Log.d("TAG", "movie added: ");
                    addMovieToWatchlist();
                }
            }
        });

        ImageView share = findViewById(R.id.share);
        share.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "I think you'll like to watch " + movie_name+". Install Findly for more movie suggestions!");
                sendIntent.setType("text/plain");

                Intent shareIntent = Intent.createChooser(sendIntent, null);
                startActivity(shareIntent);
            }
        });


    }

    public void hideActionBar() {
        androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }

    public void addMovieToWatchlist(){

        Map<String, String> info = new HashMap<>();
        info.put("user_id", user.getUid() );
        info.put("movie_id", movie_id );
        info.put("poster_path", poster_path);
            db.collection("watchlist").document()
                    .set(info)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("TAG", "DocumentSnapshot successfully added!");
                            isSelected(movie_id);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("TAG", "Error adding document", e);
                        }
                    });
    }

    public void deleteMovieFromWatchlist(){

        db.collection("watchlist").document(document_reference)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("TAG", "Movie successfully deleted!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("TAG", "Error deleting movie", e);
                    }
                });

    }

    public void NavigateBack (View v){
        finish();
    }

    public void getMovieDetails(String movie_id){
        final ImageView image = findViewById(R.id.image);
        final TextView tvName = findViewById(R.id.movie_title);
        final TextView tvReleaseDate = findViewById(R.id.release_date);
        final TextView tvRating = findViewById(R.id.rating);
        final TextView tvDescription = findViewById(R.id.description);
        final TextView tvStatus = findViewById(R.id.status);

        db.collection("movie_details")
                .whereEqualTo("movie_id", movie_id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                poster_path = document.getString("poster_path");
                                Picasso.get().load(document.getString("poster_path")).into(image);
                                tvName.setText(document.getString("name"));
                                movie_name = document.getString("name");
                                String string =document.getString("release_data");
                                String[] year = string.split("-");
                                String release_date = year[0];
                                tvReleaseDate.setText(release_date);
                                tvRating.setText(document.getString("rating")+ " / 10");
                                tvDescription.setText(document.getString("overview"));
                                tvStatus.setText(document.getString("status"));
                            }
                        } else {
                            Log.d("TAG", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    public void isSelected(final String movie_id){

        db.collection("watchlist")
                .whereEqualTo("user_id", user.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (document.getString("movie_id")!= null && document.getString("movie_id").equals(movie_id)){
                                    document_reference = document.getReference().getId();
                                    Log.d("TAG", "document reference: "+ document_reference);
                                    watchlist.setImageResource(R.drawable.bookmark_filled);
                                    addedToWatchlist = true;
                                    Log.d("TAG", "movie on users watchlist ");
                                }
                                findViewById(R.id.info).setVisibility(View.VISIBLE);
                            }

                        } else {
                            Log.d("TAG", "Error getting documents: ", task.getException());
                        }

                    }
                });
    }

}