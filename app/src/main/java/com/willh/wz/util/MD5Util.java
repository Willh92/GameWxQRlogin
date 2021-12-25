package com.willh.wz.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;

public class MD5Util {

    public static final String TAG = "MD5";
    public static char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static byte[] toMD5Byte(String str) {
        try {
            try {
                try {
                    return MessageDigest.getInstance(TAG).digest(str.getBytes("UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    return null;
                }
            } catch (Exception e2) {
                e2.printStackTrace();
                return null;
            }
        } catch (Throwable th) {
            th.printStackTrace();
            return null;
        }
    }

    public static byte[] toMD5Byte(byte[] bArr) {
        try {
            return MessageDigest.getInstance(TAG).digest(bArr);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("toMD5Byte, MessageDigest.getInstance crash!");
            return null;
        }
    }

    public static String toMD5(String str) {
        byte[] mD5Byte = toMD5Byte(str);
        if (mD5Byte == null) {
            return "";
        }
        return bytesToHexString(mD5Byte);
    }

    public static String bytesToHexString(byte[] bArr) {
        if (bArr == null || bArr.length != 16) {
            return "";
        }
        char[] cArr = new char[32];
        int i = 0;
        for (int i2 = 0; i2 < 16; i2++) {
            byte b = bArr[i2];
            int i3 = i + 1;
            char[] cArr2 = hexDigits;
            cArr[i] = cArr2[(b >>> 4) & 15];
            i = i3 + 1;
            cArr[i3] = cArr2[b & 15];
        }
        return new String(cArr);
    }

}
