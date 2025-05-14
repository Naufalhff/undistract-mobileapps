package com.example.undistract.core

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import com.example.undistract.config.AppDatabase
import com.example.undistract.features.block_permanent.data.BlockPermanentRepository
import com.example.undistract.features.block_permanent.data.local.BlockPermanentEntity
import com.example.undistract.features.block_schedules.domain.BlockScheduleManager
import com.example.undistract.features.get_visited_urls.data.VisitedUrlsRepository
import com.example.undistract.features.get_visited_urls.domain.VisitedUrlsManager
import com.example.undistract.features.variable_session.data.VariableSessionRepository
import com.example.undistract.features.variable_session.domain.VariableSessionManager
import com.example.undistract.features.variable_session.presentation.VariableSessionDialogActivity
import com.example.undistract.features.variable_session.presentation.VariableSessionViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalTime


class AppAccessibilityService : AccessibilityService() {

    private lateinit var blockScheduleManager: BlockScheduleManager
    private lateinit var variableSessionManager: VariableSessionManager
    private lateinit var visitedUrlsRepository: VisitedUrlsRepository
    private val visitedUrlsManager by lazy { VisitedUrlsManager() }
    private lateinit var variableSessionRepository: VariableSessionRepository
    private lateinit var variableSessionViewModel: VariableSessionViewModel
    private lateinit var blockPermanentRepository: BlockPermanentRepository
    private lateinit var blockedApps: List<BlockPermanentEntity>
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var lastPackageName: String? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("ACCESSIBILITY_SERVICE", "Service connected")

        // Inisialisasi database dan dao
        val database = AppDatabase.getDatabase(this)
        val visitedUrlsDao = database.visitedUrlsDao()
        val blockPermanentDao = database.blockPermanentDao()
        val blockSchedulesDao = database.blockSchedulesDao()
        val variableSessionDao = database.variableSessionDao()

        blockPermanentRepository = BlockPermanentRepository(blockPermanentDao)
        visitedUrlsRepository = VisitedUrlsRepository(visitedUrlsDao)

        // Inisialisasi manager
        blockScheduleManager = BlockScheduleManager(this, blockSchedulesDao)
        variableSessionManager = VariableSessionManager(this, variableSessionDao)
        variableSessionRepository = VariableSessionRepository(variableSessionDao)
        variableSessionViewModel = VariableSessionViewModel(variableSessionRepository)
        loadBlockedApps()

        // Setup service info untuk accessibility service
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
        }
        serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val context = this
        event ?: return

        val supportedEvents =
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                    AccessibilityEvent.TYPE_VIEW_SCROLLED

        // Ignore ketika user mengetik
        val keyboardPackages = listOf(
            "com.google.android.inputmethod.latin",
            "com.microsoft.swiftkey",
            "com.samsung.android.honeyboard"
        )

        if ((event.eventType and supportedEvents) != 0) {
            val packageName = event.packageName?.toString() ?: "Unknown Package"
            val currentTime = LocalTime.now()
            val supportedBrowsers = setOf(
                "com.android.chrome",
                "org.mozilla.firefox",
                "com.sec.android.app.sbrowser"
            )

            if (packageName !in supportedBrowsers) return

            var detectedUrl: String? = null
            var rawUrl: String? = null

            val rootNode = rootInActiveWindow
            rootNode?.let {
                visitedUrlsManager.processNodeTree(it) { raw, normalized ->
                    rawUrl = raw
                    detectedUrl = normalized
                }
            }

            val currentIdentifier = detectedUrl ?: packageName
            Log.d("CurrentIdentifier", "Identifier: $currentIdentifier")

            if (detectedUrl != null && rawUrl != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    val favicon = visitedUrlsManager.downloadFavicon(detectedUrl!!)
                    visitedUrlsRepository.insertUrl(detectedUrl!!, favicon)
                }
            }

            serviceScope.launch {

                // BLOCK ON SCHEDULES
                if (blockScheduleManager.shouldBlockApp(currentIdentifier, currentTime)) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "This app is blocked!", Toast.LENGTH_SHORT).show()
                    }
                    Log.d("BlockApp", "Menutup aplikasi: $currentIdentifier")
                    blockScheduleManager.blockApp()
                    return@launch
                }

                // VARIABLE SESSION LIMIT
                if (!variableSessionManager.canStartNewSession(currentIdentifier)) {
                    withContext(Dispatchers.Main) {
                        variableSessionManager.showToast("This app is still on cool down period!")
                    }
                    variableSessionManager.blockApp()
                    return@launch
                }

                if (variableSessionManager.askLimit(currentIdentifier)) {
                    withContext(Dispatchers.Main) {
                        val intent = Intent(context, VariableSessionDialogActivity::class.java).apply {
                            putExtra("PACKAGE_NAME", currentIdentifier)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    }
                }

                if (variableSessionManager.isLimitedApp(currentIdentifier)) {
                    if (lastPackageName != currentIdentifier && !keyboardPackages.contains(currentIdentifier)) {
                        lastPackageName?.let { previousPackage ->
                            variableSessionManager.stopTimer(previousPackage, variableSessionViewModel)
                        }

                        variableSessionManager.startTimer(currentIdentifier, variableSessionViewModel)
                        lastPackageName = currentIdentifier
                    }
                } else {
                    if (lastPackageName != null && !keyboardPackages.contains(currentIdentifier)) {
                        lastPackageName?.let { previousPackage ->
                            variableSessionManager.stopTimer(previousPackage, variableSessionViewModel)
                        }
                        lastPackageName = null
                    }
                }

                // BLOCK PERMANENT
                Log.d("AccessibilityService", "Checking if $currentIdentifier is blocked...")

                if (::blockedApps.isInitialized) {
                    if (isAppBlocked(currentIdentifier)) {
                        val appName = getAppName(currentIdentifier)
                        Log.d("AccessibilityService", "App is blocked: $currentIdentifier ($appName)")
                        withContext(Dispatchers.Main){
                            Toast.makeText(context, "App $appName is blocked!", Toast.LENGTH_SHORT).show()
                        }
                        val intent = Intent(Intent.ACTION_MAIN)
                        intent.addCategory(Intent.CATEGORY_HOME)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    } else {
                        Log.d("AccessibilityService", "App is not blocked: $currentIdentifier")
                    }
                } else {
                    Log.d("AccessibilityService", "Blocked apps not initialized yet.")
                }
            }
        }
    }

    override fun onInterrupt() {
        Log.d("BlockApp", "Service terputus!")
    }
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private fun loadBlockedApps() {
        coroutineScope.launch {
            blockPermanentRepository.getActiveBlockPermanent().collect { apps ->
                blockedApps = apps
                Log.d("AccessibilityService", "Blocked apps loaded: ${blockedApps.map { it.packageName }}")
            }
        }
    }
    private fun isAppBlocked(packageName: String): Boolean {
        return blockedApps.any { it.packageName == packageName && it.isActive }
    }

    private fun getAppName(packageName: String): String {
        return blockedApps.find { it.packageName == packageName }?.appName ?: packageName
    }
}
