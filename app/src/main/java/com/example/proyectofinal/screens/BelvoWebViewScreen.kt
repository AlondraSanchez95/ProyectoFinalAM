package com.example.proyectofinal.screens

import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebResourceRequest
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.proyectofinal.ui.theme.MiFuenteGoogle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BelvoWebViewScreen(
    accessToken: String,
    onLinkSuccess: (String) -> Unit,
    isSyncing: Boolean = false,
    syncError: String? = null,
    onBack: () -> Unit
) {
    val cleanToken = remember(accessToken) {
        accessToken.replace("\"", "").trim()
    }
    var capturedLinkId by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var pageError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Conectar Banco (Belvo)", fontFamily = MiFuenteGoogle, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    WebView(context).apply {
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            allowFileAccess = false
                            allowContentAccess = false
                            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                        }
                        android.webkit.CookieManager.getInstance().setAcceptCookie(true)
                        android.webkit.CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

                        fun interceptSuccessUrl(url: String?): Boolean {
                            if (url.isNullOrBlank() || (!url.contains("/success") && !url.contains("link="))) {
                                return false
                            }
                            val uri = android.net.Uri.parse(url)
                            val linkId = uri.getQueryParameter("link")
                                ?: uri.getQueryParameter("link_id")
                            if (!linkId.isNullOrEmpty() && capturedLinkId == null) {
                                capturedLinkId = linkId
                                onLinkSuccess(linkId)
                                return true
                            }
                            return false
                        }
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isLoading = false
                            }

                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                return interceptSuccessUrl(request?.url?.toString())
                            }

                            @Suppress("DEPRECATION")
                            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                                return interceptSuccessUrl(url)
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: android.webkit.WebResourceError?
                            ) {
                                super.onReceivedError(view, request, error)
                                if (request?.isForMainFrame == true) {
                                    isLoading = false
                                    pageError = error?.description?.toString()
                                        ?: "No se pudo cargar el widget de Belvo."
                                }
                            }
                        }

                        val belvoWidgetUrl = android.net.Uri.Builder()
                            .scheme("https")
                            .authority("widget.belvo.io")
                            .appendQueryParameter("access_token", cleanToken)
                            .build()
                            .toString()
                        loadUrl(belvoWidgetUrl)
                    }
                }
            )

            if (isLoading || isSyncing) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            val visibleError = syncError ?: pageError
            if (!visibleError.isNullOrBlank()) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = visibleError,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
