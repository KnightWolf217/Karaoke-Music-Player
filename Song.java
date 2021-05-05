
public class Song {

    private final String title;
    private final String artist;
    private final String duration;
    private final String file;

    public Song(String title, String artist, String duration, String file) {
        this.title = title;
        this.artist = artist;
        this.duration = duration;
        this.file = file;

    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getDuration() {
        return duration;
    }

    public String getFile() {
        return file;
    }

}
