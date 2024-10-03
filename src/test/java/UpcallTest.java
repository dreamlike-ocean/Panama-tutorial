import io.github.dreamlike.example.UpcallExample;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UpcallTest {

    @Test
    public void testUpcall() {
        Assertions.assertEquals(6, UpcallExample.callBackNative(1, 2));
    }

}
