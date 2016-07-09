package com.epam.rcrd.coreDF;

class PackageProperties extends com.epam.common.igLib.PackageProperties {

    private static final String PROPERTIES_PATH = "properties/coreDF.properties";

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
