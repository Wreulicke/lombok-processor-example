package com.wreulicke.lombok.javac

import org.scalatest.FunSpec
import com.google.testing.compile.JavaSourceSubjectFactory
import com.google.common.truth.Truth
import com.google.testing.compile.JavaFileObjects
import java.net.URL
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

class VarSpec extends FunSpec {
  describe("first test") {
    Truth.assertAbout(JavaSourceSubjectFactory.javaSource)
    .that(JavaFileObjects.forResource("Test.java"))
    .processedWith(new ScalaProcessor)
    .compilesWithoutError()
  }
  
}