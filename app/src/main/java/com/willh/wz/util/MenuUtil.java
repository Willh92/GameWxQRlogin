package com.willh.wz.util;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.willh.wz.Constant;
import com.willh.wz.bean.GameInfo;
import com.willh.wz.bean.Games;
import com.willh.wz.bean.JsonParse;
import com.willh.wz.bean.MenuList;
import com.willh.wz.bean.Version;

import java.io.File;
import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.Map;

public class MenuUtil {

    private static final Map<String, GameInfo> DEFAULT_GAMES = Collections.unmodifiableMap(new Games());

    public static MenuList getMenu(Context context) {
        MenuList menuList = null;
        try {
            String menuJson = FileUtil.readFromFile(new File(context.getFilesDir(), Constant.FILE_MENU));
            if (!TextUtils.isEmpty(menuJson)) {
                menuList = MenuList.parseFormJson(null, menuJson);
                if (menuList != null)
                    menuList.isCache = true;
            }
        } catch (Exception ignore) {
        }
        if (menuList == null) {
            menuList = new MenuList();
            menuList.menu = DEFAULT_GAMES;
        }
        return menuList;
    }

    public static Version getVersion(Context context) {
        Version version = null;
        try {
            String json = FileUtil.readFromFile(new File(context.getFilesDir(), Constant.FILE_VERSION));
            if (!TextUtils.isEmpty(json)) {
                version = Version.parseFormJson(null, json);
                if (version != null)
                    version.isCache = true;
            }
        } catch (Exception ignore) {
        }
        return version;
    }

    public static NetGetTask<Version> getVersion(NetCallBack<Version> callback) {
        return new NetGetTask<>(Constant.API_VERSION, callback);
    }

    public static NetGetTask<MenuList> getMenuTask(NetCallBack<MenuList> callback) {
        return new NetGetTask<>(Constant.API_MENU, callback);
    }

    @SuppressWarnings("all")
    public static class NetGetTask<T> extends AsyncTask<Object, Void, T> {

        private final String url;
        private Class<T> type;
        private NetCallBack<T> callback;

        public NetGetTask(String url, NetCallBack<T> callback) {
            this.url = url;
            this.callback = callback;
            this.type = (Class<T>) ((ParameterizedType) callback.getClass()
                    .getGenericSuperclass()).getActualTypeArguments()[0];
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (callback != null) {
                callback.onPreExecute();
            }
        }

        @Override
        protected T doInBackground(Object... objects) {
            String content = HttpClientUtil.doGet(url);
            if (!TextUtils.isEmpty(content)) {
                try {
                    T t = null;
                    if (JsonParse.class.isAssignableFrom(type)) {
                        t = type.newInstance();
                        ((JsonParse<?>) t).parse(content);
                    } else if (String.class.isAssignableFrom(type)) {
                        t = (T) content;
                    }
                    if (callback != null) {
                        return callback.onNetResult(content, t);
                    }
                    return t;
                } catch (Exception e) {
                }
            }
            if (callback != null) {
                return callback.onNetResult(null, null);
            }
            return null;
        }

        @Override
        protected void onPostExecute(T t) {
            super.onPostExecute(t);
            if (callback != null) {
                callback.onExecuted(t);
            }
            clear();
        }

        @Override
        protected void onCancelled(T t) {
            super.onCancelled(t);
            if (callback != null) {
                callback.onCancelled(t);
            }
            clear();
        }

        private void clear() {
            callback = null;
        }

    }

    public abstract static class NetCallBack<T> {

        public void onPreExecute() {

        }

        public void onCancelled(T t) {

        }

        public T onNetResult(String result, T t) {
            return t;
        }

        public void onExecuted(T t) {

        }

    }

}
