package com.faunog.m08_act03;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador personalizado para la lista de canciones en un RecyclerView.
 *
 * @author <a href="https://about.me/prof.guazina">Fauno Guazina</a>
 * @version 1.1
 * @since 29/11/2023
 */
public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {

    /**
     * Constructor de la clase SongAdapter.
     *
     * @param songList Lista de rutas de archivos de canciones.
     * @param context  Contexto de la aplicación.
     * @param listener Interfaz para manejar eventos de clic en los elementos de la lista.
     */
    public SongAdapter(List<String> songList, Context context, OnItemClickListener listener) {
        this.songList = songList;
        this.context = context;
        this.listener = listener;
    }

    /**
     * Crea una nueva instancia de ViewHolder cuando se necesita.
     *
     * @param parent   Vista padre a la que se adjuntará el nuevo ViewHolder.
     * @param viewType Tipo de vista del nuevo ViewHolder.
     * @return Nuevo ViewHolder creado.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View songView = inflater.inflate(R.layout.item_song, parent, false);
        return new ViewHolder(songView);
    }

    /**
     * Rellena los datos de la vista del elemento en la posición dada.
     *
     * @param holder   ViewHolder que debe actualizarse para representar el contenido de la posición.
     * @param position Posición del elemento en la lista.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        retriever = new MediaMetadataRetriever();
        String songPath = songList.get(position);
        File file = new File(songPath);
        if (file.exists()) {
            try {
                retriever.setDataSource(songPath);
                bindMetadataToViewHolder(holder);
                bindAlbumArtToViewHolder(holder);

            } catch (Exception e) {
                Log.e(TAG, "Error processing song data:\n" + e.getMessage(), e);

            } finally {
                try {
                    retriever.release();
                } catch (IOException e) {
                    Log.e(TAG, "Error Trying release retriever:\n" + e.getMessage(), e);
                }
            }
        } else {
            Log.e(TAG, "File does not exist: " + songPath);
        }
    }

    /**
     * Vincula los metadatos de la canción al ViewHolder.
     *
     * @param holder ViewHolder que debe actualizarse.
     */
    private void bindMetadataToViewHolder(ViewHolder holder) {
        String songTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        String songAuthor = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        String songAlbum = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);

        holder.songTitleTextView.setText(Optional.ofNullable(songTitle)
                .orElse(context.getString(R.string.activity_media_player_songTitleTextView)));
        holder.authorTextView.setText(Optional.ofNullable(songAuthor)
                .orElse(context.getString(R.string.activity_media_player_authorTextView)));
        holder.albumTextView.setText(Optional.ofNullable(songAlbum)
                .orElse(context.getString(R.string.activity_media_player_albumTitleTextView)));
    }

    /**
     * Vincula la imagen del álbum al ViewHolder.
     *
     * @param holder ViewHolder que debe actualizarse.
     */
    private void bindAlbumArtToViewHolder(ViewHolder holder) {
        byte[] albumArt = retriever.getEmbeddedPicture();
        if (albumArt != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(albumArt, 0, albumArt.length);
            holder.albumImageView.setImageBitmap(bitmap);
        } else {
            int resourceId = R.drawable.unknown_album;
            holder.albumImageView.setImageResource(resourceId);
        }
    }

    /**
     * Obtiene la cantidad total de elementos en la lista.
     *
     * @return Cantidad total de elementos.
     */
    @Override
    public int getItemCount() {
        return songList.size();
    }

    /**
     * ViewHolder que representa la vista de cada elemento en el RecyclerView.
     */
    public class ViewHolder extends RecyclerView.ViewHolder {

        /**
         * Constructor del ViewHolder.
         *
         * @param itemView Vista de un elemento en la lista.
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            initializeInterfaceElements(itemView);
            itemView.setOnClickListener(this::onClickItenView);
        }


        /**
         * Inicializa los elementos de la interfaz dentro del ViewHolder.
         *
         * @param itemView Vista de un elemento en la lista.
         */
        private void initializeInterfaceElements(@NonNull View itemView) {
            songTitleTextView = itemView.findViewById(R.id.songTitleTextView);
            authorTextView = itemView.findViewById(R.id.authorTextView);
            albumTextView = itemView.findViewById(R.id.albumTextView);
            albumImageView = itemView.findViewById(R.id.albumImageView);
        }


        /**
         * Maneja el evento de clic en un elemento de la lista.
         *
         * @param view Vista del elemento en el que se hizo clic.
         */
        private void onClickItenView(View view) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION && listener != null) {
                listener.onItemClick(songList.get(position));
            }
        }

        // Variables de clase ViewHolder
        public TextView songTitleTextView;
        public TextView authorTextView;
        public TextView albumTextView;
        public ImageView albumImageView;
    }

    /**
     * Interfaz para manejar eventos de clic en los elementos de la lista.
     */
    public interface OnItemClickListener {
        void onItemClick(String songPath);

    }

    // Variables de la clase SongAdapter
    private final List<String> songList;
    private final Context context;
    private final OnItemClickListener listener;
    private MediaMetadataRetriever retriever;
    private static final String TAG = "SongAdapter";
}
