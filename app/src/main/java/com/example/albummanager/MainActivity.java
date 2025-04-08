package com.example.albummanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import com.example.albummanager.model.Album;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private AlbumManager albumManager;
    private RecyclerView recyclerView;
    private AlbumAdapter adapter;
    private List<Album> albums = new ArrayList<>();
    private ContentObserver contentObserver;
    private boolean isSelfChange = false;
    private static final Uri CONTENT_URI = Uri.parse("content://com.example.albummanager.provider/albums");
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate called");

        // Set up toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize AlbumManager
        albumManager = new AlbumManager(getContentResolver());

        // Set up RecyclerView
        recyclerView = findViewById(R.id.albumRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AlbumAdapter(albums, this::showEditDialog);
        recyclerView.setAdapter(adapter);

        // Set up FAB
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> showAddAlbumDialog());

        // Set up Add Button
        MaterialButton addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(view -> {
            TextInputEditText artistInput = findViewById(R.id.artistInput);
            TextInputEditText nameInput = findViewById(R.id.nameInput);

            String artist = artistInput.getText().toString();
            String name = nameInput.getText().toString();

            if (!artist.isEmpty() && !name.isEmpty()) {
                isSelfChange = true;
                Log.d(TAG, "Adding album from UI: " + artist + " - " + name);
                albumManager.addAlbum(artist, name);
                artistInput.setText("");
                nameInput.setText("");
                loadAlbums();
                isSelfChange = false;
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            }
        });

        // Setup ContentObserver
        contentObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
            @Override
            public void onChange(boolean selfChange, Uri uri) {
                Log.d(TAG, "ContentObserver onChange called, selfChange: " + selfChange + ", uri: " + uri);
                if (!isSelfChange) {
                    mainHandler.post(() -> {
                        Log.d(TAG, "Data changed externally, reloading albums");
                        Toast.makeText(MainActivity.this, "Data changed in AlbumClient", Toast.LENGTH_SHORT).show();
                        loadAlbums();
                    });
                } else {
                    Log.d(TAG, "Ignoring self-change");
                }
            }
        };

        // Load initial data
        loadAlbums();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called, registering ContentObserver");
        // Register ContentObserver
        getContentResolver().registerContentObserver(
            CONTENT_URI,
            true,  // Notify for descendants
            contentObserver
        );
        // Load initial data
        loadAlbums();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause called, unregistering ContentObserver");
        // Unregister ContentObserver
        getContentResolver().unregisterContentObserver(contentObserver);
    }

    private void loadAlbums() {
        Log.d(TAG, "Loading albums");
        mainHandler.post(() -> {
            try {
                albums.clear();
                List<Album> newAlbums = albumManager.getAllAlbums();
                if (newAlbums != null) {
                    albums.addAll(newAlbums);
                    Log.d(TAG, "Loaded " + albums.size() + " albums");
                    adapter.notifyDataSetChanged();
                } else {
                    Log.e(TAG, "Failed to load albums");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading albums", e);
            }
        });
    }

    private void showAddAlbumDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_album, null);
        TextInputEditText artistInput = dialogView.findViewById(R.id.artistInput);
        TextInputEditText nameInput = dialogView.findViewById(R.id.nameInput);

        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Add Album")
            .setView(dialogView)
            .setPositiveButton("Add", (dialog, which) -> {
                String artist = artistInput.getText().toString();
                String name = nameInput.getText().toString();
                if (!artist.isEmpty() && !name.isEmpty()) {
                    isSelfChange = true;
                    albumManager.addAlbum(artist, name);
                    loadAlbums();
                    isSelfChange = false;
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showEditDialog(Album album) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_album, null);
        TextInputEditText artistInput = dialogView.findViewById(R.id.editArtist);
        TextInputEditText nameInput = dialogView.findViewById(R.id.editAlbum);

        artistInput.setText(album.getArtist());
        nameInput.setText(album.getName());

        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Edit Album")
            .setView(dialogView)
            .setPositiveButton("Save", (dialog, which) -> {
                String artist = artistInput.getText().toString();
                String name = nameInput.getText().toString();
                if (!artist.isEmpty() && !name.isEmpty()) {
                    album.setArtist(artist);
                    album.setName(name);
                    isSelfChange = true;
                    if (albumManager.updateAlbum(album)) {
                        loadAlbums();
                    }
                    isSelfChange = false;
                }
            })
            .setNegativeButton("Delete", (dialog, which) -> {
                isSelfChange = true;
                if (albumManager.deleteAlbum(album.getId())) {
                    loadAlbums();
                }
                isSelfChange = false;
            })
            .setNeutralButton("Cancel", null)
            .show();
    }

    private class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder> {
        private List<Album> albums;
        private OnAlbumClickListener listener;

        public interface OnAlbumClickListener {
            void onAlbumClick(Album album);
        }

        public AlbumAdapter(List<Album> albums, OnAlbumClickListener listener) {
            this.albums = albums;
            this.listener = listener;
        }

        @Override
        public AlbumViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_album, parent, false);
            return new AlbumViewHolder(view);
        }

        @Override
        public void onBindViewHolder(AlbumViewHolder holder, int position) {
            Album album = albums.get(position);
            holder.artistTextView.setText(album.getArtist());
            holder.nameTextView.setText(album.getName());
            holder.itemView.setOnClickListener(v -> listener.onAlbumClick(album));
        }

        @Override
        public int getItemCount() {
            return albums.size();
        }

        class AlbumViewHolder extends RecyclerView.ViewHolder {
            TextView artistTextView;
            TextView nameTextView;

            AlbumViewHolder(View itemView) {
                super(itemView);
                artistTextView = itemView.findViewById(R.id.artistTextView);
                nameTextView = itemView.findViewById(R.id.nameTextView);
            }
        }
    }
}
