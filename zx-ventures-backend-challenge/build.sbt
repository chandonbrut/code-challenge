name := "zx-ventures-backend-challenge"

version := "1.0"

scalaVersion := "2.12.2"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

herokuAppName in Compile := "zx-ventures-backend-challenge"

libraryDependencies ++= Seq(
	"com.vividsolutions" % "jts" % "1.13",
	"com.vividsolutions" % "jts-io" % "1.14.0",
	"org.mongodb.scala" %% "mongo-scala-driver" % "2.1.0"
)

libraryDependencies += guice
