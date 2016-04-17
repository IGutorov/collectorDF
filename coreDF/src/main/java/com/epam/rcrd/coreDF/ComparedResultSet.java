package com.epam.rcrd.coreDF;

import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import static com.epam.rcrd.coreDF.PackageConsts.*;

import com.epam.common.igLib.ISaveTrace;

import static com.epam.common.igLib.LibFormats.*;
import static com.epam.common.igLib.LibFilesNew.*;

import com.epam.common.igLib.TimeProfiler;
import com.epam.rcrd.coreDF.PackageConsts.ConnectionSide;

abstract class ComparedResultSet extends Thread {

    private final Merger.IRunQuery runQuery;
    protected final ConnectionSide comparedSide;
    protected final MergeParams    mergeParams;
    protected final ISaveTrace     saveTrace;
    private final String           query;

    private static final String SQL_DIRECTORY = "sql"; 
    
    protected ResultSet resultSet;
    private boolean     successGetResultSet; // ??

    // DataSet
    private boolean firstFetch = true;
    boolean         isFetched  = false;

    // DataSet. Key attribute
    String resourceNumber19 = null;
    byte   turnCharType     = 0;
    String externalID       = null;

    // (package) DataSet ??. Check attribute
    long   amount;
    String docNumber;
    long   amountDeb;
    long   amountCre;
    String accountDeb;
    String accountCre;
    //    protected boolean hasDebAcc;
    //    protected boolean hasCreAcc;
    Date   inDateTime = null;

    String mainCriterion;
    String secondCriterion;

    protected String prevResourceNumber19 = null;
    protected byte   prevTurnCharType     = 0;
    protected String prevExternalID       = null;
    protected String prevDocNumber        = null;

    ComparedResultSet(final Merger.IRunQuery runQuery, final ConnectionSide comparedSide, final MergeParams mergeParams,
            final ISaveTrace saveTrace) {
        this.runQuery = runQuery;
        this.comparedSide = comparedSide;
        this.mergeParams = mergeParams;
        this.saveTrace = saveTrace;
        this.query = getSQLQueryText(comparedSide);
    }

    private boolean queryExecute() {
        try {
            resultSet = runQuery.getResultSet(query);
        } catch (SQLException e) {
            saveTrace.saveException(e);
            return false;
        }
        return true;
    }

    boolean getSuccess() {
        return successGetResultSet; // ??
    }

    @Override
    public void run() {
        firstFetch = true;

        final TimeProfiler timeProfiler = new TimeProfiler();

        successGetResultSet = queryExecute();

        String finishMessage = String.format(mergeParams.getType().getFinishMessageTemplate(),
                runQuery.getInterfaceProductName());
        saveTrace.saveMessage("");
        saveTrace.saveMessage(finishMessage + ((timeProfiler.getTimeInterval()) / 100) / 10.0 + " сек.");
    }

    protected long getInDateTime() {
        return 0;
    }

    protected long getDocIDInner() {
        try {
            return resultSet.getLong("DOCID");
        } catch (SQLException e) {
            saveTrace.saveException(e);
            return 0;
        }
    }

    protected String getDocID() {
        return null;
    }

    protected String getResNumber() {
        return null;
    }

    protected String getResNumber(byte turnCharType) {
        return null;
    }

    private String getStringResultInner(String columnName) {
        try {
            return resultSet.getString(columnName);
        } catch (SQLException e) {
            saveTrace.saveException(e);
            return null;
        }
    }

    protected Long getLongAmountResult(String columnName) throws SQLException {
        return resultSet.getLong(columnName);
    }

    protected String getKeyValue(String key) throws Exception {
        String result = runQuery.getKeyValue(resultSet, key); // depends on DBMS
        return (result == null) ? "" : result.trim();
    }

    protected String getStringResult(final String columnName) {
        String result = getStringResultInner(columnName);
        return (result == null) ? "" : result.trim();
    }

    protected String getTurnParam(final String columnName) {
        return null;
    }

    abstract protected void getFetchedValues() throws Exception;

    abstract protected int compareFetchKey(ComparedResultSet other) throws Exception;

    abstract protected int compareFetchedValue(ComparedResultSet other);

    protected int compareAmountValue(ComparedResultSet other) {
        if (amount != other.amount)
            return NOT_EQUAL_AMOUNT;
        else
            return EQUALS;
    }

    protected int compareFetchKeyDoc(ComparedResultSet other) throws Exception {
        if (!externalID.equals(other.externalID))
            return compareString1251(externalID, other.externalID);
        if (externalID.isEmpty())
            return compareString1251(docNumber, other.docNumber);
        return 0;
    }

    protected int checkUniqueKey() {
        return EQUALS;
    }

    private String getQueryFileName(ConnectionSide comparedSide) {
        switch (comparedSide) {
            case LEFT:
                return mergeParams.getPairType().getGeneralSQL();
            case RIGHT:
                return mergeParams.getPairType().getMasterSQL();
            default:
                return null;
        }
    }

    protected String[] getQueryParams() {
        return new String[] { getStrDate112(mergeParams.getCalcDate()) };
    }

    private String getSQLQueryText(final ConnectionSide comparedSide) {


        String templateQuery = null; 
        try {
            InputStream inputStream = getResource(SQL_DIRECTORY + "/" + getQueryFileName(comparedSide) /* , SQL_DIRECTORY*/ );
            templateQuery = getResourceAsString(inputStream, WIN_CHARSET);
        } catch (IOException e) {
            saveTrace.saveException(e);
        }

        if (templateQuery == null || templateQuery.isEmpty()) {
            saveTrace.saveMessage("Query not found = " + comparedSide);
            return null;
        }
        
        String resultQuery = String.format(templateQuery, (Object[]) getQueryParams());

        saveTrace.saveMessage("sideQuery = " + comparedSide + " <" + resultQuery + ">");

        return resultQuery;
    }

    private int innerFetchAndCheckKey() throws Exception {
        mainCriterion = getStringResult("MAINCRITERION");
        secondCriterion = getStringResult("SECONDCRITERION");

        getFetchedValues();

        final int result = firstFetch ? EQUALS : checkUniqueKey();

        savePreviousValues();
        return result;
    }

    int nextWithCheckKey() throws Exception {
        try {
            isFetched = resultSet.next();
            if (isFetched)
                return innerFetchAndCheckKey();
            else
                return EQUALS;
        } finally {
            firstFetch = false;
        }
    }

    protected void savePreviousValues() {
    }

    protected static String addBalLetter(final String resourceNumber) {
        if (resourceNumber.substring(0, 1).equals("9"))
            return "\u0412" + "-" + resourceNumber; // "В"
        else
            return "\u0410" + "-" + resourceNumber; // "А"
    }

    void closeResultSet() {
        try {
            if (resultSet != null)
                resultSet.close();
            runQuery.closeStatement();
        } catch (SQLException e) {
            saveTrace.saveMessageWithException("Ошибка выполнения запроса resultSet.close()", e);
        }
    }
}
