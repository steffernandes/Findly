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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class SeriesDetails extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser user;
    String series_name;
    String poster_path;
    ImageView watchlist;
    String id;
    Boolean addedToWatchlist= false;
    String document_reference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_series_details);
        hideActionBar();
        watchlist = findViewById(R.id.bookmark);
        user = FirebaseAuth.getInstance().getCurrentUser();

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        if (extras != null){
            id = extras.getString("id");
            getTvShowDetails(id);
            isSelected(id);

        }


        watchlist.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // tv show is in user's watchlist. Delete the movie from watchlist
                if (addedToWatchlist){
                    addedToWatchlist = false;
                    watchlist.setImageResource(R.drawable.bookmark_outlined);
                    deleteTvShowFromWatchlist();
                }
                // tv show is not in user's watchlist. Add the movie to watchlist
                else{
                    addedToWatchlist = true;
                    watchlist.setImageResource(R.drawable.bookmark_filled);
                   addTvShowToWatchlist();
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
                sendIntent.putExtra(Intent.EXTRA_TEXT, "I think you'll like to watch " + series_name+". Install Findly for more tv shows suggestions!");
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

    public void addTvShowToWatchlist(){

        Map<String, Object> info = new HashMap<>();
        info.put("user_id", user.getUid() );
        info.put("series_id", id );
        info.put("poster_path", poster_path);

        db.collection("watchlist").document()
                .set(info)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("TAG", "DocumentSnapshot successfully added!");
                        isSelected(id);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("TAG", "Error adding document", e);
                    }
                });
    }

    public void deleteTvShowFromWatchlist(){

        db.collection("watchlist").document(document_reference)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("TAG", "Tv Show successfully deleted!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("TAG", "Error deleting tv show", e);
                    }
                });

    }

    public void NavigateBack (View v){
        finish();
    }

    public void getTvShowDetails(String tv_show_id){
        final ImageView image = findViewById(R.id.image);
        final TextView tvName = findViewById(R.id.movie_title);
        final TextView tvReleaseDate = findViewById(R.id.release_date);
        final TextView tvRating = findViewById(R.id.rating);
        final TextView tvDescription = findViewById(R.id.description);
        final TextView tvStatus = findViewById(R.id.status);

        db.collection("tv_shows_details")
                .whereEqualTo("id", tv_show_id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                poster_path = document.getString("poster_path");
                                Picasso.get().load(document.getString("poster_path")).into(image);
                                tvName.setText(document.getString("name"));
                                series_name = document.getString("name");
                                String string =document.getString("release_date");
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
                                if (document.getString("series_id")!= null && document.getString("series_id").equals(movie_id)){
                                    document_reference = document.getReference().getId();
                                    Log.d("TAG", "document reference: "+ document_reference);
                                    watchlist.setImageResource(R.drawable.bookmark_filled);
                                    addedToWatchlist = true;
                                }
                            }
                            findViewById(R.id.info).setVisibility(View.VISIBLE);
                        } else {
                            Log.d("TAG", "Error getting documents: ", task.getException());
                        }
                        findViewById(R.id.info).setVisibility(View.VISIBLE);
                    }
                });
    }
}