package com.example.countitup_android

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private var fileChooserCallback: ValueCallback<Array<Uri>>? = null
    private var pendingExport: ExportRequest? = null

    private val openDocumentLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val callback = fileChooserCallback
            fileChooserCallback = null
            val uri = result.data?.data
            callback?.onReceiveValue(if (result.resultCode == RESULT_OK && uri != null) arrayOf(uri) else null)
        }

    private val createDocumentLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val export = pendingExport
            pendingExport = null

            val uri = result.data?.data
            if (result.resultCode != RESULT_OK || uri == null || export == null) {
                toast(getString(R.string.export_cancelled))
                return@registerForActivityResult
            }

            runCatching {
                val writer = checkNotNull(contentResolver.openOutputStream(uri)?.bufferedWriter()) {
                    "Could not open export target."
                }
                writer.use {
                    writer.write(export.csv)
                }
            }.onSuccess {
                toast(getString(R.string.export_success))
            }.onFailure {
                toast(getString(R.string.export_failed))
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.web_view)
        configureWebView(webView)

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (webView.canGoBack()) {
                        webView.goBack()
                    } else {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        )

        if (savedInstanceState == null) {
            webView.loadUrl(LOCAL_APP_URL)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView.saveState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        webView.restoreState(savedInstanceState)
    }

    override fun onDestroy() {
        fileChooserCallback?.onReceiveValue(null)
        fileChooserCallback = null
        pendingExport = null
        webView.removeJavascriptInterface(JS_BRIDGE_NAME)
        webView.destroy()
        super.onDestroy()
    }

    private fun configureWebView(webView: WebView) {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            databaseEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT
            builtInZoomControls = false
            displayZoomControls = false
            loadWithOverviewMode = true
            useWideViewPort = true
        }
        webView.isVerticalScrollBarEnabled = false
        webView.isHorizontalScrollBarEnabled = false
        webView.webViewClient = WebViewClient()
        webView.webChromeClient =
            object : WebChromeClient() {
                override fun onShowFileChooser(
                    webView: WebView?,
                    filePathCallback: ValueCallback<Array<Uri>>,
                    fileChooserParams: FileChooserParams
                ): Boolean {
                    fileChooserCallback?.onReceiveValue(null)
                    fileChooserCallback = filePathCallback

                    val intent =
                        Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            type = CSV_FALLBACK_MIME_TYPE
                            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)

                            val mimeTypes =
                                (fileChooserParams.acceptTypes.asList() + CSV_FALLBACK_MIME_TYPES.asList())
                                    .map { it.trim().lowercase() }
                                    .flatMap { acceptType ->
                                        when {
                                            acceptType.isEmpty() -> emptyList()
                                            acceptType == ".csv" -> CSV_FALLBACK_MIME_TYPES.asList()
                                            acceptType.contains("/") -> listOf(acceptType)
                                            else -> emptyList()
                                        }
                                    }
                                    .distinct()
                                    .toTypedArray()

                            if (mimeTypes.isNotEmpty()) {
                                type = mimeTypes.first()
                                putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                            }
                        }

                    openDocumentLauncher.launch(intent)
                    return true
                }
            }
        webView.addJavascriptInterface(AndroidBridge(), JS_BRIDGE_NAME)
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    inner class AndroidBridge {
        @JavascriptInterface
        fun exportCsv(csvContent: String, suggestedFileName: String) {
            runOnUiThread {
                pendingExport = ExportRequest(csvContent, suggestedFileName.ifBlank { DEFAULT_EXPORT_FILE_NAME })
                val intent =
                    Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "text/csv"
                        putExtra(Intent.EXTRA_TITLE, pendingExport?.fileName)
                    }
                createDocumentLauncher.launch(intent)
            }
        }
    }

    private data class ExportRequest(val csv: String, val fileName: String)

    companion object {
        private const val JS_BRIDGE_NAME = "CountItUpAndroid"
        private const val LOCAL_APP_URL = "file:///android_asset/index.html"
        private const val DEFAULT_EXPORT_FILE_NAME = "count_it_up_log.csv"
        private const val CSV_FALLBACK_MIME_TYPE = "text/*"
        private val CSV_FALLBACK_MIME_TYPES =
            arrayOf(
                "text/csv",
                "text/comma-separated-values",
                "application/csv",
                "application/vnd.ms-excel",
                "text/plain"
            )
    }
}
