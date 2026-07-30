package com.auradl.download;

import com.auradl.api.MediaItem;

public class DownloadTask {
    private final String id;
    private final String url;
    private MediaItem mediaItem;
    private DownloadStatus status;
    private String errorMessage;
    private double progress;
    private String speed;
    private String eta;
    private String finalPath;

    public DownloadTask(String id, String url) {
        this.id = id;
        this.url = url;
        this.status = DownloadStatus.PENDING_FETCHING;
        this.progress = 0.0;
        this.speed = "0.0 MB/s";
        this.eta = "--:--";
    }

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public MediaItem getMediaItem() {
        return mediaItem;
    }

    public void setMediaItem(MediaItem mediaItem) {
        this.mediaItem = mediaItem;
    }

    public DownloadStatus getStatus() {
        return status;
    }

    public void setStatus(DownloadStatus status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getEta() {
        return eta;
    }

    public void setEta(String eta) {
        this.eta = eta;
    }

    public String getFinalPath() {
        return finalPath;
    }

    public void setFinalPath(String finalPath) {
        this.finalPath = finalPath;
    }
}
