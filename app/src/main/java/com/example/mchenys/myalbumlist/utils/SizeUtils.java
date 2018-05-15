package com.example.mchenys.myalbumlist.utils;

import android.content.Context;

/**
 * Created by mChenys on 2016/1/24.
 */
public class SizeUtils {

    public static int px2dp(Context context, int px) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (px / density + 0.5f);
    }

    public static int getStreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

}
