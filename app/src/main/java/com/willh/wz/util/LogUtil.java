package com.willh.wz.util;

import android.util.Log;

import com.willh.wz.BuildConfig;

public class LogUtil {

    private static final boolean DEBUG = BuildConfig.DEBUG;

    public static void d(String tag, String msg) {
        if (DEBUG)
            Log.d(tag, msg);
    }

    public static void e(String tag, String msg) {
        Log.d(tag, msg);
    }

}
