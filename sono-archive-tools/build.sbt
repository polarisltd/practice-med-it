val scala3Version = "2.13.13" //"3.3.1"

lazy val root = project
  .in(file("."))
  .settings(
    name := "sqlite-4dv-reader-3",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    unmanagedBase := baseDirectory.value / "lib"
  )

  val jacksonVersion = "2.15.2"
  val swaggerVersion = "2.2.15"
  val akkaHttpVersion = "10.2.10"


val swaggerDependencies = Seq(
  "jakarta.ws.rs" % "jakarta.ws.rs-api" % "3.1.0",
  "com.github.swagger-akka-http" %% "swagger-akka-http" % "2.11.0",
  "com.github.swagger-akka-http" %% "swagger-scala-module" % "2.11.0",
  "com.github.swagger-akka-http" %% "swagger-enumeratum-module" % "2.8.0",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion,
  "io.swagger.core.v3" % "swagger-jaxrs2-jakarta" % swaggerVersion
)

val googleApiDependencies = Seq(
  "com.google.api-client" % "google-api-client" % "2.0.0",
  "com.google.oauth-client" % "google-oauth-client-jetty" % "1.34.1",
  "com.google.apis" % "google-api-services-sheets" % "v4-rev20220927-2.0.0"
)

libraryDependencies ++= Seq(
    "org.scalameta" %% "munit" % "0.7.29" % Test,
    "org.scalatest" %% "scalatest" % "3.2.15" % Test,
    "org.xerial" % "sqlite-jdbc" % "3.40.1.0",
    "com.typesafe" % "config" % "1.4.1",
    "org.mongodb.scala" %% "mongo-scala-driver" % "5.0.0",
    "org.slf4j" % "slf4j-api" % "1.8.0-beta4",
    "ch.qos.logback" % "logback-classic" % "1.5.3",
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
    "com.typesafe.play" %% "play" % "2.9.2"

  ) ++ swaggerDependencies ++ googleApiDependencies


