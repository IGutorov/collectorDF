package com.epam.common.igLib;

public class StartTest {

    public StartTest() {
        System.out.println("PackageProperties. = " + PackageProperties.getProperty("Default.TimeZone"));
        System.out.println("printAllStats : " + PackageProperties.getStats());
        System.out.println(this);
    }

    public static void main(String[] args) {
        new StartTest();
    }

}
