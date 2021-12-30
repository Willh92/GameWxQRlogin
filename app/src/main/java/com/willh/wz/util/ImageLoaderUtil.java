package com.willh.wz.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.LruCache;
import android.widget.ImageView;

import com.willh.wz.R;
import com.willh.wz.util.image.DownloadUtil;
import com.willh.wz.util.image.ImageSize;
import com.willh.wz.util.image.ImageTag;
import com.willh.wz.util.image.ImageUtils;

import java.io.File;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageLoaderUtil {

    private static final String TAG = "ImageLoader";

    private static ImageLoaderUtil mInstance;

    /**
     * 防止重复url锁
     */
    private final Map<String, ReentrantLock> uriLocks = new WeakHashMap<>();
    /**
     * 图片缓存的核心对象
     */
    private LruCache<String, Bitmap> mLruCache;
    /**
     * 线程池
     */
    private ExecutorService mThreadPool;
    private static final int DEAFULT_THREAD_COUNT = 3;
    /**
     * 队列的调度方式
     */
    private Type mType = Type.LIFO;
    /**
     * 任务队列
     */
    private LinkedList<TaskRunnable> mTaskQueue;
    /**
     * 后台轮询线程
     */
    private Thread mPoolThread;
    private Handler mPoolThreadHandler;
    /**
     * UI线程中的Handler
     */
    private Handler mUIHandler;

    private Semaphore mSemaphorePoolThreadHandler = new Semaphore(0);
    private Semaphore mSemaphoreThreadPool;

    /**
     * 启用本地缓存
     */
    private boolean isDiskCacheEnable = true;
    /**
     *
     */
    private static final String CACHE_DIR = ".image_loader";
    /**
     * 滑动暂停锁
     */
    private final Object pauseLock = new Object();
    private final AtomicBoolean paused = new AtomicBoolean(false);

    public enum Type {
        FIFO, LIFO;
    }

    private ImageLoaderUtil(int threadCount, Type type) {
        init(threadCount, type);
    }

    /**
     * 初始化
     *
     * @param threadCount
     * @param type
     */
    private void init(int threadCount, Type type) {
        initBackThread();

        // 获取我们应用的最大可用内存
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheMemory = maxMemory / 4;
        mLruCache = new LruCache<String, Bitmap>(cacheMemory) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }
        };

        // 创建线程池
        mThreadPool = Executors.newFixedThreadPool(threadCount);
        mTaskQueue = new LinkedList<>();
        mType = type;
        mSemaphoreThreadPool = new Semaphore(threadCount);
        mUIHandler = new Handler(Looper.getMainLooper()) {
            public void handleMessage(Message msg) {
                ImgBeanHolder holder = (ImgBeanHolder) msg.obj;
                Bitmap bm = holder.bitmap;
                ImageView imageview = holder.imageView;
                ImageTag imageTag = holder.imageTag;
                String path = holder.path;
                if (imageTag.equals(imageview.getTag())) {
                    imageview.setImageBitmap(bm);
                }
            }
        };
    }

    /**
     * 初始化后台轮询线程
     */
    private void initBackThread() {
        // 后台轮询线程
        mPoolThread = new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                mPoolThreadHandler = new Handler(Looper.myLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        // 线程池去取出一个任务进行执行
                        mThreadPool.execute(getTask());
                        try {
                            mSemaphoreThreadPool.acquire();
                        } catch (InterruptedException ignore) {
                        }
                    }
                };
                // 释放一个信号量
                mSemaphorePoolThreadHandler.release();
                Looper.loop();
            }
        };

        mPoolThread.start();
    }

    public static ImageLoaderUtil getInstance() {
        if (mInstance == null) {
            synchronized (ImageLoaderUtil.class) {
                if (mInstance == null) {
                    mInstance = new ImageLoaderUtil(DEAFULT_THREAD_COUNT, Type.LIFO);
                }
            }
        }
        return mInstance;
    }

    public static ImageLoaderUtil getInstance(int threadCount, Type type) {
        if (mInstance == null) {
            synchronized (ImageLoaderUtil.class) {
                if (mInstance == null) {
                    mInstance = new ImageLoaderUtil(threadCount, type);
                }
            }
        }
        return mInstance;
    }

    public void loadImage(final String path, final ImageView imageView,
                          final boolean isFromNet, Object tag) {
        loadImage(path, R.drawable.ic_pictures_no, imageView, isFromNet, tag);
    }

    /**
     * 异步加载图片
     *
     * @param path                 图片位置
     * @param defaultImageResource 默认图片ID
     * @param imageView
     * @param isFromNet
     * @param tag
     */
    public void loadImage(final String path, final int defaultImageResource,
                          final ImageView imageView, final boolean isFromNet, Object tag) {

        if (TextUtils.isEmpty(path) || imageView == null)
            return;
        ImageTag imageTag = generateTag(imageView, path);

        if (imageTag == null) {
            imageView.setTag(null);
            imageView.setImageResource(defaultImageResource);
            return;
        }


        if (imageView.getTag() != null
                && imageView.getTag().equals(imageTag)) {
            return;
        }

        //从缓存中直接获取
        Bitmap bm = getBitmapFromLruCache(imageTag.key);
        if (bm != null) {
            imageView.setImageBitmap(bm);
            imageView.setTag(imageTag);
            return;
        }

        //缓存不存在添加到任务
        imageView.setImageResource(defaultImageResource);
        imageView.setTag(imageTag);
        addTask(buildTask(path, imageView, isFromNet, imageTag, tag));
    }

    /**
     * 根据传入的参数，新建一个任务
     *
     * @param url
     * @param imageView
     * @param isFromNet
     * @return
     */
    private TaskRunnable buildTask(final String url,
                                   final ImageView imageView, final boolean isFromNet, ImageTag imageTag, Object tag) {
        return new TaskRunnable(tag) {
            @Override
            public void run() {
                if (waitIfPaused()) {
                    mSemaphoreThreadPool.release();
                    return;
                } else {
                    if (!imageTag.equals(imageView.getTag())) {
                        mSemaphoreThreadPool.release();
                        return;
                    }
                }

                ReentrantLock loadFromUriLock = getLockForUri(MD5Util.toMD5(url));
                LogUtil.d(TAG, "start display image task");
                if (loadFromUriLock.isLocked()) {
                    LogUtil.d(TAG, "waiting for image loaded");
                }
                loadFromUriLock.lock();

                Bitmap bm = getBitmapFromLruCache(imageTag.key);
                try {
                    if (bm != null) {
                        refreshBitmap(url, imageView, imageTag, bm);
                    } else if (!isCancel()) {
                        if (isFromNet) {
                            File file = getDiskCacheFile(imageView.getContext(), imageTag.key);
                            if (file.exists()) {  // 如果在缓存文件中发现
                                LogUtil.d(TAG, "find image :" + url
                                        + " in disk cache.");
                                bm = loadImageFromLocal(file.getAbsolutePath(),
                                        imageTag.size);
                            } else {
                                // 检测是否开启硬盘缓存
                                if (isDiskCacheEnable) {
                                    File origin = getDiskCacheFile(imageView.getContext(), url);
                                    if (!origin.exists()) {  //没下载过原文件
                                        boolean downloadState = DownloadUtil   //下载原文件
                                                .downloadImgByUrl(url, origin);
                                        LogUtil.d(TAG, "download image :" + url
                                                + " to disk cache " + downloadState + ". path is "
                                                + origin.getAbsolutePath());
                                    } else {
                                        LogUtil.d(TAG, "find image origin:" + url
                                                + " to disk cache.path is "
                                                + origin.getAbsolutePath());
                                    }
                                    if (origin.exists()) {
                                        //从原文件加载指定大小的图片
                                        bm = loadImageFromLocal(
                                                origin.getAbsolutePath(),
                                                imageTag.size);
                                        if (bm == null) {
                                            origin.delete();  //原文件加载失败，删除
                                            LogUtil.d(TAG, "get image size:" + url
                                                    + " from disk error.delete origin path is "
                                                    + origin.getAbsolutePath());
                                        } else {
                                            //缓存指定大小的文件
                                            if (ImageUtils.saveBitmapFile(bm, file) != null) {
                                                LogUtil.d(TAG, "save image size:" + url
                                                        + " to disk cache. path is "
                                                        + file.getAbsolutePath());
                                            } else {
                                                LogUtil.d(TAG, "save image size:" + url
                                                        + " to disk cache error. path is "
                                                        + file.getAbsolutePath());
                                            }
                                        }
                                    }
                                } else {   // 直接从网络加载
                                    LogUtil.d(TAG, "load image :" + url
                                            + " to memory.");
                                    if (!isCancel()) {
                                        bm = DownloadUtil.loadImageByUrl(
                                                url, imageTag.size);
                                    }
                                }
                            }
                        } else {
                            bm = DownloadUtil.loadImageByUrl(url, imageTag.size);
                        }
                        if (bm != null) {  // 3、如果图片不为空，把图片加入到缓存
                            addBitmapToLruCache(imageTag.key, bm);
                            if (!isCancel()) {
                                refreshBitmap(url, imageView, imageTag, bm);
                            }
                        }
                    }
                } catch (Exception e) {
                    LogUtil.e(TAG, e.toString());
                } finally {
                    loadFromUriLock.unlock();
                    mSemaphoreThreadPool.release();
                }
            }
        };
    }

    private Bitmap loadImageFromLocal(final String path,
                                      final ImageSize imageSize) {
        return ImageUtils.decodeSampledBitmapFromPath(path, imageSize.width,
                imageSize.height);
    }

    /**
     * 从任务队列取出一个方法
     *
     * @return
     */
    private synchronized Runnable getTask() {
        if (mType == Type.FIFO) {
            return mTaskQueue.removeFirst();
        } else if (mType == Type.LIFO) {
            return mTaskQueue.removeLast();
        }
        return null;
    }

    private void refreshBitmap(final String path, final ImageView imageView,
                               final ImageTag tag, Bitmap bm) {
        if (!tag.equals(imageView.getTag()))
            return;
        Message message = Message.obtain();
        ImgBeanHolder holder = new ImgBeanHolder();
        holder.bitmap = bm;
        holder.path = path;
        holder.imageView = imageView;
        holder.imageTag = tag;
        message.obj = holder;
        mUIHandler.sendMessage(message);
    }

    /**
     * 将图片加入LruCache
     *
     * @param key
     * @param bm
     */
    protected void addBitmapToLruCache(String key, Bitmap bm) {
        if (bm != null)
            mLruCache.put(key, bm);
    }


    private ImageTag generateTag(ImageView imageView, String url) {
        ImageSize imageSize = ImageUtils.getImageViewSize(imageView);
        String key = "";
        if (TextUtils.isEmpty(url))
            return null;
        if (url.startsWith("http://") || url.startsWith("https://")) {
            String matchPicNameStr = "/([^/]*?)(.jpg|.bmp|.gif|\\?)";
            Pattern p = Pattern.compile(matchPicNameStr);
            Matcher m = p.matcher(url);
            if (m.find()) {
                key = m.group(1);
                if ("null".equals(key)) {
                    return null;
                }
            } else {
                key = url;
            }
        }
        return new ImageTag(MD5Util.toMD5(String.format(Locale.getDefault(), "%s:%d:%d", key, imageSize.width, imageSize.height))
                , imageSize);
    }

    private synchronized void addTask(TaskRunnable runnable) {
        mTaskQueue.add(runnable);
        try {
            if (mPoolThreadHandler == null)
                mSemaphorePoolThreadHandler.acquire();
        } catch (InterruptedException e) {
        }
        mPoolThreadHandler.sendEmptyMessage(0x110);
    }

    /**
     * 获取缓存路径
     *
     * @param context
     * @param url     图片原链接
     * @return
     */
    public File getDiskCacheFile(Context context, String url) {
        File cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())) {
            cachePath = context.getExternalFilesDir(CACHE_DIR);
        } else {
            cachePath = new File(context.getCacheDir(), CACHE_DIR);
        }
        if (!cachePath.exists())
            cachePath.mkdirs();
        return new File(cachePath, MD5Util.toMD5(url));
    }

    public File getDiskCacheDir(Context context) {
        File cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())) {
            cachePath = context.getExternalFilesDir(CACHE_DIR);
        } else {
            cachePath = new File(context.getCacheDir(), CACHE_DIR);
        }
        if (!cachePath.exists())
            cachePath.mkdirs();
        return cachePath;
    }

    /**
     * 根据path在缓存中获取bitmap
     *
     * @param key
     * @return
     */
    private Bitmap getBitmapFromLruCache(String key) {
        return mLruCache.get(key);
    }

    private static class ImgBeanHolder {
        Bitmap bitmap;
        ImageView imageView;
        String path;
        ImageTag imageTag;
    }

    ReentrantLock getLockForUri(String uri) {
        ReentrantLock lock = uriLocks.get(uri);
        if (lock == null) {
            lock = new ReentrantLock();
            uriLocks.put(uri, lock);
        }
        return lock;
    }

    private abstract class TaskRunnable implements Runnable {
        private boolean cancel;
        private Object tag;

        public Object getTag() {
            return tag;
        }

        public boolean isCancel() {
            return cancel;
        }

        public void cancel() {
            this.cancel = true;
        }

        public TaskRunnable(Object tag) {
            super();
            this.cancel = false;
            this.tag = tag;
        }

        abstract public void run();

    }

    public interface RequestFilter {
        public boolean apply(TaskRunnable task);
    }

    public void cancelAll(final Object tag) {
        cancelAll(task -> (tag == null || tag.equals(task.getTag())));
    }

    public void cancelAll(RequestFilter filter) {
        synchronized (mTaskQueue) {
            for (TaskRunnable task : mTaskQueue) {
                if (filter.apply(task)) {
                    task.cancel();
                }
            }
        }
    }

    public Object getPauseLock() {
        return pauseLock;
    }

    public AtomicBoolean getPaused() {
        return paused;
    }

    void pause() {
        paused.set(true);
    }

    /**
     * Resumes engine work. Paused "load&display" tasks will continue its work.
     */
    void resume() {
        paused.set(false);
        synchronized (pauseLock) {
            pauseLock.notifyAll();
        }
    }

    private boolean waitIfPaused() {
        AtomicBoolean pause = getPaused();
        if (pause.get()) {
            synchronized (getPauseLock()) {
                if (pause.get()) {
                    LogUtil.d(TAG, "watting for resume");
                    try {
                        getPauseLock().wait();
                    } catch (InterruptedException e) {
                        LogUtil.e(TAG, "task interrupted");
                        return true;
                    }
                    LogUtil.d(TAG, "resume after pasuse");
                }
            }
        }
        return false;
    }

}
