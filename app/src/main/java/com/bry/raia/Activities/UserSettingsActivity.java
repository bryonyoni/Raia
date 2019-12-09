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
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bry.raia.Adapters.UserSettingsActivityCountyItemAdapter;
import com.bry.raia.Adapters.UserSettingsActivityLanguageItemAdapter;
import com.bry.raia.Constants;
import com.bry.raia.Models.County;
import com.bry.raia.Models.Language;
import com.bry.raia.R;
import com.bry.raia.Services.DatabaseManager;
import com.bry.raia.Services.SharedPreferenceManager;
import com.bry.raia.Services.Utils;
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
import java.util.Calendar;

import butterknife.Bind;
import butterknife.ButterKnife;

public class UserSettingsActivity extends AppCompatActivity implements View.OnClickListener{
    private Context mContext;
    private int mAnimationTime = 300;
    @Bind(R.id.userImageView) ImageView userImageView;
    @Bind(R.id.userNameTextView) TextView userNameTextView;
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

            Glide.with(mContext).load(bitmapToByte(userImageBitmap)).asBitmap().centerCrop().into(new BitmapImageViewTarget(userImageView) {
                @Override
                protected void setResource(Bitmap resource) {
                    try{
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(mContext.getResources(),resource);
//                                Bitmap.createScaledBitmap(resource,100,100,false));
                        circularBitmapDrawable.setCircular(true);
                        userImageView.setImageDrawable(circularBitmapDrawable);
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

                        Glide.with(mContext).load(bitmapToByte(userImageBitmap)).asBitmap().centerCrop().into(new BitmapImageViewTarget(userImageView) {
                            @Override
                            protected void setResource(Bitmap resource) {
                                try{
                                    RoundedBitmapDrawable circularBitmapDrawable =
                                            RoundedBitmapDrawableFactory.create(mContext.getResources(),resource);
//                                Bitmap.createScaledBitmap(resource,100,100,false));
                                    circularBitmapDrawable.setCircular(true);
                                    userImageView.setImageDrawable(circularBitmapDrawable);
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

        userNameTextView.setText(new SharedPreferenceManager(mContext).loadNameInSharedPref());
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
        userImageView.setImageDrawable(getDrawable(R.drawable.grey_back));
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
                    Glide.with(mContext).load(bitmapToByte(userImageBitmap)).asBitmap().centerCrop().into(new BitmapImageViewTarget(userImageView) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            try{
                                RoundedBitmapDrawable circularBitmapDrawable =
                                        RoundedBitmapDrawableFactory.create(mContext.getResources(),resource);
//                                Bitmap.createScaledBitmap(resource,100,100,false));
                                circularBitmapDrawable.setCircular(true);
                                userImageView.setImageDrawable(circularBitmapDrawable);
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

                Glide.with(mContext).load(bitmapToByte(userImageBitmap)).asBitmap().centerCrop().into(new BitmapImageViewTarget(userImageView) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        try{
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(mContext.getResources(),resource);
//                                Bitmap.createScaledBitmap(resource,100,100,false));
                            circularBitmapDrawable.setCircular(true);
                            userImageView.setImageDrawable(circularBitmapDrawable);

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
        }
    }

    @Override
    public void onBackPressed(){
        if(isChangePhotoOptionOpen){
            closeChangePhotoOption();
        }else if(isSetLanguagePartOpen){
            closeSelectedLanguagePart();
        }else if(isSetCountyPartOpen){
            closeSelectCountyPart();
        }else if(isLogoutPartOpen){
            closeLogoutPart();
        }
        else{
            super.onBackPressed();
        }
    }

    private void showLoadingAnimationForAvatar(){
        final float alpha = 0f;
        final int duration = 600;

        final float alphaR = 0.4f;
        final int durationR = 600;

        if(canAnimateImageLoadingScreens) {
            userImageView.animate().alpha(alpha).setDuration(duration).setInterpolator(new LinearInterpolator())
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            userImageView.animate().alpha(alphaR).setDuration(durationR)
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
            userImageView.clearAnimation();
            userImageView.setAlpha(1f);
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
}
