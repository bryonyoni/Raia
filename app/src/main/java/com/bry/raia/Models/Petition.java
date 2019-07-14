package com.bry.raia.Models;

import android.graphics.Bitmap;

public class Petition {
    private String petitionId;
    private String petitionTitle;
    private String encodedPetitionImage;
    private Bitmap petitionBitmap;
    private long petitionCreationTime;
    private String uploaderId;
    private String uploaderUsername;
    private String uploaderEmail;



    public Petition(){}

    public Petition(String title, String image, long creationTime){
        this.petitionTitle = title;
        this.encodedPetitionImage = image;
        this.petitionCreationTime = creationTime;
    }


    public String getPetitionId() {
        return petitionId;
    }

    public void setPetitionId(String petitionId) {
        this.petitionId = petitionId;
    }

    public String getPetitionTitle() {
        return petitionTitle;
    }

    public void setPetitionTitle(String petitionTitle) {
        this.petitionTitle = petitionTitle;
    }


    public Bitmap getPetitionBitmap() {
        return petitionBitmap;
    }

    public void setPetitionBitmap(Bitmap petitionBitmap) {
        this.petitionBitmap = petitionBitmap;
    }

    public long getPetitionCreationTime() {
        return petitionCreationTime;
    }

    public void setPetitionCreationTime(long petitionCreationTime) {
        this.petitionCreationTime = petitionCreationTime;
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

    public void setUploaderEmail(String uploaderEmail) {
        this.uploaderEmail = uploaderEmail;
    }

    public String getEncodedPetitionImage() {
        return encodedPetitionImage;
    }

    public void setEncodedPetitionImage(String encodedPetitionImage) {
        this.encodedPetitionImage = encodedPetitionImage;
    }
}
