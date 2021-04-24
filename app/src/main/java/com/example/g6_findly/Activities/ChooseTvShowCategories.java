package com.example.g6_findly.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.CompoundButtonCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChooseTvShowCategories extends AppCompatActivity {
    List<Object> idSeries = new ArrayList<>();
    ArrayList<String> categoryIds = new ArrayList<String>();
    private final String API_KEY = "1049a3065b69fd0a85eac3a4d2b37cf6";
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    LinearLayout linearLayout;
    FirebaseUser user;
    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_tv_show_categories);
        hideActionBar();
        linearLayout = findViewById(R.id.rootContainer);
        user = FirebaseAuth.getInstance().getCurrentUser();
        uid = user.getUid();

        new getCategories().execute("https://api.themoviedb.org/3/genre/tv/list?api_key=" + API_KEY + "&language=en-US");
        for (int i = 1; i < 6; i++) {
            new GetPopularTvShows().execute("https://api.themoviedb.org/3/tv/popular?api_key=" + API_KEY + "&language=en-US&page="+ i);
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
                    CheckBox checkBox = new CheckBox(ChooseTvShowCategories.this);
                    checkBox.setText(object.getString("name"));
                    checkBox.setTextColor(getResources().getColor(R.color.white));
                    checkBox.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    ColorStateList  colorStateList = new ColorStateList(
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

                    db.collection("categories_tv_shows").document(String.valueOf(id))
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

    // add selected categories to user's preferences
    public void addCategoriesToFirebase(View v){
        if (categoryIds.size()>0) {
            Map<String, Object> categories = new HashMap<>();
            categories.put("category_ids", categoryIds);

            db.collection("user_preferences_tv_shows").document(uid)
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
        Intent intent = new Intent(this, QuizTvShows.class);
        intent.putStringArrayListExtra("categoryList", categoryIds);
        startActivityForResult(intent, 2);// Activity is started with requestCode 2

    }

    class GetPopularTvShows extends AsyncTask<String, Void, String> {
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
                    idSeries.add(String.valueOf(object.getInt("id")));

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            for (int i = 0; i <idSeries.size() ; i++) {
                new TvShowDetails().execute("https://api.themoviedb.org/3/tv/"+idSeries.get(i) +"?api_key=" + API_KEY + "&language=en-US");
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
                tv_show_details.put("poster_path", "https://image.tmdb.org/t/p/w600_and_h900_bestv2/"+response.getString("poster_path"));
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

    // Call Back method  to get the suggestion list from the quiz activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        if (requestCode == 2) {
            ArrayList suggestions = data.getStringArrayListExtra("suggestions");
            ArrayList<Object> emptyArrayList = new ArrayList<>();
            Map<String,Object> updates = new HashMap<>();
            updates.put("suggestions", suggestions);
            updates.put("blacklist", emptyArrayList);

            db.collection("user_preferences_tv_shows").document(uid)
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