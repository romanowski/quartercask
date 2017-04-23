import sbt.Keys.publishTo

val projectVersion          = "0.0.1-SNAPSHOT"
val projectOrg              = "codes.bytes"
val awsSdkVersion           = "1.11.119"
val alexaSkillsVersion      = "1.1.2"

dependencyOverrides += "org.json4s" %% "json4s-native" % "3.2.10"

lazy val commonSettings = Seq(
  organization := projectOrg,
  version := projectVersion,
  retrieveManaged := true,

  scalacOptions := Seq(
    "-encoding", "UTF-8",
    "-target:jvm-1.7",
    "-deprecation",
    "-language:_"
  ),

  bintrayOrganization := Some("quaich"),
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),

  fork in (Test, run) := true,

  addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.4" exclude("org.json4s", "json4s-native"))
)


lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "sbt-quartercask",
    publishArtifact := false,
    publishTo := Some(Resolver.file("Unused transient repository", file("target/unusedrepo"))),
    bintrayRelease := {}
  ).
  aggregate(
    lambda, util, apiGateway, alexaSkills
  )

lazy val util = (project in file("util")).
  settings(commonSettings: _*).
  settings(
    name := "sbt-quartercask-util",
    sbtPlugin := true,
    libraryDependencies ++= Seq(
      "com.amazonaws"  % "aws-java-sdk-iam"         % awsSdkVersion,
      "com.amazonaws"  % "aws-java-sdk-s3"          % awsSdkVersion
    )
  )

lazy val lambda = (project in file("lambda")).
  settings(commonSettings: _*).
  settings(
    name := "sbt-quartercask-lambda",
    sbtPlugin := true,
    libraryDependencies ++= Seq(
      "com.amazonaws"  % "aws-java-sdk-lambda"      % awsSdkVersion
    )
  ).
  dependsOn(util)

lazy val apiGateway = (project in file("api-gateway")).
  settings(commonSettings: _*).
  settings(
    name := "sbt-quartercask-api-gateway",
    sbtPlugin := true,
    libraryDependencies ++= Seq(
      "com.amazonaws"  % "aws-java-sdk-api-gateway" % awsSdkVersion
    )
  ).
  dependsOn(util)

lazy val alexaSkills = (project in file("alexa-skills")).
  settings(commonSettings: _*).
  settings(
    name := "sbt-quartercask-alexa-skills",
    sbtPlugin := true,
    libraryDependencies ++= Seq(
      "com.amazon.alexa" % "alexa-skills-kit" % alexaSkillsVersion
    )
  ).
  dependsOn(util)
