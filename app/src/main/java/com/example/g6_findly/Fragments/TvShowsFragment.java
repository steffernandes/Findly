package com.example.g6_findly.Fragments;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DiffUtil;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.g6_findly.Activities.ChooseMovieCategories;
import com.example.g6_findly.Activities.ChooseTvShowCategories;
import com.example.g6_findly.Adapters.CardStackAdapter;
import com.example.g6_findly.CardStackCallback;
import com.example.g6_findly.Models.PosterModel;
import com.example.g6_findly.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TvShowsFragment extends Fragment {
    private static final String TAG = TvShowsFragment.class.getSimpleName();
    private CardStackLayoutManager manager;
    private CardStackAdapter adapter;

    List<PosterModel> items;
    List<PosterModel> item;

    List<Object> idSeries = new ArrayList<>();

    List<Object> similar_series = new ArrayList<>();
    List<String> series;
    List<String> blacklist = new ArrayList<>();
    Map<String, Object> series_details;
    int added_series;
    int page_number=1;
    LinearLayout loader;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String uid = user.getUid();
    private final String API_KEY = "1049a3065b69fd0a85eac3a4d2b37cf6";
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String poster_image;

    public TvShowsFragment() {
        // Required empty public constructor
    }

    public static TvShowsFragment newInstance(String param1, String param2) {
        TvShowsFragment fragment = new TvShowsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_tv_shows, container, false);
        loader = root.findViewById(R.id.loadingPanel);
        ImageView changePreferences = root.findViewById(R.id.changePreferences);
        changePreferences.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent i = new Intent(getActivity(), ChooseTvShowCategories.class);
                startActivity(i);
            }
        });
        init(root);
        getTvShows();
        return root;
    }

    private void init(View root) {
        CardStackView cardStackView = root.findViewById(R.id.card_stack_view);
        manager = new CardStackLayoutManager(getContext(), new CardStackListener() {
            @Override
            public void onCardDragging(Direction direction, float ratio) {
                Log.d(TAG, "onCardDragging: d=" + direction.name() + " ratio=" + ratio);
            }

            @Override
            public void onCardSwiped(Direction direction) {
                if (manager.getTopPosition()< manager.getItemCount() && manager.getTopPosition() > series.size()-3) {
                    if (similar_series.size() > 1) {
                        updateSuggestions();
                    } else {
                        new GetPopularTvShows().execute("https://api.themoviedb.org/3/tv/popular?api_key=" + API_KEY + "&language=en-US&page="+ page_number);
                        page_number++;
                    }
                }

                // User added the movie to the watchlist
                if (direction == Direction.Right) {
                    int current_series = manager.getTopPosition();
                    if (manager.getTopPosition() < manager.getItemCount()) {
                        String series_id = series.get(current_series - 1);
                        Log.d(TAG, "series to delete: " + series_id);
                        // delete movie from suggestions array and add it to the blacklist array so it's not suggested anymore
                        deleteSuggestion(series_id);

                        addMovieToWatchlist(series_id);
                        // find similar tv shows and add them to the suggestions list
                        new GetPopularTvShows().execute("https://api.themoviedb.org/3/movie/" + series_id + "/similar?api_key=" + API_KEY + "&language=en-US&page=1");
                    }
                }

                // User denied the suggestion
                if (direction == Direction.Left){
                    int current_series =  manager.getTopPosition();
                    if (manager.getTopPosition()< manager.getItemCount()){
                        String series_id =series.get(current_series-1);
                        Log.d(TAG, "series to delete: "+ series_id);
                        // delete tv shows from suggestions array and add it to the blacklist array so it's not suggested anymore
                        deleteSuggestion(series_id);
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



    public void getTvShows(){

        series = new ArrayList();

        db.collection("user_preferences_tv_shows").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        if (document.get("suggestions")== null){
                            Intent i = new Intent(getActivity(), ChooseTvShowCategories.class);
                            startActivity(i);
                        } else {
                            items= new ArrayList<>();
                            series = (List<String>) document.get("suggestions");
                            blacklist = (List<String>) document.get("blacklist");
                            series.removeAll(blacklist);
                        }

                    } else {
                        Log.d("TAG", "No such document");
                        Intent i = new Intent(getActivity(), ChooseTvShowCategories.class);
                        startActivity(i);
                    }
                } else {
                    Log.d("TAG", "get failed with ", task.getException());
                }

                // add suggestions from database to card stack
                for (int j = 0; j <series.size() ; j++) {
                    db.collection("tv_shows_details").document(series.get(j)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                added_series = 0;
                                // if movie hasn't benn added to the collection. Add movie details
                                if (document.exists()) {
                                    if (added_series < 1) {
                                        String string = document.getString("release_date");
                                        String[] year = string.split("-");
                                        String release_date = year[0];
                                        items.add(new PosterModel(document.getString("poster_path"), document.getString("name"), release_date, document.getString("rating"), document.getString("overview"), document.getString("status"), document.getString(("id"))));
                                        added_series++;
                                    }
                                } else {
                                    Log.d("TAG", "Tv Show doesn't exist in movie details.");
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
        db.collection("tv_shows_details").document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
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
                    info.put("series_id", id );
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

    public void deleteSuggestion(String id) {
        blacklist.add(id);

        Log.d(TAG, "blacklist: "+ blacklist);
        series.remove(id);

        // Remove the 'suggestions' field from the document
        Map<String,Object> updates = new HashMap<>();
        updates.put("suggestions", series);
        updates.put("blacklist", blacklist);

        db.collection("user_preferences_tv_shows").document(uid).update(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {}
        });
    }

    public void updateSuggestions() {

        // Remove duplicates
        for(int i=similar_series.size()-1; i>0; i--) {
            for(int j=i-1; j>=0; j--) {
                if(similar_series.get(i).equals(similar_series.get(j))) {
                    similar_series.remove(i);
                    break;
                }
            }
        }
        // Remove elements blacklisted
        similar_series.removeAll(blacklist);

        db.collection("user_preferences_movies").document(uid).update("suggestions", similar_series).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                similar_series.clear();
                getTvShows();
            }

        });
    }

    public List<PosterModel> addList() {
        item = new ArrayList<>();
        return item;
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
                    final String id=String.valueOf(object.getInt("id"));

                    if (!blacklist.contains(id)){
                        similar_series.add(id);
                    }

                    db.collection("tv_shows_details").document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                // if movie hasn't benn added to the collection. Add movie details
                                if (!document.exists()) {
                                    new TvShowDetails().execute("https://api.themoviedb.org/3/tv/"+id +"?api_key=" + API_KEY + "&language=en-US");
                                }else{
                                    Log.d("TAG", "Tv shows with id "+ id + " already exists.");
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
                tv_show_details.put("release_date", response.getString("release_date"));
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
