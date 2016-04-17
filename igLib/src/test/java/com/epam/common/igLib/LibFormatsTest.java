package com.epam.common.igLib;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;
import static com.epam.common.igLib.LibFormatsNew.*;

public class LibFormatsTest {

    @Test
    public void longToStrWithCommaLong() {
        String get = longToStrWithComma(100234);
        assertEquals("100234 kopeks is writen as 1 002,34", get, "1 002,34");
    }

    @Test
    public void longToStrWithCommaShort() {
        String get = longToStrWithComma(7L);
        assertEquals("7 cents is writen as 0,07", get, "0,07");
    }

    @Test
    public void longToStrWithCommaZero() {
        String get = longToStrWithComma(0);
        assertEquals("0 cents is writen as 0,00", get, "0,00");
    }

    @Test
    public void longToStrWithCommaNegative() {
        assertEquals(longToStrWithComma(-987654321), "-9 876 543,21");
    }

    @Test
    public void concatDisticnctArrayStringNullParams() {
        String[] get = concatDisticnctArrayString(null, null);
        assertArrayEquals("Return empty with null params", get, EMPTY_STRING_ARRAY);
    }

    @Test
    public void concatDisticnctArrayStringEmptyParams() {
        String[] get = concatDisticnctArrayString(EMPTY_STRING_ARRAY, EMPTY_STRING_ARRAY);
        assertArrayEquals("Return empty with empty params", get, EMPTY_STRING_ARRAY);
    }

    @Test
    public void concatDisticnctArrayStringShouldSortedArray() {
        String[] param2 = new String[] {"what", "is", "it"};
        String[] get = concatDisticnctArrayString(null, param2);
        Arrays.sort(param2);
        assertArrayEquals("Return sorted one array when another null", get, param2);
    }

    @Test
    public void concatDisticnctArrayStringShouldSortedDistictedArrays() {
        String[] param1 = new String[] {"What", "is", "it", "?"};
        String[] param2 = new String[] {"This", "is", "salt", ".", "What", "are you", "doing", "?"};
        String[] distinctResult = new String[] {"This", "is", "salt", ".", "What", "are you", "doing", "?" , "it" };
        Arrays.sort(distinctResult);
        String[] get = concatDisticnctArrayString(param1, param2);
        assertArrayEquals("Return should be sorted and disticted", get, distinctResult);
    }
}
