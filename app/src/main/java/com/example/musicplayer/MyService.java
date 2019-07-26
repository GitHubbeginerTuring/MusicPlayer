package com.example.musicplayer;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MyService extends Service implements MediaPlayer.OnPreparedListener {
    //标记当前歌曲的序号
    private int mCurrentPosition = 0;
    private ArrayList<Songs> mSongsList = new ArrayList<>();
    private MyBinder mBinder = new MyBinder();

    //初始化MediaPlayer
    public MediaPlayer mMediaPlayer = new MediaPlayer();

    private NotificationManager mNotificationManager;



    public MyService() {
    }

    private static final String TAG = "MyService";

    private Boolean mCurrentStatus;


    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: ");
        super.onCreate();
        // shared preference 从仓库获取信息
        SharedPreferences pref = getApplicationContext().getSharedPreferences("data", Context.MODE_PRIVATE);
        mCurrentPosition = pref.getInt(Constants.SONGS_INDEX, 0);
        mCurrentStatus = pref.getBoolean(Constants.SONG_STATUS, false);
        initializeSongs();
        iniMediaPlayerFile(mCurrentPosition);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    private Bitmap getAlbumArt(int album_id) {
        String mUriAlbums = "content://media/external/audio/albums";
        String[] projection = new String[]{"album_art"};
        Cursor cur = getContentResolver().query
                (Uri.parse(mUriAlbums + "/" + Integer.toString(album_id)), projection, null, null, null);
        String album_art = null;
        if (cur.getCount() > 0 && cur.getColumnCount() > 0) {
            cur.moveToNext();
            album_art = cur.getString(0);
        }
        cur.close();
        Bitmap bm = null;
        if (album_art != null) {
            bm = BitmapFactory.decodeFile(album_art);
        } else {
            bm = BitmapFactory.decodeResource(getResources(), R.drawable.default_cover);
        }
        return bm;
    }

    public void initializeSongs() {
        Log.d(TAG, "initializeSongs: ");
        Cursor c = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
                null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        try {
            while (c.moveToNext()) {
                Songs songs = new Songs();
                songs.setPath(c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)));
                songs.setName(c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)));
                songs.setAlbum(c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)));
                songs.setSinger(c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)));
                songs.setMills(c.getInt(c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)));
                int albumId = c.getInt(c.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                //根据专辑ID获取到专辑封面图
                songs.setCover(getAlbumArt(albumId));
                mSongsList.add(songs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    private String getMusicPath(int index) {
        if (index < 0 || index > mSongsList.size()) {
            return null;
        }
        return mSongsList.get(index).getPath();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //如果关闭应用前是播放状态
        if (!mSongsList.isEmpty()) {
            final Songs songs = mSongsList.get(mCurrentPosition);
//            mCurrentAlbum = songs.getAlbum();
//            mCurrentTitle = songs.getName();
//            mCurrentPath = songs.getPath();
        }
        if (mCurrentStatus) {
            mp.start();
        }
    }

    public class MyBinder extends Binder {

        public int getCurrentPosition() {
            return mCurrentPosition;
        }

        public Bitmap getCurrentCover(int currentPosition) {
            return mSongsList.get(currentPosition).getCover();
        }

        public String getCurrentTitle(int currentPosition) {
            Log.d(TAG, "getCurrentTitle: " + mSongsList.get(currentPosition).getName());
            return mSongsList.get(currentPosition).getName();
        }

        public String getCurrentAlbum(int currentPosition) {
            return mSongsList.get(currentPosition).getAlbum();
        }

        public String getCurrentSinger(int currentPosition) {
            return mSongsList.get(currentPosition).getSinger();
        }

        public String getCurrentPath(int currentPosition) {
            return mSongsList.get(currentPosition).getPath();
        }

        public Boolean getCurrentStatus() {
            return mCurrentStatus;

        }

        public int getDuration(int currentPosition) {
            return mSongsList.get(currentPosition).getMills();
        }

        public ArrayList<Songs> getSongsList() {
            return mSongsList;
        }

        public void playMusic() {
            if (!mMediaPlayer.isPlaying()) {
                //如果还没开始播放，就开始
                mMediaPlayer.start();
            }
        }

        public Boolean changePlayStatus(int currentPosition) {
            if (currentPosition != mCurrentPosition) {
                //如果开始播放，且播放的时列表中其他歌曲
                Log.d("testttt", "currentPosition ="+currentPosition+"mc.."+mCurrentPosition);
                mCurrentStatus = true;
            } else if (mCurrentStatus == true) {
                mCurrentStatus = false;
            } else {
                mCurrentStatus = true;
            }
            return mCurrentStatus;

        }

        /**
         * 下一首
         */
        public int nextMusic() {
            if (mMediaPlayer != null && mCurrentPosition < mSongsList.size() && mCurrentPosition >= 0) {
                //切换歌曲reset()很重要很重要很重要，没有会报IllegalStateException
                mMediaPlayer.reset();
                //这里的if只是为了不让歌曲的序号越界
                if (mCurrentPosition == mSongsList.size() - 1) {
                    iniMediaPlayerFile(mCurrentPosition);
                } else {
                    mCurrentPosition += 1;
                    iniMediaPlayerFile(mCurrentPosition);
                }
                playMusic();
            }
            return mCurrentPosition;
        }

        /**
         * 上一首
         */
        public int previousMusic() {
            if (mMediaPlayer != null && mCurrentPosition < mSongsList.size() && mCurrentPosition > 0) {
                mMediaPlayer.reset();
                if (mCurrentPosition == 0) {
                    iniMediaPlayerFile(mCurrentPosition);
                } else {
                    mCurrentPosition -= 1;
                    iniMediaPlayerFile(mCurrentPosition);
                }
                playMusic();
            }
            return mCurrentPosition;
        }

        public void playSongByPosition(int position) {
            if (mMediaPlayer == null) {
                return;
            }
            if (position < 0 || position >= mSongsList.size()) {
                return;
            }
            mMediaPlayer.reset();
            iniMediaPlayerFile(position);
            playMusic();
        }

        /**
         * 关闭播放器
         */
        public void closeMedia() {
            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
            }
        }


        /**
         * 播放指定位置
         */
        public void seekToPosition(int msec) {
            mMediaPlayer.seekTo(msec);
        }

        public void changeCurrentPosition(int currentPosition) {
            mCurrentPosition=currentPosition;
        }
    }

    private void iniMediaPlayerFile(int position) {
        //获取文件路径
        try {
            //此处的两个方法需要捕获IO异常
            //设置音频文件到MediaPlayer对象中
            mMediaPlayer.setDataSource(getMusicPath(position));
            //让MediaPlayer对象准备
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            Log.d(TAG, "设置资源，准备阶段出错");
            e.printStackTrace();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

    }

}
