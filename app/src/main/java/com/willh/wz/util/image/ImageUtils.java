package com.willh.wz.util.image;

import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import com.willh.wz.BuildConfig;

import java.lang.reflect.Field;

public class ImageUtils {

    private static final String TAG = "ImageUtils";
    private static final boolean DEBUG = BuildConfig.DEBUG;

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
        // 源图片的宽度
        int width = options.outWidth;
        int height = options.outHeight;
        int inSampleSize = 1;

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
                if (DEBUG)
                    Log.e(TAG, value + "");
            }
        } catch (Exception ignore) {
        }
        return value;
    }

}
