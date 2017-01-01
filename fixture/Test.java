public class Test {
  @interface var {
  }

  public var a = 10;
  public var b = "bbb";
  public var c = 'x';
  public var d = 2.0;
  public var f = 2f;

  public void sample() {
    var msg = "test data";
    System.out.println(msg);
  }
  public static void main(String... args){
    Test x=new Test();
  	System.out.println(x.a);
  	System.out.println(x.b);
  	System.out.println(x.c);
  	System.out.println(x.d);
  	System.out.println(x.f);
  }
}
