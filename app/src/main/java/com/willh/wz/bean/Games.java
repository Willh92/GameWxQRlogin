package com.willh.wz.bean;

import java.util.LinkedHashMap;

public final class Games extends LinkedHashMap<String, GameInfo> {
    {
        put("王者荣耀", new GameInfo("王者荣耀", "wx95a3a4d7c627e07d", "com.tencent.tmgp.sgame"));
    }
}
