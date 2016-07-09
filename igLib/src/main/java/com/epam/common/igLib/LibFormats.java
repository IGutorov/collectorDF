package com.epam.common.igLib;

import static com.epam.common.igLib.LibFiles.UTF_CHARSET;
import static com.epam.common.igLib.LibFiles.WIN_CHARSET;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.stream.StreamSource;

public final class LibFormats {

    public static final String   EMPTY_STRING       = "";
    public static final String[] EMPTY_STRING_ARRAY = new String[] {};
    static final byte[]          EMPTY_BYTE_ARRAY   = new byte[] {};

    private LibFormats() {
    }

    public static int compareStringCharset(String current, String other, String charsetName)
            throws UnsupportedEncodingException {
        final byte[] currentB = (current == null) ? EMPTY_BYTE_ARRAY : current.getBytes(charsetName);
        final byte[] otherB = (other == null) ? EMPTY_BYTE_ARRAY : other.getBytes(charsetName);
        return compareArrayByte(currentB, otherB);
    }

    public static int compareString1251(String current, String other) throws UnsupportedEncodingException {
        return compareStringCharset(current, other, WIN_CHARSET);
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

    public static boolean hasDelimiter(String in) {
        if (in == null || in.isEmpty())
            return false;
        return (in.contains(" ") || in.contains(";") || in.contains("\t") || in.contains("\r") || in.contains("\n"));
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

    private static StringBuilder addLeadingZero(StringBuilder number) {
        switch (number.length()) {
            case 2:
                return number.insert(0, "0");
            case 1:
                return number.insert(0, "00");
            default:
                return number;
        }
    }

    /**
     * Функция перевода целочисленного числа (копеек/центов) в строку суммы с разделителем целой и дробной части и групп
     * разрядов. Пример convertLongToStringBuilderDMark(1234567, ".", true) вернёт строку "12 345.67"
     */
    private static StringBuilder convertLongToStringBuilderDMark(long in, char decimalMark, boolean addSpaces) {
        boolean negativeNum = (in < 0);
        if (negativeNum)
            in = -in;
        StringBuilder convertLong = new StringBuilder();
        convertLong.append(in);
        addLeadingZero(convertLong); // добавим лидирующие нули маленьким ("коротким") числам
        convertLong.insert(convertLong.length() - 2, decimalMark); // добавим разделитель целой и дробной части
        StringBuilder result = addSpaces ? addSpacesToAmount(convertLong) : convertLong;
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
     * Возвращает отсортированный объединённый массив неповторяющихся строк и добавляет к результату пустую строку
     * 
     * @param array1
     * @param array2
     * @return
     */
    public static String[] concatDisticnctArrayString(String[] array1, String[] array2) {
        return concatDisticnctArrayString(array1, array2, true);
    }

    public static String[] concatDisticnctArrayString(String[] array1, String[] array2, boolean addEmptyStr) {
        Set<String> set = new HashSet<String>();
        if (array1 != null)
            set.addAll(Arrays.asList(array1));
        if (array2 != null)
            set.addAll(Arrays.asList(array2));
        if (addEmptyStr)
            set.add(EMPTY_STRING);
        String[] result = getStringList(set.toArray());
        Arrays.sort(result);
        return result;
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

    public static String getGUID() {
        return UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }

    private static Object convertXMLToObject(Reader readerXML, Class<?> objectClass) throws JAXBException {
        StreamSource streamSource = new StreamSource(readerXML);
        return JAXBContext.newInstance(objectClass).createUnmarshaller().unmarshal(streamSource);
    }

    public static Object convertXMLToObject(InputStream streamXML, String charsetName, Class<?> objectClass)
            throws JAXBException, UnsupportedEncodingException {
        if (charsetName == null)
            charsetName = UTF_CHARSET;
        return convertXMLToObject(new InputStreamReader(streamXML, charsetName), objectClass);
    }

    public static Object convertXMLToObject(String stringXML, Class<?> objectClass) throws JAXBException {
        return convertXMLToObject(new StringReader(stringXML), objectClass);
    }

    public static Object convert2XMLToObject(String XML, Class<?> objectClass) throws JAXBException {
        StreamSource streamSource = new StreamSource(new StringReader(XML));
        return JAXBContext.newInstance(objectClass).createUnmarshaller().unmarshal(streamSource);
    }

    public static String convertObjectToXML(Object object) throws JAXBException {
        Marshaller marshaller = JAXBContext.newInstance(object.getClass()).createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        StringWriter stringWriter = new StringWriter();
        marshaller.marshal(object, stringWriter);
        return stringWriter.toString();
    }

    static StringBuilder addTwoDigits(StringBuilder sb, int num) {
        if (num < 0 || num > 99)
            return sb.append("00");
        if (num < 10)
            return sb.append('0').append(num);
        else
            return sb.append(num);
    }

}
