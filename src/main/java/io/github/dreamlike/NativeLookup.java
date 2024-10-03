package io.github.dreamlike;

import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.util.Optional;

public class NativeLookup implements SymbolLookup {

    public static final NativeLookup INSTANCE = new NativeLookup();

    @Override
    public Optional<MemorySegment> find(String name) {
        return SymbolLookup.loaderLookup()
                .find(name)
                .or(() -> Linker.nativeLinker().defaultLookup().find(name));
    }
}