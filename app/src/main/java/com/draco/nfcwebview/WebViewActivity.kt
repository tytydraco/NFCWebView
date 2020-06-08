package com.draco.nfcwebview

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.draco.nfcwebview.utils.Nfc

class WebViewActivity : AppCompatActivity() {
    private lateinit var nfc: Nfc
    private lateinit var webView: WebView

    /* Upload contents to HTML view */
    private fun handleNfcScan(intent: Intent) {
        /* Get tag contents */
        val bytes = nfc.readBytes(intent)

        /* Get html content passed to this activity */
        webView.loadDataWithBaseURL(null, String(bytes), "text/html", null, null)
    }

    /* Allow intent following */
    class CustomWebViewClient(private val context: Context) : WebViewClient() {
        /* If we try to navigate to a non-network URL, consider it an intent */
        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            if (request != null) {
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

        /* If we opened the app by scanning a tag, process it */
        handleNfcScan(intent)
    }

    /* Catch Nfc tag scan in our foreground intent filter */
    override fun onNewIntent(thisIntent: Intent?) {
        super.onNewIntent(thisIntent)

        /* Call Nfc tag handler if we are sure this is an Nfc scan */
        if (thisIntent != null)
            handleNfcScan(thisIntent)
    }

    /* Use back button to operate the web view */
    override fun onBackPressed() {
        if (webView.canGoBack())
            webView.goBack()
        else
            super.onBackPressed()
    }

    /* Enable foreground scanning */
    override fun onResume() {
        super.onResume()
        nfc.enableForegroundIntent(this)
    }

    /* Disable foreground scanning */
    override fun onPause() {
        super.onPause()
        nfc.disableForegroundIntent(this)
    }
}