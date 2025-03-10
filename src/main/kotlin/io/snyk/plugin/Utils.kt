@file:JvmName("UtilsKt")

package io.snyk.plugin

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.ide.util.PsiNavigationSupport
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.util.Alarm
import com.intellij.util.FileContentUtil
import com.intellij.util.messages.Topic
import io.snyk.plugin.cli.Platform
import io.snyk.plugin.services.SnykAnalyticsService
import io.snyk.plugin.services.SnykApiService
import io.snyk.plugin.services.SnykApplicationSettingsStateService
import io.snyk.plugin.services.SnykCliAuthenticationService
import io.snyk.plugin.services.SnykCodeService
import io.snyk.plugin.services.SnykProjectSettingsStateService
import io.snyk.plugin.services.SnykTaskQueueService
import io.snyk.plugin.services.download.SnykCliDownloaderService
import io.snyk.plugin.settings.SnykProjectSettingsConfigurable
import io.snyk.plugin.snykcode.core.AnalysisData
import io.snyk.plugin.snykcode.core.RunUtils
import io.snyk.plugin.ui.SnykBalloonNotificationHelper
import io.snyk.plugin.ui.toolwindow.SnykToolWindowFactory
import io.snyk.plugin.ui.toolwindow.SnykToolWindowPanel
import snyk.advisor.AdvisorService
import snyk.advisor.AdvisorServiceImpl
import snyk.advisor.SnykAdvisorModel
import snyk.amplitude.AmplitudeExperimentService
import snyk.common.SnykCachedResults
import snyk.common.UIComponentFinder
import snyk.container.ContainerService
import snyk.container.KubernetesImageCache
import snyk.iac.IacScanService
import snyk.oss.OssService
import snyk.oss.OssTextRangeFinder
import java.io.File
import java.net.URL
import java.security.KeyStore
import java.util.Objects.nonNull
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager
import javax.swing.JComponent

private val logger = Logger.getInstance("#io.snyk.plugin.UtilsKt")

fun getOssService(project: Project): OssService? = project.serviceIfNotDisposed()

fun getIacService(project: Project): IacScanService? = project.serviceIfNotDisposed()

fun getSnykCode(project: Project): SnykCodeService? = project.serviceIfNotDisposed()

fun getKubernetesImageCache(project: Project): KubernetesImageCache? = project.serviceIfNotDisposed()

fun getSnykTaskQueueService(project: Project): SnykTaskQueueService? = project.serviceIfNotDisposed()

fun getSnykToolWindowPanel(project: Project): SnykToolWindowPanel? = project.serviceIfNotDisposed()

fun getSnykCachedResults(project: Project): SnykCachedResults? = project.serviceIfNotDisposed()

fun getContainerService(project: Project): ContainerService? = project.serviceIfNotDisposed()

fun getAmplitudeExperimentService(): AmplitudeExperimentService = getApplicationService()

fun getSnykCliAuthenticationService(project: Project): SnykCliAuthenticationService? = project.serviceIfNotDisposed()

fun getSnykCliDownloaderService(): SnykCliDownloaderService = getApplicationService()

fun getSnykProjectSettingsService(project: Project): SnykProjectSettingsStateService? = project.serviceIfNotDisposed()

fun getCliFile() = File(getPluginPath(), Platform.current().snykWrapperFileName)

fun isCliInstalled(): Boolean = ApplicationManager.getApplication().isUnitTestMode || getCliFile().exists()

fun pluginSettings(): SnykApplicationSettingsStateService = getApplicationService()

fun getSnykApiService(): SnykApiService = getApplicationService()

fun getSnykAnalyticsService(): SnykAnalyticsService = getApplicationService()

fun getSnykAdvisorModel(): SnykAdvisorModel = getApplicationService()

fun getAdvisorService(): AdvisorService = getApplicationService<AdvisorServiceImpl>()

fun getOssTextRangeFinderService(): OssTextRangeFinder = getApplicationService()

fun getPluginPath() = PathManager.getPluginsPath() + "/snyk-intellij-plugin"

fun isProjectSettingsAvailable(project: Project?) = nonNull(project) && !project!!.isDefault

fun snykToolWindow(project: Project): ToolWindow? {
    return ToolWindowManager.getInstance(project).getToolWindow(SnykToolWindowFactory.SNYK_TOOL_WINDOW)
}

// see project.service<T>() in com.intellij.openapi.components
private inline fun <reified T : Any> Project.serviceIfNotDisposed(): T? {
    if (this.isDisposed) return null
    return try {
        getService(T::class.java)
    } catch (ignored: Throwable) {
        null
    }
}

/**
 * Copy of [com.intellij.openapi.components.service] to make code compilable with jvm 11 bytecode (Idea 2022.1)
 */
private inline fun <reified T : Any> getApplicationService(): T {
    val serviceClass = T::class.java
    return ApplicationManager.getApplication().getService(serviceClass)
        ?: throw RuntimeException("Cannot find service ${serviceClass.name} (classloader=${serviceClass.classLoader})")
}

fun <L> getSyncPublisher(project: Project, topic: Topic<L>): L? {
    val messageBus = project.messageBus
    if (messageBus.isDisposed) return null
    return messageBus.syncPublisher(topic)
}

val <T> List<T>.tail: List<T>
    get() = drop(1)

val <T> List<T>.head: T
    get() = first()

fun isUrlValid(url: String?): Boolean =
    url == null || url.isEmpty() || try {
        URL(url).toURI()
        true
    } catch (throwable: Throwable) {
        false
    }

fun isOssRunning(project: Project): Boolean {
    val indicator = getSnykTaskQueueService(project)?.ossScanProgressIndicator
    return indicator != null && indicator.isRunning && !indicator.isCanceled
}

fun isSnykCodeRunning(project: Project): Boolean =
    AnalysisData.instance.isUpdateAnalysisInProgress(project) || RunUtils.instance.isFullRescanRequested(project)

fun isIacRunning(project: Project): Boolean {
    val indicator = getSnykTaskQueueService(project)?.iacScanProgressIndicator
    return indicator != null && indicator.isRunning && !indicator.isCanceled
}

fun isContainerRunning(project: Project): Boolean {
    val indicator = getSnykTaskQueueService(project)?.containerScanProgressIndicator
    return indicator != null && indicator.isRunning && !indicator.isCanceled
}

fun isScanRunning(project: Project): Boolean =
    isOssRunning(project) || isSnykCodeRunning(project) || isIacRunning(project) || isContainerRunning(project)

fun isCliDownloading(): Boolean = getSnykCliDownloaderService().isCliDownloading()

// check sastEnablement in a loop with rising timeout
fun startSastEnablementCheckLoop(parentDisposable: Disposable, onSuccess: () -> Unit = {}) {
    val settings = pluginSettings()
    val alarm = Alarm(Alarm.ThreadToUse.POOLED_THREAD, parentDisposable)

    var currentAttempt = 1
    val maxAttempts = 20
    lateinit var checkIfSastEnabled: () -> Unit
    checkIfSastEnabled = {
        if (settings.sastOnServerEnabled != true) {
            settings.sastOnServerEnabled = getSnykApiService().sastSettings?.sastEnabled ?: false
            if (settings.sastOnServerEnabled == true) {
                onSuccess.invoke()
            } else if (!alarm.isDisposed && currentAttempt < maxAttempts) {
                currentAttempt++
                alarm.addRequest(checkIfSastEnabled, 2000 * currentAttempt)
            }
        }
    }
    checkIfSastEnabled.invoke()
}

private val alarm = Alarm()

fun controlExternalProcessWithProgressIndicator(
    indicator: ProgressIndicator,
    onCancel: () -> Unit
) {
    lateinit var checkCancelled: () -> Unit
    checkCancelled = {
        if (indicator.isCanceled) {
            onCancel()
        } else {
            alarm.addRequest(checkCancelled, 100)
        }
    }
    checkCancelled.invoke()
}

fun isIacEnabled(): Boolean = true

fun isContainerEnabled(): Boolean = true

fun isFileListenerEnabled(): Boolean = pluginSettings().fileListenerEnabled

fun isNewRefactoredTreeEnabled(): Boolean = Registry.`is`("snyk.preview.new.refactored.tree.enabled", false)

fun isReportFalsePositivesEnabled(): Boolean = Registry.`is`("snyk.code.report.false.positives.enabled", false) &&
    pluginSettings().reportFalsePositivesEnabled == true

fun getWaitForResultsTimeout(): Long =
    Registry.intValue(
        "snyk.timeout.results.waiting",
        DEFAULT_TIMEOUT_FOR_SCAN_WAITING_MS
    ).toLong()

const val DEFAULT_TIMEOUT_FOR_SCAN_WAITING_MIN = 12L
val DEFAULT_TIMEOUT_FOR_SCAN_WAITING_MS =
    TimeUnit.MILLISECONDS.convert(DEFAULT_TIMEOUT_FOR_SCAN_WAITING_MIN, TimeUnit.MINUTES).toInt()

fun getSSLContext(): SSLContext {
    val trustManager = getX509TrustManager()
    val sslContext = SSLContext.getInstance("TLSv1.2")
    sslContext.init(null, arrayOf<TrustManager>(trustManager), null)
    return sslContext
}

fun getX509TrustManager(): X509TrustManager {
    val trustManagerFactory: TrustManagerFactory = TrustManagerFactory.getInstance(
        TrustManagerFactory.getDefaultAlgorithm()
    )
    trustManagerFactory.init(null as KeyStore?)
    val trustManagers: Array<TrustManager> = trustManagerFactory.trustManagers
    check(!(trustManagers.size != 1 || trustManagers[0] !is X509TrustManager)) {
        ("Unexpected default trust managers:${trustManagers.contentToString()}")
    }
    return trustManagers[0] as X509TrustManager
}

fun findPsiFileIgnoringExceptions(virtualFile: VirtualFile, project: Project): PsiFile? =
    if (!virtualFile.isValid || project.isDisposed) {
        null
    } else {
        try {
            PsiManager.getInstance(project).findFile(virtualFile)
        } catch (ignored: Throwable) {
            null
        }
    }

fun refreshAnnotationsForOpenFiles(project: Project) {
    if (project.isDisposed) return
    val openFiles = FileEditorManager.getInstance(project).openFiles
    FileContentUtil.reparseFiles(project, openFiles.asList(), true)
    openFiles.forEach {
        val psiFile = findPsiFileIgnoringExceptions(it, project)
        if (psiFile != null) {
            DaemonCodeAnalyzer.getInstance(project).restart(psiFile)
        }
    }
}

fun navigateToSource(
    project: Project,
    virtualFile: VirtualFile,
    selectionStartOffset: Int,
    selectionEndOffset: Int? = null
) {
    if (!virtualFile.isValid) return
    val psiFile = RunUtils.computeInReadActionInSmartMode(
        project,
        Computable { PsiManager.getInstance(project).findFile(virtualFile) }
    ) ?: return
    val textLength = psiFile.textLength
    if (selectionStartOffset in (0 until textLength)) {
        // jump to Source
        PsiNavigationSupport.getInstance().createNavigatable(
            project,
            virtualFile,
            selectionStartOffset
        ).navigate(false)
    } else {
        logger.warn("Navigation to wrong offset: $selectionStartOffset with file length=$textLength")
    }

    if (selectionEndOffset != null) {
        // highlight(by selection) suggestion range in source file
        if (selectionEndOffset in (0 until textLength) &&
            selectionStartOffset < selectionEndOffset
        ) {
            val editor = FileEditorManager.getInstance(project).selectedTextEditor
            editor?.selectionModel?.setSelection(selectionStartOffset, selectionEndOffset)
        } else {
            logger.warn("Selection of wrong range: [$selectionStartOffset:$selectionEndOffset]")
        }
    }
}

// `Memory leak` deceted by Jetbrains, see details here:
// https://youtrack.jetbrains.com/issue/IJSDK-979/Usage-of-ShowSettingsUtilshowSettingsDialogProject-jClassT-will-cause-Memory-leak-detected-KotlinCompilerConfigurableTab
fun showSettings(project: Project, componentNameToFocus: String, componentHelpHint: String) {
    ShowSettingsUtil.getInstance()
        .showSettingsDialog(project, SnykProjectSettingsConfigurable::class.java) {
            val componentToFocus = UIComponentFinder.getComponentByName(
                it.snykSettingsDialog.getRootPanel(),
                JComponent::class,
                componentNameToFocus
            )
            if (componentToFocus != null) {
                it.snykSettingsDialog.runBackgroundable({
                    componentToFocus.requestFocusInWindow()
                    SnykBalloonNotificationHelper
                        .showInfoBalloonForComponent(componentHelpHint, componentToFocus, true)
                }, delayMillis = 1000)
            } else {
                logger.warn("Can't find component with name: $componentNameToFocus")
            }
        }
}
