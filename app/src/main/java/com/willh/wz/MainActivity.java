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
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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
import com.willh.wz.bean.Games;
import com.willh.wz.fragment.MsgDialogFragment;
import com.willh.wz.util.MD5Util;

import java.io.File;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@SuppressLint("SetJavaScriptEnabled")
public class MainActivity extends Activity {

    private final static String _mmessage_content = "";
    private final static int _mmessage_sdkVersion = 621086720;
    private final static String _mmessage_appPackage = "com.tencent.mm";

    private final static String CONFIG_GAME = "select_game";

    private static final String URL = "https://open.weixin.qq.com/connect/app/qrconnect?" +
            "appid=%s&bundleid=%s" +
            "&scope=snsapi_base,snsapi_userinfo,snsapi_friend,snsapi_message&state=weixin";

    private MenuItem mShareMenu;
    private WebView mWebView;
    private Bitmap mQrBitmap;

    private SharedPreferences mConfig;
    private GameInfo mCurrentGame;
    private String mCurrentUrl;

    private final Map<String, GameInfo> GAMES = Collections.unmodifiableMap(new Games());

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clear();
    }

    private void clear() {
        if (mQrBitmap != null) {
            mQrBitmap.recycle();
            mQrBitmap = null;
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
        mShareMenu = (MenuItem) menu.findItem(R.id.share);
        mShareMenu.setVisible(false);
        Set<String> titles = GAMES.keySet();
        for (String title : titles) {
            menu.add(title);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.refresh) {
            if (mWebView != null) {
                mShareMenu.setVisible(false);
                mWebView.clearHistory();
                mWebView.loadUrl(mCurrentUrl);
            }
            return true;
        } else if (item.getItemId() == R.id.share) {
            if (checkPermission()) {
                startShare();
            }
            return true;
        } else if (item.getItemId() == R.id.help) {
            if (mCurrentGame != null)
                showMsgDialog(getString(R.string.help_dialog_title, mCurrentGame.name), mCurrentGame.help);
        } else {
            GameInfo gameInfo = GAMES.get(item.getTitle().toString());
            if (gameInfo != null)
                selectGame(gameInfo);
        }
        return super.onOptionsItemSelected(item);
    }

    private void startShare() {
        try {
            if (mQrBitmap != null && !mQrBitmap.isRecycled()) {
                File img = saveBitmapFile(mQrBitmap, "qr.jpg");
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

    private void initView() {
        WebView.setWebContentsDebuggingEnabled(true);
        setStatusBarColor();
        setContentView(R.layout.activity_main);
        mWebView = (WebView) findViewById(R.id.web_view);
        WebSettings settings = mWebView.getSettings();
        //  设置缓存规则
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        mWebView.getSettings().setDomStorageEnabled(true);
        String appCachePath = getApplicationContext().getCacheDir()
                .getAbsolutePath() + "/BrowserCache/";
        mWebView.getSettings().setAppCachePath(appCachePath);
        mWebView.getSettings().setAllowFileAccess(true);
        mWebView.getSettings().setAppCacheEnabled(true);

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new MyWebViewClient());
        mWebView.setWebChromeClient(new MyWebChromeClient());
        mWebView.addJavascriptInterface(new JavaScriptInterface(), "android");
        settings.setUserAgentString("Mozilla/5.0 (Linux; Android 7.0; Mi-4c Build/NRD90M; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/53.0.2785.49 Mobile MQQBrowser/6.2 TBS/043632 Safari/537.36 MicroMessenger/6.6.1.1220(0x26060135) NetType/WIFI Language/zh_CN MicroMessenger/6.6.1.1220(0x26060135) NetType/WIFI Language/zh_CN miniProgram");
        mConfig = getSharedPreferences("config", Context.MODE_PRIVATE);
        selectGame(GAMES.get(mConfig.getString(CONFIG_GAME, "王者荣耀")));
//        showMsgDialog(getString(R.string.help_dialog_title, mCurrentGame.name), mCurrentGame.help);
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
                    view.loadUrl(JS_MODIFY);
                }
            }
        }

    }

    public class MyWebViewClient extends WebViewClient {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            if (mShareMenu != null)
                mShareMenu.setVisible(false);
        }

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
                        intent.putExtras(generateBundle(url));
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

    private final static String JS_MODIFY = "javascript:function modifyPage(){var cancel=document.getElementById('js_cancel_login');cancel.style.display='none';document.getElementsByClassName('auth_rights_tips')[0].innerHTML='扫码只用于授权，不会登录你的微信<br>部分特殊游戏登录，请查说明<br>版本:" + BuildConfig.VERSION_NAME + "';if(!document.getElementById('help')){var help = document.createElement('a');help.id='help';help.className='auth_msg_ft_link';help.style.display='block';help.style.marginTop='10px';help.href='javascript:android.showHelp()';help.appendChild(document.createTextNode('查看说明'));document.getElementsByClassName('auth_rights_tips')[0].appendChild(help);}};modifyPage();";
    private final static String JS_FINISH = "javascript:function getBase64Image(img,width,height){var canvas=document.createElement(\"canvas\");canvas.width=width?width:img.width;canvas.height=height?height:img.height;var ctx=canvas.getContext(\"2d\");ctx.drawImage(img,0,0,canvas.width,canvas.height);var dataURL=canvas.toDataURL();return dataURL};function loadImage(){modifyPage();var img=new Image();img.src=document.getElementsByClassName('auth_qrcode')[0].src;if(img.complete){android.loadQrcodeResult(getBase64Image(img));return}img.onload=function(){console.loadQrcodeResult(getBase64Image(img))}};jQuery(document).ready(function(){});loadImage();";

    private boolean isIntentSafe(Intent intent) {
        return getPackageManager()
                .queryIntentActivities(intent, 0).size() > 0;
    }

    private Bundle generateBundle(String req) {
        Bundle bundle = new Bundle();
        int indexOf = req.indexOf("&state=");
        String code = indexOf >= 0 ? req.substring(req.indexOf("code=") + 5, indexOf) : "";
        String state = indexOf >= 0 ? req.substring(indexOf + 7) : "";
        //auth start
        bundle.putInt("_wxapi_command_type", 1);
        bundle.putString("_wxapi_sendauth_resp_token", code);
        bundle.putString("_wxapi_sendauth_resp_state", state);
        bundle.putString("_wxapi_sendauth_resp_url", req);
        bundle.putString("_wxapi_sendauth_resp_lang", "zh_CN");
        bundle.putString("_wxapi_sendauth_resp_country", "CN");
        bundle.putBoolean("_wxapi_sendauth_resp_auth_result", true);
        bundle.putString("_wxapi_baseresp_openId", null);
        bundle.putString("_wxapi_baseresp_transaction", null);
        bundle.putInt("_wxapi_baseresp_errcode", 0);
        bundle.putString("_wxapi_baseresp_errstr", null);
        //auth end
        bundle.putString("_message_token", null);
        bundle.putString("_mmessage_appPackage", _mmessage_appPackage);
        bundle.putString("_mmessage_content", _mmessage_content);
        bundle.putByteArray("_mmessage_checksum", generateCheckSum(_mmessage_content, _mmessage_sdkVersion, _mmessage_appPackage));
        bundle.putString("wx_token_key", "com.tencent.mm.openapi.token");
        bundle.putInt("_mmessage_sdkVersion", _mmessage_sdkVersion);
        return bundle;
    }

    private final char[] HEX = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private byte[] generateCheckSum(String content, int sdkVersion, String appPackage) {
        String result = "";
        StringBuilder sb = new StringBuilder();
        if (content != null) {
            sb.append(content);
        }
        sb.append(sdkVersion);
        sb.append(appPackage);
        sb.append("mMcShCsTr");
        byte[] bytes = sb.substring(1, 9).getBytes();
        try {
            MessageDigest instance = MessageDigest.getInstance(MD5Util.TAG);
            instance.update(bytes);
            byte[] digest = instance.digest();
            char[] hex = new char[digest.length * 2];
            int index = 0;
            for (byte b : digest) {
                hex[index++] = HEX[(b >>> 4) & 15];
                hex[index++] = HEX[b & 15];
            }
            result = new String(hex);
        } catch (Exception ignore) {
        }
        return result.getBytes();
    }

    private class JavaScriptInterface {

        @JavascriptInterface
        public void loadQrcodeResult(String image) {
            clear();
            mQrBitmap = base64ToBitmap(image.replace("data:image/png;base64,", ""));
            runOnUiThread(() -> mShareMenu.setVisible(true));
        }

        @JavascriptInterface
        public void showHelp() {
            runOnUiThread(() -> showMsgDialog(getString(R.string.help_dialog_title, mCurrentGame.name)
                    , mCurrentGame.help));
        }

    }

    public static Bitmap base64ToBitmap(String base64Data) {
        byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    public File saveBitmapFile(Bitmap b, String fileName) {
        File dir;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            dir = getCacheDir();
        } else {
            dir = getGalleryPath();
        }
        FileOutputStream fos = null;
        try {
            File file = new File(dir, fileName
                    + ".temp");
            if (file.exists()) {
                file.delete();
            }
            fos = new FileOutputStream(file);
            b.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            File picFile = new File(dir, fileName);
            if (file.renameTo(picFile)) {
                return picFile;
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

    private void selectGame(GameInfo gameInfo) {
        if (gameInfo == null) {
            gameInfo = GAMES.get(GAMES.keySet().iterator().next());
        }
        mCurrentGame = gameInfo;
        mCurrentUrl = String.format(Locale.getDefault(), URL, mCurrentGame.appId, mCurrentGame.bundleId);
        mWebView.clearHistory();
        mWebView.loadUrl(mCurrentUrl);
        mConfig.edit().putString(CONFIG_GAME, gameInfo.name).apply();
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

    private File getGalleryPath() {
        File galleryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        if (!galleryPath.exists() && !galleryPath.mkdir()) {
            return null;
        }
        return galleryPath;
    }

    private MsgDialogFragment mHelpDialog;

    private void showMsgDialog(String title, String msg) {
        if (mHelpDialog == null) {
            mHelpDialog = new MsgDialogFragment();
            mHelpDialog.setLeftGone(true)
                    .setRightText(getString(R.string.help_known));
        }
        mHelpDialog.setTitleText(title).setMsgText(msg).show(getFragmentManager(), "MsgDialogFragment");
    }

}