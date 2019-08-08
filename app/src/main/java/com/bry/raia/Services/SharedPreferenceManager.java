package com.bry.raia.Services;

import android.content.Context;
import android.content.SharedPreferences;

import com.bry.raia.Models.Petition;
import com.bry.raia.Models.Poll;
import com.bry.raia.Models.PollOption;

import java.util.HashMap;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class SharedPreferenceManager {
    private final String TAG = SharedPreferenceManager.class.getSimpleName();
    private Context mContext;
    private final String Avatar = "AVATAR";
    private final String Name = "NAME";
    private final String Email = "EMAIL";
    private final String Phone = "PHONE_NO";
    private final String SignupDate = "SIGN_UP_DATE";
    private final String FirstTimeLaunch = "IS_FIRST_TIME_LAUNCH";

    public SharedPreferenceManager(Context context){
        this.mContext = context;
    }

    public void setAvatar(String uri){
        SharedPreferences pref = mContext.getSharedPreferences(Avatar, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear().putString(Avatar, uri).apply();
    }

    public String loadAvatar(){
        SharedPreferences prefs = mContext.getSharedPreferences(Avatar, MODE_PRIVATE);
        return prefs.getString(Avatar, "");
    }

    public SharedPreferenceManager setNameInSharedPref(String name){
        SharedPreferences pref = mContext.getSharedPreferences(Name, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear().putString(Name, name).apply();
        return this;
    }

    public String loadNameInSharedPref(){
        SharedPreferences prefs = mContext.getSharedPreferences(Name, MODE_PRIVATE);
        return prefs.getString(Name, "");
    }

    public void setEmailInSharedPref(String email){
        SharedPreferences pref = mContext.getSharedPreferences(Email, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear().putString(Email, email).apply();
    }

    public String loadEmailInSharedPref(){
        SharedPreferences prefs = mContext.getSharedPreferences(Email, MODE_PRIVATE);
        return prefs.getString(Email, "");
    }

    public void setPhoneInSharedPref(String phoneNo){
        SharedPreferences pref = mContext.getSharedPreferences(Phone, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear().putString(Phone, phoneNo).apply();
    }

    public String loadPhoneInSharedPref(){
        SharedPreferences prefs = mContext.getSharedPreferences(Phone, MODE_PRIVATE);
        return prefs.getString(Phone, "");
    }

//    public MyTime loadSignUpDateInSharedPref(){
//        SharedPreferences prefs = mContext.getSharedPreferences(SignupDate, MODE_PRIVATE);
//        Calendar c = Calendar.getInstance();
//        c.setTimeInMillis(Long.parseLong(prefs.getString(SignupDate, "0")));
//        return new MyTime(c);
//    }

//    public void setSignUpDateInSharedPref(MyTime time){
//        String c = Long.toString(time.getC().getTimeInMillis());
//        SharedPreferences pref = mContext.getSharedPreferences(SignupDate, MODE_PRIVATE);
//        SharedPreferences.Editor editor = pref.edit();
//        editor.clear().putString(SignupDate,c).apply();
//    }

    public void setIsFirstTimeLaunch(Boolean b){
        SharedPreferences pref = mContext.getSharedPreferences(FirstTimeLaunch, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear().putBoolean(FirstTimeLaunch, b).apply();
    }

    public boolean isFirstTimeLaunch(){
        SharedPreferences prefs = mContext.getSharedPreferences(FirstTimeLaunch, MODE_PRIVATE);
        return prefs.getBoolean(FirstTimeLaunch, true);
    }

    public void recordPetition(String id){
        SharedPreferences pref = mContext.getSharedPreferences(id, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(id, id).apply();
    }

    public void removePetitionSignature(Petition p){
        String id = p.getPetitionId();
        SharedPreferences pref = mContext.getSharedPreferences(id, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.remove(id).apply();
    }

    public boolean hasUserSignedPetition(Petition p){
        String id = p.getPetitionId();
        String nullVal = "NULL YEAH NULL";
        SharedPreferences pref = mContext.getSharedPreferences(id, MODE_PRIVATE);
        String s = pref.getString(id,nullVal);
        return !s.equals(nullVal);
    }


    public void recordPollVote(String id, PollOption po){
        String poId = po.getOptionId();
        SharedPreferences pref = mContext.getSharedPreferences(id, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(id, poId).apply();
    }

    public void removePollVote(Poll p){
        String id = p.getPollId();
        SharedPreferences pref = mContext.getSharedPreferences(id, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.remove(id).apply();
    }

    public boolean hasUserVotedInPoll(Poll p){
        String id = p.getPollId();
        String nullVal = "NULL YEAH NULL";
        SharedPreferences pref = mContext.getSharedPreferences(id, MODE_PRIVATE);
        String s = pref.getString(id,nullVal);
        return !s.equals(nullVal);
    }

    public PollOption getWhichPollOptionSelected(Poll p){
        String pollId = p.getPollId();
        String nullVal = "NULL YEAH NULL";
        SharedPreferences pref = mContext.getSharedPreferences(pollId, MODE_PRIVATE);
        String optionId = pref.getString(pollId,nullVal);
        if(!optionId.equals(nullVal)){
            for(PollOption po:p.getPollOptions()){
                if(po.getOptionId().equals(optionId)) return po;
            }
        }
        return p.getPollOptions().get(0);
    }

    public void recordAllPollVotes(HashMap<String,PollOption> allPolls){
        for(String pollId:allPolls.keySet()){
            PollOption option = allPolls.get(pollId);
            recordPollVote(pollId,option);
        }
    }

    public void recordAllPetitions(List<String> petitionIds){
        for(String pIds: petitionIds){
            recordPetition(pIds);
        }
    }

}
