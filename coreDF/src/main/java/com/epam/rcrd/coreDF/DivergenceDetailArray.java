package com.epam.rcrd.coreDF;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.epam.rcrd.coreDF.PackageConsts.*;
import com.epam.common.igLib.LegendArrayOfNamedObjects;

final class DivergenceDetailArray implements IGetResultSet {

    private class DivDetail implements IParamPutGet, ISelfLocking {

        private final Object[] values;

        private boolean locked;

        private DivDetail() {
            values = ((getParamsCount() > 0) ? new Object[getParamsCount()] : null);
        }

        @Override
        public void put(final String keyData, final Object value) {
            if (locked || value == null)
                return;
            int pos = fields.getCheckedFieldIndex(keyData, value.getClass());
            if (pos != -1)
                values[pos] = value;
        }

        @Override
        public Object getObject(final String keyData) {
            int pos = fields.getFieldIndex(keyData);
            if (pos == -1)
                return "";
            return values[pos];
        }

        @Override
        public void lock() {
            locked = true;            
        }
        
        @Override
        public String toString() {
            return "DivDetail [values=" + Arrays.toString(values) + ", locked=" + locked + "]";
        }
    }

    private int rowNumber;
    private final List<IParamPutGet>        resultList;
    private final LegendArrayOfNamedObjects fields;

    private static final IParamPutGet[] EMPTY_DIVLIST = new IParamPutGet[] {};
    private IParamPutGet[] resultArray = EMPTY_DIVLIST;

    DivergenceDetailArray(final LegendArrayOfNamedObjects fieldsLegend) {
        fields = fieldsLegend;
        resultList = new ArrayList<IParamPutGet>();
        rowNumber = 0;
    }

    private int getParamsCount() {
        return fields.length();
    }

    IParamPutGet getNewDivergenceDetail() {
        return (IParamPutGet) new DivDetail();
    }

    void add(final IParamPutGet divDetail) {
        rowNumber++;
        divDetail.put("rowNumber", Integer.valueOf(rowNumber));
        if (divDetail instanceof ISelfLocking)
            ((ISelfLocking) divDetail).lock();
        resultList.add(divDetail);
        resultArray = EMPTY_DIVLIST;
    }

    @Override
    public IParamPutGet[] getResultArray() {
        if (EMPTY_DIVLIST.equals(resultArray) && !resultList.isEmpty()) {
            int len = resultList.size();
            resultArray = new IParamPutGet[len];
            for (int i = 0; i < len; i++)
                resultArray[i] = resultList.get(i);
        }
        return resultArray;
    }

    interface ISelfLocking {
        void lock();
    }

    @Override
    public String toString() {
        return "DivergenceDetailArray [rowNumber=" + rowNumber + ", resultList=" + resultList + ", fields=" + fields
                + ", resultArray=" + Arrays.toString(getResultArray()) + "]";
    }
}
