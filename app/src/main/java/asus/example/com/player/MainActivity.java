package asus.example.com.player;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private Button prev;
    private Button next;
    private Button play;
    private Button list;
    private Snackbar snackbar;
    private SeekBar seekBar;
    private TextView curTime;
    private TextView songName;
    private TextView artistName;
    private Handler handler = new Handler();

    private final int SONG_LENGTH = 120;
    private int secs = 0;

    private final String ARTIST = "ARTIST";
    private final String SONG = "SONG";


    View.OnClickListener snackbarOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            snackbar.dismiss();
        }
    };

    private void onPrevClick(final View view) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        snackbar = Snackbar.make(view, "Prev", Snackbar.LENGTH_INDEFINITE).setAction("Close", snackbarOnClickListener);
                        snackbar.show();
                    }
                });
            }

        });
        thread.run();
    }

    private void onPlayClick() {
//        Runnable runnable = new Runnable() {
//            @Override
//            public void run() {
//                double percent = (double) secs/SONG_LENGTH;
//                int progress =(int) (percent*100);
//                seekBar.setProgress(progress);
//                secs++;
//            }
//        };
//        for (int i = seekBar.getProgress()*SONG_LENGTH/100; i<=SONG_LENGTH;i++) {
//            handler.postDelayed(runnable,1000*i);
//        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

            }
        };
    }

    private void onNextClick(final View view) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        snackbar = Snackbar.make(view, "Next", Snackbar.LENGTH_INDEFINITE).setAction("Close", snackbarOnClickListener);
                        snackbar.show();
                    }
                });
            }
        });
        thread.run();
    }

    private void onListClick() {
        Intent intent = new Intent(this, ListOfSongsActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prev = findViewById(R.id.prev);
        play = findViewById(R.id.play);
        next = findViewById(R.id.next);
        list = findViewById(R.id.list);
        seekBar = findViewById(R.id.seekBar);
        curTime = findViewById(R.id.curTime);
        songName = findViewById(R.id.songName);
        artistName = findViewById(R.id.artistName);
        prev.setOnClickListener(this);
        play.setOnClickListener(this);
        next.setOnClickListener(this);
        list.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        String artist = getIntent().getStringExtra(ARTIST);
        String song = getIntent().getStringExtra(SONG);
        artistName.setText(artist);
        songName.setText(song);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.prev:
                onPrevClick(v);
                break;
            case R.id.play:
                onPlayClick();
                break;
            case R.id.next:
                onNextClick(v);
                break;
            case R.id.list:
                onListClick();
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int seconds = seekBar.getProgress() * SONG_LENGTH / 100;
        @SuppressLint("DefaultLocale")
        String result = DateUtils.formatElapsedTime(seconds);
        curTime.setText(result);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}