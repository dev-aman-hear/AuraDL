package com.auradl.config;

import java.util.ArrayList;
import java.util.List;

public class Config {
    private int maxConcurrentDownloads = 3;
    private String browserName = "edge";
    private String browserProfile = "Default";
    private String cookies = "./cookies.txt";
    private String wrapperBaseUrl = "http://localhost";
    private String mediaUserToken = "";
    private ApiMethod apiMethod = ApiMethod.COOKIES;
    private String language = "en-US";
    private String outputDir = new java.io.File(System.getProperty("user.dir"), "Apple Music").getAbsolutePath().replace('\\', '/');
    private String tempDir = ".";
    private String downloadMode = "ytdlp";
    private String nm3u8dlrePath = "N_m3u8DL-RE.exe";
    private String ffmpegPath = "ffmpeg.exe";
    private String albumFolderTemplate = "{album_artist}/{album}";
    private String songFileTemplate = "{track:02d} {title}";
    private String songCodecPriority = "alac,atmos,aac";
    private String coverFormat = "jpg";
    private int coverSize = 1200;
    private String syncedLyricsFormat = "lrc";
    private boolean saveCover = true;
    private boolean savePlaylist = false;
    private boolean overwrite = false;
    private String musicVideoResolution = "1080p";
    private String musicVideoCodecPriority = "h264,h265";
    private String musicVideoRemuxFormat = "mp4";
    private List<String> songCodecs = new ArrayList<>(List.of("alac", "atmos", "aac"));

    public Config() {}

    public int getMaxConcurrentDownloads() {
        return maxConcurrentDownloads;
    }

    public void setMaxConcurrentDownloads(int maxConcurrentDownloads) {
        this.maxConcurrentDownloads = maxConcurrentDownloads;
    }

    public String getBrowserName() {
        return browserName;
    }

    public void setBrowserName(String browserName) {
        this.browserName = browserName;
    }

    public String getBrowserProfile() {
        return browserProfile;
    }

    public void setBrowserProfile(String browserProfile) {
        this.browserProfile = browserProfile;
    }

    public String getCookies() {
        return cookies;
    }

    public void setCookies(String cookies) {
        this.cookies = cookies;
    }

    public String getWrapperBaseUrl() {
        return wrapperBaseUrl;
    }

    public void setWrapperBaseUrl(String wrapperBaseUrl) {
        this.wrapperBaseUrl = wrapperBaseUrl;
    }

    public String getMediaUserToken() {
        return mediaUserToken;
    }

    public void setMediaUserToken(String mediaUserToken) {
        this.mediaUserToken = mediaUserToken;
    }

    public ApiMethod getApiMethod() {
        return apiMethod;
    }

    public void setApiMethod(ApiMethod apiMethod) {
        this.apiMethod = apiMethod;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    public String getTempDir() {
        return tempDir;
    }

    public void setTempDir(String tempDir) {
        this.tempDir = tempDir;
    }

    public String getDownloadMode() {
        return downloadMode;
    }

    public void setDownloadMode(String downloadMode) {
        this.downloadMode = downloadMode;
    }

    public String getNm3u8dlrePath() {
        return nm3u8dlrePath;
    }

    public void setNm3u8dlrePath(String nm3u8dlrePath) {
        this.nm3u8dlrePath = nm3u8dlrePath;
    }

    public String getFfmpegPath() {
        return ffmpegPath;
    }

    public void setFfmpegPath(String ffmpegPath) {
        this.ffmpegPath = ffmpegPath;
    }

    public String getAlbumFolderTemplate() {
        return albumFolderTemplate;
    }

    public void setAlbumFolderTemplate(String albumFolderTemplate) {
        this.albumFolderTemplate = albumFolderTemplate;
    }

    public String getSongFileTemplate() {
        return songFileTemplate;
    }

    public void setSongFileTemplate(String songFileTemplate) {
        this.songFileTemplate = songFileTemplate;
    }

    public String getSongCodecPriority() {
        return songCodecPriority;
    }

    public void setSongCodecPriority(String songCodecPriority) {
        this.songCodecPriority = songCodecPriority;
    }

    public String getCoverFormat() {
        return coverFormat;
    }

    public void setCoverFormat(String coverFormat) {
        this.coverFormat = coverFormat;
    }

    public int getCoverSize() {
        return coverSize;
    }

    public void setCoverSize(int coverSize) {
        this.coverSize = coverSize;
    }

    public String getSyncedLyricsFormat() {
        return syncedLyricsFormat;
    }

    public void setSyncedLyricsFormat(String syncedLyricsFormat) {
        this.syncedLyricsFormat = syncedLyricsFormat;
    }

    public boolean isSaveCover() {
        return saveCover;
    }

    public void setSaveCover(boolean saveCover) {
        this.saveCover = saveCover;
    }

    public boolean isSavePlaylist() {
        return savePlaylist;
    }

    public void setSavePlaylist(boolean savePlaylist) {
        this.savePlaylist = savePlaylist;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    public List<String> getSongCodecs() {
        return songCodecs;
    }

    public void setSongCodecs(List<String> songCodecs) {
        this.songCodecs = songCodecs;
    }

    public String getMusicVideoResolution() {
        return musicVideoResolution;
    }

    public void setMusicVideoResolution(String musicVideoResolution) {
        this.musicVideoResolution = musicVideoResolution;
    }

    public String getMusicVideoCodecPriority() {
        return musicVideoCodecPriority;
    }

    public void setMusicVideoCodecPriority(String musicVideoCodecPriority) {
        this.musicVideoCodecPriority = musicVideoCodecPriority;
    }

    public String getMusicVideoRemuxFormat() {
        return musicVideoRemuxFormat;
    }

    public void setMusicVideoRemuxFormat(String musicVideoRemuxFormat) {
        this.musicVideoRemuxFormat = musicVideoRemuxFormat;
    }
}
