package com.saj.casualparkbd;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.os.Bundle;
import android.widget.Toast;
import android.net.Uri;
import androidx.appcompat.app.AlertDialog;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;


public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private DownloadManager downloadManager;
   // String USER_AGENT_ = "Mozilla/5.0 (Linux; Android 4.1.1; Galaxy Nexus Build/JRO03C) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.166 Mobile Safari/535.19";
    private static final int REQUEST_SELECT_FILE = 100;
    private ValueCallback<Uri[]> mFilePathCallback;
    private LinearLayout mainLinearLayout;
    private LinearLayout SubMainLinearLayoutNoInternet;
    private SwipeRefreshLayout mainSwipeRefreshLayout;
    private SwipeRefreshLayout SubMainSwipeRefreshId;
    private WebView mainWebView;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainLinearLayout = findViewById(R.id.mainLinerLayoutWebViewId);
        mainSwipeRefreshLayout = findViewById(R.id.mainSwipeRefreshId);
        mainWebView = findViewById(R.id.mainWebViewId);
        SubMainLinearLayoutNoInternet = findViewById(R.id.SubMainLinearLayoutNoInternetId);
        SubMainSwipeRefreshId = findViewById(R.id.SubMainSwipeRefreshId);

        // webView code
      //  mainWebView.getSettings().setUserAgentString(USER_AGENT_);
        mainWebView.getSettings().setJavaScriptEnabled(true);
        mainWebView.getSettings().setAllowFileAccess(true);
        mainWebView.getSettings().setLoadWithOverviewMode(true);
        mainWebView.getSettings().setUseWideViewPort(true);
        mainWebView.getSettings().setPluginState(WebSettings.PluginState.ON);
        mainWebView.setWebViewClient(new HelloWebViewClient());
        mainWebView.getSettings().setDomStorageEnabled(true);
        mainWebView.getSettings().setDomStorageEnabled(true);
        mainWebView.getSettings().setLoadsImagesAutomatically(true);
        mainWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        mainWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mainWebView.getSettings().setAllowFileAccess(true);
        mainWebView.loadUrl(getString(R.string.website_link));


        //--------------------------------------------->>
        mainWebView.setWebChromeClient(new WebChromeClient() {
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
                // Create an intent to open the file chooser dialog
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*"); // Set the MIME type of the files to be selected
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                // Start the intent and wait for the result
                startActivityForResult(Intent.createChooser(intent, "Select File"), REQUEST_SELECT_FILE);
                // Store the callback for later use
                mFilePathCallback = filePathCallback;
                return true;
            }
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
            }
        });

        //--------------------------------------------->>
        mainSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
               mainWebView.reload();
               noInternetConnectionCheck();
            }
        });

        //--------------------------------------------->>
        SubMainSwipeRefreshId.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mainWebView.reload();
              noInternetConnectionCheck();
            }
        });


        // Permission code
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
        mainWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {
                try {
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                    request.setMimeType(mimeType);
                    String cookies = CookieManager.getInstance().getCookie(url);
                    request.addRequestHeader("cookie", cookies);
                    request.addRequestHeader("User-Agent", userAgent);
                    request.setDescription("Downloading file...");
                    request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType));
                    request.allowScanningByMediaScanner();
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, contentDisposition, mimeType));
                    DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                    dm.enqueue(request);
                    Toast.makeText(getApplicationContext(), "Downloading file...", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Error downloading file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Toast.makeText(getApplicationContext(), "Take screenshot and share...", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });


    }// onCreate Method End


    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with app logic
            } else {
                // Permission denied, show error message
                Toast.makeText(this, "Permission denied to write to external storage", Toast.LENGTH_SHORT).show();
            }
        }

    }



    // Override the onActivityResult() method to handle the file chooser result
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SELECT_FILE && mFilePathCallback != null) {
            if (data != null) {
                Uri[] uris = { data.getData() };
                mFilePathCallback.onReceiveValue(uris);
            } else {
                mFilePathCallback.onReceiveValue(null);
            }
            mFilePathCallback = null;
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }



    //--------------------------------------------->>
    private class HelloWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            //====================================
            if (url.startsWith("mailto:") || url.startsWith("tel:") || url.startsWith("geo:") || url.startsWith("http:") || url.startsWith("https:")) {

                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(url));

                startActivity(intent);

                return true;

            } else if (url.startsWith("whatsapp:")) {

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "This is my text to send.");
                sendIntent.setType("text/plain");
                sendIntent.setPackage("com.whatsapp");

                startActivity(sendIntent);

                return true;

            }
            return false;

        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            mainSwipeRefreshLayout.setRefreshing(false);
            SubMainSwipeRefreshId.setRefreshing(false);

        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
             mainLinearLayout.setVisibility(View.GONE);
             SubMainLinearLayoutNoInternet.setVisibility(View.VISIBLE);
            super.onReceivedError(view, request, error);
        }

    } //End



    // onBackPressed method code
    @Override
    public void onBackPressed() {
        // When landing in home screen
        if (!mainWebView.canGoBack()) {

           new AlertDialog.Builder(MainActivity.this)
                    .setTitle("WARNING")
                    .setMessage("Are you sure? You want to exit!")
                    .setIcon(R.drawable.alert_icon)
                    .setNeutralButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    })
                    .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            finish();

                        }
                    }).show();


        } else {
            mainWebView.goBack();
        }

    } //End


    // no internet method code
    public void noInternetConnectionCheck() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileData = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (wifi != null && wifi.isConnected() || mobileData != null && mobileData.isConnected()) {
            mainLinearLayout.setVisibility(View.VISIBLE);
            SubMainLinearLayoutNoInternet.setVisibility(View.GONE);
            mainWebView.reload();

        } else {
            mainLinearLayout.setVisibility(View.GONE);
            SubMainLinearLayoutNoInternet.setVisibility(View.VISIBLE);
        }

    }//End

}//End