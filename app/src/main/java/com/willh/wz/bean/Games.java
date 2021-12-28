package com.willh.wz.bean;

import java.util.LinkedHashMap;

public final class Games extends LinkedHashMap<String, GameInfo> {
    {
        put("王者荣耀", new GameInfo("王者荣耀", "wx95a3a4d7c627e07d", "com.tencent.tmgp.sgame", "", "wzry"));
        put("英雄联盟手游", new GameInfo("英雄联盟", "wx5a611599efa17e78", "com.tencent.lolm"
                , "1.打开要登录的游戏，注销当前登录，杀掉游戏进程\n" +
                "2.切换到扫码APP，出现二维码后点击分享给对方。\n" +
                "3.在扫码APP内等待对方扫码授权，成功授权后会自动跳转。\n" +
                "4.成功跳转后，等待游戏加载到登录页面，稍等片刻再次杀掉游戏进程，重新打开游戏即可自动登录。\n" +
                "5.如果还是没登录，请仔细检查步骤，步骤没错，那就是工具失效了。", "", "yxlm"));
        put("和平精英", new GameInfo("和平精英", "wxc4c0253df149f02d", "com.tencent.tmgp.pubgmhd","","hpjy"));
        put("腾讯欢乐麻将", new GameInfo("欢乐麻将", "wx3bef52104e238bff", "com.qqgame.happymj","","txhlmjqj"));
        put("欢乐斗地主", new GameInfo("欢乐斗地主", "wx76fc280041c16519", "com.qqgame.hlddz","","hlddz"));
        put("QQ飞车手游", new GameInfo("QQ飞车", "wx360b06d575d20cc3", "com.tencent.tmgp.speedmobile","","qqfc"));
    }
}

