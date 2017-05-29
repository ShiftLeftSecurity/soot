name := "soot-shiftleft"
organization := "ca.mcgill.sable"
version := "2016-07-28-SNAPSHOT"

javaSource in Compile := baseDirectory.value/"src"
unmanagedSourceDirectories in Compile += baseDirectory.value/"generated/singletons"
unmanagedSourceDirectories in Compile += baseDirectory.value/"generated/sablecc"
unmanagedSourceDirectories in Compile += baseDirectory.value/"generated/options"
unmanagedSourceDirectories in Compile += baseDirectory.value/"generated/jastadd"
javaSource in Test := baseDirectory.value/"tests"
resourceDirectory in Compile := baseDirectory.value/"src"
includeFilter in (Compile, unmanagedResources) := "*.dat" || "baf.scc" || "jimple.scc" || "make-singletons.xsl" || "singletons.xml"

libraryDependencies ++= Seq(
      "org.smali" % "dexlib2" % "2.2b4",
      "org.ow2.asm" % "asm-debug-all" % "5.1",
      "org.javassist" % "javassist" % "3.18.2-GA",
      "xmlpull" % "xmlpull" % "1.1.3.4d_b4_min",
      "org.jboss" % "jboss-common-core" % "2.5.0.Final",
      "org.apache.ant" % "ant" % "1.9.7",
      "heros" % "heros" % "0.0.1-b6",
      "ca.mcgill.sable" % "polyglot" % "2006",
      "ca.mcgill.sable" % "jasmin" % "2016-07-27",
      "ca.mcgill.sable" % "axmlprinter2" % "2016-07-27",
    
    //<!-- Testing dependencies -->

      "junit" % "junit" % "4.11" % Test,
      "org.hamcrest" % "hamcrest-all" % "1.3" % Test,
      "org.mockito" % "mockito-all" % "1.10.8" % Test,
      "org.powermock" % "powermock-mockito-release-full" % "1.6.1" % Test
)

resolvers ++= Seq(
  "Bedatadriven for SOOT dependencies" at "https://nexus.bedatadriven.com/content/groups/public"
)
updateOptions := updateOptions.value.withLatestSnapshots(false)
updateOptions := updateOptions.value.withCachedResolution(true)
publishTo := {
  val jfrog = "https://shiftleft.jfrog.io/shiftleft/"
  if (isSnapshot.value)
    Some("snapshots" at jfrog + "libs-snapshot-local")
  else
    Some("releases"  at jfrog + "libs-release-local")
}

retrieveManaged := true
publishMavenStyle := true
crossPaths := false

// This disables java doc generation.
mappings in (Compile, packageDoc) := Seq()
