package com.example.albumapi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DatabaseManager dbManager;
    private TextView resultView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        resultView = findViewById(R.id.res);
        dbManager = new DatabaseManager(this);
    }

    public void onClickShowDetails(View view) {
        List<DataModel> albumList = dbManager.getAllAlbums();

        if (!albumList.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (DataModel album : albumList) {
                sb.append(album.toString()).append("\n");
            }
            resultView.setText(sb.toString());
        } else {
            resultView.setText("No Records Found");
        }
    }
}
