package com.faunog.m08_act03;

import android.os.Bundle;
import android.os.Environment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class M08_Act03_MidiaPlayer extends AppCompatActivity {
    private RecyclerView recyclerView;
    private List<String> songList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializa la lista de canciones
        songList = new ArrayList<>();

        // Encuentra la referencia al ListView
        recyclerView = findViewById(R.id.recyclerView);

        // Obtiene la lista de canciones y la muestra en el ListView
        loadSongList();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new SongAdapter(songList));
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
                    songList.add(file.getName());
                }
            }
        }
    }
}