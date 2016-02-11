// Copyright (C) 2015-2016 Typesafe Inc. <http://typesafe.com>

package com.typesafe.tools.sculpt

package object util {
  implicit class RegexInterpolator(sc: StringContext) {
    def r = new scala.util.matching.Regex(sc.parts.mkString, sc.parts.tail.map(_ => "x"): _*)
  }
}
