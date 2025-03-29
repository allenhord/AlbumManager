package com.example.albumapi;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String CONTENT_URI = "content://com.demo.user.provider/users";
    private Context context;

    public DatabaseManager(Context context) {
        this.context = context;
    }

    public List<DataModel> getAllAlbums() {
        List<DataModel> albumList = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(Uri.parse(CONTENT_URI), null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String id = cursor.getString(cursor.getColumnIndex("id"));
                String artist = cursor.getString(cursor.getColumnIndex("artist"));
                String name = cursor.getString(cursor.getColumnIndex("name"));
                albumList.add(new DataModel(id, artist, name));
            } while (cursor.moveToNext());

            cursor.close();
        }

        return albumList;
    }
}