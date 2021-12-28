package com.willh.wz.filter;

import android.text.SpannableStringBuilder;
import android.text.Spanned;

import java.util.regex.Pattern;

public class NameFilter extends EmojiFilter {

    private static final String PATTERN_STR = "\\s";
    private static final Pattern PATTERN = Pattern.compile(PATTERN_STR, Pattern.CASE_INSENSITIVE);

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        if (source instanceof SpannableStringBuilder) {
            return super.filter(source, start, end, dest, dstart, dend);
        }
        return PATTERN.matcher(super.filter(source, start, end, dest, dstart, dend)).replaceAll("");
    }

}
