package com.example.g6_findly.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.g6_findly.Activities.MovieDetails;
import com.example.g6_findly.Activities.TvShowDelete;
import com.example.g6_findly.Models.ListElementsModel;
import com.example.g6_findly.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DeleteSeriesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DeleteSeriesFragment extends Fragment {

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



    public DeleteSeriesFragment() {
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
    public static DeleteSeriesFragment newInstance(String param1, String param2) {
        DeleteSeriesFragment fragment = new DeleteSeriesFragment();
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
        View view= inflater.inflate(R.layout.fragment_delete_series, container, false);
        ButterKnife.bind(this, view);
        init();
        listTvShows();
        return view;
    }

    private void init(){
        LinearLayoutManager manager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, true);
        ListElements.setLayoutManager(manager);
        db = FirebaseFirestore.getInstance();
    }

    public void listTvShows() {
        Query query =db.collection("tv_shows_details").limit(20);

        FirestoreRecyclerOptions<ListElementsModel> response = new FirestoreRecyclerOptions.Builder<ListElementsModel>()
                .setQuery(query, ListElementsModel.class).setLifecycleOwner(this)
                .build();

        adapter = new FirestoreRecyclerAdapter<ListElementsModel, DeleteHolder>(response) {
            @Override

            public void onBindViewHolder(final DeleteHolder holder, final int position, final ListElementsModel model) {
                holder.title.setText(model.getName());
                holder.overview.setText(model.getOverview());
                holder.date.setText(model.getRelease_data());
                Glide.with(getContext())
                        .load(model.getPoster_path())
                        .into(holder.imageView);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("TAG", "image: clicked "+ model.getId());
                        Intent i = new Intent(getActivity(), TvShowDelete.class);
                        i.putExtra("id", model.getId());
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