package io.snyk.plugin.cli

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.intellij.openapi.components.Service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.util.io.HttpRequests
import io.snyk.plugin.getApplicationSettingsStateService
import io.snyk.plugin.getCli
import io.snyk.plugin.getCliFile
import io.snyk.plugin.tail
import java.lang.String.format
import java.net.URL
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Objects.isNull
import java.util.Objects.nonNull

@Service
class CliDownloaderService(val project: Project) {

    companion object {
        val LATEST_RELEASES_URL = "https://api.github.com/repos/snyk/snyk/releases/latest"
        val LATEST_RELEASE_DOWNLOAD_URL = "https://github.com/snyk/snyk/releases/download/%s/%s"

        val NUMBER_OF_DAYS_BETWEEN_RELEASE_CHECK = 4
    }

    private var latestReleaseInfo: LatestReleaseInfo? = null

    fun requestLatestReleasesInformation(): LatestReleaseInfo? {
        val jsonResponseStr = URL(LATEST_RELEASES_URL).readText()

        latestReleaseInfo = Gson().fromJson(jsonResponseStr, LatestReleaseInfo::class.java)

        return latestReleaseInfo
    }

    fun downloadLatestRelease() {
        val latestReleasesInfo = requestLatestReleasesInformation()

        ProgressManager.getInstance().run(object: Task.Backgroundable(project, "Download", true) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = true
                indicator.pushState()

                try {
                    indicator.setText("Downloading latest Snyk CLI release...")

                    val snykWrapperFileName = Platform.current().snykWrapperFileName

                    val cliVersion = latestReleasesInfo!!.tagName

                    val url = URL(format(LATEST_RELEASE_DOWNLOAD_URL, cliVersion, snykWrapperFileName)).toString()

                    val cliFile = getCliFile()

                    if (cliFile.exists()) {
                        cliFile.delete()
                    }

                    HttpRequests
                        .request(url)
                        .productNameAsUserAgent()
                        .saveToFile(cliFile, indicator)

                    cliFile.setExecutable(true)


                    getApplicationSettingsStateService().setCliVersion(cliVersionNumbers(cliVersion))
                    getApplicationSettingsStateService().setLastCheckDate(LocalDate.now())
                } finally {
                    indicator.popState()
                }
            }
        })
    }

    fun cliSilentAutoUpdate() {
        if (isCliInstalledByPlugin() && isFourDaysPassedSinceLastCheck()) {
            val latestReleaseInfo = requestLatestReleasesInformation()

            val applicationSettingsStateService = getApplicationSettingsStateService()

            if (nonNull(latestReleaseInfo)
                && latestReleaseInfo?.tagName != null && latestReleaseInfo.tagName.isNotEmpty()
                && isNewVersionAvailable(applicationSettingsStateService.getCliVersion(), cliVersionNumbers(latestReleaseInfo.tagName))) {

                downloadLatestRelease()

                applicationSettingsStateService.setLastCheckDate(LocalDate.now())
            }
        }
    }

    fun isCliInstalledByPlugin(): Boolean {
        val cli = getCli(project)

        return !cli.checkIsCliInstalledManuallyByUser() && cli.checkIsCliInstalledAutomaticallyByPlugin()
    }

    fun isFourDaysPassedSinceLastCheck(): Boolean {
        val previousDate = getApplicationSettingsStateService().getLastCheckDate()

        if (isNull(previousDate)) {
            return true
        }

        return ChronoUnit.DAYS.between(previousDate, LocalDate.now()) >= NUMBER_OF_DAYS_BETWEEN_RELEASE_CHECK
    }

    fun isNewVersionAvailable(currentCliVersion: String, newCliVersion: String): Boolean {
        if (isNull(currentCliVersion) || currentCliVersion.isEmpty()) {
            return true
        }

        tailrec fun checkIsNewVersionAvailable(currentCliVersionNumbers: List<String>, newCliVersionNumbers: List<String>): Boolean {
            return if (currentCliVersionNumbers.isNotEmpty() && newCliVersionNumbers.isNotEmpty()) {
                val newVersionNumber = newCliVersionNumbers[0].toInt()
                val currentVersionNumber = currentCliVersionNumbers[0].toInt()

                when (val compareResult = newVersionNumber.compareTo(currentVersionNumber)) {
                    0 -> checkIsNewVersionAvailable(currentCliVersionNumbers.tail, newCliVersionNumbers.tail)
                    else -> compareResult > 0
                }
            } else {
                false
            }
        }

        return checkIsNewVersionAvailable(currentCliVersion.split('.'), newCliVersion.split('.'))
    }

    fun getLatestReleaseInfo(): LatestReleaseInfo? = this.latestReleaseInfo

    /**
     * Clear version number: v1.143.1 => 1.143.1

     * @param sourceVersion - source cli version string
     *
     * @return String
     */
    private fun cliVersionNumbers(sourceVersion: String): String = sourceVersion.substring(1, sourceVersion.length)
}

class LatestReleaseInfo(
    val id: Long,
    val url: String,
    val name: String,
    @SerializedName("tag_name") val tagName: String) {
}
