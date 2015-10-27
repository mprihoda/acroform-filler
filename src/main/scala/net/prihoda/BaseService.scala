package net.prihoda

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport

trait BaseService extends Protocol with SprayJsonSupport with Config with ExecutionEnv
