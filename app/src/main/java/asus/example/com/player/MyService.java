package asus.example.com.player;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import java.io.IOException;
import java.util.ArrayList;

public class MyService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    private       MediaPlayer     player;
    private       ArrayList<Song> songs;
    private       int             songPos;
    private final IBinder         MUSICAL_BIND = new MusicBinder();

    public MyService() {
    }

    @Override
    public void onCreate(){
        super.onCreate();
        songPos = 0;
        player = new MediaPlayer();
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
        initMusicPlayer();
    }

    public void initMusicPlayer(){
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return MUSICAL_BIND;
    }

    @Override
    public boolean onUnbind(Intent intent){
        player.stop();
        player.release();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        playNext();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();

        Intent intentPrev = new Intent(Constants.ACTION_PREV);
        PendingIntent pendingIntentPrev = PendingIntent.getBroadcast(this, Constants.PREV_CODE,
                intentPrev, PendingIntent.FLAG_UPDATE_CURRENT);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intentPrev);

        Intent intentPlay = new Intent(Constants.ACTION_PLAY);
        PendingIntent pendingIntentPlay = PendingIntent.getBroadcast(this, Constants.PAUSE_CODE,
                intentPlay, PendingIntent.FLAG_UPDATE_CURRENT);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intentPlay);

        Intent intentNext = new Intent(Constants.ACTION_NEXT);
        PendingIntent pendingIntentNext = PendingIntent.getBroadcast(this, Constants.NEXT_CODE,
                intentNext, PendingIntent.FLAG_UPDATE_CURRENT);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intentNext);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this).
                setSmallIcon(R.drawable.notification).setContentTitle(songs.get(songPos).getTitle()).
                setContentText(songs.get(songPos).getArtist()).
                addAction(R.drawable.previous, Constants.ACTION_PREV, pendingIntentPrev).
                addAction(R.drawable.pause, Constants.ACTION_PAUSE, pendingIntentPlay).
                addAction(R.drawable.next, Constants.ACTION_NEXT, pendingIntentNext);
        Notification notification = builder.build();

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
        Intent onPreparedIntent = new Intent(Constants.ACTION_MEDIA_PLAYER_PREPARED);
        LocalBroadcastManager.getInstance(this).sendBroadcast(onPreparedIntent);

    }

    public void setList(ArrayList<Song> songs){
        this.songs = songs;
    }

    class MusicBinder extends Binder{
        MyService getService(){
            return MyService.this;
        }
    }

    public void setSong(int songIndex){
        songPos = songIndex;
    }

    public void playSong(){
        player.reset();
        Song playSong = songs.get(songPos);
        long curSong = playSong.getId();
        Uri trackURI = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, curSong);
        try {
            player.setDataSource(getApplicationContext(), trackURI);
        } catch (IOException e) {
            e.printStackTrace();
        }
        player.prepareAsync();
    }

    public int getPos(){
        return player.getCurrentPosition();
    }

    public int getDur(){
        return player.getDuration();
    }

    public boolean isPlaying(){
        return player.isPlaying();
    }

    public void pausePlayer(){
        player.pause();
    }

    public void seek(int posn){
        player.seekTo(posn);
    }

    public void go(){
        player.start();
    }

    public void playPrev(){
        if (songPos==0){
            songPos = songs.size()-1;
        }
        else {
            songPos--;
        }
        playSong();
    }

    public void playNext(){
        songPos++;
        if (songPos==songs.size()){
            songPos=0;
        }
        playSong();
    }


}