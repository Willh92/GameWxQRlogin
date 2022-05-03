package com.willh.wz.util.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import com.willh.wz.BuildConfig;
import com.willh.wz.util.LogUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;

public class ImageUtils {

    private static final String TAG = "ImageUtils";

    /**
     * 根据ImageView获得适当的压缩的宽和高
     *
     * @param imageView
     * @return
     */
    public static ImageSize getImageViewSize(ImageView imageView) {
        ImageSize imageSize = new ImageSize();
        final DisplayMetrics displayMetrics = imageView.getContext()
                .getResources().getDisplayMetrics();
        final LayoutParams params = imageView.getLayoutParams();

        int width = params.width == LayoutParams.WRAP_CONTENT ? 0 : imageView
                .getWidth(); // Get actual image width
        if (width <= 0)
            width = params.width; // Get layout width parameter
        if (width <= 0)
            width = getImageViewFieldValue(imageView, "mMaxWidth"); // Check
        // maxWidth
        // parameter
        if (width <= 0) {
            if (params.width == LayoutParams.WRAP_CONTENT) {
                width = LayoutParams.WRAP_CONTENT;
            } else {
                width = displayMetrics.widthPixels;
            }
        }

        int height = params.height == LayoutParams.WRAP_CONTENT ? 0 : imageView
                .getHeight(); // Get actual image height
        if (height <= 0)
            height = params.height; // Get layout height parameter
        if (height <= 0)
            height = getImageViewFieldValue(imageView, "mMaxHeight"); // Check
        // maxHeight
        // parameter
        if (height <= 0) {
            if (params.height == LayoutParams.WRAP_CONTENT) {
                height = LayoutParams.WRAP_CONTENT;
            } else {
                height = displayMetrics.heightPixels;
            }
        }
        imageSize.width = width;
        imageSize.height = height;
        return imageSize;
    }

    /**
     * 计算inSampleSize，用于压缩图片
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        int inSampleSize = 1;

        if (reqWidth <= 0 || reqHeight <= 0)
            return inSampleSize;

        // 源图片的宽度
        int width = options.outWidth;
        int height = options.outHeight;

        if (width > reqWidth && height > reqHeight) {
            // 计算出实际宽度和目标宽度的比率
            int widthRatio = Math.round((float) width / (float) reqWidth);
            int heightRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = Math.min(widthRatio, heightRatio);
        }
        return inSampleSize;
    }

    /**
     * 反射获得ImageView设置的最大宽度和高度
     *
     * @param object
     * @param fieldName
     * @return
     */
    private static int getImageViewFieldValue(Object object, String fieldName) {
        int value = 0;
        try {
            Field field = ImageView.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            int fieldValue = (Integer) field.get(object);
            if (fieldValue > 0 && fieldValue < Integer.MAX_VALUE) {
                value = fieldValue;
                LogUtil.e(TAG, value + "");
            }
        } catch (Exception ignore) {
        }
        return value;
    }

    /**
     * 根据图片需要显示的宽和高对图片进行压缩
     *
     * @param path
     * @param width
     * @param height
     * @return
     */
    public static Bitmap decodeSampledBitmapFromPath(String path, int width,
                                                     int height) {
        // 获得图片的宽和高，并不把图片加载到内存中
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        options.inSampleSize = ImageUtils.calculateInSampleSize(options, width,
                height);

        // 使用获得到的InSampleSize再次解析图片
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);

        // 不保存宽高比缩放
        // Bitmap tempBitmap = bitmap;
        // if (tempBitmap != null
        // && (tempBitmap.getWidth() > width || tempBitmap.getHeight() >
        // height)) {
        // bitmap = Bitmap.createScaledBitmap(tempBitmap, width, height, true);
        // tempBitmap.recycle();
        // } else {
        // bitmap = tempBitmap;
        // }

        return bitmap;
    }

    public static File saveQrBitmapFile(Context context, Bitmap bitmap, String fileName) {
        return saveBitmapFile(bitmap, new File(getSaveDir(context), fileName));
    }

    public static File getSaveDir(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return context.getCacheDir();
        } else {
            return getGalleryPath();
        }
    }

    public static File saveBitmapFile(Bitmap bitmap, File save) {
        FileOutputStream fos = null;
        try {
            File file = new File(save.getParent(), save.getName().split("\\.")[0]
                    + ".temp");
            if (file.exists()) {
                file.delete();
            }
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            if (file.renameTo(save)) {
                return save;
            } else {
                return null;
            }
        } catch (Exception ignore) {
        } finally {
            try {
                if (fos != null) {
                    fos.flush();
                    fos.close();
                }
            } catch (Exception ignore) {
            }
        }
        return null;
    }

    public static File getGalleryPath() {
        File galleryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        if (!galleryPath.exists() && !galleryPath.mkdir()) {
            return null;
        }
        return galleryPath;
    }

}
