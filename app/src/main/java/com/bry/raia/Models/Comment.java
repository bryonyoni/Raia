package com.bry.raia.Models;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Comment {
    private String mCommentId;
    private String mCommentText;
    private Long mCommentTime;
    private String mCommenterUId;
    private String mCommenterName;
    private List<Comment> mReplies = new ArrayList<>();

    public Comment(){}

    public Comment (String text,String Uid, String username){
        this.mCommentText = text;
        this.mCommenterUId = Uid;
        this.mCommenterName = username;
        this.mCommentTime = Calendar.getInstance().getTimeInMillis();
    }

    public String getCommentId() {
        return mCommentId;
    }

    public void setCommentId(String mCommentId) {
        this.mCommentId = mCommentId;
    }

    public String getCommentText() {
        return mCommentText;
    }

    public void setCommentText(String mCommentText) {
        this.mCommentText = mCommentText;
    }

    public Long getCommentTime() {
        return mCommentTime;
    }

    public void setCommentTime(Long mCommentTime) {
        this.mCommentTime = mCommentTime;
    }

    public String getCommenterUId() {
        return mCommenterUId;
    }

    public void setCommenterUId(String mCommenterUId) {
        this.mCommenterUId = mCommenterUId;
    }

    public List<Comment> getReplies() {
        return mReplies;
    }

    public void setReplies(List<Comment> mReplies) {
        this.mReplies = mReplies;
    }

    public void addReply(Comment reply){
        mReplies.add(reply);
    }

    public String getCommenterName() {
        return mCommenterName;
    }

    public void setCommenterName(String mCommenterName) {
        this.mCommenterName = mCommenterName;
    }
}
