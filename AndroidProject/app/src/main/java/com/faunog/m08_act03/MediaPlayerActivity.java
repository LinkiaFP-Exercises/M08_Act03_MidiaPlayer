package com.faunog.m08_act03;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.IOException;
import java.util.List;

public class MediaPlayerActivity extends AppCompatActivity {
    private Toolbar toolbar;
    public TextView songTitleTextView, authorTextView, albumTextView;
    public ImageView albumImageView;
    private Button playPauseButton, stopButton, forwardButton, backwardButton, nextButton, prevButton;
    private SeekBar seekBar;
    private Chronometer chronometerStart, chronometerEnd;
    private MediaPlayer mediaPlayer;
    private List<String> songList;
    private int totalDuration, currentSongPosition = 0;
    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player);
        toolbarNavigationFunction();
        initializeInterfaceElements();
        initializeMediaPlayerElements();

        playPauseButton.setOnClickListener(v -> {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                playPauseButton.setText(getString(R.string.activity_media_player_ButtonPlay));
            } else {
                mediaPlayer.start();
                playPauseButton.setText(getString(R.string.activity_media_player_ButtonPause));
            }
            updateSeekBar();
        });

        stopButton.setOnClickListener(v -> {
            mediaPlayer.seekTo(0);
            mediaPlayer.pause();
            playPauseButton.setText(getString(R.string.activity_media_player_ButtonPlay));
            updateSeekBar();
        });

        forwardButton.setOnClickListener(v -> {
            int currentPosition = mediaPlayer.getCurrentPosition();
            mediaPlayer.seekTo(currentPosition + 10000); // Avanzar 10 segundos
            updateSeekBar();
        });

        backwardButton.setOnClickListener(v -> {
            int currentPosition = mediaPlayer.getCurrentPosition();
            mediaPlayer.seekTo(currentPosition - 10000); // Retroceder 10 segundos
            updateSeekBar();
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
                    inflateMediaPlayerCharacteristics();
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
            updateSeekBar();
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
                    inflateMediaPlayerCharacteristics();
                } catch (IOException e) {
                    Log.e("MediaPlayerActivity", "Error al reproducir la canción anterior:\n" + e.getMessage(), e);
                }
            } else {
                currentSongPosition = songList.size() - 1;
                mediaPlayer.stop();
                mediaPlayer.reset();
            }
            updateSeekBar();
        });


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Actualiza la posición de reproducción según sea necesario
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                    updateChronometers(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                }
            }
        });

        mediaPlayer.setOnCompletionListener(mp -> {
            currentSongPosition++;

            if (currentSongPosition < songList.size()) {
                try {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(songList.get(currentSongPosition));
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    playPauseButton.setText(getString(R.string.activity_media_player_ButtonPause));
                } catch (IOException e) {
                    Log.e("MediaPlayerActivity", "Error in IF setOnCompletionListener:\n" + e.getMessage(), e);
                }
            } else {
                try {
                    currentSongPosition = 0;
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(songList.get(currentSongPosition));
                    mediaPlayer.prepare();
                    playPauseButton.setText(getString(R.string.activity_media_player_ButtonPlay));
                } catch (IOException e) {
                    Log.e("MediaPlayerActivity", "Error in ELSE setOnCompletionListener:\n" + e.getMessage(), e);
                }
            }
            updateSeekBar();
        });

    }

    private void initializeInterfaceElements() {
        toolbar = findViewById(R.id.toolbar);

        songTitleTextView = findViewById(R.id.songTitleTextView);
        authorTextView = findViewById(R.id.authorTextView);
        albumTextView = findViewById(R.id.albumTitleTextView);
        albumImageView = findViewById(R.id.albumImageView);

        playPauseButton = findViewById(R.id.playPauseButton);
        stopButton = findViewById(R.id.stopButton);
        forwardButton = findViewById(R.id.forwardButton);
        backwardButton = findViewById(R.id.backwardButton);
        nextButton = findViewById(R.id.nextButton);
        prevButton = findViewById(R.id.prevButton);
        seekBar = findViewById(R.id.seekBar);
        chronometerStart = findViewById(R.id.chronometerStart);
        chronometerEnd = findViewById(R.id.chronometerEnd);
    }

    private void toolbarNavigationFunction() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.app_name));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(this, M08_Act03_MidiaPlayer.class);
            this.startActivity(intent);
            this.finish();
        });
    }

    private void initializeMediaPlayerElements() {
        mediaPlayer = new MediaPlayer();
        Intent intent = getIntent();
        songList = intent.getStringArrayListExtra("SONG_LIST");
        String songPath = intent.getStringExtra("SONG_PATH");
        currentSongPosition = songList.indexOf(songPath);

        try {
            mediaPlayer.setDataSource(songPath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            inflateMediaPlayerCharacteristics();
            updateSeekBar();
        } catch (IOException e) {
            Log.e("MediaPlayerActivity", "Error in initializeMediaPlayerElements():\n" + e.getMessage(), e);
        }
    }

    /**
     * @noinspection resource
     */
    private void inflateMediaPlayerCharacteristics() {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            String songPath = songList.get(currentSongPosition);
            retriever.setDataSource(songPath);

            String songTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            String songAuthor = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            String songAlbum = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            byte[] albumArt = retriever.getEmbeddedPicture();

            if (songTitle == null) {
                Log.e("SongAdapter", "No se encontraron metadatos para: " + songPath);
                songTitle = getString(R.string.activity_media_player_songTitleTextView);
            }

            songTitleTextView.setText(songTitle);
            authorTextView.setText(songAuthor != null ? songAuthor : getString(R.string.activity_media_player_authorTextView));
            albumTextView.setText(songAlbum != null ? songAlbum : getString(R.string.activity_media_player_albumTitleTextView));

            if (albumArt != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(albumArt, 0, albumArt.length);
                albumImageView.setImageBitmap(bitmap);
            } else {
                // Mostrar imagen predeterminada
                int resourceId = R.drawable.unknown_album;
                albumImageView.setImageResource(resourceId);
            }

        } catch (Exception e) {
            Log.e("MediaPlayerActivity", "Error in inflateMediaPlayerCharacteristics:\n" + e.getMessage(), e);
        } finally {
            try {
                retriever.release();
            } catch (IOException e) {
                Log.e("MediaPlayerActivity", "Error Trying release retriever:\n" + e.getMessage(), e);
            }
        }
    }

    private void updateSeekBar() {
        int currentDuration = mediaPlayer.getCurrentPosition();
        totalDuration = mediaPlayer.getDuration();
        seekBar.setMax(totalDuration);
        seekBar.setProgress(currentDuration);
        updateChronometers(currentDuration);
        if (mediaPlayer.isPlaying() && currentDuration < totalDuration) {
            handler.postDelayed(this::updateSeekBar, 1000); // Actualiza cada segundo
        }
    }

    private void updateChronometers(int currentDuration) {
        int remainingDuration = totalDuration - currentDuration;

        chronometerStart.setBase(SystemClock.elapsedRealtime() - currentDuration);
        chronometerStart.start();

        // Actualiza el cronómetro de tiempo decreciente
        chronometerEnd.setBase(SystemClock.elapsedRealtime() + remainingDuration);
        chronometerEnd.start();
    }
}
