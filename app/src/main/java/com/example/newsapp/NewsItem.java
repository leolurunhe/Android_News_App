package com.example.newsapp;



import java.io.Serializable;

public class NewsItem implements Serializable {
    public final String section;
    public final String time;
    public final String title;
    public final String id;
    public final String url;
    public final String image;
    public boolean ifFav = false;
    public String realTime = "";

    public NewsItem(String section, String time, String content, String id, String img, String url) {
        this.section = section;
        this.title = content;
        this.time = time;
        this.id = id;
        this.image = img;
        this.url = url;
    }


    @Override
    public String toString() {
        return title;
    }
}