package com.epam.common.igLib;

public interface IDate extends Comparable<IDate> {

    int getYear();

    int getMonth(); // Jan = 1, Feb = 2...  , Dec = 12

    int getDayOfMonth();

}
