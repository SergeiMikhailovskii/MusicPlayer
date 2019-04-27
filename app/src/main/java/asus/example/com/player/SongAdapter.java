package asus.example.com.player;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class SongAdapter extends BaseAdapter {
    private ArrayList<Song> songs;
    private LayoutInflater layoutInflater;

    SongAdapter(Context context, ArrayList<Song> songs) {
        this.songs = songs;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        @SuppressLint("ViewHolder")
        LinearLayout songLay = (LinearLayout) layoutInflater.inflate(R.layout.song, parent, false);
        TextView songTitle = songLay.findViewById(R.id.songTitle);
        TextView songArtist = songLay.findViewById(R.id.songArtist);
        Song curSong = songs.get(position);
        songTitle.setText(curSong.getTitle());
        songArtist.setText(curSong.getArtist());
        songLay.setTag(position);
        return songLay;
    }
}