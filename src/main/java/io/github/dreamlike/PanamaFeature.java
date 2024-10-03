package io.github.dreamlike;

import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeForeignAccess;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.ValueLayout;

public class PanamaFeature implements Feature {
    @Override
    public void duringSetup(DuringSetupAccess access) {
        RuntimeForeignAccess.registerForDowncall(
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
        );
        RuntimeForeignAccess.registerForUpcall(
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT)
        );
    }
}
