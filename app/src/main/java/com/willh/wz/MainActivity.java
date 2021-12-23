package com.willh.wz;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
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
import android.util.Log;
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

import java.io.File;
import java.io.FileOutputStream;
import java.security.MessageDigest;

@SuppressLint("SetJavaScriptEnabled")
public class MainActivity extends Activity {

    private final static String _mmessage_content = "";
    private final static int _mmessage_sdkVersion = 621086720;
    private final static String _mmessage_appPackage = "com.tencent.mm";

    private static final String URL = "https://open.weixin.qq.com/connect/app/qrconnect?appid=wx95a3a4d7c627e07d&bundleid=com.tencent.tmgp.sgame&scope=snsapi_base%2Csnsapi_userinfo%2Csnsapi_friend%2Csnsapi_message&state=weixin";

    private MenuItem mShareMenu;
    private WebView mWebView;
    private Bitmap mQrBitmap;

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
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.refresh) {
            if (mWebView != null) {
                mShareMenu.setVisible(false);
                mWebView.reload();
            }
            return true;
        } else if (item.getItemId() == R.id.share) {
            if (checkPermission()) {
                startShare();
            }
            return true;
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
//        WebView.setWebContentsDebuggingEnabled(true);
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
        mWebView.loadUrl(URL);
    }

    private void setStatusBarColor() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
    }

    public static class MyWebChromeClient extends WebChromeClient {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            if (newProgress == 100) {
                if (URL.equals(view.getUrl())) {
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
            if (URL.equals(url)) {
                view.loadUrl(JS_FINISH);
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView webView, String url) {
            Log.e("web", url);
            try {
                if (!TextUtils.isEmpty(url)) {
                    if (url.startsWith("wx95a3a4d7c627e07d://")) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setComponent(new ComponentName("com.tencent.tmgp.sgame", "com.tencent.tmgp.sgame.wxapi.WXEntryActivity"));
                        intent.putExtras(generateBundle(url));
                        if (isIntentSafe(intent)) {
                            startActivity(intent);
                            return true;
                        }
                    } else if (!url.startsWith("http://") && !url.startsWith("https://")) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        if (isIntentSafe(intent)) {
                            startActivity(intent);
                            return true;
                        }
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

    private final static String JS_MODIFY = "javascript:function modifyPage(){var cancel=document.getElementById('js_cancel_login');cancel.style.display='none';document.getElementsByClassName('auth_rights_tips')[0].innerHTML='扫码只用于授权，不会登录你的微信<br>版本:" + BuildConfig.VERSION_NAME + "'};modifyPage();";
    private final static String JS_FINISH = "javascript:function getBase64Image(img,width,height){var canvas=document.createElement(\"canvas\");canvas.width=width?width:img.width;canvas.height=height?height:img.height;var ctx=canvas.getContext(\"2d\");ctx.drawImage(img,0,0,canvas.width,canvas.height);var dataURL=canvas.toDataURL();return dataURL};function loadImage(){modifyPage();var img=new Image();img.src=document.getElementsByClassName('auth_qrcode')[0].src;if(img.complete){android.loadQrcodeResult(getBase64Image(img));return}img.onload=function(){console.loadQrcodeResult(getBase64Image(img))}};jQuery(document).ready(function(){});loadImage();";

    private boolean isIntentSafe(Intent intent) {
        return getPackageManager()
                .queryIntentActivities(intent, 0).size() > 0;
    }

    private Bundle generateBundle(String req) {
        Bundle bundle = new Bundle();
        int indexOf = req.indexOf("&state=weixin");
        String code = indexOf >= 0 ? req.substring(req.indexOf("code=") + 5, indexOf) : "";
        bundle.putString("_message_token", null);
        bundle.putString("_wxapi_sendauth_resp_token", code);
        bundle.putString("_mmessage_appPackage", "com.tencent.mm");
        bundle.putString("_wxapi_baseresp_transaction", null);
        bundle.putString("_wxapi_sendauth_resp_lang", "zh_CN");
        bundle.putInt("_wxapi_command_type", 1);
        bundle.putString("_mmessage_content", _mmessage_content);
        bundle.putString("_wxapi_sendauth_resp_country", "CN");
        bundle.putByteArray("_mmessage_checksum", generateCheckSum(_mmessage_content, _mmessage_sdkVersion, _mmessage_appPackage));
        bundle.putString("wx_token_key", "com.tencent.mm.openapi.token");
        bundle.putString("_wxapi_sendauth_resp_url", req);
        bundle.putInt("_mmessage_sdkVersion", 621086720);
        bundle.putInt("_wxapi_baseresp_errcode", 0);
        bundle.putString("_wxapi_baseresp_errstr", null);
        bundle.putString("_wxapi_baseresp_openId", null);
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

}