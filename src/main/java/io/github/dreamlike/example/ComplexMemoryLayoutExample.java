package io.github.dreamlike.example;

import io.github.dreamlike.LibLoader;
import io.github.dreamlike.NativeLookup;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public class ComplexMemoryLayoutExample {

    public static final int NATIVE_STRUCT_SIZE;
    public static final MemoryLayout LONG_AND_LONG_LAYOUT = MemoryLayout.structLayout(
            ValueLayout.JAVA_LONG.withName("a"),
            ValueLayout.JAVA_LONG.withName("b")
    );

    public static final MemoryLayout COMPLEX_LAYOUT = MemoryLayout.structLayout(
            ValueLayout.JAVA_LONG.withName("a"),
            MemoryLayout.sequenceLayout(3, ValueLayout.JAVA_LONG).withName("long_array"),
            LONG_AND_LONG_LAYOUT.withName("sub_struct"),
            ValueLayout.ADDRESS.withTargetLayout(MemoryLayout.sequenceLayout(Integer.MAX_VALUE, ValueLayout.JAVA_LONG)).withName("long_array_ptr"),
            ValueLayout.ADDRESS.withTargetLayout(LONG_AND_LONG_LAYOUT).withName("long_and_long_ptr")
    );

    static {
        try {
            MethodHandles.lookup().ensureInitialized(LibLoader.class);
            MemorySegment fp = NativeLookup.INSTANCE.find("complex_size").get();
            NATIVE_STRUCT_SIZE = ((int) Linker.nativeLinker().downcallHandle(
                    fp,
                    FunctionDescriptor.of(ValueLayout.JAVA_INT)
            ).invokeExact());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static long getLongArrayIndex1(MemorySegment memorySegment) {
        VarHandle varHandle = COMPLEX_LAYOUT.varHandle(
                MemoryLayout.PathElement.groupElement("long_array"),
                MemoryLayout.PathElement.sequenceElement(/*index*/ 1)
        );

        varHandle = MethodHandles.insertCoordinates(varHandle, 1, 0);

        return (long) varHandle.get(memorySegment);
    }

    public static long getSubStructFieldB(MemorySegment memorySegment) {
        VarHandle varHandle = COMPLEX_LAYOUT.varHandle(
                MemoryLayout.PathElement.groupElement("sub_struct"),
                MemoryLayout.PathElement.groupElement("b")
        );
        varHandle = MethodHandles.insertCoordinates(varHandle, 1, 0);
        return (long) varHandle.get(memorySegment);
    }

    public static long getLongArrayPtrIndex4(MemorySegment segment) {
        VarHandle varHandle = COMPLEX_LAYOUT.varHandle(
                MemoryLayout.PathElement.groupElement("long_array_ptr"),
                MemoryLayout.PathElement.dereferenceElement(),
                MemoryLayout.PathElement.sequenceElement(4)
        );
        //类似于
        //struct.long_array_ptr[4]
        varHandle = MethodHandles.insertCoordinates(varHandle, 1, 0);

        return (long) varHandle.get(segment);
    }

    public static long getLongAndLongPtrFieldB(MemorySegment segment) {
        VarHandle varHandle = COMPLEX_LAYOUT.varHandle(
                MemoryLayout.PathElement.groupElement("long_and_long_ptr"),
                MemoryLayout.PathElement.dereferenceElement(),
                MemoryLayout.PathElement.groupElement("b")
        );
        //类似于
        //struct.long_and_long_ptr->b
        varHandle = MethodHandles.insertCoordinates(varHandle, 1, 0);
        return (long) varHandle.get(segment);
    }
}
