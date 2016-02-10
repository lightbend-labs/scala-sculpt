// Copyright (C) 2015 Typesafe Inc. <http://typesafe.com>

package com.typesafe.tools.sculpt.model

import spray.json._
import java.lang.StringBuilder

object FullDependenciesPrinter extends JsonPrinter {
  override def print(x: JsValue, sb: StringBuilder): Unit =
    printRootArray(x.asInstanceOf[JsArray].elements, sb)

  protected def printRootArray(elements: Seq[JsValue], sb: StringBuilder) {
    sb.append("[\n  ")
    printSeq(elements, sb.append(",\n  "))(printCompact(_, sb))
    sb.append("\n]")
  }

  def printCompact(x: JsValue, sb: StringBuilder) {
    x match {
      case JsObject(x) => printCompactObject(x, sb)
      case JsArray(x)  => printCompactArray(x, sb)
      case _ => printLeaf(x, sb)
    }
  }

  protected def printCompactObject(members: Map[String, JsValue], sb: StringBuilder) {
    sb.append('{')
    printSeq(members, sb.append(", ")) { m =>
      printString(m._1, sb)
      sb.append(": ")
      printCompact(m._2, sb)
    }
    sb.append('}')
  }

  protected def printCompactArray(elements: Seq[JsValue], sb: StringBuilder) {
    sb.append('[')
    printSeq(elements, sb.append(", "))(printCompact(_, sb))
    sb.append(']')
  }
}
