package com.bry.raia.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.bry.raia.Adapters.ChatsActivityMessageItemAdapter;
import com.bry.raia.Constants;
import com.bry.raia.Models.Chat;
import com.bry.raia.Models.Message;
import com.bry.raia.R;
import com.bry.raia.Variables;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class UserChatFragment extends Fragment {
    private Context mContext;
    private Chat mChat;
    private RecyclerView messagesRecyclerView;
    private EditText typeMessageEditText;
    private ImageView sendMessageImageView;
    private ChatsActivityMessageItemAdapter chatsActivityMessageItemAdapter;
    private boolean isListenerSet = false;

    public UserChatFragment(){}

    public void setData(Context context, Chat chat){
        mContext = context;
        mChat = chat;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_chat, container, false);
        messagesRecyclerView = view.findViewById(R.id.messagesRecyclerView);
        typeMessageEditText = view.findViewById(R.id.typeMessageEditText);
        sendMessageImageView = view.findViewById(R.id.sendMessageImageView);

        sendMessageImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(typeMessageEditText.getText().toString().trim().equals(""))
                    typeMessageEditText.setError(getResources().getString(R.string.say_something));
                else sendMessage();
            }
        });

        loadMessages();

        if(mChat.getFirebaseChatId()!=null){
            DatabaseReference chatMessageRef = FirebaseDatabase.getInstance().getReference(Constants.CHATS).child(mChat.getFirebaseChatId());
            chatMessageRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    Log.e("UserChatFragment", dataSnapshot.toString());
                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    Message lastMessage = null;
                    if(dataSnapshot.child(Constants.FIREBASE_MESSAGES).exists()){
                        for(DataSnapshot lastSnap: dataSnapshot.child(Constants.FIREBASE_MESSAGES).getChildren()){
                            lastMessage = lastSnap.getValue(Message.class);
                        }
                        if(!lastMessage.getSenderId().equals(uid)){
                            //its not from me
                            mChat.getAllMessages().add(lastMessage);
                            loadMessages();
                        }
                    }
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            isListenerSet = true;
        }

        return view;
    }

    private void loadMessages() {
        chatsActivityMessageItemAdapter = new ChatsActivityMessageItemAdapter(mContext, mChat.getAllMessages());
        messagesRecyclerView.setAdapter(chatsActivityMessageItemAdapter);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
    }

    private void SendBroadCastToMessage(String code, Message message){
        Intent i = new Intent(message.getMessageId());
        i.putExtra("code",code);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(i);
    }

    private void sendMessage(){
        String messageText = typeMessageEditText.getText().toString().trim();
        final Message message = new Message(messageText, Calendar.getInstance().getTimeInMillis());
        boolean isNewChat = false;
        if(mChat.getFirebaseChatId()==null){
            isNewChat = true;
            DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference(Constants.CHATS);
            DatabaseReference specificChatRef = chatRef.push();
            String key = specificChatRef.getKey();
            mChat.setFirebaseChatId(key);

            specificChatRef.setValue(mChat);

            DatabaseReference usersChatRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS)
                    .child(mChat.getUser().getUId()).child(Constants.CHATS);
            usersChatRef.push().setValue(key);

            usersChatRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS)
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(Constants.CHATS);
            usersChatRef.push().setValue(key);
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        message.setSenderId(uid);
        message.setChatId(mChat.getFirebaseChatId());

        DatabaseReference chatMessageRef = FirebaseDatabase.getInstance().getReference(Constants.CHATS)
                .child(mChat.getFirebaseChatId()).child(Constants.FIREBASE_MESSAGES);
        DatabaseReference specificChatRef = chatMessageRef.push();
        String key = specificChatRef.getKey();
        message.setMessageId(key);

        mChat.getAllMessages().add(message);
        loadMessages();

        SendBroadCastToMessage(Constants.SENDING, message);
        specificChatRef.setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                SendBroadCastToMessage(Constants.SENT,message);
            }
        });

        if(isNewChat) {
            Variables.chat = mChat;
            Intent intent = new Intent(Constants.CHAT_EVENTS);
            intent.putExtra("code", Constants.NEW_CHAT_CREATED);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        }else{
            Variables.chat = mChat;
            Intent intent = new Intent(Constants.CHAT_EVENTS);
            intent.putExtra("code", Constants.NEW_MESSAGE_EVENT);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        }
        if(!isListenerSet) setListenerForNewMessages();
    }

    private void setListenerForNewMessages(){
        DatabaseReference chatMessageRef = FirebaseDatabase.getInstance().getReference(Constants.CHATS).child(mChat.getFirebaseChatId());
        chatMessageRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                if(dataSnapshot.exists()){
                    Message newMessage = dataSnapshot.getValue(Message.class);
                    if(!newMessage.getSenderId().equals(uid)){
                        //its not from me
                        mChat.getAllMessages().add(newMessage);
                        loadMessages();
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        isListenerSet = true;
    }
}
