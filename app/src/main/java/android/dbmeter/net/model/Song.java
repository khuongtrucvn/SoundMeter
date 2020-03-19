package android.dbmeter.net.model;

public class Song {
    private String SongId;
    private String SongTitle;
    private String SongDescription;
    private String SongThumbnail;
    private String SongUrl;

    public Song(){
        SongId = "";
        SongTitle = "";
        SongDescription = "";
        SongThumbnail = "";
        SongUrl = "";
    }

    public Song(String songId, String songTitle, String songDescription, String songThumbnail, String songUrl){
        SongId = songId;
        SongTitle = songTitle;
        SongDescription = songDescription;
        SongThumbnail = songThumbnail;
        SongUrl = songUrl;
    }

    public String getSongId() {
        return SongId;
    }

    public void setSongId(String songId) {
        SongId = songId;
    }

    public String getSongTitle() {
        return SongTitle;
    }

    public void setSongTitle(String songTitle) {
        SongTitle = songTitle;
    }

    public String getSongDescription() {
        return SongDescription;
    }

    public void setSongDescription(String songDescription) {
        SongDescription = songDescription;
    }

    public String getSongThumbnail() {
        return SongThumbnail;
    }

    public void setSongThumbnail(String songThumbnail) {
        SongThumbnail = songThumbnail;
    }

    public String getSongUrl() {
        return SongUrl;
    }

    public void setSongUrl(String songUrl) {
        SongUrl = songUrl;
    }
}
