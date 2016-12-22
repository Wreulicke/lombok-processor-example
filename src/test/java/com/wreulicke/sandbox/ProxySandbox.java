package com.wreulicke.sandbox;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.Callable;

import org.junit.Test;

import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.matcher.ElementMatchers;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class ProxySandbox {
  @Test
  public void testCreateDynamicProxy() {
    SomeInterface impl = new Some();
    SomeInterface proxy = (SomeInterface) Proxy.newProxyInstance(this.getClass()
      .getClassLoader(), new Class[] {
        SomeInterface.class
    }, (Object _proxy, Method method, Object[] args) -> {
      System.out.println(method.getName() + ":start");
      method.invoke(impl, args);
      System.out.println(method.getName() + ":end");
      return null;
    });
    proxy.apply();
  }

  @Test
  public void testCreateProxyWithJavassist() throws NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException,
    InvocationTargetException {
    ProxyFactory factory = new ProxyFactory();

    factory.setSuperclass(Some.class);
    Some some = (Some) factory.createClass()
      .newInstance();

    ProxyObject proxyObject = (ProxyObject) some;
    proxyObject.setHandler((Object self, Method thisMethod, Method proceed, Object[] args) -> {
      System.out.println(thisMethod.getName() + ":start");
      proceed.invoke(self, args);
      System.out.println(thisMethod.getName() + ":end");
      return null;
    });
    some.apply();
  }

  @Test
  public void testCreateProxyWithCglib() {
    Enhancer enhancer = new Enhancer();
    enhancer.setSuperclass(Some.class);
    MethodInterceptor interceptor = (Object obj, Method method, Object[] args, MethodProxy proxy) -> {
      System.out.println(method.getName() + ":start");
      proxy.invokeSuper(obj, args);
      System.out.println(method.getName() + ":end");
      return null;
    };
    enhancer.setCallback(interceptor);
    Some some = (Some) enhancer.create();
    some.apply();
  }

  @Test
  public void testCreateProxyWithByteBuddy() throws InstantiationException, IllegalAccessException {
    Some some = (Some) new ByteBuddy().subclass(Some.class)
      .method(ElementMatchers.any())
      .intercept(MethodDelegation.to(TestInterceptor.class))
      .make()
      .load(this.getClass()
        .getClassLoader())
      .getLoaded()
      .newInstance();
    some.apply();
  }

  public static class TestInterceptor {
    @RuntimeType
    public static Object intercept(@SuperCall Callable<?> zuper, @Origin Method method) throws Exception {
      System.out.println(method.getName() + ":start");
      zuper.call();
      System.out.println(method.getName() + ":end");
      return null;
    }
  }
  public static class Some implements SomeInterface {
    @Override
    public void apply() {
      System.out.println("hello world");
    }

  }
  interface SomeInterface {
    public void apply();
  }
}
