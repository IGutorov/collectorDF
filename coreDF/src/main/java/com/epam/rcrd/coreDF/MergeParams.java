package com.epam.rcrd.coreDF;

import java.util.Date;

import javax.swing.table.AbstractTableModel;

import com.epam.rcrd.coreDF.CompareSystem.IOnePairRecType;
import com.epam.rcrd.coreDF.IMergerStarterCore.TableDesign;
import com.epam.rcrd.coreDF.IMergerStarterCore.TypeReconciliation;

import static com.epam.common.igLib.LibDateFormats.*;
import static com.epam.rcrd.coreDF.PackageConsts.*;

final class MergeParams {

    private static final int DEFAULT_LAG = 10;

    private final TypeReconciliation type;
    private final TableModelMerger   summaryTableModel;
    private final TableModelMerger   dataTableModel;
    private final TableModelMerger   dataClassicTableModel;
    private final IOnePairRecType    pairType;

    private Date    startDatePeriod;
    private Date    endDatePeriod;
    private Date    calcDate;
    private boolean balanceA;
    private String  account;
    private String  transportTypeDoc;
    private int     lagTimeMinutes = DEFAULT_LAG;

    private long   cutOffTimeForError;
    private String absoluteFileName;

    private String  errorMessage;
    private boolean messageIsWarning;
    private boolean completed;

    MergeParams(IOnePairRecType pairType) throws Exception {
        this.pairType = pairType;
        this.type = pairType.getType();

        summaryTableModel = TableModelMerger.getTableModelMerger(pairType.getSummaryLegendName(), LEGEND_SUMMARY, true);
        dataTableModel = TableModelMerger.getTableModelMerger(pairType.getLegendName(), pairType.getLegendDataTypes(), false);
        dataClassicTableModel = TableModelMerger.getTableModelMerger(pairType.getClassicLegendName(),
                pairType.getLegendDataTypes(), false);
    }

    boolean isParamsChecked() {
        switch (type) {
            case Turns:
            case AccountBalance:
                return (calcDate != null);
            case Documents:
                return (calcDate != null) && checkTypeDoc() && (lagTimeMinutes != 0);
            case AccountStatement:
                return (account != null) && (startDatePeriod != null) && (endDatePeriod != null);
            default:
                return false;
        }
    }

    private boolean checkTypeDoc() {
        if (transportTypeDoc != null && !transportTypeDoc.isEmpty())
            return true;
        return (pairType.getHiddenParams().indexOf("typeDoc") >= 0);
    }

    Date getDateStart() {
        return startDatePeriod;
    }

    Date getCalcDate() {
        return calcDate;
    }

    Date getMainDate() {
        if (type == TypeReconciliation.AccountStatement)
            return getDateEnd();
        else
            return getCalcDate();
    }

    Date getDateEnd() {
        return endDatePeriod;
    }

    boolean isBalanceA() {
        return balanceA;
    }

    String getAccount() {
        return account;
    }

    String getTransportTypeDoc() {
        return transportTypeDoc;
    }

    TypeReconciliation getType() {
        return type;
    }

    String getMainHeader(String GLProductName, String masterProductName) {
        return type.getMainHeader(this, GLProductName, masterProductName);        
    }

    Date getStartDatePeriod() {
        return startDatePeriod;
    }

    Date getEndDatePeriod() {
        return endDatePeriod;
    }

    String getAbsoluteFileName() {
        return absoluteFileName;
    }

    static Date convertStrToDate(final String value) throws Exception {
        if (value == null || value.isEmpty())
            return null;
        if ("today".equalsIgnoreCase(value)) // текуща€ дата (по локальному таймеру)
            return getToday();
        if ("yesterday".equalsIgnoreCase(value)) // вчерашн€€ дата (по локальному таймеру)
            return getYesterdayDate();
        // текуща€ дата с запазданием, по маске todayHH:MI
        // (сегодн€шн€€ - если врем€ локального таймера превышает заданное в маске, иначе вчерашн€€)
        if ("today".equalsIgnoreCase(value.substring(0, 5)) && value.length() == 10) {
                String time = value.substring(5, 10);
                if (checkTimeMask(time))               
                    return getDateWithShiftSeconds(-getTimeInSeconds(time));
        }
        if (checkDate112(value)) {
            return getDate112(value);
        }
        if (checkDate104(value)) {
            return getDate104(value);
        }

        return null;
    }

    private int convertStrToInt(final String value) throws Exception {
        int result = 0;
        if (value == null || value.isEmpty())
            return 0;
        try {
            result = Integer.valueOf(value);
        } catch (NumberFormatException e) {
            throw new Exception("Invalid integer value <" + value + ">", e);
        }
        return result;
    }

    void setParam(String param, String value) throws Exception {
        if (param == null)
            throw new Exception("Param not defined.");
        if (param.isEmpty())
            throw new Exception("Name param is empty.");

        if (param.equals("transportTypeDoc"))
            transportTypeDoc = value;
        else if (param.equals("account"))
            account = value;
        else if (param.equals("balanceA"))
            balanceA = ("true".equalsIgnoreCase(value));
        else if (param.equals("lagTime"))
            lagTimeMinutes = convertStrToInt(value);
        else if (param.equals("calcDate"))
            calcDate = convertStrToDate(value);
        else if (param.equals("startDatePeriod"))
            startDatePeriod = convertStrToDate(value);
        else if (param.equals("endDatePeriod"))
            endDatePeriod = convertStrToDate(value);
    }

    void setBalanceA(final boolean balanceA) {
        this.balanceA = balanceA;
    }

    void setStartDatePeriod(final Date startDatePeriod) {
        this.startDatePeriod = startDatePeriod;
    }

    void setEndDatePeriod(final Date endDatePeriod) {
        this.endDatePeriod = endDatePeriod;
    }

    void setAccount(final String account) {
        this.account = account;
    }

    void setLagTime(final int lagTime) {
        this.lagTimeMinutes = lagTime;
    }

    void setAbsoluteFileName(final String absoluteFileName) {
        this.absoluteFileName = absoluteFileName;
    }

    void setCompleted(boolean in) {
        if (messageIsWarning)
            completed = true;
        else
            completed = in;
    }

    boolean getCompleted() {
        return completed;
    }

    void setCompleted(final boolean in, final String message) {
        errorMessage = message;
        setCompleted(in);
    }

    void setNonCriticalMessage() {
        messageIsWarning = true;
    }

    String getErrorMessage() {
        return errorMessage;
    }

    void setStartTime(final long startTime) {
        cutOffTimeForError = startTime - lagTimeMinutes * 60;
    }

    boolean checkCutOffTime(final long inDateTime) {
        if (cutOffTimeForError == 0)
            return true;
        return (cutOffTimeForError > inDateTime);
    }

    AbstractTableModel getTableModel(final TableDesign tableDesign) {
        switch (tableDesign) {
            case SummaryTable:
                return summaryTableModel;
            case DataTable:
                return dataTableModel;
            case DataClassicTable:
                return dataClassicTableModel;
            default:
                return null;
        }
    }

    Integer[] getColumnSizes(final TableDesign tableDesign) {
        final AbstractTableModel curr = getTableModel(tableDesign);
        if (curr != null && curr instanceof TableModelMerger)
            return ((TableModelMerger) getTableModel(tableDesign)).getColumnSizes();
        else
            return null;
    }

    void uploadDataToModel(MergeResults mergeRes) {
        summaryTableModel.applyResultSet(mergeRes.getSummaryResult());
        dataTableModel.applyResultSet(mergeRes.getDivDetailArray());
        if (dataClassicTableModel != null)
            dataClassicTableModel.applyResultSet(mergeRes.getDivDetailArray());
    }

    ProductVersion getMasterProduct() {
        return pairType.getMasterProduct();
    }

    ProductVersion getGeneralProduct() {
        return pairType.getMasterProduct();
    }

    IOnePairRecType getPairType() { // ??
        return pairType;
    }

    boolean checkHiddenParam(String paramName) {
        String hiddenParams = pairType.getHiddenParams();
        return (hiddenParams != null && hiddenParams.lastIndexOf(paramName) != -1);
    }
}
