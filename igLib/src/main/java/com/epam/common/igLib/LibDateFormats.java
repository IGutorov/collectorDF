package com.epam.common.igLib;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;

import static com.epam.common.igLib.LibFormats.*;

public final class LibDateFormats {

    private static final String     DEFAULT_TIMEZONE  = "GMT+3";

    private static final int        MIN_YEAR          = 2000;
    private static final int        MAX_YEAR          = 2050;
    private static final DateFormat dateFormat112     = new SimpleDateFormat("yyyyMMdd");
    private static final DateFormat dateFormat112full = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
    private static final DateFormat dateFormat104     = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.GERMANY);  // DD.MM.YYYY

    private static final String     TWO_DIGITS_MASK   = "\\d\\d";
    private static final String     SHORT_TIME_MASK   = TWO_DIGITS_MASK + ":" + TWO_DIGITS_MASK;
    private static final String     FULL_TIME_MASK    = TWO_DIGITS_MASK + ":" + TWO_DIGITS_MASK + ":" + TWO_DIGITS_MASK;

    private static final TimeZone   currentTimeZone;
    private static final Calendar   currentCalendar;
    static {
        String timeZone = PackageProperties.getProperty("Default.TimeZone");
        if (timeZone.isEmpty())
            timeZone = DEFAULT_TIMEZONE;
        currentTimeZone = TimeZone.getTimeZone(timeZone);
        currentCalendar = new GregorianCalendar(currentTimeZone);
        currentCalendar.setTimeInMillis(0);
    }

    private LibDateFormats() {
    }

    public static IDate getLocalIDate(Date date) {
        return new IntDate(date);
    }

    public static IDate getLocalIDate(long millis) {
        return new IntDate(millis);
    }

    private static class IntDate implements IDate {
        private final int year;
        private final int month;     // 1 : Jan - 12 : Dec 
        private final int dayOfMonth;

        //  millis the new time in UTC milliseconds from the epoch
        private IntDate(long millis) {
            Calendar calendar = getLocalCalendarByMillis(millis);
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH) + 1;
            dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        }

        private IntDate(Date date) {
            Calendar calendar = getLocalCalendar(date);
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH) + 1;
            dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        }

        @Override
        public String toString() {
            return "IntDate [year=" + year + ", month=" + month + ", dayOfMonth=" + dayOfMonth + "]";
        }

        @Override
        public int getYear() {
            return year;
        }

        @Override
        public int getMonth() {
            return month;
        }

        @Override
        public int getDayOfMonth() {
            return dayOfMonth;
        }

        @Override
        public int compareTo(IDate another) {
            int result = Integer.signum(year - another.getYear());
            if (result != 0)
                return result;
            result = Integer.signum(month - another.getMonth());
            if (result != 0)
                return result;
            return Integer.signum(dayOfMonth - another.getDayOfMonth());
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + dayOfMonth;
            result = prime * result + month;
            result = prime * result + year;
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
            IntDate other = (IntDate) obj;
            if (dayOfMonth != other.dayOfMonth)
                return false;
            if (month != other.month)
                return false;
            if (year != other.year)
                return false;
            return true;
        }
    }

    private static Calendar getLocalCalendarByMillis(long millis) {
        Calendar result = new GregorianCalendar(currentTimeZone);
        result.setTimeInMillis(millis);
        return result;
    }

    /**
     * Процедура получения даты/времени с учётом часового пояса.
     * java.time.LocalTime/java.time.LocalDate/java.time.LocalDataTime поддерживается только в Java 8
     * 
     * @param date
     *            UTC-дата/время
     * @return дата/время приведённое к заданному (в настройке) часовому поясу
     */
    private static Calendar getLocalCalendar(Date date) {
        if (date == null)
            return (Calendar) currentCalendar.clone();
        return getLocalCalendarByMillis(date.getTime());
    }

    private static void midnight(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    public static String getStrDate104(Date date) {
        IDate iDate = getLocalIDate(date);
        StringBuilder result = new StringBuilder();
        addTwoDigits(result, iDate.getDayOfMonth());
        result.append(".");
        addTwoDigits(result, iDate.getMonth());
        result.append(".");
        result.append(iDate.getYear());
        return result.toString(); // dateFormat104.format(date); // format wrong works ((
    }

    public static String getStrDate112(Date date) {
        IDate iDate = getLocalIDate(date);
        StringBuilder result = new StringBuilder();
        result.append(iDate.getYear());
        addTwoDigits(result, iDate.getMonth());
        addTwoDigits(result, iDate.getDayOfMonth());
        return result.toString();
    }

    /**
     * Приведение даты/времени к формату HH:MI:SS в текущем часовом поясе
     * 
     * @param date
     *            UTC-дата/время
     * @return
     */
    public static String getStrDate108(Date date) {
        Calendar localeDateTime = getLocalCalendar(date);
        StringBuilder result = new StringBuilder();
        addTime(result, localeDateTime, true);
        return result.toString();
    }

    private static StringBuilder addTime(StringBuilder sb, Calendar localeDateTime, boolean withDelimeter) {
        addTwoDigits(sb, localeDateTime.get(Calendar.HOUR_OF_DAY));
        if (withDelimeter)
            sb.append(":");
        addTwoDigits(sb, localeDateTime.get(Calendar.MINUTE));
        if (withDelimeter)
            sb.append(":");
        addTwoDigits(sb, localeDateTime.get(Calendar.SECOND));
        return sb;
    }

    public static String getStrDate112full(Date date) {
        Calendar localeDateTime = getLocalCalendar(date);
        StringBuilder result = new StringBuilder();
        result.append(localeDateTime.get(Calendar.YEAR));
        addTwoDigits(result, localeDateTime.get(Calendar.MONTH) + 1);
        addTwoDigits(result, localeDateTime.get(Calendar.DAY_OF_MONTH));
        result.append(" ");
        addTime(result, localeDateTime, true);
        return result.toString();
    }

    /**
     * Приведение даты/времени к формату HHMISS в текущем часовом поясе
     * 
     * @param date
     *            UTC-дата/время
     * @return
     */
    public static String getStrDate108c(Date date) {
        Calendar localeDateTime = getLocalCalendar(date);
        StringBuilder result = new StringBuilder();
        addTime(result, localeDateTime, false);
        return result.toString();
    }

    public static Date getDateWithShiftSeconds(long seconds) {
        Calendar d1 = getLocalCalendar(new Date());
        d1.add(Calendar.SECOND, (int) seconds);
        midnight(d1);
        return d1.getTime();
    }

    public static Date getCurrentDateWithShift(int shiftHours) {
        Calendar d1 = getLocalCalendarByMillis(System.currentTimeMillis());
        d1.add(Calendar.HOUR, shiftHours);
        midnight(d1);
        return d1.getTime();
    }

    public static Date getYesterdayDate() {
        return getCurrentDateWithShift(-24);
    }

    public static Date getToday() {
        return getCurrentDateWithShift(0);
    }

    public static Date getNow() {
        return getLocalCalendarByMillis(System.currentTimeMillis()).getTime();
    }

    public static Date getFirstDatePreviousYear() {
        Calendar d1 = getLocalCalendar(new Date());
        midnight(d1);
        d1.set(Calendar.MONTH, 1);
        d1.set(Calendar.DAY_OF_MONTH, 1);
        return d1.getTime();
    }

    /**
     * Проверка строки на корректность значения даты заданного формата
     * 
     * @param strDate
     * @param dateFormat
     * @return
     */
    private static boolean checkDateFormat(String strDate, DateFormat dateFormat) {
        boolean result = true;
        Date date = null;
        try {
            date = dateFormat.parse(strDate);
            result = (dateFormat.format(date).equals(strDate)); // String -> Date -> String
            if (result) {
                int year = getYear(date);
                result = (year >= MIN_YEAR && year <= MAX_YEAR);
            }
        } catch (ParseException e) {
            result = false;
        }
        return result;
    }

    public static boolean checkDate112full(String strDate) {
        return checkDateFormat(strDate, dateFormat112full); // YYYYMMDD HH:MI:SS
    }

    public static boolean checkDate112(String strDate) {
        return checkDateFormat(strDate, dateFormat112); // YYYYMMDD
    }

    public static boolean checkDate104(String strDate) {
        return checkDateFormat(strDate, dateFormat104); // DD.MM.YYYY
    }

    public static Date getDate112(String strDate) throws ParseException {
        if (!checkDate112(strDate))
            throw new ParseException("Invalid format date <" + strDate + ">.", 0);
        int year = Integer.parseInt(strDate.substring(0, 4));
        int month = Integer.parseInt(strDate.substring(4, 6));
        int day = Integer.parseInt(strDate.substring(6, 8));
        Calendar g1 = getLocalCalendar(null);
        midnight(g1);
        g1.set(Calendar.YEAR, year);
        g1.set(Calendar.MONTH, month - 1);
        g1.set(Calendar.DAY_OF_MONTH, day);
        return g1.getTime(); // dateFormat112.parse(strDate); // parser wrong works ((
    }

    public static Date getDate112full(String strDate) throws ParseException {
        if (!checkDate112full(strDate))
            throw new ParseException("Invalid format date <" + strDate + ">.", 0);
        int year = Integer.parseInt(strDate.substring(0, 4));
        int month = Integer.parseInt(strDate.substring(4, 6));
        int day = Integer.parseInt(strDate.substring(6, 8));
        int hour = Integer.parseInt(strDate.substring(9, 11));
        int minute = Integer.parseInt(strDate.substring(12, 14));
        int second = Integer.parseInt(strDate.substring(15, 17));
        Calendar g1 = getLocalCalendar(null);
        g1.set(Calendar.YEAR, year);
        g1.set(Calendar.MONTH, month - 1);
        g1.set(Calendar.DAY_OF_MONTH, day);
        g1.set(Calendar.HOUR_OF_DAY, hour);
        g1.set(Calendar.MINUTE, minute);
        g1.set(Calendar.SECOND, second);
        g1.set(Calendar.MILLISECOND, 0);
        return g1.getTime();
    }

    public static Date getDate104(String strDate) throws ParseException {
        if (!checkDate104(strDate))
            throw new ParseException("Invalid format date <" + strDate + ">.", 0);
        int day = Integer.valueOf(strDate.substring(0, 2));
        int month = Integer.valueOf(strDate.substring(3, 5));
        int year = Integer.valueOf(strDate.substring(6, 10));
        Calendar g1 = getLocalCalendar(null);
        midnight(g1);
        g1.set(Calendar.YEAR, year);
        g1.set(Calendar.MONTH, month - 1);
        g1.set(Calendar.DAY_OF_MONTH, day);
        return g1.getTime(); // dateFormat104.parse(strDate); // no work ((
    }

    public static int getYear(Date date) {
        return getLocalCalendar(date).get(Calendar.YEAR);
    }

    public static int getMonth(Date date) {
        return getLocalCalendar(date).get(Calendar.MONTH);
    }

    public static int getDayOfMonth(Date date) {
        return getLocalCalendar(date).get(Calendar.DAY_OF_MONTH);
    }

    public static long getTimeInSeconds(String in) {
        if (Pattern.compile(FULL_TIME_MASK).matcher(in).matches()) {
            int hour = Integer.valueOf(in.substring(0, 2));
            int minute = Integer.valueOf(in.substring(3, 5));
            int second = Integer.valueOf(in.substring(6, 8));
            return ((hour * 60 + minute) * 60 + second);
        }
        if (Pattern.compile(SHORT_TIME_MASK).matcher(in).matches()) {
            return getTimeInSeconds(in + ":00");
        }
        return 0;
    }

    public static boolean checkFullTimeMask(String in) {
        if (Pattern.compile(FULL_TIME_MASK).matcher(in).matches()) {
            int hour = Integer.valueOf(in.substring(0, 2));
            int minute = Integer.valueOf(in.substring(3, 5));
            int second = Integer.valueOf(in.substring(6, 8));
            return (hour < 24 && minute < 60 && second < 60);
        }
        return false;
    }

    public static boolean checkTimeMask(String in) {
        if (Pattern.compile(SHORT_TIME_MASK).matcher(in).matches()) {
            return (Integer.valueOf(in.substring(0, 2)) < 24 && Integer.valueOf(in.substring(3, 5)) < 60);
        }
        return false;
    }
}
