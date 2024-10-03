import io.github.dreamlike.example.ComplexMemoryLayoutExample;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class ComplexMemoryLayoutTest {

    @Test
    public void testSize() {
        Assertions.assertEquals(ComplexMemoryLayoutExample.NATIVE_STRUCT_SIZE, ComplexMemoryLayoutExample.COMPLEX_LAYOUT.byteSize());
    }


    @Test
    public void testLongArrayIndexValue() {
        try (Arena scope = Arena.ofConfined()) {
            MemorySegment memorySegment = scope.allocate(ComplexMemoryLayoutExample.COMPLEX_LAYOUT);
            memorySegment.set(ValueLayout.JAVA_LONG, 8 + 8, 2001);
            Assertions.assertEquals(2001, ComplexMemoryLayoutExample.getLongArrayIndex1(memorySegment));
        }
    }

    @Test
    public void testSubStruct() {
        try (Arena scope = Arena.ofConfined()) {
            MemorySegment memorySegment = scope.allocate(ComplexMemoryLayoutExample.COMPLEX_LAYOUT);
            memorySegment.set(ValueLayout.JAVA_LONG, 8 + 8 * 3 + 8, 2001);
            Assertions.assertEquals(2001, ComplexMemoryLayoutExample.getSubStructFieldB(memorySegment));
        }
    }

    @Test
    public void testDeferenceArray() {
        try (Arena scope = Arena.ofConfined()) {
            MemorySegment longArray = scope.allocate(ValueLayout.JAVA_LONG, 10);
            longArray.setAtIndex(ValueLayout.JAVA_LONG, 4, 2001);

            MemorySegment memorySegment = scope.allocate(ComplexMemoryLayoutExample.COMPLEX_LAYOUT);
            memorySegment.set(ValueLayout.ADDRESS, 8 + 8 * 3 + 8 * 2, longArray);

            Assertions.assertEquals(2001, ComplexMemoryLayoutExample.getLongArrayPtrIndex4(memorySegment));
        }
    }

    @Test
    public void testDeferenceStruct() {
        try (Arena scope = Arena.ofConfined()) {
            MemorySegment memorySegment = scope.allocate(ComplexMemoryLayoutExample.COMPLEX_LAYOUT);
            MemorySegment subStruct = scope.allocate(ComplexMemoryLayoutExample.LONG_AND_LONG_LAYOUT);
            subStruct.set(ValueLayout.JAVA_LONG, 8, 2002);

            memorySegment.set(ValueLayout.ADDRESS, 8 + 8 * 3 + 8 * 2 + ValueLayout.ADDRESS.byteSize(), subStruct);

            Assertions.assertEquals(2002, ComplexMemoryLayoutExample.getLongAndLongPtrFieldB(memorySegment));
        }
    }
}
