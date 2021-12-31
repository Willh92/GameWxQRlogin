package com.willh.wz.bean;

public class GameInfo {

    public String name;
    public String appId;
    public String bundleId;
    public String pkg;
    public String cls;
    public String help;
    public String icon;
    public String py;

    public GameInfo(String name, String appId, String bundleId) {
        this(name, appId, bundleId, bundleId, bundleId + ".wxapi.WXEntryActivity"
                , "1.打开要登录的游戏，注销当前登录。\n" +
                        "2.登录页如有用户协议等，勾选后游戏切换后台\n" +
                        "3.切换到扫码APP，出现二维码后点击分享给对方\n" +
                        "4.在扫码APP内等待对方扫码授权就可以自动跳转并登录游戏。\n" +
                        "5.如果跳转后没有自动登录，请杀掉游戏进程，重新打开游戏查看。\n" +
                        "6.如果还是没登录，请仔细检查步骤，步骤没错，那就是工具失效了。", "", "");
    }

    public GameInfo(String name, String appId, String bundleId, String help) {
        this(name, appId, bundleId, bundleId, bundleId + ".wxapi.WXEntryActivity", help, "", "");
    }

    public GameInfo(String name, String appId, String bundleId, String icon, String py) {
        this(name, appId, bundleId);
        this.icon = icon;
        this.py = py;
    }

    public GameInfo(String name, String appId, String bundleId, String help, String icon, String py) {
        this(name, appId, bundleId, help);
        this.icon = icon;
        this.py = py;
    }

    public GameInfo(String name, String appId, String bundleId, String pkg, String cls, String help, String icon, String py) {
        this.name = name;
        this.appId = appId;
        this.bundleId = bundleId;
        this.pkg = pkg;
        this.cls = cls;
        this.help = help;
        this.icon = icon;
        this.py = py;
    }

}
