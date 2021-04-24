package com.example.g6_findly.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.g6_findly.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuizMovies extends AppCompatActivity {
    Integer new_id= 1;
    Integer movie_counter=0;
    Integer liked_movies=0;
    List<Object> matching_movies = new ArrayList<>();
    List<Object> similar_movies = new ArrayList<>();
    List<Object> images = new ArrayList<>();
    private final String API_KEY = "1049a3065b69fd0a85eac3a4d2b37cf6";
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ImageView image;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_movies);
        hideActionBar();
        image = findViewById(R.id.image);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        if (extras != null){
            ArrayList categoryList = extras.getIntegerArrayList("categoryList");
            // search in movies table for movies with the same category id as the provided by the user
            filterMovies(categoryList);
        }
    }

    public void hideActionBar() {
        androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }

    public void filterMovies(ArrayList categoryList){

        db.collection("movie_details").whereArrayContainsAny("category_ids", categoryList).limit(40).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                matching_movies.add(document.get("movie_id"));
                                images.add(document.get("poster_path"));
                            }
                            Log.d("list", " => " + matching_movies);
                            Picasso.get().load(images.get(movie_counter).toString()).into(image);
                            findViewById(R.id.layout).setVisibility(View.VISIBLE);
                            findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                        } else {
                            Log.d("TAG", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    public void getOpinion(View v){
        String tag = v.getTag().toString();
        switch (tag){
            case "pass":

            case "dislike":
                if (movie_counter <= matching_movies.size()){
                    // load next image
                    Picasso.get().load(images.get(movie_counter++).toString()).into(image);

                } else{
                    suggestionsFinished();
                }

                break;

            case "like":
                liked_movies++;
                findSimilarMovies(movie_counter);
                if (movie_counter<= matching_movies.size() && liked_movies<=5){
                    // load next image
                    Picasso.get().load(images.get(movie_counter++).toString()).into(image);

                } else{
                    suggestionsFinished();
                }
                break;
        }
    }

    public void suggestionsFinished(){
        // Create a new ArrayList
        ArrayList<Object> suggestions = new ArrayList<>();
        Log.d("TAG", "suggestionsFinished: "+ similar_movies);
        // Remove duplicates
        for (Object element : similar_movies) {
            // If this element is not present in newList
            // then add it
            if (!suggestions.contains(element)) {
                suggestions.add(element);
            }
        }

        Log.d("suggestion", "=>"+ suggestions);
        Intent intent = new Intent(this, ChooseMovieCategories.class);
        intent.putExtra("suggestions",suggestions);
        setResult(2,intent);
        finish();//finishing activity

    }

    public void findSimilarMovies(Integer movie){
        new GetSimilarMovies().execute("https://api.themoviedb.org/3/movie/"+  matching_movies.get(movie).toString()+"/similar?api_key="+API_KEY+"&language=en-US&page=1");
    }

    class GetSimilarMovies extends AsyncTask<String, Void, String> {
        HttpURLConnection connection;
        @Override
        protected String doInBackground(String... fileUrl) {

            StringBuilder result = new StringBuilder();

            try {
                URL url = new URL(fileUrl[0]);
                connection = (HttpURLConnection) url.openConnection();

                connection.connect();
                InputStream in = new BufferedInputStream(connection.getInputStream());

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                connection.disconnect();
            }
            return result.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                JSONObject jsonResponse = new JSONObject(result);

                JSONArray response = jsonResponse.getJSONArray("results");

                Integer length = response.length();

                for (int i = 0; i < length; i++) {
                    JSONObject object = response.getJSONObject(i);
                    similar_movies.add(String.valueOf(object.getInt("id")));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d("TAG", "similar size: "+ similar_movies.size());
            for (int i = 0; i <similar_movies.size() ; i++) {
                new MovieDetails().execute("https://api.themoviedb.org/3/movie/"+similar_movies.get(i) +"?api_key=" + API_KEY + "&language=en-US");
            }
        }

    }

    class MovieDetails extends AsyncTask<String, Void, String> {
        HttpURLConnection connection;

        @Override
        protected String doInBackground(String... fileUrl) {

            StringBuilder result = new StringBuilder();

            try {
                URL url = new URL(fileUrl[0]);
                connection = (HttpURLConnection) url.openConnection();

                connection.connect();
                InputStream in = new BufferedInputStream(connection.getInputStream());

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                connection.disconnect();
            }


            return result.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {

                JSONObject response = new JSONObject(result);

                final Map<String, Object> movie_details = new HashMap<>();
                final String id = String.valueOf(response.getInt("id"));
                movie_details.put("movie_id", id);
                movie_details.put("name", response.getString("title"));
                movie_details.put("overview", response.getString("overview"));
                movie_details.put("poster_path", "https://image.tmdb.org/t/p/w600_and_h900_bestv2/"+response.getString("poster_path"));
                movie_details.put("release_data", response.getString("release_date"));
                movie_details.put("runtime", String.valueOf(response.getInt("runtime")));
                movie_details.put("status", response.getString("status"));
                movie_details.put("rating", String.valueOf(response.getInt("vote_average")));
                movie_details.put("hasVideo", String.valueOf(response.getBoolean("video")));
                movie_details.put("popularity", String.valueOf(response.getInt("popularity")));
                JSONArray production_companies = response.getJSONArray("production_companies");

                Integer companiesLength = production_companies.length();
                List<String> production_companies_ids = new ArrayList<>();

                for(int j=0; j < companiesLength; j++){
                    JSONObject object = production_companies.getJSONObject(j);
                    production_companies_ids.add(object.getString("name"));

                }

                movie_details.put("production_companies", production_companies_ids);

                JSONArray genresArray = response.getJSONArray("genres");
                Integer genresLength = genresArray.length();
                List<String> category_ids = new ArrayList<>();

                for(int j=0; j < genresLength; j++){
                    JSONObject object = genresArray.getJSONObject(j);
                    category_ids.add(String.valueOf(object.getInt("id")));
                }

                movie_details.put("category_ids", category_ids);

                 db.collection("movie_details").document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (!document.exists()) {
                                    db.collection("movie_details").document(id)
                                            .set(movie_details, SetOptions.merge())
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
                                }else{
                                    Log.d("TAG", "Movie already exists in the database");
                                }
                            } else {
                                Log.d("TAG", "get failed with ", task.getException());
                            }
                        }
                    });

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}