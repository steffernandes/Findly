package com.example.g6_findly;

import androidx.recyclerview.widget.DiffUtil;

import com.example.g6_findly.Models.PosterModel;

import java.util.List;

public class CardStackCallback extends DiffUtil.Callback {

    private List<PosterModel> oldList, newList;

    public CardStackCallback(List<PosterModel> old, List<PosterModel> newList) {
        this.oldList = old;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).getImage() == newList.get(newItemPosition).getImage();
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition) == newList.get(newItemPosition);
    }
}
