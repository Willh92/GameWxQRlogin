package com.willh.wz.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;

import com.willh.wz.bean.GameInfo;
import com.willh.wz.bean.Games;
import com.willh.wz.bean.MenuList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class MenuUtil {

    private static final Map<String, GameInfo> DEFAULT_GAMES = Collections.unmodifiableMap(new Games());

    public static MenuList getMenu(Context context) {
        MenuList menuList = null;
        try {
            String menuJson = readFromFile(context);
            if (TextUtils.isEmpty(menuJson))
                menuJson = getAssetsJson(context);
            menuList = jsonToMenuInfo(menuJson);
        } catch (Exception ignore) {
        }
        if (menuList == null) {
            menuList = new MenuList();
            menuList.menu = DEFAULT_GAMES;
        }
        return menuList;
    }

    private static MenuList jsonToMenuInfo(String menuJson) throws JSONException {
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
            MenuList menuList;
            menuList = new MenuList();
            menuList.version = version;
            menuList.defaultHelp = defaultHelp;
            menuList.menu = menu;
            return menuList;
        }
        return null;
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

    public static class MenuTask extends AsyncTask<Context, Void, MenuList> {

        private MenuTaskCallback callback;

        public MenuTask(MenuTaskCallback callback) {
            this.callback = callback;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (callback != null) {
                callback.onMenuTaskPreExecute();
            }
        }

        @Override
        protected MenuList doInBackground(Context... contexts) {
            Context context = contexts[0];
            String json = HttpClientUtil.doGet("https://gitee.com/willhz/GameWxQRlogin/raw/main/games/gameList-min.json");
            try {
                MenuList menuList = jsonToMenuInfo(json);
                if (menuList != null) {
                    saveToFile(context, json);
                }
                return menuList;
            } catch (Exception ignore) {
            }
            return null;
        }

        @Override
        protected void onPostExecute(MenuList menuList) {
            super.onPostExecute(menuList);
            if (callback != null) {
                callback.onMenuTaskExecuted(menuList);
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            if (callback != null) {
                callback.onMenuTaskCancelled();
            }
        }
    }

    public interface MenuTaskCallback {
        void onMenuTaskPreExecute();

        void onMenuTaskCancelled();

        void onMenuTaskExecuted(MenuList menuList);
    }

    private static boolean saveToFile(Context context, String json) {
        BufferedWriter out = null;
        File file = new File(context.getFilesDir(), "menu.json");
        try {
            if (file.exists()) {
                file.delete();
            }
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true)));
            out.write(json);
            out.flush();
            return true;
        } catch (Exception ignore) {
            if (file.exists()) {
                file.delete();
            }
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ignore) {
                }
            }
        }
        return false;
    }

    private static String readFromFile(Context context) {
        File file = new File(context.getFilesDir(), "menu.json");
        if (!file.exists())
            return null;
        BufferedReader reader = null;
        FileInputStream fis;
        try {
            StringBuilder sbd = new StringBuilder();
            fis = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(fis));
            String row;
            while ((row = reader.readLine()) != null) {
                sbd.append(row);
            }
            return sbd.toString();
        } catch (Exception ignore) {
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

}
