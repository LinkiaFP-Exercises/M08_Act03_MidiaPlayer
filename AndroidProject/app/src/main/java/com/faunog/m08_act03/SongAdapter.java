package com.faunog.m08_act03;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(String songPath);
    }

    private final List<String> songList;
    private final Context context;
    private final OnItemClickListener listener;

    public SongAdapter(List<String> songList, Context context, OnItemClickListener listener) {
        this.songList = songList;
        this.context = context;
        this.listener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView songTitleTextView;
        public TextView authorTextView;
        public TextView albumTextView;
        public ImageView albumImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            songTitleTextView = itemView.findViewById(R.id.songTitleTextView);
            authorTextView = itemView.findViewById(R.id.authorTextView);
            albumTextView = itemView.findViewById(R.id.albumTextView);
            albumImageView = itemView.findViewById(R.id.albumImageView);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(songList.get(position));
                }
            });
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View songView = inflater.inflate(R.layout.item_song, parent, false);

        return new ViewHolder(songView);
    }

    /** @noinspection resource*/
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String songPath = songList.get(position);

        @SuppressWarnings("ResourceType") MediaMetadataRetriever retriever = new MediaMetadataRetriever();

        try {
            retriever.setDataSource(context, Uri.parse(songPath));

            String songTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            String songAuthor = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            String songAlbum = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            byte[] albumArt = retriever.getEmbeddedPicture();

            holder.songTitleTextView.setText(songTitle != null ? songTitle : context.getText(R.string.activity_media_player_songTitleTextView));

            holder.authorTextView.setText(songAuthor != null ? songAuthor : context.getText(R.string.activity_media_player_authorTextView));

            holder.albumTextView.setText(songAlbum != null ? songAlbum : context.getText(R.string.activity_media_player_albumTitleTextView));

            if (albumArt != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(albumArt, 0, albumArt.length);
                holder.albumImageView.setImageBitmap(bitmap);
            } else {
                int resourceId = R.drawable.unknown_album;
                holder.albumImageView.setImageResource(resourceId);
            }
        } catch (Exception e) {
            Log.e("SongAdapter", "Error processing song data:\n" + e.getMessage(), e);
        } finally {
            try {
                retriever.release();
            } catch (IOException e) {
                Log.e("SongAdapter", "Error Trying release retriever:\n" + e.getMessage(), e);
            }
        }
    }

    @Override
    public int getItemCount() {
        // Devuelve el tamaño de la lista de canciones
        return songList.size();
    }
}
