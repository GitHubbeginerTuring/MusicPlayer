package com.example.musicplayer;


import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import de.hdodenhof.circleimageview.CircleImageView;


public class SongsContentFragment extends Fragment {

    private static final String TAG = "SongsContentFragment";
    private SeekBar mSeekBar;
    private Button mToLast;
    private Button mToNext;
    private Button mPauseOrPlay;
    private Button mToList;
    private TextView mSongName;
    private TextView mSinger;
    private TextView mAlbum;
    private TextView mMills;
    private CircleImageView mCover;

    private ObjectAnimator mRotateAnimator;
    public static final int STATE_PLAYING = 1;//正在播放
    public static final int STATE_PAUSE = 2;//暂停
    public static final int STATE_STOP = 3;//停止
    public int mState;

    private View mRootView;
    SongsContentViewListener mSongsContentViewListener;
    MainActivity mMainActivity;


    public interface SongsContentViewListener {
        void onSeekBarChanged(int progress);
        void onPlayNextSong();
        void onPlayPreviousSong();
        void changeSongs(int index);
        void changePlayStatus();
    }



    public void registerListener(SongsContentViewListener listener) {
        mSongsContentViewListener = listener;
    }
    public void changeCoverByStatus() {
        if (mState == STATE_STOP) {
            mRotateAnimator.start();//动画开始
            mState = STATE_PLAYING;
        } else if (mState == STATE_PAUSE) {
            mRotateAnimator.resume();//动画继续
            mState = STATE_PLAYING;
        } else if (mState == STATE_PLAYING) {
            mRotateAnimator.pause();//动画暂停
            mState = STATE_PAUSE;
        }
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.songs_content_frag, container, false);
        Log.d(TAG, "onCreateView: ");
        mMainActivity = (MainActivity) getActivity();
      //  SharedPreferences pref = mMainActivity.getSharedPreferences("data", Context.MODE_PRIVATE);
      //  updateContent(pref.getInt(Constants.SONGS_INDEX, 0));
        initViews();
        return mRootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mMainActivity = (MainActivity) getActivity();
        Log.d(TAG, "onActivityCreated: ");
    }


    private void initViews() {
        Log.d(TAG, "initViews: ");
        //获取控件
        mCover=mRootView.findViewById(R.id.iv_cover);
        mSeekBar = mRootView.findViewById(R.id.seek_bar);
        mToLast = mRootView.findViewById(R.id.bt_last);
        mToNext = mRootView.findViewById(R.id.bt_next);
        mPauseOrPlay = mRootView.findViewById(R.id.bt_playOrPause);
        mToList = mRootView.findViewById(R.id.bt_toList);
        mSongName = mRootView.findViewById(R.id.tv_name);
        mSinger = mRootView.findViewById(R.id.tv_singer);
        mAlbum = mRootView.findViewById(R.id.tv_album);
        mMills = mRootView.findViewById(R.id.tv_mills);

        mState = mMainActivity.getCurrentStatus()?STATE_STOP:STATE_PLAYING;
        mRotateAnimator = ObjectAnimator.ofFloat(mCover, "Rotation", (float) 0.0, 360);
        mRotateAnimator.setDuration(20000);
        mRotateAnimator.setRepeatCount(Animation.INFINITE);//设定无限循环
        mRotateAnimator.setRepeatMode(ObjectAnimator.RESTART);// 循环模式
        mRotateAnimator.setInterpolator(new LinearInterpolator());// 匀速

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mSongsContentViewListener != null) {
                    mSongsContentViewListener.onSeekBarChanged(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        //为控件设置点击事件
        mToLast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSongsContentViewListener != null) {
                    mSongsContentViewListener.onPlayPreviousSong();
                }
                Toast.makeText(getActivity(), "ToLastSong", Toast.LENGTH_SHORT).show();
            }
        });

        mToNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSongsContentViewListener != null) {
                    mSongsContentViewListener.onPlayNextSong();
                }
                Toast.makeText(getActivity(), "ToNextSong", Toast.LENGTH_SHORT).show();
            }
        });

        mPauseOrPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCoverByStatus();
                if (mSongsContentViewListener != null) {
                    mSongsContentViewListener.changePlayStatus();
                    if (mMainActivity.getCurrentStatus()) {
                        mPauseOrPlay.setBackgroundResource(R.drawable.pause);

                    } else {
                        mPauseOrPlay.setBackgroundResource(R.drawable.play);
                    }
                }

            }
        });

        mToList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: list");
                mMainActivity.showFragment(MainActivity.FRAGMENT_SONGS_LIST);
            }
        });
    }

    public void updateContent(int currentPosition) {
        Log.d(TAG, "updateContent: ");
        mMainActivity=(MainActivity) getActivity();
        initViews();
        mCover.setImageBitmap(mMainActivity.getSongCover(currentPosition));
        mSongName.setText(mMainActivity.getSongTitle(currentPosition));
        mSinger.setText(mMainActivity.getSongSinger(currentPosition));
        mAlbum.setText(mMainActivity.getSongAlbum(currentPosition));
        mMills.setText(mMainActivity.millsTransfer(mMainActivity.getSongDuration(currentPosition)));
        mSeekBar.setMax(mMainActivity.getSongDuration(currentPosition));
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.d(TAG, "onHiddenChanged: ");
        if (!hidden) {
            initViews();
        }
    }
}
