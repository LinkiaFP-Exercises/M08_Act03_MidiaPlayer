package com.faunog.m08_act03;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.IOException;
import java.util.List;

public class MediaPlayerActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private Button playPauseButton, stopButton, forwardButton, backwardButton, nextButton, prevButton;
    private SeekBar seekBar;
    private MediaPlayer mediaPlayer;
    private List<String> songList;
    private String songPath;
    private int currentSongPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player);

        initializeInterfaceElements();
        setSupportActionBar(toolbar);
        initializeMediaPlayerElements();
        try {
            mediaPlayer.setDataSource(songPath);
            mediaPlayer.prepare();
        } catch (IOException e) {
            Log.e("MediaPlayerActivity", "Error en Try mediaPlayer.setDataSource(songPath):\n" + e.getMessage(), e);
        }
        mediaPlayer.setLooping(true); // Repetir la canción

        // Configura los listeners para los botones
        playPauseButton.setOnClickListener(v -> {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                playPauseButton.setText(getString(R.string.activity_media_player_ButtonPlay));
            } else {
                mediaPlayer.start();
                playPauseButton.setText(getString(R.string.activity_media_player_ButtonPause));
            }
        });

        stopButton.setOnClickListener(v -> {
            mediaPlayer.seekTo(0);
            mediaPlayer.pause();
            playPauseButton.setText(getString(R.string.activity_media_player_ButtonPlay));
        });

        forwardButton.setOnClickListener(v -> {
            int currentPosition = mediaPlayer.getCurrentPosition();
            mediaPlayer.seekTo(currentPosition + 10000); // Avanzar 10 segundos
        });

        backwardButton.setOnClickListener(v -> {
            int currentPosition = mediaPlayer.getCurrentPosition();
            mediaPlayer.seekTo(currentPosition - 10000); // Retroceder 10 segundos
        });

        nextButton.setOnClickListener(v -> {
            if (currentSongPosition < songList.size() - 1) {
                // Hay una siguiente canción en la lista
                currentSongPosition++;

                try {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(songList.get(currentSongPosition));
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    updateSeekBar();
                } catch (IOException e) {
                    Log.e("MediaPlayerActivity", "Error al reproducir la siguiente canción:\n" + e.getMessage(), e);
                }
            } else {
                // Estás en la última canción, puedes decidir qué hacer aquí
                // Por ejemplo, volver al principio o detener la reproducción
                currentSongPosition = 0;
                mediaPlayer.stop();
                mediaPlayer.reset();
            }
        });

        prevButton.setOnClickListener(v -> {
            if (currentSongPosition > 0) {
                // Hay una canción anterior en la lista
                currentSongPosition--;

                try {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(songList.get(currentSongPosition));
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    updateSeekBar();
                } catch (IOException e) {
                    Log.e("MediaPlayerActivity", "Error al reproducir la canción anterior:\n" + e.getMessage(), e);
                }
            } else {
                // Estás en la primera canción, puedes decidir qué hacer aquí
                // Por ejemplo, volver al final o detener la reproducción
                currentSongPosition = songList.size() - 1;
                mediaPlayer.stop();
                mediaPlayer.reset();
            }
        });


        // Configura el listener para la barra de progreso
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Actualiza la posición de reproducción según sea necesario
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Puedes realizar acciones cuando el usuario comienza a arrastrar la barra
                // Por ejemplo, pausar la reproducción si está en curso
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Puedes realizar acciones cuando el usuario deja de arrastrar la barra
                // Por ejemplo, reanudar la reproducción si estaba pausada
                if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                }
            }
        });


        // Actualiza la barra de progreso de la canción
        updateSeekBar();

        // Configura el listener para el final de la canción
        mediaPlayer.setOnCompletionListener(mp -> {
            // Incrementa la posición actual para reproducir la siguiente canción
            currentSongPosition++;

            // Verifica si llegaste al final de la lista
            if (currentSongPosition < songList.size()) {
                // Configura la nueva fuente de datos y comienza la reproducción
                try {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(songList.get(currentSongPosition));
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    updateSeekBar();  // Asegúrate de actualizar la barra de progreso
                } catch (IOException e) {
                    Log.e("MediaPlayerActivity", "Error en mediaPlayer.setOnCompletionListener:\n" + e.getMessage(), e);
                }
            } else {
                // Llegaste al final de la lista, puedes decidir qué hacer aquí
                // Por ejemplo, regresar al principio o detener la reproducción
                currentSongPosition = 0;
                mediaPlayer.stop();
                mediaPlayer.reset();
            }
        });

    }

    private void initializeInterfaceElements() {
        toolbar = findViewById(R.id.toolbar);
        playPauseButton = findViewById(R.id.playPauseButton);
        stopButton = findViewById(R.id.stopButton);
        forwardButton = findViewById(R.id.forwardButton);
        backwardButton = findViewById(R.id.backwardButton);
        nextButton = findViewById(R.id.nextButton);
        prevButton = findViewById(R.id.prevButton);
        seekBar = findViewById(R.id.seekBar);
    }

    private void initializeMediaPlayerElements() {
        mediaPlayer = new MediaPlayer();
        Intent intent = getIntent();
        songList = intent.getStringArrayListExtra("SONG_LIST");
        songPath = intent.getStringExtra("SONG_PATH");
    }

    // Método para actualizar la barra de progreso
    private void updateSeekBar() {
        seekBar.setProgress(mediaPlayer.getCurrentPosition());
        if (mediaPlayer.isPlaying()) {
            Runnable runnable = this::updateSeekBar;
            seekBar.postDelayed(runnable, 1000); // Actualiza cada segundo
        }
    }
}
