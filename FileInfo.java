package com.onetime.filelink;

public class FileInfo {

    private String path;
    private long expiryTime;
    private String contentType;

    public FileInfo(String path, long expiryTime, String contentType) {
        this.path = path;
        this.expiryTime = expiryTime;
        this.contentType = contentType;
    }

    public String getPath() {
        return path;
    }

    public long getExpiryTime() {
        return expiryTime;
    }

    public String getContentType() {
        return contentType;
    }
}
