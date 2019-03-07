import com.typesafe.sbt.digest.Import.digest
import com.typesafe.sbt.web.Import.pipelineStages
import com.typesafe.sbt.web.SbtWeb
import play.routes.compiler.StaticRoutesGenerator
import play.sbt.routes.RoutesKeys.routesGenerator
import sbt.Keys._
import sbt._
import scoverage.ScoverageKeys
import uk.gov.hmrc.SbtAutoBuildPlugin
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._
import uk.gov.hmrc.versioning.SbtGitVersioning
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion
import uk.gov.hmrc.SbtArtifactory

trait MicroService {

  val appName: String

  lazy val appDependencies: Seq[ModuleID] = ???
  lazy val plugins: Seq[Plugins] = Seq.empty
  lazy val playSettings: Seq[Setting[_]] = Seq(
    pipelineStages := Seq(digest)
  )

  val excludedPackages = Seq[String](
    "<empty>;Reverse.*",
    "app.*",
    "uk.gov.hmrc.nisp.auth.*",
    "uk.gov.hmrc.nisp.views.*",
    "uk.gov.hmrc.nisp.config.*",
    "uk.gov.hmrc.BuildInfo"
  )

  lazy val scoverageSettings = {
  Seq(
    ScoverageKeys.coverageExcludedPackages := excludedPackages.mkString(";"),
    ScoverageKeys.coverageMinimum := 80,
    ScoverageKeys.coverageFailOnMinimum := false,
    ScoverageKeys.coverageHighlighting := true
  )
}

lazy val microservice = Project(appName, file("."))
  .enablePlugins(Seq(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin, SbtArtifactory, SbtWeb) ++ plugins: _*)
  .settings(publishingSettings: _*)
  .settings(playSettings ++ scoverageSettings: _*)
  .settings(
    scalaVersion := "2.11.11",
    libraryDependencies ++= appDependencies,
    dependencyOverrides +=  "uk.gov.hmrc" %% "play-config" % "7.3.0",
    retrieveManaged := true,
    resolvers ++= Seq(
      Resolver.bintrayRepo("hmrc", "releases"),
      Resolver.jcenterRepo,
      "hmrc-releases" at "https://artefacts.tax.service.gov.uk/artifactory/hmrc-releases/"
    ),
    evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
    routesGenerator := StaticRoutesGenerator
  )
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(resolvers ++= Seq(Resolver.jcenterRepo))
  .settings(majorVersion := 9)
}