package com.bry.raia.Models;

import com.bry.raia.Constants;

public class Post {
    private Announcement announcement;
    private Petition petition;
    private Poll poll;
    private String mPostType;

    public Post(){}


    public Announcement getAnnouncement() {
        return announcement;
    }

    public void setAnnouncement(Announcement announcement) {
        this.announcement = announcement;
    }



    public Petition getPetition() {
        return petition;
    }

    public void setPetition(Petition petition) {
        this.petition = petition;
    }



    public Poll getPoll() {
        return poll;
    }

    public void setPoll(Poll poll) {
        this.poll = poll;
    }

    public String getPostType2(){
        String type;
        if(announcement!=null){
            type= Constants.ANNOUNCEMENTS;
        }else if(petition!=null){
            type= Constants.PETITIONS;
        }else if(poll!=null){
            type= Constants.POLLS;
        }else type = null;
        return type;
    }


    public String getPostType() {
        return mPostType;
    }

    public void setPostType(String mPostType) {
        this.mPostType = mPostType;
    }
}
