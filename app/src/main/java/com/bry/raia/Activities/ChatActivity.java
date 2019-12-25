package com.bry.raia.Activities;

import android.animation.Animator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bry.raia.Adapters.ChatActivitySearchedItemAdapter;
import com.bry.raia.Adapters.ChatsActivityConversationItemAdapter;
import com.bry.raia.Constants;
import com.bry.raia.Fragments.UserChatFragment;
import com.bry.raia.Models.Chat;
import com.bry.raia.Models.Message;
import com.bry.raia.Models.User;
import com.bry.raia.R;
import com.bry.raia.Services.Utils;
import com.bry.raia.Variables;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener{
    private Context mContext;
    @Bind(R.id.existingUsersCardView) CardView existingUsersCardView;
    private boolean isSearchPartOpen = false;
    @Bind(R.id.searchUserEditText) EditText searchUserEditText;
    @Bind(R.id.noChatsTextView) TextView noChatsTextView;
    @Bind(R.id.searchSomethingTextView) TextView searchSomethingTextView;
    @Bind(R.id.chatsRecyclerView) RecyclerView chatsRecyclerView;
    private ChatsActivityConversationItemAdapter chaConvoItemAdapter;
    private int mAnimationTime = 300;

    @Bind(R.id.progressBarRelativeLayout) RelativeLayout progressBarRelativeLayout;
    @Bind(R.id.mainLinearLayout) LinearLayout mainLinearLayout;
    @Bind(R.id.searchedUsersRecyclerView) RecyclerView searchedUsersRecyclerView;

    private List<User> allUsers = new ArrayList<>();
    private List<User> selectedUsers = new ArrayList<>();
    @Bind(R.id.noFoundUsersTextView) TextView noFoundUsersTextView;
    private ChatActivitySearchedItemAdapter chatActivityFilterUser;

    @Bind(R.id.resultsNumberTextView) TextView resultsNumberTextView;

    @Bind(R.id.textChatViewPager) ViewPager textChatViewPager;
    private MyChatPagerAdapter mChatPagerAdapter;
    private boolean isTextChatPartOpen = false;
    @Bind(R.id.userImageView) ImageView userImageView;
    @Bind(R.id.ChatTitle) TextView ChatTitleTextView;
    private List<Chat> myChats = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mContext = this.getApplicationContext();
        ButterKnife.bind(this);
        setClickListeners();

        loadAllUsers();

        LocalBroadcastManager.getInstance(mContext).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String code = intent.getExtras().getString("code");
                if(code.equals(Constants.NEW_CHAT_CREATED)){
//                    myChats.add(Variables.chat);
                    loadMyChatsIntoRecyclerView();
                }else if(code.equals(Constants.NEW_MESSAGE_EVENT)){
                    int pos = getChatForUser(Variables.chat.getUser());
                    if(pos!=-1){
//                        Message newMsg = Variables.chat.getLastSentMessage();
//                        myChats.get(pos).getAllMessages().add(newMsg);
                        loadMyChatsIntoRecyclerView();
                    }
                }
            }
        },new IntentFilter(Constants.CHAT_EVENTS));
    }

    private void loadAllUsers() {
        showLoadingScreen();
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS);
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot userSnap:dataSnapshot.getChildren()){
                        String name = userSnap.child(Constants.USERNAME).getValue(String.class);
                        String email = userSnap.child(Constants.EMAIL).getValue(String.class);
                        String signUpTime = userSnap.child(Constants.TIME_OF_SIGNUP).getValue(String.class);
                        String uid = userSnap.getKey();

                        User user = new User(name,uid);
                        user.setEmail(email);
                        user.setSignUpTime(Long.parseLong(signUpTime));

                        String my_uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        if(!my_uid.equals(uid)) allUsers.add(user);
                    }
                }
                loadAllChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void loadAllChats(){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference chatsRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS)
                .child(uid).child(Constants.CHATS);
        chatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> chatIds = new ArrayList<>();
                for(DataSnapshot snap: dataSnapshot.getChildren()){
                    String chat = snap.getValue(String.class);
                    chatIds.add(chat);
                }
//                iterations = chatIds.size();
                loadMyConversations(chatIds);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadMyConversations(final List<String> chats){
        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference(Constants.CHATS);
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.e("ChatActivity",dataSnapshot.toString());
                if(dataSnapshot.exists()){
                    for(String chatString: chats){
                        Log.e("ChatActivity",chatString);
                        Chat chat = dataSnapshot.child(chatString).getValue(Chat.class);
                        if(dataSnapshot.child(chatString).child(Constants.FIREBASE_MESSAGES).exists()){
                            for(DataSnapshot messageSnap: dataSnapshot.child(chatString).child(Constants.FIREBASE_MESSAGES).getChildren()){
                                Log.e("ChatActivity",messageSnap.toString());
                                Message message = messageSnap.getValue(Message.class);
                                chat.getAllMessages().add(message);
                            }
                        }
                        if(dataSnapshot.child(chatString).child("user").exists()){
                            User user = dataSnapshot.child(chatString).child("user").getValue(User.class);
                            chat.setUser(user);
                        }
                        myChats.add(chat);
                    }
                }
                loadMyChatsIntoRecyclerView();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private int iterations;
    private void loadAllConversations(List<String> chats){
        if(iterations == -1 || chats.size()==0){
            // its finished up loading chats
            iterations = 0;
            loadMyChatsIntoRecyclerView();
        }else{
            loadEachConversation(chats.get(iterations-1), chats);
        }
    }


    private void loadEachConversation(String chatId, final List<String> chats){
        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference(Constants.CHATS).child(chatId);
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    if(dataSnapshot.child(Constants.FIREBASE_MESSAGES).exists()){
                        for(DataSnapshot messageSnap: dataSnapshot.child(Constants.FIREBASE_MESSAGES).getChildren()){
                            Message message = messageSnap.getValue(Message.class);
                            chat.getAllMessages().add(message);
                        }
                    }
                    myChats.add(chat);
                }
                iterations--;
                loadAllConversations(chats);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadMyChatsIntoRecyclerView(){
        if(myChats.isEmpty()){
            noChatsTextView.setVisibility(View.VISIBLE);
        }else{
            noChatsTextView.setVisibility(View.GONE);
        }
        chaConvoItemAdapter = new ChatsActivityConversationItemAdapter(ChatActivity.this,myChats);
        chatsRecyclerView.setAdapter(chaConvoItemAdapter);
        chatsRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));

        hideLoadingScreen();
        LocalBroadcastManager.getInstance(mContext).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                openChatWithUser(Variables.chatToOpen.getUser());
            }
        },new IntentFilter(Constants.OPEN_CHAT));
    }



    private void showLoadingScreen(){
        mainLinearLayout.setVisibility(View.INVISIBLE);
        progressBarRelativeLayout.setVisibility(View.VISIBLE);
    }

    private void hideLoadingScreen(){
        mainLinearLayout.setVisibility(View.VISIBLE);
        progressBarRelativeLayout.setVisibility(View.INVISIBLE);
    }

    private void setClickListeners(){
        searchSomethingTextView.setOnClickListener(this);
    }


    private void openSearchPart(){
        isSearchPartOpen = true;
        existingUsersCardView.setVisibility(View.VISIBLE);
        searchUserEditText.setText("");
        searchUserEditText.setVisibility(View.VISIBLE);
        searchSomethingTextView.animate().alpha(0f).setDuration(mAnimationTime).start();

        existingUsersCardView.setVisibility(View.VISIBLE);
        chatsRecyclerView.setTranslationY(Utils.dpToPx(-200));
        chatsRecyclerView.animate().setDuration(mAnimationTime).setInterpolator(new LinearOutSlowInInterpolator()).translationY(Utils.dpToPx(0))
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        chatsRecyclerView.setTranslationY(0);
                        searchSomethingTextView.setAlpha(0f);
                        searchSomethingTextView.setVisibility(View.GONE);
                        searchUserEditText.setFocusableInTouchMode(true);
                        searchUserEditText.requestFocus();
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                }).start();

        searchUserEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String changedWords = charSequence.subSequence(i, charSequence.length()).toString();
                selectedUsers = new ArrayList<>();

                for(User c: allUsers){
                    if(c.getName().toLowerCase().contains(changedWords.toLowerCase())){
                        selectedUsers.add(c);
                    }
                }
                loadAllUsers(selectedUsers);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        loadAllUsers(allUsers);

        LocalBroadcastManager.getInstance(mContext).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int userId = intent.getExtras().getInt("user");
                if(!selectedUsers.isEmpty()){
                    User user = selectedUsers.get(userId);
                    openChatWithUser(user);
                }else{
                    User user = allUsers.get(userId);
                    openChatWithUser(user);
                }
                closeSearchPart();
            }
        },new IntentFilter(Constants.SELECTED_PERSON));


    }

    private void loadAllUsers(List<User> selectedUsers){
        resultsNumberTextView.setText(String.format(getResources().getString(R.string.results_found), selectedUsers.size()));
        if(selectedUsers.isEmpty()){
            noFoundUsersTextView.setVisibility(View.VISIBLE);
        }else{
            noFoundUsersTextView.setVisibility(View.GONE);
        }
        chatActivityFilterUser = new ChatActivitySearchedItemAdapter(selectedUsers,ChatActivity.this);
        searchedUsersRecyclerView.setAdapter(chatActivityFilterUser);
        searchedUsersRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
    }

    private void closeSearchPart(){
        isSearchPartOpen = false;

        searchSomethingTextView.setVisibility(View.VISIBLE);
        searchSomethingTextView.animate().alpha(1f).setDuration(mAnimationTime).start();
        existingUsersCardView.setVisibility(View.GONE);
        chatsRecyclerView.setTranslationY(Utils.dpToPx(200));

        chatsRecyclerView.animate().setDuration(mAnimationTime).setInterpolator(new LinearOutSlowInInterpolator()).translationY(Utils.dpToPx(0))
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        chatsRecyclerView.setTranslationY(0);
                        searchSomethingTextView.setAlpha(1f);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                }).start();
    }

    @Override
    public void onBackPressed(){
        if(isSearchPartOpen) closeSearchPart();
        else if(isTextChatPartOpen) closeChatWithUser();
        else super.onBackPressed();
    }


    @Override
    public void onClick(View view) {
        if(view.equals(searchSomethingTextView)){
            openSearchPart();
        }
    }


    private void openChatWithUser(User user){
        isTextChatPartOpen = true;

        textChatViewPager.setVisibility(View.VISIBLE);
        userImageView.setVisibility(View.VISIBLE);

        userImageView.animate().alpha(1f).setDuration(mAnimationTime).start();

        ChatTitleTextView.animate().translationX(0).setDuration(mAnimationTime).setInterpolator(new LinearOutSlowInInterpolator()).start();

        textChatViewPager.animate().alpha(1f).setDuration(mAnimationTime).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                textChatViewPager.setAlpha(1f);
                userImageView.setAlpha(1f);
                ChatTitleTextView.setTranslationX(0);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        }).start();


        if(doesChatExistForUser(user)){
            sortChats();
            mChatPagerAdapter = new MyChatPagerAdapter(getSupportFragmentManager(),mContext, myChats);
            textChatViewPager.setAdapter(mChatPagerAdapter);
            textChatViewPager.setCurrentItem(getChatForUser(user));
        }else{
            //create new chat for user
            Chat chat = new Chat(user);
            chat.setTimeOfStart(Calendar.getInstance().getTimeInMillis());
            chat.setChatId(generateRandomString());
            chat.setLastActiveTime(Calendar.getInstance().getTimeInMillis());
            myChats.add(chat);

            sortChats();
            MyChatPagerAdapter mcPagerAdapter = new MyChatPagerAdapter(getSupportFragmentManager(),mContext, myChats);
            textChatViewPager.setAdapter(mcPagerAdapter);
        }

        textChatViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                ChatTitleTextView.setText(myChats.get(i).getUser().getName());
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        if(Variables.blurredBacks.containsKey(user.getUId())){
            userImageView.setImageBitmap(Variables.blurredBacks.get(user.getUId()));

            Glide.with(mContext).load(bitmapToByte(Objects.requireNonNull(Variables.blurredBacks.get(user.getUId())))).asBitmap().centerCrop()
                    .into(new BitmapImageViewTarget(userImageView) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            try {
                                RoundedBitmapDrawable circularBitmapDrawable =
                                        RoundedBitmapDrawableFactory.create(mContext.getResources(), resource);
//                                Bitmap.createScaledBitmap(resource,100,100,false));
                                circularBitmapDrawable.setCircular(true);
                                userImageView.setImageDrawable(circularBitmapDrawable);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
        }else{
            userImageView.setImageDrawable(getResources().getDrawable(R.drawable.grey_back));
        }

        ChatTitleTextView.setText(user.getName());
    }

    private byte[] bitmapToByte(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte[] byteArray = baos.toByteArray();
        return byteArray;
    }

    private String generateRandomString(){
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    private void sortChats() {
        if(!myChats.isEmpty()){
            List<Long> times = new ArrayList<>();
            HashMap<Long,String> timesAndChats = new LinkedHashMap<>();
            HashMap<String,Chat> chatsHashMap = new LinkedHashMap<>();
            for(Chat chat: myChats){
                timesAndChats.put(chat.getLastActiveTime(),chat.getChatId());
                times.add(chat.getLastActiveTime());
                chatsHashMap.put(chat.getChatId(),chat);
            }
            Collections.sort(times);
            List<Chat> newChatList = new ArrayList<>();
            for(Long time:times){
                String chatId = timesAndChats.get(time);
                Chat chat = chatsHashMap.get(chatId);
                newChatList.add(chat);
            }
            myChats = newChatList;
        }
    }

    private boolean doesChatExistForUser(User user){
        for(Chat chat: myChats){
            if(chat.getUser().getUId().equals(user.getUId())) return true;
        }
        return false;
    }

    private int getChatForUser(User user){
        for(Chat chat: myChats){
            if(chat.getUser().getUId().equals(user.getUId())) return myChats.indexOf(chat);
        }
        return -1;
    }

    public static class MyChatPagerAdapter extends FragmentPagerAdapter {
        private Context myContext;
        private List<Chat> myChats;

        public MyChatPagerAdapter(FragmentManager fragmentManager, Context context, List<Chat> chats) {
            super(fragmentManager);
            myContext = context;
            this.myChats = chats;
        }



        // Returns total number of pages
        @Override
        public int getCount() {
            return myChats.size();
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            UserChatFragment frag = new UserChatFragment();
            frag.setData(myContext,myChats.get(position));
            return frag;
        }


    }

    private void closeChatWithUser(){
        isTextChatPartOpen = false;

        userImageView.animate().alpha(0f).setDuration(mAnimationTime).start();

        ChatTitleTextView.setText(getResources().getString(R.string.chats));

        ChatTitleTextView.animate().translationX(-Utils.dpToPx(30)).setDuration(mAnimationTime).start();

        textChatViewPager.animate().alpha(0f).setDuration(mAnimationTime).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                textChatViewPager.setAlpha(0f);
                textChatViewPager.setVisibility(View.GONE);

                userImageView.setAlpha(1f);
                userImageView.setVisibility(View.INVISIBLE);

                ChatTitleTextView.setTranslationX(-Utils.dpToPx(30));
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        }).start();
    }
}
