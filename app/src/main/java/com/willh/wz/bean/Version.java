package com.willh.wz.bean;

import com.willh.wz.BuildConfig;

import org.json.JSONObject;

public class Version implements JsonParse<Version> {

    public int version = 0;
    public int versionCode = BuildConfig.VERSION_CODE;
    public String versionName = BuildConfig.VERSION_NAME;
    public int menuVersion = MenuList.DEFAULT_VERSION;
    public Msg msg = new Msg();

    public boolean isCache = false;

    public static class Msg {
        public boolean enable = false;
        public String title = "";
        public String msg = "";
        public int mode = 0;
    }

    public static final int MSG_SHOW_MODE_START = 0; //每次启动显示
    public static final int MSG_SHOW_MODE_ONE = 1;   //只显示一次
    public static final int MSG_SHOW_MODE_DAY = 2;   //每天一次

    @Override
    public void parse(String json) {
        parseFormJson(this, json);
    }

    @Override
    public String toJson() {
        return toJson(this);
    }

    public static String toJson(Version version) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("version", version.version);
            jsonObject.put("versionCode", version.versionCode);
            jsonObject.put("versionName", version.versionName);
            jsonObject.put("menuVersion", version.menuVersion);

            JSONObject msgJson = new JSONObject();
            jsonObject.put("msg", msgJson);
            if (version.msg != null) {
                msgJson.put("enable", version.msg.enable);
                msgJson.put("title", version.msg.title);
                msgJson.put("msg", version.msg.msg);
                msgJson.optInt("mode", version.msg.mode);
            }
            return jsonObject.toString();
        } catch (Exception ignore) {
        }
        return null;
    }

    public static Version parseFormJson(Version version, String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            if (version == null)
                version = new Version();
            version.version = jsonObject.optInt("versionCode", 0);
            version.versionCode = jsonObject.optInt("versionCode", BuildConfig.VERSION_CODE);
            version.versionName = jsonObject.optString("versionName", BuildConfig.VERSION_NAME);
            version.menuVersion = jsonObject.optInt("menuVersion", MenuList.DEFAULT_VERSION);
            JSONObject msgJson = jsonObject.optJSONObject("msg");
            if (msgJson != null) {
                version.msg.enable = msgJson.optBoolean("enable", false);
                version.msg.title = msgJson.optString("title", "");
                version.msg.msg = msgJson.optString("msg", "");
                version.msg.mode = msgJson.optInt("mode", 0);
            }
            return version;
        } catch (Exception ignore) {
        }
        return null;
    }

}
