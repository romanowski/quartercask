/*
 * Copyright (c) 2016 Brendan McAdams & Thomas Lockney
 * Portions Copyright (c) Gilt Groupe, based upon their work
 * at https://github.com/gilt/sbt-aws-lambda
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package codes.bytes.quartercask.lambda

import codes.bytes.quartercask._
import com.amazonaws.services.lambda.AWSLambdaClientBuilder
import com.amazonaws.services.lambda.model.Runtime.Java8
import com.amazonaws.services.lambda.model._
import sbt._

import scala.util.Try

private [quartercask] case class LambdaParams(name: LambdaName, handlerName: HandlerName, timeout: Option[Timeout], memory: Option[Memory])

private [quartercask] case class S3Params(s3BucketId: S3BucketId, s3Key: S3Key)

private[quartercask] class AWSLambdaClient(region: Region) {

  private lazy val lambdaClient = buildAwsClient

  def deployLambda(lambdaParams: LambdaParams, roleName: RoleARN, s3Params: S3Params)(implicit log: Logger): Try[Either[CreateFunctionResult, UpdateFunctionCodeResult]] = {
    Try {


      val isNew = try {
        val getRequest = new GetFunctionRequest
        getRequest.setFunctionName(lambdaParams.name.value)
        lambdaClient.getFunction(getRequest)
        false
      } catch {
        case _: ResourceNotFoundException => true
      }

      if (isNew) {
        createNewLambda(lambdaParams, roleName, s3Params)
      } else {
        updateExistingLambda(lambdaParams, s3Params, roleName)
      }
    }
  }

  private def createNewLambda(lambdaParams: LambdaParams, roleName: RoleARN, s3Params: S3Params)(implicit log: Logger): Either[CreateFunctionResult, UpdateFunctionCodeResult] = {
    print(s"Creating new AWS Lambda function '${lambdaParams.name.value}'\n")
    val request = createNewLambdaRequest(lambdaParams, roleName, s3Params)
    val createResult = lambdaClient.createFunction(request)

    log.info(s"Created Lambda: ${createResult.getFunctionArn}\n")
    Left(createResult)
  }

  private def createNewLambdaRequest(lambdaParams: LambdaParams, roleName: RoleARN, s3Params: S3Params) = {
    val req = new CreateFunctionRequest()
    req.setFunctionName(lambdaParams.name.value)
    req.setHandler(lambdaParams.handlerName.value)
    req.setRole(roleName.value)
    req.setRuntime(Java8)

    if (lambdaParams.timeout.isDefined) req.setTimeout(lambdaParams.timeout.get.value)
    if (lambdaParams.memory.isDefined) req.setMemorySize(lambdaParams.memory.get.value)

    val functionCode = createFunctionCodeParams(s3Params)

    req.setCode(functionCode)

    req
  }

  private def updateExistingLambda(lambdaParams: LambdaParams, s3Params: S3Params, roleName: RoleARN)(implicit log: Logger): Either[CreateFunctionResult, UpdateFunctionCodeResult] = {
    print(s"Updating existing AWS Lambda function '${lambdaParams.name.value}'\n")
    val updateLambdaReq = createUpdateLambdaRequest(lambdaParams, s3Params)
    val updateCodeResult = lambdaClient.updateFunctionCode(updateLambdaReq)
    log.info(s"Successfully updated function code: ${updateCodeResult.getFunctionArn}")

    val updateFunctionConfReq = new UpdateFunctionConfigurationRequest()
    updateFunctionConfReq.setFunctionName(lambdaParams.name.value)
    updateFunctionConfReq.setHandler(lambdaParams.handlerName.value)
    updateFunctionConfReq.setRole(roleName.value)
    updateFunctionConfReq.setRuntime(Java8)
    lambdaParams.timeout.foreach { t => updateFunctionConfReq.setTimeout(t.value) }
    lambdaParams.memory.foreach { m => updateFunctionConfReq.setMemorySize(m.value) }
    val updateConfResult = lambdaClient.updateFunctionConfiguration(updateFunctionConfReq)
    log.info(s"Successfully updated function configuration: ${updateConfResult.getFunctionArn}")


    log.info(s"Updated lambda ${updateCodeResult.getFunctionArn}")
    Right(updateCodeResult)
  }

  private def createUpdateLambdaRequest(lambdaParams: LambdaParams, s3Params: S3Params): UpdateFunctionCodeRequest = {
    val req = new UpdateFunctionCodeRequest()
    req.setFunctionName(lambdaParams.name.value)
    req.setS3Bucket(s3Params.s3BucketId.value)
    req.setS3Key(s3Params.s3Key.value)
    req
  }

  private def createFunctionCodeParams(s3Params: S3Params): FunctionCode = {
    val code = new FunctionCode
    code.setS3Bucket(s3Params.s3BucketId.value)
    code.setS3Key(s3Params.s3Key.value)
    code
  }

  private def buildAwsClient = {
    val clientBuilder = AWSLambdaClientBuilder
      .standard()
      .withCredentials(AWSCredentials.provider)

    clientBuilder.setRegion(region.value)
    clientBuilder.build()
  }
}
