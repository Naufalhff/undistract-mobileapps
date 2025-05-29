package com.example.undistract.core

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import com.example.undistract.config.AppDatabase
import com.example.undistract.features.block_permanent.data.BlockPermanentRepository
import com.example.undistract.features.block_permanent.data.local.BlockPermanentEntity
import com.example.undistract.features.block_permanent.domain.BlockPermanentManager
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

    private lateinit var blockPermanentManager: BlockPermanentManager
    private lateinit var blockScheduleManager: BlockScheduleManager
    private lateinit var variableSessionManager: VariableSessionManager
    private lateinit var visitedUrlsRepository: VisitedUrlsRepository
    private lateinit var visitedUrlsManager: VisitedUrlsManager
    private lateinit var variableSessionRepository: VariableSessionRepository
    private lateinit var variableSessionViewModel: VariableSessionViewModel
    private lateinit var blockPermanentRepository: BlockPermanentRepository
    private lateinit var blockedApps: List<BlockPermanentEntity>
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var lastPackageName: String? = null

    // URL Detection State Management
    private var isPollingUrl = false
    private var urlDetectionHandler: Handler? = null

    // Ignore ketika user mengetik
    private val keyboardPackages = listOf(
        "com.google.android.inputmethod.latin",
        "com.microsoft.swiftkey",
        "com.samsung.android.honeyboard"
    )

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
        blockPermanentManager = BlockPermanentManager()
        blockScheduleManager = BlockScheduleManager(this, blockSchedulesDao)
        variableSessionManager = VariableSessionManager(this, variableSessionDao)
        variableSessionRepository = VariableSessionRepository(variableSessionDao)
        variableSessionViewModel = VariableSessionViewModel(variableSessionRepository)
        visitedUrlsManager = VisitedUrlsManager (
            visitedUrlsRepository,
            ::handleAppBlocking,
            { rootInActiveWindow }
        )
        loadBlockedApps()

        // Setup service info untuk accessibility service
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
        }
        serviceInfo = info

        // Initialize URL detection handler
        urlDetectionHandler = Handler(Looper.getMainLooper())
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return

        val packageName = event.packageName?.toString() ?: return

        val supportedEvents =
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                    AccessibilityEvent.TYPE_VIEW_SCROLLED or
                    AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED or
                    AccessibilityEvent.TYPE_VIEW_FOCUSED

        val browserPackages = setOf(
            "com.android.chrome",
            "org.mozilla.firefox",
            "com.microsoft.emmx",
            "com.brave.browser",
            "com.sec.android.app.sbrowser",
            "com.opera.browser",
            "com.opera.mini.native",
            "com.UCMobile.intl",
            "com.duckduckgo.mobile.android",
            "com.vivaldi.browser",
            "com.kiwibrowser.browser",
            "com.yandex.browser",
            "org.torproject.torbrowser",
            "com.cloudmosa.puffinFree",
            "com.transsion.phoenix"
        )

        if ((event.eventType and supportedEvents) != 0) {
            val currentTime = LocalTime.now()

            if (packageName in keyboardPackages) return

            if (packageName in browserPackages) {
                handleAppBlocking(packageName, currentTime)

                if (!isPollingUrl) {
                    visitedUrlsManager.startPolling()
                }

                return
            } else {
                visitedUrlsManager.stopPolling()
                visitedUrlsManager.resetState()
            }

            Log.d("CurrentIdentifier", "Identifier: $packageName")
            handleAppBlocking(packageName, currentTime)
        }
    }

    override fun onInterrupt() {
        Log.d("BlockApp", "Service terputus!")
        visitedUrlsManager.stopPolling()
    }

    override fun onDestroy() {
        super.onDestroy()
        visitedUrlsManager.cleanup()
        visitedUrlsManager.stopPolling()
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

    private fun handleAppBlocking(currentIdentifier: String, currentTime: LocalTime) {
        serviceScope.launch {
            // BLOCK ON SCHEDULES
            if (blockScheduleManager.shouldBlockApp(currentIdentifier, currentTime)) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AppAccessibilityService, "This app is blocked!", Toast.LENGTH_SHORT).show()
                }
                Log.d("BlockApp", "Menutup aplikasi: $currentIdentifier")
                blockScheduleManager.blockApp(this@AppAccessibilityService)
                return@launch
            }

            // VARIABLE SESSION LIMIT
            if (!variableSessionManager.canStartNewSession(currentIdentifier)) {
                withContext(Dispatchers.Main) {
                    variableSessionManager.showToast("This app is still on cool down period!")
                }
                variableSessionManager.blockApp(this@AppAccessibilityService)
                return@launch
            }

            if (variableSessionManager.askLimit(currentIdentifier)) {
                withContext(Dispatchers.Main) {
                    val intent = Intent(this@AppAccessibilityService, VariableSessionDialogActivity::class.java).apply {
                        putExtra("PACKAGE_NAME", currentIdentifier)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    startActivity(intent)
                }
            }

            if (variableSessionManager.isLimitedApp(currentIdentifier)) {
                if (lastPackageName != currentIdentifier && !keyboardPackages.contains(currentIdentifier)) {
                    lastPackageName?.let { previousPackage ->
                        variableSessionManager.stopTimer(previousPackage, variableSessionViewModel)
                    }

                    variableSessionManager.startTimer(currentIdentifier, variableSessionViewModel, this@AppAccessibilityService)
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
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AppAccessibilityService, "App $appName is blocked!", Toast.LENGTH_SHORT).show()
                    }
                    blockPermanentManager.blockApp(this@AppAccessibilityService)
                } else {
                    Log.d("AccessibilityService", "App is not blocked: $currentIdentifier")
                }
            } else {
                Log.d("AccessibilityService", "Blocked apps not initialized yet.")
            }
        }
    }
}