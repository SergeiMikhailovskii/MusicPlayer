package asus.example.com.player;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;

import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;


public class ListOfSongsActivity extends AppCompatActivity implements MediaController.MediaPlayerControl {

    private       ArrayList<Song> songsList;
    private final String          TAG            = this.getClass().getSimpleName();
    private final String          ARTIST         = "ARTIST";
    private final String          SONG           = "SONG";
    private       MyService       musicService;
    private       Intent          playIntent;
    private       boolean         musicBounds    = false;
    private       MusicController controller;
    private       boolean         paused         = false;
    private       boolean         playbackPaused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_songs);
        ListView songsListView = findViewById(R.id.songList);
        songsList = new ArrayList<>();
        if (isStoragePermissionGranted()) {
            getSongsList();
        }
        Collections.sort(songsList, new Comparator<Song>() {
            @Override
            public int compare(Song o1, Song o2) {
                return o1.getTitle().compareTo(o2.getTitle());
            }
        });
        SongAdapter adapter = new SongAdapter(this, songsList);
        songsListView.setAdapter(adapter);
        songsListView.setOnItemClickListener(onClickListener);
//        songsListView.setOnItemLongClickListener(onLongClickListener);
        registerForContextMenu(songsListView);
        setController();
        registerReceiver(NotificationReceiver, new IntentFilter(Constants.ACTION_PREV));
        registerReceiver(NotificationReceiver, new IntentFilter(Constants.ACTION_NEXT));
        registerReceiver(NotificationReceiver, new IntentFilter(Constants.ACTION_PLAY));
        Toast.makeText(getApplicationContext(), "On create finished!", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (playIntent == null){
            playIntent = new Intent(this, MyService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        paused = true;
    }

    @Override
    protected void onResume(){
        super.onResume();
        if (paused){
            setController();
            paused = false;
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(onPrepareReceiver, new IntentFilter("MEDIA_PLAYER_PREPARED"));
    }

    @Override
    protected void onStop() {
        super.onStop();
        controller.hide();
    }

    public void getSongsList(){
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        @SuppressLint("Recycle")
        Cursor musicCursor = musicResolver.query(musicUri,null,null,null,null);
        if (musicCursor!=null && musicCursor.moveToFirst()){
            int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);

            do{
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                songsList.add(new Song(thisId, thisTitle, thisArtist));
            }while (musicCursor.moveToNext());
        }
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else {
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    private AdapterView.OnItemClickListener onClickListener = new AdapterView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            songPicked(view);
//            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//            intent.putExtra(ARTIST, songsList.get(position).getArtist());
//            intent.putExtra(SONG, songsList.get(position).getTitle());
//            startActivity(intent);
        }
    };



    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyService.MusicBinder binder = (MyService.MusicBinder) service;
            musicService = binder.getService();
            musicService.setList(songsList);
            musicBounds = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBounds = false;
        }
    };

    public void songPicked(View view){
        musicService.setSong(Integer.parseInt(view.getTag().toString()));
        musicService.playSong();
        if (playbackPaused){
            setController();
            playbackPaused = false;
        }
        controller.show(0);
    }

    @Override
    public void start() {
        musicService.go();
    }

    @Override
    public void pause() {
        musicService.pausePlayer();
    }

    @Override
    public int getDuration() {
        if (musicService!=null && musicService.isPlaying() && musicBounds){
            return musicService.getDur();
        }
        else {
            return 0;
        }
    }

    @Override
    public int getCurrentPosition() {
        return musicService.getPos();

    }

    @Override
    public void seekTo(int pos) {
        musicService.seek(pos);
    }

    @Override
    public boolean isPlaying() {
        if (musicService!=null && musicBounds){
            return musicService.isPlaying();
        }
        else {
            return false;
        }
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    private void setController(){
        if (controller == null) {
            controller = new MusicController(this);
        }
        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });
        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.songList));
        controller.setEnabled(true);
    }

    private void playNext(){
        musicService.playNext();
        if (playbackPaused){
            setController();
            playbackPaused = false;
        }
        controller.show(0);
    }

    private void playPrev(){
        musicService.playPrev();
        if (playbackPaused){
            setController();
            playbackPaused = false;
        }
        controller.show(0);
    }

    private BroadcastReceiver onPrepareReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            controller.show(0);
        }
    };

    private BroadcastReceiver NotificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Objects.equals(intent.getAction(), Constants.ACTION_PREV)){
                playPrev();
            }
            else if (Objects.equals(intent.getAction(), Constants.ACTION_NEXT)){
                playNext();
            }
            else if (Objects.equals(intent.getAction(), Constants.ACTION_PLAY)){
                start();
            }
        }
    };

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo){
        if (view.getId() == R.id.songList){
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            menu.setHeaderTitle(songsList.get(info.position).getTitle());
            String[] menuItems = getResources().getStringArray(R.array.context_menu_listview);
            for (int i = 0; i<menuItems.length;i++){
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }

        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem menuItem){
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuItem.getMenuInfo();
        if (menuItem.getItemId() == Constants.UPLOAD_POSITION) {
            onUploadClick(info.position);
        }
        else if (menuItem.getItemId() == Constants.SHOW_POSITION){
            onShowClick(info.position);
        }

        return true;
    }

    private void onUploadClick(int position){
        Intent intent = new Intent(this, UploadLyricsActivity.class);
        intent.putExtra(Constants.TITLE, songsList.get(position).getTitle());
        intent.putExtra(Constants.ARTIST, songsList.get(position).getArtist());
        startActivity(intent);
    }

    private void onShowClick(int position){
        Intent intent = new Intent(this, SongLyricsActivity.class);
        intent.putExtra(Constants.TITLE, songsList.get(position).getTitle());
        startActivity(intent);
    }


}