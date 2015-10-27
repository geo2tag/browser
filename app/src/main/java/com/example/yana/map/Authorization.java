package com.example.yana.map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.example.yana.map.util.Util;

/**
 * Created by Yana on 27.10.2015.
 */
public class Authorization {

    public static void execute(final Dialog auth_dialog) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.ctx);
            builder.setTitle("Authorization")
                    .setMessage("To get start workin with application please log in.")
                    .setCancelable(false)
                    .setNeutralButton("Log in", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            auth_dialog.setContentView(R.layout.auth_dialog);
                            final WebView web = (WebView) auth_dialog.findViewById(R.id.webView);
                            web.getSettings().setJavaScriptEnabled(true);
                            web.loadUrl(MainActivity.OATH_URL);
                            web.setWebViewClient(new WebViewClient() {
                                boolean authComplete = false;
                                Intent resultIntent = new Intent();

                                @Override
                                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                                    super.onPageStarted(view, url, favicon);
                                }

                                @Override
                                public void onPageFinished(WebView view, String url) {
                                    super.onPageFinished(view, url);
                                    if (url.contains("?code=") && !authComplete) {
                                        if (CookieManager.getInstance().getCookie(url) != null) {
                                            MainActivity.success = true;
                                        }
                                        MainActivity.cookie = CookieManager.getInstance().getCookie(url);
                                        Log.d("", "\nAll the cookies in a string: " +MainActivity.cookie);
                                        Uri uri = Uri.parse(url);
                                        String authCode = uri.getQueryParameter("code");
                                        Log.d("", "code: " + authCode);
                                        authComplete = true;
                                        resultIntent.putExtra("code", authCode);
                                        Util.saveCookie(MainActivity.ctx, MainActivity.cookie);
                                    } else if (url.contains("error=access_denied")) {
                                        Log.d("", "ACCESS_DENIED_HERE");
                                        authComplete = true;
                                        MainActivity.success = false;
                                        Toast.makeText(MainActivity.ctx, "Error Occured", Toast.LENGTH_SHORT).show();
                                        auth_dialog.dismiss();
                                    }
                                    if (MainActivity.success) {
                                        Toast.makeText(MainActivity.ctx, "Success!", Toast.LENGTH_SHORT).show();
                                        auth_dialog.dismiss();
                                        MainActivity.getPoints();
                                    }
                                }
                            });
                            auth_dialog.show();
                            auth_dialog.setTitle("Authorisation...");
                            auth_dialog.setCancelable(true);
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
    }
}