import io.github.dreamlike.example.ComplexMemoryLayoutExample;
import io.github.dreamlike.example.DowncallExample;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class DowncallTest {

    @Test
    public void testDowncall() {
        Assertions.assertEquals(3, DowncallExample.add(1, 2));
        Assertions.assertEquals(3, DowncallExample.addFast(1, 2));
        DowncallExample.rawBenchmark();
    }

    @Test
    public void testPassJavaHeap() {
        long[] longArray = new long[]{1, 2, 3};
        Assertions.assertEquals(3, DowncallExample.getByIndex(longArray, 2));
    }

    @Test
    public void testBigStruct() {
        try (Arena allocate = Arena.ofConfined()){
            MemorySegment memorySegment = DowncallExample.newBigStruct(allocate);
            Assertions.assertEquals(1,memorySegment.get(ValueLayout.JAVA_LONG,0));
            Assertions.assertEquals(2,memorySegment.get(ValueLayout.JAVA_LONG,8));
            Assertions.assertEquals(3,memorySegment.get(ValueLayout.JAVA_LONG,16));
            Assertions.assertEquals(4,memorySegment.get(ValueLayout.JAVA_LONG,24));
        }
    }

    @Test
    public void testErrorSyscall() {
        int i = DowncallExample.errorSyscall(1024);
        Assertions.assertEquals(1024, -i);
    }

    @Test
    public void testBigStructErrorSyscall() {
        try (Arena allocate = Arena.ofConfined()){
            MemorySegment memorySegment = DowncallExample.newBigStructErrorNo(allocate);
            Assertions.assertEquals(1,memorySegment.get(ValueLayout.JAVA_LONG,0));
            Assertions.assertEquals(2,memorySegment.get(ValueLayout.JAVA_LONG,8));
            Assertions.assertEquals(3,memorySegment.get(ValueLayout.JAVA_LONG,16));
            Assertions.assertEquals(4,memorySegment.get(ValueLayout.JAVA_LONG,24));
        }
    }
}
