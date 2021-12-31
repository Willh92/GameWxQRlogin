package com.willh.wz.util;

import android.os.Bundle;

import com.willh.wz.Constant;

import java.security.MessageDigest;

public class LoginUtil {

    public static Bundle generateBundle(String req) {
        Bundle bundle = new Bundle();
        int indexOf = req.indexOf("&state=");
        String code = indexOf >= 0 ? req.substring(req.indexOf("code=") + 5, indexOf) : "";
        String state = indexOf >= 0 ? req.substring(indexOf + 7) : "";
        //auth start
        bundle.putInt("_wxapi_command_type", 1);
        bundle.putString("_wxapi_sendauth_resp_token", code);
        bundle.putString("_wxapi_sendauth_resp_state", state);
        bundle.putString("_wxapi_sendauth_resp_url", req);
        bundle.putString("_wxapi_sendauth_resp_lang", "zh_CN");
        bundle.putString("_wxapi_sendauth_resp_country", "CN");
        bundle.putBoolean("_wxapi_sendauth_resp_auth_result", true);
        bundle.putString("_wxapi_baseresp_openId", null);
        bundle.putString("_wxapi_baseresp_transaction", null);
        bundle.putInt("_wxapi_baseresp_errcode", 0);
        bundle.putString("_wxapi_baseresp_errstr", null);
        //auth end
        bundle.putString("_message_token", null);
        bundle.putString("_mmessage_appPackage", Constant.MMESSAGE_APPPACKAGE);
        bundle.putString("_mmessage_content", Constant.MMESSAGE_CONTENT);
        bundle.putByteArray("_mmessage_checksum", generateCheckSum(Constant.MMESSAGE_CONTENT, Constant.MMESSAGE_SDKVERSION
                , Constant.MMESSAGE_APPPACKAGE));
        bundle.putString("wx_token_key", "com.tencent.mm.openapi.token");
        bundle.putInt("_mmessage_sdkVersion", Constant.MMESSAGE_SDKVERSION);
        return bundle;
    }

    private static final char[] HEX = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private static byte[] generateCheckSum(String content, int sdkVersion, String appPackage) {
        String result = "";
        StringBuilder sb = new StringBuilder();
        if (content != null) {
            sb.append(content);
        }
        sb.append(sdkVersion);
        sb.append(appPackage);
        sb.append("mMcShCsTr");
        byte[] bytes = sb.substring(1, 9).getBytes();
        try {
            MessageDigest instance = MessageDigest.getInstance(MD5Util.TAG);
            instance.update(bytes);
            byte[] digest = instance.digest();
            char[] hex = new char[digest.length * 2];
            int index = 0;
            for (byte b : digest) {
                hex[index++] = HEX[(b >>> 4) & 15];
                hex[index++] = HEX[b & 15];
            }
            result = new String(hex);
        } catch (Exception ignore) {
        }
        return result.getBytes();
    }

}
