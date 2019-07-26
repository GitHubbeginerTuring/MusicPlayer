package com.example.musicplayer;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements SongsContentFragment.SongsContentViewListener {
    FragmentManager fragmentManager = getSupportFragmentManager();
    FragmentTransaction ft = fragmentManager.beginTransaction();
    private Handler mHandler = new Handler();
    private static final String TAG = "MainActivity";
    public MyService.MyBinder mMyBinder;
    public SongsContentFragment mSongsContentFragment;
    public SongsFragment mSongsFragment;

    public static final int FRAGMENT_SONGS_CONTENT = 0;
    public static final int FRAGMENT_SONGS_LIST = 1;
    private int position;
    private Boolean mStatus;
    int mCurrentPosition;
    public ArrayList<Songs> mSongsList;

//    public class InitTask extends AsyncTask<String,Integer,Integer> {
//        //任务执行前
//        @Override
//        protected void onPreExecute() {
//            Toast.makeText(MainActivity.this,"Initialize Start...",Toast.LENGTH_SHORT).show();
//        }
//
//        //获取专辑封面方法
//        private Bitmap getAlbumArt(int album_id) {
//            String mUriAlbums = "content://media/external/audio/albums";
//            String[] projection = new String[]{"album_art"};
//            Cursor cur = getContentResolver().query
//                    (Uri.parse(mUriAlbums + "/" + Integer.toString(album_id)), projection, null, null, null);
//            String album_art = null;
//            if (cur.getCount() > 0 && cur.getColumnCount() > 0) {
//                cur.moveToNext();
//                album_art = cur.getString(0);
//            }
//            cur.close();
//            Bitmap bm = null;
//            if (album_art != null) {
//                bm = BitmapFactory.decodeFile(album_art);
//            } else {
//                bm = BitmapFactory.decodeResource(getResources(), R.drawable.default_cover);
//            }
//            return bm;
//        }
//
//        //任务执行时
//        @Override
//        protected Integer doInBackground(String... params) {
//
//                Log.d(TAG, "initializeSongs: ");
//                Cursor c = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
//                        null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
//                try {
//                    while (c.moveToNext()) {
//                        Songs songs = new Songs();
//                        songs.setPath(c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)));
//                        songs.setName(c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)));
//                        songs.setAlbum(c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)));
//                        songs.setSinger(c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)));
//                        songs.setMills(c.getInt(c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)));
//                        int albumId = c.getInt(c.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
//                        //根据专辑ID获取到专辑封面图
//                        songs.setCover(getAlbumArt(albumId));
//                        mSongsList.add(songs);
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                } finally {
//                    if (c != null) {
//                        c.close();
//                    }
//                }
//                return 0;
//        }
//
//
//        @Override
//        protected void onPostExecute(Integer Status) {
//            Toast.makeText(MainActivity.this,"Initialize Finished!",Toast.LENGTH_SHORT).show();
//        }
//    }


    //链接活动与服务
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMyBinder = (MyService.MyBinder) service;
            //mHandler.post(mRunnable);
            Log.d(TAG, "Service与Activity已连接");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        //请求获取权限
        if (ContextCompat.checkSelfPermission
                (MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        } else {
            //初始化布局
            showFragment(FRAGMENT_SONGS_CONTENT);
        }
        Intent serviceIntent = new Intent(this, MyService.class);
        bindService(serviceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    public void hideFragment(FragmentTransaction ft) {
        //如果不为空，就先隐藏起来
        if (mSongsContentFragment != null) {
            ft.hide(mSongsContentFragment);
        }
        if (mSongsFragment != null) {
            ft.hide(mSongsFragment);
        }
    }

    public void showFragment(int index) {

        FragmentTransaction ft = fragmentManager.beginTransaction();
        hideFragment(ft);

        //注意这里设置位置
        position = index;

        switch (index) {
            case FRAGMENT_SONGS_CONTENT:
                /**
                 * 如果Fragment为空，就新建一个实例
                 * 如果不为空，就将它从栈中显示出来
                 */
                if (mSongsContentFragment == null) {
                    mSongsContentFragment = new SongsContentFragment();
                    mSongsContentFragment.registerListener(this);
                    ft.add(R.id.replaced_layout, mSongsContentFragment);
                } else {
                    ft.show(mSongsContentFragment);
                }
                break;
            case FRAGMENT_SONGS_LIST:
                if (mSongsFragment == null) {
                    mSongsFragment = new SongsFragment();
                    ft.add(R.id.replaced_layout, mSongsFragment);
                } else {
                    ft.show(mSongsFragment);
                }
                break;
        }

        ft.commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(this, "you denied the permission", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }
    @Override
    public void onSeekBarChanged(int progress) {
        if (mMyBinder != null) {
            mMyBinder.seekToPosition(progress);
        }
    }






    @Override
    public void onPlayNextSong() {

        if (mMyBinder != null) {
            mCurrentPosition=mMyBinder.nextMusic();
        }
        mSongsContentFragment.updateContent(mCurrentPosition);
    }

    @Override
    public void onPlayPreviousSong() {
        if (mMyBinder != null) {
            mCurrentPosition=mMyBinder.previousMusic();
        }

        mSongsContentFragment.updateContent(mCurrentPosition);

    }

    public Bitmap getSongCover(int currentPosition) {

        if (mMyBinder != null) {
            return mMyBinder.getCurrentCover(currentPosition);
        }
        return null;
    }

    public String getSongTitle(int currentPosition) {
        Log.d(TAG, "getSongTitle: ");
        if (mMyBinder != null) {
            return mMyBinder.getCurrentTitle(currentPosition);
        }
        return null;
    }

    public String getSongSinger(int currentPosition) {
        if (mMyBinder != null) {
            return mMyBinder.getCurrentSinger(currentPosition);
        }
        return null;
    }

    public String getSongAlbum(int currentPosition) {
        if (mMyBinder != null) {
            return mMyBinder.getCurrentAlbum(currentPosition);
        }
        return null;
    }

    public int getSongDuration(int currentPosition) {
        if (mMyBinder != null) {
            return mMyBinder.getDuration(currentPosition);
        }
        return 0;
    }

    public boolean getCurrentStatus() {
        if (mMyBinder != null) {
            return mMyBinder.getCurrentStatus();
        }
        return false;
    }

    public String getSongPath(int currentPosition) {
        if (mMyBinder != null) {
            return mMyBinder.getCurrentPath(currentPosition);
        }
        return null;
    }

    public ArrayList<Songs> getSongsList() {
        if (mMyBinder != null) {
            return mMyBinder.getSongsList();
        }
        return null;
    }

    public void changeSongs(int currentPosition) {
        mMyBinder.playSongByPosition(currentPosition);
        mSongsContentFragment.changeCoverByStatus();
        mSongsContentFragment.updateContent(currentPosition);

        showFragment(MainActivity.FRAGMENT_SONGS_CONTENT);
        mCurrentPosition=currentPosition;
        mMyBinder.changeCurrentPosition(currentPosition);
   //     mHandler.postDelayed(mRunnable, 1000);
    }

    @Override
    public void changePlayStatus() {
        mMyBinder.changePlayStatus(mCurrentPosition);
    }

    public int getCurrentPosition() {
        return mMyBinder.getCurrentPosition();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //我们的handler发送是定时1000s发送的，如果不关闭，MediaPlayer release掉了还在获取getCurrentPosition就会爆IllegalStateException错误
 //       mHandler.removeCallbacks(mRunnable);
        mMyBinder.closeMedia();
        unbindService(mServiceConnection);

    }

    @Override
    protected void onStop() {
        super.onStop();
        //存入stop时的播放位置
        SharedPreferences.Editor editor = getSharedPreferences("data", Context.MODE_PRIVATE).edit();
        editor.putInt(Constants.SONGS_INDEX,mCurrentPosition);
         editor.putBoolean(Constants.SONG_STATUS, getCurrentStatus());
        Log.d("stop", "onStop: "+mCurrentPosition);
        editor.apply();
    }

//    private Runnable mRunnable = new Runnable() {
//        @Override
//        public void run() {
//            mSongsContentFragment.updateContent(mCurrentPosition);
//            mHandler.postDelayed(mRunnable, 1000);
//        }
//    };

    public String millsTransfer(int i) {
        int second = (i / 1000) % 60;
        int minute = (i / 1000)/60;
        return minute + ":" + second;
    }

}

