package com.example.g6_findly.Fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.g6_findly.Activities.MovieDetails;
import com.example.g6_findly.Activities.SeriesDetails;
import com.example.g6_findly.Activities.Settings;
import com.example.g6_findly.Activities.EditProfilePicture;
import com.example.g6_findly.R;
import com.example.g6_findly.Models.WatchlistElement;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
//, WatchlistAdapter.ItemClickListener
public class ProfileFragment extends Fragment {
  @BindView(R.id.recycler_view)
  RecyclerView watchlistElements;

  private FirestoreRecyclerAdapter adapter;

  List<WatchlistElement> watchlist;
  ArrayList<Integer> categoryIds = new ArrayList<Integer>();

  ImageView imageView;
  TextView tvname;

  FirebaseFirestore db = FirebaseFirestore.getInstance();
  public ProfileFragment() {
    // Required empty public constructor
  }

  public static ProfileFragment newInstance(String param1, String param2) {
    ProfileFragment fragment = new ProfileFragment();
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

  }
  private void init(){
    GridLayoutManager manager = new GridLayoutManager(getContext(), 3, GridLayoutManager.VERTICAL, false);
   watchlistElements.setLayoutManager(manager);
    db = FirebaseFirestore.getInstance();
  }

  private void getWatchlist() {
    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    Query query = db.collection("watchlist").whereEqualTo("user_id", user.getUid());

      db.collection("watchlist").whereEqualTo("user_id", user.getUid()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
          @Override
          public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
              Log.d("TAG", "data: "+ queryDocumentSnapshots);
          }
      });

     FirestoreRecyclerOptions<WatchlistElement> response = new FirestoreRecyclerOptions.Builder<WatchlistElement>()
            .setQuery(query, WatchlistElement.class)
            .build();

    adapter = new FirestoreRecyclerAdapter<WatchlistElement, WatchlistHolder>(response) {
      @Override

      public void onBindViewHolder(final WatchlistHolder holder, final int position, final WatchlistElement model) {
       // progressBar.setVisibility(View.GONE);
       Glide.with(getContext())
               .load(model.getPoster_path())
                .into(holder.imageView);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              if (model.getMovie_id()!= null){
                  Intent i = new Intent(getActivity(), MovieDetails.class);
                  i.putExtra("movie_id", model.getMovie_id());
                  startActivity(i);
              } else {
                  Intent i = new Intent(getActivity(), SeriesDetails.class);
                  i.putExtra("id", model.getSeries_id());
                  startActivity(i);
              }

          }
        });
      }

      @Override
      public WatchlistHolder onCreateViewHolder(ViewGroup group, int i) {
        View view = LayoutInflater.from(group.getContext())
                .inflate(R.layout.layout_watchlist, group, false);

        return new WatchlistHolder(view);
      }

      @Override
      public void onError(FirebaseFirestoreException e) {
        Log.e("error", e.getMessage());
      }
    };

    adapter.notifyDataSetChanged();
    watchlistElements.setAdapter(adapter);
  }


  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

   final View view =inflater.inflate(R.layout.fragment_profile, container, false);
    ImageView settings = view.findViewById(R.id.settings);
    settings.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        Intent i = new Intent(getActivity(), Settings.class);
        startActivity(i);
      }
    });

    ImageView bottom_sheets = view.findViewById(R.id.showBottomSheet);
    bottom_sheets.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        Intent i = new Intent(getActivity(), EditProfilePicture.class);
        startActivity(i);
      }
    });

    imageView=  view.findViewById(R.id.profile_picture);
    tvname =  view.findViewById(R.id.name);

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    if (user != null) {
      String name = user.getDisplayName();
      tvname.setText(name);
      if (user.getPhotoUrl() != null) {
        Picasso.get()
                .load(user.getPhotoUrl())
                .resize(100, 100)
                .centerCrop()
                .into(imageView);
      }
    }
    ButterKnife.bind(this, view);
    init();
    getWatchlist();
    // Inflate the layout for this fragment
    return view;
  }

  public class WatchlistHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.tvImage)
    ImageView imageView;


    public WatchlistHolder(View itemView) {
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