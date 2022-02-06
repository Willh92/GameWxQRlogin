package com.willh.wz;

public class Constant {

    public static final String MMESSAGE_CONTENT = "";
    public static final int MMESSAGE_SDKVERSION = 621086720;
    public static final String MMESSAGE_APPPACKAGE = "com.tencent.mm";

    public static final String CONFIG_VERSION = "version";
    public static final String CONFIG_ANNOUNCEMENT_SHOW = "announcement_show";
    public static final String CONFIG_ANNOUNCEMENT_TIME = "announcement_time";
    public static final String CONFIG_LIST_VERSION = "game_list_version";
    public static final String CONFIG_GAME = "select_game";

    public static final String FILE_MENU = "menu.json";
    public static final String FILE_VERSION = "version.json";

    public static final String URL = "https://open.weixin.qq.com/connect/app/qrconnect?" +
            "appid=%s&bundleid=%s" +
            "&scope=snsapi_base,snsapi_userinfo,snsapi_friend,snsapi_message&state=weixin";

    public static final String SERVER = "https://gitee.com/willhz/GameWxQRlogin/raw/main/";
    public static final String API_VERSION = SERVER + "config/v.json";
    public static final String API_MENU = SERVER + "games/gameList2-min.json";

}
