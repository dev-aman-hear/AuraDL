package com.auradl.api;

public class MediaItem {
    private String id;
    private String title;
    private String artist;
    private String artworkUrl;
    private String type; // "song", "album", "playlist", "video", "artist"
    private long durationMs;
    private String url;

    // Extended metadata
    private String genre      = "";
    private String collectionName = "";
    private int    trackNumber = 0;
    private boolean explicit  = false;
    private int    trackCount = 0;   // for albums/playlists
    private String country    = "US";
    // Quality info: codec available on Apple Music servers for this item
    // We compute a best-guess from the kind/wrapperType returned by iTunes
    private String quality    = "";

    public MediaItem(String id, String title, String artist, String artworkUrl,
                     String type, long durationMs, String url) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.artworkUrl = artworkUrl;
        this.type = type;
        this.durationMs = durationMs;
        this.url = url;
    }

    // ── Getters ─────────────────────────────────────────────────────
    public String  getId()             { return id; }
    public String  getTitle()          { return title; }
    public String  getArtist()         { return artist; }
    public String  getArtworkUrl()     { return artworkUrl; }
    public String  getType()           { return type; }
    public long    getDurationMs()     { return durationMs; }
    public String  getUrl()            { return url; }
    public String  getGenre()          { return genre; }
    public String  getCollectionName() { return collectionName; }
    public int     getTrackNumber()    { return trackNumber; }
    public boolean isExplicit()        { return explicit; }
    public int     getTrackCount()     { return trackCount; }
    public String  getCountry()        { return country; }
    public String  getQuality()        { return quality; }

    // ── Setters ─────────────────────────────────────────────────────
    public void setGenre(String genre)                { this.genre = genre != null ? genre : ""; }
    public void setCollectionName(String n)           { this.collectionName = n != null ? n : ""; }
    public void setTrackNumber(int n)                 { this.trackNumber = n; }
    public void setExplicit(boolean e)                { this.explicit = e; }
    public void setTrackCount(int n)                  { this.trackCount = n; }
    public void setCountry(String c)                  { this.country = c != null ? c : "US"; }
    public void setQuality(String q)                  { this.quality = q != null ? q : ""; }
    public void setArtworkUrl(String url)             { this.artworkUrl = url; }

    /** Human-readable duration: "3:45" or "1:02:30" */
    public String getFormattedDuration() {
        if (durationMs <= 0) return "";
        long s = durationMs / 1000;
        long m = s / 60; s %= 60;
        long h = m / 60; m %= 60;
        if (h > 0) return String.format("%d:%02d:%02d", h, m, s);
        return String.format("%d:%02d", m, s);
    }
}
