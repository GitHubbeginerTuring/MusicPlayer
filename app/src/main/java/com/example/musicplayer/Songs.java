package com.example.musicplayer;

import android.graphics.Bitmap;

public class Songs {
    private String name;
    private String singer;
    private String album;
    private Bitmap cover;
    private int mills;
    private String path;

    public int getMills() {
        return mills;
    }

    public void setMills(int mills) {
        this.mills = mills;
    }



    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public Bitmap getCover() {
        return cover;
    }

    public void setCover(Bitmap cover) {
        this.cover = cover;
    }

    public Songs() {
    }


}
