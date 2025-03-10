package icons

import com.intellij.openapi.util.IconLoader.getIcon
import io.snyk.plugin.Severity
import javax.swing.Icon
import javax.swing.ImageIcon

object SnykIcons {
    private fun getIconFromResources(name: String): ImageIcon = ImageIcon(this::class.java.getResource(name))

    @JvmField
    val TOOL_WINDOW = getIcon("/icons/snyk-dog.svg")

    val LOGO = getIcon("/icons/logo_snyk.png")

    val VULNERABILITY_16 = getIconFromResources("/icons/vulnerability_16.png")
    val VULNERABILITY_24 = getIconFromResources("/icons/vulnerability.png")

    val OPEN_SOURCE_SECURITY = getIcon("/icons/oss.svg")
    val OPEN_SOURCE_SECURITY_DISABLED = getIcon("/icons/oss_disabled.svg")
    val SNYK_CODE = getIcon("/icons/code.svg")
    val SNYK_CODE_DISABLED = getIcon("/icons/code_disabled.svg")
    val IAC = getIcon("/icons/iac.svg")
    val IAC_DISABLED = getIcon("/icons/iac_disabled.svg")
    val CONTAINER = getIcon("/icons/container.svg", SnykIcons::class.java)
    val CONTAINER_DISABLED = getIcon("/icons/container_disabled.svg", SnykIcons::class.java)
    val CONTAINER_IMAGE = getIcon("/icons/container_image.svg", SnykIcons::class.java)
    val CONTAINER_IMAGE_24 = getIcon("/icons/container_image_24.svg", SnykIcons::class.java)

    val GRADLE = getIcon("/icons/gradle.svg")
    val MAVEN = getIcon("/icons/maven.svg")
    val NPM = getIcon("/icons/npm.svg")
    val PYTHON = getIcon("/icons/python.svg")
    val RUBY_GEMS = getIcon("/icons/rubygems.svg")
    val YARN = getIcon("/icons/yarn.svg")
    val SBT = getIcon("/icons/sbt.svg")
    val GOlANG_DEP = getIcon("/icons/golangdep.svg")
    val GO_VENDOR = getIcon("/icons/govendor.svg")
    val GOLANG = getIcon("/icons/golang.svg")
    val NUGET = getIcon("/icons/nuget.svg")
    val PAKET = getIcon("/icons/paket.svg")
    val COMPOSER = getIcon("/icons/composer.svg")
    val LINUX = getIcon("/icons/linux.svg")
    val DEB = getIcon("/icons/deb.svg")
    val APK = getIcon("/icons/apk.svg")
    val COCOAPODS = getIcon("/icons/cocoapods.svg")
    val RPM = getIcon("/icons/rpm.svg")
    val DOCKER = getIcon("/icons/docker.svg")

    private val CRITICAL_SEVERITY_16 = getIcon("/icons/severity_critical_16.svg")
    private val CRITICAL_SEVERITY_32 = getIcon("/icons/severity_critical_32.svg")
    private val HIGH_SEVERITY_16 = getIcon("/icons/severity_high_16.svg")
    private val HIGH_SEVERITY_32 = getIcon("/icons/severity_high_32.svg")
    private val LOW_SEVERITY_16 = getIcon("/icons/severity_low_16.svg")
    private val LOW_SEVERITY_32 = getIcon("/icons/severity_low_32.svg")
    private val MEDIUM_SEVERITY_16 = getIcon("/icons/severity_medium_16.svg")
    private val MEDIUM_SEVERITY_32 = getIcon("/icons/severity_medium_32.svg")

    fun getSeverityIcon(severity: Severity, iconSize: IconSize = IconSize.SIZE16): Icon {
        return when (severity) {
            Severity.CRITICAL -> when (iconSize) {
                IconSize.SIZE16 -> CRITICAL_SEVERITY_16
                IconSize.SIZE32 -> CRITICAL_SEVERITY_32
            }
            Severity.HIGH -> when (iconSize) {
                IconSize.SIZE16 -> HIGH_SEVERITY_16
                IconSize.SIZE32 -> HIGH_SEVERITY_32
            }
            Severity.MEDIUM -> when (iconSize) {
                IconSize.SIZE16 -> MEDIUM_SEVERITY_16
                IconSize.SIZE32 -> MEDIUM_SEVERITY_32
            }
            Severity.LOW -> when (iconSize) {
                IconSize.SIZE16 -> LOW_SEVERITY_16
                IconSize.SIZE32 -> LOW_SEVERITY_32
            }
            else -> when (iconSize) {
                IconSize.SIZE16 -> VULNERABILITY_16
                IconSize.SIZE32 -> VULNERABILITY_24
            }
        }
    }

    enum class IconSize {
        SIZE16, SIZE32
    }
}
