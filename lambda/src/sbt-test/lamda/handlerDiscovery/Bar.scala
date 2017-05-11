package bar

@codes.bytes.quaich.api.http.macros.LambdaHTTPApi
class BarFQN

import codes.bytes.quaich.api.http.macros._

@LambdaHTTPApi
class BarImported

import codes.bytes.quaich.api.http.macros.{LambdaHTTPApi => MyLambda}

@MyLambda
class BarRenamed