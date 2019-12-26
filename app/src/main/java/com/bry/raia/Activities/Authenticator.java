package com.bry.raia.Activities;

import android.animation.Animator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.internal.Util;

public class Authenticator extends AppCompatActivity {
    private final String TAG = Authenticator.class.getSimpleName();
    private Context mContext;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private int SPLASH_DISPLAY_LENGTH = 3021;
    private int mAnimationDuration = 300;
    private int transitionOutTranslation = -200;
    private int transitionInTranslation = 400;

    /* splash screen and login views*/
    private boolean isShowingLoginLayout = false;
    @Bind(R.id.splashScreenRelativeLayout) RelativeLayout splashScreenRelativeLayout;
    @Bind(R.id.loginRelativeLayout) RelativeLayout loginRelativeLayout;
    @Bind(R.id.loginBackImageView) ImageView loginBackImageView;
    @Bind(R.id.signInTitle) TextView signInTitle;
    @Bind(R.id.loginEmailLinearLayout) LinearLayout loginEmailLinearLayout;
    @Bind(R.id.loginEmailEditText) EditText loginEmailEditText;
    private boolean isShowingLoginEmailView = false;
    private String mEnteredLoginEmailString = "";
    @Bind(R.id.LoginPasswordLinearLayout) LinearLayout LoginPasswordLinearLayout;
    @Bind(R.id.LoginPasswordEditText) EditText LoginPasswordEditText;
    private boolean isShowingLoginPasswordView = false;
    private String mEnteredLoginPasswordString = "";
    @Bind(R.id.loginNextButton) Button loginNextButton;
    @Bind(R.id.signUpLink) TextView signUpLink;
    @Bind(R.id.viewLoginPasswordImageView) ImageView viewLoginPasswordImageView;

    /* progressbar layout part*/
    @Bind(R.id.progressBarRelativeLayout) RelativeLayout progressBarRelativeLayout;
    @Bind(R.id.loginLinearLayout) LinearLayout loginLinearLayout;
    private boolean isShowingSpinner = false;

    /* signup views*/
    private boolean isShowingSignUpLayout = false;
    @Bind(R.id.signUpLinearLayout) LinearLayout signUpLinearLayout;
    @Bind(R.id.signUpRelativeLayout) RelativeLayout signUpRelativeLayout;
    @Bind(R.id.backImageView) ImageView backImageView;
    @Bind(R.id.signUpTitle) TextView signUpTitle;
    @Bind(R.id.nameLinearLayout) LinearLayout nameLinearLayout;
    @Bind(R.id.nameEditText) EditText nameEditText;
    private boolean isShowingNameView = true;
    private String mEnteredNameString = "";
    @Bind(R.id.emailLinearLayout) LinearLayout emailLinearLayout;
    @Bind(R.id.emailEditText) EditText emailEditText;
    private boolean isShowingEmailView = false;
    private String mEnteredEmailString = "";
    @Bind(R.id.passwordLinearLayout) LinearLayout passwordLinearLayout;
    @Bind(R.id.passwordEditText) EditText passwordEditText;
    private boolean isShowingPasswordView = false;
    private String mEnteredPasswordString = "";
    @Bind(R.id.retypePasswordLinearLayout) LinearLayout retypePasswordLinearLayout;
    @Bind(R.id.passwordRetypeEditText) EditText passwordRetypeEditText;
    private boolean isShowingRetypePasswordView = true;
    private String mEnteredRetypedPasswordString = "";
    @Bind(R.id.nextButton) Button nextButton;
    @Bind(R.id.signInLink) TextView signInLink;
    @Bind(R.id.viewPasswordImageView) ImageView viewPasswordImageView;
    private boolean isPasswordVisible = false;
    private List<String> easyPasswords = new ArrayList<>(Arrays.asList
            ("123456", "987654","qwerty","asdfgh","zxcvbn","123456abc","123456qwe","987654qwe", "987654asd",""));

    private boolean isSigningUpForFirstTime = false;
    @Bind(R.id.selectCountyRelativeLayout) RelativeLayout selectCountyRelativeLayout;
    @Bind(R.id.countyListRecyclerView) RecyclerView countyListRecyclerView;
    @Bind(R.id.newCountyTextView) TextView newCountyTextView;
    @Bind(R.id.setCountyButton) Button setCountyButton;
    private County pickedCountyOption;

    @Bind(R.id.selectLanguageRelativeLayout) RelativeLayout selectLanguageRelativeLayout;
    @Bind(R.id.languagesListRecyclerView) RecyclerView languagesListRecyclerView;
    @Bind(R.id.newLanguageTextView) TextView newLanguageTextView;
    @Bind(R.id.setLanguageButton) Button setLanguageButton;
    private Language pickedLanguageOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authenticator);
        ButterKnife.bind(this);
        mContext = this.getApplicationContext();

        if(new SharedPreferenceManager(mContext).isFirstTimeLaunch()) {
            loadSelectLanguagePart();
        }else{
            loadSplashScreenLogic();
        }
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth){
                final FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    new DatabaseManager(mContext,"").loadUserDataFromFirebase();
                    Log.d(TAG,"User was found, opening main activity");
                    loadMainActivity();
                }
            }
        };
    }

    private void loadSplashScreenLogic() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(mAuth.getCurrentUser()!=null){
                    splashScreenRelativeLayout.animate().alpha(0f).setDuration(mAnimationDuration).setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            splashScreenRelativeLayout.setAlpha(0f);
                            loadMainActivity();
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    }).start();
                }else{
                    loginRelativeLayout.setVisibility(View.VISIBLE);
                    loginRelativeLayout.animate().alpha(1).translationY(0).setInterpolator(new LinearOutSlowInInterpolator()).setStartDelay(100)
                            .setDuration(mAnimationDuration).start();

                    splashScreenRelativeLayout.animate().scaleX(0.8f).scaleY(0.8f).translationY(0).setInterpolator(new LinearOutSlowInInterpolator())
                            .setDuration(mAnimationDuration).setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            loginRelativeLayout.setAlpha(1);
                            loginRelativeLayout.setTranslationY(0);

                            splashScreenRelativeLayout.setScaleX(0.8f);
                            splashScreenRelativeLayout.setScaleY(0.8f);
                            splashScreenRelativeLayout.setTranslationY(0);

                            loadLoginLogic();
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    }).start();
                }
            }
            },SPLASH_DISPLAY_LENGTH);
    }

    private void loadSelectLanguagePart(){
        selectLanguageRelativeLayout.setVisibility(View.VISIBLE);
        selectLanguageRelativeLayout.animate().alpha(1f).setDuration(mAnimationDuration).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                selectLanguageRelativeLayout.setAlpha(1f);
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
//                    new DatabaseManager(mContext,"").updatePreferredLanguage(pickedLanguageOption);

                    selectLanguageRelativeLayout.animate().alpha(0f).setDuration(mAnimationDuration).setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            selectLanguageRelativeLayout.setAlpha(0f);
                            selectLanguageRelativeLayout.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {

                        }
                    }).start();
                    setLocale(getLanguageCode(pickedLanguageOption));
//                    loadSplashScreenLogic();

                }else{
                    Toast.makeText(mContext,"Pick Something!",Toast.LENGTH_SHORT).show();
                }
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

        UserSettingsActivityLanguageItemAdapter UserSettingsActivityLanguageItemAdapter =
                new UserSettingsActivityLanguageItemAdapter(Utils.loadLanguages(mContext),Authenticator.this);
        languagesListRecyclerView.setAdapter(UserSettingsActivityLanguageItemAdapter);
        languagesListRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
    }

    private String getLanguageCode(Language language){
        if(language.getName().equals("English")){
            return "en";
        }else if(language.getName().equals("Swahili")){
            return "swa";
        }else return "en";
    }

    private void loadLoginPart() {
        isShowingLoginLayout = true;
        isShowingSignUpLayout = false;
        loginRelativeLayout.setVisibility(View.VISIBLE);
        signUpRelativeLayout.animate().alpha(0f).setDuration(mAnimationDuration-100).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        signUpRelativeLayout.setVisibility(View.GONE);
                        signUpRelativeLayout.setAlpha(0f);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();

        loginRelativeLayout.animate().translationY(0).alpha(1f).setDuration(mAnimationDuration).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        loginRelativeLayout.setTranslationY(0);
                        loginRelativeLayout.setAlpha(1f);

                        loadLoginLogic();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();
    }

    private void loadLoginLogic() {
        loginBackImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isShowingSpinner) onLoginBackPressed();
            }
        });

        signUpLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isShowingSpinner) loadSignUpPart();
            }
        });

        setUpEmailInputView();
    }

    private void setUpEmailInputView() {
        isShowingLoginEmailView = true;
        if(!mEnteredLoginEmailString.equals("")) loginEmailEditText.setText(mEnteredLoginEmailString);
        loginEmailLinearLayout.setVisibility(View.VISIBLE);
        loginEmailLinearLayout.animate().translationX(0).alpha(1f).setInterpolator(new LinearOutSlowInInterpolator())
                .setDuration(mAnimationDuration).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                hideBackButtonForLogin();
                loginEmailLinearLayout.setTranslationX(0);
                loginEmailLinearLayout.setAlpha(1f);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).start();

        loginNextButton.setText(getResources().getString(R.string.next));
        loginNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEnteredLoginEmailString = loginEmailEditText.getText().toString().trim();
                if(mEnteredLoginEmailString.equals("")) loginEmailEditText.setError("You need to type your email");
                else if(!isLoginValidEmail(mEnteredLoginEmailString)) loginEmailEditText.setError("That's not a real email");
                else{
                    isShowingLoginEmailView = false;
                    loginEmailLinearLayout.animate().translationX(Utils.dpToPx(transitionOutTranslation)).alpha(0f).setInterpolator(new LinearOutSlowInInterpolator())
                            .setDuration(mAnimationDuration).setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            loginEmailLinearLayout.setTranslationX(Utils.dpToPx(transitionOutTranslation));
                            loginEmailLinearLayout.setAlpha(0f);
                            loginEmailLinearLayout.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    }).start();

                    setUpPasswordInputView();
                }
            }
        });
        viewLoginPasswordImageView.animate().scaleY(0f).scaleX(0f).setDuration(mAnimationDuration).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                viewLoginPasswordImageView.setVisibility(View.INVISIBLE);
                viewLoginPasswordImageView.setScaleX(0f);
                viewLoginPasswordImageView.setScaleY(0f);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).start();
    }

    private void setUpPasswordInputView() {
        isShowingLoginPasswordView = true;
        if(!mEnteredLoginPasswordString.equals("")) LoginPasswordEditText.setText(mEnteredLoginPasswordString);
        LoginPasswordLinearLayout.setVisibility(View.VISIBLE);

        LoginPasswordLinearLayout.animate().translationX(0).alpha(1f).setInterpolator(new LinearOutSlowInInterpolator())
                .setDuration(mAnimationDuration).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                showBackButtonForLogIn();
                LoginPasswordLinearLayout.setTranslationX(0);
                LoginPasswordLinearLayout.setAlpha(1f);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).start();

        loginNextButton.setText(getResources().getString(R.string.log_in));
        loginNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEnteredLoginPasswordString = LoginPasswordEditText.getText().toString().trim();
                if(mEnteredLoginPasswordString.equals("")) LoginPasswordEditText.setError("We need a password.");
                else{
                    loginUser();
                }
            }
        });

        viewLoginPasswordImageView.setVisibility(View.VISIBLE);
        viewLoginPasswordImageView.animate().scaleY(1f).scaleX(1f).setDuration(mAnimationDuration).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                viewLoginPasswordImageView.setVisibility(View.VISIBLE);
                viewLoginPasswordImageView.setScaleX(1f);
                viewLoginPasswordImageView.setScaleY(1f);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).start();
        viewLoginPasswordImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makePasswordReadable();
            }
        });
    }

    private void loginUser(){
        if(!isOnline()){
            Snackbar.make(findViewById(R.id.authenticatorCoordinatorLayout), R.string.no_internet_connection,
                    Snackbar.LENGTH_LONG).show();
        }else{
            isSigningUpForFirstTime = false;
            showLoadingScreen();
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            mAuth.signInWithEmailAndPassword(mEnteredLoginEmailString, mEnteredLoginPasswordString)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d(TAG,"signInWithEmail:onComplete"+task.isSuccessful());
                            if(!task.isSuccessful()){
                                Log.w(TAG,"SignInWithEmail",task.getException());
                                Snackbar.make(findViewById(R.id.authenticatorCoordinatorLayout), getResources().getString(R.string.failed_sign_in), Snackbar.LENGTH_LONG).show();
                                hideLoadingScreens();
                            }
                        }
                    });
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        //should check null because in airplane mode it will be null
        return (netInfo != null && netInfo.isConnected());
    }





    private void loadSignUpPart() {
        isShowingLoginLayout = false;
        isShowingSignUpLayout = true;
        signUpRelativeLayout.setAlpha(0f);
        signUpRelativeLayout.setVisibility(View.VISIBLE);
        signUpRelativeLayout.animate().alpha(1f).setDuration(mAnimationDuration).setStartDelay(200).setInterpolator(new LinearInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
//                        signUpRelativeLayout.setAlpha(1f);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();

        loginRelativeLayout.animate().translationY(Utils.dpToPx(200)).alpha(0).setDuration(mAnimationDuration).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        loginRelativeLayout.setTranslationY(Utils.dpToPx(200));
                        loginRelativeLayout.setAlpha(0);
                        loginRelativeLayout.setVisibility(View.GONE);
                        loadSignUpLogic();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();
    } //loads the ui for the sign up part

    private void loadSignUpLogic() {
        backImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isShowingSpinner) onSignUpBackPressed();
            }
        });

        signInLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isShowingSpinner) loadLoginPart();
            }
        });

        setUpNameInputView();
    } // loads the logic for the sign up part



    private void showBackButtonForSignUp(){
        backImageView.setVisibility(View.VISIBLE);
        signUpTitle.animate().translationX(Utils.dpToPx(30)).setInterpolator(new LinearOutSlowInInterpolator()).setDuration(mAnimationDuration).start();
        backImageView.animate().setInterpolator(new LinearOutSlowInInterpolator()).alpha(1f).setDuration(mAnimationDuration)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        backImageView.setAlpha(1f);
                        signUpTitle.setTranslationX(Utils.dpToPx(30));
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();
    }

    private void hideBackButtonForSignUp(){
        signUpTitle.animate().translationX(0).setInterpolator(new LinearOutSlowInInterpolator()).setDuration(mAnimationDuration).start();
        backImageView.animate().setInterpolator(new LinearOutSlowInInterpolator()).alpha(0f).setDuration(mAnimationDuration)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        backImageView.setAlpha(0f);
                        signUpTitle.setTranslationX(0);
                        backImageView.setVisibility(View.INVISIBLE);

                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();
    }



    private void showBackButtonForLogIn(){
        loginBackImageView.setVisibility(View.VISIBLE);
        signInTitle.animate().translationX(Utils.dpToPx(30)).setInterpolator(new LinearOutSlowInInterpolator()).setDuration(mAnimationDuration).start();
        loginBackImageView.animate().setInterpolator(new LinearOutSlowInInterpolator()).alpha(1f).setDuration(mAnimationDuration)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        loginBackImageView.setAlpha(1f);
                        signInTitle.setTranslationX(Utils.dpToPx(30));
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();
    }

    private void hideBackButtonForLogin(){
        signInTitle.animate().translationX(0).setInterpolator(new LinearOutSlowInInterpolator()).setDuration(mAnimationDuration).start();
        loginBackImageView.animate().setInterpolator(new LinearOutSlowInInterpolator()).alpha(0f).setDuration(mAnimationDuration)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        loginBackImageView.setAlpha(0f);
                        signInTitle.setTranslationX(0);
                        loginBackImageView.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();
    }

    private void onSignUpBackPressed() {
       if(isShowingEmailView) {
           isShowingEmailView = false;
           emailLinearLayout.animate().translationX(Utils.dpToPx(transitionInTranslation)).alpha(0f).setInterpolator(new LinearOutSlowInInterpolator())
                   .setDuration(mAnimationDuration).setListener(new Animator.AnimatorListener() {
               @Override
               public void onAnimationStart(Animator animation) {

               }

               @Override
               public void onAnimationEnd(Animator animation) {
                   emailLinearLayout.setTranslationX(Utils.dpToPx(transitionInTranslation));
                   emailLinearLayout.setAlpha(0f);
                   emailLinearLayout.setVisibility(View.GONE);
               }

               @Override
               public void onAnimationCancel(Animator animation) {

               }

               @Override
               public void onAnimationRepeat(Animator animation) {

               }
           }).start();
           setUpNameInputView();
       }else if(isShowingPasswordView){
           isShowingPasswordView = false;
           passwordLinearLayout.animate().translationX(Utils.dpToPx(transitionInTranslation)).alpha(0f).setInterpolator(new LinearOutSlowInInterpolator())
                   .setDuration(mAnimationDuration).setListener(new Animator.AnimatorListener() {
               @Override
               public void onAnimationStart(Animator animation) {

               }

               @Override
               public void onAnimationEnd(Animator animation) {
                   passwordLinearLayout.setTranslationX(Utils.dpToPx(transitionInTranslation));
                   passwordLinearLayout.setAlpha(0f);
                   passwordLinearLayout.setVisibility(View.GONE);
               }

               @Override
               public void onAnimationCancel(Animator animation) {

               }

               @Override
               public void onAnimationRepeat(Animator animation) {

               }
           }).start();
           setUpEmailInputView();
       }else if(isShowingRetypePasswordView){
           isShowingRetypePasswordView = false;
           retypePasswordLinearLayout.animate().translationX(Utils.dpToPx(transitionInTranslation)).alpha(0f).setInterpolator(new LinearOutSlowInInterpolator())
                   .setDuration(mAnimationDuration).setListener(new Animator.AnimatorListener() {
               @Override
               public void onAnimationStart(Animator animation) {

               }

               @Override
               public void onAnimationEnd(Animator animation) {
                   retypePasswordLinearLayout.setTranslationX(Utils.dpToPx(transitionInTranslation));
                   retypePasswordLinearLayout.setAlpha(0f);
                   retypePasswordLinearLayout.setVisibility(View.GONE);
               }

               @Override
               public void onAnimationCancel(Animator animation) {

               }

               @Override
               public void onAnimationRepeat(Animator animation) {

               }
           }).start();
           setUpSignUpPasswordInputView();
       }
    }

    private void onLoginBackPressed() {
        if(isShowingLoginPasswordView){
            isShowingLoginPasswordView = false;
            LoginPasswordLinearLayout.animate().translationX(Utils.dpToPx(transitionInTranslation)).alpha(0f).setInterpolator(new LinearOutSlowInInterpolator())
                    .setDuration(mAnimationDuration).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    LoginPasswordLinearLayout.setTranslationX(Utils.dpToPx(transitionInTranslation));
                    LoginPasswordLinearLayout.setAlpha(0f);
                    LoginPasswordLinearLayout.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            }).start();
            setUpEmailInputView();
        }
    }

    private void showLoadingScreen(){
        isShowingSpinner = true;
        progressBarRelativeLayout.setVisibility(View.VISIBLE);
        loginLinearLayout.setAlpha(Constants.LOADING_SCREEN_BACKGROUND_ALPHA);
        signUpLinearLayout.setAlpha(Constants.LOADING_SCREEN_BACKGROUND_ALPHA);
//        LoginPasswordEditText.setFocusable(false);
        LoginPasswordEditText.setEnabled(false);
//        passwordRetypeEditText.setFocusable(false);
        passwordRetypeEditText.setEnabled(false);
        nextButton.setClickable(false);
        viewPasswordImageView.setClickable(false);
        viewLoginPasswordImageView.setClickable(false);
    }

    private void hideLoadingScreens(){
        isShowingSpinner = false;
        progressBarRelativeLayout.setVisibility(View.GONE);
        loginLinearLayout.setAlpha(1f);
        signUpLinearLayout.setAlpha(1f);
//        LoginPasswordEditText.setFocusable(true);
        LoginPasswordEditText.setEnabled(true);
//        passwordRetypeEditText.setFocusable(true);
        passwordRetypeEditText.setEnabled(true);
        nextButton.setClickable(true);
        viewPasswordImageView.setClickable(true);
        viewLoginPasswordImageView.setClickable(true);
    }


    private void setUpNameInputView() {
        isShowingNameView = true;

        nameLinearLayout.setVisibility(View.VISIBLE);
        if(!mEnteredNameString.equals("")) nameEditText.setText(mEnteredNameString);
        nameLinearLayout.animate().translationX(0).alpha(1f).setInterpolator(new LinearOutSlowInInterpolator())
                .setDuration(mAnimationDuration).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                hideBackButtonForSignUp();
                nameLinearLayout.setVisibility(View.VISIBLE);
                nameLinearLayout.setTranslationX(0);
                nameLinearLayout.setAlpha(1f);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).start();

        nextButton.setText(getResources().getString(R.string.next));
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEnteredNameString = nameEditText.getText().toString().trim();
                if(mEnteredNameString.equals("")) nameEditText.setError("Please enter a name");
                else if(mEnteredNameString.length()>16) nameEditText.setError("That name is too long!");
                else{
                    isShowingNameView = false;
                    nameLinearLayout.animate().translationX(Utils.dpToPx(transitionOutTranslation)).alpha(0f).setInterpolator(new LinearOutSlowInInterpolator())
                            .setDuration(mAnimationDuration).setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            nameLinearLayout.setVisibility(View.GONE);
                            nameLinearLayout.setTranslationX(Utils.dpToPx(transitionOutTranslation));
                            nameLinearLayout.setAlpha(0f);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    }).start();
                    setUpSignUpEmailInputView();
                }
            }
        });
    }

    private void setUpSignUpEmailInputView() {
        isShowingEmailView = true;
        if(!mEnteredEmailString.equals("")) emailEditText.setText(mEnteredEmailString);
        emailLinearLayout.setVisibility(View.VISIBLE);
        emailLinearLayout.animate().translationX(0).alpha(1f).setInterpolator(new LinearOutSlowInInterpolator())
                .setDuration(mAnimationDuration).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                showBackButtonForSignUp();
                emailLinearLayout.setTranslationX(0);
                emailLinearLayout.setAlpha(1f);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).start();

        nextButton.setText(getResources().getString(R.string.next));
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEnteredEmailString = emailEditText.getText().toString().trim();
                if(isValidEmail(mEnteredEmailString)){
                    isShowingEmailView = false;
                    emailLinearLayout.animate().translationX(Utils.dpToPx(transitionOutTranslation)).alpha(0f).setInterpolator(new LinearOutSlowInInterpolator())
                            .setDuration(mAnimationDuration).setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            emailLinearLayout.setTranslationX(Utils.dpToPx(transitionOutTranslation));
                            emailLinearLayout.setAlpha(0f);
                            emailLinearLayout.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    }).start();

                    setUpSignUpPasswordInputView();
                }
            }
        });
        viewPasswordImageView.animate().scaleY(0f).scaleX(0f).setDuration(mAnimationDuration).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                viewPasswordImageView.setVisibility(View.INVISIBLE);
                viewPasswordImageView.setScaleX(0f);
                viewPasswordImageView.setScaleY(0f);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).start();
    }

    private boolean isValidEmail(String email) {
        if(email.equals("")){
            emailEditText.setError("We need your email.");
            return false;
        }

        boolean isGoodEmail = (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches());

        if(!email.contains("@")){
            emailEditText.setError("That's not an email address.");
            return false;
        }

        int counter = 0;
        for( int i=0; i<email.length(); i++ ) {
            if(email.charAt(i) == '.' ) {
                counter++;
            }
        }
        if(counter!=1 && counter!=2 && counter!=3){
            emailEditText.setError("We need your actual email address.");
            return false;
        }

        int counter2 = 0;
        boolean continueIncrement = true;
        for( int i=0; i<email.length(); i++ ) {
            if(email.charAt(i) == '@' ) {
                continueIncrement = false;
            }
            if(continueIncrement)counter2++;
        }
        if(counter2<=3){
            emailEditText.setError("That's not a real email address");
            return false;
        }

        if(!isGoodEmail){
            emailEditText.setError("We need your actual email address please");
            return false;
        }
        return isGoodEmail;
    }

    private boolean isLoginValidEmail(String email){
        if(email.equals("")){
            return false;
        }

        boolean isGoodEmail = (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches());

        if(!email.contains("@")){
            return false;
        }

        int counter = 0;
        for( int i=0; i<email.length(); i++ ) {
            if(email.charAt(i) == '.' ) {
                counter++;
            }
        }
        if(counter!=1 && counter!=2 && counter!=3){
            emailEditText.setError("We need your actual email address.");
            return false;
        }

        int counter2 = 0;
        boolean continueIncrement = true;
        for( int i=0; i<email.length(); i++ ) {
            if(email.charAt(i) == '@' ) {
                continueIncrement = false;
            }
            if(continueIncrement)counter2++;
        }
        if(counter2<=3){
            emailEditText.setError("That's not a real email address");
            return false;
        }

        if(!isGoodEmail){
            emailEditText.setError("We need your actual email address please");
            return false;
        }
        return isGoodEmail;
    }


    private void setUpSignUpPasswordInputView() {
        isShowingPasswordView = true;
        if(!mEnteredPasswordString.equals("")) passwordEditText.setText(mEnteredPasswordString);
        passwordLinearLayout.setVisibility(View.VISIBLE);
        passwordLinearLayout.animate().translationX(0).alpha(1f).setInterpolator(new LinearOutSlowInInterpolator())
                .setDuration(mAnimationDuration).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                passwordLinearLayout.setTranslationX(0);
                passwordLinearLayout.setAlpha(1f);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).start();

        nextButton.setText(getResources().getString(R.string.next));
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEnteredPasswordString = passwordEditText.getText().toString().trim();
                if(mEnteredPasswordString.equals("")) passwordEditText.setError(getResources().getString(R.string.youll_need_a_password));
                else if(mEnteredPasswordString.length() < 6) passwordEditText.setError(getResources().getString(R.string.your_password_needs_to_be_at_least_6_characters));
                else if(easyPasswords.contains(mEnteredPasswordString)) passwordEditText.setError(getResources().getString(R.string.you_cant_use_that_as_a_password));
//                else if(!isPasswordValid(mEnteredPasswordString))passwordEditText.setError(getResources().getString(R.string.password_must_be_strong));
                else{
//                    passwordEditText.setText("");
                    isShowingPasswordView = false;
                    passwordLinearLayout.animate().translationX(Utils.dpToPx(transitionOutTranslation)).alpha(0f).setInterpolator(new LinearOutSlowInInterpolator())
                            .setDuration(mAnimationDuration).setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            passwordLinearLayout.setTranslationX(Utils.dpToPx(transitionOutTranslation));
                            passwordLinearLayout.setAlpha(0f);
                            passwordLinearLayout.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    }).start();
                    setUpConfirmPasswordView();
                }
            }
        });

        viewPasswordImageView.setVisibility(View.VISIBLE);
        viewPasswordImageView.animate().scaleY(1f).scaleX(1f).setDuration(mAnimationDuration).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                viewPasswordImageView.setVisibility(View.VISIBLE);
                viewPasswordImageView.setScaleX(1f);
                viewPasswordImageView.setScaleY(1f);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).start();
        viewPasswordImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makePasswordReadable();
            }
        });
    }

    private void makePasswordReadable(){
        passwordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordRetypeEditText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        LoginPasswordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);

        passwordEditText.setSelection(passwordEditText.getText().length());
        passwordRetypeEditText.setSelection(passwordRetypeEditText.getText().length());
        LoginPasswordEditText.setSelection(LoginPasswordEditText.getText().length());

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                makePasswordUnreadable();
            }
        },2000);
    }

    private void makePasswordUnreadable(){
        passwordEditText.setInputType(129);
        passwordEditText.setTypeface(Typeface.DEFAULT);
        passwordRetypeEditText.setInputType(129);
        passwordRetypeEditText.setTypeface(Typeface.DEFAULT);
        LoginPasswordEditText.setInputType(129);
        LoginPasswordEditText.setTypeface(Typeface.DEFAULT);

        passwordEditText.setSelection(passwordEditText.getText().length());
        passwordRetypeEditText.setSelection(passwordRetypeEditText.getText().length());
        LoginPasswordEditText.setSelection(LoginPasswordEditText.getText().length());
    }

    private boolean isPasswordValid(String password){
        Pattern pattern;
        Matcher matcher;
        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$";
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);
        return matcher.matches();

    }

    private void setUpConfirmPasswordView() {
        isShowingRetypePasswordView = true;
        if(!mEnteredRetypedPasswordString.equals("")) passwordRetypeEditText.setText(mEnteredRetypedPasswordString);
        retypePasswordLinearLayout.setVisibility(View.VISIBLE);
        retypePasswordLinearLayout.animate().translationX(0).alpha(1f).setInterpolator(new LinearOutSlowInInterpolator())
                .setDuration(mAnimationDuration).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                retypePasswordLinearLayout.setTranslationX(0);
                retypePasswordLinearLayout.setAlpha(1f);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).start();

        nextButton.setText(getResources().getString(R.string.finish));
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEnteredRetypedPasswordString = passwordRetypeEditText.getText().toString().trim();
                if(mEnteredRetypedPasswordString.equals("")) passwordRetypeEditText.setError("Please retype the password.");
                else if(!mEnteredRetypedPasswordString.equals(mEnteredPasswordString)) passwordRetypeEditText.setError("The passwords don't match.");
                else{
                    setUpNewUser();
                }
            }
        });
    }

    @Override
    public void onBackPressed(){
//        if(!isShowingSpinner) {
//            if (isShowingLoginLayout) {
//                loginBackImageView.performClick();
//            }else if(isShowingSignUpLayout){
//                backImageView.performClick();
//            }else super.onBackPressed();
//        }
        super.onBackPressed();
    }

    private void setUpNewUser() {
        if(!isOnline()){
            Snackbar.make(findViewById(R.id.authenticatorCoordinatorLayout), R.string.no_internet_connection, Snackbar.LENGTH_LONG).show();
        } else{
            showLoadingScreen();
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            mAuth.createUserWithEmailAndPassword(mEnteredEmailString,mEnteredPasswordString)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Log.d(TAG,"authentication successful");
                                isSigningUpForFirstTime = true;
                                createFirebaseUserProfile(task.getResult().getUser());
                            }else {
                                Snackbar.make(findViewById(R.id.authenticatorCoordinatorLayout), getResources().getString(R.string.something_went_wrong), Snackbar.LENGTH_LONG).show();
                                hideLoadingScreens();
                            }
                        }
                    });
        }

    }

    private void createFirebaseUserProfile(final FirebaseUser user) {
        UserProfileChangeRequest addProfileName = new UserProfileChangeRequest.Builder().setDisplayName(mEnteredNameString).build();
        user.updateProfile(addProfileName).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.d(TAG,"Created new username,");
                    new SharedPreferenceManager(mContext).setNameInSharedPref(mEnteredNameString).setEmailInSharedPref(mEnteredEmailString);
                    new DatabaseManager(mContext,"").setUpNewUserInFirebase(mEnteredNameString,mEnteredEmailString);
                }
            }
        });
    }

    private void loadMainActivity(){
        if(isSigningUpForFirstTime){
            selectCountyRelativeLayout.setVisibility(View.VISIBLE);
            selectCountyRelativeLayout.animate().alpha(1f).setDuration(mAnimationDuration).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    selectCountyRelativeLayout.setAlpha(1f);
                    selectCountyRelativeLayout.setVisibility(View.VISIBLE);
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

                        if(pickedLanguageOption!= null) {
                            new SharedPreferenceManager(mContext).setLanguageInSharedPref(pickedLanguageOption);
                            new DatabaseManager(mContext, "").updatePreferredLanguage(pickedLanguageOption);
                        }

                        Intent intent = new Intent(Authenticator.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }else{
                        Toast.makeText(mContext,"Pick Something!",Toast.LENGTH_SHORT).show();
                    }
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
                    new UserSettingsActivityCountyItemAdapter(Utils.loadCounties(mContext),Authenticator.this);
            countyListRecyclerView.setAdapter(UserSettingsActivityCountyItemAdapter);
            countyListRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));

        }else {
            Intent intent = new Intent(Authenticator.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if(mAuth.getCurrentUser()==null) mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) mAuth.removeAuthStateListener(mAuthListener);
    }

    private Locale myLocale;
    String currentLanguage = "en", currentLang;
    public void setLocale(String localeName) {
        if (!localeName.equals(currentLanguage)) {
            myLocale = new Locale(localeName);
            Resources res = getResources();
            DisplayMetrics dm = res.getDisplayMetrics();
            Configuration conf = res.getConfiguration();

            conf.locale = myLocale;
            res.updateConfiguration(conf, dm);
            new SharedPreferenceManager(mContext).setIsFirstTimeLaunch(false);

            Intent refresh = new Intent(this, Authenticator.class);
            refresh.putExtra(currentLang, localeName);
            startActivity(refresh);
        }
    }

}