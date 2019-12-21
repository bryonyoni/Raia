package com.bry.raia.Models;

import android.graphics.Bitmap;

public class User {
    private String uId;
    private String mName;
    private Bitmap mImageBitmap;
    private String mImageString;
    private String mEmail;
    private long mSignUpTime;

    public User(){}

    public User(String name, String uid){
        this.mName = name;
        this.uId = uid;
    }


    public String getUId() {
        return uId;
    }

    public void setUId(String uId) {
        this.uId = uId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public Bitmap getImageBitmap() {
        return mImageBitmap;
    }

    public void setImageBitmap(Bitmap mImageBitmap) {
        this.mImageBitmap = mImageBitmap;
    }

    public String getImageString() {
        return mImageString;
    }

    public void setImageString(String mImageString) {
        this.mImageString = mImageString;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String mEmail) {
        this.mEmail = mEmail;
    }

    public long getSignUpTime() {
        return mSignUpTime;
    }

    public void setSignUpTime(long mSignUpTime) {
        this.mSignUpTime = mSignUpTime;
    }
}
