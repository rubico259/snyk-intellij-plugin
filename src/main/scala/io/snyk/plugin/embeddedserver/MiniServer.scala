package io.snyk.plugin.embeddedserver

import java.net.{URI, URL}

import fi.iki.elonen.NanoHTTPD

import io.snyk.plugin.datamodel.SnykVulnResponse
import ColorProvider.RichColor
import HandlebarsEngine.RichTemplate
import io.snyk.plugin.ui.state.SnykPluginState

/**
  * A low-impact embedded HTTP server, built on top of the NanoHTTPD engine.
  * Minimal routing to process `.hbs` files via handlebars.
  *
  * Special logic to show an interim animation if the requested URL needs a new scan,
  * then asynchronously show the requested URL once the scan is complete
  */
class MiniServer(
  protected val pluginState: SnykPluginState,
  colorProvider: ColorProvider,
  port0: Int = 0
) extends NanoHTTPD(port0)
  with ServerFilters
  with ServerResponses { // 0 == first available port

  import NanoHTTPD._
  import pluginState.apiClient

  start(SOCKET_READ_TIMEOUT, false)
  val port: Int = this.getListeningPort

  val rootUrl = new URL(s"http://localhost:$port")
  println(s"Mini-server on $rootUrl \n")

  val handlebarsEngine = new HandlebarsEngine

  protected def navigateTo(path: String, params: ParamSet): Unit = pluginState.navigator.navigateTo(path, params)

//  val defaultScanning = "/anim/scanning/scanning.hbs"
  /** The default URL to show when async scanning if an explicit `interstitial` page hasn't been requested **/
  val defaultScanning = "/html/scanning.hbs"

  /**
    * Core NanoHTTPD serving method; Parse params for our own needs, extract the URI, and delegate to our own `serve`
    */
  override def serve(session: IHTTPSession): Response = {
    println(s"miniserver serving $session")
    val uri = URI.create(session.getUri).normalize()
    val params = ParamSet.from(session)
    val path = uri.getPath
    println(s"miniserver routing $path")
    try {
      router.route(uri, params) getOrElse notFoundResponse(path)
    } catch { case ex: Exception =>
      ex.printStackTrace()
      newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", ex.toString)
    }
  }

  private lazy val router = HttpRouter(
    "/assets/*"              -> serveStatic,
    "/partials/*"            -> serveHandlebars,
    "/perform-login"         -> performLogin,
    "/vulnerabilities"       -> serveVulns,
    "/login-required"        -> simpleServeHandlebars,
    "/logging-in"            -> simpleServeHandlebars,
    "/no-project-available"  -> simpleServeHandlebars,
    "/scanning"              -> simpleServeHandlebars
  )

  /**
    * A tiny helper for handling /<path> from the template /html/<path>.hbs
    */
  def simpleServeHandlebars(path: String)(params: ParamSet): Response = serveHandlebars(s"/html${path}.hbs")(params)

  def serveStatic(path: String)(params: ParamSet): Response = {
    val mime = MimeType of path
    println(s"miniserver serving static http://localhost:$port$path as $mime")
    val conn = WebInf.instance.openConnection(path)
    newFixedLengthResponse(Response.Status.OK, mime, conn.getInputStream, conn.getContentLengthLong)
  }

  def performLogin(path: String)(params: ParamSet): Response = {
    println("Performing login")
    val redir = asyncAuthAndRedirectTo("/vulnerabilities", "/vulnerabilities", params)
    redirectTo("/logging-in")
  }

  val serveVulns =
    requireAuth {
      requireProjectId {
        requireScan {
          url => serveHandlebars("/html/vulns.hbs")
        }
      }
    }

  def serveHandlebars(path: String)(params: ParamSet): Response = {
    println(s"miniserver serving handlebars template http://localhost:$port$path")
    println(s"params = $params")

    val template = handlebarsEngine.compile(path)
    def latestScanResult = pluginState.latestScanForSelectedProject getOrElse SnykVulnResponse.empty

    val ctx = Map.newBuilder[String, Any]

    ctx ++= params.contextMap
    ctx ++= colorProvider.toMap.mapValues(_.hexRepr)
    ctx += "currentProject" -> pluginState.selectedProjectId.get
    ctx += "projectIds" -> pluginState.rootProjectIds
    ctx += "miniVulns" -> latestScanResult.miniVulns.sortBy(_.spec)
    ctx += "vulnerabilities" -> latestScanResult.vulnerabilities

    //TODO: should these just be added to state?
    val paramFlags = params.all("flags").map(_.toLowerCase -> true).toMap
    ctx += "flags" -> (paramFlags ++ pluginState.flags.asStringMap)
    ctx += "localhost" -> s"http://localhost:$port"
    ctx += "apiAvailable" -> apiClient.isAvailable

    val body = template render ctx.result()
    newFixedLengthResponse(Response.Status.OK, "text/html", body)
  }

}
