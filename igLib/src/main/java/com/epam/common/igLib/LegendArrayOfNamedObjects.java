package com.epam.common.igLib;

public final class LegendArrayOfNamedObjects {

    public static final class ClassField {
        private final String   fieldName;
        private final Class<?> fieldClass;

        public ClassField(final String fieldName, final Class<?> fieldClass) {
            this.fieldName = fieldName.trim();
            this.fieldClass = fieldClass;
        }

        private String getFieldName() {
            return fieldName;
        }

        private Class<?> getFieldClass() {
            return fieldClass;
        }
    }

    private final ClassField[] fieldList;

    public LegendArrayOfNamedObjects(ClassField[] fields) {
        this.fieldList = fields.clone();
    }

    public int length() {
        if (fieldList == null)
            return 0;
        else
            return fieldList.length;
    }

    public boolean checkObjectClass(final String keyData, final Class<?> objClass) {
        if (objClass == null)
            return false;
        int pos = getFieldIndex(keyData);
        if (pos == -1)
            return false;
        else
            return (objClass == fieldList[pos].getFieldClass());
    }

    public int getCheckedFieldIndex(final String keyData, final Class<?> objClass) {
        if (objClass == null)
            return -1;
        int result = getFieldIndex(keyData);
        if (result != -1)
            if (objClass != fieldList[result].getFieldClass())
                return -1;
        return result;
    }

    public int getFieldIndex(final String keyData) {
        if (keyData != null)
            for (int i = 0; i < fieldList.length; i++)
                if (keyData.equals(fieldList[i].getFieldName()))
                    return i;
        return -1;
    }

    public String[] getKeys() {
        String[] result = null;
        int len = length();
        if (len > 0) {
            result = new String[len];
            for (int i = 0; i < len; i++)
                result[i] = fieldList[i].getFieldName();
        }
        return result;
    }

    public Class<?> getFieldClass(final String keyData) {
        int pos = getFieldIndex(keyData);
        if (pos == -1)
            return null;
        else
            return fieldList[pos].getFieldClass();
    }

    public Class<?>[] getFieldsClasses() {
        Class<?>[] result = null;
        int len = length();
        if (len > 0) {
            result = new Class<?>[len];
            for (int i = 0; i < len; i++)
                result[i] = fieldList[i].getFieldClass();
        }
        return result;
    }
}
