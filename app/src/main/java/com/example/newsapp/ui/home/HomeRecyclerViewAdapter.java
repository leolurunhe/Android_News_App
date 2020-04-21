package com.example.newsapp.ui.home;


import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

/**
 * TODO: Replace the implementation with code for your data type.
 */
public class HomeRecyclerViewAdapter extends RecyclerView.Adapter<HomeRecyclerViewAdapter.ViewHolder> {

    private List<NewsItem> mValues;
    //private final HomeFragment.OnHomeFragmentInteractionListener mListener;
    private File mFile;
    private final Context mContext;
    private final onHomeItemListener mItemListener;
    private final String FAV_FILE = "favorite_news";

    HomeRecyclerViewAdapter(List<NewsItem> items, File file, Context context, onHomeItemListener itemListener) {
        mValues = items;
        mFile = file;
        mContext = context;
        mItemListener = itemListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.news_item_row, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.mItem = mValues.get(position);
        String imgURL = holder.mItem.image;
        if(!imgURL.equals("")){
            Picasso.get().load(imgURL).into(holder.mImageView);
        }

        holder.mTimeSectionView.setText(mValues.get(position).time + " | " + mValues.get(position).section);
        holder.mContentView.setText(mValues.get(position).title);
        if(holder.mItem.ifFav){
            holder.mEmptyBookmark.setBackground(mContext.getDrawable(R.drawable.ic_bookmark_full_24dp));
        }
        else{
            holder.mEmptyBookmark.setBackground(mContext.getDrawable(R.drawable.baseline_bookmark_border_black_18dp));
        }

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mItemListener.onHomeItemClickListener(holder.mItem);
            }
        });

        holder.mView.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v){
                mItemListener.onHomeItemLongClickListener(holder.mItem);
                return false;
            }
        });

    }

    private void addNote(NewsItem item){
        mFile = new File(mContext.getFilesDir(), FAV_FILE);
        List<NewsItem> list;
        try (ObjectInputStream input =
                     new ObjectInputStream(
                             new FileInputStream(mFile))) {
            list = (ArrayList<NewsItem>) input.readObject();
        } catch (IOException | ClassNotFoundException exception) {
            list = new ArrayList<>();
        }
        item.ifFav = true;
        list.add(item);
        writeNotes(list);
    }

    private void removeNote(NewsItem item){
        mFile = new File(mContext.getFilesDir(), FAV_FILE);
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
                //System.out.println(i);
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
            //System.out.println("write" + list.get(0).id);
        } catch (IOException exception) {
            // cause runtime error
            throw new IllegalStateException(
                    "something bad happened");
        }

    }

    public interface onHomeItemListener{
        void onHomeItemClickListener(NewsItem item);
        void onHomeItemLongClickListener(NewsItem item);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        final TextView mTimeSectionView;
        final TextView mContentView;
        final Button mEmptyBookmark;
        final ImageView mImageView;
        public NewsItem mItem;

        @SuppressLint("ClickableViewAccessibility")
        public ViewHolder(View view) {
            super(view);
            mView = view;
            mTimeSectionView = view.findViewById(R.id.time_section);
            mContentView = view.findViewById(R.id.content);
            mImageView = view.findViewById(R.id.homeImageView);
            mEmptyBookmark = view.findViewById(R.id.empty_bookmark);
            mEmptyBookmark.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mItem.ifFav){
                        mEmptyBookmark.setBackground(mContext.getDrawable(R.drawable.baseline_bookmark_border_black_18dp));
                        removeNote(mItem);

                        for(int i=0; i<mValues.size(); ++i){
                            if(mItem.id.equals(mValues.get(i).id)){
                                mValues.get(i).ifFav = false;
                                notifyItemChanged(i);
                            }
                        }
                        Toast.makeText(mContext, '"' + mItem.title + "' " + "is removed from BookMarks", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        mEmptyBookmark.setBackground(mContext.getDrawable(R.drawable.ic_bookmark_full_24dp));
                        addNote(mItem);
                        for(int i=0; i<mValues.size(); ++i){
                            if(mItem.id.equals(mValues.get(i).id)){
                                mValues.get(i).ifFav = true;
                                notifyItemChanged(i);
                            }
                        }
                        Toast.makeText(mContext, '"' + mItem.title + "' " + "is added to BookMarks", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }


        @NonNull
        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }



    }
}
