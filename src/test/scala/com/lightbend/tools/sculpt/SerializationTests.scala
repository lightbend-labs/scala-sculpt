// Copyright (C) 2015-2020 Lightbend Inc. <http://lightbend.com>

package com.lightbend.tools.sculpt

import model._
import spray.json._

class SerializationTests extends munit.FunSuite {

  // JSON -> JSValue -> JSON
  def roundTripThroughJsonASTs(sample: Sample): Unit = {
    assert(sample.json ==
      FullDependenciesPrinter.print(sample.json.parseJson))
  }

  // JSON -> JSValue -> Seq[FullDependency] -> JSValue -> JSON
  def roundTripThroughModel(sample: Sample): Unit = {
    import ModelJsonProtocol._
    val dependencies =
      sample.json.parseJson.convertTo[Seq[FullDependency]]
    assert(sample.json ==
      FullDependenciesPrinter.print(dependencies.toJson))
  }

  for (sample <- Samples.samples) {
    test(s"${sample.name}: through ASTs") {
      roundTripThroughJsonASTs(sample)
    }
    test(s"${sample.name}: through model") {
      roundTripThroughModel(sample)
    }
  }

}
