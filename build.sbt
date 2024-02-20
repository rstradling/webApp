val http4sVersion = "0.23.23"
val mUnitVersion = "0.7.29"
val logbackVersion = "1.2.13"
val mUnitCatsEffectVersion = "1.0.7"
val doobieVersion = "1.0.0-RC1"
val catsEffectVersion = "3.5.2"
val catsCoreVersion = "2.10.0"
val circeVersion = "0.14.5"

lazy val root = (project in file("."))
  .settings(
    assembly / mainClass := Some("com.strad.Main"),
    organization := "com.strad",
    name := "webApp",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "3.3.1",
    libraryDependencies ++= Seq(
      "org.slf4j"  % "slf4j-api" % "1.7.36",
      "org.typelevel"   %% "cats-effect"         % catsEffectVersion,
      "ch.qos.logback"  %  "logback-classic"     % logbackVersion,
      "org.http4s"      %% "http4s-ember-server" % http4sVersion,
      "org.http4s"      %% "http4s-ember-client" % http4sVersion,
      "org.http4s"      %% "http4s-circe"        % http4sVersion,
      "org.http4s"      %% "http4s-dsl"          % http4sVersion,
      "io.circe"        %% "circe-core"          % circeVersion,
      "io.circe"        %% "circe-generic"       % circeVersion,
      "org.tpolecat"    %% "doobie-core"         % doobieVersion,
      "org.tpolecat"    %% "doobie-hikari"       % doobieVersion,
      "org.tpolecat"    %% "doobie-munit"        % doobieVersion          % Test,
      "org.tpolecat"    %% "doobie-postgres"     % doobieVersion,
      "org.scalameta"   %% "munit"               % mUnitVersion           % Test,
      "org.typelevel"   %% "munit-cats-effect-3" % mUnitCatsEffectVersion % Test,
    ),
    testFrameworks += new TestFramework("munit.Framework")
  )
