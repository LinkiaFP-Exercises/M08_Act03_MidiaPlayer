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

/**
 * Clase principal que gestiona la reproducción de archivos de audio y la visualización en una lista.
 *
 * @author <a href="https://about.me/prof.guazina">Fauno Guazina</a>
 * @version 1.1
 * @since 29/11/2023
 */
public class M08_Act03_MidiaPlayer extends AppCompatActivity {

    /**
     * Método llamado cuando se crea la actividad.
     *
     * @param savedInstanceState El estado guardado de la actividad.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fillSongList();
        buildRecyclerView();
        checkAndRequestPermission();
    }

    /**
     * Llena la lista de canciones con archivos de audio encontrados en el directorio de descargas.
     */
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

    /**
     * Construye el RecyclerView para mostrar la lista de canciones.
     */
    private void buildRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(getSongAdapter());
    }

    /**
     * Obtiene un adaptador para la lista de canciones.
     *
     * @return Adaptador de la lista de canciones.
     */
    @NonNull
    private SongAdapter getSongAdapter() {
        return new SongAdapter(songList, this, this::onSongClick);
    }

    /**
     * Acción realizada al hacer clic en una canción.
     *
     * @param songPath Ruta de la canción seleccionada.
     */
    private void onSongClick(String songPath) {
        Intent intent = new Intent(M08_Act03_MidiaPlayer.this, MediaPlayerActivity.class);
        intent.putExtra("SONG_PATH", songPath);
        intent.putStringArrayListExtra("SONG_LIST", new ArrayList<>(songList));
        startActivity(intent);
    }

    /**
     * Recarga la lista de archivos de audio y notifica al adaptador si ha habido cambios.
     */
    @SuppressLint("NotifyDataSetChanged")
    private void reloadFiles() {
        List<String> oldList = new ArrayList<>(songList);
        fillSongList();

        if (!oldList.equals(songList)) {
            Objects.requireNonNull(recyclerView.getAdapter()).notifyDataSetChanged();
        }
    }


    /**
     * Verifica y solicita permisos para acceder al almacenamiento externo.
     *
     * @see M08_Act03_MidiaPlayer#reloadFiles()
     */
    private void checkAndRequestPermission() {
        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
        int read_external = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (read_external != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        } else {
            reloadFiles();
        }

    }

    /**
     * Maneja la respuesta de la solicitud de permisos.
     *
     * @param requestCode  Código de solicitud.
     * @param permissions  Arreglo de permisos solicitados.
     * @param grantResults Arreglo de resultados de la solicitud de permisos.
     * @see M08_Act03_MidiaPlayer#showPermissionDeniedMessage()
     */
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

    /**
     * Muestra un mensaje indicando que se denegaron los permisos y proporciona un enlace para configurarlos.
     */
    private void showPermissionDeniedMessage() {
        Toast.makeText(this, "Es necesario el permiso para acceder a los archivos.", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", getPackageName(), null));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /**
     * RecyclerView utilizado para mostrar la lista de canciones.
     */
    private RecyclerView recyclerView;

    /**
     * Lista de rutas de archivos de audio.
     */
    private List<String> songList;

    /**
     * Constante que representa el código de solicitud de permisos.
     */
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
}