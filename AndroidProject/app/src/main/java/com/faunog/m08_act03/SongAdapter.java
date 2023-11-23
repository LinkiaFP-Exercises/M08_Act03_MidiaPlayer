package com.faunog.m08_act03;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {

    private final List<String> songList;

    // Constructor que recibe la lista de canciones
    public SongAdapter(List<String> songList) {
        this.songList = songList;
    }

    // Clase ViewHolder que representa cada elemento en el RecyclerView
    public static class ViewHolder extends RecyclerView.ViewHolder {
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
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Infla el diseño de cada elemento
        View songView = inflater.inflate(R.layout.item_song, parent, false);

        // Devuelve una nueva instancia del ViewHolder
        return new ViewHolder(songView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String songTitle = songList.get(position);

        // Aquí obtén información adicional sobre la canción, autor, álbum, y la imagen del álbum
        // Puedes usar la posición para acceder a los datos en otras listas o recursos

        // Actualiza la vista del ViewHolder con los datos de la canción
        holder.songTitleTextView.setText(songTitle);
        // Similarmente, actualiza otras vistas según sea necesario
    }

    @Override
    public int getItemCount() {
        // Devuelve el tamaño de la lista de canciones
        return songList.size();
    }
}
