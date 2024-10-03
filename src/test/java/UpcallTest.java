import io.github.dreamlike.example.UpcallExample;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class UpcallTest {

    @Test
    public void testUpcall() {
        Assertions.assertEquals(6, UpcallExample.callBackNative(1, 2));
    }

    @Test
    public void testCallUpcall() throws NoSuchMethodException, IllegalAccessException {
        Assertions.assertEquals(3, UpcallExample.callUpcall(1, 2));
    }


    @Test
    public void testCapturedLambdaCallUpcall() throws NoSuchMethodException, IllegalAccessException {
        Assertions.assertEquals(6, UpcallExample.capturedLambdaUpcall("123456"));
    }

}
