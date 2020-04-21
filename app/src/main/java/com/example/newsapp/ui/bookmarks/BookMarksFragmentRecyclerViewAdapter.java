package com.example.newsapp.ui.bookmarks;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsapp.NewsItem;
import com.example.newsapp.R;
import com.squareup.picasso.Picasso;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class BookMarksFragmentRecyclerViewAdapter extends RecyclerView.Adapter<BookMarksFragmentRecyclerViewAdapter.ViewHolder> {
    private List<NewsItem> ITEMS;
    private Context mContext;
    private final String FAV_FILE = "favorite_news";
    private OnBookmarksPageClickListener mListener;
    private NestedScrollView mNestedScrollView;
    private TextView mTextView;

    BookMarksFragmentRecyclerViewAdapter(List<NewsItem> items, OnBookmarksPageClickListener listener, Context context, NestedScrollView nestedScrollView, TextView textView){
        ITEMS = items;
        mListener = listener;
        mContext = context;
        mNestedScrollView = nestedScrollView;
        mTextView = textView;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bookmarks_item_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.mItem = ITEMS.get(position);
        String imgURL = holder.mItem.image;
        if(!imgURL.equals("")){
            Picasso.get().load(imgURL).into(holder.mImageView);
        }
        holder.mDateSection.setText(ITEMS.get(position).realTime + " | " + ITEMS.get(position).section);
        holder.mTitle.setText(ITEMS.get(position).title);
        holder.mView.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                mListener.onBookmarksItemClickListener(holder.mItem);
            }
        });
        holder.mView.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {
                mListener.onBookmarksItemLongClickListener(holder.mItem);
                return false;
            }
        });
    }


    @Override
    public int getItemCount() {
        return ITEMS.size();
    }

    public interface OnBookmarksPageClickListener{
        void onBookmarksItemClickListener(NewsItem item);
        void onBookmarksItemLongClickListener(NewsItem item);
    }

    private void removeNote(NewsItem item){
        File mFile = new File(mContext.getFilesDir(), FAV_FILE);
        List<NewsItem> list;
        try (ObjectInputStream input =
                     new ObjectInputStream(
                             new FileInputStream(mFile))) {
            list = (ArrayList<NewsItem>) input.readObject();
        } catch (IOException | ClassNotFoundException exception) {
            list = new ArrayList<>();
        }
        for(int i=0; i<list.size(); ++i){
            NewsItem cur = list.get(i);
            if(item.id.equals(cur.id)){
                list.remove(i);
                break;
            }
        }
        writeNotes(list);
    }

    private void writeNotes(List<NewsItem> list) {
        File file;
        file = new File(mContext.getFilesDir(), FAV_FILE);
        try (ObjectOutputStream output =
                     new ObjectOutputStream(
                             new FileOutputStream(file))) {
            if(list.size() == 0){
                list = new ArrayList<>();
            }
            output.writeObject(list);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "something bad happened");
        }

    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        NewsItem mItem;
        View mView;
        ImageView mImageView;
        TextView mTitle;
        TextView mDateSection;
        ImageButton mButton;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            mImageView = itemView.findViewById(R.id.bookmarks_image);
            mTitle = itemView.findViewById(R.id.bookmarks_title);
            mDateSection = itemView.findViewById(R.id.bookmarks_date_section);
            mButton = itemView.findViewById(R.id.bookmarks_button);
            mButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeNote(mItem);
                    ITEMS.remove(mItem);
                    notifyDataSetChanged();
                    if(ITEMS.isEmpty()){
                        mTextView.setVisibility(View.VISIBLE);
                        mNestedScrollView.setVisibility(View.GONE);
                    }

                    Toast.makeText(mContext, '"' + mItem.title + "' " + "is removed from BookMarks", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
