package foo

import scala.annotation.Annotation
import scala.annotation.StaticAnnotation

class LambdaHTTPApi extends StaticAnnotation

@LambdaHTTPApi
class Foo