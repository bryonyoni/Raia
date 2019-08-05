package com.bry.raia.Activities;

import android.animation.Animator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bry.raia.Constants;
import com.bry.raia.Models.Announcement;
import com.bry.raia.Models.Petition;
import com.bry.raia.Models.Poll;
import com.bry.raia.Models.PollOption;
import com.bry.raia.R;
import com.bry.raia.Services.DatabaseManager;
import com.bry.raia.Services.Utils;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class UploadPostActivity extends AppCompatActivity implements View.OnClickListener{
    private final String TAG = UploadPostActivity.class.getSimpleName();
    private Context mContext;
    @Bind(R.id.newPostLinearLayout) LinearLayout newPostLinearLayout;
    @Bind(R.id.uploadActivityCoordinatorLayout) CoordinatorLayout uploadActivityCoordinatorLayout;
    @Bind(R.id.previousActivityImageView) ImageView previousActivityImageView;

    private boolean isAtSelectUploadType = false;
    @Bind(R.id.uploadSelectorLinearLayout) LinearLayout uploadSelectorLinearLayout;
    @Bind(R.id.openAnnouncementLinearLayout) LinearLayout openAnnouncementLinearLayout;
    @Bind(R.id.openAnnouncementImageView) ImageView openAnnouncementImageView;
    @Bind(R.id.openPollLinearLayout) LinearLayout openPollLinearLayout;
    @Bind(R.id.openPollImageView) ImageView openPollImageView;
    @Bind(R.id.openPetitionLinearLayout) LinearLayout openPetitionLinearLayout;
    @Bind(R.id.openPetitionImageView) ImageView openPetitionImageView;

    private boolean isAtAnnouncementForm = false;
    @Bind(R.id.announcementFormLinearLayout) LinearLayout announcementFormLinearLayout;
    @Bind(R.id.announcementPreviousImageView) ImageView announcementPreviousImageView;
    @Bind(R.id.announcementDetailsEditText) EditText announcementDetailsEditText;
    private String announcementDetailsText = "";
    @Bind(R.id.openAnnouncementCameraImageView) ImageView openAnnouncementCameraImageView;
    @Bind(R.id.openAnnouncementGalleryImageView) ImageView openAnnouncementGalleryImageView;
    private Bitmap selectedAnnouncementImage = null;
    @Bind(R.id.selectedAnnouncementCardView) CardView selectedAnnouncementCardView;
    @Bind(R.id.selectedAnnouncementImageView) ImageView selectedAnnouncementImageView;
    private Bitmap blurredBackImage = null;
    @Bind(R.id.selectedAnnouncementImageViewBack) ImageView selectedAnnouncementImageViewBack;
    @Bind(R.id.announcementBlurProgressBar) ProgressBar announcementBlurProgressBar;
    @Bind(R.id.uploadAnnouncementTextView) TextView uploadAnnouncementTextView;

    private boolean isAtPollForm = false;
    @Bind(R.id.pollFormLinearLayout) LinearLayout pollFormLinearLayout;
    @Bind(R.id.pollPreviousImageView) ImageView pollPreviousImageView;
    @Bind(R.id.uploadPollTextView) TextView uploadPollTextView;
    @Bind(R.id.pollQueryEditText) EditText pollQueryEditText;
    private String pollQueryText = "";
    @Bind(R.id.addPollOptionImageView) ImageView addPollOptionImageView;
    @Bind(R.id.pollOptionEditText) EditText pollOptionEditText;
    private List<PollOption> pollOptions = new ArrayList<>();
    @Bind(R.id.addedOption1LinearLayout) LinearLayout addedOption1LinearLayout;
    @Bind(R.id.deleteAddedOption1ImageView) ImageView deleteAddedOption1ImageView;
    @Bind(R.id.addedOption1TextView) TextView addedOption1TextView;
    @Bind(R.id.addedOption2LinearLayout) LinearLayout addedOption2LinearLayout;
    @Bind(R.id.deleteAddedOption2ImageView) ImageView deleteAddedOption2ImageView;
    @Bind(R.id.addedOption2TextView) TextView addedOption2TextView;
    @Bind(R.id.addedOption3LinearLayout) LinearLayout addedOption3LinearLayout;
    @Bind(R.id.deleteAddedOption3ImageView) ImageView deleteAddedOption3ImageView;
    @Bind(R.id.addedOption3TextView) TextView addedOption3TextView;
    @Bind(R.id.addedOption4LinearLayout) LinearLayout addedOption4LinearLayout;
    @Bind(R.id.deleteAddedOption4ImageView) ImageView deleteAddedOption4ImageView;
    @Bind(R.id.addedOption4TextView) TextView addedOption4TextView;

    private boolean isAtFinishedUpPart = false;
    @Bind(R.id.finishedUpLinearLayout) LinearLayout finishedUpLinearLayout;
    @Bind(R.id.finishUpButton) Button finishUpButton;

    private boolean isAtPetitionForm = false;
    @Bind(R.id.petitionFormLinearLayout) LinearLayout petitionFormLinearLayout;
    @Bind(R.id.petitionPreviousImageView) ImageView petitionPreviousImageView;
    @Bind(R.id.uploadPetitionTextView) TextView uploadPetitionTextView;
    @Bind(R.id.petitionDetailsEditText) EditText petitionDetailsEditText;
    @Bind(R.id.petitionSignatureTargetEditText) EditText petitionSignatureTargetEditText;
    private String petitionDetailsText = "";
    private long targetPetitionSignatureNumber = 100;
    @Bind(R.id.openPetitionCameraImageView) ImageView openPetitionCameraImageView;
    @Bind(R.id.openPetitionGalleryImageView) ImageView openPetitionGalleryImageView;
    private Bitmap selectedPetitionImage = null;
    @Bind(R.id.selectedPetitionCardView) CardView selectedPetitionCardView;
    @Bind(R.id.selectedPetitionImageView) ImageView selectedPetitionImageView;
    private Bitmap blurredPetitionBackImage = null;
    @Bind(R.id.selectedPetitionImageViewBack) ImageView selectedPetitionImageViewBack;
    @Bind(R.id.petitionBlurProgressBar) ProgressBar petitionBlurProgressBar;


    @Bind(R.id.progressBarRelativeLayout) RelativeLayout progressBarRelativeLayout;
    private boolean isShowingSpinner = false;

    private int mAnimationDuration = 300;
    private int transitionOutTranslation =Utils.dpToPx(-200);
    private int transitionInTranslation = Utils.dpToPx(400);

    private final int PICK_IMAGE_REQUEST = 1012;
    private final int REQUEST_IMAGE_CAPTURE = 1;
    private Uri mFilepath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_post);
        ButterKnife.bind(this);
        mContext = this.getApplicationContext();

        previousActivityImageView.setOnClickListener(this);

        loadUploadSelector();
    }

    private void loadUploadSelector() {
        isAtSelectUploadType = true;
        uploadSelectorLinearLayout.setVisibility(View.VISIBLE);
        uploadSelectorLinearLayout.animate().translationX(0).alpha(1f).setDuration(mAnimationDuration).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        uploadSelectorLinearLayout.setTranslationX(0);
                        uploadSelectorLinearLayout.setAlpha(1f);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();

        openAnnouncementLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAtSelectUploadType = false;
                uploadSelectorLinearLayout.animate().translationX(transitionOutTranslation).alpha(0f).setDuration(mAnimationDuration).setInterpolator(new LinearOutSlowInInterpolator())
                        .setListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                uploadSelectorLinearLayout.setTranslationX(transitionOutTranslation);
                                uploadSelectorLinearLayout.setAlpha(0f);
                                uploadSelectorLinearLayout.setVisibility(View.GONE);
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        }).start();
                openAnnouncementFormPart();
            }
        });
        openAnnouncementImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAnnouncementLinearLayout.performClick();
            }
        });


        openPollLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAtSelectUploadType = false;
                uploadSelectorLinearLayout.animate().translationX(transitionOutTranslation).alpha(0f).setDuration(mAnimationDuration).setInterpolator(new LinearOutSlowInInterpolator())
                        .setListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                uploadSelectorLinearLayout.setTranslationX(transitionOutTranslation);
                                uploadSelectorLinearLayout.setAlpha(0f);
                                uploadSelectorLinearLayout.setVisibility(View.GONE);
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        }).start();
                openPollFormPart();
            }
        });
        openPollImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPollLinearLayout.performClick();
            }
        });


        openPetitionLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAtSelectUploadType = false;
                uploadSelectorLinearLayout.animate().translationX(transitionOutTranslation).alpha(0f).setDuration(mAnimationDuration).setInterpolator(new LinearOutSlowInInterpolator())
                        .setListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                uploadSelectorLinearLayout.setTranslationX(transitionOutTranslation);
                                uploadSelectorLinearLayout.setAlpha(0f);
                                uploadSelectorLinearLayout.setVisibility(View.GONE);
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        }).start();
                openPetitionPart();
            }
        });
        openPetitionImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPetitionLinearLayout.performClick();
            }
        });
    }

    @Override
    public void onClick(View v) {
        if(v.equals(previousActivityImageView)){
            finish();
        }
    }


    private void openAnnouncementFormPart(){
        isAtAnnouncementForm = true;
        announcementFormLinearLayout.setVisibility(View.VISIBLE);
        announcementFormLinearLayout.animate().translationX(0).alpha(1f).setDuration(mAnimationDuration).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        announcementFormLinearLayout.setTranslationX(0);
                        announcementFormLinearLayout.setAlpha(1f);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();

        announcementPreviousImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isShowingSpinner) {
                    announcementFormLinearLayout.animate().translationX(transitionInTranslation).alpha(0f).setDuration(mAnimationDuration).setInterpolator(new LinearOutSlowInInterpolator())
                            .setListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {

                                }

                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    announcementFormLinearLayout.setTranslationX(transitionInTranslation);
                                    announcementFormLinearLayout.setAlpha(0f);
                                    announcementFormLinearLayout.setVisibility(View.GONE);
                                }

                                @Override
                                public void onAnimationCancel(Animator animation) {

                                }

                                @Override
                                public void onAnimationRepeat(Animator animation) {

                                }
                            }).start();
                    isAtAnnouncementForm = false;
                    loadUploadSelector();
                }
            }
        });

        openAnnouncementCameraImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isShowingSpinner)pickPhotoFromCamera();
            }
        });

        openAnnouncementGalleryImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isShowingSpinner)pickPhotoFromStorage();
            }
        });

        uploadAnnouncementTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isShowingSpinner){
                    announcementDetailsText = announcementDetailsEditText.getText().toString().trim();
                    if(announcementDetailsText.equals("")) announcementDetailsEditText.setError(getResources().getString(R.string.youll_need_to_add_this));
                    else if(selectedAnnouncementImage == null) {
                        Snackbar.make(uploadActivityCoordinatorLayout, getResources().getString(R.string.youll_need_to_add_an_image), Snackbar.LENGTH_LONG).show();
                    }else{
                        showLoadingScreens();
                        String imageString = encodeBitmapForFirebaseStorage(selectedAnnouncementImage);
                        Announcement announcement = new Announcement(announcementDetailsText,imageString, Calendar.getInstance().getTimeInMillis());
                        String SUCCESSFUL_INTENT = "SUCCESSFUL_INTENT";
                        new DatabaseManager(mContext,SUCCESSFUL_INTENT).uploadAnnouncement(announcement);
                        LocalBroadcastManager.getInstance(mContext).registerReceiver(new BroadcastReceiver() {
                            @Override
                            public void onReceive(Context context, Intent intent) {
                                hideLoadingScreens();
                                isAtAnnouncementForm = false;
                                announcementFormLinearLayout.animate().translationX(transitionOutTranslation).alpha(0f).setDuration(mAnimationDuration)
                                        .setInterpolator(new LinearOutSlowInInterpolator()).setListener(new Animator.AnimatorListener() {
                                            @Override
                                            public void onAnimationStart(Animator animation) {

                                            }

                                            @Override
                                            public void onAnimationEnd(Animator animation) {
                                                announcementFormLinearLayout.setTranslationX(transitionOutTranslation);
                                                announcementFormLinearLayout.setAlpha(0f);
                                                announcementFormLinearLayout.setVisibility(View.GONE);
                                            }

                                            @Override
                                            public void onAnimationCancel(Animator animation) {

                                            }

                                            @Override
                                            public void onAnimationRepeat(Animator animation) {

                                            }
                                        }).start();
                                openFinishedUpPart();
                            }
                        },new IntentFilter(SUCCESSFUL_INTENT));

                    }

                }
            }
        });
    }

    private void openPetitionPart(){
        isAtPetitionForm = true;
        petitionFormLinearLayout.setVisibility(View.VISIBLE);
        petitionFormLinearLayout.animate().translationX(0).alpha(1f).setDuration(mAnimationDuration).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        petitionFormLinearLayout.setTranslationX(0);
                        petitionFormLinearLayout.setAlpha(1f);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();

        petitionPreviousImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isShowingSpinner) {
                    petitionFormLinearLayout.animate().translationX(transitionInTranslation).alpha(0f).setDuration(mAnimationDuration).setInterpolator(new LinearOutSlowInInterpolator())
                            .setListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {

                                }

                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    petitionFormLinearLayout.setTranslationX(transitionInTranslation);
                                    petitionFormLinearLayout.setAlpha(0f);
                                    petitionFormLinearLayout.setVisibility(View.GONE);
                                }

                                @Override
                                public void onAnimationCancel(Animator animation) {

                                }

                                @Override
                                public void onAnimationRepeat(Animator animation) {

                                }
                            }).start();
                    isAtPetitionForm = false;
                    loadUploadSelector();
                }
            }
        });

        uploadPetitionTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                petitionDetailsText = petitionDetailsEditText.getText().toString().trim();
                if(!petitionSignatureTargetEditText.getText().toString().trim().equals(""))targetPetitionSignatureNumber = Long.parseLong(petitionSignatureTargetEditText.getText().toString().trim());
                if(petitionDetailsText.equals(""))petitionDetailsEditText.setError(getResources().getString(R.string.youll_need_to_add_this));
                else if(selectedPetitionImage == null) {
                    Snackbar.make(uploadActivityCoordinatorLayout, getResources().getString(R.string.youll_need_to_add_an_image), Snackbar.LENGTH_LONG).show();
                }
                else if(petitionSignatureTargetEditText.getText().toString().trim().equals("")){
                    petitionSignatureTargetEditText.setError(getResources().getString(R.string.youll_need_to_add_this));
                }else{
                    showLoadingScreens();
                    String imageString = encodeBitmapForFirebaseStorage(selectedPetitionImage);
                    String SUCCESSFUL_INTENT = "SUCCESSFUL_INTENT";

                    Petition petition = new Petition(petitionDetailsText,imageString,Calendar.getInstance().getTimeInMillis());
                    petition.setPetitionSignatureTarget(targetPetitionSignatureNumber);
                    new DatabaseManager(mContext,SUCCESSFUL_INTENT).uploadPetition(petition);

                    LocalBroadcastManager.getInstance(mContext).registerReceiver(new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            hideLoadingScreens();
                            isAtPetitionForm = false;
                            petitionFormLinearLayout.animate().translationX(transitionOutTranslation).alpha(0f).setDuration(mAnimationDuration)
                                    .setInterpolator(new LinearOutSlowInInterpolator()).setListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {

                                }

                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    petitionFormLinearLayout.setTranslationX(transitionOutTranslation);
                                    petitionFormLinearLayout.setAlpha(0f);
                                    petitionFormLinearLayout.setVisibility(View.GONE);
                                }

                                @Override
                                public void onAnimationCancel(Animator animation) {

                                }

                                @Override
                                public void onAnimationRepeat(Animator animation) {

                                }
                            }).start();
                            openFinishedUpPart();
                        }
                    },new IntentFilter(SUCCESSFUL_INTENT));
                }
            }
        });

        openPetitionCameraImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isShowingSpinner) pickPhotoFromCamera();
            }
        });

        openPetitionGalleryImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isShowingSpinner) pickPhotoFromStorage();
            }
        });
    }

    private void openPollFormPart(){
        isAtPollForm = true;
        pollFormLinearLayout.setVisibility(View.VISIBLE);

        pollFormLinearLayout.animate().translationX(0).alpha(1f).setDuration(mAnimationDuration).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        pollFormLinearLayout.setTranslationX(0);
                        pollFormLinearLayout.setAlpha(1f);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();

        pollPreviousImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isShowingSpinner) {
                    pollFormLinearLayout.animate().translationX(transitionInTranslation).alpha(0f).setDuration(mAnimationDuration).setInterpolator(new LinearOutSlowInInterpolator())
                            .setListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {

                                }

                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    pollFormLinearLayout.setTranslationX(transitionInTranslation);
                                    pollFormLinearLayout.setAlpha(0f);
                                    pollFormLinearLayout.setVisibility(View.GONE);
                                }

                                @Override
                                public void onAnimationCancel(Animator animation) {

                                }

                                @Override
                                public void onAnimationRepeat(Animator animation) {

                                }
                            }).start();
                    isAtPollForm = false;
                    loadUploadSelector();
                }
            }
        });

        uploadPollTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pollQueryText = pollQueryEditText.getText().toString().trim();
                if(pollQueryText.equals(""))pollOptionEditText.setError(getResources().getString(R.string.youll_need_to_add_this));
                else if(pollOptions.size()<2){
                    Snackbar.make(uploadActivityCoordinatorLayout, getResources().getString(R.string.youll_need_to_add_an_option), Snackbar.LENGTH_LONG).show();
                }else{
                    Poll poll = new Poll(pollQueryText,pollOptions);
                    String SUCCESSFUL_INTENT = "SUCCESSFUL_INTENT";
                    new DatabaseManager(mContext,SUCCESSFUL_INTENT).uploadPoll(poll);
                    LocalBroadcastManager.getInstance(mContext).registerReceiver(new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            hideLoadingScreens();
                            isAtPollForm = false;
                            pollFormLinearLayout.animate().translationX(transitionOutTranslation).alpha(0f).setDuration(mAnimationDuration)
                                    .setInterpolator(new LinearOutSlowInInterpolator()).setListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {

                                }

                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    pollFormLinearLayout.setTranslationX(transitionOutTranslation);
                                    pollFormLinearLayout.setAlpha(0f);
                                    pollFormLinearLayout.setVisibility(View.GONE);
                                }

                                @Override
                                public void onAnimationCancel(Animator animation) {

                                }

                                @Override
                                public void onAnimationRepeat(Animator animation) {

                                }
                            }).start();
                            openFinishedUpPart();
                        }
                    },new IntentFilter(SUCCESSFUL_INTENT));
                }
            }
        });

        addPollOptionImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pollText = pollOptionEditText.getText().toString().trim();
                if(pollText.equals(""))pollOptionEditText.setError(getResources().getString(R.string.youll_need_to_add_this));
                else if(pollOptions.size()==Constants.MAX_POLL_AMOUNT) pollOptionEditText.setError(getResources().getString(R.string.maximum_of_4_poll_options_is_allowed));
                else{
                    PollOption pollOption = new PollOption(pollText);
                    pollOptions.add(pollOption);
                    setPollOptions();
                    pollOptionEditText.setText("");
                }
            }
        });
    }

    private void setPollOptions(){
        addedOption1LinearLayout.setVisibility(View.GONE);
        addedOption2LinearLayout.setVisibility(View.GONE);
        addedOption3LinearLayout.setVisibility(View.GONE);
        addedOption4LinearLayout.setVisibility(View.GONE);

        for(int i=0;i<pollOptions.size();i++){
            if(i==0){
                addedOption1LinearLayout.setVisibility(View.VISIBLE);
                addedOption1TextView.setText(pollOptions.get(i).getOptionText());
                pollOptions.get(i).setOptionId(""+i);
                final int finalI = i;
                deleteAddedOption1ImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pollOptions.remove(finalI);
                        setPollOptions();
                    }
                });
            }else if(i==1){
                addedOption2LinearLayout.setVisibility(View.VISIBLE);
                addedOption2TextView.setText(pollOptions.get(i).getOptionText());
                pollOptions.get(i).setOptionId(""+i);
                final int finalI = i;
                deleteAddedOption2ImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pollOptions.remove(finalI);
                        setPollOptions();
                    }
                });
            }else if(i==2){
                addedOption3LinearLayout.setVisibility(View.VISIBLE);
                addedOption3TextView.setText(pollOptions.get(i).getOptionText());
                pollOptions.get(i).setOptionId(""+i);
                final int finalI = i;
                deleteAddedOption3ImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pollOptions.remove(finalI);
                        setPollOptions();
                    }
                });
            }else if(i==3){
                addedOption4LinearLayout.setVisibility(View.VISIBLE);
                addedOption4TextView.setText(pollOptions.get(i).getOptionText());
                pollOptions.get(i).setOptionId(""+i);
                final int finalI = i;
                deleteAddedOption4ImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pollOptions.remove(finalI);
                        setPollOptions();
                    }
                });
            }
        }
    }

    private void openFinishedUpPart(){
        isAtFinishedUpPart = true;
        finishedUpLinearLayout.setVisibility(View.VISIBLE);
        finishedUpLinearLayout.animate().translationX(0).alpha(1f).setDuration(mAnimationDuration).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        finishedUpLinearLayout.setTranslationX(0);
                        finishedUpLinearLayout.setAlpha(1f);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();

        finishUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAtFinishedUpPart = false;
                finish();
            }
        });
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
    protected void onActivityResult(int requestCode, int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            if (data.getData() != null) {
                mFilepath = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mFilepath);
                    Bitmap bm = getResizedBitmap(bitmap,1500);

                    if(isAtAnnouncementForm){
                        selectedAnnouncementImage = bm;
                        selectedAnnouncementCardView.setVisibility(View.VISIBLE);
                        selectedAnnouncementImageView.setImageBitmap(bm);

                        BlurAnnouncementBackTask op = new BlurAnnouncementBackTask();
                        announcementBlurProgressBar.setVisibility(View.VISIBLE);
                        op.execute("");
                        selectedAnnouncementImageViewBack.setImageBitmap(null);
                    }else if(isAtPetitionForm){
                        selectedPetitionImage = bm;
                        selectedPetitionCardView.setVisibility(View.VISIBLE);
                        selectedPetitionImageView.setImageBitmap(bm);

                        BlurPetitionBackTask op = new BlurPetitionBackTask();
                        petitionBlurProgressBar.setVisibility(View.VISIBLE);
                        op.execute("");
                        selectedPetitionImageViewBack.setImageBitmap(null);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try{
                Bundle extras = data.getExtras();
                Bitmap bitmap = (Bitmap) extras.get("data");
                Bitmap bm = getResizedBitmap(bitmap,1500);

                if(isAtAnnouncementForm){
                    selectedAnnouncementImage = bm;
                    selectedAnnouncementCardView.setVisibility(View.VISIBLE);
                    selectedAnnouncementImageView.setImageBitmap(bm);

                    BlurAnnouncementBackTask op = new BlurAnnouncementBackTask();
                    announcementBlurProgressBar.setVisibility(View.VISIBLE);
                    op.execute("");
                }else if(isAtPetitionForm){
                    selectedPetitionImage = bm;
                    selectedPetitionCardView.setVisibility(View.VISIBLE);
                    selectedPetitionImageView.setImageBitmap(bm);

                    BlurPetitionBackTask op = new BlurPetitionBackTask();
                    petitionBlurProgressBar.setVisibility(View.VISIBLE);
                    op.execute("");
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

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

    private class BlurAnnouncementBackTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            blurredBackImage = fastBlur(selectedAnnouncementImage,0.7f,27);
            return "executed";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            selectedAnnouncementImageViewBack.setImageBitmap(blurredBackImage);
            announcementBlurProgressBar.setVisibility(View.GONE);
        }

    }

    private class BlurPetitionBackTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            blurredPetitionBackImage = fastBlur(selectedPetitionImage,0.7f,27);
            return "executed";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            selectedPetitionImageViewBack.setImageBitmap(blurredPetitionBackImage);
            petitionBlurProgressBar.setVisibility(View.GONE);
        }

    }

    private Bitmap fastBlur(Bitmap sentBitmap, float scale, int radius) {

        int width = Math.round(sentBitmap.getWidth() * scale);
        int height = Math.round(sentBitmap.getHeight() * scale);
        sentBitmap = Bitmap.createScaledBitmap(sentBitmap, width, height, false);

        Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
//        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = ( 0xff000000 & pix[yi] ) | ( dv[rsum] << 16 ) | ( dv[gsum] << 8 ) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

//        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.setPixels(pix, 0, w, 0, 0, w, h);

        return (bitmap);
    }

    private void showLoadingScreens(){
        progressBarRelativeLayout.setVisibility(View.VISIBLE);
        newPostLinearLayout.setAlpha(Constants.LOADING_SCREEN_BACKGROUND_ALPHA);
        isShowingSpinner = true;
    }

    private void hideLoadingScreens(){
        isShowingSpinner = false;
        progressBarRelativeLayout.setVisibility(View.GONE);
        newPostLinearLayout.setAlpha(1f);
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        //should check null because in airplane mode it will be null
        return (netInfo != null && netInfo.isConnected());
    }

    private String encodeBitmapForFirebaseStorage(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,80,baos);
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }


}
