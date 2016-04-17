package com.epam.common.igLib;

import java.util.Arrays;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "LegendTableModel")
public class LegendTableModel {

    public static class ColumnLegend {

        private String  keyData;
        private String  columnName;
        private String  columnNameHTML;
        private int     columnWidth;
        private int     columnWidthHTML;
        private String  defaultSummaryValue;
        private boolean summable;

        public ColumnLegend() {
        }

        public ColumnLegend(String keyData, String columnName) {
            this(keyData, columnName, 0, null, false);
        }

        public ColumnLegend(String keyData, String columnName, int columnWidth) {
            this(keyData, columnName, columnWidth, null, false);
        }

        public ColumnLegend(String keyData, String columnName, int columnWidth, boolean summable) {
            this(keyData, columnName, columnWidth, null, summable);
        }

        public ColumnLegend(String keyData, String columnName, int columnWidth, String defaultSummaryValue) {
            this(keyData, columnName, columnWidth, defaultSummaryValue, false);
        }

        private ColumnLegend(String keyData, String columnName, int columnWidth, String defaultSummaryValue,
                boolean summable) {
            this.keyData = keyData.trim();
            this.columnName = columnName.trim();
            this.columnWidth = columnWidth;
            this.defaultSummaryValue = defaultSummaryValue;
            this.summable = summable;
        }

        @XmlElement(name = "KeyData")
        public void setKeyData(String keyData) {
            this.keyData = keyData.trim();
        }

        public String getKeyData() {
            return keyData;
        }

        @XmlElement(name = "ColumnName")
        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }

        public String getColumnName() {
            return columnName;
        }

        @XmlElement(name = "ColumnNameHTML")
        public void setColumnNameHTML(String columnNameHTML) {
            this.columnNameHTML = columnNameHTML;
        }

        public String getColumnNameHTML() {
            if (columnNameHTML != null && !columnNameHTML.isEmpty())
                return columnNameHTML;
            else
                return columnName;
        }

        @XmlElement(name = "ColumnWidth")
        public void setColumnWidth(int columnWidth) {
            this.columnWidth = columnWidth;
        }

        public int getColumnWidth() {
            return columnWidth;
        }

        @XmlElement(name = "WidthHTML")
        public void setColumnWidthHTML(int columnWidthHTML) {
            this.columnWidthHTML = columnWidthHTML;
        }

        public int getColumnWidthHTML() {
            if (columnWidthHTML > 0)
                return columnWidthHTML;
            else
                return columnWidth;
        }

        @XmlElement(name = "DefaultSummaryValue")
        public String getDefaultSummaryValue() {
            return defaultSummaryValue;
        }

        @XmlElement(name = "Summable")
        public boolean isSummable() {
            return summable;
        }

        public void setDefaultSummaryValue(String defaultSummaryValue) {
            this.defaultSummaryValue = defaultSummaryValue;
        }

        public void setSummable(boolean summable) {
            this.summable = summable;
        }

        @Override
        public String toString() {
            return "ColumnLegend [keyData=" + keyData + ", columnName=" + columnName + ", columnNameHTML="
                    + columnNameHTML + ", columnWidth=" + columnWidth + ", columnWidthHTML=" + columnWidthHTML
                    + ", defaultSummaryValue=" + defaultSummaryValue + ", summable=" + summable + "]";
        }
    }

    private ColumnLegend[] columns;
    private String[]       captionLines;
    private String[]       tailLines;

    public ColumnLegend[] getColumns() {
        return columns;
    }

    @XmlElement(name = "CaptionLineHTML")
    public void setCaptionLineHTML(String[] captionLine) {
        this.captionLines = captionLine;
    }

    public String[] getCaptionLineHTML() {
        return captionLines;
    }

    @XmlElement(name = "TailLineHTML")
    public void setTailLineHTML(String[] headline) {
        this.tailLines = headline;
    }

    public String[] getTailLineHTML() {
        return tailLines;
    }

    @XmlElement(name = "Column")
    public void setColumns(ColumnLegend[] columns) {
        this.columns = columns;
    }

    public LegendTableModel() {
    }

    public int length() {
        if (columns == null)
            return 0;
        else
            return columns.length;
    }

    public int getColumnWidthHTML(int columnIndex) {
        if (columnIndex < 0 || columnIndex >= length())
            return 0;
        return columns[columnIndex].getColumnWidthHTML();
    }

    public Integer[] getColumnsWidth() {
        int len = length(); 
        if (len == 0)
            return null;
        Integer[] result = new Integer[len];
        for (int i = 0; i < len; i++)
            result[i] = Integer.valueOf(columns[i].columnWidth);
        return result;
    }

    public boolean isSummable(int columnIn) {
        if (columnIn < 0 || columnIn >= length())
            return false;
        return columns[columnIn].isSummable();
    }

    public String getDefaultSummaryValue(int columnIn) {
        if (columnIn < 0 || columnIn >= length())
            return "";
        return columns[columnIn].getDefaultSummaryValue();
    }

    public String getColumnName(int columnIn) {
        if (columnIn < 0 || columnIn >= length())
            return "";
        return columns[columnIn].getColumnName();
    }

    public String getColumnNameHTML(int columnIn) {
        if (columnIn < 0 || columnIn >= length())
            return "";
        return columns[columnIn].getColumnNameHTML();
    }

    public String getKeyData(int columnIn) {
        if (columnIn < 0 || columnIn >= length())
            return "";
        return columns[columnIn].getKeyData();
    }

    @Override
    public String toString() {
        return "LegendTableModel [columns=" + Arrays.toString(columns) + ", captionLines="
                + Arrays.toString(captionLines) + ", tailLines=" + Arrays.toString(tailLines) + "]";
    }
}
