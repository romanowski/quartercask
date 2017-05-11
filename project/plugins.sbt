addSbtPlugin("me.lessis" % "bintray-sbt" % "0.3.0")

resolvers += Resolver.jcenterRepo
libraryDependencies += "org.scala-sbt" % "scripted-plugin" % sbtVersion.value