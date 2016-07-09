package com.epam.common.igLib;

import java.util.Date;

import static com.epam.common.igLib.LibDateFormats.*;
import static com.epam.common.igLib.LibFormats.*;

public final class StrDate104 implements IDate {

    private final int yearMonthDay;

    public StrDate104(String strDate104) throws Exception {
        this(getDate104(strDate104));
    }

    public StrDate104(long millis) {
        yearMonthDay = init(getLocalIDate(millis));
    }

    public StrDate104(Date date) {
        yearMonthDay = init(getLocalIDate(date));
    }

    public StrDate104(int year, int month, int day) {
        year = (year % 10000); // year < 10000
        month = ((month - 1) % 12) + 1; // 1 <= month <= 12
        day = ((day - 1) % 31) + 1; //  1 <= day < = 31
        yearMonthDay = init(year, month, day);
    }

    private int init(IDate iDate) {
        return init(iDate.getYear(), iDate.getMonth(), iDate.getDayOfMonth());
    }

    private int init(int year, int month, int day) {
        return year * 10000 + month * 100 + day; // 22-Jan-2015 -> 20150322 (20_150_322)
    }

    @Override
    public int getYear() {
        return yearMonthDay % 100;
    }

    @Override
    public int getMonth() {
        return (yearMonthDay / 100) % 100;
    }

    @Override
    public int getDayOfMonth() {
        return yearMonthDay / 10000;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        addTwoDigits(sb, getYear());
        sb.append(".");
        addTwoDigits(sb, getMonth());
        sb.append(".");
        sb.append(getDayOfMonth());
        return sb.toString(); // DD.MM.YYYY
    }

    @Override
    public int compareTo(IDate another) {
        int anotherYearMonthDay = (another instanceof StrDate104) ? ((StrDate104) another).yearMonthDay : init(
                another.getYear(), another.getMonth(), another.getDayOfMonth());
        return (yearMonthDay < anotherYearMonthDay ? -1 : (yearMonthDay == anotherYearMonthDay ? 0 : 1));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + yearMonthDay;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StrDate104 other = (StrDate104) obj;
        if (yearMonthDay != other.yearMonthDay)
            return false;
        return true;
    }
}
