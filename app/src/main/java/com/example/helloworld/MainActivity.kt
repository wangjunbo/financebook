package com.example.helloworld

import android.content.Context
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.activity.compose.BackHandler
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                WebViewScreen()
            }
        }
    }
}

/**
 * 判断今天是否是第一次打开，如果是则返回true并更新记录
 */
fun isFirstOpenToday(context: Context): Boolean {
    val prefs = context.getSharedPreferences("webview_cache", Context.MODE_PRIVATE)
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val lastDate = prefs.getString("last_refresh_date", "")
    return if (today != lastDate) {
        prefs.edit().putString("last_refresh_date", today).apply()
        true
    } else {
        false
    }
}

@Composable
fun WebViewScreen() {
    val context = LocalContext.current
    val forceRefresh = remember { isFirstOpenToday(context) }

    // 创建WebView实例
    val webView = remember {
        WebView(context).apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                allowFileAccess = true
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                cacheMode = if (forceRefresh) {
                    WebSettings.LOAD_NO_CACHE
                } else {
                    WebSettings.LOAD_DEFAULT
                }
            }
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    url?.let { view?.loadUrl(it) }
                    return true
                }
            }
            // 加载URL
            loadUrl("https://www.ddz.cool/p/finance.html")
        }
    }

    // 处理返回键
    BackHandler {
        if (webView.canGoBack()) {
            webView.goBack()
        }
    }

    // 清理资源
    DisposableEffect(webView) {
        onDispose {
            webView.stopLoading()
            webView.destroy()
        }
    }

    // WebView 部分
    AndroidView(
        factory = { webView },
        modifier = Modifier.fillMaxSize()
    )
}
