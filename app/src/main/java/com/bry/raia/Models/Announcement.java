package com.bry.raia.Models;

import android.graphics.Bitmap;

public class Announcement {
    private String announcementId;
    private String announcementTitle;
    private String encodedAnnouncementImage;
    private Bitmap announcementBitmap;
    private long announcementCreationTime;
    private String uploaderId;
    private String uploaderUsername;
    private String uploaderEmail;
    private County county;


    public Announcement(){}

    public Announcement(String title, String image, long creationTime){
        this.announcementTitle = title;
        this.encodedAnnouncementImage = image;
        this.announcementCreationTime = creationTime;
    }


    public String getAnnouncementId() {
        return announcementId;
    }

    public void setAnnouncementId(String announcementId) {
        this.announcementId = announcementId;
    }

    public String getAnnouncementTitle() {
        return announcementTitle;
    }

    public void setAnnouncementTitle(String announcementTitle) {
        this.announcementTitle = announcementTitle;
    }

    public String getEncodedAnnouncementImage() {
        return encodedAnnouncementImage;
    }

    public void setEncodedAnnouncementImage(String encodedAnnouncementImage) {
        this.encodedAnnouncementImage = encodedAnnouncementImage;
    }

    public Bitmap getAnnouncementBitmap() {
        return announcementBitmap;
    }

    public void setAnnouncementBitmap(Bitmap announcementBitmap) {
        this.announcementBitmap = announcementBitmap;
    }

    public long getAnnouncementCreationTime() {
        return announcementCreationTime;
    }

    public void setAnnouncementCreationTime(long announcementCreationTime) {
        this.announcementCreationTime = announcementCreationTime;
    }

    public String getUploaderId() {
        return uploaderId;
    }

    public void setUploaderId(String uploaderId) {
        this.uploaderId = uploaderId;
    }

    public String getUploaderUsername() {
        return uploaderUsername;
    }

    public void setUploaderUsername(String uploaderUsername) {
        this.uploaderUsername = uploaderUsername;
    }

    public String getUploaderEmail() {
        return uploaderEmail;
    }

    public void setUploaderEmail(String uploaderName) {
        this.uploaderEmail = uploaderName;
    }

    public County getCounty() {
        if (county==null){
            return new County();
        }else return county;
    }

    public void setCounty(County county) {
        this.county = county;
    }
}
