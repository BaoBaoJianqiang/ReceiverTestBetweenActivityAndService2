package jianqiang.com.receivertestbetweenactivityandservice;

import android.app.Service;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Binder;
import android.os.IBinder;

import java.io.IOException;

import jianqiang.com.receivertestbetweenactivityandservice.data.IServiceInterface;
import jianqiang.com.receivertestbetweenactivityandservice.data.MyMusics;

public class MyService extends Service {

    AssetManager am;

    MediaPlayer mPlayer;
    int status = 0x11;
    int current = 0;

    private class MyBinder extends Binder implements IServiceInterface {

        @Override
        public void play() {
            if (status == 0x11) {
                prepareAndPlay(MyMusics.musics[current].name);
                status = 0x12;
            } else if (status == 0x12) {
                mPlayer.pause();
                status = 0x13;
            } else if (status == 0x13) {
                mPlayer.start();
                status = 0x12;
            }

            sendMessageToActivity(status, current);
        }

        @Override
        public void stop() {
            if (status == 0x12 || status == 0x13) {
                mPlayer.stop();
                status = 0x11;
            }

            sendMessageToActivity(status, current);
        }
    }

    MyBinder myBinder = null;

    @Override
    public void onCreate() {
        myBinder = new MyBinder();

        am = getAssets();

        mPlayer = new MediaPlayer();
        mPlayer.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                current++;
                if (current >= 3) {
                    current = 0;
                }
                prepareAndPlay(MyMusics.musics[current].name);

                sendMessageToActivity(-1, current);
            }
        });
        super.onCreate();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        myBinder = null;
        return super.onUnbind(intent);
    }

    private void sendMessageToActivity(int status1, int current1) {
        Intent sendIntent = new Intent("UpdateActivity");
        sendIntent.putExtra("status", status1);
        sendIntent.putExtra("current", current1);
        sendBroadcast(sendIntent);
    }

    private void prepareAndPlay(String music) {
        try {
            AssetFileDescriptor afd = am.openFd(music);
            mPlayer.reset();
            mPlayer.setDataSource(afd.getFileDescriptor()
                    , afd.getStartOffset()
                    , afd.getLength());
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}