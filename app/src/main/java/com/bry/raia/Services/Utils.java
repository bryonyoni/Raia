package com.bry.raia.Services;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;

import com.bry.raia.Models.County;
import com.bry.raia.Models.Language;
import com.bry.raia.Models.Petition;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Utils {
    private static final String TAG = "Utils";

    public static Point getDisplaySize(WindowManager windowManager){
        try {
            Display display = windowManager.getDefaultDisplay();
            DisplayMetrics displayMetrics = new DisplayMetrics();
            display.getMetrics(displayMetrics);
            return new Point(displayMetrics.widthPixels, displayMetrics.heightPixels);
        }catch (Exception e){
            e.printStackTrace();
            return new Point(0, 0);
        }
    }

    public static List<County> loadCounties(Context context){
        try{
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            JSONArray array = new JSONArray(loadJSONFromAsset(context, "counties.json"));
            List<County> adList = new ArrayList<>();
            List<String> countyNames = new ArrayList<>();
            for(int i=0;i<array.length();i++){
                County profile = gson.fromJson(array.getString(i), County.class);
                countyNames.add(profile.getName());
//                adList.add(profile);
            }

            Collections.sort(countyNames);
            for(String s:countyNames){
                County c = new County();
                c.setName(s);
                adList.add(c);
            }
            return adList;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static List<Language> loadLanguages(Context context){
        try{
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            JSONArray array = new JSONArray(loadJSONFromAsset(context, "languages.json"));
            List<Language> adList = new ArrayList<>();
            List<String> languageNames = new ArrayList<>();
            for(int i=0;i<array.length();i++){
                Language profile = gson.fromJson(array.getString(i), Language.class);
                languageNames.add(profile.getName());
            }
            Collections.sort(languageNames);
            for(String s:languageNames){
                adList.add(new Language(s));
            }
            return adList;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private static String loadJSONFromAsset(Context context, String jsonFileName) {
        String json = null;
        InputStream is=null;
        try {
            AssetManager manager = context.getAssets();
            Log.d(TAG,"path "+jsonFileName);
            is = manager.open(jsonFileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int spToPx(float sp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
    }

    public static int dpToSp(int dp, Context context) {
        return (int) (dpToPx(dp) / context.getResources().getDisplayMetrics().scaledDensity);
    }
}
