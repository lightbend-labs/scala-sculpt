// Copyright (C) 2015-2021 Lightbend Inc. <http://lightbend.com>

package com.lightbend.tools.sculpt.model

import java.lang.StringBuilder

import spray.json._

// we didn't like the appearance of the output with either of spray-json's formatters
// (`compactPrint` and `prettyPrint`), so we rolled our own

object FullDependenciesPrinter extends JsonPrinter {

  def print(js: JsValue): String = {
    val sb = new java.lang.StringBuilder
    print(js, sb)
    sb.toString
  }

  override def print(x: JsValue, sb: StringBuilder): Unit =
    printRootArray(x.asInstanceOf[JsArray].elements, sb)

  protected def printRootArray(elements: Seq[JsValue], sb: StringBuilder): Unit = {
    sb.append("[\n  ")
    printSeq(elements, sb.append(",\n  "))(printCompact(_, sb))
    sb.append("\n]")
  }

  def printCompact(x: JsValue, sb: StringBuilder): Unit = {
    x match {
      case JsObject(x) => printCompactObject(x, sb)
      case JsArray(x)  => printCompactArray(x, sb)
      case _ => printLeaf(x, sb)
    }
  }

  protected def printCompactObject(members: Map[String, JsValue], sb: StringBuilder): Unit = {
    sb.append('{')
    printSeq(members, sb.append(", ")) { m =>
      printString(m._1, sb)
      sb.append(": ")
      printCompact(m._2, sb)
    }
    sb.append('}')
  }

  protected def printCompactArray(elements: Seq[JsValue], sb: StringBuilder): Unit = {
    sb.append('[')
    printSeq(elements, sb.append(", "))(printCompact(_, sb))
    sb.append(']')
  }
}
