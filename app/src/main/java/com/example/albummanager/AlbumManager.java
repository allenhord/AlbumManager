package com.example.albummanager;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.example.albummanager.model.Album;

import java.util.ArrayList;
import java.util.List;

public class AlbumManager {
    private static final String TAG = "AlbumManager";
    private static final Uri CONTENT_URI = Uri.parse("content://com.example.albummanager.provider/albums");
    private final ContentResolver contentResolver;

    public AlbumManager(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    /**
     * Add a new album to the database
     * @param artist The artist name
     * @param name The album name
     * @return The ID of the newly created album, or -1 if failed
     */
    public long addAlbum(String artist, String name) {
        Log.d(TAG, "Adding album: " + artist + " - " + name);
        ContentValues values = new ContentValues();
        values.put("artist", artist);
        values.put("name", name);
        Uri result = contentResolver.insert(CONTENT_URI, values);
        long id = result != null ? Long.parseLong(result.getLastPathSegment()) : -1;
        Log.d(TAG, "Added album with ID: " + id);
        return id;
    }

    /**
     * Get all albums from the database
     * @return List of albums, or empty list if none found
     */
    public List<Album> getAllAlbums() {
        Log.d(TAG, "Getting all albums");
        List<Album> albums = new ArrayList<>();
        Cursor cursor = contentResolver.query(CONTENT_URI, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndex("id"));
                String artist = cursor.getString(cursor.getColumnIndex("artist"));
                String name = cursor.getString(cursor.getColumnIndex("name"));
                albums.add(new Album(id, artist, name));
            }
            cursor.close();
        }
        Log.d(TAG, "Retrieved " + albums.size() + " albums");
        return albums;
    }

    /**
     * Get a specific album by ID
     * @param id The album ID
     * @return The album if found, null otherwise
     */
    public Album getAlbumById(long id) {
        Log.d(TAG, "Getting album by ID: " + id);
        Cursor cursor = null;
        try {
            String selection = "id=?";
            String[] selectionArgs = new String[]{String.valueOf(id)};
            cursor = contentResolver.query(CONTENT_URI, null, selection, selectionArgs, null);
            
            if (cursor != null && cursor.moveToFirst()) {
                return Album.fromCursor(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting album by ID: " + e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * Update an existing album
     * @param album The album to update
     * @return true if successful, false otherwise
     */
    public boolean updateAlbum(Album album) {
        Log.d(TAG, "Updating album: " + album);
        ContentValues values = new ContentValues();
        values.put("artist", album.getArtist());
        values.put("name", album.getName());
        String selection = "id=?";
        String[] selectionArgs = new String[]{String.valueOf(album.getId())};
        int updated = contentResolver.update(CONTENT_URI, values, selection, selectionArgs);
        Log.d(TAG, "Update result: " + (updated > 0));
        return updated > 0;
    }

    /**
     * Delete an album by ID
     * @param id The album ID to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteAlbum(long id) {
        Log.d(TAG, "Deleting album with ID: " + id);
        String selection = "id=?";
        String[] selectionArgs = new String[]{String.valueOf(id)};
        int deleted = contentResolver.delete(CONTENT_URI, selection, selectionArgs);
        Log.d(TAG, "Delete result: " + (deleted > 0));
        return deleted > 0;
    }

    /**
     * Search albums by artist name
     * @param artist The artist name to search for
     * @return List of matching albums
     */
    public List<Album> searchAlbumsByArtist(String artist) {
        Log.d(TAG, "Searching albums by artist: " + artist);
        List<Album> albums = new ArrayList<>();
        String selection = "artist LIKE ?";
        String[] selectionArgs = new String[]{"%" + artist + "%"};
        Cursor cursor = contentResolver.query(CONTENT_URI, null, selection, selectionArgs, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndex("id"));
                String name = cursor.getString(cursor.getColumnIndex("name"));
                albums.add(new Album(id, artist, name));
            }
            cursor.close();
        }
        Log.d(TAG, "Found " + albums.size() + " albums by artist");
        return albums;
    }

    /**
     * Search albums by album name
     * @param name The album name to search for
     * @return List of matching albums
     */
    public List<Album> searchAlbumsByName(String name) {
        Log.d(TAG, "Searching albums by name: " + name);
        List<Album> albums = new ArrayList<>();
        String selection = "name LIKE ?";
        String[] selectionArgs = new String[]{"%" + name + "%"};
        Cursor cursor = contentResolver.query(CONTENT_URI, null, selection, selectionArgs, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndex("id"));
                String artist = cursor.getString(cursor.getColumnIndex("artist"));
                albums.add(new Album(id, artist, name));
            }
            cursor.close();
        }
        Log.d(TAG, "Found " + albums.size() + " albums by name");
        return albums;
    }
} 