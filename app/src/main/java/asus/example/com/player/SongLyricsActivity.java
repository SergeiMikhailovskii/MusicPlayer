package asus.example.com.player;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class SongLyricsActivity extends AppCompatActivity {

    private TextView lyricsTextView;
    private ImageView songIconImageView;
    private ContentValues contentValues;
    private SQLiteDatabase database;
    private String songName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_song_lyrics);
        super.onCreate(savedInstanceState);
        songName = getIntent().getStringExtra(Constants.TITLE);
        lyricsTextView = findViewById(R.id.lyrics_textview);
        songIconImageView = findViewById(R.id.song_icon_imageview);
        LyricsDatabase lyricsDatabase = new LyricsDatabase(getApplicationContext());
        contentValues = new ContentValues();
        database = lyricsDatabase.getWritableDatabase();
        Cursor cursor = database.query("LYRICS", new String[]{"SONG_LYRICS"},
                "SONG_LYRICS = ?", new String[]{songName}, null, null, null);
        cursor.moveToFirst();
        int idLyricsIndex = cursor.getColumnIndex("SONG_LYRICS");
        lyricsTextView.setText(cursor.getString(idLyricsIndex));
        database.close();
    }
}
