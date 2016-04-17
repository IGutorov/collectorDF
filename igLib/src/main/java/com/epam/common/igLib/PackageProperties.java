package com.epam.common.igLib;

public class PackageProperties extends PackagePropertiesImpl {

    private static final String PROPERTIES_PATH = "properties/igLib.properties";

    public static String getProperty(String param) {
        return getProperty(PROPERTIES_PATH, param);
    }

    static String getStats() {
        return getStats(PROPERTIES_PATH);
    }

    // sample of use
    // com.epam.common.igLib.PackageProperties.reloadAllProperties();
    public static void reloadAllProperties() {
        PackagePropertiesImpl.reloadProps();
    }
        
    protected PackageProperties() throws Exception {
        super();
    }
}
