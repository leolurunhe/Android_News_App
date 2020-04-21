package com.example.newsapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.text.HtmlCompat;

import android.view.View;
import android.widget.Button;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DetailActivity extends AppCompatActivity {
    private String id;
    private String url;
    private final String FAV_FILE = "favorite_news";
    protected Context context = this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        Intent myIntent = getIntent();
        this.id = myIntent.getStringExtra("id");
        this.url = myIntent.getStringExtra("url");
        Button fullPage = findViewById(R.id.button_view_full_article);
        fullPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setData(Uri.parse(url));
                intent.setAction(Intent.ACTION_VIEW);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        initData();

    }

    @Override
    protected void onResume(){
        super.onResume();

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void addNote(NewsItem cur){
        File file = new File(getFilesDir(), FAV_FILE);
        List<NewsItem> list;
        try (ObjectInputStream input =
                     new ObjectInputStream(
                             new FileInputStream(file))) {
            list = (ArrayList<NewsItem>) input.readObject();
        } catch (IOException | ClassNotFoundException exception) {
            list = new ArrayList<>();
        }
        cur.ifFav = true;
        list.add(cur);
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

    private void initData() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url =getString(R.string.detailPageUrl);
        url += this.id;
        url += "&news=Guardian";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    String imgURL = "";
                    if(!response.getJSONObject("response").has("content"))
                        imgURL = "";
                    else if(!response.getJSONObject("response").getJSONObject("content").has("blocks"))
                        imgURL = "";
                    else if(!response.getJSONObject("response").getJSONObject("content").getJSONObject("blocks").has("main"))
                        imgURL = "";
                    else if(!response.getJSONObject("response").getJSONObject("content").getJSONObject("blocks").getJSONObject("main").has("elements"))
                        imgURL = "";
                    else if(response.getJSONObject("response").getJSONObject("content").getJSONObject("blocks").getJSONObject("main").getJSONArray("elements").length() <= 0)
                        imgURL = "";
                    else if(!response.getJSONObject("response").getJSONObject("content").getJSONObject("blocks").getJSONObject("main").getJSONArray("elements").getJSONObject(0).has("assets"))
                        imgURL = "";
                    else if(!response.getJSONObject("response").getJSONObject("content").getJSONObject("blocks").getJSONObject("main").getJSONArray("elements").getJSONObject(0).has("type"))
                        imgURL = "";
                    else if(response.getJSONObject("response").getJSONObject("content").getJSONObject("blocks").getJSONObject("main").getJSONArray("elements").getJSONObject(0).getJSONArray("assets").length() <= 0)
                        imgURL = "";
                    else if(!response.getJSONObject("response").getJSONObject("content").getJSONObject("blocks").getJSONObject("main").getJSONArray("elements").getJSONObject(0).getJSONArray("assets").getJSONObject(0).has("file"))
                        imgURL = "";
                    else if(response.getJSONObject("response").getJSONObject("content").getJSONObject("blocks").getJSONObject("main").getJSONArray("elements").getJSONObject(0).getString("type").equals("image")){
                        imgURL = response.getJSONObject("response").getJSONObject("content").getJSONObject("blocks").getJSONObject("main").getJSONArray("elements").getJSONObject(0).getJSONArray("assets").getJSONObject(0).getString("file");
                    }
                    if(imgURL.equals("")){
                        imgURL = "https://assets.guim.co.uk/images/eada8aa27c12fe2d5afa3a89d3fbae0d/fallback-logo.png";
                    }
                    final String content = response.getJSONObject("response").getJSONObject("content").getJSONObject("blocks").getJSONArray("body").getJSONObject(0).getString("bodyHtml");
                    final String section = response.getJSONObject("response").getJSONObject("content").getString("sectionName");
                    String date = response.getJSONObject("response").getJSONObject("content").getString("webPublicationDate");
                    final String title = response.getJSONObject("response").getJSONObject("content").getString("webTitle");
                    final String webURL = response.getJSONObject("response").getJSONObject("content").getString("webUrl");
                    Instant instant = Instant.parse(date);
                    ZoneId zoneId = ZoneId.of( "America/Los_Angeles" );
                    ZonedDateTime zdtAtLA = instant.atZone(zoneId);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
                    final String time = zdtAtLA.format(formatter);
                    LinearLayout progress = findViewById(R.id.progressbar_view);
                    progress.setVisibility(View.GONE);
                    CardView cardView = findViewById(R.id.detail_news_card);
                    cardView.setVisibility(View.VISIBLE);
                    TextView detailTitle = findViewById(R.id.detail_title);
                    detailTitle.setText(title);
                    ImageView detail_content_image = findViewById(R.id.detail_content_image);

                    Picasso.get().load(imgURL).into(detail_content_image);
                    TextView detail_content_title = findViewById(R.id.detail_content_title);
                    detail_content_title.setText(title);

                    TextView detail_section = findViewById(R.id.detail_section);
                    detail_section.setText(section);
                    TextView dateView = findViewById(R.id.detail_date);
                    dateView.setText(time);
                    ImageButton twitter = findViewById(R.id.detail_twitter);
                    twitter.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            String hashTag = "CSCI571NewsSearch";
                            Intent intent = new Intent();
                            String text = "Check out this Link:\n";
                            intent.setData(Uri.parse(getString(R.string.twitterShareUrl) + "?text=" + text + "&url="+ webURL +"&hashtags="+ hashTag));
                            startActivity(intent);
                        }
                    });
                    final ImageButton bookmark = findViewById(R.id.detail_bookmark);
                    if(readFavs(id)){
                        bookmark.setImageDrawable(getDrawable(R.drawable.ic_bookmark_full_24dp));
                    }
                    else{
                        bookmark.setImageDrawable(getDrawable(R.drawable.baseline_bookmark_border_black_18dp));
                    }
                    final String finalImgURL = imgURL;
                    bookmark.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View v){
                            if(readFavs(id)){
                                bookmark.setImageDrawable(getDrawable(R.drawable.baseline_bookmark_border_black_18dp));
                                removeNote(id);
                                Toast.makeText(context, '"' + title + "' " + "is removed from BookMarks", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                bookmark.setImageDrawable(getDrawable(R.drawable.ic_bookmark_full_24dp));
                                NewsItem cur = new NewsItem(section, time, title, id, finalImgURL, webURL);
                                cur.realTime = time.substring(0, 6);
                                cur.ifFav = true;
                                addNote(cur);
                                Toast.makeText(context, '"' + title + "' " + "is added to BookMarks", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    TextView mainContent = findViewById(R.id.detail_main_content);
                    mainContent.setText(HtmlCompat.fromHtml(content, HtmlCompat.FROM_HTML_MODE_LEGACY));
                    //System.out.println(content);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("This is not working " + error);
            }
        });

        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);


    }

}
