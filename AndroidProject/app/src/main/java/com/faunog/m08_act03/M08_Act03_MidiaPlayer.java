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
import java.util.List;
import java.util.Objects;

public class M08_Act03_MidiaPlayer extends AppCompatActivity {
    private RecyclerView recyclerView;
    private List<String> songList;

    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializa la lista de canciones
        songList = new ArrayList<>();

        // Encuentra la referencia al RecyclerView
        recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Crea el adaptador y agrega el OnItemClickListener
        SongAdapter songAdapter = new SongAdapter(songList, this, songPath -> {
            // Abre la actividad del reproductor de música y pasa la lista de canciones y la posición actual
            Intent intent = new Intent(M08_Act03_MidiaPlayer.this, MediaPlayerActivity.class);
            intent.putExtra("SONG_PATH", songPath);
            intent.putStringArrayListExtra("SONG_LIST", new ArrayList<>(songList));
            startActivity(intent);
        });
        recyclerView.setAdapter(songAdapter);

        checkAndRequestPermission();
    }

    // Método para cargar la lista de canciones en el directorio "Download"
    private void loadSongList() {
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();

        File directory = new File(path);

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().endsWith(".mp3")) {
                    // Agrega el nombre de la canción a la lista
                    songList.add(file.getAbsolutePath());
                }
            }
        }
        Log.d("SongListSize", "Size: " + songList.size());
    }

    @SuppressLint("NotifyDataSetChanged")
    private void reloadFiles() {
        // Limpiar la lista actual
        songList.clear();

        // Volver a cargar los archivos MP3 desde la carpeta "Download"
        loadSongList();

        // Notificar al adaptador que los datos han cambiado
        Objects.requireNonNull(recyclerView.getAdapter()).notifyDataSetChanged();
    }

    private void checkAndRequestPermission() {
        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
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
        // Muestra un mensaje al usuario informándole sobre la importancia del permiso
        // y cómo puede otorgarlo manualmente a través de la configuración de la aplicación en el dispositivo.
        // También puedes abrir la configuración de la aplicación directamente.
        Toast.makeText(this, "Es necesario el permiso para acceder a los archivos.", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", getPackageName(), null));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}