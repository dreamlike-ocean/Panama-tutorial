import com.sun.jdi.Value;
import io.github.dreamlike.example.ComplexMemoryLayoutExample;
import io.github.dreamlike.example.MemoryLayoutExample;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.lang.foreign.*;
import java.util.List;

public class MemoryLayoutTest {

    @Test
    public void tesGetInt() {
        try (Arena scope = Arena.ofConfined()) {
            MemorySegment memorySegment = scope.allocate(ValueLayout.JAVA_INT);
            memorySegment.set(ValueLayout.JAVA_INT, 0, 2001);
            Assertions.assertEquals(2001, MemoryLayoutExample.getInt(memorySegment, false));
            Assertions.assertEquals(2001, MemoryLayoutExample.getInt(memorySegment, true));

            IllegalArgumentException illegalArgumentException = Assertions.assertThrows(IllegalArgumentException.class, () -> {
                var memorySegment1 = scope.allocate(ValueLayout.JAVA_INT.byteSize() + 1);
                ValueLayout.JAVA_INT.varHandle().set(memorySegment1, 1, 2001);
            });

            var memorySegment1 = scope.allocate(ValueLayout.JAVA_INT.byteSize() + 1);
            ValueLayout.JAVA_INT_UNALIGNED.varHandle().set(memorySegment1, 1, 2001);
            Assertions.assertEquals(2001, ValueLayout.JAVA_INT_UNALIGNED.varHandle().get(memorySegment1, 1));
        }
    }

    @Test
    public void testSum() {
        int[] ints = new int[10];
        for (int i = 0; i < ints.length; i++) {
            ints[i] = i;
        }

        Assertions.assertEquals(45, MemoryLayoutExample.sum(MemorySegment.ofArray(ints)));
    }

    @Test
    public void testStruct() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            StructLayout structLayout = MemoryLayout.structLayout(
                    ValueLayout.JAVA_INT.withName("a"),
                    ValueLayout.JAVA_LONG.withName("n")
            );
        });

        Assertions.assertEquals(MemoryLayoutExample.NATIVE_STRUCT_SIZE, MemoryLayoutExample.PERSON_LAYOUT.byteSize());

        try (Arena scope = Arena.ofConfined()) {
            MemorySegment nativeHeapSegment = scope.allocate(MemoryLayoutExample.PERSON_LAYOUT);
            nativeHeapSegment.set(ValueLayout.JAVA_LONG, 8, 2001);
            Assertions.assertEquals(2001, MemoryLayoutExample.getN(nativeHeapSegment, false));
            Assertions.assertEquals(2001, MemoryLayoutExample.getN(nativeHeapSegment, true));
        }

    }

    @Test
    public void testUnion() {
        Assertions.assertEquals(ValueLayout.JAVA_LONG.byteSize(), MemoryLayoutExample.UNION_SAMPLE_STRUCT_LAYOUT.byteSize());
    }

    @Test
    public void testAlign() {
        StructLayout layout = MemoryLayoutExample.calAlignLayout(
                ValueLayout.JAVA_INT,
                ValueLayout.JAVA_LONG,
                ValueLayout.JAVA_BYTE
        );
        Assertions.assertEquals( 8 + 8 + 1 + 7, layout.byteSize());

        List<MemoryLayout> actual = layout.memberLayouts();
        List<MemoryLayout> expected = MemoryLayout.structLayout(
                ValueLayout.JAVA_INT,
                MemoryLayout.paddingLayout(4),
                ValueLayout.JAVA_LONG,
                ValueLayout.JAVA_BYTE,
                MemoryLayout.paddingLayout(7)
        ).memberLayouts();

        Assertions.assertEquals(expected, actual);

    }
}
