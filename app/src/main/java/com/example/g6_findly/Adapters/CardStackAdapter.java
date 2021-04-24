package com.example.g6_findly.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.g6_findly.Models.PosterModel;
import com.example.g6_findly.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CardStackAdapter extends RecyclerView.Adapter<CardStackAdapter.ViewHolder> {

    private List<PosterModel> items;
    public CardStackAdapter(List<PosterModel> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.movie_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setData(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        ImageView image;
         TextView tvName;
         TextView tvReleaseDate;
         TextView tvRating;
         TextView tvDescription;
         TextView tvStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.item_image);
            tvName = itemView.findViewById(R.id.movie_title);
            tvReleaseDate = itemView.findViewById(R.id.release_date);
            tvRating = itemView.findViewById(R.id.rating);
            tvDescription = itemView.findViewById(R.id.description);
            tvStatus = itemView.findViewById(R.id.status);

        }
        public void setData(PosterModel data) {
           Picasso.get()
                    .load(data.getImage())
                    .into(image);

           tvName.setText(data.getName());
           tvReleaseDate.setText(data.getDate());
            tvRating.setText(data.getRating()+ " /10");
            tvDescription.setText(data.getOverview());
            tvStatus.setText(data.getStatus());

        }
    }

    public List<PosterModel> getItems() {
        return items;
    }

    public void setItems(List<PosterModel> items) {
        this.items = items;
    }
}
