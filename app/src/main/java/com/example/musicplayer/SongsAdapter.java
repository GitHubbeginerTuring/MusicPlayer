package com.example.musicplayer;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.ViewHolder> {
    private List<Songs> mSongsLists;
    private ItemClickListener mItemClickListener;

    public interface ItemClickListener {
        void ItemClick(int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView mSongsName;
        TextView mSinger;
        TextView mAlbum;
        TextView mSongsMills;
        View songs_view;

        public ViewHolder(View v) {
            super(v);
            songs_view = v;
            mSongsName = v.findViewById(R.id.tv_name);
            mSinger = v.findViewById(R.id.tv_singer);
            mAlbum = v.findViewById(R.id.tv_album);
            mSongsMills = v.findViewById(R.id.tv_mills);
        }
    }

    public SongsAdapter(List<Songs> SongsList, ItemClickListener listener) {
        mSongsLists = SongsList;
        mItemClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.songs_item, viewGroup, false);
        final ViewHolder vh = new ViewHolder(v);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = vh.getAdapterPosition();
                Log.d("about", "!hidden");
                if (mItemClickListener != null) {
                    mItemClickListener.ItemClick(position);
                }
            }
        });
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Songs Songs = mSongsLists.get(position);
        holder.mSongsName.setText(Songs.getName());
        holder.mSinger.setText(Songs.getSinger());
        holder.mAlbum.setText(Songs.getAlbum());
        holder.mSongsMills.setText(millsTransfer(Songs.getMills()));
    }

    @Override
    public int getItemCount() {
        return mSongsLists.size();
    }

    public String millsTransfer(int i) {
        int second = (i / 1000) % 60;
        int minute = (i / 1000) / 60;
        return minute + ":" + second;
    }
}