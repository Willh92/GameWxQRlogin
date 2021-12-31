package com.willh.wz.bean;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

public class MenuList implements JsonParse<MenuList> {

    public final static int DEFAULT_VERSION = 1;

    public int version = DEFAULT_VERSION;
    public String defaultHelp;
    public Map<String, GameInfo> menu;

    public boolean isCache = false;

    @Override
    public void parse(String json) {
        parseFormJson(this, json);
    }

    @Override
    public String toJson() {
        return null;
    }

    public static MenuList parseFormJson(MenuList menuList, String menuJson) {
        try {
            JSONObject jsonObject = new JSONObject(menuJson);
            int version = jsonObject.optInt("version");
            String defaultHelp = jsonObject.optString("defaultHelp");
            JSONArray game = jsonObject.optJSONArray("game");
            if (game != null) {
                Map<String, GameInfo> menu = new LinkedHashMap<>();
                for (int i = 0; i < game.length(); i++) {
                    JSONObject g = game.getJSONObject(i);
                    String name = g.optString("name");
                    String appId = g.optString("appId");
                    String bundleId = g.optString("bundleId");
                    String pkg = g.optString("pkg");
                    String cls = g.optString("cls");
                    String help = g.optString("help");
                    String py = g.optString("py", "");
                    String icon = g.optString("icon", "");
                    GameInfo gameInfo = new GameInfo(name, appId, bundleId, defaultHelp, icon, py);
                    if (!TextUtils.isEmpty(pkg)) {
                        gameInfo.pkg = pkg;
                    }
                    if (!TextUtils.isEmpty(cls)) {
                        gameInfo.cls = cls;
                    }
                    if (!TextUtils.isEmpty(help)) {
                        gameInfo.help = help;
                    }
                    menu.put(gameInfo.name, gameInfo);
                }
                if (menuList == null)
                    menuList = new MenuList();
                menuList.version = version;
                menuList.defaultHelp = defaultHelp;
                menuList.menu = menu;
                return menuList;
            }
        } catch (Exception ignore) {
        }
        return null;
    }

}
