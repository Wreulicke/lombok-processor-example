package com.wreulicke.lombok.javac

import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitSuite

import com.google.common.truth.Truth
import com.google.testing.compile.JavaFileObjects
import com.google.testing.compile.JavaSourceSubjectFactory
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class VarSpec extends FunSpec {
  describe("first test") {
    it("no compile error") {
      Truth.assertAbout(JavaSourceSubjectFactory.javaSource)
        .that(JavaFileObjects.forResource("Test.java"))
        .processedWith(new ScalaProcessor)
        .compilesWithoutError()
    }
  }

}