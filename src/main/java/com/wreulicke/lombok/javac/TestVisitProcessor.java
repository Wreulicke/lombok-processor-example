package com.wreulicke.lombok.javac;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

import com.sun.source.tree.TreeVisitor;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreeScanner;
import com.sun.source.util.Trees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyFactory.ClassLoaderProvider;
import javassist.util.proxy.ProxyObject;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("*")
public class TestVisitProcessor extends AbstractProcessor {

  @SuppressWarnings("unchecked")
  public TestVisitProcessor() {}

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    JavacProcessingEnvironment environment = (JavacProcessingEnvironment) processingEnv;
    ProxyFactory factory = new ProxyFactory();

    factory.setSuperclass(TreeScanner.class);
    factory.classLoaderProvider = new ClassLoaderProvider() {

      @Override
      public ClassLoader get(ProxyFactory pf) {
        return TestVisitProcessor.class.getClassLoader();
      }
    };
    try {
      TreeVisitor<?, ?> visitor = (TreeVisitor<?, ?>) factory.createClass()
        .newInstance();
      ProxyObject proxyObject = (ProxyObject) visitor;
      proxyObject.setHandler(new MethodHandler() {
        int nest = 0;

        @Override
        public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
          String indent = Stream.generate(() -> "  ")
            .limit(nest)
            .collect(Collectors.joining());
          System.out.println(indent + thisMethod.getName() + ":start");
          nest++;
          if (thisMethod.getName()
            .indexOf("visit") != 0 && args.length != 0) {
            String data = Stream.of(args)
              .filter((o) -> o != null)
              .map((o) -> indent + o.getClass() + ":" + o)
              .collect(Collectors.joining(", "));
            if (!data.isEmpty())
              System.out.println(data);
          }
          proceed.invoke(self, args);
          nest--;
          System.out.println(indent + thisMethod.getName() + ":end");
          return null;
        }
      });
      Trees trees = Trees.instance(environment);
      roundEnv.getRootElements()
        .stream()
        .map(trees::getPath)
        .map(TreePath::getCompilationUnit)
        .forEach((t) -> {
          t.accept(visitor, null);
        });

    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
    return false;
  }

}
