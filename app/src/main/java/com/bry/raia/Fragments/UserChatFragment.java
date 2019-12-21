package com.bry.raia.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bry.raia.R;

public class UserChatFragment extends Fragment {
    private Context mContext;

    public UserChatFragment(){}

    public void setData(Context context){
        mContext = context;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_chat, container, false);


        return view;
    }
}
