package com.example.undistract.features.get_visited_urls.domain

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Patterns
import android.view.accessibility.AccessibilityNodeInfo
import com.example.undistract.features.get_visited_urls.data.VisitedUrlsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.io.ByteArrayOutputStream
import java.net.URL
import java.time.LocalTime

class VisitedUrlsManager (
    private val visitedUrlsRepository: VisitedUrlsRepository,
    private val handleBlocking: (String, LocalTime) -> Unit,
    private val getRootNode: () -> AccessibilityNodeInfo?,
) {
    // Track processed nodes to avoid duplicates
    private val processedNodeIds = HashSet<Int>()
    private var lastDetectedUrl: String? = null
    private var urlConfirmationCount = 0
    private val urlConfirmationThreshold = 3
    private val pollingInterval = 2000L
    private val minUrlCheckInterval = 1500L
    private var lastUrlCheckTime = 0L
    private var urlDetectionHandler: Handler? = Handler(Looper.getMainLooper())
    private var urlDetectionRunnable: Runnable? = null
    var isPollingUrl: Boolean = false
        private set
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    fun cleanup() {
        job.cancel() // Panggil ini saat Service dihentikan
    }

    fun processNodeTree(
        node: AccessibilityNodeInfo?,
        urlCallback: (rawUrl: String, normalizedUrl: String) -> Unit
    ) {
        // Clear processed nodes before each traversal
        processedNodeIds.clear()
        traverseNodeTree(node) { rawUrl ->
            val normalizedUrl = normalizeUrl(rawUrl)
            urlCallback(rawUrl, normalizedUrl)
        }
    }

    private fun traverseNodeTree(node: AccessibilityNodeInfo?, urlCallback: (String) -> Unit) {
        if (node == null) return

        // Skip if we've already processed this node
        val nodeId = node.hashCode()
        if (processedNodeIds.contains(nodeId)) return
        processedNodeIds.add(nodeId)

        // Check node text for URLs
        node.text?.toString()?.let { text ->
            extractUrl(text)?.let { url ->
                urlCallback(url)
            }
        }

        // Check content description for URLs
        node.contentDescription?.toString()?.let { desc ->
            extractUrl(desc)?.let { url ->
                urlCallback(url)
            }
        }

        // Traverse all child nodes
        for (i in 0 until node.childCount) {
            try {
                node.getChild(i)?.let { traverseNodeTree(it, urlCallback) }
            } catch (e: Exception) {
                Log.e("GetVisitedUrlsManager", "Error traversing child node: ${e.message}")
            }
        }
    }

    private fun isIpAddress(text: String): Boolean {
        return text.matches(Regex("^\\d{1,3}(\\.\\d{1,3}){3}$"))
    }

    private fun extractUrl(text: String): String? {
        // Pre-filter: Jangan proses jika tidak ada titik atau terlalu pendek atau tidak mirip URL
        val cleaned = text.trim()
        if (cleaned.length < 6 || !cleaned.contains('.') || cleaned.contains(" ")) {
            return null
        }

        // Strategy 1: Full URL patterns (standard format)
        val fullUrlPattern = Patterns.WEB_URL.matcher(text)
        if (fullUrlPattern.find()) {
            val fullUrl = fullUrlPattern.group()
            // Tolak jika IP address atau hanya huruf/angka
            if (isIpAddress(fullUrl) || fullUrl.matches(Regex("^[a-zA-Z0-9]+$"))) return null

            // Validasi: harus mengandung titik dan bukan angka/huruf doang
            if (!fullUrl.contains(".") || fullUrl.matches(Regex("^[a-zA-Z0-9]+$"))) return null

            return fullUrl
        }

        // Strategy 2: Look for domain patterns (common TLDs)
        val domainPattern = "([a-zA-Z0-9][-a-zA-Z0-9]*\\.)+[a-zA-Z]{2,}".toRegex()
        val domainMatcher = domainPattern.find(text)
        domainMatcher?.let {
            val domain = it.value

            // Validasi tambahan
            if (domain.length < 6 || !isLikelyValidDomain(domain)) return null

            return domain
        }

        return null
    }

    private fun normalizeUrl(url: String): String {
        val cleanedUrl = url.trim().replace("\\", "/")
        val finalUrl = if (cleanedUrl.startsWith("http://", true) || cleanedUrl.startsWith("https://", true)) {
            cleanedUrl
        } else {
            "https://$cleanedUrl"
        }

        return try {
            val parsedUrl = URL(finalUrl)
            var host = parsedUrl.host.lowercase()

            if (host.startsWith("www.") || host.startsWith("m.")) {
                host = host.substringAfter(".")
            }

            host // ← ini sudah cukup, return akan mengambil nilai terakhir dari blok try
        } catch (e: Exception) {
            var normalizedUrl = cleanedUrl

            if (normalizedUrl.startsWith("http://", true)) {
                normalizedUrl = normalizedUrl.removePrefix("http://")
            } else if (normalizedUrl.startsWith("https://", true)) {
                normalizedUrl = normalizedUrl.removePrefix("https://")
            }

            normalizedUrl = normalizedUrl.split("/", "?", "#").firstOrNull()?.lowercase() ?: return url

            if (normalizedUrl.startsWith("www.")) {
                normalizedUrl = normalizedUrl.substringAfter(".")
            } else if (normalizedUrl.startsWith("m.")) {
                normalizedUrl = normalizedUrl.substringAfter(".")
            }

            if (isWellKnownDomain(normalizedUrl)) {
                val domainOnly = normalizedUrl.split("/").firstOrNull()
                return domainOnly ?: normalizedUrl
            }

            normalizedUrl
        }
    }

    private fun isLikelyValidDomain(domain: String): Boolean {
        // Common TLDs - add more as needed
        val commonTlds = listOf(
            ".com", ".org", ".net", ".io", ".app", ".co", ".edu", ".gov",
            ".info", ".blog", ".me", ".tv", ".uk", ".us", ".ru", ".de",
            ".jp", ".cn", ".fr", ".it", ".nl", ".es", ".id", ".au"
        )

        // Check for common websites
        val commonSites = listOf(
            "google", "youtube", "facebook", "instagram", "twitter", "tiktok",
            "reddit", "linkedin", "github", "amazon", "netflix", "spotify",
            "whatsapp", "telegram", "pinterest", "snapchat", "twitch"
        )

        // Check if domain ends with a common TLD
        val hasTld = commonTlds.any { domain.endsWith(it) }

        // Check if domain contains a common website name
        val isCommonSite = commonSites.any { domain.contains(it) }

        return hasTld || isCommonSite
    }

    private fun isWellKnownDomain(domain: String): Boolean {
        // Daftar domain terkenal yang cukup simpan domain-nya saja
        val wellKnownDomains = listOf(
            "youtube.com", "instagram.com", "facebook.com", "twitter.com",
            "tiktok.com", "reddit.com", "linkedin.com", "github.com",
            "amazon.com", "netflix.com", "spotify.com", "whatsapp.com",
            "telegram.org", "pinterest.com", "snapchat.com", "twitch.tv",
            "google.com", "gmail.com", "yahoo.com", "bing.com",
            "microsoft.com", "apple.com", "discord.com", "zoom.us"
        )

        return wellKnownDomains.any { domain.equals(it, ignoreCase = true) ||
                domain.endsWith(".$it", ignoreCase = true) }
    }

    private suspend fun downloadFavicon(url: String): ByteArray? = withContext(Dispatchers.IO) {
        try {
            var fixedUrl = url
            if (!fixedUrl.startsWith("http://") && !fixedUrl.startsWith("https://")) {
                fixedUrl = "https://$fixedUrl"
            }

            val baseUri = URL(fixedUrl)
            val doc = Jsoup.connect(fixedUrl).get()

            // Prioritaskan rel icon
            val iconElements = doc.select("link[rel~=(?i)^icon$], link[rel~=(?i)^apple-touch-icon$]")
            val iconUrls = iconElements
                .mapNotNull { it.attr("href") }
                .map { href -> URL(baseUri, href).toString() }

            // Tambahkan fallback URL
            iconUrls.plus(
                listOf(
                    "${baseUri.protocol}://${baseUri.host}/apple-touch-icon.png",
                    "${baseUri.protocol}://${baseUri.host}/android-chrome-192x192.png",
                    "${baseUri.protocol}://${baseUri.host}/favicon.ico"
                )
            ).forEach { iconUrl ->
                try {
                    val inputStream = URL(iconUrl).openStream()
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    if (bitmap != null) {
                        val outputStream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                        Log.d("FaviconDownloader", "Downloaded from: $iconUrl")
                        return@withContext outputStream.toByteArray()
                    }
                } catch (e: Exception) {
                    Log.w("FaviconDownloader", "Failed to load favicon from: $iconUrl")
                }
            }

            Log.w("FaviconDownloader", "No valid favicon found.")
            null
        } catch (e: Exception) {
            Log.e("FaviconDownloader", "Error: ${e.message}", e)
            null
        }
    }

    fun startPolling() {
        if (isPollingUrl) return

        isPollingUrl = true
        resetState()

        urlDetectionRunnable = object : Runnable {
            override fun run() {
                if (!isPollingUrl) return

                val currentTime = System.currentTimeMillis()
                if (currentTime - lastUrlCheckTime < minUrlCheckInterval) {
                    urlDetectionHandler?.postDelayed(this, minUrlCheckInterval)
                    return
                }

                lastUrlCheckTime = currentTime

                getRootNode()?.let { root ->
                    var foundUrl: String? = null

                    processNodeTree(root) { _, normalized ->
                        if (foundUrl == null) {
                            foundUrl = normalized
                        }
                    }

                    processDetection(foundUrl)
                }

                urlDetectionHandler?.postDelayed(this, pollingInterval)
            }
        }

        urlDetectionHandler?.post(urlDetectionRunnable!!)
    }

    fun stopPolling() {
        isPollingUrl = false
        urlDetectionRunnable?.let {
            urlDetectionHandler?.removeCallbacks(it)
        }
        urlDetectionRunnable = null
    }

    fun resetState() {
        lastDetectedUrl = null
        urlConfirmationCount = 0
    }

    private fun processDetection(detectedUrl: String?) {
        if (detectedUrl == null) {
            if (lastDetectedUrl != null) {
                Log.d("UrlDetection", "URL cleared")
                resetState()
            }
            return
        }

        if (detectedUrl == lastDetectedUrl) {
            urlConfirmationCount++
            Log.d("UrlDetection", "URL stable: $detectedUrl (count: $urlConfirmationCount)")

            if (urlConfirmationCount == urlConfirmationThreshold) {
                scope.launch {
                    val favicon = downloadFavicon(detectedUrl)
                    visitedUrlsRepository.insertUrl(detectedUrl, favicon)
                }

                handleBlocking(detectedUrl, LocalTime.now())
            }
        } else {
            Log.d("UrlDetection", "URL changed from '$lastDetectedUrl' to '$detectedUrl'")
            lastDetectedUrl = detectedUrl
            urlConfirmationCount = 1
        }
    }
}