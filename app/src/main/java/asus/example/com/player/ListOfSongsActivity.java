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
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.MediaController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;


public class ListOfSongsActivity extends AppCompatActivity
        implements MediaController.MediaPlayerControl {

    private ArrayList<Song> songsList;
    private MyService musicService;
    private Intent playIntent;
    private boolean musicBounds = false;
    private MusicController controller;
    private boolean paused = false;
    private boolean playbackPaused = false;
    private boolean isShuffled = false;
    private boolean isLooped = false;
    private MenuItem shuffleItem;
    private MenuItem loopItem;
    private ListView songsListView;
    private ArrayList<Song> artistArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_songs);
        songsListView = findViewById(R.id.songList);
        songsList = new ArrayList<>();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (isStoragePermissionGranted()) {
            getSongsList();
        }
        sortArrayList();
        createAdapter(songsList);
        songsListView.setOnItemClickListener(onClickListener);
        registerForContextMenu(songsListView);
        setController();
        registerReceiver(NotificationReceiver, new IntentFilter(Constants.ACTION_PREV));
        registerReceiver(NotificationReceiver, new IntentFilter(Constants.ACTION_NEXT));
        registerReceiver(NotificationReceiver, new IntentFilter(Constants.ACTION_PLAY));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (playIntent == null) {
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
    protected void onResume() {
        super.onResume();
        if (paused) {
            setController();
            paused = false;
        }
        LocalBroadcastManager.getInstance(this).
                registerReceiver(onPrepareReceiver, new IntentFilter(Constants.ACTION_MEDIA_PLAYER_PREPARED));
    }

    @Override
    protected void onStop() {
        super.onStop();
        controller.hide();
    }

    public void getSongsList() {
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        @SuppressLint("Recycle")
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
        if (musicCursor != null && musicCursor.moveToFirst()) {
            int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);

            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                songsList.add(new Song(thisId, thisTitle, thisArtist));
            } while (musicCursor.moveToNext());
        }
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else {
            return true;
        }
    }

    private AdapterView.OnItemClickListener onClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            songPicked(view);
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

    public void songPicked(View view) {
        musicService.setSong(Integer.parseInt(view.getTag().toString()));
        musicService.playSong();
        if (playbackPaused) {
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
        if (musicService != null && musicService.isPlaying() && musicBounds) {
            return musicService.getDur();
        } else {
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
        if (musicService != null && musicBounds) {
            return musicService.isPlaying();
        } else {
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

    private void setController() {
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

    private void playNext() {
        musicService.playNext();
        if (playbackPaused) {
            setController();
            playbackPaused = false;
        }
        controller.show(0);
    }

    private void playPrev() {
        musicService.playPrev();
        if (playbackPaused) {
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
            if (Objects.equals(intent.getAction(), Constants.ACTION_PREV)) {
                playPrev();
            } else if (Objects.equals(intent.getAction(), Constants.ACTION_NEXT)) {
                playNext();
            } else if (Objects.equals(intent.getAction(), Constants.ACTION_PLAY)) {
                start();
            }
        }
    };

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        if (view.getId() == R.id.songList) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            menu.setHeaderTitle(songsList.get(info.position).getTitle());
            String[] menuItems = getResources().getStringArray(R.array.context_menu_listview);
            for (int i = 0; i < menuItems.length; i++) {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }

        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem menuItem) {
        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) menuItem.getMenuInfo();
        switch (menuItem.getItemId()){
            case Constants.UPLOAD_POSITION:
                onUploadClick(info.position);
                break;
            case Constants.SHOW_POSITION:
                onShowClick(info.position);
                break;
            case Constants.ARTIST_PLAY_POSITION:
                onPlayArtistClick(info.position);
                break;

        }

        return true;
    }

    private void onPlayArtistClick(int position) {
        artistArray = new ArrayList<>();
        for (int i = 0; i<songsList.size();i++){
            if (songsList.get(i).getArtist().equalsIgnoreCase(songsList.get(position).getArtist())){
                artistArray.add(songsList.get(i));
            }
        }
        createAdapter(artistArray);
    }

    private void onUploadClick(int position) {
        Intent intent = new Intent(this, UploadLyricsActivity.class);
        intent.putExtra(Constants.TITLE, songsList.get(position).getTitle());
        intent.putExtra(Constants.ARTIST, songsList.get(position).getArtist());
        startActivity(intent);
    }

    private void onShowClick(int position) {
        Intent intent = new Intent(this, SongLyricsActivity.class);
        intent.putExtra(Constants.TITLE, songsList.get(position).getTitle());
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.shuffle:
                onShuffleClick();
                break;
            case R.id.loop:
                onLoopClick();
                break;
        }

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.list_of_song_menu, menu);
        shuffleItem = menu.findItem(R.id.shuffle);
        loopItem = menu.findItem(R.id.loop);
        return true;
    }

    private void onShuffleClick(){
        if (!isShuffled) {
            Collections.shuffle(songsList);
            shuffleItem.setIcon(R.drawable.shuffle_white);
            isShuffled = true;
            createAdapter(songsList);
        }
        else {
            shuffleItem.setIcon(R.drawable.shuffle);
            isShuffled = false;
            sortArrayList();
            createAdapter(songsList);
        }
    }

    private void sortArrayList(){
        Collections.sort(songsList, new Comparator<Song>() {
            @Override
            public int compare(Song o1, Song o2) {
                return o1.getTitle().compareTo(o2.getTitle());
            }
        });
    }

    private void onLoopClick(){
        if (!isLooped){
            loopItem.setIcon(R.drawable.loop_white);
            isLooped = true;
            musicService.setLooping(true);
        }
        else {
            loopItem.setIcon(R.drawable.loop_black);
            isLooped = false;
            musicService.setLooping(false);
        }

    }

    private void createAdapter(ArrayList<Song> list){
        SongAdapter adapter = new SongAdapter(this, list);
        songsListView.setAdapter(adapter);
    }


}