package com.example.musicplayer;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SongsFragment extends Fragment implements SongsAdapter.ItemClickListener
{
    private MainActivity mMainActivity;
    private View mRootView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.songs_frag, container, false);
        return mRootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mMainActivity = (MainActivity) getActivity();
        RecyclerView rv = mRootView.findViewById(R.id.songs_recycler_view);
        LinearLayoutManager lm = new LinearLayoutManager(mMainActivity);
        rv.setLayoutManager(lm);
        SongsAdapter na = new SongsAdapter(mMainActivity.getSongsList(), this);
        rv.setAdapter(na);
    }

    @Override
    public void ItemClick(int position) {
        mMainActivity.changeSongs(position);
    }



}