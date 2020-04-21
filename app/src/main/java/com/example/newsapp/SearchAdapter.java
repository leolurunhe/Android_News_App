package com.example.newsapp;

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

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.searchViewHolder> {
    private final List<NewsItem> mValues;
    private final SearchItemClickListener mListener;
    private File mFile;
    private final Context mContext;

    SearchAdapter(List<NewsItem> mValues, SearchItemClickListener mListener, File file, Context context) {
        this.mValues = mValues;
        this.mListener = mListener;
        this.mFile = file;
        this.mContext = context;
    }

    @NonNull
    @Override
    public searchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_item_row, parent, false);

        return new searchViewHolder(root);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final searchViewHolder holder, int position) {
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
                if (null != mListener) {
                    mListener.onSearchItemClick(holder.mItem);

                }
            }
        });

        holder.mView.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v){
                if(null != mListener){
                    mListener.onSearchItemLongClick(holder.mItem);
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {

        return mValues.size();
    }


    public interface SearchItemClickListener{
        void onSearchItemClick(NewsItem item);
        void onSearchItemLongClick(NewsItem item);
    }

    private void addNote(NewsItem item){
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
        File file = mFile;
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

    public class searchViewHolder extends RecyclerView.ViewHolder{
        public final View mView;
        final TextView mTimeSectionView;
        final TextView mContentView;
        Button mEmptyBookmark;
        ImageView mImageView;
        public NewsItem mItem;

        @SuppressLint("ClickableViewAccessibility")
        searchViewHolder(View view) {
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
                        Toast.makeText(mContext, '"' + mItem.title + "' " + "is added from BookMarks", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }






}
