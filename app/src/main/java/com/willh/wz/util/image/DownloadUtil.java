package com.willh.wz.util.image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;

import com.willh.wz.util.LogUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadUtil {

    private static final String TAG = "DownloadUtil";

    private static final int CONNECTION_TIMEOUT = 3_000;

    /**
     * 根据url下载图片在指定的文件
     *
     * @param urlStr
     * @param imageFile
     * @return
     */
    public static boolean downloadImgByUrl(String urlStr, File imageFile) {
        HttpURLConnection conn = null;
        FileOutputStream fos = null;
        InputStream is = null;
        boolean loaded = false;
        File tmpFile = new File(imageFile.getAbsolutePath() + ".tmp");
        try {
            URL url = new URL(urlStr);
            LogUtil.d(TAG, "downloadImgByUrl start:" + urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(CONNECTION_TIMEOUT);
            conn.setReadTimeout(CONNECTION_TIMEOUT);
            conn.connect();
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                is = conn.getInputStream();
                fos = new FileOutputStream(tmpFile);
                byte[] buf = new byte[1024];
                int len;
                while ((len = is.read(buf)) != -1) {
                    fos.write(buf, 0, len);
                }
                fos.flush();
                LogUtil.d(TAG, "downloadImgByUrl success:" + urlStr);
                loaded = true;
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "downloadImgByUrl error:" + e.toString());
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException ignore) {
            }
            try {
                if (fos != null)
                    fos.close();
            } catch (IOException ignore) {
            }
            if (loaded && !tmpFile.renameTo(imageFile)) {
                loaded = false;
            }
            if (!loaded) {
                tmpFile.delete();
            }
            try {
                if (conn != null) {
                    conn.disconnect();
                }
            } catch (Exception ignore) {
            }
        }
        return loaded;
    }

    public static Bitmap loadImageByUrl(String urlStr, ImageSize imageViewSize) {
        InputStream is = null;
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            is = new BufferedInputStream(conn.getInputStream());
            is.mark(is.available());

            Options opts = new Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, opts);

            // 获取imageview想要显示的宽和高
            opts.inSampleSize = ImageUtils.calculateInSampleSize(opts,
                    imageViewSize.width, imageViewSize.height);

            opts.inJustDecodeBounds = false;
            is.reset();
            Bitmap bitmap = BitmapFactory.decodeStream(is, null, opts);
            conn.disconnect();
            return bitmap;
        } catch (Exception ignore) {
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException ignore) {
            }
            try {
                if (conn != null) {
                    conn.disconnect();
                }
            } catch (Exception ignore) {
            }
        }
        return null;
    }

}
