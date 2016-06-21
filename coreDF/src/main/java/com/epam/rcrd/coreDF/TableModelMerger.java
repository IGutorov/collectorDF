package com.epam.rcrd.coreDF;

import javax.swing.table.AbstractTableModel;

import static com.epam.rcrd.coreDF.PackageConsts.*;

import com.epam.common.igLib.LegendArrayOfNamedObjects;
import com.epam.common.igLib.LegendTableModel;
import com.epam.common.igLib.LibFiles;

import static com.epam.common.igLib.LibFiles.*;
import static com.epam.common.igLib.LibFormats.*;

import com.epam.common.igLib.Money100;

final class TableModelMerger extends AbstractTableModel {

    private static final long               serialVersionUID = 1L;

    private final LegendTableModel          tableLegend;
    private final LegendArrayOfNamedObjects dataFields;
    private final boolean                   withSummaryRow;

    private IParamPutGet[]                  resultData;

    private static final String             DESIGN_DIRECTORY = "design";

    TableModelMerger(final String legendName, final LegendArrayOfNamedObjects LegendFields, final boolean withSummaryRow)
            throws Exception {
        this.withSummaryRow = withSummaryRow;
        tableLegend = (LegendTableModel) convertXMLToObject(LibFiles.getResource(legendName, DESIGN_DIRECTORY), null,
                LegendTableModel.class);
        dataFields = LegendFields;
    }

    static TableModelMerger getTableModelMerger(String legendName, LegendArrayOfNamedObjects LegendFields,
            boolean withSummaryRow) throws Exception {
        if (legendName == null || legendName.isEmpty())
            return null;
        return new TableModelMerger(legendName, LegendFields, withSummaryRow);
    }

    @Override
    public int getRowCount() {
        if (resultData == null)
            return 0;

        if (withSummaryRow)
            return resultData.length + 1;
        else
            return resultData.length;
    }

    @Override
    public int getColumnCount() {
        if (tableLegend == null)
            return 0;
        else
            return tableLegend.length();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (tableLegend == null || dataFields == null)
            return String.class; // null ??
        Class<?> result = dataFields.getFieldClass(tableLegend.getKeyData(columnIndex));
        if (result == null)
            System.err.println("Error data tableLegend or dataFields"); // Exception ??
        return result;
    }

    @Override
    public String getColumnName(final int columnIn) {
        if (tableLegend == null)
            return super.getColumnName(columnIn);
        else
            return tableLegend.getColumnName(columnIn);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (resultData == null || tableLegend == null)
            return null;

        if (rowIndex < resultData.length)
            return resultData[rowIndex].getObject(tableLegend.getKeyData(columnIndex));

        if (rowIndex == resultData.length && withSummaryRow) {
            Object defaultSummary = tableLegend.getDefaultSummaryValue(columnIndex);
            if (defaultSummary != null)
                return defaultSummary;
            if (tableLegend.isSummable(columnIndex))
                return getSumColumn(columnIndex);
        }
        return null;
    }

    private Object getSumColumn(final int columnIndex) {
        Class<?> columnClass = getColumnClass(columnIndex);
        if (columnClass != Money100.class && columnClass != Integer.class)
            return null;

        Object result = null;
        long resSum = 0;
        String keyData = tableLegend.getKeyData(columnIndex);
        for (IParamPutGet curr : resultData) {
            if (curr.getObject(keyData) instanceof Money100)
                resSum += ((Money100) curr.getObject(keyData)).getAmount();
            if (curr.getObject(keyData) instanceof Integer)
                resSum += ((Integer) curr.getObject(keyData)).longValue();
        }
        if (columnClass == Money100.class)
            result = new Money100(resSum);
        if (columnClass == Integer.class)
            result = Integer.valueOf((int) resSum);
        return result;
    }

    String getCaptionLineHTML() {
        final StringBuilder result = new StringBuilder();
        for (String curr : tableLegend.getCaptionLineHTML())
            result.append(curr).append(LINE_SEPARATOR);
        return result.toString();
    }

    String getTailLineHTML() {
        final StringBuilder result = new StringBuilder();
        for (String curr : tableLegend.getTailLineHTML())
            result.append(curr).append(LINE_SEPARATOR);
        return result.toString();
    }

    Integer[] getColumnSizes() { // ?? rename ?? getWidthOfColumns
        return tableLegend.getColumnsWidth();
    }

    String getLegendHTML() {
        final StringBuilder result = new StringBuilder();
        result.append("<tr>").append(LINE_SEPARATOR);
        int columnCount = getColumnCount();
        for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
            result.append("<td width=").append(tableLegend.getColumnWidthHTML(columnIndex)).append(">");
            result.append(tableLegend.getColumnNameHTML(columnIndex));
            result.append("</td>").append(LINE_SEPARATOR);
        }
        result.append("</tr>").append(LINE_SEPARATOR);
        return result.toString();
    }

    String getDataHTML() {
        StringBuilder result = new StringBuilder();
        final int columnCount = getColumnCount();
        final int rowCount = getRowCount();
        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            final StringBuilder sbLine = new StringBuilder();
            for (int columnIndex = 0; columnIndex < columnCount; columnIndex++)
                sbLine.append("<td>").append(getValueAt(rowIndex, columnIndex)).append("</td>");
            result.append("<tr>").append(sbLine).append("</tr>").append(LINE_SEPARATOR);
        }
        return result.toString();
    }

    void applyResultSet(final IGetResultSet resultSet) {
        if (resultSet == null)
            return;
        resultData = resultSet.getResultArray();
        if (resultData != null)
            fireTableDataChanged();
    }
}
