package com.epam.rcrd.coreDF;

public final class StartDF {

    public static IConnectionsSetter getConnectionsSetter() {
        return new ConnectionsSetter();
    }
}
