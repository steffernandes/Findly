package com.example.g6_findly.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.g6_findly.Activities.MovieDetails;
import com.example.g6_findly.Activities.SeriesDetails;
import com.example.g6_findly.Models.SearchResults;
import com.example.g6_findly.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchSeriesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchSeriesFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    @BindView(R.id.recycler_view)
    RecyclerView searchResults;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirestoreRecyclerAdapter adapter;

    public SearchSeriesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FlightsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SearchSeriesFragment newInstance(String param1, String param2) {
        SearchSeriesFragment fragment = new SearchSeriesFragment();
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
        View view= inflater.inflate(R.layout.fragment_search_series, container, false);
        EditText searchView = view.findViewById(R.id.search_bar); // inititate a search view
        ButterKnife.bind(this, view);
        init();
        getResults("/"); // !! without this it thrown an error

        searchView.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                Log.d("TAG", "afterTextChanged: "+ s.toString());
                if(s.toString()== null){
                    getResults("/");
                } else {
                    //String capitalized = s.toString().substring(0, 1).toUpperCase() + s.toString().substring(1,s.length());
                    getResults(s.toString());
                }

            }
        });
        return view;
    }
    private void init(){
        GridLayoutManager manager = new GridLayoutManager(getContext(), 3, GridLayoutManager.VERTICAL, false);
        searchResults.setLayoutManager(manager);
        db = FirebaseFirestore.getInstance();
    }

    public void getResults(String search) {
        Query query =db.collection("tv_shows_details").orderBy("name").startAt(search).endAt(search+"\uf8ff");

        FirestoreRecyclerOptions<SearchResults> response = new FirestoreRecyclerOptions.Builder<SearchResults>()
                .setQuery(query, SearchResults.class).setLifecycleOwner(this)
                .build();

        adapter = new FirestoreRecyclerAdapter<SearchResults, SearchHolder>(response) {
            @Override

            public void onBindViewHolder(final SearchHolder holder, final int position, final SearchResults model) {

                Glide.with(getContext())
                        .load(model.getPoster_path())
                        .into(holder.imageView);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getActivity(), SeriesDetails.class);
                        i.putExtra("id", model.getId());
                        startActivity(i);
                    }
                });
            }

            @Override
            public SearchHolder onCreateViewHolder(ViewGroup group, int i) {
                View view = LayoutInflater.from(group.getContext())
                        .inflate(R.layout.layout_search, group, false);

                return new SearchHolder(view);
            }

            @Override
            public void onError(FirebaseFirestoreException e) {
                Log.e("error", e.getMessage());
            }
        };
        adapter.notifyDataSetChanged();
        searchResults.setAdapter(adapter);
    }

    public class SearchHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvImage)
        ImageView imageView;

        public SearchHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}