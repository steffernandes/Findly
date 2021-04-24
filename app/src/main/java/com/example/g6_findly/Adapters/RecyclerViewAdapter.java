package com.example.g6_findly.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.g6_findly.Activities.ChooseMovieCategories;
import com.example.g6_findly.Models.Category;
import com.example.g6_findly.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private List<Category> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    public RecyclerViewAdapter(Context context, List<Category> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.layout_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Category category = mData.get(position);
        holder.tvName.setText(category.getName());
            Picasso.get()
                    .load(category.getImage()).resize(100, 100)
                    .centerCrop()
                    .into(holder.tvImage);
    }


    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tvName;
        ImageView tvImage;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvImage = itemView.findViewById(R.id.tvImage);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());

        }
    }

    // Método utilitário para obter o nome da categoria
    String getName(int id) {
        return mData.get(id).getName();
    }

    // Método utilitário para obter o id da categoria
    public int getId(int id) {
        return mData.get(id).getId();
    }

    // Método utilitário para obter a imagem categoria
    public int getImage(int id) {
        return mData.get(id).getImage() ;
    }

    //Método utilitário para mudar a imagem categoria
    public void setImage(int id, int image) {
         mData.get(id).setImage(image) ;
          return;
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);

    }
}