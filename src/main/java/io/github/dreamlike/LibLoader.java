package io.github.dreamlike;

import java.io.File;

public class LibLoader {

    static {
        System.load(new File("libperson.so").getAbsolutePath());
    }
}
