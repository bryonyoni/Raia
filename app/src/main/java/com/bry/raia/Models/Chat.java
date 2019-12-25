package com.bry.raia.Models;

import java.util.ArrayList;
import java.util.List;

public class Chat {
    private String chatId;
    private String firebaseChatId;
    private User user;
    private long timeOfStart;
    private long lastActiveTime;
    private List<Message> allMessages = new ArrayList<>();

    public Chat(){}

    public Chat(User user){
        this.user = user;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public long getTimeOfStart() {
        return timeOfStart;
    }

    public void setTimeOfStart(long timeOfStart) {
        this.timeOfStart = timeOfStart;
    }

    public List<Message> getAllMessages() {
        return allMessages;
    }

    public void setAllMessages(List<Message> allMessages) {
        this.allMessages = allMessages;
    }

    public long getLastActiveTime() {
        return lastActiveTime;
    }

    public void setLastActiveTime(long lastActiveTime) {
        this.lastActiveTime = lastActiveTime;
    }

    public String getFirebaseChatId() {
        return firebaseChatId;
    }

    public void setFirebaseChatId(String firebaseChatId) {
        this.firebaseChatId = firebaseChatId;
    }

    public Message getLastSentMessage(){
        if(allMessages.isEmpty()){
            return null;
        }else{
            return allMessages.get(allMessages.size()-1);
        }
    }

}
