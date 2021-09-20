package com.draco.nfcwebview

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class WebViewActivity : AppCompatActivity() {
    private lateinit var nfc: Nfc
    private lateinit var webView: WebView

    /* Allow intent following */
    class CustomWebViewClient(private val context: Context) : WebViewClient() {
        /* If we try to navigate to a non-network URL, consider it an intent */
        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            if (request != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val url = request.url.toString()
                if (!URLUtil.isNetworkUrl(url)) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    ContextCompat.startActivity(context, intent, null)
                    return true
                }
            }

            /* Otherwise, handle it as usual */
            return super.shouldOverrideUrlLoading(view, request)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* Register our Nfc helper class */
        nfc = Nfc(this)

        /* Instantiate fresh web view for this activity */
        webView = WebView(this)
        webView.settings.javaScriptEnabled = true
        webView.settings.allowFileAccess = true
        webView.settings.useWideViewPort = true
        webView.settings.loadWithOverviewMode = true
        webView.webViewClient = CustomWebViewClient(this)
        webView.webChromeClient = WebChromeClient()

        /* Show web view */
        setContentView(webView)

        /* Get tag contents */
        val bytes = nfc.readBytes(intent)

        /* Get html content passed to this activity */
        webView.loadDataWithBaseURL(null, String(bytes), "text/html", null, null)
    }

    /* Use back button to operate the web view */
    override fun onBackPressed() {
        if (webView.canGoBack())
            webView.goBack()
        else
            super.onBackPressed()
    }
}