package com.epam.rcrd.coreDF;

public final class StartDF {

    /*
    ??
    public static IConnectionsSetter getConnectionsSetter() {
        return getCS();
    }
    */
    public static IConnectionsSetter getConnectionsSetter() throws Exception {
        ConnectionsSetter result = new ConnectionsSetter();
        return result;
    }

    /*
    ??
    public static IConnectionsSetter getConnectionsSetterByPath(String absoluteFolderName) {
        ConnectionsSetter result = getCS();
        result.setAliasAbsoluteFolder(absoluteFolderName);        
        return result;
    }
    */
}
