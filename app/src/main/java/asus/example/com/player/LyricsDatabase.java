package asus.example.com.player;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LyricsDatabase extends SQLiteOpenHelper {
    LyricsDatabase(Context context) {
        super(context, "Lyrics Database", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE LYRICS(" +
                "ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "SONG_TITLE VARCHAR," +
                "SONG_ARTIST VARCHAR," +
                "SONG_LYRICS TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
