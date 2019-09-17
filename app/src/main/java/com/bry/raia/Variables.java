package com.bry.raia;

import android.graphics.Bitmap;

import com.bry.raia.Models.Post;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class Variables {
    public static Post postToBeViewed;
    public static Bitmap postToBeViewedImageBackground;

    public static HashMap<String,Bitmap> blurredBacks = new LinkedHashMap<>();


    public static boolean hasReachedBottomOfPage;
    public static boolean hasOptionsCardOpen = false;
}
