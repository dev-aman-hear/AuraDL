package com.auradl.download;

public enum DownloadStatus {
    PENDING_FETCHING("pending-fetching"),
    FETCHING("fetching"),
    PENDING_DOWNLOADING("pending-downloading"),
    DOWNLOADING("downloading"),
    DECRYPTING("decrypting DRM"),
    EXTRACTING("extracting audio"),
    SAVING_TAGS("embedding tags"),
    COMPLETED("completed"),
    FAILED("failed");

    private final String value;

    DownloadStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
