package com.epam.common.igLib;

import java.math.BigDecimal;
import java.math.BigInteger;
import static com.epam.common.igLib.LibFormats.*;

public final class Money100 implements Comparable<Money100> {    

    private static final int        FRACTIONAL_SCALE = 2;
    private static final BigDecimal CENT_DIMENSION   = new BigDecimal(100);

    private static final char COMMA               = ',';
    private static final char ALTER_RADIX_POINT   = '.';
    private static final char DEFAULT_RADIX_POINT = COMMA;

    private final long value;
    private final char radixPoint;

    public Money100(long amount) {
        this(amount, DEFAULT_RADIX_POINT);
    }

    public Money100(long value, char radixPoint) {
        this.value = value;
        this.radixPoint = checkRadixPoint(radixPoint);
    }
    
    public Money100(BigDecimal bigValue) {
        this(bigValue, DEFAULT_RADIX_POINT);
    }

    public Money100(BigDecimal bigValue, char radixPoint) {
        this(bigValue.multiply(CENT_DIMENSION).longValue(), radixPoint);
    }

    private char checkRadixPoint(char in) {
        return (in == ALTER_RADIX_POINT) ? ALTER_RADIX_POINT : DEFAULT_RADIX_POINT;
    }

    public static Money100 subtraction(Money100 minuend, Money100 subtrahend) {
        return new Money100(minuend.value - subtrahend.value);
    }

    public static Money100 addition(Money100 summand1, Money100 summand2) {
        return new Money100(summand1.value + summand2.value);
    }

    @Override
    public String toString() {
        return longToStrWithDelimiter(value, radixPoint);
    }

    @Override
    public int compareTo(Money100 another) {
        return (value < another.value ?  -1 : (value == another.value ? 0 : 1));
    }

    public BigDecimal getBigDecimal() {
        return new BigDecimal(BigInteger.valueOf(value), FRACTIONAL_SCALE);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (value ^ (value >>> 32));
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
        Money100 other = (Money100) obj;
        if (value != other.value)
            return false;
        return true;
    }

    public long getAmount() {
        return value;        
    }
}
