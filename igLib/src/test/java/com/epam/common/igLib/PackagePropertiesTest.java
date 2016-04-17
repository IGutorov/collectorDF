package com.epam.common.igLib;

import static org.junit.Assert.*;

import org.junit.Test;

public class PackagePropertiesTest {

    @Test
    public void shouldPackagePropertiesAlwaysNotNullResult() {
        String get = PackageProperties.getProperty(null);
        assertNotNull("PackageProperties.getProperty is not null", get);
    }

    @Test
    public void shouldPackagePropertiesNotNullResult() {
        String get = PackageProperties.getProperty("Wrong.param");
        assertNotNull("PackageProperties.getProperty with any param is not null", get);
    }

    @Test
    public void shouldPackagePropertiesDefinedTimeZone() {
        String get = PackageProperties.getProperty("Default.TimeZone");
        assertTrue("PackageProperties.getProperty is not empty String", !get.isEmpty());
    }

    @Test
    public void checkPackagePropertiesDefaultTimeZones() {
        String get = PackageProperties.getProperty("Default.TimeZone");
        assertEquals("PackageProperties.getProperty is not empty String", get, "GMT+3");
    }


}
