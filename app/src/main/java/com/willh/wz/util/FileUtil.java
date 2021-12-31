package com.willh.wz.util;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class FileUtil {

    public static String getAssetsJson(Context context) {
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

    public static boolean saveToFile(File file, String json) {
        BufferedWriter out = null;
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

    public static String readFromFile(File file) {
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
