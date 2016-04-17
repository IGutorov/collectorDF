package com.epam.rcrd.swingDF;

public class PackageProperties extends com.epam.common.igLib.PackageProperties {

    private static final String PROPERTIES_PATH = "properties/swingDF.properties";

    public static String getProperty(String param) {
        return getProperty(PROPERTIES_PATH, param);
    }

    static String getStats() {
        return getStats(PROPERTIES_PATH);
    }

    protected PackageProperties() throws Exception {
        super();
    }

}
