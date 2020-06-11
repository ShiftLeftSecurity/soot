package soot.asm.function;

public interface Function<ParameterType, ReturnType> {
  ReturnType apply(ParameterType parameter);
}
