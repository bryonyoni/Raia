package com.bry.raia.Models;

public class PollOption {
    private String optionId;
    private String optionText;
    private long votes = 0;

    public PollOption(){}

    public PollOption(String optionText){
        this.optionText = optionText;
    }


    public String getOptionId() {
        return optionId;
    }

    public void setOptionId(String optionId) {
        this.optionId = optionId;
    }

    public String getOptionText() {
        return optionText;
    }

    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }

    public long getVotes() {
        return votes;
    }

    public void setVotes(long votes) {
        this.votes = votes;
    }
}
