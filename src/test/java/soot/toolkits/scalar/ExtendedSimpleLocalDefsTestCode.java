//package soot.toolkits.scalar;

public class ExtendedSimpleLocalDefsTestCode {
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
    staticBase.member = 3;
    System.out.println(staticBase.member);
    return base.member;
  }
}
