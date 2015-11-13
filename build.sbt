enablePlugins(DockerPlugin)

organization := "com.github.jw3.examples"
name := "gpio-microservice"
version := "0.1-SNAPSHOT"
licenses +=("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))

scalaVersion := "2.11.7"
scalacOptions += "-target:jvm-1.8"

resolvers += "jw3 at bintray" at "https://dl.bintray.com/jw3/maven"

libraryDependencies ++= {
    val akkaVersion = "2.4.0"
    val akkaStreamVersion = "1.0"

    Seq(
        "wiii" %% "awebapi" % "0.2",
        "wiii" %% "akka-injects" % "0.1",
        "gpio4s" %% "gpio4s" % "0.2",
        "gpio4s" %% "gpio4s-pi4j" % "0.1",
        "gpio4s" %% "gpiocfg" % "0.1",

        "io.spray" %% "spray-json" % "1.3.2",
        "com.typesafe" % "config" % "1.3.0",
        "net.ceedubs" %% "ficus" % "1.1.2",

        "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
        "ch.qos.logback" % "logback-classic" % "1.1.3",

        "com.typesafe.akka" %% "akka-actor" % akkaVersion,
        "com.typesafe.akka" %% "akka-stream-experimental" % akkaStreamVersion,
        "com.typesafe.akka" %% "akka-http-experimental" % akkaStreamVersion,
        "com.typesafe.akka" %% "akka-http-core-experimental" % akkaStreamVersion,
        "com.typesafe.akka" %% "akka-http-xml-experimental" % akkaStreamVersion,
        "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaStreamVersion,
        "com.typesafe.akka" %% "akka-slf4j" % akkaVersion % Runtime,

        "org.scalatest" %% "scalatest" % "2.2.5" % Test,
        "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
        "com.typesafe.akka" %% "akka-http-testkit-experimental" % akkaStreamVersion % Test
    )
}

docker <<= (docker dependsOn assembly)
dockerfile in docker := {
    val artifact = assemblyOutputPath in assembly value
    val artifactTargetPath = s"/app/${artifact.name}"
    new sbtdocker.mutable.Dockerfile {
        from("java:8")
        add(artifact, artifactTargetPath)
        copy(baseDirectory(_ / "config" / "docker.conf").value, file("app/docker.conf"))
        copy(baseDirectory(_ / "bin" / "boot.sh").value, file("app/boot.sh"))
        copy(artifact, artifactTargetPath)
        expose(2222)
        entryPoint("java", "-jar", artifactTargetPath)
    }
}

mainClass in assembly := Option("services.GpioRestService")
test in assembly := {}
assembleArtifact in assemblyPackageScala := true
assemblyMergeStrategy in assembly := {
    case m if m.toLowerCase.endsWith("manifest.mf") => MergeStrategy.discard
    case m if m.toLowerCase.matches("meta-inf.*\\.sf$") => MergeStrategy.discard
    case "reference.conf" => MergeStrategy.concat
    case _ => MergeStrategy.first
}
