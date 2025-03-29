package com.example.albumapi;

public class DataModel {
    private String id;
    private String artist;
    private String name;

    public DataModel(String id, String artist, String name) {
        this.id = id;
        this.artist = artist;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getArtist() {
        return artist;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return id + " - " + artist + " - " + name;
    }
}
