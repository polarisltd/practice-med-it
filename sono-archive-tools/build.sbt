val scala3Version = "2.13.13" //"3.3.1"

lazy val root = project
  .in(file("."))
  .settings(
    name := "sqlite-4dv-reader-3",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    unmanagedBase := baseDirectory.value / "lib",

  libraryDependencies ++= Seq(
    "org.scalameta" %% "munit" % "0.7.29" % Test,
    "org.scalatest" %% "scalatest" % "3.2.15" % Test,
    "org.xerial" % "sqlite-jdbc" % "3.40.1.0",
    "com.typesafe" % "config" % "1.4.1",
    "org.mongodb.scala" %% "mongo-scala-driver" % "5.0.0",
    "org.slf4j" % "slf4j-api" % "1.7.30",
    "ch.qos.logback" % "logback-classic" % "1.2.9"
  )

)
