package io.github.dreamlike.example;

import io.github.dreamlike.LibLoader;
import io.github.dreamlike.NativeLookup;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class NativeImageExample {


    static {
        try {
            MethodHandles.lookup().ensureInitialized(LibLoader.class);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    public static long getByIndex(long[] array, int index) {
        try {
            var getArrayByIndexMH = Linker.nativeLinker().downcallHandle(
                    NativeLookup.INSTANCE.find("get_array_by_index").get(),
                    FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_INT),
                    Linker.Option.critical(true)
            );
            return (long) getArrayByIndexMH.invokeExact(MemorySegment.ofArray(array), index);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
    public static int capturedLambdaUpcall(String someString) {
        interface Add {
            int add(int a, int b);
        }
        try {
            MethodHandle methodHandle = MethodHandles.lookup().findVirtual(Add.class, "add", MethodType.methodType(int.class, int.class, int.class));
            methodHandle = methodHandle.bindTo((Add) (a, b) -> {
                System.out.println(someString);
                return someString.length();
            });

            var capturedLambdaUpcallStub = Linker.nativeLinker().upcallStub(
                    methodHandle,
                    FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT),
                    Arena.global()
            );

            return (int) Linker.nativeLinker()
                    .downcallHandle(
                            capturedLambdaUpcallStub,
                            FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT)
                    ).invokeExact(1, 2);
        } catch (Throwable r) {
            throw new RuntimeException(r);
        }
    }

}
