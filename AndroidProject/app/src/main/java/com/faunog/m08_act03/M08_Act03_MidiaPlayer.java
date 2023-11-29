package com.faunog.m08_act03;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class M08_Act03_MidiaPlayer extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fillSongList();
        buildRecyclerView();
        checkAndRequestPermission();
    }

    private void fillSongList() {
        String pathToDirectoryDownloads = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
        File directoryDownloads = new File(pathToDirectoryDownloads);
        File[] files = directoryDownloads.listFiles();
        if (files != null) {
            songList = Arrays.stream(files)
                    .filter(file -> file.getName().endsWith(".mp3"))
                    .map(File::getAbsolutePath)
                    .collect(Collectors.toList());
        }
        Log.d("SongListSize", "Size: " + songList.size());
    }

    private void buildRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(getSongAdapter());
    }

    @NonNull
    private SongAdapter getSongAdapter() {
        return new SongAdapter(songList, this, this::onSongClick);
    }

    private void onSongClick(String songPath) {
        Intent intent = new Intent(M08_Act03_MidiaPlayer.this, MediaPlayerActivity.class);
        intent.putExtra("SONG_PATH", songPath);
        intent.putStringArrayListExtra("SONG_LIST", new ArrayList<>(songList));
        startActivity(intent);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void reloadFiles() {
        List<String> oldList = new ArrayList<>(songList);
        fillSongList();

        if (!oldList.equals(songList)) {
            Objects.requireNonNull(recyclerView.getAdapter()).notifyDataSetChanged();
        }
    }


    private void checkAndRequestPermission() {
        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
        int read_external = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (read_external != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        } else {
            reloadFiles();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                reloadFiles();
            } else {
                showPermissionDeniedMessage();
            }
        }
    }

    private void showPermissionDeniedMessage() {
        Toast.makeText(this, "Es necesario el permiso para acceder a los archivos.", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", getPackageName(), null));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private RecyclerView recyclerView;
    private List<String> songList;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
}