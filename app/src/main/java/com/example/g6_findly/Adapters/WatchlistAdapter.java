package com.example.g6_findly.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.g6_findly.Models.WatchlistElement;
import com.example.g6_findly.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class WatchlistAdapter extends RecyclerView.Adapter<WatchlistAdapter.ViewHolder>  {
    private List<WatchlistElement> mData;
    private LayoutInflater mInflater;
    private WatchlistAdapter.ItemClickListener mClickListener;

    public WatchlistAdapter(Context context, List<WatchlistElement> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    @Override
    public WatchlistAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.layout_watchlist, parent, false);
        return new WatchlistAdapter.ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(WatchlistAdapter.ViewHolder holder, int position) {
        WatchlistElement element = mData.get(position);

        if(element.getPoster_path()!=null && !element.getPoster_path().isEmpty()) {
            Picasso.get().load(element.getPoster_path()).into(holder.tvImage);
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView tvImage;

        ViewHolder(View itemView) {
            super(itemView);
            tvImage = itemView.findViewById(R.id.tvImage);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // Método utilitário para obter o id do elemento
    public String getSeries_id(int id) {
        return mData.get(id).getSeries_id();
    }

    // Método utilitário para obter o id do elemento
    public String getMovie_id(int id) {
        return mData.get(id).getMovie_id();
    }

    // Método utilitário para obter a imagem do elemento
    public String getPoster_path(int id) {
        return mData.get(id).getPoster_path() ;
    }

    public void setClickListener(WatchlistAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
