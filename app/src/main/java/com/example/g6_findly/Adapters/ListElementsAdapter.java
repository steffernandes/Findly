package com.example.g6_findly.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.g6_findly.Activities.ListElements;
import com.example.g6_findly.Models.ListElementsModel;
import com.example.g6_findly.Models.SearchResults;
import com.example.g6_findly.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ListElementsAdapter extends RecyclerView.Adapter<ListElementsAdapter.ViewHolder> {
    private List<ListElementsModel> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    public ListElementsAdapter(Context context, List<ListElementsModel> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.layout_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ListElementsAdapter.ViewHolder holder, int position) {
        ListElementsModel results = mData.get(position);
        holder.tvTitle.setText(results.getName());
        holder.tvData.setText(results.getRelease_data());
        holder.tvOverview.setText(results.getOverview());
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
        TextView tvData;
        TextView tvOverview;
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

    String getName(int id) {
        return mData.get(id).getName();
    }

    public String  getMovie_id(int id) {
        return mData.get(id).getMovie_id();
    }

    public String  getId(int id) {
        return mData.get(id).getId();
    }

    public String getPoster_path(int id) {
        return mData.get(id).getPoster_path() ;
    }

    public String getRelease_data(int id) {
        return mData.get(id).getRelease_data() ;
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}