package com.willh.wz.filter;

import android.text.Spannable;
import android.text.Spanned;
import android.text.TextUtils;

import java.util.regex.Pattern;

public class NameFilter extends EmojiFilter {

    private static final String PATTERN_STR = "\\s";
    private static final Pattern PATTERN = Pattern.compile(PATTERN_STR, Pattern.CASE_INSENSITIVE);

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        if ((TextUtils.isEmpty(source) && start == end) || source instanceof Spannable)
            return null;
        return PATTERN.matcher(super.filter(source, start, end, dest, dstart, dend)).replaceAll("");
    }

}
