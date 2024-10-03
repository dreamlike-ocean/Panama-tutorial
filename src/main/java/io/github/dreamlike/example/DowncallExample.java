package io.github.dreamlike.example;

import io.github.dreamlike.LibLoader;
import io.github.dreamlike.NativeLookup;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public class DowncallExample {

    public static final StructLayout BigStructLayout = MemoryLayout.structLayout(
            ValueLayout.JAVA_LONG.withName("a"),
            ValueLayout.JAVA_LONG.withName("b"),
            ValueLayout.JAVA_LONG.withName("c"),
            ValueLayout.JAVA_LONG.withName("d")
    );
    private static final MethodHandle addMH;
    private static final MethodHandle addFastMH;
    private static final MethodHandle getArrayByIndexMH;
    private static final MethodHandle newBigStructMH;
    private static final MethodHandle errorSyscallMH;
    private static final MethodHandle newBigStructErrorNoMH;

    static {

        try {
            MethodHandles.lookup().ensureInitialized(LibLoader.class);
            MemorySegment fp = NativeLookup.INSTANCE.find("add").get();
            addMH = Linker.nativeLinker().downcallHandle(
                    fp,
                    FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT)
            );
            addFastMH = Linker.nativeLinker().downcallHandle(
                    fp,
                    FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT),
                    Linker.Option.critical(false)
            );

            getArrayByIndexMH = Linker.nativeLinker().downcallHandle(
                    NativeLookup.INSTANCE.find("get_array_by_index").get(),
                    FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_INT),
                    Linker.Option.critical(true)
            );

            newBigStructMH = Linker.nativeLinker().downcallHandle(
                    NativeLookup.INSTANCE.find("new_big_struct").get(),
                    FunctionDescriptor.of(BigStructLayout)
            );

            errorSyscallMH = Linker.nativeLinker().downcallHandle(
                    NativeLookup.INSTANCE.find("error_syscall").get(),
                    FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT),
                    Linker.Option.captureCallState("errno")
            );

            newBigStructErrorNoMH = Linker.nativeLinker().downcallHandle(
                    NativeLookup.INSTANCE.find("new_big_struct").get(),
                    FunctionDescriptor.of(BigStructLayout),
                    Linker.Option.captureCallState("errno")
            );
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static int add(int a, int b) {
        try {
            return (int) addMH.invokeExact(a, b);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static int addFast(int a, int b) {
        try {
            return (int) addFastMH.invokeExact(a, b);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void rawBenchmark() {
        long start = System.currentTimeMillis();
        long count = 10_0000;
        for (int i = 0; i < count; i++) {
            add(1, 2);
        }
        System.out.println("add rawBenchmark: " + (System.currentTimeMillis() - start));
        start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            addFast(1, 2);
        }
        System.out.println("addFast rawBenchmark: " + (System.currentTimeMillis() - start));
    }

    public static long getByIndex(long[] array, int index) {
        try {
            return (long) getArrayByIndexMH.invokeExact(MemorySegment.ofArray(array), index);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static MemorySegment newBigStruct(SegmentAllocator segmentAllocator) {
        try {
            return (MemorySegment) newBigStructMH.invokeExact(segmentAllocator);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static int errorSyscall(int errorno) {
        try (Arena arena = Arena.ofConfined()) {
            StructLayout capturedStateLayout = Linker.Option.captureStateLayout();
            VarHandle errnoHandle = capturedStateLayout.varHandle(MemoryLayout.PathElement.groupElement("errno"));
            MemorySegment capturedState = arena.allocate(capturedStateLayout);

            var _ = (int) errorSyscallMH.invokeExact(capturedState, errorno);
            return (int) errnoHandle.get(capturedState, 0L);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static MemorySegment newBigStructErrorNo(SegmentAllocator segmentAllocator) {
        try (Arena arena = Arena.ofConfined()) {
            StructLayout capturedStateLayout = Linker.Option.captureStateLayout();
            VarHandle errnoHandle = capturedStateLayout.varHandle(MemoryLayout.PathElement.groupElement("errno"));
            MemorySegment capturedState = arena.allocate(capturedStateLayout);

            //先放分配器 再放capturedState
            return  (MemorySegment) newBigStructErrorNoMH.invokeExact(segmentAllocator, capturedState);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
