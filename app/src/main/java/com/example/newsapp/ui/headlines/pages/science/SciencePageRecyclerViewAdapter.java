package com.example.newsapp.ui.headlines.pages.science;

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

public class SciencePageRecyclerViewAdapter extends RecyclerView.Adapter<SciencePageRecyclerViewAdapter.ViewHolder> {
    private List<NewsItem> ITEMS;
    private Context mContext;
    private final String FAV_FILE = "favorite_news";
    private OnSciencePageFragmentListener mListener;

    SciencePageRecyclerViewAdapter(List<NewsItem> items, OnSciencePageFragmentListener listener, Context context){
        ITEMS = items;
        mListener = listener;
        mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_item_row, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.mItem = ITEMS.get(position);
        String imgURL = holder.mItem.image;
        if(!imgURL.equals("")){
            Picasso.get().load(imgURL).into(holder.mImageView);
        }

        holder.mDateSectionView.setText(ITEMS.get(position).time + " | " + ITEMS.get(position).section);
        holder.mTitleView.setText(ITEMS.get(position).title);
        if(holder.mItem.ifFav){
            holder.mButton.setBackground(mContext.getDrawable(R.drawable.ic_bookmark_full_24dp));
        }
        else{
            holder.mButton.setBackground(mContext.getDrawable(R.drawable.baseline_bookmark_border_black_18dp));
        }
        holder.mView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mListener.onSciencePageItemClickListener(holder.mItem);
            }
        });

        holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mListener.onSciencePageItemLongClickListener(holder.mItem);
                return false;
            }
        });
    }

    public interface OnSciencePageFragmentListener{
        void onSciencePageItemClickListener(NewsItem item);
        void onSciencePageItemLongClickListener(NewsItem item);
    }

    private void addNote(NewsItem item){
        File mFile = new File(mContext.getFilesDir(), FAV_FILE);
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

    @Override
    public int getItemCount() {
        return ITEMS.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        final ImageView mImageView;
        final TextView mTitleView;
        final TextView mDateSectionView;
        final Button mButton;
        NewsItem mItem;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.mView = itemView;
            mDateSectionView = itemView.findViewById(R.id.time_section);
            mTitleView = itemView.findViewById(R.id.content);
            mImageView = itemView.findViewById(R.id.homeImageView);
            mButton = itemView.findViewById(R.id.empty_bookmark);
            mButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mItem.ifFav){
                        mButton.setBackground(mContext.getDrawable(R.drawable.baseline_bookmark_border_black_18dp));
                        removeNote(mItem);
                        for(int i=0; i<ITEMS.size(); ++i){
                            if(mItem.id.equals(ITEMS.get(i).id)){
                                ITEMS.get(i).ifFav = false;
                                notifyItemChanged(i);
                            }
                        }
                        Toast.makeText(mContext, '"' + mItem.title + "' " + "is removed from BookMarks", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        mButton.setBackground(mContext.getDrawable(R.drawable.ic_bookmark_full_24dp));
                        addNote(mItem);
                        for(int i=0; i<ITEMS.size(); ++i){
                            if(mItem.id.equals(ITEMS.get(i).id)){
                                ITEMS.get(i).ifFav = true;
                                notifyItemChanged(i);
                            }
                        }
                        Toast.makeText(mContext, '"' + mItem.title + "' " + "is added to BookMarks", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
