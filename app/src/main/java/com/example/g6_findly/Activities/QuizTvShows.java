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
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuizTvShows extends AppCompatActivity {
    Integer showed_counter=0;
    Integer liked_tv_shows=0;
    List<Object> matching_tv_shows = new ArrayList<>();
    List<Object> similar_tv_shows = new ArrayList<>();
    List<Object> images = new ArrayList<>();
    private final String API_KEY = "1049a3065b69fd0a85eac3a4d2b37cf6";
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ImageView image ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_tv_shows);
        hideActionBar();
        image = findViewById(R.id.image);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        if (extras != null){
            ArrayList categoryList = extras.getIntegerArrayList("categoryList");
            // search in movies table for movies with the same category id as the provided by the user
            filterTvShows(categoryList);
        }
    }

    public void hideActionBar() {
        androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }

    public void filterTvShows(ArrayList categoryList){

        db.collection("tv_shows_details").whereArrayContainsAny("category_id", categoryList).limit(40).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                matching_tv_shows.add(document.getString("id"));
                                images.add(document.getString("poster_path"));
                            }
                            Log.d("list", " => " + matching_tv_shows);
                            Picasso.get().load(images.get(0).toString()).into(image);
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
                if (showed_counter<= matching_tv_shows.size()){
                    // load next image
                    Picasso.get().load(images.get(showed_counter++).toString()).into(image);

                } else{
                    suggestionsFinished();
                }

                break;

            case "like":
                liked_tv_shows++;
                findSimilarTvShows(showed_counter);
                Log.d("TAG", "liked: "+ matching_tv_shows.get(showed_counter));

                if (showed_counter<= matching_tv_shows.size() && liked_tv_shows<=5){
                    // load next image
                    Picasso.get().load(images.get(showed_counter++).toString()).into(image);

                } else{
                    suggestionsFinished();
                }
                break;
        }
    }

    public void suggestionsFinished(){
        // Create new ArrayList
        ArrayList<Object> suggestions = new ArrayList<>();
        Log.d("TAG", "suggestionsFinished: "+ similar_tv_shows);
        // Remove duplicates
        for (Object element : similar_tv_shows) {
            // If this element is not present in newList
            // then add it
            if (!suggestions.contains(element)) {
                suggestions.add(element);
            }
        }


        Intent intent = new Intent(this, ChooseTvShowCategories.class);
        intent.putExtra("suggestions",suggestions);
        setResult(2,intent);
        finish();
    }

    public void findSimilarTvShows(Integer id){
        new GetSimilarTvShows().execute("https://api.themoviedb.org/3/tv/"+  matching_tv_shows.get(id).toString()+"/similar?api_key="+API_KEY+"&language=en-US&page=1");
    }

    class GetSimilarTvShows extends AsyncTask<String, Void, String> {
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
                    similar_tv_shows.add(String.valueOf(object.getInt("id")));
                    new TvShowDetails().execute("https://api.themoviedb.org/3/tv/"+object.getInt("id")+"?api_key=" + API_KEY + "&language=en-US");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    class TvShowDetails extends AsyncTask<String, Void, String> {
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

                final Map<String, Object> tv_show_details = new HashMap<>();
                tv_show_details.put("id", String.valueOf(response.getInt("id")));
                final String id = String.valueOf(response.getInt("id"));
                tv_show_details.put("name", response.getString("name"));
                tv_show_details.put("overview", response.getString("overview"));
                tv_show_details.put("poster_path", "https://image.tmdb.org/t/p/w600_and_h900_bestv2"+response.getString("poster_path"));
                tv_show_details.put("release_date", response.getString("first_air_date"));
                tv_show_details.put("seasons", String.valueOf(response.getInt("number_of_seasons")));
                tv_show_details.put("status", response.getString("status"));
                tv_show_details.put("rating", String.valueOf(response.getInt("vote_average")));
                JSONArray networks = response.getJSONArray("networks");

                Integer networksLength = networks.length();
                List<String> network_logo = new ArrayList<>();

                for(int j=0; j < networksLength; j++){
                    JSONObject object = networks.getJSONObject(j);
                    network_logo.add("https://image.tmdb.org/t/p/w500"+object.getString("logo_path"));
                }

                tv_show_details.put("networks", network_logo);

                JSONArray genresArray = response.getJSONArray("genres");
                Integer genresLength = genresArray.length();
                List<String> category_ids = new ArrayList<>();
                List<String> category_name = new ArrayList<>();

                for(int j=0; j < genresLength; j++){
                    JSONObject object = genresArray.getJSONObject(j);
                    category_ids.add(String.valueOf(object.getInt("id")));
                    category_name.add(String.valueOf(object.getString("name")));

                }

                tv_show_details.put("category_id", category_ids);
                tv_show_details.put("category_name", category_name);

                db.collection("tv_shows_details").document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            assert document != null;
                            if (!document.exists()) {
                                db.collection("tv_shows_details").document(id).set(tv_show_details, SetOptions.merge())
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
                                Log.d("TAG", "Tv show already exists in the database");
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