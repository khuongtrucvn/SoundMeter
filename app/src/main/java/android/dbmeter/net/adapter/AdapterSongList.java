package android.dbmeter.net.adapter;

import android.content.Context;
import android.dbmeter.net.R;
import android.dbmeter.net.databinding.ItemFragmentMusicPlayerSongListBinding;
import android.dbmeter.net.model.Song;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterSongList extends RecyclerView.Adapter<AdapterSongList.SongListViewHolder> {
    private List<Song> listData;
    private Context context;
    private LayoutInflater inflater;
    private AdapterSongList.OnItemClickedListener onItemClickedListener;

    class SongListViewHolder extends RecyclerView.ViewHolder {
        private ItemFragmentMusicPlayerSongListBinding binding;

        SongListViewHolder(ItemFragmentMusicPlayerSongListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface OnItemClickedListener {
        void onItemClick(int index);
    }

    public AdapterSongList(Context context, List<Song> list) {
        this.context = context;
        this.listData = list;
    }

    @NonNull
    @Override
    public AdapterSongList.SongListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (inflater == null)
            inflater = LayoutInflater.from(parent.getContext());
        ItemFragmentMusicPlayerSongListBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.item_fragment_music_player_song_list, parent, false);
        return new AdapterSongList.SongListViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull final AdapterSongList.SongListViewHolder holder, final int position) {
        Song song = listData.get(position);

        holder.binding.songTitle.setText(song.getSongTitle());
        holder.binding.songDescription.setText(song.getSongDescription());

        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.image_music_frame)
                .error(R.drawable.image_music_frame);
        Glide.with(context).load(song.getSongThumbnail()).apply(options).into(holder.binding.songThumbnail);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onItemClickedListener != null) {
                    onItemClickedListener.onItemClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    public void setOnItemClickedListener(AdapterSongList.OnItemClickedListener onItemClickedListener) {
        this.onItemClickedListener = onItemClickedListener;
    }

    public void notify(List<Song> list) {
        listData.clear();
        listData.addAll(list);
        notifyDataSetChanged();
    }
}