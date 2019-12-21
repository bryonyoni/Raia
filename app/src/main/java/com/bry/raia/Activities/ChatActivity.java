package com.bry.raia.Activities;

import android.animation.Animator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bry.raia.Adapters.ChatActivitySearchedItemAdapter;
import com.bry.raia.Constants;
import com.bry.raia.Fragments.UserChatFragment;
import com.bry.raia.Models.User;
import com.bry.raia.R;
import com.bry.raia.Services.Utils;
import com.bry.raia.Variables;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener{
    private Context mContext;
    @Bind(R.id.existingUsersCardView) CardView existingUsersCardView;
    private boolean isSearchPartOpen = false;
    @Bind(R.id.searchUserEditText) EditText searchUserEditText;
    @Bind(R.id.searchSomethingTextView) TextView searchSomethingTextView;
    @Bind(R.id.chatsRecyclerView) RecyclerView chatsRecyclerView;
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
    private boolean isTextChatPartOpen = false;
    @Bind(R.id.userImageView) ImageView userImageView;
    @Bind(R.id.ChatTitle) TextView ChatTitleTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mContext = this.getApplicationContext();
        ButterKnife.bind(this);
        setClickListeners();

        loadAllUsers();
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
                hideLoadingScreen();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

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

        if(Variables.uploaderImage !=null){
            userImageView.setImageBitmap(Variables.uploaderImage);
        }else{
            userImageView.setImageDrawable(getResources().getDrawable(R.drawable.grey_back));
        }
        ChatTitleTextView.setText(user.getName());

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

        MyChatPagerAdapter mcPagerAdapter = new MyChatPagerAdapter(getSupportFragmentManager(),mContext);
        textChatViewPager.setAdapter(mcPagerAdapter);
    }

    public static class MyChatPagerAdapter extends FragmentPagerAdapter {
        private  int NUM_ITEMS = 3;
        private Context myContext;

        public MyChatPagerAdapter(FragmentManager fragmentManager, Context context) {
            super(fragmentManager);
            myContext = context;
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            UserChatFragment frag = new UserChatFragment();
            frag.setData(myContext);
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
