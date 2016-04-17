package com.epam.common.igLib;

import java.util.Date;

public final class StrDate104 implements Comparable<StrDate104> {

    private final int yearMonthDay;

    public StrDate104(String strDate104) throws Exception {
        this(LibFormats.getDate104(strDate104));
    }

    public StrDate104(long datetime) {
        this(new Date(datetime));
    }

    public StrDate104(Date date) {
        int year = LibFormats.getYear(date);
        int month = LibFormats.getMonth(date);
        int day  = LibFormats.getDayOfMonth(date);
        yearMonthDay = init(year, month, day);
    }
    
    public StrDate104(int year, int month, int day) {
        year = (year % 10000); // year < 10000
        month = ((month - 1) % 12) + 1; // 1 <= month <= 12
        day = ((day - 1) % 31) + 1; //  1 <= day < = 31
        yearMonthDay = init(year, month, day);
    }

    private int init(int year, int month, int day) {
        return year * 10000 + month * 100 + day; // 22-Jan-2015 -> 20150322
    }

    private static void addTwoDigits2SB(StringBuilder stringBuilder, int num) {
        if (num < 0 || num > 99) {
            stringBuilder.append("00");
            return;
        }
        if (num < 10)
            stringBuilder.append("0");
        stringBuilder.append(num);
    }

    public int getYear() {
        return yearMonthDay % 100;
    }
    
    public int getMonth() {
        return (yearMonthDay / 100) % 100;
    }
    
    public int getDayOfMonth() {
        return yearMonthDay / 10000;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        addTwoDigits2SB(sb, getYear());
        sb.append(".");
        addTwoDigits2SB(sb, getMonth());
        sb.append(".");
        sb.append(getDayOfMonth());
        return sb.toString(); // DD.MM.YYYY
    }

    @Override
    public int compareTo(StrDate104 another) {
        return (yearMonthDay < another.yearMonthDay ?  -1 : (yearMonthDay == another.yearMonthDay ? 0 : 1));
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
