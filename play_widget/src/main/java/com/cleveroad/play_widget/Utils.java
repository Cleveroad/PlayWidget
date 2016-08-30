package com.cleveroad.play_widget;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.ColorRes;

class Utils {
    @TargetApi(Build.VERSION_CODES.M)
    public static int getColor(Resources resources, @ColorRes int colorRes) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return resources.getColor(colorRes, null);
        }
        //noinspection deprecation
        return resources.getColor(colorRes);
    }
}
