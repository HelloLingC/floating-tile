package com.lingc.nfloatingtile.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Create by LingC on 2019/8/6 17:21
 */
public class SpUtil {

    public static SharedPreferences getSp(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("appSettings", Context.MODE_PRIVATE);
        return sharedPreferences;
    }

}
