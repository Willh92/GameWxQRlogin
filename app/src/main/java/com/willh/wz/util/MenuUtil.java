package com.willh.wz.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;

import com.willh.wz.bean.GameInfo;
import com.willh.wz.bean.Games;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class MenuUtil {

    private static final Map<String, GameInfo> DEFAULT_GAMES = Collections.unmodifiableMap(new Games());

    public static Map<String, GameInfo> getMenuFromAssets(Context context) {
        Map<String, GameInfo> menu;
        try {
            menu = new LinkedHashMap<>();
            String menuJson = getAssetsJson(context);
            jsonToMenuInfo(menu, menuJson);
        } catch (Exception ignore) {
            menu = null;
        }
        return menu == null ? DEFAULT_GAMES : menu;
    }

    private static void jsonToMenuInfo(Map<String, GameInfo> menu, String menuJson) throws JSONException {
        JSONObject jsonObject = new JSONObject(menuJson);
        int version = jsonObject.optInt("version");
        String defaultHelp = jsonObject.optString("defaultHelp");
        JSONArray game = jsonObject.optJSONArray("game");
        if (game != null) {
            for (int i = 0; i < game.length(); i++) {
                JSONObject g = game.getJSONObject(i);
                String name = g.optString("name");
                String appId = g.optString("appId");
                String bundleId = g.optString("bundleId");
                String pkg = g.optString("pkg");
                String cls = g.optString("cls");
                String help = g.optString("help");
                GameInfo gameInfo = new GameInfo(name, appId, bundleId, defaultHelp);
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
        }
    }

    private static String getAssetsJson(Context context) {
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        try {
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open("gameList.json");
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = bufferedInputStream.read(buffer)) != -1) {
                bao.write(buffer, 0, len);
            }
        } catch (IOException ignore) {
        } finally {
            try {
                bao.close();
            } catch (IOException ignore) {
            }
        }
        return bao.toString();
    }

}
