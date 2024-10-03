package io.github.dreamlike.example;

import io.github.dreamlike.LibLoader;
import io.github.dreamlike.Main;
import io.github.dreamlike.NativeLookup;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class UpcallExample {

    private final static MemorySegment upcallStub;

    private final static MethodHandle callbackMH;

    static {
        try {
            MethodHandles.lookup().ensureInitialized(LibLoader.class);

            MemorySegment callbackFp = NativeLookup.INSTANCE.find("callback").get();

            callbackMH = Linker.nativeLinker()
                    .downcallHandle(
                            callbackFp,
                            FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
                    );

            upcallStub = Linker.nativeLinker().upcallStub(
                    MethodHandles.lookup().findStatic(Math.class, "addExact", MethodType.methodType(int.class, int.class, int.class)),
                    FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT),
                    Arena.global()
            );


        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }


    public static int callBackNative(int a, int b) {
        try {
            return (int) callbackMH.invokeExact(a, b, upcallStub);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

}
