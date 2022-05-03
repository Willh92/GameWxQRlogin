package com.willh.wz;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.willh.wz.bean.GameInfo;
import com.willh.wz.bean.MenuList;
import com.willh.wz.bean.Version;
import com.willh.wz.fragment.MenuDialogFragment;
import com.willh.wz.fragment.MsgDialogFragment;
import com.willh.wz.fragment.ProgressDialogFragment;
import com.willh.wz.menu.MenuAdapter;
import com.willh.wz.util.CommonUtil;
import com.willh.wz.util.FileUtil;
import com.willh.wz.util.ImageLoaderUtil;
import com.willh.wz.util.LoginUtil;
import com.willh.wz.util.MenuUtil;
import com.willh.wz.util.image.ImageUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

@SuppressLint("SetJavaScriptEnabled")
public class MainActivity extends Activity implements MenuDialogFragment.MenuClickListener {

    private MenuAdapter mMenuAdapter;
    private WebView mWebView;
    private Bitmap mQrBitmap;
    private Bitmap mIdentityBitmap;

    private SharedPreferences mConfig;
    private GameInfo mCurrentGame;
    private String mCurrentUrl;

    private Version mVersion;
    private MenuUtil.NetGetTask<Version> mVersionTask;
    private MenuList mMenuList;
    private MenuUtil.NetGetTask<MenuList> mMenuTask;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        initView();
    }

    private void initView() {
        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG);
        setStatusBarColor();
        setContentView(R.layout.activity_main);
        mWebView = findViewById(R.id.web_view);
        WebSettings settings = mWebView.getSettings();
        //  设置缓存规则
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setDomStorageEnabled(true);
        String appCachePath = getApplicationContext().getCacheDir()
                .getAbsolutePath() + "/BrowserCache/";
        settings.setAppCachePath(appCachePath);
        settings.setAllowFileAccess(true);
        settings.setAppCacheEnabled(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        settings.setJavaScriptEnabled(true);
        settings.setUserAgentString("Mozilla/5.0 (Linux; Android 7.0; Mi-4c Build/NRD90M; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/53.0.2785.49 Mobile MQQBrowser/6.2 TBS/043632 Safari/537.36 MicroMessenger/6.6.1.1220(0x26060135) NetType/WIFI Language/zh_CN MicroMessenger/6.6.1.1220(0x26060135) NetType/WIFI Language/zh_CN miniProgram");

        mWebView.setWebViewClient(new MyWebViewClient());
        mWebView.setWebChromeClient(new MyWebChromeClient());
        mWebView.addJavascriptInterface(new JavaScriptInterface(), "android");

        getConfig();
        getVersion();
    }

    private void initMenu(Version version) {
        mVersion = version;
        int menuVersion = getConfig().getInt(Constant.CONFIG_LIST_VERSION, -1);
        if (menuVersion == -1) { //本地没有
            updateMenu(true);
        } else {
            mMenuList = MenuUtil.getMenu(this);
            selectGame(mMenuList.menu.get(getConfig().getString(Constant.CONFIG_GAME, "")));
            checkVersion();
        }
    }

    private SharedPreferences getConfig() {
        if (mConfig == null) {
            mConfig = getSharedPreferences("config", Context.MODE_PRIVATE);
        }
        return mConfig;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clear();
    }

    private void clear() {
        ImageLoaderUtil.getInstance().cancelAll(null);
        if (mQrBitmap != null) {
            mQrBitmap.recycle();
            mQrBitmap = null;
        }
        if (mIdentityBitmap != null) {
            mIdentityBitmap.recycle();
            mIdentityBitmap = null;
        }
        if (mVersionTask != null) {
            mVersionTask.cancel(false);
        }
        if (mMenuTask != null) {
            mMenuTask.cancel(false);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startShare();
            } else {
                Toast.makeText(this, "二维码写入失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.refresh) {
            if (mWebView != null) {
                mWebView.clearHistory();
                mWebView.loadUrl(mCurrentUrl);
            }
            return true;
        } else if (item.getItemId() == R.id.more) {
            showGameMenu();
        } else if (item.getItemId() == R.id.identity) {
            showIdentityDialog();
        } else {
            GameInfo gameInfo = mMenuList.menu.get(item.getTitle().toString());
            if (gameInfo != null)
                selectGame(gameInfo);
        }
        return super.onOptionsItemSelected(item);
    }

    private void startShare() {
        try {
            if (mQrBitmap != null && !mQrBitmap.isRecycled()) {
                File img = ImageUtils.saveQrBitmapFile(this, mQrBitmap, "qr.jpg");
                if (img != null)
                    shareOneFile(img.getAbsolutePath());
            }
        } catch (Exception ignore) {
            Toast.makeText(this, "分享二维码失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void startShareIdentity() {
        try {
            if (mIdentityBitmap == null || mIdentityBitmap.isRecycled()) {
                mIdentityBitmap = BitmapFactory.decodeStream(getAssets().open("identity_qr.png"));
            }
            if (mIdentityBitmap != null && !mIdentityBitmap.isRecycled()) {
                File img = ImageUtils.saveQrBitmapFile(this, mIdentityBitmap, "identity.jpg");
                if (img != null)
                    shareOneFile(img.getAbsolutePath());
            }
        } catch (Exception ignore) {
            Toast.makeText(this, "分享二维码失败", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        return true;
    }

    private void setStatusBarColor() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
    }

    public class MyWebChromeClient extends WebChromeClient {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            if (newProgress == 100) {
                if (mCurrentUrl.equals(view.getUrl())) {
                    String versionName = "";
                    String updateVisibility = "hidden";
                    if (mVersion != null) {
                        versionName = mVersion.versionName;
                        if (CommonUtil.getVersionCode(MainActivity.this) < mVersion.versionCode) {
                            updateVisibility = "visible";
                        }
                    }
                    view.loadUrl(String.format(Locale.getDefault(), JS_MODIFY
                            , updateVisibility, versionName));
                }
            }
        }

    }

    public class MyWebViewClient extends WebViewClient {

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (mCurrentUrl.equals(url)) {
                view.loadUrl(JS_FINISH);
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView webView, String url) {
            try {
                if (!TextUtils.isEmpty(url)) {
                    GameInfo gameInfo = mCurrentGame;
                    if (url.startsWith(gameInfo.appId)) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setComponent(new ComponentName(gameInfo.pkg, gameInfo.cls));
                        intent.putExtras(LoginUtil.generateBundle(url));
                        if (isIntentSafe(intent)) {
                            startActivity(intent);
                        }
                        return true;
                    } else if (!url.startsWith("http://") && !url.startsWith("https://")) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        if (isIntentSafe(intent)) {
                            startActivity(intent);
                        }
                        return true;
                    }
                }
            } catch (Exception ignore) {
            }
            return false;
        }

        @Override
        public void onReceivedSslError(WebView webView, SslErrorHandler sslErrorHandler, SslError sslError) {
            sslErrorHandler.proceed();
        }
    }

    private final static String JS_MODIFY = "javascript:function modifyPage(){var cancel=document.getElementById('js_cancel_login');cancel.style.display='none';document.getElementsByClassName('auth_msg_bd')[0].style.marginTop='60px';document.getElementsByClassName('auth_rights_tips')[0].innerHTML='扫码只用于授权，不会登录你的微信<br>部分特殊游戏登录，<a class=\"auth_msg_ft_link\" href=\"javascript:android.showHelp()\">查看说明</a><br>仅支持摄像头扫码识别<br>版本:" + BuildConfig.VERSION_NAME + "';if(!document.getElementById('update')){var update = document.createElement('div');update.id='update';update.style.display='block';update.style.color='#f00';update.style.marginTop='10px';update.style.visibility='%s';update.innerHTML='发现新版本%s<a class=\"auth_msg_ft_link\" href=\"javascript:android.toUpdate()\">去更新</a>';document.getElementsByClassName('auth_rights_tips')[0].appendChild(update);}if(!document.getElementById('share')){var share = document.createElement('a');share.id='share';share.className='auth_msg_ft_link';share.style.display='block';share.style.marginTop='10px';share.style.visibility='hidden';share.href='javascript:android.shareQr()';share.appendChild(document.createTextNode('点击分享二维码'));document.getElementsByClassName('auth_msg_hd')[0].appendChild(share);}};modifyPage();";
    private final static String JS_FINISH = "javascript:function getBase64Image(img,width,height){var canvas=document.createElement('canvas');let w=width?width:img.width;let h=height?height:img.height;canvas.width=w*1.1;canvas.height=h*1.1;var ctx=canvas.getContext('2d');ctx.fillStyle='#f3f3f3';ctx.fillRect(0,0,canvas.width,canvas.height);let padding=(canvas.width-w)/2;ctx.drawImage(img,padding,0,w,h);let fontSize=canvas.width/13;ctx.font=fontSize+'px Arial';ctx.textAlign='center';ctx.textBaseline='bottom';ctx.fillStyle='#f00';ctx.fillText('仅支持摄像头扫码识别',canvas.width/2,h*1.05);var dataURL=canvas.toDataURL();return dataURL};function loadImage(){modifyPage();var img=new Image();img.src=document.getElementsByClassName('auth_qrcode')[0].src;if(img.complete){android.loadQrcodeResult(getBase64Image(img));return}img.onload=function(){console.loadQrcodeResult(getBase64Image(img))}};jQuery(document).ready(function(){});loadImage();";
    private final static String JS_SHOW_SHARE = "javascript:function showShare(){var share=document.getElementById('share');if(share){share.style.visibility='visible'}};showShare();";

    private class JavaScriptInterface {

        @JavascriptInterface
        public void loadQrcodeResult(String image) {
            clear();
            mQrBitmap = base64ToBitmap(image.replace("data:image/png;base64,", ""));
            runOnUiThread(() -> {
                if (mWebView != null) {
                    mWebView.loadUrl(JS_SHOW_SHARE);
                }
            });
        }

        @JavascriptInterface
        public void showHelp() {
            runOnUiThread(() -> showMsgDialog(getString(R.string.help_dialog_title, mCurrentGame.name)
                    , mCurrentGame.help));
        }

        @JavascriptInterface
        public void shareQr() {
            runOnUiThread(MainActivity.this::startShare);
        }

        @JavascriptInterface
        public void toUpdate() {
            runOnUiThread(MainActivity.this::startToRelease);
        }

    }

    private boolean isIntentSafe(Intent intent) {
        return getPackageManager()
                .queryIntentActivities(intent, 0).size() > 0;
    }

    public static Bitmap base64ToBitmap(String base64Data) {
        byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }


    private void selectGame(GameInfo gameInfo) {
        if (gameInfo == null) {
            gameInfo = mMenuList.menu.get(mMenuList.menu.keySet().iterator().next());
        }
        if (gameInfo != null) {
            mCurrentGame = gameInfo;
            mCurrentUrl = String.format(Locale.getDefault(), Constant.URL, mCurrentGame.appId, mCurrentGame.bundleId);
            mWebView.clearHistory();
            mWebView.loadUrl(mCurrentUrl);
            getConfig().edit().putString(Constant.CONFIG_GAME, gameInfo.name).apply();
        }
    }

    private void shareOneFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return;
        }
        Uri fileUri = getUri(file);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(fileUri, "image/*");
        intent.putExtra(Intent.EXTRA_STREAM, fileUri);
        startActivity(Intent.createChooser(intent, "二维码分享"));
    }

    private Uri getUri(File file) {
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            String authority = getApplicationInfo().packageName + ".fileProvider";
            uri = FileProvider.getUriForFile(getApplicationContext(), authority, file);
        } else {
            uri = Uri.fromFile(file);
        }
        return uri;
    }

    private MenuDialogFragment mMenuDialog;

    private void showGameMenu() {
        if (mMenuList == null)
            return;
        if (mMenuDialog == null) {
            mMenuAdapter = new MenuAdapter(this, new ArrayList<>(mMenuList.menu.values()));
            mMenuDialog = MenuDialogFragment.getInstance(mMenuAdapter, this);
        }
        mMenuDialog.showAllowingStateLoss(getFragmentManager(), "MenuDialog");
    }

    private void getVersion() {
        if (mVersionTask != null) {
            mVersionTask.cancel(false);
        }
        mVersionTask = MenuUtil.getVersion(new MenuUtil.NetCallBack<Version>() {

            @Override
            public void onPreExecute() {
                super.onPreExecute();
                showProgressDialog();
            }

            @Override
            public Version onNetResult(String result, Version version) {
                if (version == null) {
                    version = MenuUtil.getVersion(MainActivity.this);
                } else {
                    boolean save = FileUtil.saveToFile(new File(MainActivity.this.getFilesDir(), Constant.FILE_VERSION), result);
                    if (save) {
                        int oldVersion = getConfig().getInt(Constant.CONFIG_VERSION, 0);
                        SharedPreferences.Editor editor = getConfig().edit();
                        if (oldVersion < version.version) {
                            editor.putBoolean(Constant.CONFIG_ANNOUNCEMENT_SHOW, false);
                            editor.putLong(Constant.CONFIG_ANNOUNCEMENT_TIME, 0L);
                        }
                        editor.putInt(Constant.CONFIG_VERSION, version.version);
                        editor.apply();
                    }
                }
                return super.onNetResult(result, version);
            }

            @Override
            public void onExecuted(Version version) {
                super.onExecuted(version);
                dismissProgressDialog();
                initMenu(version);
            }

        });
        mVersionTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void updateMenu(boolean first) {
        if (mMenuTask != null) {
            mMenuTask.cancel(false);
        }
        mMenuTask = MenuUtil.getMenuTask(new MenuUtil.NetCallBack<MenuList>() {
            @Override
            public void onPreExecute() {
                super.onPreExecute();
                showProgressDialog();
            }

            @Override
            public MenuList onNetResult(String result, MenuList menuList) {
                if (menuList == null) {
                    menuList = MenuUtil.getMenu(MainActivity.this);
                } else {
                    boolean save = FileUtil.saveToFile(new File(MainActivity.this.getFilesDir(), Constant.FILE_MENU), result);
                    if (save)
                        getConfig().edit().putInt(Constant.CONFIG_LIST_VERSION, menuList.version).apply();
                }
                return super.onNetResult(result, menuList);
            }

            @Override
            public void onExecuted(MenuList menuList) {
                super.onExecuted(menuList);
                mMenuList = menuList;
                if (mMenuAdapter != null) {
                    mMenuAdapter.clear();
                    mMenuAdapter.addAll(menuList.menu.values());
                    mMenuAdapter.getFilter().filter(null);
                }
                if (mMenuDialog != null) {
                    mMenuDialog.resetView();
                }
                selectGame(null);
                dismissProgressDialog();
                if (first)
                    checkVersion();
            }

        });
        mMenuTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void checkVersion() {
        if (mVersion != null) {
            Version.Msg msg = mVersion.msg;
            if (msg != null && msg.enable) {
                showMsg(msg);
            } else {
                checkGameUpdate();
            }
        }
    }

    private void showMsg(Version.Msg msg) {
        switch (msg.mode) {
            case Version.MSG_SHOW_MODE_DAY:
                if (CommonUtil.isSameDate(System.currentTimeMillis()
                        , getConfig().getLong(Constant.CONFIG_ANNOUNCEMENT_TIME, 0L))) {
                    checkGameUpdate();
                } else {
                    showAnnouncementDialog(msg.title, msg.msg);
                }
                break;
            case Version.MSG_SHOW_MODE_ONE:
                if (getConfig().getBoolean(Constant.CONFIG_ANNOUNCEMENT_SHOW, false)) {
                    checkGameUpdate();
                } else {
                    showAnnouncementDialog(msg.title, msg.msg);
                }
                break;
            case Version.MSG_SHOW_MODE_START:
                showAnnouncementDialog(msg.title, msg.msg);
                break;
        }
    }

    private void checkGameUpdate() {
        if (mVersion != null && mVersion.menuVersion > mMenuList.version) {
            showUpdateDialog();
        }
    }

    private MsgDialogFragment mAnnouncementDialog;

    private void showAnnouncementDialog(String title, String msg) {
        if (mAnnouncementDialog == null) {
            mAnnouncementDialog = new MsgDialogFragment();
            mAnnouncementDialog.setOnDismissListener(dialog -> checkGameUpdate());
            mAnnouncementDialog.setLeftGone(true)
                    .setRightText(getString(R.string.help_known));
        }
        SharedPreferences.Editor editor = getConfig().edit();
        editor.putBoolean(Constant.CONFIG_ANNOUNCEMENT_SHOW, true);
        editor.putLong(Constant.CONFIG_ANNOUNCEMENT_TIME, System.currentTimeMillis());
        editor.apply();
        mAnnouncementDialog.setTitleText(title).setMsgText(msg).show(getFragmentManager(), "AnnouncementDialog");
    }

    private MsgDialogFragment mUpdateDialog;

    private void showUpdateDialog() {
        if (mUpdateDialog == null) {
            mUpdateDialog = new MsgDialogFragment()
                    .setMsgText(getString(R.string.update_tips))
                    .setLeftText(getString(R.string.common_cancel))
                    .setRightText(getString(R.string.update))
                    .setRightButtonClickListener(msgDialogFragment -> {
                        updateMenu(false);
                        msgDialogFragment.dismissAllowingStateLoss();
                    });
        }
        mUpdateDialog.show(getFragmentManager(), "UpdateDialog");
    }

    private MsgDialogFragment mHelpDialog;

    private void showMsgDialog(String title, String msg) {
        if (mHelpDialog == null) {
            mHelpDialog = new MsgDialogFragment();
            mHelpDialog.setLeftGone(true)
                    .setRightText(getString(R.string.help_known));
        }
        mHelpDialog.setTitleText(title).setMsgText(msg).show(getFragmentManager(), "MsgDialog");
    }

    private MsgDialogFragment mIdentityDialog;

    private void showIdentityDialog() {
        if (mIdentityDialog == null) {
            mIdentityDialog = new MsgDialogFragment()
                    .setTitleText(getString(R.string.identity_title))
                    .setMsgText(getString(R.string.identity_share_qr_info))
                    .setRightText(getString(R.string.identity_share_qr))
                    .setLeftGone(true)
                    .setRightButtonClickListener(msgDialogFragment -> {
                        startShareIdentity();
                        msgDialogFragment.dismissAllowingStateLoss();
                    });
        }
        mIdentityDialog.show(getFragmentManager(), "IdentityDialog");
    }

    private ProgressDialogFragment mProgressDialog;

    public void showProgressDialog() {
        if (getDialog() != null) {
            getDialog().showAllowingStateLoss(getFragmentManager(), "dialog");
        }
    }

    public void dismissProgressDialog() {
        if (getDialog() != null) {
            try {
                getDialog().dismissAllowingStateLoss();
            } catch (Exception ignore) {
            }
        }
    }

    public ProgressDialogFragment getDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialogFragment();
        }
        return mProgressDialog;
    }

    private void startToRelease() {
        try {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            Uri content_url = Uri.parse("https://dl.willh.cn/qrlogin.apk");
            intent.setData(content_url);
            startActivity(intent);
        } catch (Exception ignore) {
        }
    }

    @Override
    public void onGameUpdateClick() {
        updateMenu(false);
    }

    @Override
    public void onGameSelect(GameInfo gameInfo) {
        selectGame(gameInfo);
    }

}