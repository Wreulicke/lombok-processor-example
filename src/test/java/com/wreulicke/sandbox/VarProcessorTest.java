package com.wreulicke.sandbox;

import java.util.Arrays;

import org.junit.Test;

import com.google.common.truth.Truth;
import com.google.testing.compile.JavaFileObjects;
import com.google.testing.compile.JavaSourceSubjectFactory;
import com.wreulicke.lombok.javac.ScalaProcessor;

public class VarProcessorTest {
  @Test
  public void test() {
    String[] paths = System.getProperty("java.class.path")
      .split(";");
    Arrays.stream(paths).forEach(System.out::println);
    Truth.assertAbout(JavaSourceSubjectFactory.javaSource())
      .that(JavaFileObjects.forResource("Test.java"))
      .processedWith(new ScalaProcessor())
      .compilesWithoutError();
  }
}
