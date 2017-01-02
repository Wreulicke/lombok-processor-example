name := """lombok-processor-example"""

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
    "org.javassist" % "javassist" % "3.21.0-GA",
    "cglib" % "cglib" % "3.2.4",
    "net.bytebuddy" % "byte-buddy-dep" % "1.5.10",
    "com.google.testing.compile" % "compile-testing" % "0.10",
    "junit" % "junit" % "4.1.2" % "test",
    "org.scalatest" %% "scalatest" % "3.0.0" % "test"
)

unmanagedJars in Compile := (file(System.getProperty("java.home")) / ".." / "lib" * "tools.jar").classpath

javacOptions += "-proc:none"