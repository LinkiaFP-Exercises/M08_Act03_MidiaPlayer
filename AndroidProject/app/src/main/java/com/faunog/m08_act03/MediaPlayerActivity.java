package com.faunog.m08_act03;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class MediaPlayerActivity  extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    private Button playPauseButton, stopButton, forwardButton, backwardButton, nextButton, prevButton;
    private SeekBar seekBar;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player);

        // Inicializa los elementos de la interfaz
        playPauseButton = findViewById(R.id.playPauseButton);
        stopButton = findViewById(R.id.stopButton);
        forwardButton = findViewById(R.id.forwardButton);
        backwardButton = findViewById(R.id.backwardButton);
        nextButton = findViewById(R.id.nextButton);
        prevButton = findViewById(R.id.prevButton);
        seekBar = findViewById(R.id.seekBar);

        // Inicializa el reproductor
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource("ruta de la canción seleccionada");
            mediaPlayer.prepare();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        mediaPlayer.setLooping(true); // Repetir la canción

        // Configura los listeners para los botones
        playPauseButton.setOnClickListener(v -> {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                playPauseButton.setText("Play");
            } else {
                mediaPlayer.start();
                playPauseButton.setText("Pause");
            }
        });

        stopButton.setOnClickListener(v -> {
            mediaPlayer.seekTo(0);
            mediaPlayer.pause();
            playPauseButton.setText("Play");
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
            // Lógica para reproducir la siguiente canción
        });

        prevButton.setOnClickListener(v -> {
            // Lógica para reproducir la canción anterior
        });

        // Configura el listener para la barra de progreso
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Actualiza la barra de progreso de la canción
        updateSeekBar();
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
