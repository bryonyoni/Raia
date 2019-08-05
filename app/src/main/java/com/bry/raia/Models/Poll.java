package com.bry.raia.Models;

import java.util.ArrayList;
import java.util.List;

public class Poll {
    private String pollId;
    private String pollTitle;
    private List<PollOption> pollOptions = new ArrayList<>();
    private long pollCreationTime;
    private String uploaderId;
    private String uploaderUsername;
    private String uploaderEmail;
    private County county;

    public Poll(){}

    public Poll(String title, List<PollOption>options){
        this.pollTitle = title;
        this.pollOptions = options;
    }


    public String getPollId() {
        return pollId;
    }

    public void setPollId(String pollId) {
        this.pollId = pollId;
    }

    public String getPollTitle() {
        return pollTitle;
    }

    public void setPollTitle(String pollTitle) {
        this.pollTitle = pollTitle;
    }

    public List<PollOption> getPollOptions() {
        return pollOptions;
    }

    public void setPollOptions(List<PollOption> pollOptions) {
        this.pollOptions = pollOptions;
    }

    public long getPollCreationTime() {
        return pollCreationTime;
    }

    public void setPollCreationTime(long pollCreationTime) {
        this.pollCreationTime = pollCreationTime;
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

    public County getCounty() {
        return county;
    }

    public void setCounty(County county) {
        this.county = county;
    }
}
