package io.snyk.plugin.services.download

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.snyk.plugin.cli.Platform
import io.snyk.plugin.mockCliDownload
import io.snyk.plugin.pluginSettings
import io.snyk.plugin.services.SnykApplicationSettingsStateService
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import junit.framework.TestCase.fail
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files

class CliDownloaderTest {
    @Before
    fun setUp() {
        unmockkAll()
        mockkStatic("io.snyk.plugin.UtilsKt")
        every { pluginSettings() } returns SnykApplicationSettingsStateService()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `calculateSha256 should verify the SHA of a given file and return false if no match`() {
        val filePath = "src/test/resources/dummy-binary"
        val expectedSha = "2b418a5d0573164b4f93188fc94de0332fc0968e7a8439b01f530a4cdde1dcf2"
        val bytes = Files.readAllBytes(File(filePath).toPath())

        assertEquals(expectedSha, CliDownloader().calculateSha256(bytes))
    }

    @Test
    fun `should refer to snyk static website as base url`() {
        assertEquals("https://static.snyk.io", CliDownloader.BASE_URL)
    }

    @Test
    fun `should download version information from base url`() {
        assertEquals("${CliDownloader.BASE_URL}/cli/latest/version", CliDownloader.LATEST_RELEASES_URL)
    }

    @Test
    fun `should download cli information from base url`() {
        assertEquals(
            "${CliDownloader.BASE_URL}/cli/latest/${Platform.current().snykWrapperFileName}",
            CliDownloader.LATEST_RELEASE_DOWNLOAD_URL
        )
    }

    @Test
    fun `should download sha256 from base url`() {
        assertEquals(
            "${CliDownloader.BASE_URL}/cli/latest/${Platform.current().snykWrapperFileName}.sha256",
            CliDownloader.SHA256_DOWNLOAD_URL
        )
    }

    @Test
    fun `should not delete file if checksum verification fails`() {
        val testFile = createTempFile()
        testFile.deleteOnExit()
        val dummyContent = "test test test".toByteArray()
        Files.write(testFile.toPath(), dummyContent)
        val cut = CliDownloader()
        val expectedSha = cut.calculateSha256("wrong sha".toByteArray())

        mockCliDownload()

        try {
            cut.downloadFile(testFile, expectedSha, mockk(relaxed = true))
            fail("Should have thrown ChecksumVerificationException")
        } catch (e: ChecksumVerificationException) {
            assertTrue("testFile should still exist, as $e was thrown", testFile.exists())
        } finally {
            testFile.delete()
        }
    }
}
