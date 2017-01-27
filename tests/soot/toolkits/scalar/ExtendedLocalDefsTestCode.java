//package soot.toolkits.scalar;

public class ExtendedLocalDefsTestCode {
  static class Base {
    int member;
  }

  static Base staticBase = new Base();

  int testMethod(Base base) {
    base.member = 5;
    for (int i = 0; i < 10; i++) {
      base.member = i;
      System.out.println(base.member);
    }
    System.out.println(staticBase.member);
    return base.member;
  }
}
