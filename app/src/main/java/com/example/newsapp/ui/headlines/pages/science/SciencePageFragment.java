package com.example.newsapp.ui.headlines.pages.science;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.newsapp.DetailActivity;
import com.example.newsapp.NewsItem;
import com.example.newsapp.R;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SciencePageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SciencePageFragment extends Fragment implements SciencePageRecyclerViewAdapter.OnSciencePageFragmentListener {
    private List<NewsItem> mItems;
    private RequestQueue queue;
    private File file;
    private final String FAV_FILE = "favorite_news";
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout progressbar_view;
    private SciencePageRecyclerViewAdapter mAdapter;
    private SciencePageRecyclerViewAdapter.OnSciencePageFragmentListener itemListener;

    public SciencePageFragment() {
        // Required empty public constructor
    }


    private static Fragment newInstance() {
        return new SciencePageFragment();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        queue = Volley.newRequestQueue(requireActivity());
        file = new File(requireActivity().getFilesDir(), FAV_FILE);
        itemListener = this;
        mItems = new ArrayList<>();
        initData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_science_page, container, false);
        progressbar_view = root.findViewById(R.id.progressbar_view);
        mAdapter = new SciencePageRecyclerViewAdapter(mItems, itemListener, getActivity());
        RecyclerView recyclerView = root.findViewById(R.id.science_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(root.getContext()));
        recyclerView.setAdapter(mAdapter);
        swipeRefreshLayout = root.findViewById(R.id.swipe_refresh_science);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initData();
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(swipeRefreshLayout.isRefreshing()){
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }
                }, 1500);
            }
        });
        return root;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        queue = null;
        mItems.clear();
        mAdapter = null;
        file = null;
    }

    @Override
    public void onStart(){
        super.onStart();

    }

    @Override
    public void onResume(){
        super.onResume();
        for(int i=0; i<mItems.size(); ++i){
            String curId = mItems.get(i).id;
            Boolean ifFav = readFavs(curId);
            if(mItems.get(i).ifFav != ifFav){
                mItems.get(i).ifFav = ifFav;
                mAdapter.notifyItemChanged(i);
            }
        }
    }

    @Override
    public void onSciencePageItemClickListener(NewsItem item) {
        Intent myIntent = new Intent(getActivity(), DetailActivity.class);
        myIntent.putExtra("id", item.id);
        myIntent.putExtra("url", item.url);
        startActivity(myIntent);
    }

    @Override
    public void onSciencePageItemLongClickListener(final NewsItem item) {
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
        final String title = item.title;
        final String section = item.section;
        final String time = item.time;
        final String finalImgURL = item.image;
        final String webURL = item.url;
        if(item.ifFav)
            bookmark.setImageDrawable(requireActivity().getDrawable(R.drawable.ic_bookmark_full_24dp));
        bookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(readFavs(id)){
                    bookmark.setImageDrawable(requireActivity().getDrawable(R.drawable.baseline_bookmark_border_black_18dp));
                    removeNote(id);
                    for(int i=0; i<mItems.size(); ++i){
                        if(mItems.get(i).id.equals(id)){
                            mItems.get(i).ifFav = false;
                            mAdapter.notifyItemChanged(i);
                            break;
                        }
                    }
                    Toast.makeText(getContext(), '"' + title + "' " + "is removed from BookMarks", Toast.LENGTH_SHORT).show();
                }
                else{
                    bookmark.setImageDrawable(requireActivity().getDrawable(R.drawable.ic_bookmark_full_24dp));
                    NewsItem cur = new NewsItem(section, time, title, id, finalImgURL, webURL);
                    cur.ifFav = true;
                    addNote(cur);
                    for(int i=0; i<mItems.size(); ++i){
                        if(mItems.get(i).id.equals(id)){
                            mItems.get(i).ifFav = true;
                            mAdapter.notifyItemChanged(i);
                            break;
                        }
                    }
                    Toast.makeText(getContext(), '"' + title + "' " + "is added to BookMarks", Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialog.show();
    }

    private void initData() {
        String url = getString(R.string.sciencePageUrl);
        System.out.println("Loading...");
        // Request a string response from the provided URL.
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    mItems.clear();
                    for(int i=0; i<10; ++i){
                        String imgURL = "";
                        if(!response.has("response"))
                            continue;
                        else if(!response.getJSONObject("response").has("results"))
                            continue;
                        else if(response.getJSONObject("response").getJSONArray("results").length() <= i)
                            continue;
                        else if(!response.getJSONObject("response").getJSONArray("results").getJSONObject(i).has("webTitle"))
                            continue;
                        else if(!response.getJSONObject("response").getJSONArray("results").getJSONObject(i).has("id"))
                            continue;
                        else if(!response.getJSONObject("response").getJSONArray("results").getJSONObject(i).has("sectionName"))
                            continue;
                        else if(!response.getJSONObject("response").getJSONArray("results").getJSONObject(i).has("webPublicationDate"))
                            continue;
                        else if(!response.getJSONObject("response").getJSONArray("results").getJSONObject(i).has("webUrl"))
                            continue;
                        else if(!response.getJSONObject("response").getJSONArray("results").getJSONObject(i).has("blocks"))
                            imgURL = "";
                        else if(!response.getJSONObject("response").getJSONArray("results").getJSONObject(i).getJSONObject("blocks").has("main"))
                            imgURL = "";
                        else if(!response.getJSONObject("response").getJSONArray("results").getJSONObject(i).getJSONObject("blocks").getJSONObject("main").has("elements"))
                            imgURL = "";
                        else if(response.getJSONObject("response").getJSONArray("results").getJSONObject(i).getJSONObject("blocks").getJSONObject("main").getJSONArray("elements").length() <= 0)
                            imgURL = "";
                        else if(!response.getJSONObject("response").getJSONArray("results").getJSONObject(i).getJSONObject("blocks").getJSONObject("main").getJSONArray("elements").getJSONObject(0).has("assets"))
                            imgURL = "";
                        else if(!response.getJSONObject("response").getJSONArray("results").getJSONObject(i).getJSONObject("blocks").getJSONObject("main").getJSONArray("elements").getJSONObject(0).has("type"))
                            imgURL = "";
                        else if(response.getJSONObject("response").getJSONArray("results").getJSONObject(i).getJSONObject("blocks").getJSONObject("main").getJSONArray("elements").getJSONObject(0).getJSONArray("assets").length() <= 0)
                            imgURL = "";
                        else if(!response.getJSONObject("response").getJSONArray("results").getJSONObject(i).getJSONObject("blocks").getJSONObject("main").getJSONArray("elements").getJSONObject(0).getJSONArray("assets").getJSONObject(0).has("file"))
                            imgURL = "";
                        else if(response.getJSONObject("response").getJSONArray("results").getJSONObject(i).getJSONObject("blocks").getJSONObject("main").getJSONArray("elements").getJSONObject(0).getString("type").equals("image")){
                            imgURL = response.getJSONObject("response").getJSONArray("results").getJSONObject(i).getJSONObject("blocks").getJSONObject("main").getJSONArray("elements").getJSONObject(0).getJSONArray("assets").getJSONObject(0).getString("file");
                        }
                        if(imgURL.equals("")){
                            imgURL = "https://assets.guim.co.uk/images/eada8aa27c12fe2d5afa3a89d3fbae0d/fallback-logo.png";
                        }
                        String content = response.getJSONObject("response").getJSONArray("results").getJSONObject(i).getString("webTitle");
                        String id = response.getJSONObject("response").getJSONArray("results").getJSONObject(i).getString("id");
                        String section = response.getJSONObject("response").getJSONArray("results").getJSONObject(i).getString("sectionName");
                        String date = response.getJSONObject("response").getJSONArray("results").getJSONObject(i).getString("webPublicationDate");
                        String webUrl = response.getJSONObject("response").getJSONArray("results").getJSONObject(i).getString("webUrl");
                        Instant instant = Instant.parse(date);
                        ZoneId zoneId = ZoneId.of( "America/Los_Angeles" );
                        ZonedDateTime zdtAtLA = instant.atZone(zoneId);
                        ZonedDateTime now = ZonedDateTime.now(zoneId);
                        Duration d = Duration.between(  zdtAtLA, now );
                        String diff = d.abs().toString();
                        String time = "";

                        for(int j=0; j<diff.length(); ++j){
                            if (Character.isDigit(diff.charAt(j))) {
                                time += diff.charAt(j);
                                for(int k=j+1; k<diff.length(); ++k){
                                    if(Character.isDigit(diff.charAt(k))){
                                        time += diff.charAt(k);
                                    }
                                    else{
                                        if(diff.charAt(k) == '.'){
                                            time += "s";
                                        }
                                        else{
                                            if(diff.charAt(k) == 'H'){
                                                int temp = Integer.parseInt(time);
                                                if(temp >= 24){
                                                    temp = temp / 24;
                                                    time = String.valueOf(temp);
                                                    time += "d";
                                                }
                                                else{
                                                    time += diff.charAt(k);
                                                }
                                            }
                                            else{
                                                time += diff.charAt(k);
                                            }

                                        }
                                        break;
                                    }
                                }
                                break;
                            }
                        }
                        time = time.toLowerCase();
                        time += " ago";
                        NewsItem curItem = new NewsItem(section, time, content, id, imgURL, webUrl);
                        if(readFavs(id)){
                            curItem.ifFav = true;
                        }
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
                        final String realTime = zdtAtLA.format(formatter);
                        curItem.realTime = realTime.substring(0, 6);
                        mItems.add(curItem);
                        mAdapter.notifyItemChanged(i);
                    }

                    RecyclerView scienceView = requireActivity().findViewById(R.id.science_recycler_view);
                    if(progressbar_view.getVisibility() == View.VISIBLE){
                        progressbar_view.setVisibility(View.GONE);
                    }
                    scienceView.setVisibility(View.VISIBLE);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("This is not working " + error);
            }
        });
        queue.add(jsonObjectRequest);
    }

    private boolean readFavs(String id) {
        file = new File(requireActivity().getFilesDir(), FAV_FILE);
        List<NewsItem> list;
        try (ObjectInputStream input =
                     new ObjectInputStream(
                             new FileInputStream(file))) {
            list = (ArrayList<NewsItem>) input.readObject();
        } catch (IOException | ClassNotFoundException exception) {
            list = new ArrayList<>();
        }
        if(list.size() != 0){
            for(int i=0; i<list.size(); ++i){
                NewsItem cur = list.get(i);
                if(id.equals(cur.id)){
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    private void addNote(NewsItem item){
        file = new File(requireActivity().getFilesDir(), FAV_FILE);
        List<NewsItem> list;
        try (ObjectInputStream input =
                     new ObjectInputStream(
                             new FileInputStream(file))) {
            list = (ArrayList<NewsItem>) input.readObject();
        } catch (IOException | ClassNotFoundException exception) {
            list = new ArrayList<>();
        }
        item.ifFav = true;
        list.add(item);
        writeNotes(list);
    }

    private void removeNote(String id){
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
            if(id.equals(cur.id)){
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
}
