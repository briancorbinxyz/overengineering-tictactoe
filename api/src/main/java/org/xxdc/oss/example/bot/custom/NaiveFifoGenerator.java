package org.xxdc.oss.example.bot.custom;

import java.lang.classfile.ClassFile;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodHandles;

public class NaiveFifoGenerator {

  public static CustomBotStrategy newGeneratedBot() {
    try {
      byte[] classBytes = generateBotClass();
      MethodHandles.Lookup lookup = MethodHandles.lookup();
      Class<?> clazz = lookup.defineClass(classBytes);
      return (CustomBotStrategy) clazz.getDeclaredConstructor().newInstance();
    } catch (Throwable e) {
      throw new RuntimeException("Failed to load Naive FIFO bot class", e);
    }
  }

  private static byte[] generateBotClass() {
    return ClassFile.of()
        .build(
            ClassDesc.of("org.xxdc.oss.example.bot.custom.NaiveFifo"),
            clb ->
                clb.withFlags(ClassFile.ACC_PUBLIC)
                    .withSuperclass(ClassDesc.of("java.lang.Object"))
                    .withInterfaceSymbols(
                        ClassDesc.of("org.xxdc.oss.example.bot.custom.CustomBotStrategy"))
                    .withMethod(
                        ConstantDescs.INIT_NAME,
                        MethodTypeDesc.ofDescriptor("()V"),
                        ClassFile.ACC_PUBLIC,
                        mb ->
                            mb.withCode(
                                code ->
                                    code.aload(0)
                                        .invokespecial(
                                            ClassDesc.of("java.lang.Object"),
                                            ConstantDescs.INIT_NAME,
                                            MethodTypeDesc.ofDescriptor("()V"))
                                        .return_()))
                    .withMethod(
                        "bestMove",
                        MethodTypeDesc.ofDescriptor("(Ljava/util/List;)I"),
                        ClassFile.ACC_PUBLIC,
                        mb ->
                            mb.withCode(
                                code -> {
                                  var nonEmpty = code.newLabel();
                                  code.aload(1) // Load `availableMoves`
                                      .invokeinterface(
                                          ClassDesc.of("java.util.List"),
                                          "isEmpty",
                                          MethodTypeDesc.ofDescriptor(
                                              "()Z")) // Call availableMoves.isEmpty()
                                      .ifeq(nonEmpty) // If not empty, jump to 'nonEmpty'

                                      // Throw new RuntimeException("No moves available.")
                                      .new_(ClassDesc.of("java.lang.RuntimeException"))
                                      .dup()
                                      .ldc("No moves available.")
                                      .invokespecial(
                                          ClassDesc.of("java.lang.RuntimeException"),
                                          ConstantDescs.INIT_NAME, //   "<init>"
                                          MethodTypeDesc.ofDescriptor("(Ljava/lang/String;)V"))
                                      .athrow()

                                      // If non-empty, return first element: availableMoves.get(0)
                                      .labelBinding(nonEmpty)
                                      .aload(1) // Load `availableMoves`
                                      .iconst_0() // Load `0`
                                      .invokeinterface(
                                          ClassDesc.of("java.util.List"),
                                          "get",
                                          MethodTypeDesc.ofDescriptor(
                                              "(I)Ljava/lang/Object;")) // Call get(0)
                                      .checkcast(
                                          ClassDesc.of(
                                              "java.lang.Integer")) // Cast Object -> Integer
                                      .invokevirtual(
                                          ClassDesc.of("java.lang.Integer"),
                                          "intValue",
                                          MethodTypeDesc.ofDescriptor(
                                              "()I")) // Unbox Integer -> int
                                      .ireturn();
                                })));
  }
}
