package io.github.dreamlike.example;

import io.github.dreamlike.LibLoader;
import io.github.dreamlike.NativeLookup;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;

public class MemoryLayoutExample {
    private static final VarHandle GET_INT_OFFSET_0 = MethodHandles.insertCoordinates(ValueLayout.JAVA_INT.varHandle(), 1, 0);

    public static final int NATIVE_STRUCT_SIZE;

    public static final MemoryLayout PERSON_LAYOUT = MemoryLayout.structLayout(
            ValueLayout.JAVA_INT.withName("a"),
            MemoryLayout.paddingLayout(4),
            ValueLayout.JAVA_LONG.withName("n")
    );

    public static final MemoryLayout UNION_SAMPLE_STRUCT_LAYOUT = MemoryLayout.unionLayout(
            ValueLayout.JAVA_INT.withName("a"),
            ValueLayout.JAVA_LONG.withName("n")
    );



    static {
        try {
            MethodHandles.lookup().ensureInitialized(LibLoader.class);
            MemorySegment fp = NativeLookup.INSTANCE.find("person_size").get();
            NATIVE_STRUCT_SIZE = ((int) Linker.nativeLinker().downcallHandle(
                    fp,
                    FunctionDescriptor.of(ValueLayout.JAVA_INT)
            ).invokeExact());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static int getInt(MemorySegment memorySegment, boolean useOffset) {
        VarHandle varHandle = ValueLayout.JAVA_INT.varHandle();

        if (useOffset) {
            return (int) varHandle.get(memorySegment, 0);
        }
        return (int) GET_INT_OFFSET_0.get(memorySegment);
    }

    public static int sum(MemorySegment intArray) {
        int count = (int) (intArray.byteSize() / ValueLayout.JAVA_INT.byteSize());
        SequenceLayout sequenceLayout = MemoryLayout.sequenceLayout(/*count*/count, ValueLayout.JAVA_INT);

        //真实使用的时候务必将VarHandle const化
        VarHandle varHandle = sequenceLayout.varHandle(MemoryLayout.PathElement.sequenceElement());

        int sum = 0;
        for (int i = 0; i < count - 1; i++) {
            sum += (int) varHandle.get(intArray, 0, i);
        }

        //专门用来获取最后一个元素 同时给 VarHandle 插入基准偏移量 0
        VarHandle indexVarhandle = MethodHandles.insertCoordinates(sequenceLayout.varHandle(MemoryLayout.PathElement.sequenceElement(count - 1)), 1, 0);

        sum += (int) indexVarhandle.get(intArray);

        return sum;
    }


    public static long getN(MemorySegment memorySegment, boolean useName) {
        VarHandle varHandle = useName
                //由于我们使用了ValueLayout.JAVA_LONG.withName("n") 所以可以通过名字获取varhandle
                ? PERSON_LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("n"))
                // 由于名字为n的字段是第三个布局元素（包含填充类型的布局） 所以这里是2
                : PERSON_LAYOUT.varHandle(MemoryLayout.PathElement.groupElement(2));
        //老样子习惯性插入基准偏移量
        varHandle = MethodHandles.insertCoordinates(varHandle, 1, 0);
        return (long) varHandle.get(memorySegment);
    }

    public static StructLayout calAlignLayout(MemoryLayout... memoryLayouts) {
        long size = 0;
        long align = 1;
        ArrayList<MemoryLayout> layouts = new ArrayList<>();
        for (MemoryLayout memoryLayout : memoryLayouts) {
            //当前布局是否与size对齐
            if (size % memoryLayout.byteAlignment() == 0) {
                size = Math.addExact(size, memoryLayout.byteSize());
                align = Math.max(align, memoryLayout.byteAlignment());
                layouts.add(memoryLayout);
                continue;
            }
            long multiple = size / memoryLayout.byteAlignment();
            //计算填充
            long padding = (multiple + 1) * memoryLayout.byteAlignment() - size;
            size = Math.addExact(size, padding);
            //添加填充
            layouts.add(MemoryLayout.paddingLayout(padding));
            //添加当前布局
            layouts.add(memoryLayout);
            size = Math.addExact(size, memoryLayout.byteSize());
            align = Math.max(align, memoryLayout.byteAlignment());
        }

        //尾部对齐
        if (size % align != 0) {
            long multiple = size / align;
            long padding = (multiple + 1) * align - size;
            size = Math.addExact(size, padding);
            layouts.add(MemoryLayout.paddingLayout(padding));
        }
        return MemoryLayout.structLayout(layouts.toArray(MemoryLayout[]::new));
    }
}
