package com.wreulicke.sandbox;

import org.junit.Test;

import com.google.common.truth.Truth;
import com.google.testing.compile.JavaFileObjects;
import com.google.testing.compile.JavaSourceSubjectFactory;
import com.wreulicke.lombok.javac.ScalaProcessor;

public class VarProcessorTest {
  @Test
  public void test() {
    Truth.assertAbout(JavaSourceSubjectFactory.javaSource())
      .that(JavaFileObjects.forResource("Test.java"))
      .processedWith(new ScalaProcessor())
      .compilesWithoutError();
  }
}
