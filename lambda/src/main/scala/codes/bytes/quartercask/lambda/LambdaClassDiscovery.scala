package codes.bytes.quartercask.lambda

import sbt.inc.Analysis
import xsbti.api._

private[lambda] object LambdaClassDiscovery {
  def perform(analysis: Analysis): Seq[String] ={
    val classes = for{
      (_, source) <- analysis.apis.internal
      definition <- source.api().definitions()
      if definition.annotations().exists(isHttpApiAnnotation)
    } yield definition.name()

    classes.toSeq
  }


  private val annotationPackage = "codes.bytes.quaich.api.http.macros"
  private val annotationName = "LambdaHTTPApi"
  private val annotationPackageType = new Singleton(new Path(annotationPackage.split('.').map(new Id(_)) :+ new This()))
  private val annotationFQN = new Projection(annotationPackageType, annotationName)

  private def isHttpApiAnnotation(annotation: Annotation): Boolean =
    annotation.base() == annotationFQN
}
