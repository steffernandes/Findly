package com.example.g6_findly.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.g6_findly.Models.Category;
import com.example.g6_findly.Models.SearchResults;
import com.example.g6_findly.Models.WatchlistElement;
import com.example.g6_findly.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {
    private List<SearchResults> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    public SearchAdapter(Context context, List<SearchResults> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.layout_search, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SearchAdapter.ViewHolder holder, int position) {
        SearchResults results = mData.get(position);

        if(results.getPoster_path()!=null && !results.getPoster_path().isEmpty()) {
            Picasso.get().load(results.getPoster_path()).into(holder.tvImage);
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tvTitle;
        ImageView tvImage;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvImage = itemView.findViewById(R.id.tvImage);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    public String getId(int id) {
        return mData.get(id).getId();
    }

    public String  getMovie_id(int id) {
        return mData.get(id).getMovie_id();
    }

    public String getPoster_path(int id) {
        return mData.get(id).getPoster_path() ;
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}