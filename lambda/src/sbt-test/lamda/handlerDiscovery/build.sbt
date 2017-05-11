enablePlugins(AWSLambdaPlugin)

region := "not-important"

val testLambdas = inputKey[Unit]("testLambdas")

testLambdas := {
  val expected = Def.spaceDelimited().parsed
  val found: Seq[String] = discoverAWSLambdaClasses.value
  assert(expected == found, s"Expected: $expected but found: $found")
}