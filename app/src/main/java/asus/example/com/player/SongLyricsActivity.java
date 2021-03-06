package asus.example.com.player;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

public class SongLyricsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_song_lyrics);
        super.onCreate(savedInstanceState);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        String title = getIntent().getStringExtra(Constants.TITLE);
        TextView lyricsTextView = findViewById(R.id.lyrics_textview);
        getSupportActionBar().setTitle(title);
        LyricsDatabase lyricsDatabase = new LyricsDatabase(getApplicationContext());
        SQLiteDatabase database = lyricsDatabase.getWritableDatabase();
        try {
            @SuppressLint("Recycle")
            Cursor cursor = database.query("LYRICS", new String[]{"SONG_LYRICS"},
                    "SONG_TITLE = ?", new String[]{title}, null,
                    null, null);
            cursor.moveToFirst();
            int idLyricsIndex = cursor.getColumnIndex("SONG_LYRICS");
            lyricsTextView.setText(cursor.getString(idLyricsIndex));
            database.close();
        }catch (RuntimeException e){
            Toast.makeText(getApplicationContext(), getString(R.string.show_lyrics_runtime_exception),
                    Toast.LENGTH_SHORT).show();
        }
    }
}
