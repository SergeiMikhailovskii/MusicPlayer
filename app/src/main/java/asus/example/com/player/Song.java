package asus.example.com.player;

public class Song {
    private long id;
    private String title;
    private String artist;


    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    String getArtist() {
        return artist;
    }

    Song(long id, String title, String artist){
        this.id = id;
        this.title = title;
        this.artist = artist;
    }
}
