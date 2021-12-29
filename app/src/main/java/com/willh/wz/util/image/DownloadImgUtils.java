package com.willh.wz.util.image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.util.Log;
import android.widget.ImageView;

import com.willh.wz.BuildConfig;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadImgUtils {

    private static final String TAG = "DownloadImgUtils";
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * 根据url下载图片在指定的文件
     *
     * @param urlStr
     * @param file
     * @return
     */
    public static boolean downloadImgByUrl(String urlStr, File file) {
        FileOutputStream fos = null;
        InputStream is = null;
        boolean loaded = false;
        File tmpFile = new File(file.getAbsolutePath() + ".tmp");
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            is = conn.getInputStream();
            fos = new FileOutputStream(tmpFile);
            byte[] buf = new byte[512];
            int len = 0;
            while ((len = is.read(buf)) != -1) {
                fos.write(buf, 0, len);
            }
            fos.flush();
            loaded = true;

        } catch (Exception e) {
            if (DEBUG)
                Log.e(TAG, e.toString());
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
            if (loaded && !tmpFile.renameTo(file)) {
                loaded = false;
            }
            if (!loaded) {
                tmpFile.delete();
            }
        }
        return loaded;
    }

    public static Bitmap downloadImgByUrl(String urlStr, ImageView imageview) {
        FileOutputStream fos = null;
        InputStream is = null;
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            is = new BufferedInputStream(conn.getInputStream());
            is.mark(is.available());

            Options opts = new Options();
            opts.inJustDecodeBounds = true;
            Bitmap bitmap = BitmapFactory.decodeStream(is, null, opts);

            // 获取imageview想要显示的宽和高
            ImageSize imageViewSize = ImageUtils.getImageViewSize(imageview);
            opts.inSampleSize = ImageUtils.calculateInSampleSize(opts,
                    imageViewSize.width, imageViewSize.height);

            opts.inJustDecodeBounds = false;
            is.reset();
            bitmap = BitmapFactory.decodeStream(is, null, opts);

            conn.disconnect();
            return bitmap;

        } catch (Exception e) {
            e.printStackTrace();
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
        }
        return null;
    }

}
