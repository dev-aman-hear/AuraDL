package com.auradl.api;

import java.util.List;

public class AlbumDetails {
    private final MediaItem album;
    private final List<MediaItem> tracks;

    public AlbumDetails(MediaItem album, List<MediaItem> tracks) {
        this.album = album;
        this.tracks = tracks;
    }

    public MediaItem getAlbum() {
        return album;
    }

    public List<MediaItem> getTracks() {
        return tracks;
    }
}
