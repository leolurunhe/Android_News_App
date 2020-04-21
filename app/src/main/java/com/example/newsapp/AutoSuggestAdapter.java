package com.example.newsapp;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AutoSuggestAdapter extends ArrayAdapter implements Filterable {
    private List<String> mListData;
    AutoSuggestAdapter(@NonNull Context context, int resource) {
        super(context, resource);
        mListData = new ArrayList<>();
    }
    public void setData(List<String> list) {
        mListData.clear();
        mListData.addAll(list);
    }
    @Override
    public int getCount() {
        return mListData.size();
    }
    @Nullable
    @Override
    public String getItem(int position) {
        return mListData.get(position);
    }
    /**
     * Used to Return the full object directly from adapter.
     */
    public String getObject(int position) {
        return mListData.get(position);
    }
    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    filterResults.values = mListData;
                    filterResults.count = mListData.size();
                }
                return filterResults;
            }
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && (results.count > 0)) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
    }
}
