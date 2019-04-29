package asus.example.com.player;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;


public class UploadLyricsActivity extends AppCompatActivity {

    private String title;
    private String artist;
    private String lyrics;
    private ContentValues contentValues;
    private EditText artistEdit;
    private EditText titleEdit;
    private EditText lyricsEdit;
    private SQLiteDatabase database;
    private boolean areLyricsFound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_lyrics);
        titleEdit = findViewById(R.id.title_edittext);
        artistEdit = findViewById(R.id.artist_edittext);
        lyricsEdit = findViewById(R.id.lyrics_edittext);
        Button downloadButton = findViewById(R.id.download_button);
        Button saveButton = findViewById(R.id.save_button);
        title = getIntent().getStringExtra(Constants.TITLE);
        artist = getIntent().getStringExtra(Constants.ARTIST);
        downloadButton.setOnClickListener(downloadClick);
        saveButton.setOnClickListener(saveClick);
        titleEdit.setText(title);
        artistEdit.setText(artist);
        LyricsDatabase lyricsDatabase = new LyricsDatabase(getApplicationContext());
        contentValues = new ContentValues();
        database = lyricsDatabase.getWritableDatabase();
    }

    private View.OnClickListener downloadClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                lyrics = new NetworkLyricsDownload().execute(artist, title).get();
                lyricsEdit.setText(lyrics);
                if (lyrics.equals("")) {
                    Toast.makeText(getApplicationContext(), getString(R.string.lyrics_not_found), Toast.LENGTH_SHORT)
                            .show();
                    areLyricsFound = false;
                } else {
                    areLyricsFound = true;
                }
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    private View.OnClickListener saveClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (areLyricsFound) {
                String artist = artistEdit.getText().toString();
                String title = titleEdit.getText().toString();
                contentValues.put(Constants.SONG_TITLE, title);
                contentValues.put(Constants.SONG_ARTIST, artist);
                contentValues.put(Constants.SONG_LYRICS, lyrics);
                database.insert(Constants.LYRICS_TABLE, null, contentValues);
                database.close();
                Toast.makeText(getApplicationContext(), getString(R.string.save_clicked), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.not_save_lyrics), Toast.LENGTH_SHORT)
                        .show();
            }
        }
    };

    private static class NetworkLyricsDownload extends AsyncTask<String, Void, String> {

        private InputStream getInputStream(URL url) {
            try {
                return url.openConnection().getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected String doInBackground(String... strings) {
            String result = null;
            try {

                String artist = strings[Constants.ARTIST_POSITION];
                String artistArr[] = artist.split(" ");
                String title = strings[Constants.TITLE_POSITION];
                String titleArr[] = title.split(" ");

                StringBuilder urlStr = new StringBuilder("http://api.chartlyrics.com/apiv1.asmx/SearchLyricDirect?artist=");
                if (artistArr.length > 1) {
                    for (int i = 0; i < artistArr.length - 1; i++) {
                        urlStr.append(artistArr[i]).append(Constants.URL_SPLIT);
                    }
                }
                urlStr.append(artistArr[artistArr.length - 1]);
                urlStr.append("&song=");
                if (titleArr.length > 1) {
                    for (int i = 0; i < titleArr.length - 1; i++) {
                        urlStr.append(titleArr[i]).append(Constants.URL_SPLIT);
                    }
                }
                urlStr.append(titleArr[titleArr.length - 1]);

                URL url = new URL(urlStr.toString());
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(false);
                XmlPullParser xmlPullParser = factory.newPullParser();
                xmlPullParser.setInput(getInputStream(url), "UTF-8");
                int eventType = xmlPullParser.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xmlPullParser.getName().equalsIgnoreCase(Constants.LYRIC_TAG)) {
                            result = xmlPullParser.nextText();
                        }
                    }
                    eventType = xmlPullParser.next();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return result;
        }
    }
}
