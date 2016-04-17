package com.epam.rcrd.coreDF;

class LoopCounter {

    // limiters
    private static final int MAX_COUNT_COMPARED;
    private static final int MAX_COUNT_ERROR;

    private static final int DEFAULT_MAX_COUNT_COMPARED = 20000000; // больше 20 миллионов - это зацикливание
    private static final int DEFAULT_MAX_COUNT_ERROR    = 50000;    // ограничение "ничего не сходится"
    private static final int MIN_COUNT_COMPARED         = 1000;
    private static final int MIN_COUNT_ERROR            = 100;

    static {
        MAX_COUNT_COMPARED = getIntGlobalProperty("Compared.maxCountCompared", DEFAULT_MAX_COUNT_COMPARED, MIN_COUNT_COMPARED);
        MAX_COUNT_ERROR = getIntGlobalProperty("Compared.maxCountError", DEFAULT_MAX_COUNT_ERROR, MIN_COUNT_ERROR);
    }

    private static int getIntGlobalProperty(final String propertyName, final int defaultValue, final int minValue) {
        int result = defaultValue;
        final String strValue = PackageProperties.getProperty(propertyName);
        if (!strValue.isEmpty()) {
            int propValue = Integer.parseInt(strValue);
            if (propValue >= minValue)
                result = propValue;
        }
        return result;
    }

    private int  countError;
    private int countFetch;
    
    LoopCounter() {
        countError = 0;
        countFetch = 0;
    }

    void next() throws Exception {
        // Break infinite loop
        if (++countFetch >= MAX_COUNT_COMPARED) {
            throw new Exception("Ошибка. Count Res > " + countFetch + " . выполнение сверки прервано.");
        }
    }

    void addError() throws Exception {
        // Break overflow
        if (++countError > MAX_COUNT_ERROR) {
            throw new Exception("Ошибка. Count Diff > " + countError + " . выполнение сверки прервано.");
        }

    }
}
