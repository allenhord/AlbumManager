package com.example.albummanager;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;
import java.util.HashMap;

public class AlbumProvider extends ContentProvider {
    private static final String TAG = "AlbumProvider";

    // defining authority so that other application can access it
    static final String PROVIDER_NAME = "com.example.albummanager.provider";

    // defining content URI
    static final String URL = "content://" + PROVIDER_NAME + "/albums";

    // parsing the content URI
    static final Uri CONTENT_URI = Uri.parse(URL);

    static final String id = "id";
    static final String artist = "artist";
    static final String name = "name";
    static final int uriCode = 1;
    static final UriMatcher uriMatcher;
    private static HashMap<String, String> values;

    static {
        // Initialize the values HashMap
        values = new HashMap<>();
        values.put(id, id);
        values.put(artist, artist);
        values.put(name, name);

        // to match the content URI
        // every time user access table under content provider
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        // to access whole table
        uriMatcher.addURI(PROVIDER_NAME, "albums", uriCode);

        // to access a particular row
        // of the table
        uriMatcher.addURI(PROVIDER_NAME, "albums/*", uriCode);
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case uriCode:
                return "vnd.android.cursor.dir/albums";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public boolean onCreate() {
        Log.d(TAG, "onCreate called");
        Context context = getContext();
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();
        return db != null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Log.d(TAG, "query called with URI: " + uri);
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_NAME);
        
        // Set projection map if provided
        if (projection != null) {
            qb.setProjectionMap(values);
        }
        
        if (sortOrder == null || sortOrder.isEmpty()) {
            sortOrder = id;
        }
        
        Cursor c = qb.query(db, projection, selection, selectionArgs, null,
                null, sortOrder);
        
        // Set notification URI to CONTENT_URI
        if (c != null) {
            c.setNotificationUri(getContext().getContentResolver(), CONTENT_URI);
            Log.d(TAG, "Query completed, cursor count: " + c.getCount());
        } else {
            Log.e(TAG, "Query returned null cursor");
        }
        return c;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.d(TAG, "insert called with URI: " + uri);
        long rowID = db.insert(TABLE_NAME, "", values);
        if (rowID > 0) {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            Log.d(TAG, "Insert successful, notifying change for URI: " + CONTENT_URI);
            getContext().getContentResolver().notifyChange(CONTENT_URI, null);
            return _uri;
        }
        Log.e(TAG, "Insert failed");
        throw new SQLiteException("Failed to add a record into " + uri);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Log.d(TAG, "update called with URI: " + uri);
        int count = db.update(TABLE_NAME, values, selection, selectionArgs);
        if (count > 0) {
            Log.d(TAG, "Update successful, notifying change for URI: " + CONTENT_URI);
            getContext().getContentResolver().notifyChange(CONTENT_URI, null);
        }
        return count;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.d(TAG, "delete called with URI: " + uri);
        int count = db.delete(TABLE_NAME, selection, selectionArgs);
        if (count > 0) {
            Log.d(TAG, "Delete successful, notifying change for URI: " + CONTENT_URI);
            getContext().getContentResolver().notifyChange(CONTENT_URI, null);
        }
        return count;
    }

    private SQLiteDatabase db;
    static final String DATABASE_NAME = "AlbumDB";
    static final String TABLE_NAME = "Albums";
    static final int DATABASE_VERSION = 1;
    static final String CREATE_DB_TABLE = " CREATE TABLE " + TABLE_NAME
            + " (id INTEGER PRIMARY KEY AUTOINCREMENT, artist TEXT NOT NULL,"
            + " name TEXT NOT NULL);";

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(TAG, "Creating database table");
            db.execSQL(CREATE_DB_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }
}
