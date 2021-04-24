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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.g6_findly.Activities.Login;
import com.example.g6_findly.Activities.MovieDelete;
import com.example.g6_findly.Activities.MovieDetails;
import com.example.g6_findly.Models.ListElementsModel;
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
 * Use the {@link DeleteMovieFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DeleteMovieFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    @BindView(R.id.recycler_view)
    RecyclerView ListElements;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private FirestoreRecyclerAdapter adapter;

    public DeleteMovieFragment() {
        // Required empty public constructor
    }

    public static DeleteMovieFragment newInstance(String param1, String param2) {
        DeleteMovieFragment fragment = new DeleteMovieFragment();
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
        View view= inflater.inflate(R.layout.fragment_delete_movies, container, false);
        ButterKnife.bind(this, view);
        init();
        listMovies();
        return view;
    }

    private void init(){
        LinearLayoutManager manager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, true);
        ListElements.setLayoutManager(manager);
        db = FirebaseFirestore.getInstance();
    }

    public void listMovies() {
        Query query =db.collection("movie_details").limit(20);

        FirestoreRecyclerOptions<ListElementsModel> response = new FirestoreRecyclerOptions.Builder<ListElementsModel>()
                .setQuery(query, ListElementsModel.class).setLifecycleOwner(this)
                .build();

        adapter = new FirestoreRecyclerAdapter<ListElementsModel, DeleteHolder>(response) {
            @Override

            public void onBindViewHolder(final DeleteHolder holder, final int position, final ListElementsModel model) {
                // progressBar.setVisibility(View.GONE);
                holder.title.setText(model.getName());
                holder.overview.setText(model.getOverview());
                holder.date.setText(model.getRelease_data());
                Glide.with(getContext())
                        .load(model.getPoster_path())
                        .into(holder.imageView);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getActivity(), MovieDelete.class);
                        i.putExtra("movie_id", model.getMovie_id());
                        startActivity(i);
                    }
                });
            }

            @Override
            public DeleteHolder onCreateViewHolder(ViewGroup group, int i) {
                View view = LayoutInflater.from(group.getContext())
                        .inflate(R.layout.layout_list, group, false);

                return new DeleteHolder(view);
            }

            @Override
            public void onError(FirebaseFirestoreException e) {
                Log.e("error", e.getMessage());
            }
        };
        adapter.notifyDataSetChanged();
        ListElements.setAdapter(adapter);
    }



    public class DeleteHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvImage)
        ImageView imageView;

        @BindView(R.id.tvTitle)
        TextView title;

        @BindView(R.id.tvDate)
        TextView date;

        @BindView(R.id.tvOverview)
        TextView overview;


        public DeleteHolder(View itemView) {
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