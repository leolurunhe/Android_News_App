package com.example.newsapp.ui.bookmarks;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsapp.DetailActivity;
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
import java.util.Objects;

public class BookmarksFragment extends Fragment implements BookMarksFragmentRecyclerViewAdapter.OnBookmarksPageClickListener {
    private TextView mTextView;
    private final String FAV_FILE = "favorite_news";
    private File file;
    private List<NewsItem> mItems;
    private NestedScrollView mNestedScrollView;
    private BookMarksFragmentRecyclerViewAdapter mAdapter;
    private BookMarksFragmentRecyclerViewAdapter.OnBookmarksPageClickListener mListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        file = new File(requireActivity().getFilesDir(), FAV_FILE);
        mListener = this;
        mItems = new ArrayList<>();
        try (ObjectInputStream input =
                     new ObjectInputStream(
                             new FileInputStream(file))) {
            mItems = (ArrayList<NewsItem>) input.readObject();
        } catch (IOException | ClassNotFoundException exception) {
            mItems = new ArrayList<>();
        }
        for(int i=0; i<mItems.size(); ++i){
            mItems.get(i).ifFav = true;
        }
        writeNotes(mItems);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_bookmarks, container, false);
        mTextView = root.findViewById(R.id.text_no_bookmarks);
        RecyclerView mRecyclerView = root.findViewById(R.id.bookmarks_recycler_view);
        mNestedScrollView = root.findViewById(R.id.bookmarks_nested_scroll_view);
        mAdapter = new BookMarksFragmentRecyclerViewAdapter(mItems, mListener, getActivity(), mNestedScrollView, mTextView);
        mRecyclerView.setLayoutManager(new GridLayoutManager(root.getContext(), 2));
        mRecyclerView.setAdapter(mAdapter);
        if(ifBookmarksEmpty()){
            mTextView.setVisibility(View.VISIBLE);
            mNestedScrollView.setVisibility(View.GONE);
        }
        else{
            mTextView.setVisibility(View.GONE);
            mNestedScrollView.setVisibility(View.VISIBLE);
        }

        return root;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onResume(){
        super.onResume();
        file = new File(requireActivity().getFilesDir(), FAV_FILE);
        List<NewsItem> list;
        try (ObjectInputStream input =
                     new ObjectInputStream(
                             new FileInputStream(file))) {
            list = (ArrayList<NewsItem>) input.readObject();
        } catch (IOException | ClassNotFoundException exception) {
            list = new ArrayList<>();
        }
        mItems.clear();
        mItems.addAll(list);
        mAdapter.notifyDataSetChanged();
        if(mItems.isEmpty()){
            mNestedScrollView.setVisibility(View.GONE);
            mTextView.setVisibility(View.VISIBLE);
        }
        else{
            mNestedScrollView.setVisibility(View.VISIBLE);
            mTextView.setVisibility(View.GONE);
        }
    }

    private boolean ifBookmarksEmpty(){
        return mItems.isEmpty();
    }



    private void removeNote(NewsItem item){
        file = new File(requireActivity().getFilesDir(), FAV_FILE);
        List<NewsItem> list;
        try (ObjectInputStream input =
                     new ObjectInputStream(
                             new FileInputStream(file))) {
            list = (ArrayList<NewsItem>) input.readObject();
        } catch (IOException | ClassNotFoundException exception) {
            list = new ArrayList<>();
        }
        for(int i=0; i<list.size(); ++i){
            NewsItem cur = list.get(i);
            if(item.id.equals(cur.id)){
                list.remove(cur);
                break;
            }
        }
        writeNotes(list);
    }

    private void writeNotes(List<NewsItem> list) {
        file = new File(requireActivity().getFilesDir(), FAV_FILE);
        try (ObjectOutputStream output =
                     new ObjectOutputStream(
                             new FileOutputStream(file))) {
            if(list.size() == 0){
                list = new ArrayList<>();
            }
            output.writeObject(list);
        } catch (IOException exception) {
            // cause runtime error
            throw new IllegalStateException(
                    "something bad happened");
        }
    }

    @Override
    public void onBookmarksItemClickListener(NewsItem item) {
        Intent myIntent = new Intent(getActivity(), DetailActivity.class);
        myIntent.putExtra("id", item.id);
        myIntent.putExtra("url", item.url);
        startActivity(myIntent);
    }

    @Override
    public void onBookmarksItemLongClickListener(final NewsItem item) {
        Dialog dialog = new Dialog(requireActivity());
        dialog.setContentView(R.layout.custom_dialog);
        Uri imgUri = Uri.parse(item.image);
        ImageView dialogImg = dialog.findViewById(R.id.dialog_image);
        if(!item.image.equals(""))
            Picasso.get().load(imgUri).into(dialogImg);
        TextView dialogTitle = dialog.findViewById(R.id.dialog_title);
        dialogTitle.setText(item.title);
        final ImageButton bookmark = dialog.findViewById(R.id.dialog_bookmark);
        ImageButton twitter = dialog.findViewById(R.id.dialog_twitter);
        twitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = item.url;
                String hashTag = "CSCI571NewsSearch";
                Intent intent = new Intent();
                String text = "Check out this Link:\n";
                intent.setData(Uri.parse(getString(R.string.twitterShareUrl) + "?text=" + text + "&url="+ url +"&hashtags="+ hashTag));
                startActivity(intent);
            }
        });
        final String id = item.id;
        bookmark.setImageDrawable(requireActivity().getDrawable(R.drawable.ic_bookmark_full_24dp));
        bookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bookmark.setImageDrawable(requireActivity().getDrawable(R.drawable.baseline_bookmark_border_black_18dp));
                removeNote(item);
                int position = 0;
                for(int i=0; i<mItems.size(); ++i){
                    if(mItems.get(i).id.equals(id)){
                        position = i;
                        break;
                    }
                }
                mItems.remove(item);
                mAdapter.notifyItemRemoved(position);
                if(mItems.isEmpty()){
                    mTextView.setVisibility(View.VISIBLE);
                    mNestedScrollView.setVisibility(View.GONE);
                }
                Toast.makeText(getContext(), '"' + item.title + "' " + "is removed from BookMarks", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }
}
