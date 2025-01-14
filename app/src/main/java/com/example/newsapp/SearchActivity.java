package com.example.newsapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
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

public class SearchActivity extends AppCompatActivity implements SearchAdapter.SearchItemClickListener{
    private final String FAV_FILE = "favorite_news";
    RecyclerView mRecyclerView;
    List<NewsItem> resultList;
    SearchAdapter mAdapter;
    SearchAdapter.SearchItemClickListener mListener;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    Context context = this;
    String keyword;
    private RequestQueue queue;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = findViewById(R.id.search_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        queue = Volley.newRequestQueue(this);
        Intent myIntent = getIntent();
        keyword = myIntent.getStringExtra("keyword");
        TextView title = toolbar.findViewById(R.id.search_results_title);
        title.setText(getString(R.string.search_results) + " " + keyword);
        mListener = this;
        resultList = new ArrayList<NewsItem>();
        File file = new File(getFilesDir(), FAV_FILE);
        mAdapter = new SearchAdapter(resultList, mListener, file, this);
        mRecyclerView = findViewById(R.id.search_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);
        initData(this.keyword);
        mSwipeRefreshLayout = findViewById(R.id.swiperefresh_search);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initData(keyword);
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(mSwipeRefreshLayout.isRefreshing()) {
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    }
                }, 1500);
            }
        });
    }

    @Override
    protected void onStart(){
        super.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();
        for(int i=0; i<resultList.size(); ++i){
            boolean curBool = readFavs(resultList.get(i).id);
            if(curBool != resultList.get(i).ifFav){
                resultList.get(i).ifFav = curBool;
                mAdapter.notifyItemChanged(i);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void addNote(NewsItem item){
        File file = new File(getFilesDir(), FAV_FILE);
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
        File file = new File(getFilesDir(), FAV_FILE);
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
        File file = new File(getFilesDir(), FAV_FILE);
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

    private boolean readFavs(String id) {
        File file = new File(getFilesDir(), FAV_FILE);
        List<NewsItem> list;
        try (ObjectInputStream input =
                     new ObjectInputStream(
                             new FileInputStream(file))) {
            list = (ArrayList<NewsItem>) input.readObject();
        } catch (IOException | ClassNotFoundException exception) {
            list = new ArrayList<>();
        }
        //System.out.println("listsize:" + list.size());

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


    private void initData(String keyword) {
        resultList.clear();

        String url = getString(R.string.searchUrl);
        url += keyword + "&news=Guardian";

        // Request a string response from the provided URL.
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                // Display the first 500 characters of the response string.
                //textView.setText("Response is: "+ response.substring(0,500));
                try {
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

                        resultList.add(curItem);
                        mAdapter.notifyItemChanged(i);
                    }


                    LinearLayout progressbar_view = findViewById(R.id.progressbar_view_search);
                    if(progressbar_view.getVisibility() == View.VISIBLE){
                        progressbar_view.setVisibility(View.GONE);
                        RecyclerView searchView = findViewById(R.id.search_recycler_view);
                        searchView.setVisibility(View.VISIBLE);
                    }
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


    @Override
    public void onSearchItemClick(NewsItem item) {
        Intent myIntent = new Intent(this, DetailActivity.class);
        myIntent.putExtra("id", item.id);
        myIntent.putExtra("url", item.url);
        startActivity(myIntent);
    }

    @Override
    public void onSearchItemLongClick(final NewsItem item) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.custom_dialog);
        Uri imgUri = Uri.parse(item.image);
        ImageView dialogImg = dialog.findViewById(R.id.dialog_image);
        if(!item.image.equals(""))
            Picasso.get().load(imgUri).into(dialogImg);
        TextView dialogTitle = dialog.findViewById(R.id.dialog_title);
        dialogTitle.setText(item.title);
        final ImageButton bookmark = dialog.findViewById(R.id.dialog_bookmark);
        if(item.ifFav){
            bookmark.setImageDrawable(getDrawable(R.drawable.ic_bookmark_full_24dp));
        }
        bookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = item.id;
                if(readFavs(id)){
                    bookmark.setImageDrawable(getDrawable(R.drawable.baseline_bookmark_border_black_18dp));
                    removeNote(id);

                    for(int i=0; i<resultList.size(); ++i){
                        if(resultList.get(i).id.equals(id)){
                            resultList.get(i).ifFav = false;
                            mAdapter.notifyItemChanged(i);
                            break;
                        }
                    }

                    Toast.makeText(context, '"' + item.title + "' " + "is removed from BookMarks", Toast.LENGTH_SHORT).show();
                }
                else{
                    bookmark.setImageDrawable(getDrawable(R.drawable.ic_bookmark_full_24dp));
                    item.ifFav = true;
                    addNote(item);
                    for(int i=0; i<resultList.size(); ++i){
                        if(resultList.get(i).id.equals(id)){
                            resultList.get(i).ifFav = true;
                            mAdapter.notifyItemChanged(i);
                            break;
                        }
                    }
                    Toast.makeText(context, '"' + item.title + "' " + "is added to BookMarks", Toast.LENGTH_SHORT).show();
                }
            }
        });
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



        dialog.show();
    }
}
