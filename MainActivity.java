package com.teal.expo;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.webkit.WebViewAssetLoader;
import androidx.webkit.WebViewClientCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int FILE_CHOOSER_REQUEST = 1204;
    private WebView webView;
    private ValueCallback<Uri[]> filePathCallback;
    private Uri cameraOutputUri;
    private File cameraOutputFile;
    private WebViewAssetLoader assetLoader;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(17, 42, 70));
        getWindow().setNavigationBarColor(Color.rgb(17, 42, 70));
        getWindow().getDecorView().setSystemUiVisibility(0);

        webView = new WebView(this);
        webView.setBackgroundColor(Color.rgb(243, 246, 249));
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setDefaultTextEncodingName("UTF-8");

        assetLoader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                .build();

        webView.addJavascriptInterface(new AndroidBridge(), "AndroidBridge");
        webView.setWebViewClient(new WebViewClientCompat() {
            @Override
            public android.webkit.WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return assetLoader.shouldInterceptRequest(request.getUrl());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                // Keep the app fully offline: only bundled local app assets may navigate.
                return !"appassets.androidplatform.net".equals(uri.getHost());
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView webView,
                    ValueCallback<Uri[]> filePathCallback,
                    FileChooserParams fileChooserParams) {
                if (MainActivity.this.filePathCallback != null) {
                    MainActivity.this.filePathCallback.onReceiveValue(null);
                }
                MainActivity.this.filePathCallback = filePathCallback;
                boolean wantsImage = false;
                String[] accepts = fileChooserParams.getAcceptTypes();
                if (accepts != null) {
                    for (String accept : accepts) {
                        if (accept != null && accept.toLowerCase(Locale.ROOT).contains("image")) {
                            wantsImage = true;
                            break;
                        }
                    }
                }
                launchPicker(wantsImage);
                return true;
            }
        });
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        webView.loadUrl("https://appassets.androidplatform.net/assets/index.html");
    }

    private void launchPicker(boolean wantsImage) {
        Intent contentIntent = new Intent(Intent.ACTION_GET_CONTENT);
        contentIntent.addCategory(Intent.CATEGORY_OPENABLE);
        contentIntent.setType(wantsImage ? "image/*" : "*/*");

        Intent chooser = Intent.createChooser(contentIntent, wantsImage ? "Take a photo or choose an image" : "Choose backup file");
        if (wantsImage) {
            try {
                cameraOutputFile = File.createTempFile("teal_visitor_", ".jpg", getCacheDir());
                cameraOutputUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", cameraOutputFile);
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraOutputUri);
                cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{cameraIntent});
            } catch (Exception e) {
                cameraOutputUri = null;
                cameraOutputFile = null;
            }
        } else {
            cameraOutputUri = null;
            cameraOutputFile = null;
        }

        try {
            startActivityForResult(chooser, FILE_CHOOSER_REQUEST);
        } catch (Exception e) {
            if (filePathCallback != null) filePathCallback.onReceiveValue(null);
            filePathCallback = null;
            Toast.makeText(this, "No compatible file picker is available.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != FILE_CHOOSER_REQUEST) return;

        Uri[] results = null;
        if (resultCode == RESULT_OK) {
            Uri selected = data != null ? data.getData() : null;
            if (selected != null) {
                results = new Uri[]{selected};
            } else if (cameraOutputUri != null && cameraOutputFile != null
                    && cameraOutputFile.exists() && cameraOutputFile.length() > 0) {
                results = new Uri[]{cameraOutputUri};
            }
        }
        if (filePathCallback != null) {
            filePathCallback.onReceiveValue(results);
            filePathCallback = null;
        }
        if (results == null && cameraOutputFile != null) {
            // Remove an empty camera placeholder after cancellation.
            // A selected/captured file remains available to the WebView through its URI.
            // Keep non-empty files until the app has read the URI.
            if (!cameraOutputFile.exists() || cameraOutputFile.length() == 0) cameraOutputFile.delete();
        }
        cameraOutputUri = null;
        cameraOutputFile = null;
    }

    public final class AndroidBridge {
        @JavascriptInterface
        public boolean isNative() { return true; }

        @JavascriptInterface
        public boolean saveFile(String base64Data, String fileName, String mimeType, String callbackId) {
            final String safeName = (fileName == null ? "TEAL_Expo_Export.bin" : fileName.replaceAll("[^A-Za-z0-9._-]", "_"));
            final String safeMime = (mimeType == null || mimeType.trim().isEmpty()) ? "application/octet-stream" : mimeType;
            new Thread(() -> {
                boolean success = false;
                String resultMessage;
                try {
                    byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        ContentValues values = new ContentValues();
                        values.put(MediaStore.MediaColumns.DISPLAY_NAME, safeName);
                        values.put(MediaStore.MediaColumns.MIME_TYPE, safeMime);
                        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/TEAL Expo");
                        Uri destination = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                        if (destination == null) throw new IllegalStateException("Android could not create a Downloads file.");
                        try (OutputStream output = getContentResolver().openOutputStream(destination)) {
                            if (output == null) throw new IllegalStateException("Could not open the output file.");
                            output.write(bytes);
                            output.flush();
                        }
                        resultMessage = "Saved to Downloads / TEAL Expo / " + safeName;
                    } else {
                        File dir = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "TEAL Expo");
                        if (!dir.exists() && !dir.mkdirs()) throw new IllegalStateException("Could not create the app export folder.");
                        File outputFile = new File(dir, safeName);
                        try (FileOutputStream output = new FileOutputStream(outputFile)) {
                            output.write(bytes);
                            output.flush();
                        }
                        resultMessage = "Saved to app Downloads folder: " + outputFile.getAbsolutePath();
                    }
                    success = true;
                } catch (Exception e) {
                    resultMessage = "Export failed: " + (e.getMessage() == null ? "unknown error" : e.getMessage());
                }
                final boolean ok = success;
                final String message = resultMessage;
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                    String jsMessage = "window.onNativeSaveResult && window.onNativeSaveResult('" +
                            callbackId.replace("'", "") + "'," + ok + ",\"" +
                            message.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ") + "\");";
                    webView.evaluateJavascript(jsMessage, null);
                });
            }).start();
            return true;
        }
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (filePathCallback != null) {
            filePathCallback.onReceiveValue(null);
            filePathCallback = null;
        }
        if (webView != null) {
            webView.stopLoading();
            webView.loadUrl("about:blank");
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
