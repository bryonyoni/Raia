package com.bry.raia.Activities;

import android.animation.Animator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bry.raia.Adapters.UserSettingsActivityCountyItemAdapter;
import com.bry.raia.Adapters.UserSettingsActivityLanguageItemAdapter;
import com.bry.raia.Adapters.UserSettingsPostItemAdapter;
import com.bry.raia.Adapters.ViewPostActivityCommentItemAdapter;
import com.bry.raia.Constants;
import com.bry.raia.Models.Announcement;
import com.bry.raia.Models.Comment;
import com.bry.raia.Models.County;
import com.bry.raia.Models.Language;
import com.bry.raia.Models.MyRecyclerView;
import com.bry.raia.Models.Petition;
import com.bry.raia.Models.PetitionSignature;
import com.bry.raia.Models.Poll;
import com.bry.raia.Models.PollOption;
import com.bry.raia.Models.Post;
import com.bry.raia.R;
import com.bry.raia.Services.DatabaseManager;
import com.bry.raia.Services.SharedPreferenceManager;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class UserSettingsActivity extends AppCompatActivity implements View.OnClickListener{
    private Context mContext;
    private int mAnimationTime = 300;
    @Bind(R.id.userImageView) ImageView userProfileImageView;
    @Bind(R.id.userNameTextView) TextView userProfileNameTextView;
    @Bind(R.id.userEmailTextView) TextView userEmailTextView;
    @Bind(R.id.signUpTimeTextView) TextView signUpTimeTextView;

    @Bind(R.id.setCountryRelativeLayout) RelativeLayout setCountryRelativeLayout;
    @Bind(R.id.setCountyTextView) TextView setCountyTextView;
    @Bind(R.id.setLanguageRelativeLayout) RelativeLayout setLanguageRelativeLayout;
    @Bind(R.id.setLanguageTextView) TextView setLanguageTextView;
    @Bind(R.id.logoutButtonRelativeLayout) RelativeLayout logoutButtonRelativeLayout;

    @Bind(R.id.setProfilePhotoRelativeLayout) RelativeLayout setProfilePhotoRelativeLayout;
    @Bind(R.id.viewPostsRelativeLayout) RelativeLayout viewPostsRelativeLayout;
    @Bind(R.id.backBlackRelativeLayout) RelativeLayout backBlackRelativeLayout;

    @Bind(R.id.openCameraImageView) ImageView openCameraImageView;
    @Bind(R.id.openFilesImageView) ImageView openFilesImageView;
    @Bind(R.id.deletePhotoImageView) ImageView deletePhotoImageView;

    @Bind(R.id.ChangePhotoRelativeLayout) RelativeLayout ChangePhotoRelativeLayout;
    private boolean isChangePhotoOptionOpen = false;
    private boolean isBlackBackOpen = false;

    private final int PICK_IMAGE_REQUEST = 1012;
    private final int REQUEST_IMAGE_CAPTURE = 1;
    private Uri mFilepath;

    private Bitmap userImageBitmap = null;
    private boolean canAnimateImageLoadingScreens = false;

    @Bind(R.id.ChangeLanguageRelativeLayout) RelativeLayout ChangeLanguageRelativeLayout;
    @Bind(R.id.languagesListRecyclerView) RecyclerView languagesListRecyclerView;
    @Bind(R.id.newLanguageTextView) TextView newLanguageTextView;
    @Bind(R.id.setLanguageButton) Button setLanguageButton;
    @Bind(R.id.cancelSetLanguageButton) Button cancelSetLanguageButton;
    private boolean isSetLanguagePartOpen = false;
    private Language pickedLanguageOption;

    @Bind(R.id.ChangeCountyRelativeLayout) RelativeLayout ChangeCountyRelativeLayout;
    @Bind(R.id.countyListRecyclerView) RecyclerView countyListRecyclerView;
    @Bind(R.id.newCountyTextView) TextView newCountyTextView;
    @Bind(R.id.setCountyButton) Button setCountyButton;
    @Bind(R.id.cancelSetCountyButton) Button cancelSetCountyButton;
    private boolean isSetCountyPartOpen = false;
    private County pickedCountyOption;

    @Bind(R.id.LogoutRelativeLayout) RelativeLayout LogoutRelativeLayout;
    private boolean isLogoutPartOpen = false;
    @Bind(R.id.logoutButton) Button logoutButton;
    @Bind(R.id.cancelLogoutButton) Button cancelLogoutButton;

    @Bind(R.id.loadedPostsRecyclerView) MyRecyclerView loadedPostsRecyclerView;
    private UserSettingsPostItemAdapter UserSettingsPostItemAdapter;
    @Bind(R.id.loadingContainerLinearLayout) LinearLayout loadingContainerLinearLayout;
    private boolean canAnimateLoadingScreens = false;

    @Bind(R.id.myPostsRelativelayout) RelativeLayout myPostsRelativelayout;
    private boolean isMyPostsPartOpen = false;
    private List<String> announcements = new ArrayList<>();
    private List<String> petitions = new ArrayList<>();
    private List<String> polls = new ArrayList<>();

    @Bind(R.id.viewPostRelativeLayout) RelativeLayout viewPostRelativeLayout;
    private boolean isViewPostShowing = false;
    @Bind(R.id.postTypeTextView) TextView postTypeTextView;
    @Bind(R.id.vpuserNameTextView) TextView userNameTextView;
    @Bind(R.id.postTitleTextView) TextView postTitleTextView;

    @Bind(R.id.announcementCardView) LinearLayout announcementCardView;
    @Bind(R.id.countyTextViewAnnouncement) TextView countyTextViewAnnouncement;
    @Bind(R.id.announcementPostImageViewBack) ImageView announcementPostImageViewBack;
    @Bind(R.id.announcementImageView) ImageView announcementImageView;
    @Bind(R.id.AnnouncementTitleTextView) TextView AnnouncementTitleTextView;

    @Bind(R.id.petitionUiLinearLayout) LinearLayout petitionUiLinearLayout;
    @Bind(R.id.countyTextViewPetition) TextView countyTextViewPetition;
    @Bind(R.id.petitionImageViewBack) ImageView petitionImageViewBack;
    @Bind(R.id.petitionImageView) ImageView petitionImageView;
    @Bind(R.id.numberSignedTextView) TextView numberSignedTextView;
    @Bind(R.id.petitionPercentageView)
    ProgressBar petitionPercentageView;
    @Bind(R.id.signTextView) TextView signTextView;
    @Bind(R.id.PetitionTitleTextView) TextView PetitionTitleTextView;

    @Bind(R.id.pollRelativeLayout) RelativeLayout pollRelativeLayout;
    @Bind(R.id.option1LinearLayout) LinearLayout option1LinearLayout;
    @Bind(R.id.countyTextViewPoll) TextView countyTextViewPoll;
    @Bind(R.id.pollOption1CheckBox) CheckBox pollOption1CheckBox;
    @Bind(R.id.option1PercentageTextView)TextView option1PercentageTextView;
    @Bind(R.id.option1PercentageBarView)ProgressBar option1PercentageBarView;
    @Bind(R.id.option2LinearLayout)LinearLayout option2LinearLayout;
    @Bind(R.id.option2CheckBox)CheckBox option2CheckBox;
    @Bind(R.id.option2PercentageTextView)TextView option2PercentageTextView;
    @Bind(R.id.option2PercentageBarView)ProgressBar option2PercentageBar;
    @Bind(R.id.option3LinearLayout)LinearLayout option3LinearLayout;
    @Bind(R.id.option3CheckBox)CheckBox option3CheckBox;
    @Bind(R.id.option3PercentageTextView)TextView option3PercentageTextView;
    @Bind(R.id.option3PercentageBarView)ProgressBar option3PercentageView;
    @Bind(R.id.option4LinearLayout)LinearLayout option4LinearLayout;
    @Bind(R.id.option4CheckBox)CheckBox option4CheckBox;
    @Bind(R.id.option4PercentageTextView)TextView option4PercentageTextView;
    @Bind(R.id.option4PercentageBarView)ProgressBar option4PercentageBarView;

    @Bind(R.id.pollVoteCountTextView) TextView pollVoteCountTextView;
    @Bind(R.id.PollTitleTextView) TextView PollTitleTextView;
    private boolean isPresetingCheckButons = false;

    @Bind(R.id.addCommentEditText)
    EditText addCommentEditText;
    @Bind(R.id.sendCommentImageView) ImageView sendCommentImageView;
    @Bind(R.id.commentsRecyclerViewMain) RecyclerView commentsRecyclerView;
    @Bind(R.id.noCommentsTextView) TextView noCommentsTextView;
    ViewPostActivityCommentItemAdapter vpActivityCommentAdapter;
    List<Comment> allComments = new ArrayList<>();
    @Bind(R.id.loadingCommentsContainerLinearLayout) LinearLayout loadingCommentsContainerLinearLayout;

    @Bind(R.id.viewCommentLinearLayout) LinearLayout viewCommentLinearLayout;
    private boolean isShowingCommentRepliesPart = false;
    @Bind(R.id.userNameRepliesTextView) TextView userNameRepliesTextView;
    @Bind(R.id.commentTimeTextView) TextView commentTimeTextView;
    @Bind(R.id.commentBodyTextView) TextView commentBodyTextView;
    @Bind(R.id.addReplyEditText) EditText addReplyEditText;
    @Bind(R.id.replyButtonTextView) TextView replyButtonTextView;
    @Bind(R.id.repliesRecyclerView) RecyclerView repliesRecyclerView;
    @Bind(R.id.noRepliesMessage) TextView noRepliesMessage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);
        ButterKnife.bind(this);

        mContext = this.getApplicationContext();
        setClickListeners();
        loadData();
    }

    private void loadData(){
        if(!new SharedPreferenceManager(mContext).loadAvatar().equals("")){
            String uiString = new SharedPreferenceManager(mContext).loadAvatar();

            byte[] decodedByteArray = android.util.Base64.decode(uiString, Base64.DEFAULT);
            userImageBitmap = BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);

            Glide.with(mContext).load(bitmapToByte(userImageBitmap)).asBitmap().centerCrop().into(new BitmapImageViewTarget(userProfileImageView) {
                @Override
                protected void setResource(Bitmap resource) {
                    try{
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(mContext.getResources(),resource);
//                                Bitmap.createScaledBitmap(resource,100,100,false));
                        circularBitmapDrawable.setCircular(true);
                        userProfileImageView.setImageDrawable(circularBitmapDrawable);
                        new SharedPreferenceManager(mContext).setAvatar(encodeBitmapForFirebaseStorage(userImageBitmap));
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
        }else{
            showLoadingAnimationForAvatar();
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference avatarRef = FirebaseDatabase.getInstance().getReference(Constants.IMAGE_AVATAR).child(uid);
            avatarRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        String uiString = dataSnapshot.getValue(String.class);
                        new SharedPreferenceManager(mContext).setAvatar(uiString);
                        byte[] decodedByteArray = android.util.Base64.decode(uiString, Base64.DEFAULT);
                        userImageBitmap = BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);

                        Glide.with(mContext).load(bitmapToByte(userImageBitmap)).asBitmap().centerCrop().into(new BitmapImageViewTarget(userProfileImageView) {
                            @Override
                            protected void setResource(Bitmap resource) {
                                try{
                                    RoundedBitmapDrawable circularBitmapDrawable =
                                            RoundedBitmapDrawableFactory.create(mContext.getResources(),resource);
//                                Bitmap.createScaledBitmap(resource,100,100,false));
                                    circularBitmapDrawable.setCircular(true);
                                    userProfileImageView.setImageDrawable(circularBitmapDrawable);
                                    new SharedPreferenceManager(mContext).setAvatar(encodeBitmapForFirebaseStorage(userImageBitmap));
                                    hideLoadingAnimationForAvatar();
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        userProfileNameTextView.setText(new SharedPreferenceManager(mContext).loadNameInSharedPref());
        userEmailTextView.setText(new SharedPreferenceManager(mContext).loadEmailInSharedPref());

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(new SharedPreferenceManager(mContext).loadSignUpDateInSharedPref());

        int day = cal.get(Calendar.DAY_OF_MONTH);
        String month = getMonthName_Abbr(cal.get(Calendar.MONTH));
        int year = cal.get(Calendar.YEAR);

        signUpTimeTextView.setText(String.format(getResources().getString(R.string.acc_created_on), day, month, year));

        setLanguageTextView.setText(String.format(getResources().getString(R.string.language_set),
                new SharedPreferenceManager(mContext).loadLanguageInSharedPref().getName()));
    }

    private static String getMonthName_Abbr(int month) {
        Calendar calx = Calendar.getInstance();
        calx.set(Calendar.MONTH, month);
        SimpleDateFormat month_date = new SimpleDateFormat("MMM");
        return month_date.format(calx.getTime());
    }

    private void setClickListeners() {
        setProfilePhotoRelativeLayout.setOnClickListener(this);
        setCountryRelativeLayout.setOnClickListener(this);
        setLanguageRelativeLayout.setOnClickListener(this);
        logoutButtonRelativeLayout.setOnClickListener(this);
        viewPostsRelativeLayout.setOnClickListener(this);
    }

    private void openChangePhotoOption(){
        isChangePhotoOptionOpen = true;
        isBlackBackOpen = true;
        backBlackRelativeLayout.setVisibility(View.VISIBLE);
        backBlackRelativeLayout.animate().alpha(0.7f).setDuration(mAnimationTime).start();

        ChangePhotoRelativeLayout.setVisibility(View.VISIBLE);
        ChangePhotoRelativeLayout.animate().alpha(1f).translationY(0).setDuration(mAnimationTime).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        backBlackRelativeLayout.setVisibility(View.VISIBLE);
                        backBlackRelativeLayout.setAlpha(0.7f);

                        ChangePhotoRelativeLayout.setVisibility(View.VISIBLE);
                        ChangePhotoRelativeLayout.setAlpha(1f);
                        ChangePhotoRelativeLayout.setTranslationY(0);

                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                }).start();

        openFilesImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickPhotoFromStorage();
            }
        });

        openCameraImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickPhotoFromCamera();
            }
        });

        deletePhotoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DeletePhoto();
                closeChangePhotoOption();
            }
        });

    }

    private void DeletePhoto() {
        userProfileImageView.setImageDrawable(getDrawable(R.drawable.grey_back));
        new SharedPreferenceManager(mContext).setAvatar("");
        new DatabaseManager(mContext,"").updateImageAvatar("");
    }

    private void pickPhotoFromCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void pickPhotoFromStorage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"),PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            if (data.getData() != null) {
                mFilepath = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mFilepath);
                    userImageBitmap = getResizedBitmap(bitmap,1000);
                    Glide.with(mContext).load(bitmapToByte(userImageBitmap)).asBitmap().centerCrop().into(new BitmapImageViewTarget(userProfileImageView) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            try{
                                RoundedBitmapDrawable circularBitmapDrawable =
                                        RoundedBitmapDrawableFactory.create(mContext.getResources(),resource);
//                                Bitmap.createScaledBitmap(resource,100,100,false));
                                circularBitmapDrawable.setCircular(true);
                                userProfileImageView.setImageDrawable(circularBitmapDrawable);
                                String image = encodeBitmapForFirebaseStorage(userImageBitmap);
                                new SharedPreferenceManager(mContext).setAvatar(image);
                                new DatabaseManager(mContext,"").updateImageAvatar(image);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    });
                    closeChangePhotoOption();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try{
                Bundle extras = data.getExtras();
                Bitmap bitmap = (Bitmap) extras.get("data");
                userImageBitmap = getResizedBitmap(bitmap,1000);

                Glide.with(mContext).load(bitmapToByte(userImageBitmap)).asBitmap().centerCrop().into(new BitmapImageViewTarget(userProfileImageView) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        try{
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(mContext.getResources(),resource);
//                                Bitmap.createScaledBitmap(resource,100,100,false));
                            circularBitmapDrawable.setCircular(true);
                            userProfileImageView.setImageDrawable(circularBitmapDrawable);

                            String image = encodeBitmapForFirebaseStorage(userImageBitmap);
                            new SharedPreferenceManager(mContext).setAvatar(image);
                            new DatabaseManager(mContext,"").updateImageAvatar(image);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
                closeChangePhotoOption();
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }

    private byte[] bitmapToByte(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte[] byteArray = baos.toByteArray();
        return byteArray;
    }

    private static Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    private String encodeBitmapForFirebaseStorage(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,80,baos);
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }

    private void closeChangePhotoOption(){
        isChangePhotoOptionOpen = false;
        isBlackBackOpen = false;
        backBlackRelativeLayout.animate().alpha(0f).setDuration(mAnimationTime).start();

        ChangePhotoRelativeLayout.animate().alpha(0.7f).translationY(Utils.dpToPx(200)).setDuration(mAnimationTime).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        backBlackRelativeLayout.setVisibility(View.GONE);
                        backBlackRelativeLayout.setAlpha(0f);

                        ChangePhotoRelativeLayout.setVisibility(View.GONE);
                        ChangePhotoRelativeLayout.setAlpha(0.7f);
                        ChangePhotoRelativeLayout.setTranslationY(Utils.dpToPx(200));

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
    public void onClick(View v) {
        if(!isBlackBackOpen) {
            if (v.equals(setProfilePhotoRelativeLayout)) {
                openChangePhotoOption();
            }
            else if(v.equals(setCountryRelativeLayout)){
                openSelectCountyPart();
            }
            else if(v.equals(setLanguageRelativeLayout)){
                openSelectLanguagePart();
            }
            else if(v.equals(logoutButtonRelativeLayout)){
                openLogoutPart();
            }
            else if(v.equals(viewPostsRelativeLayout)){
                openViewMyPosts();
            }
        }
    }

    @Override
    public void onBackPressed(){
        if(isChangePhotoOptionOpen){
            closeChangePhotoOption();
        }
        else if(isSetLanguagePartOpen){
            closeSelectedLanguagePart();
        }
        else if(isSetCountyPartOpen){
            closeSelectCountyPart();
        }
        else if(isLogoutPartOpen){
            closeLogoutPart();
        }
        else if(isShowingCommentRepliesPart){
            hideCommentReplies();
        }
        else if(isViewPostShowing){
            hideViewPostPart();
        }
        else if(isMyPostsPartOpen){
            closeViewMyPosts();
        }else{
            super.onBackPressed();
        }
    }

    private void showLoadingAnimationForAvatar(){
        final float alpha = 0f;
        final int duration = 600;

        final float alphaR = 0.4f;
        final int durationR = 600;

        if(canAnimateImageLoadingScreens) {
            userProfileImageView.animate().alpha(alpha).setDuration(duration).setInterpolator(new LinearInterpolator())
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            userProfileImageView.animate().alpha(alphaR).setDuration(durationR)
                                    .setInterpolator(new LinearInterpolator()).setListener(new Animator.AnimatorListener() {
                                        @Override
                                        public void onAnimationStart(Animator animator) {

                                        }

                                        @Override
                                        public void onAnimationEnd(Animator animator) {
                                            showLoadingAnimationForAvatar();
                                        }

                                        @Override
                                        public void onAnimationCancel(Animator animator) {

                                        }

                                        @Override
                                        public void onAnimationRepeat(Animator animator) {

                                        }
                                    });
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {

                        }
                    }).start();
        }else{
            userProfileImageView.clearAnimation();
            userProfileImageView.setAlpha(1f);
        }
    }

    private void hideLoadingAnimationForAvatar(){
        canAnimateImageLoadingScreens = false;
    }


    private void openSelectLanguagePart(){
        isSetLanguagePartOpen = true;
        isBlackBackOpen = true;
        backBlackRelativeLayout.setVisibility(View.VISIBLE);
        backBlackRelativeLayout.animate().alpha(0.7f).setDuration(mAnimationTime).start();

        ChangeLanguageRelativeLayout.setVisibility(View.VISIBLE);
        ChangeLanguageRelativeLayout.animate().alpha(1f).translationY(0).setDuration(mAnimationTime).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        backBlackRelativeLayout.setVisibility(View.VISIBLE);
                        backBlackRelativeLayout.setAlpha(0.7f);

                        ChangeLanguageRelativeLayout.setVisibility(View.VISIBLE);
                        ChangeLanguageRelativeLayout.setAlpha(1f);
                        ChangeLanguageRelativeLayout.setTranslationY(0);

                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                }).start();

        if(pickedLanguageOption!=null) newLanguageTextView.setText(pickedLanguageOption.getName());

        setLanguageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(pickedLanguageOption!= null) {
                    new SharedPreferenceManager(mContext).setLanguageInSharedPref(pickedLanguageOption);
                    new DatabaseManager(mContext,"").updatePreferredLanguage(pickedLanguageOption);
                    closeSelectedLanguagePart();
                }else{
                    Toast.makeText(mContext,"Pick Something!",Toast.LENGTH_SHORT).show();
                }
            }
        });

        cancelSetLanguageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeSelectedLanguagePart();
            }
        });

        LocalBroadcastManager.getInstance(mContext).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String languageName = intent.getExtras().getString("language");
                pickedLanguageOption = new Language(languageName);
                newLanguageTextView.setText(languageName);
            }
        },new IntentFilter(Constants.SELECTED_LANGUAGE));

        UserSettingsActivityLanguageItemAdapter UserSettingsActivityLanguageItemAdapter = new UserSettingsActivityLanguageItemAdapter(Utils.loadLanguages(mContext),UserSettingsActivity.this);
        languagesListRecyclerView.setAdapter(UserSettingsActivityLanguageItemAdapter);
        languagesListRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
    }

    private void closeSelectedLanguagePart(){
        isSetCountyPartOpen = false;
        isBlackBackOpen = false;
        backBlackRelativeLayout.animate().alpha(0f).setDuration(mAnimationTime).start();

        ChangeLanguageRelativeLayout.animate().alpha(0.7f).translationY(Utils.dpToPx(350)).setDuration(mAnimationTime)
                .setInterpolator(new LinearOutSlowInInterpolator()).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        backBlackRelativeLayout.setVisibility(View.GONE);
                        backBlackRelativeLayout.setAlpha(0f);

                        ChangeCountyRelativeLayout.setVisibility(View.GONE);
                        ChangeCountyRelativeLayout.setAlpha(0.7f);
                        ChangeCountyRelativeLayout.setTranslationY(Utils.dpToPx(350));

                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                }).start();
    }

    private void openSelectCountyPart(){
        isSetCountyPartOpen = true;
        isBlackBackOpen = true;
        backBlackRelativeLayout.setVisibility(View.VISIBLE);
        backBlackRelativeLayout.animate().alpha(0.7f).setDuration(mAnimationTime).start();

        ChangeCountyRelativeLayout.setVisibility(View.VISIBLE);
        ChangeCountyRelativeLayout.animate().alpha(1f).translationY(0).setDuration(mAnimationTime).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        backBlackRelativeLayout.setVisibility(View.VISIBLE);
                        backBlackRelativeLayout.setAlpha(0.7f);

                        ChangeCountyRelativeLayout.setVisibility(View.VISIBLE);
                        ChangeCountyRelativeLayout.setAlpha(1f);
                        ChangeCountyRelativeLayout.setTranslationY(0);

                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                }).start();

        if(pickedCountyOption!=null) newCountyTextView.setText(pickedCountyOption.getName());

        setCountyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(pickedCountyOption!= null) {
                    new SharedPreferenceManager(mContext).setCountyInSharedPref(pickedCountyOption);
                    new DatabaseManager(mContext,"").updatePreferredCounty(pickedCountyOption);
                    closeSelectCountyPart();
                }else{
                    Toast.makeText(mContext,"Pick Something!",Toast.LENGTH_SHORT).show();
                }
            }
        });

        cancelSetCountyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeSelectCountyPart();
            }
        });

        LocalBroadcastManager.getInstance(mContext).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String CountyName = intent.getExtras().getString("county");
                pickedCountyOption = new County();
                pickedCountyOption.setName(CountyName);
                newCountyTextView.setText(CountyName);
            }
        },new IntentFilter(Constants.SELECTED_COUNTY));

        UserSettingsActivityCountyItemAdapter UserSettingsActivityCountyItemAdapter =
                new UserSettingsActivityCountyItemAdapter(Utils.loadCounties(mContext),UserSettingsActivity.this);
        countyListRecyclerView.setAdapter(UserSettingsActivityCountyItemAdapter);
        countyListRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
    }

    private void closeSelectCountyPart(){
        isSetCountyPartOpen = false;
        isBlackBackOpen = false;
        backBlackRelativeLayout.animate().alpha(0f).setDuration(mAnimationTime).start();

        ChangeCountyRelativeLayout.animate().alpha(0.7f).translationY(Utils.dpToPx(350)).setDuration(mAnimationTime)
                .setInterpolator(new LinearOutSlowInInterpolator()).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                backBlackRelativeLayout.setVisibility(View.GONE);
                backBlackRelativeLayout.setAlpha(0f);

                ChangeCountyRelativeLayout.setVisibility(View.GONE);
                ChangeCountyRelativeLayout.setAlpha(0.7f);
                ChangeCountyRelativeLayout.setTranslationY(Utils.dpToPx(350));

            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        }).start();
    }

    private void openLogoutPart(){
        isLogoutPartOpen = true;
        isBlackBackOpen = true;
        backBlackRelativeLayout.setVisibility(View.VISIBLE);
        backBlackRelativeLayout.animate().alpha(0.7f).setDuration(mAnimationTime).start();

        LogoutRelativeLayout.setVisibility(View.VISIBLE);
        LogoutRelativeLayout.animate().alpha(1f).translationY(0).setDuration(mAnimationTime).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        backBlackRelativeLayout.setVisibility(View.VISIBLE);
                        backBlackRelativeLayout.setAlpha(0.7f);

                        LogoutRelativeLayout.setVisibility(View.VISIBLE);
                        LogoutRelativeLayout.setAlpha(1f);
                        LogoutRelativeLayout.setTranslationY(0);

                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                }).start();

        cancelLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeLogoutPart();
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(UserSettingsActivity.this, Authenticator.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    private void closeLogoutPart(){
        isLogoutPartOpen = false;
        isBlackBackOpen = false;
        backBlackRelativeLayout.animate().alpha(0f).setDuration(mAnimationTime).start();

        LogoutRelativeLayout.animate().alpha(0.7f).translationY(Utils.dpToPx(200)).setDuration(mAnimationTime)
                .setInterpolator(new LinearOutSlowInInterpolator()).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                backBlackRelativeLayout.setVisibility(View.GONE);
                backBlackRelativeLayout.setAlpha(0f);

                LogoutRelativeLayout.setVisibility(View.GONE);
                LogoutRelativeLayout.setAlpha(0.7f);
                LogoutRelativeLayout.setTranslationY(Utils.dpToPx(200));

            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        }).start();
    }


    private void openViewMyPosts(){
        isMyPostsPartOpen = true;
        myPostsRelativelayout.setVisibility(View.VISIBLE);
        myPostsRelativelayout.animate().alpha(1f).translationY(0).setDuration(mAnimationTime).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        myPostsRelativelayout.setVisibility(View.VISIBLE);
                        myPostsRelativelayout.setAlpha(1f);
                        myPostsRelativelayout.setTranslationY(0);

                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                }).start();
        loadMyPosts();
    }

    private void closeViewMyPosts(){
        isMyPostsPartOpen = false;
        myPostsRelativelayout.animate().alpha(0f).translationY(Utils.dpToPx(300)).setDuration(mAnimationTime)
                .setInterpolator(new LinearOutSlowInInterpolator()).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                myPostsRelativelayout.setVisibility(View.GONE);
                myPostsRelativelayout.setAlpha(0f);
                myPostsRelativelayout.setTranslationY(Utils.dpToPx(300));

            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        }).start();
    }

    private void loadMyPosts(){
        showLoadingAnimations();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference myUploadRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_USERS).child(uid);
        myUploadRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.child(Constants.UPLOADED_ANNOUNCEMENTS).exists()){
                    DataSnapshot ann_snaps = dataSnapshot.child(Constants.UPLOADED_ANNOUNCEMENTS);
                    for(DataSnapshot snap_id: ann_snaps.getChildren()){
                        announcements.add(snap_id.getValue(String.class));
                    }
                }
                if(dataSnapshot.child(Constants.UPLOADED_PETITIONS).exists()){
                    DataSnapshot pet_snaps = dataSnapshot.child(Constants.UPLOADED_PETITIONS);
                    for(DataSnapshot snap_id: pet_snaps.getChildren()){
                        petitions.add(snap_id.getValue(String.class));
                    }
                }
                if(dataSnapshot.child(Constants.UPLOADED_POLLS).exists()){
                    DataSnapshot pol_snaps = dataSnapshot.child(Constants.UPLOADED_POLLS);
                    for(DataSnapshot snap_id: pol_snaps.getChildren()){
                        polls.add(snap_id.getValue(String.class));
                    }
                }

                loadPosts();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private List<Post> allLoadedPosts = new ArrayList<>();
    private List<Announcement> allLoadedAnnouncements = new ArrayList<>();
    private boolean hasAnnouncementsLoaded = false;
    private List<Petition> allLoadedPetitions = new ArrayList<>();
    private boolean hasPetitionsLoaded = false;
    private List<Poll> allLoadedPolls = new ArrayList<>();
    private boolean hasPollsLoaded = false;
    private void loadPosts() {
        showLoadingAnimations();
        allLoadedPosts.clear();
        allLoadedAnnouncements.clear();
        allLoadedPetitions.clear();
        allLoadedPolls.clear();
        DatabaseReference announcementRef = FirebaseDatabase.getInstance().getReference(Constants.ANNOUNCEMENTS);
        announcementRef.limitToFirst(Constants.POST_LOADING_LIMIT).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for (DataSnapshot snap: dataSnapshot.getChildren()) {
                        Announcement announcement = snap.getValue(Announcement.class);
                        Post p = new Post();
                        p.setAnnouncement(announcement);

                        if(announcements.contains(announcement.getAnnouncementId())) {
                            allLoadedPosts.add(p);
                            allLoadedAnnouncements.add(announcement);
                        }
                    }
                }
                hasAnnouncementsLoaded = true;

                if(hasAnnouncementsLoaded && hasPetitionsLoaded && hasPollsLoaded){
                    sortPosts();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference petitionsRef = FirebaseDatabase.getInstance().getReference(Constants.PETITIONS);
        petitionsRef.limitToFirst(Constants.POST_LOADING_LIMIT).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot snap: dataSnapshot.getChildren()){
                        Petition petition = snap.getValue(Petition.class);
                        petition.getSignatures().clear();

                        for(DataSnapshot signatureSnap:dataSnapshot.child(Constants.PETITION_SIGNATURES).getChildren()){
                            PetitionSignature s = signatureSnap.getValue(PetitionSignature.class);
                            petition.addSignature(s);
                        }

                        Post p = new Post();
                        p.setPetition(petition);

                        if(petitions.contains(petition.getPetitionId())) {
                            allLoadedPosts.add(p);
                            allLoadedPetitions.add(petition);
                        }
                    }
                }
                hasPetitionsLoaded = true;

                if(hasAnnouncementsLoaded && hasPetitionsLoaded && hasPollsLoaded){
                    sortPosts();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference pollsRef = FirebaseDatabase.getInstance().getReference(Constants.POLLS);
        pollsRef.limitToFirst(Constants.POST_LOADING_LIMIT).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot snap: dataSnapshot.getChildren()){
                        Poll poll = snap.getValue(Poll.class);
                        poll.getPollOptions().clear();
                        for(DataSnapshot pollVoteSnap: snap.child(Constants.POLL_VOTES).getChildren()){
                            PollOption option = pollVoteSnap.getValue(PollOption.class);
                            poll.getPollOptions().add(option);
                        }
                        Post p = new Post();
                        p.setPoll(poll);

                        if(polls.contains(poll.getPollId())) {
                            allLoadedPosts.add(p);
                            allLoadedPolls.add(poll);
                        }
                    }
                }
                hasPollsLoaded = true;

                if(hasAnnouncementsLoaded && hasPetitionsLoaded && hasPollsLoaded){
                    sortPosts();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void sortPosts() {
        hasPollsLoaded = false;
        hasPetitionsLoaded = false;
        hasAnnouncementsLoaded = false;

        HashMap<Long,Post> postsHashMap = new LinkedHashMap<>();
        for(Post p: allLoadedPosts){
            if(p.getPostType().equals(Constants.ANNOUNCEMENTS)){
                //its a announcement
                Long time = p.getAnnouncement().getAnnouncementCreationTime();
                postsHashMap.put(time,p);

            }else if(p.getPostType().equals(Constants.PETITIONS)){
                //its a petition
                Long time = p.getPetition().getPetitionCreationTime();
                postsHashMap.put(time,p);

            }else{
                //its a poll
                Long time = p.getPoll().getPollCreationTime();
                postsHashMap.put(time,p);
            }

        }

        List<Long> timesToSort = new ArrayList<>(postsHashMap.keySet());
        Collections.sort(timesToSort);

        allLoadedPosts.clear();
        for(Long time:timesToSort) {
            allLoadedPosts.add(postsHashMap.get(time));
        }

        loadPostsIntoRecyclerView();
    }

    private void loadPostsIntoRecyclerView() {
        Log.e("MainActivity","Number of items: "+allLoadedPosts.size());
        UserSettingsPostItemAdapter = new UserSettingsPostItemAdapter(allLoadedPosts, UserSettingsActivity.this);
        loadedPostsRecyclerView.setAdapter(UserSettingsPostItemAdapter);
        loadedPostsRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));

        UserSettingsPostItemAdapter.setOnBottomReachedListener(new com.bry.raia.Adapters.UserSettingsPostItemAdapter.OnBottomReachedListener() {
            @Override
            public void onBottomReached(int position) {
                //when user has scrolled to bottom of list
//                loadMorePostItems();
            }
        });

        hideLoadingAnimations();

        LocalBroadcastManager.getInstance(mContext).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                showViewPostPart();
            }
        },new IntentFilter(Constants.SHOW_MY_VIEW_POST));

    }

    private void showViewPostPart(){
        isViewPostShowing = true;
        viewPostRelativeLayout.setVisibility(View.VISIBLE);
        viewPostRelativeLayout.animate().alpha(1f).translationY(0).setDuration(mAnimationTime).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        viewPostRelativeLayout.setAlpha(1f);
                        viewPostRelativeLayout.setTranslationY(0);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });

        Post mPost = Variables.postToBeViewed;
        if(mPost.getPostType().equals(Constants.ANNOUNCEMENTS)) {
            Announcement announcement = mPost.getAnnouncement();
            postTypeTextView.setText(getString(R.string.announcement));
            userNameTextView.setText(String.format("By %s", announcement.getUploaderUsername()));
            countyTextViewAnnouncement.setText(String.format("To %s", announcement.getCounty().getName()));
            postTitleTextView.setText(announcement.getAnnouncementTitle());

            announcementCardView.setVisibility(View.VISIBLE);
            petitionUiLinearLayout.setVisibility(View.GONE);
            pollRelativeLayout.setVisibility(View.GONE);
            announcementImageView.setImageBitmap(Variables.image);
            announcementPostImageViewBack.setImageBitmap(Variables.imageBack);

            AnnouncementTitleTextView.setText(announcement.getAnnouncementTitle());
        }else if(mPost.getPostType().equals(Constants.PETITIONS)) {
            final Petition petition = mPost.getPetition();
            postTypeTextView.setText(getString(R.string.petition));
            userNameTextView.setText(String.format("By %s", petition.getUploaderUsername()));
            countyTextViewPetition.setText(String.format("To %s", petition.getCounty().getName()));
            postTitleTextView.setText(petition.getPetitionTitle());
            PetitionTitleTextView.setText(petition.getPetitionTitle());

            petitionUiLinearLayout.setVisibility(View.VISIBLE);
            announcementCardView.setVisibility(View.GONE);
            pollRelativeLayout.setVisibility(View.GONE);
            petitionImageView.setImageBitmap(Variables.image);
            petitionImageViewBack.setImageBitmap(Variables.imageBack);

            numberSignedTextView.setText(String.format("%d signed", petition.getSignatures().size()));

            long percentage = (petition.getSignatures().size()/petition.getPetitionSignatureTarget())*100;
            petitionPercentageView.setProgress((int)percentage);

            if(new SharedPreferenceManager(mContext).hasUserSignedPetition(petition)){
                signTextView.setAlpha(0.4f);
            }

            signTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!new SharedPreferenceManager(mContext).hasUserSignedPetition(petition)) {
                        updatePetitionDataInSharedPreferencesAndFirebase(petition);
                        signTextView.setAlpha(0.4f);
                    }
                }
            });

        }else{
            //its a poll
            final Poll poll = mPost.getPoll();
            pollRelativeLayout.setVisibility(View.VISIBLE);
            announcementCardView.setVisibility(View.GONE);
            petitionUiLinearLayout.setVisibility(View.GONE);
            postTypeTextView.setText(getString(R.string.poll));
            userNameTextView.setText(String.format("By %s", poll.getUploaderUsername()));
            countyTextViewPoll.setText(String.format("To %s", poll.getCounty().getName()));
            postTitleTextView.setText(poll.getPollTitle());
            PollTitleTextView.setText(poll.getPollTitle());


            if(new SharedPreferenceManager(this).hasUserVotedInPoll(poll)){
                PollOption po = new SharedPreferenceManager(this).getWhichPollOptionSelected(poll);
                isPresetingCheckButons = true;
                if(poll.getPollOptions().get(0).getOptionId().equals(po.getOptionId())){
                    pollOption1CheckBox.setChecked(true);
                    pollOption1CheckBox.setEnabled(false);
                    option2CheckBox.setEnabled(false);
                    option3CheckBox.setEnabled(false);
                    option4CheckBox.setEnabled(false);
                }else if(poll.getPollOptions().size()>1 && poll.getPollOptions().get(1).getOptionId().equals(po.getOptionId())){
                    option2CheckBox.setChecked(true);
                    pollOption1CheckBox.setEnabled(false);
                    option2CheckBox.setEnabled(false);
                    option3CheckBox.setEnabled(false);
                    option4CheckBox.setEnabled(false);
                }else if(poll.getPollOptions().size()>2 && poll.getPollOptions().get(2).getOptionId().equals(po.getOptionId())){
                    option3CheckBox.setChecked(true);
                    pollOption1CheckBox.setEnabled(false);
                    option2CheckBox.setEnabled(false);
                    option3CheckBox.setEnabled(false);
                    option4CheckBox.setEnabled(false);
                }else if(poll.getPollOptions().size()>3 && poll.getPollOptions().get(3).getOptionId().equals(po.getOptionId())){
                    option4CheckBox.setChecked(true);
                    pollOption1CheckBox.setEnabled(false);
                    option2CheckBox.setEnabled(false);
                    option3CheckBox.setEnabled(false);
                    option4CheckBox.setEnabled(false);
                }
                isPresetingCheckButons = false;
            }

            pollOption1CheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    pollOption1CheckBox.setEnabled(false);
                    option2CheckBox.setEnabled(false);
                    option3CheckBox.setEnabled(false);
                    option4CheckBox.setEnabled(false);

                    if(!isPresetingCheckButons) {
                        poll.getPollOptions().get(0).addVote();
                        updatePollDataInSharedPrefAndFirebase(poll, poll.getPollOptions().get(0));
                        setPollData(poll, true);
                    }
                }
            });

            option2CheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    pollOption1CheckBox.setEnabled(false);
                    option2CheckBox.setEnabled(false);
                    option3CheckBox.setEnabled(false);
                    option4CheckBox.setEnabled(false);

                    if(!isPresetingCheckButons) {
                        poll.getPollOptions().get(1).addVote();
                        updatePollDataInSharedPrefAndFirebase(poll, poll.getPollOptions().get(1));
                        setPollData(poll, true);
                    }
                }
            });

            option3CheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    pollOption1CheckBox.setEnabled(false);
                    option2CheckBox.setEnabled(false);
                    option3CheckBox.setEnabled(false);
                    option4CheckBox.setEnabled(false);

                    if(!isPresetingCheckButons) {
                        poll.getPollOptions().get(2).addVote();
                        updatePollDataInSharedPrefAndFirebase(poll, poll.getPollOptions().get(2));
                        setPollData(poll, true);
                    }
                }
            });

            option4CheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    pollOption1CheckBox.setEnabled(false);
                    option2CheckBox.setEnabled(false);
                    option3CheckBox.setEnabled(false);
                    option4CheckBox.setEnabled(false);

                    if(!isPresetingCheckButons) {
                        poll.getPollOptions().get(3).addVote();
                        updatePollDataInSharedPrefAndFirebase(poll, poll.getPollOptions().get(3));
                        setPollData(poll, true);
                    }
                }
            });
            setPollData(poll,false);
        }

        sendCommentImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addComment();
            }
        });
        loadComments();

    }

    private void loadComments() {
        startCommentLoadingAnimations();
        showCommentsLoadingAnimations();
        String postId;
        Post mPost = Variables.postToBeViewed;
        if(mPost.getPostType().equals(Constants.ANNOUNCEMENTS)){
            postId = mPost.getAnnouncement().getAnnouncementId();
        }else if(mPost.getPostType().equals(Constants.PETITIONS)){
            postId = mPost.getPetition().getPetitionId();
        }else {
            postId = mPost.getPoll().getPollId();
        }

        DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference(Constants.COMMENTS).child(postId);
        commentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Comment> allComments = new ArrayList<>();
                if(dataSnapshot.exists()){
                    for(DataSnapshot commentSnap:dataSnapshot.getChildren()){
                        Comment comment = commentSnap.getValue(Comment.class);
                        for(DataSnapshot replySnap:commentSnap.child(Constants.REPLIES).getChildren()){
                            Comment reply = replySnap.getValue(Comment.class);
                            comment.addReply(reply);
                        }
                        allComments.add(comment);
                    }
                }

                loadCommentsIntoRecyclerView(allComments);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void addComment() {
        String postId;
        Post mPost = Variables.postToBeViewed;
        if(mPost.getPostType().equals(Constants.ANNOUNCEMENTS)){
            postId = mPost.getAnnouncement().getAnnouncementId();
        }else if(mPost.getPostType().equals(Constants.PETITIONS)){
            postId = mPost.getPetition().getPetitionId();
        }else{
            postId = mPost.getPoll().getPollId();
        }

        String commentText = addCommentEditText.getText().toString().trim();
        if(!commentText.equals("")){
            addCommentEditText.setText("");

            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Comment comment = new Comment(commentText,uid,new SharedPreferenceManager(mContext).loadNameInSharedPref());

            DatabaseReference replyRef = FirebaseDatabase.getInstance().getReference(Constants.COMMENTS).child(postId);
            DatabaseReference pushRef = replyRef.push();
            String commentId = pushRef.getKey();
            comment.setCommentId(commentId);

            pushRef.setValue(comment);
            addNewCommentToRecyclerView(comment);

        }else{
            addCommentEditText.setError(getResources().getString(R.string.say_something));
        }
    }

    private void loadCommentsIntoRecyclerView(final List<Comment> loadedComments) {
        Post mPost = Variables.postToBeViewed;
        if(loadedComments.isEmpty()){
            noCommentsTextView.setVisibility(View.VISIBLE);
            commentsRecyclerView.setVisibility(View.GONE);
        }else{
            vpActivityCommentAdapter = new ViewPostActivityCommentItemAdapter( UserSettingsActivity.this,loadedComments, true,mPost);
            commentsRecyclerView.setAdapter(vpActivityCommentAdapter);
            commentsRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
            allComments = loadedComments;
            noCommentsTextView.setVisibility(View.GONE);

            LocalBroadcastManager.getInstance(mContext).registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    int pos = intent.getIntExtra(Constants.COMMENT_NO,0);
                    showCommentReplies(allComments.get(pos));
                }
            },new IntentFilter(Constants.SHOW_COMMENT_REPLIES));
        }
        hideCommentsLoadingAnimations();
    }

    private void addNewCommentToRecyclerView(Comment comment){
        allComments.add(comment);
//        vpActivityCommentAdapter.addComment(comment);
////        vpActivityCommentAdapter.notifyItemInserted(allComments.size()-1);
//        vpActivityCommentAdapter.notifyDataSetChanged();

        loadCommentsIntoRecyclerView(allComments);
    }


    private void startCommentLoadingAnimations(){
        final float alpha = 0.3f;
        final int duration = 2000;

        final float alphaR = 1f;
        final int durationR = 800;

        if(canAnimateLoadingScreens) {
            loadingCommentsContainerLinearLayout.animate().alpha(alpha).setDuration(duration).setInterpolator(new LinearInterpolator())
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            loadingCommentsContainerLinearLayout.animate().alpha(alphaR).setDuration(durationR).setInterpolator(new LinearInterpolator())
                                    .setListener(new Animator.AnimatorListener() {
                                        @Override
                                        public void onAnimationStart(Animator animator) {

                                        }

                                        @Override
                                        public void onAnimationEnd(Animator animator) {
                                            startCommentLoadingAnimations();
                                        }

                                        @Override
                                        public void onAnimationCancel(Animator animator) {

                                        }

                                        @Override
                                        public void onAnimationRepeat(Animator animator) {

                                        }
                                    });
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {

                        }
                    });
        }
    }

    private void hideCommentsLoadingAnimations(){
        canAnimateLoadingScreens = false;
        loadingContainerLinearLayout.setVisibility(View.GONE);
        commentsRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showCommentsLoadingAnimations(){
        canAnimateLoadingScreens = true;
        loadingContainerLinearLayout.setVisibility(View.VISIBLE);
        commentsRecyclerView.setVisibility(View.GONE);
        startLoadingAnimations();
    }


    private void showCommentReplies(final Comment comment){
        viewCommentLinearLayout.setVisibility(View.VISIBLE);
        isShowingCommentRepliesPart = true;
        final Post mPost = Variables.postToBeViewed;
        viewCommentLinearLayout.animate().alpha(1f).translationY(0).setDuration(mAnimationTime).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        viewCommentLinearLayout.setAlpha(1f);
                        viewCommentLinearLayout.setTranslationY(0);
                        viewCommentLinearLayout.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                }).start();

        userNameRepliesTextView.setText("By "+comment.getCommenterName());
        commentTimeTextView.setText(getTimeInMills(comment.getCommentTime()));

        commentBodyTextView.setText(comment.getCommentText());
        loadCommentRepliesIntoRecyclerView(mPost,comment);
        replyButtonTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String reply = addReplyEditText.getText().toString().trim();
                if(reply.equals("")){
                    addReplyEditText.setError(getResources().getString(R.string.say_something));
                }else{
                    String postId;

                    if(mPost.getPostType().equals(Constants.ANNOUNCEMENTS)){
                        postId = mPost.getAnnouncement().getAnnouncementId();
                    }else if(mPost.getPostType().equals(Constants.PETITIONS)){
                        postId = mPost.getPetition().getPetitionId();
                    }else {
                        postId = mPost.getPoll().getPollId();
                    }

                    addReplyEditText.setText("");
                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    Comment newreply = new Comment(reply,uid,new SharedPreferenceManager(mContext).loadNameInSharedPref());
                    newreply.setCommentTime(Calendar.getInstance().getTimeInMillis());

                    DatabaseReference replyRef = FirebaseDatabase.getInstance().getReference(Constants.COMMENTS).child(postId).child(comment.getCommentId())
                            .child(Constants.REPLIES);
                    DatabaseReference pushRef = replyRef.push();
                    String commentId = pushRef.getKey();
                    newreply.setCommentId(commentId);

                    pushRef.setValue(newreply);
                    comment.addReply(newreply);

                    loadCommentRepliesIntoRecyclerView(mPost,comment);
                    loadCommentsIntoRecyclerView(allComments);
                }
            }
        });
    }

    private void loadCommentRepliesIntoRecyclerView(final Post mPost, Comment comment){
        if(comment.getReplies().isEmpty()){
            noRepliesMessage.setVisibility(View.VISIBLE);
        }else{
            noRepliesMessage.setVisibility(View.GONE);
            final ViewPostActivityCommentItemAdapter vpActivityReplyAdapter = new ViewPostActivityCommentItemAdapter(UserSettingsActivity.this,
                    comment.getReplies(), false, mPost);
            repliesRecyclerView.setAdapter(vpActivityReplyAdapter);
            repliesRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        }
    }

    private String getTimeInMills(long commentTimeMills){
        long currentTimeInMills = Calendar.getInstance().getTimeInMillis();
        long howLongAgoInMills = (currentTimeInMills-commentTimeMills);

        if(howLongAgoInMills< (24*60*60*1000)){
            if(howLongAgoInMills< (60*60*1000)){
                if(howLongAgoInMills< (60*1000)){
                    return "Just now.";
                }else{
                    //comment is more than one minute old
                    long minCount = howLongAgoInMills/(60*1000);
                    return minCount+" min.";
                }
            }else{
                //comment is more than an hour ago
                long hrsCount = howLongAgoInMills/(60*60*1000);
                return hrsCount+" hrs.";
            }
        }else{
            //comment is more than a day old
            long daysCount = howLongAgoInMills/(24*60*60*1000);
            return (daysCount+" days.");
        }
    }

    private void hideCommentReplies(){
        isShowingCommentRepliesPart = false;
        viewCommentLinearLayout.animate().alpha(0f).translationY(Utils.dpToPx(170)).setDuration(mAnimationTime).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        viewCommentLinearLayout.setAlpha(0f);
                        viewCommentLinearLayout.setTranslationY(Utils.dpToPx(170));
                        viewCommentLinearLayout.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                }).start();

    }

    private void setPollData(Poll poll, boolean isShowingResult){
        isShowingResult = new SharedPreferenceManager(mContext).hasUserVotedInPoll(poll);

        int totalVotes = 0;
        int barWidth = Utils.dpToPx(Constants.POST_CARD_VIEW_WIDTH-20);
        for(PollOption op:poll.getPollOptions()){
            totalVotes+=op.getVotes();
        }
        option1LinearLayout.setVisibility(View.GONE);
        option2LinearLayout.setVisibility(View.GONE);
        option3LinearLayout.setVisibility(View.GONE);
        option4LinearLayout.setVisibility(View.GONE);
        //option 1
        pollOption1CheckBox.setText(poll.getPollOptions().get(0).getOptionText());
        option1LinearLayout.setVisibility(View.VISIBLE);

        if(totalVotes==0) pollVoteCountTextView.setText(getResources().getString(R.string.zero_votes));
        else if(totalVotes==1)pollVoteCountTextView.setText(getResources().getString(R.string.one_vote));
        else pollVoteCountTextView.setText(totalVotes+getResources().getString(R.string.votes));

        int div = totalVotes;
        if(div==0) div=1;
        int option1Percentage = (int)((poll.getPollOptions().get(0).getVotes()/(div))*100);
        if(isShowingResult){
            option1PercentageTextView.setText(option1Percentage+"%");
            option1PercentageBarView.setProgress(option1Percentage);
        }else{
            option1PercentageTextView.setText("0%");
            option1PercentageBarView.setProgress(option1Percentage);
        }

        if(poll.getPollOptions().size()>1) {
            //option 2
            option2CheckBox.setText(poll.getPollOptions().get(1).getOptionText());
            option2LinearLayout.setVisibility(View.VISIBLE);
            int option2Percentage = (int)(poll.getPollOptions().get(1).getVotes() / div) * 100;
            if(isShowingResult){
                option2PercentageTextView.setText(option2Percentage + "%");
                option2PercentageBar.setProgress(option2Percentage);
            }else{
                option2PercentageTextView.setText("0%");
                option2PercentageBar.setProgress(option2Percentage);
            }

        }

        if(poll.getPollOptions().size()>2) {
            //option 3
            option3CheckBox.setText(poll.getPollOptions().get(2).getOptionText());
            option3LinearLayout.setVisibility(View.VISIBLE);
            int option3Percentage = (int)(poll.getPollOptions().get(2).getVotes() / div) * 100;
            if(isShowingResult){
                option3PercentageTextView.setText(option3Percentage + "%");
                option3PercentageView.setProgress(option3Percentage);
            }else{
                option3PercentageTextView.setText("0%");
                option3PercentageView.setProgress(option3Percentage);
            }

        }

        if(poll.getPollOptions().size()>3) {
            //option 4
            option4LinearLayout.setVisibility(View.VISIBLE);
            option4CheckBox.setText(poll.getPollOptions().get(3).getOptionText());
            int option4Percentage = (int)(poll.getPollOptions().get(3).getVotes() / div) * 100;
            if(isShowingResult){
                option4PercentageTextView.setText(option4Percentage + "%");
                option4PercentageBarView.setProgress(option4Percentage);
            }else{
                option4PercentageTextView.setText("0%");
                option4PercentageBarView.setProgress(option4Percentage);
            }

        }
    }

    private void updatePollDataInSharedPrefAndFirebase(Poll poll, PollOption po){
        new DatabaseManager(this,"").recordPollVote(poll,po).updatePollOptionData(poll,po);
        new SharedPreferenceManager(this).recordPollVote(poll.getPollId(),po);
    }

    private void updatePetitionDataInSharedPreferencesAndFirebase(Petition p){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        String name = new SharedPreferenceManager(this).loadNameInSharedPref();
        long timestamp = Calendar.getInstance().getTimeInMillis();
        PetitionSignature signature = new PetitionSignature(uid,name,email,timestamp);

        new DatabaseManager(this,"").recordPetitionSignature(p).updatePetitionSignatureData(p,signature);

        new SharedPreferenceManager(this).recordPetition(p.getPetitionId());
    }

    private void hideViewPostPart(){
        isViewPostShowing = false;
        viewPostRelativeLayout.animate().alpha(0f).translationY(Utils.dpToPx(180)).setDuration(mAnimationTime).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        viewPostRelativeLayout.setAlpha(0f);
                        viewPostRelativeLayout.setTranslationY(Utils.dpToPx(180));
                        viewPostRelativeLayout.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });

        allComments.clear();
    }


    private void startLoadingAnimations(){
        final float alpha = 0.3f;
        final int duration = 800;

        final float alphaR = 1f;
        final int durationR = 800;

        if(canAnimateLoadingScreens) {
            loadingContainerLinearLayout.animate().alpha(alpha).setDuration(duration).setInterpolator(new LinearInterpolator())
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            loadingContainerLinearLayout.animate().alpha(alphaR).setDuration(durationR).setInterpolator(new LinearInterpolator())
                                    .setListener(new Animator.AnimatorListener() {
                                        @Override
                                        public void onAnimationStart(Animator animator) {

                                        }

                                        @Override
                                        public void onAnimationEnd(Animator animator) {
                                            startLoadingAnimations();
                                        }

                                        @Override
                                        public void onAnimationCancel(Animator animator) {

                                        }

                                        @Override
                                        public void onAnimationRepeat(Animator animator) {

                                        }
                                    });
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {

                        }
                    });
        }
    }

    private void hideLoadingAnimations(){
        canAnimateLoadingScreens = false;
        loadingContainerLinearLayout.setVisibility(View.GONE);
        loadedPostsRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showLoadingAnimations(){
        canAnimateLoadingScreens = true;
        loadingContainerLinearLayout.setVisibility(View.VISIBLE);
        loadedPostsRecyclerView.setVisibility(View.GONE);
        startLoadingAnimations();
    }
}
