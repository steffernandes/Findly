package com.example.g6_findly.Fragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DiffUtil;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.g6_findly.Activities.ChooseMovieCategories;
import com.example.g6_findly.Activities.ChooseTvShowCategories;
import com.example.g6_findly.Activities.EditProfilePicture;
import com.example.g6_findly.Activities.MainActivity;
import com.example.g6_findly.Activities.MovieDetails;
import com.example.g6_findly.Activities.QuizMovies;
import com.example.g6_findly.Adapters.CardStackAdapter;
import com.example.g6_findly.CardStackCallback;
import com.example.g6_findly.Models.Category;
import com.example.g6_findly.Models.PosterModel;
import com.example.g6_findly.Models.Suggestions;
import com.example.g6_findly.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.protobuf.StringValue;
import com.squareup.picasso.Picasso;
import com.yuyakaido.android.cardstackview.CardStackLayoutManager;
import com.yuyakaido.android.cardstackview.CardStackListener;
import com.yuyakaido.android.cardstackview.CardStackView;
import com.yuyakaido.android.cardstackview.Direction;
import com.yuyakaido.android.cardstackview.StackFrom;
import com.yuyakaido.android.cardstackview.SwipeableMethod;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

public class MoviesFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = MoviesFragment.class.getSimpleName();
    List<PosterModel> items;
    List<PosterModel> item;
    private CardStackLayoutManager manager;
    private CardStackAdapter adapter;

    List<Object> idMovies = new ArrayList<>();

    List<Object> similar_movies = new ArrayList<>();
    List<String> movies;
    List<String> blacklist = new ArrayList<>();
    Map<String, Object> movie_details;
    int added_movie;
    LinearLayout loader;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String uid = user.getUid();

    String poster_image;

    private final String API_KEY = "1049a3065b69fd0a85eac3a4d2b37cf6";
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public MoviesFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static MoviesFragment newInstance(String param1, String param2) {
        MoviesFragment fragment = new MoviesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_movies, container, false);
        loader = root.findViewById(R.id.loadingPanel);
        ImageView changePreferences = root.findViewById(R.id.changePreferences);
        changePreferences.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent i = new Intent(getActivity(), ChooseMovieCategories.class);
                startActivity(i);
            }
        });
        init(root);
        getMovies();
        return root;
    }

    private void init(View root) {
        CardStackView cardStackView = root.findViewById(R.id.card_stack_view);
        manager = new CardStackLayoutManager(getContext(), new CardStackListener() {
            @Override
            public void onCardDragging(Direction direction, float ratio) {
            }

            @Override
            public void onCardSwiped(Direction direction) {
                if (manager.getTopPosition()< manager.getItemCount() && manager.getTopPosition() > movies.size()-3) {
                    if (similar_movies.size() > 1) {
                        updateSuggestions();
                    } else {
                        new GetPopularMovies().execute("https://api.themoviedb.org/3/movie/popular?api_key=" + API_KEY + "&language=en-US&page=1");
                    }
                }

                // User added the movie to the watchlist
                if (direction == Direction.Right) {
                    int current_movie = manager.getTopPosition();
                    if (manager.getTopPosition() < manager.getItemCount()) {
                        String movie_id = movies.get(current_movie - 1);
                        Log.d(TAG, "movie to delete: " + movie_id);
                        // delete movie from suggestions array and add it to the blacklist array so it's not suggested anymore
                        deleteSuggestion(movie_id);

                        addMovieToWatchlist(movie_id);
                        // find similar movies and add them to the suggestions list
                        new GetSimilarMovies().execute("https://api.themoviedb.org/3/movie/" + movie_id + "/similar?api_key=" + API_KEY + "&language=en-US&page=1");
                    }
                }

                // User denied the suggestion
                if (direction == Direction.Left){
                    int current_movie =  manager.getTopPosition();
                    if (manager.getTopPosition()< manager.getItemCount()){
                        String movie_id =movies.get(current_movie-1);
                        Log.d(TAG, "movie to delete: "+ movie_id);
                        // delete movie from suggestions array and add it to the blacklist array so it's not suggested anymore
                        deleteSuggestion(movie_id);
                    }
                }
            }

            @Override
            public void onCardRewound() {
            }

            @Override
            public void onCardCanceled() {
            }

            @Override
            public void onCardAppeared(View view, int position) {
            }

            @Override
            public void onCardDisappeared(View view, int position) {
            }
        });

        manager.setStackFrom(StackFrom.None);
        manager.setVisibleCount(3);
        manager.setTranslationInterval(8.0f);
        manager.setScaleInterval(0.95f);
        manager.setSwipeThreshold(0.3f);
        manager.setMaxDegree(20.0f);
        manager.setDirections(Direction.FREEDOM);
        manager.setCanScrollHorizontal(true);
        manager.setCanScrollVertical(false);
        manager.setSwipeableMethod(SwipeableMethod.Manual);
        manager.setOverlayInterpolator(new LinearInterpolator());
        adapter = new CardStackAdapter(addList());
        cardStackView.setHasFixedSize(true);
        cardStackView.setLayoutManager(manager);
        cardStackView.setAdapter(adapter);
        cardStackView.setItemAnimator(new DefaultItemAnimator());

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
                    similar_movies.add(String.valueOf(object.getInt("id")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            for (int i = 0; i <idMovies.size() ; i++) {
                new MovieDetails().execute("https://api.themoviedb.org/3/movie/"+idMovies.get(i) +"?api_key=" + API_KEY + "&language=en-US");
            }
            updateSuggestions();
        }
    }

    public void getMovies(){

        movies = new ArrayList();

        db.collection("user_preferences_movies").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        if (document.get("suggestions")== null){
                            Intent i = new Intent(getActivity(), ChooseMovieCategories.class);
                            startActivity(i);
                        } else {
                            items= new ArrayList<>();
                            movies = (List<String>) document.get("suggestions"); // get movie suggestions id's
                            blacklist = (List<String>) document.get("blacklist");
                            movies.removeAll(blacklist);
                        }


                    } else {
                        Log.d("TAG", "No such document");
                        Intent i = new Intent(getActivity(), ChooseMovieCategories.class);
                        startActivity(i);
                    }
                } else {
                    Log.d("TAG", "get failed with ", task.getException());
                }

                // add suggestions from database to card stack
                for (int j = 0; j <movies.size() ; j++) {
                    db.collection("movie_details").document(movies.get(j)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                added_movie = 0;
                                // if movie hasn't benn added to the collection. Add movie details
                                if (document.exists()) {
                                    if (added_movie < 1) {
                                        String string = document.getString("release_data");
                                        String[] year = string.split("-");
                                        String release_date = year[0];
                                        items.add(new PosterModel(document.getString("poster_path"), document.getString("name"), release_date, document.getString("rating"), document.getString("overview"), document.getString("status"), document.getString(("movie_id"))));
                                        added_movie++;
                                    }
                                } else {
                                    Log.d("TAG", "Movie doesn't exist in movie details.");
                                }
                                Log.d(TAG, "items size: "+ items.size());
                                List<PosterModel> oldList = adapter.getItems();
                                List<PosterModel> newList = items;
                                CardStackCallback callback = new CardStackCallback(oldList,newList);
                                DiffUtil.DiffResult result = DiffUtil.calculateDiff(callback);
                                adapter.setItems(newList);
                                result.dispatchUpdatesTo(adapter);
                            } else {
                                Log.d("TAG", "get failed with ", task.getException());
                            }
                        }
                    });
                }
                loader.setVisibility(View.GONE);
            }
        });
    }

    public void addMovieToWatchlist(final String id){
        db.collection("movie_details").document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    // if movie hasn't benn added to the collection. Add movie details
                    if (document.exists()) {
                         poster_image = document.getString("poster_path")   ;
                    }
                    Map<String, String> info = new HashMap<>();
                    info.put("user_id", user.getUid() );
                    info.put("movie_id", id );
                    info.put("poster_path", poster_image);
                    db.collection("watchlist").document()
                            .set(info)
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
                } else {
                    Log.d("TAG", "get failed with ", task.getException());
                }
            }
        });


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

                // checks if the suggestion is't blacklisted. If its not than add to similar movies. If movie id doesn't exist in details table, request details and at to table
                for (int i = 0; i < length; i++) {
                    JSONObject object = response.getJSONObject(i);
                    final String movie_id=String.valueOf(object.getInt("id"));

                    if (!blacklist.contains(movie_id)){
                        similar_movies.add(movie_id);
                    }

                    db.collection("movie_details").document(movie_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                // if movie hasn't benn added to the collection. Add movie details
                                if (!document.exists()) {
                                    new MovieDetails().execute("https://api.themoviedb.org/3/movie/"+ movie_id +"?api_key=" + API_KEY + "&language=en-US");
                                }else{
                                    Log.d("TAG", "Movie with id "+ movie_id + " already exists.");
                                }
                            } else {
                                Log.d("TAG", "get failed with ", task.getException());
                            }
                        }
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
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
                movie_details = new HashMap<>();
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

                Log.d(TAG, "add movie details: " + id);
                movie_details.put("category_ids", category_ids);
                db.collection("movie_details").document(id)
                        .set(movie_details)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("TAG", "Movie with id "+ id + " successfully written!");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("TAG", "Error writing document", e);
                            }
                        });

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void deleteSuggestion(String movie_id) {
        blacklist.add(movie_id);

        Log.d(TAG, "blacklist: "+ blacklist);
        Log.d(TAG, "movies before: "+ movies);
        movies.remove(movie_id);
        Log.d(TAG, "movies after: "+ movies);

        // Remove the 'suggestions' field from the document
        Map<String,Object> updates = new HashMap<>();
        updates.put("suggestions", movies);
        updates.put("blacklist", blacklist);

        db.collection("user_preferences_movies").document(uid).update(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {}
        });
    }

    public void updateSuggestions() {

        Log.d(TAG, "updateSuggestions: similar movies before "+ similar_movies);
        Log.d(TAG, "updateSuggestions: blacklist "+ blacklist);

        // Remove duplicates
        for(int i=similar_movies.size()-1; i>0; i--) {
            for(int j=i-1; j>=0; j--) {
                if(similar_movies.get(i).equals(similar_movies.get(j))) {
                    similar_movies.remove(i);
                    break;
                }
            }
        }
        // Remove elements blacklisted
        similar_movies.removeAll(blacklist);

        db.collection("user_preferences_movies").document(uid).update("suggestions", similar_movies).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                similar_movies.clear();
                getMovies();
            }

        });
    }

    public List<PosterModel> addList() {
        item = new ArrayList<>();
        return item;
    }

    @Override
    public void onStop() {
        super.onStop();
        getMovies();
    }
}