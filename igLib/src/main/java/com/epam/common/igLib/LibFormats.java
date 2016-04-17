package com.epam.common.igLib;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.stream.StreamSource;

public final class LibFormats {

    public static final String[] EMPTY_STRING_ARRAY = new String[] {};

    private static final String DEFAULT_TIMEZONE = "GMT+3";

    private static final int        MIN_YEAR          = 2000;
    private static final int        MAX_YEAR          = 2050;
    private static final DateFormat dateFormat112     = new SimpleDateFormat("yyyyMMdd");
    private static final DateFormat dateFormat112full = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
    private static final DateFormat dateFormat104     = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.GERMANY); // DD.MM.YYYY

    private static final String TWO_DIGITS_MASK = "\\d\\d";
    private static final String SHORT_TIME_MASK = TWO_DIGITS_MASK + ":" + TWO_DIGITS_MASK;
    private static final String FULL_TIME_MASK  = TWO_DIGITS_MASK + ":" + TWO_DIGITS_MASK + ":" + TWO_DIGITS_MASK;

    private LibFormats() {
    }

    private static TimeZone getCurrentTimeZone() {
        String timeZone = PackageProperties.getProperty("Default.TimeZone");
        if (timeZone.isEmpty())
            timeZone = DEFAULT_TIMEZONE;
        return TimeZone.getTimeZone(timeZone);
    }

    /**
     * Процедура получения даты/времени с учётом часового пояса.
     * java.time.LocalTime/java.time.LocalDate/java.time.LocalDataTime поддерживается только в Java 8
     * 
     * @param date
     *            UTC-дата/время
     * @return дата/время приведённое к заданному (в настройке) часовому поясу
     */
    private static GregorianCalendar getGregorianCalendar(Date date) {
        GregorianCalendar result = new GregorianCalendar(getCurrentTimeZone());
        long time = 0;
        if (date != null)
            time = date.getTime();
        result.setTimeInMillis(time);
        return result;
    }

    private static void midnight(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    public static String getStrDate104(Date date) {
        return dateFormat104.format(date);
    }

    public static String getStrDate112(Date date) {
        return dateFormat112.format(date);
    }

    /**
     * Приведение даты/времени к формату HH:MI:SS в текущем часовом поясе
     * 
     * @param date
     *            UTC-дата/время
     * @return
     */
    public static String getStrDate108(Date date) {
        GregorianCalendar localeDateTime = getGregorianCalendar(date);
        StringBuilder result = new StringBuilder();
        result = addTime(result, localeDateTime, true);
        return result.toString();
    }

    private static StringBuilder addTwoDigits(StringBuilder sb, int num) {
        if (num < 0 || num > 99)
            return sb.append("00");
        if (num < 10)
            return sb.append('0').append(num);
        else
            return sb.append(num);
    }

    private static StringBuilder addTime(StringBuilder sb, GregorianCalendar localeDateTime, boolean withDelimiter) {
        sb = addTwoDigits(sb, localeDateTime.get(Calendar.HOUR_OF_DAY));
        if (withDelimiter)
            sb.append(":");
        sb = addTwoDigits(sb, localeDateTime.get(Calendar.MINUTE));
        if (withDelimiter)
            sb.append(":");
        sb = addTwoDigits(sb, localeDateTime.get(Calendar.SECOND));        
        return sb;
    }
    
    public static String getStrDate112full(Date date) {
        GregorianCalendar localeDateTime = getGregorianCalendar(date);
        StringBuilder result = new StringBuilder();
        result.append(localeDateTime.get(Calendar.YEAR));
        result = addTwoDigits(result, localeDateTime.get(Calendar.MONTH) + 1);
        result = addTwoDigits(result, localeDateTime.get(Calendar.DAY_OF_MONTH));
        result.append(" ");
        result = addTime(result, localeDateTime, true);
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
        GregorianCalendar localeDateTime = getGregorianCalendar(date);
        StringBuilder result = new StringBuilder();
        result = addTime(result, localeDateTime, false);
        return result.toString();      
    }

    public static Date getDateWithShiftSeconds(long seconds) {
        GregorianCalendar d1 = getGregorianCalendar(new Date());
        d1.add(Calendar.SECOND, (int) seconds);
        midnight(d1);
        return d1.getTime();
    }

    public static Date getCurrentDateWithShift(int shiftHours) {
        GregorianCalendar d1 = getGregorianCalendar(new Date());
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

    public static Date getFirstDatePreviousYear() {
        GregorianCalendar d1 = getGregorianCalendar(null);
        midnight(d1);
        d1.set(Calendar.YEAR, getCurrentYear() - 1);
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
        GregorianCalendar g1 = getGregorianCalendar(null);
        midnight(g1);
        g1.set(Calendar.YEAR, year);
        g1.set(Calendar.MONTH, month - 1); // WTF
        g1.set(Calendar.DAY_OF_MONTH, day);
        return g1.getTime(); // dateFormat112.parse(strDate); // parser no work ((
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
        GregorianCalendar g1 = getGregorianCalendar(null);
        g1.set(Calendar.YEAR, year);
        g1.set(Calendar.MONTH, month - 1); // WTF
        g1.set(Calendar.DAY_OF_MONTH, day);
        g1.set(Calendar.HOUR, hour);
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
        GregorianCalendar g1 = getGregorianCalendar(null);
        midnight(g1);
        g1.set(Calendar.YEAR, year);
        g1.set(Calendar.MONTH, month - 1); // WTF
        g1.set(Calendar.DAY_OF_MONTH, day);
        return g1.getTime(); // dateFormat104.parse(strDate); // no work ((
    }

    private static int getCurrentYear() {
        return Calendar.getInstance().get(Calendar.YEAR); // TimeZone no matter
    }

    public static int getYear(Date date) {
        return getGregorianCalendar(date).get(Calendar.YEAR);
    }

    public static int getMonth(Date date) {
        return getGregorianCalendar(date).get(Calendar.MONTH);
    }

    public static int getDayOfMonth(Date date) {
        return getGregorianCalendar(date).get(Calendar.DAY_OF_MONTH);
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

    private static final byte[] EMPTY_BYTE_ARRAY = new byte[] {};

    public static int compareStringCharset(String current, String other, String charsetName)
            throws UnsupportedEncodingException {
        final byte[] currentB = (current == null) ? EMPTY_BYTE_ARRAY : current.getBytes(charsetName);
        final byte[] otherB = (other == null) ? EMPTY_BYTE_ARRAY : other.getBytes(charsetName);
        return compareArrayByte(currentB, otherB);
    }

    public static int compareString1251(String current, String other) throws UnsupportedEncodingException {
        return compareStringCharset(current, other, "cp1251");
    }

    private static int compareArrayByte(final byte[] current, final byte[] other) {
        final int len = current.length;
        final int lenOther = other.length;
        final int n = Math.min(len, lenOther);
        for (int i = 0; i < n; i++)
            if (current[i] != other[i]) {
                int sign = Integer.signum((int) current[i]);
                int otherSign = Integer.signum((int) other[i]);
                return (sign == otherSign ? current[i] - other[i] : otherSign - sign);
            }
        return len - lenOther;
    }

    /**
     * Функция добавления пробелов для разделения групп разрядов в строку содержащую сумму и оканчивающуюся на
     * разделитель и два дробных знака после него
     */
    private static StringBuilder addSpacesToAmount(StringBuilder source) {
        StringBuilder reverseStr = new StringBuilder(source.reverse());
        int lenSource = source.length();
        int nDel = 3; // разделяем по 3 разряда
        int posFin = 2 + 1; // два знака под копейки/центы + один под разделитель целой и дробной части
        int posSt = 0;
        StringBuilder withSpaces = new StringBuilder();
        do {
            if (posSt > 0)
                withSpaces.append(" ");
            posFin += nDel;
            withSpaces.append(reverseStr.substring(posSt, Math.min(posFin, lenSource)));
            posSt = posFin;
        } while (posFin < lenSource);

        return withSpaces.reverse();
    }

    /**
     * Функция перевода целочисленного числа (копеек/центов) в строку суммы с разделителем целой и дробной части и групп
     * разрядов. Пример convertLongToStringBuilderDMark(1234567, ".", true) вернёт строку "12 345.67"
     */
    private static StringBuilder convertLongToStringBuilderDMark(long in, char decimalMark, boolean addSpaces) {
        boolean negativeNum = (in < 0); // признак для отриц. чисел
        if (negativeNum)
            in = -in;
        StringBuilder convertLong = new StringBuilder();
        convertLong.append(in);
        switch (convertLong.length()) { // добавим лидирующие нули маленьким ("коротким") числам
            case 2:
                convertLong.insert(0, "0");
            break;
            case 1:
                convertLong.insert(0, "00");
            break;
            default:
            break;
        }
        convertLong.insert(convertLong.length() - 2, decimalMark); // добавим разделитель целой и дробной части
        StringBuilder result = convertLong;
        if (addSpaces)
            result = addSpacesToAmount(convertLong); // добавим разделители групп-разрядов (пробелы)
        if (negativeNum)
            result.insert(0, "-");
        return result;
    }

    public static String longToStrWithDelimiter(long in, char decimalMark) {
        return convertLongToStringBuilderDMark(in, decimalMark, true).toString();
    }

    public static String longToStrWithComma(long in) {
        return convertLongToStringBuilderDMark(in, ',', true).toString();
    }

    public static String longToStrWithDot(long in) {
        return convertLongToStringBuilderDMark(in, '.', true).toString();
    }

    /**
     * Возвращает массив строк(метод toString()) по массиву объектов
     * 
     * @param in
     * @return
     */
    public static String[] getStringList(Object[] in) {
        if (in == null || in.length == 0)
            return EMPTY_STRING_ARRAY;
        int len = in.length;
        String[] result = new String[len];
        for (int i = 0; i < len; i++)
            result[i] = in[i].toString();
        return result;
    }

    private static Object convertXMLToObject(Reader readerXML, Class<?> objectClass) throws JAXBException {
        StreamSource streamSource = new StreamSource(readerXML);
        return JAXBContext.newInstance(objectClass).createUnmarshaller().unmarshal(streamSource);
    }

    public static Object convertXMLToObject(InputStream streamXML, String charsetName, Class<?> objectClass) throws JAXBException, UnsupportedEncodingException {
        if (charsetName == null)
            charsetName = LibFilesNew.UTF_CHARSET;        
        
        return convertXMLToObject(new InputStreamReader(streamXML, charsetName), objectClass);
    }
    
    public static Object convertXMLToObject(String stringXML, Class<?> objectClass) throws JAXBException {
        return convertXMLToObject(new StringReader(stringXML), objectClass);
    }

    public static String convertObjectToXML(Object object) throws JAXBException {
        Marshaller marshaller = JAXBContext.newInstance(object.getClass()).createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        StringWriter stringWriter = new StringWriter();
        marshaller.marshal(object, stringWriter);
        return stringWriter.toString();
    }

}
