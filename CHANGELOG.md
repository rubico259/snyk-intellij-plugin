# Snyk Changelog

## [2.4.34]

### Fixed
- All failed scan results for artifacts(file, image) are now shown in the results(Tree) too (right after successful scan results).
- Open Source Scan results annotations are shown for Gradle(Groovy) dependencies.

### Added
- Navigation to the Editor for the Open Source Scan results and all failed artifact's scan results.

## [2.4.33]

### Fixed
- Unify names for Types of Scan (Snyk Products) in different places of UI
- Other minor UI improvements (including updated Severity icons) 

### Changed
- Separation of **Filtering**(for results in the Tree) and **Enablement**(in the Settings)for: 
  - Types of Scan (Snyk Products)
  - Severities.

## [2.4.32]

### Fixed

- tweak and update Snyk Code communication with server:
  * encode and compress POST/PUT requests
  * update fallbacks for supported extensions and configFiles
  * do not proceed (send) files if only configFiles presence
 
### Changed
- Show specific description when no IaC or OSS files found but corresponded scan performed

## [2.4.31]

### Fixed

- Empty (or cancelled request for) proxy credentials
- `IllegalStateException` when no `Document` found for invalid(obsolete) file
- For Snyk Code multi-file issues code snippet in some data flow steps was shown from wrong file
- Correctly show some messages on macOS: Terms and Condition message on Auth panel and Container specific message for tree node
- Make re-try attempts for most Snyk Code api calls if not succeed for any reason (except 401 - auth failed)

## [2.4.30]

### Fixed

- URL encode proxy credentials
- Honoring user's product selection on welcome screen before first ever scan
- Proper message when Code product is disabled due to unreachable org settings on backend
- Auth screen has priority to be shown in case of invalid/empty token
- Code scan endpoint url was internally corrupted after any Snyk settings change

## [2.4.29]

### Fixed

- AlreadyDisposedException when annotations async refreshed for disposed project
- `IllegalArgumentException: wrong column` for issue's description panel creation
- failed psiFile search for invalid virtualFile
- `InvocationTargetExceptions` by extracting caches to separate class

## [2.4.28]

### Changed
- Enable HTTP request logging when Snyk Code logger in debug mode

### Fixed
- generic .dcignore file creation failure and exceptions

## [2.4.27]

### Changed
- Snyk Code: add support for Single Tenant setups
- Update organization setting tooltip text to clarify expected value.

### Fixed
- Avoiding IllegalArgumentException and IndexOutOfBoundsException due to accessing out-of-bound offset.

## [2.4.26]

### Changed

- Updated Snyk Code API to 2.3.1 (limit file size to 1 MB)
- Provide link to [Privacy Policy](https://snyk.io/policies/privacy/) and [Terms of Service](https://snyk.io/policies/terms-of-service/) on Welcome screen.
### Fixed

- Avoiding IncorrectOperationException by remove direct project service's requests.
- Avoid ClassNotFoundException due to not bundled Json support in some IDE.
- Fix possible duplicates and missing annotations for Snyk Code

## [2.4.25]

### Fixed

- Sanitise TextRange in file before accessing it.
- Request re-authentication from Snyk Code scan if token found to be invalid.

## [2.4.24]

### Fixed
- Disable force [auto-save in annotators](https://github.com/snyk/snyk-intellij-plugin/issues/324)

## [2.4.23]

### Fixed
- Snyk Code: make analysis retrieval more resilient to server/net errors.
- Snyk Code: add analysis context to improve analysis retrieval.
- Usage of kotlin-plugin specific method removed (cause `ClassNotFoundException: org.jetbrains.kotlin.psi.psiUtil`)

## [2.4.22]

### Changed
- Provide an additional info to the “Organization” setting

### Fixed

- Split caches update to be performed independently per product to avoid cross-affection if any failed.
- Container scan now extract images from Helm generated k8s yaml with image names inside quotes.
- Container scan should distinct image names when calling CLI.
- Container scan should add annotations for all duplicated images.
- Container scan should correctly proceed images with registry hostname in the name.
- Container scan should extract images with `port` and `tag` as well as `digest` in the path.
- Avoiding errors due to VirtualFile validity check while PsiFile obtaining.

## [2.4.21]

## [2.4.20]

### Changed
- Check Snyk Code enablement using configured organization from settings.
- Container specific messages when Container root tree node is selected.

### Fixed

- avoid any IDE internal InvocationTargetException for project services
- Container results/images cache was not updated on full/partial file content deletion.

## [2.4.19]

### Changed

- Renamed plugin to `Snyk Security - Code, Open Source, Container, IaC Configurations`
- Disable Snyk Code upload when Local Code Engine is enabled

### Fixed

- Snyk Vulnerability Database issue url resulting in 404 when opened from details panel.

## [2.4.18]

### Changed

- Snyk Open Source: added editor annotations for Maven, NPM, and Kotlin Gradle
- Snyk Open Source: added quickfix capability for package managers
- Snyk Code: Annotations if plugins for the language are installed
- Add amplitude events to quickfix display and invocations

### Fixed

- Fix Container: invalid token shows error and does not redirect to Auth panel
- Fix Container: should handle case if no images in project found
- Fix Container: node still showing last results even if disabled
- Improved Snyk Container image parsing in K8S files

## [2.4.17]

### Changed

- 2022.2 EAP support

### Fixed

- Fix IndexOutOfBoundsException during line number requests

## [2.4.16]

### Fixed

- Fix exception in IaC and Container annotators

## [2.4.15]

### Fixed

- Recognition of k8s YAML files started with `---`
- Caret is wrongly jumping to the last selected line in the Tree when editing file with issues

## [2.4.14]

### Changed

- Make Snyk Container generally available
- Make Snyk Infrastructure as Code generally available

## [2.4.13]

### Changed

- Use IntelliJ http(s) proxy settings for all remote connections
- Ignoring issues in Snyk IaC now ignores the instance of the issue instead of all instances of the issue type.

### Fixed

- Caches update and cleaning
- InvocationTargetException on project-less authentication
- Memory leak in Authentication panel

## [2.4.12]

### Fixed

- align IaC description panel inner indentations

### Changed

- needs new Snyk CLI executable for ignore functionality
- ignore issue now provides path to CLI instead of ignoring an issue project-wide

## [2.4.11]

### Changed

- enable new onboarding workflow for all users
- feedback link with new address

### Fixed

- don't delete CLI before download has finished successfully
- don't show balloon notifications if a Snyk product does not find supported files
- UI fixes for detail panel in Snyk view
- recreate API client on token change for Snyk Advisor
- several other bug fixes

## [2.4.10]

### Fixed

- avoid AlreadyDisposedException due to Project disposal before using project's service
- ignore exceptions in PsiManager.findFile()

## [2.4.9]

### Changed

- updated README with Log4Shell detection note

## [2.4.8]

### Fixed

- don't show an error, when no supported package manager was found for Snyk OSS
- use Snyk Code API 2.2.1, gives support to automatically handling empty files
- updated dependencies

## [2.4.7]

### Changed

- use new Snyk Code API
- remove scan reminder from experiment

## [2.4.6]

### Changed

- new tool window icon
- new experimental welcome workflow

## [2.4.5]

### Fixed

- run CLI download retries in background instead of UI thread
- validate CLI download with sha-256
- allow Snyk Code scan for multi-modules project (cause IllegalStateException before)

### Changed

- feedback link and message update

## [2.4.4]

### Fixed

- Handle errors when downloading Snyk CLI

### Changed

- Send missing analytic events by plugin install/uninstall
- Rename IssueIsViewed analytic event to IssueInTreeIsClicked
- Show links (Introduced through) only for NPM packages
- Create Sentry release on plugin release to be able to assign bugs to be fixed in next release
- Download Snyk CLI from static.snyk.io instead of GitHub releases
- Use TLS 1.2 as default to communicate with Snyk API

## [2.4.3]

### Fixed

- Show correct result in the tree when Snyk Code scan was stopped by user
- Send analytic events correctly when triggering Snyk Code analysis

### Changed

- Increase timeout for Snyk Code scan to 12 minutes. Configurable via 'snyk.timeout.results.waiting' registry key.
- Make plugin compatible with 2021.3 version

## [2.4.2]

### Changed

- Require restarting the IDE when updating or uninstalling the
  plugin ([GH-182](https://github.com/snyk/snyk-intellij-plugin/issues/182))

### Fixed

- Remove IntelliJ API methods that are scheduled for removal in 2021.3

## [2.4.1]

### Fixed

- Fix ClassCastException when updating the plugin without rebooting IDE

## [2.4.0]

### Added

- Allow submitting error reports anonymously to Sentry when exceptions occur

### Changed

- Increase performance when collecting files for Snyk Code

### Fixed

- Fix exception when Advisor analyse in-memory files ([GH-161](https://github.com/snyk/snyk-intellij-plugin/issues/161))
- Fix wrong cache invalidation for OSS results when modifying non-manifest files

## [2.3.0]

### Added

- Add Advisor support for Python packages in requirements.txt
- Show welcome notification after first plugin install
- Run `runPluginVerifier` task as part of CI workflow

### Changed

- Remove logo image from product selection panel
- Update Iteratively tracking plan for Advisor events

### Fixed

- Fix exception by downloading CLI when no GitHub release info available
- Fix error when initializing Iteratively library

## [2.2.2]

### Added

- Download CLI if needed when starting scans

### Fixed

- Fix exception by empty CLI output ([GH-153](https://github.com/snyk/snyk-intellij-plugin/issues/153))
- Fix error when parsing malformed JSON produced by CLI
- Disable `Run scan` action during `CLI download` task
- Fix cancelling action for `CLI download` task
- Fix issue with partially downloaded CLI file

## [2.2.1]

### Changed

- Make plugin compatible with 2021.2 version

## [2.2.0]

### Added

- [Advisor](https://snyk.io/advisor) support for NPM packages in package.json

## [2.1.9]

### Fixed

- Fix ProcessNotCreatedException when running OSS scans and CLI is still downloading
- Allow authentication while CLI is still downloading

## [2.1.8]

### Added

- Notify user in case of network/connection problem
- Welcome(onboarding) screen simplification by removing interactive alerts
- User feedback request
- Display auth link for possibility of manual open/copy into browser

### Changed

- Reduce network attempts to ask for SAST feature

### Fixed

- Exception(SunCertPathBuilderException) in case of custom/invalid certificate usage
- Welcome and Auth screen was missed to display in some cases
- Run action availability before auth passed

## [2.1.7]

### Fixed

- Set maximal attempts for asking for SAST feature

## [2.1.6]

### Fixed

- Fix [Exception when Settings panel invoked](https://github.com/snyk/snyk-intellij-plugin/issues/121)

## [2.1.5]

### Changed

- Check if Snyk Code (SAST) enabled for organisation on server side

### Fixed

- Consider `ignoreUnknownCA` option for all external network calls
- Fix Retrofit creation Exception for invalid endpoint

## [2.1.4]

### Changed

- Support critical severity level

### Fixed

- Consider `ignoreUnknownCA` option for Snyk Code
- Fix an error when displaying license issues

## [2.1.3]

### Changed

- Show OSS results as `obsolete` when it's outdated
- Fix errors on Project close while scan is running
- Fix and improve .dcignore and .gitignore parsing and usage
- Bugfix for `missing file parameter` exception

## [2.1.2]

### Changed

- Make plugin compatible with 2021.1 version

## [2.1.1]

### Changed

- Update plugin description marketplace page

## [2.1.0]

### Added

- Integrate Snyk Code product
- Simplify authentication process
- Add results filtering in Snyk tool window
- Add results caching to improve UI experience

### Changed

- Rework and add missing icons in the tree component

### Fixed

- Remove duplicated vulnerability reporting in the tree component

## [2.0.4]

### Fixed

- Fix handling custom endpoint option

## [2.0.3]

### Added

- Allow specifying CLI working directory for project submodules
- Improve integration names between different JetBrains IDEs

### Changed

- Change since/until build to `202-203.*`

### Fixed

- Split CLI additional arguments correctly

## [2.0.2]

### Added

- Add icons for supported package managers

## [2.0.1]

### Added

- Propagate integration name for JetBrain IDEs

### Fixed

- Hide API token in configuration settings

## [2.0.0]

### Added

- Use IntelliJ tree component to display vulnerabilities
- Use GitHub Actions for build and release process

### Fixed

- Remove JavaFX component for displaying vulnerabilities
- Remove Travis CI integration
