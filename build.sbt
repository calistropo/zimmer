import Dependencies._

lazy val root = (project in file("."))
  .enablePlugins(DockerPlugin)
  .settings(
    inThisBuild(List(
      organization := "com.example",
      scalaVersion := "2.12.3",
      version := "0.2.0-SNAPSHOT"
    )),
    name := "Hello",
    dockerfile in docker := {
      val jarFile: File = sbt.Keys.`package`.in(Compile, packageBin).value
      val classpath = (managedClasspath in Compile).value
      val mainclass = mainClass.in(Compile, packageBin).value.getOrElse(sys.error("Expected exactly one main class"))
      val jarTarget = s"/app/${jarFile.getName}"
      // Make a colon separated classpath with the JAR file
      val classpathString = classpath.files.map("/app/" + _.getName)
        .mkString(":") + ":" + jarTarget
      new Dockerfile {
        // Base image
        from("java")
        // Add all files on the classpath
        add(classpath.files, "/app/")
        // Add the JAR file
        add(jarFile, jarTarget)
        // On launch run Java with the classpath and the main class
        entryPoint("java", "-cp", classpathString, mainclass)
      }
    },
    libraryDependencies ++= Seq(scalaTest % Test,
      "com.typesafe.akka" %% "akka-stream" % "2.5.6",
      "com.typesafe.akka" %% "akka-http" % "10.0.10",
      "com.typesafe.play" %% "play-json" % "2.6.6",
      "com.typesafe.akka" %% "akka-stream-kafka" % "0.17"
    )
  )
