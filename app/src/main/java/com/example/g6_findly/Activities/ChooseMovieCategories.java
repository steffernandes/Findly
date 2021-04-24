package com.example.g6_findly.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.CompoundButtonCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.g6_findly.Models.Category;
import com.example.g6_findly.R;
import com.example.g6_findly.Adapters.RecyclerViewAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChooseMovieCategories extends AppCompatActivity {

    List<Object> idMovies = new ArrayList<>();
    ArrayList<String> categoryIds = new ArrayList<String>();
    private final String API_KEY = "1049a3065b69fd0a85eac3a4d2b37cf6";
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    LinearLayout linearLayout;
    FirebaseUser user;
    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_movie_categories);
        hideActionBar();
        linearLayout = findViewById(R.id.rootContainer);
        user = FirebaseAuth.getInstance().getCurrentUser();
        uid = user.getUid();

        new getCategories().execute("https://api.themoviedb.org/3/genre/movie/list?api_key=" + API_KEY + "&language=en-US");
        for (int i = 1; i < 6; i++) {
            new GetPopularMovies().execute("https://api.themoviedb.org/3/movie/popular?api_key=" + API_KEY + "&language=en-US&page="+ i);
        }

    }
    public void hideActionBar() {
        androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }

    class getCategories extends AsyncTask<String, Void, String> {
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

                JSONArray response = jsonResponse.getJSONArray("genres");
                Integer length = response.length();
                // add categories from API to Firebase
                for (int i = 0; i < length; i++) {
                    JSONObject object = response.getJSONObject(i);
                    Map<String, Object> category = new HashMap<>();
                    final Integer id = object.getInt("id");
                    category.put("id",id);
                    category.put("name", object.getString("name"));
                    // Create Checkbox Dynamically
                    CheckBox checkBox = new CheckBox(ChooseMovieCategories.this);
                    checkBox.setText(object.getString("name"));
                    checkBox.setTextColor(getResources().getColor(R.color.white));
                    checkBox.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    ColorStateList colorStateList = new ColorStateList(
                            new int[][]{
                                    new int[]{-android.R.attr.state_checked}, // unchecked
                                    new int[]{android.R.attr.state_checked} , // checked
                            },
                            new int[]{
                                    Color.parseColor("#FFFFFF"),
                                    Color.parseColor("#F33939"),
                            }
                    );

                    CompoundButtonCompat.setButtonTintList(checkBox,colorStateList);
                    checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if(isChecked){
                                categoryIds.add(id.toString());
                            } else {
                                categoryIds.remove(id.toString());
                            }
                            if (categoryIds.size()>0) {
                                findViewById(R.id.next).setEnabled(true);

                            } else {
                                findViewById(R.id.next).setEnabled(false);
                            }
                            Log.d("TAG", "categories: "+ categoryIds);
                        }
                    });

                    // Add Checkbox to LinearLayout
                    if (linearLayout != null) {
                        linearLayout.addView(checkBox);
                    }

                    db.collection("categories_movies").document(String.valueOf(id))
                            .set(category, SetOptions.merge())
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
                }
                findViewById(R.id.layout).setVisibility(View.VISIBLE);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void addCategoriesToFirebase(View v){

         if (categoryIds.size()>0) {
            Map<String, Object> categories = new HashMap<>();
            categories.put("category_ids", categoryIds);

            db.collection("user_preferences_movies").document(uid)
                .set(categories, SetOptions.merge())
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
            }
        Intent intent = new Intent(this, QuizMovies.class);
        intent.putStringArrayListExtra("categoryList", categoryIds);
        startActivityForResult(intent, 2);// Activity is started with requestCode 2
    }

    class GetPopularMovies extends AsyncTask<String, Void, String> {
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

                // add popular movies from API to Firebase
                for (int i = 0; i < length; i++) {
                    final JSONObject object = response.getJSONObject(i);
                    idMovies.add(String.valueOf(object.getInt("id")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            for (int i = 0; i <idMovies.size() ; i++) {
                new MovieDetails().execute("https://api.themoviedb.org/3/movie/"+idMovies.get(i) +"?api_key=" + API_KEY + "&language=en-US");
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

                db.collection("movies_details").document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
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

    // Call Back method  to get the suggestion list from the quiz activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        if(requestCode==2)
        {
            ArrayList suggestions = data.getStringArrayListExtra("suggestions");
            ArrayList<Object> emptyArrayList = new ArrayList<>();
            Map<String,Object> updates = new HashMap<>();
            updates.put("suggestions", suggestions);
            updates.put("blacklist", emptyArrayList);

            db.collection("user_preferences_movies").document(uid)
                    .update(updates)
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

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }
}
