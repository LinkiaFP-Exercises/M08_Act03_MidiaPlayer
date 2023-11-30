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
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Clase que gestiona la reproducción de archivos de audio y la visualización de metadatos.
 *
 * @author <a href="https://about.me/prof.guazina">Fauno Guazina</a>
 * @version 1.1
 * @since 29/11/2023
 */
public class MediaPlayerActivity extends AppCompatActivity {

    /**
     * Método llamado cuando se crea la actividad.
     * Inicializa la interfaz de usuario y los elementos del reproductor multimedia.
     *
     * @param savedInstanceState El estado guardado de la actividad.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player);
        toolbarNavigationFunction();
        initializeInterfaceElements();
        initializeMediaPlayerElements();
        playPauseButton.setOnClickListener(this::onClickPlayPauseButton);
        stopButton.setOnClickListener(this::onClickStopButton);
        forwardButton.setOnClickListener(this::onClickForwardButton);
        backwardButton.setOnClickListener(this::onClickBackwardButton);
        nextButton.setOnClickListener(v -> playNextSongIfNotLastOrPrepareFirstSong());
        prevButton.setOnClickListener(this::onClickPrevButton);
        mediaPlayer.setOnCompletionListener(mp -> playNextSongIfNotLastOrPrepareFirstSong());
        seekBar.setOnSeekBarChangeListener(getOnSeekBarChangeListener());

    }

    /**
     * Acción ejecutada al hacer clic en el botón de reproducción/pausa.
     * Controla la reproducción o pausa del archivo de audio.
     * Actualiza la interfaz de usuario y la barra de progreso.
     *
     * @param view La vista del botón clicado.
     */
    private void onClickPlayPauseButton(View view) {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            playPauseButton.setText(getString(R.string.activity_media_player_ButtonPlay));
        } else {
            mediaPlayer.start();
            playPauseButton.setText(getString(R.string.activity_media_player_ButtonPause));
        }
        updateSeekBar();
    }

    /**
     * Acción ejecutada al hacer clic en el botón de detener.
     * Reinicia la reproducción al principio del archivo de audio y pausa la reproducción.
     * Actualiza la interfaz de usuario y la barra de progreso.
     *
     * @param view La vista del botón clicado.
     */
    private void onClickStopButton(View view) {
        mediaPlayer.seekTo(0);
        mediaPlayer.pause();
        playPauseButton.setText(getString(R.string.activity_media_player_ButtonPlay));
        updateSeekBar();
    }

    /**
     * Acción ejecutada al hacer clic en el botón de avance rápido.
     * Avanza la reproducción en 10 segundos.
     * Actualiza la interfaz de usuario y la barra de progreso.
     *
     * @param view La vista del botón clicado.
     */
    private void onClickForwardButton(View view) {
        int currentPosition = mediaPlayer.getCurrentPosition();
        mediaPlayer.seekTo(currentPosition + 10000); // Avanzar 10 segundos
        updateSeekBar();
    }

    /**
     * Acción ejecutada al hacer clic en el botón de retroceso.
     * Retrocede la reproducción en 10 segundos.
     * Actualiza la interfaz de usuario y la barra de progreso.
     *
     * @param view La vista del botón clicado.
     */
    private void onClickBackwardButton(View view) {
        int currentPosition = mediaPlayer.getCurrentPosition();
        mediaPlayer.seekTo(currentPosition - 10000); // Retroceder 10 segundos
        updateSeekBar();
    }

    /**
     * Acción ejecutada al hacer clic en el botón de pista anterior.
     * Cambia a la pista anterior en la lista de reproducción o vuelve a la última si es la primera pista.
     * Actualiza la interfaz de usuario y la barra de progreso.
     *
     * @param view La vista del botón clicado.
     */
    private void onClickPrevButton(View view) {
        if (currentSongPosition > 0) {
            currentSongPosition--;
            resetMediaPlayerAndPlayNextSong();
        } else {
            currentSongPosition = songList.size() - 1;
            setSongPositionTo0AndPrepareForPlay();
        }
        inflateMediaPlayerCharacteristics();
        updateSeekBar();
    }

    /**
     * Obtiene un escuchador de cambios en la barra de progreso.
     *
     * @return El escuchador de cambios en la barra de progreso.
     */
    @NonNull
    private SeekBar.OnSeekBarChangeListener getOnSeekBarChangeListener() {
        return new SeekBar.OnSeekBarChangeListener() {
            /**
             * Método llamado cuando cambia el progreso de la barra de progreso.
             * Actualiza la posición de reproducción según sea necesario.
             *
             * @param seekBar  La barra de progreso.
             * @param progress La posición actual.
             * @param fromUser Indica si el cambio fue realizado por el usuario.
             */
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Actualiza la posición de reproducción según sea necesario
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                    updateChronometers(progress);
                }
            }

            /**
             * Método llamado cuando se comienza a realizar un seguimiento táctil en la barra de progreso.
             * Pausa la reproducción si está en curso.
             *
             * @param seekBar La barra de progreso.
             */
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    playPauseButton.setText(getString(R.string.activity_media_player_ButtonPlay));
                }
            }

            /**
             * Método llamado cuando se detiene el seguimiento táctil en la barra de progreso.
             * Reanuda la reproducción si estaba en pausa.
             *
             * @param seekBar La barra de progreso.
             */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                    playPauseButton.setText(getString(R.string.activity_media_player_ButtonPause));
                }
            }
        };
    }

    /**
     * Método llamado al completarse la reproducción de una pista.
     * Cambia a la siguiente pista en la lista de reproducción o vuelve al principio si es la última pista.
     * Actualiza la interfaz de usuario y la barra de progreso.
     *
     * @see MediaPlayerActivity#resetMediaPlayerAndPlayNextSong()
     * @see MediaPlayerActivity#setSongPositionTo0AndPrepareForPlay()
     */
    private void playNextSongIfNotLastOrPrepareFirstSong() {
        if (currentSongPosition < songList.size() - 1) {
            currentSongPosition++;
            resetMediaPlayerAndPlayNextSong();
        } else {
            currentSongPosition = 0;
            setSongPositionTo0AndPrepareForPlay();
        }
        inflateMediaPlayerCharacteristics();
        updateSeekBar();
    }

    /**
     * Reinicia el reproductor multimedia y reproduce la siguiente pista en la lista de reproducción.
     */
    private void resetMediaPlayerAndPlayNextSong() {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(songList.get(currentSongPosition));
            mediaPlayer.prepare();
            mediaPlayer.start();
            playPauseButton.setText(getString(R.string.activity_media_player_ButtonPause));
        } catch (IOException e) {
            Log.e("MediaPlayerActivity", "Error resetMediaPlayerAndPlayNextSong:\n" + e.getMessage(), e);
        }
    }

    /**
     * Establece la posición de la canción en 0 y prepara el reproductor para la reproducción.
     */
    private void setSongPositionTo0AndPrepareForPlay() {
        try {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.setDataSource(songList.get(currentSongPosition));
            mediaPlayer.prepare();
            playPauseButton.setText(getString(R.string.activity_media_player_ButtonPlay));
        } catch (IOException e) {
            Log.e("MediaPlayerActivity", "Error setSongPositionTo0AndPrepareForPlay:\n" + e.getMessage(), e);
        }
    }

    /**
     * Inicializa los elementos de la interfaz de usuario.
     * Asocia los elementos de la interfaz con las variables correspondientes.
     */
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

    /**
     * Configura la función de navegación de la barra de herramientas.
     * Si se presiona, navega de vuelta a la actividad principal.
     */
    private void toolbarNavigationFunction() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.activity_main_TextView_TOP));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(this, M08_Act03_MidiaPlayer.class);
            this.startActivity(intent);
            this.finish();
        });
    }

    /**
     * Inicializa los elementos del reproductor multimedia.
     * Configura el reproductor multimedia con la pista seleccionada.
     * Obtiene metadatos y actualiza la interfaz de usuario.
     */
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
     * Infla la interfaz de usuario con metadatos de la pista actual.
     * Muestra el título, autor y álbum de la pista, así como la imagen del álbum si está disponible.
     * Si no hay metadatos, se utilizan valores predeterminados.
     *
     * @see MediaPlayerActivity#bindMetadataToTextViewInPlayer()
     * @see MediaPlayerActivity#bindAlbumArtToImageViewInPlayer()
     */
    private void inflateMediaPlayerCharacteristics() {
        retriever = new MediaMetadataRetriever();
        String songPath = songList.get(currentSongPosition);
        try {
            retriever.setDataSource(songPath);
            bindMetadataToTextViewInPlayer();
            bindAlbumArtToImageViewInPlayer();

        } catch (Exception e) {
            Log.e(TAG, "Error in inflateMediaPlayerCharacteristics:\n" + e.getMessage(), e);
        } finally {
            try {
                retriever.release();
            } catch (IOException e) {
                Log.e(TAG, "Error Trying release retriever:\n" + e.getMessage(), e);
            }
        }
    }

    /**
     * Vincula los metadatos de la canción a los TextViews correspondientes.
     */
    private void bindMetadataToTextViewInPlayer() {
        String songTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        String songAuthor = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        String songAlbum = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);

        songTitleTextView.setText(Optional.ofNullable(songTitle)
                .orElse(getString(R.string.activity_media_player_songTitleTextView)));
        authorTextView.setText(Optional.ofNullable(songAuthor)
                .orElse(getString(R.string.activity_media_player_authorTextView)));
        albumTextView.setText(Optional.ofNullable(songAlbum)
                .orElse(getString(R.string.activity_media_player_albumTitleTextView)));
    }

    /**
     * Vincula la imagen del álbum al ImageView correspondiente.
     */
    private void bindAlbumArtToImageViewInPlayer() {
        byte[] albumArt = retriever.getEmbeddedPicture();
        if (albumArt != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(albumArt, 0, albumArt.length);
            albumImageView.setImageBitmap(bitmap);
        } else {
            int resourceId = R.drawable.unknown_album;
            albumImageView.setImageResource(resourceId);
        }
    }

    /**
     * Actualiza la barra de progreso y los cronómetros.
     * Actualiza la posición y duración actuales de la reproducción.
     *
     * @see MediaPlayerActivity#updateChronometers(int)
     */
    private void updateSeekBar() {
        if (mediaPlayer != null) {
            int currentDuration = mediaPlayer.getCurrentPosition();
            totalDuration = mediaPlayer.getDuration();
            seekBar.setMax(totalDuration);
            seekBar.setProgress(currentDuration);
            updateChronometers(currentDuration);
            if (mediaPlayer.isPlaying() && currentDuration < totalDuration) {
                handler.postDelayed(this::updateSeekBar, 1000); // Actualiza cada segundo
            }
        }
    }

    /**
     * Actualiza los cronómetros con el tiempo transcurrido y restante de la canción.
     *
     * @param currentDuration Duración actual de la canción en reproducción.
     */
    private void updateChronometers(int currentDuration) {
        int remainingDuration = totalDuration - currentDuration;

        chronometerStart.setBase(SystemClock.elapsedRealtime() - currentDuration);
        chronometerEnd.setBase(SystemClock.elapsedRealtime() + remainingDuration);

        if (mediaPlayer.isPlaying()) {
            chronometerStart.start();
            chronometerEnd.start();
        } else {
            chronometerStart.stop();
            chronometerEnd.stop();
        }
    }

    /**
     * Realiza tareas de limpieza cuando la actividad se destruye.
     * Llama a super.onDestroy().
     * Elimina los callbacks y mensajes del handler.
     * Libera el mediaPlayer si no es nulo.
     * Establece el mediaPlayer en nulo.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        handler.removeCallbacksAndMessages(null);

        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    /**
     * Método llamado antes de que la actividad sea destruida para guardar el estado actual.
     *
     * @param outState Objeto Bundle donde se guarda el estado de la actividad.
     *                 Se guardan la posición actual del reproductor multimedia,
     *                 la duración total de la canción, la posición de la canción actual
     *                 en la lista y el estado de reproducción del reproductor multimedia.
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("CURRENT_POSITION", mediaPlayer.getCurrentPosition());
        outState.putInt("TOTAL_DURATION", totalDuration);
        outState.putInt("CURRENT_SONG_POSITION", currentSongPosition);
        outState.putBoolean("IS_PLAYING", mediaPlayer.isPlaying());
    }

    /**
     * Método llamado después de que la actividad ha sido recreada para restaurar el estado guardado.
     *
     * @param savedInstanceState Objeto Bundle que contiene el estado guardado de la actividad.
     *                           Se recuperan la posición actual del reproductor multimedia,
     *                           la duración total de la canción, la posición de la canción actual
     *                           en la lista y el estado de reproducción del reproductor multimedia.
     *                           Se utiliza para configurar el reproductor multimedia y actualizar
     *                           la interfaz de usuario según sea necesario.
     */
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Restaura el estado guardado después de un cambio en la configuración
        int currentPosition = savedInstanceState.getInt("CURRENT_POSITION");
        totalDuration = savedInstanceState.getInt("TOTAL_DURATION");
        currentSongPosition = savedInstanceState.getInt("CURRENT_SONG_POSITION");
        boolean isPlaying = savedInstanceState.getBoolean("IS_PLAYING");

        if (mediaPlayer != null) {

            try {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(songList.get(currentSongPosition));
                mediaPlayer.prepare();
                mediaPlayer.seekTo(currentPosition);
                if (isPlaying) {
                    mediaPlayer.start();
                    playPauseButton.setText(getString(R.string.activity_media_player_ButtonPause));
                } else {
                    mediaPlayer.pause();
                    playPauseButton.setText(getString(R.string.activity_media_player_ButtonPlay));
                }
            } catch (IOException e) {
                Log.e(TAG, "onRestoreInstanceState: " + e.getMessage(), e);
            }

            updateSeekBar();
            inflateMediaPlayerCharacteristics();
        }
    }


    /*
    Variables de la clase
     */
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
    private MediaMetadataRetriever retriever;
    public static final String TAG = "MediaPlayerActivity";
}
