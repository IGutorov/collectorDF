package com.epam.rcrd.coreDF;

import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.apache.log4j.Logger;

import static com.epam.rcrd.coreDF.PackageConsts.*;
import static com.epam.common.igLib.LibDateFormats.*;
import static com.epam.common.igLib.LibFormats.*;
import static com.epam.common.igLib.LibFiles.*;

import com.epam.common.igLib.IConnectorSQL;
import com.epam.common.igLib.TimeProfiler;
import com.epam.rcrd.coreDF.PackageConsts.ConnectionSide;

abstract class ComparedResultSet extends Thread {

    private final IConnectorSQL    runQuery;
    protected final ConnectionSide comparedSide;
    protected final MergeParams    mergeParams;
    protected final Logger         logger;
    private final String           query;

    private static final String    SQL_DIRECTORY        = "sql";

    protected volatile ResultSet   resultSet;
    private volatile boolean       successGetResultSet;         // ??

    // DataSet
    private boolean                firstFetch           = true;
    boolean                        isFetched            = false;

    // DataSet. Key attribute
    protected String               resourceNumber19     = null;
    protected byte                 turnCharType         = 0;
    String                         externalID           = null;

    // (package) DataSet ??. Check attribute
    long                           amount;
    String                         docNumber;
    long                           amountDeb;
    long                           amountCre;
    String                         accountDeb;
    String                         accountCre;
    //    protected boolean hasDebAcc;
    //    protected boolean hasCreAcc;
    Date                           inDateTime           = null;

    String                         mainCriterion;
    String                         secondCriterion;

    protected String               prevResourceNumber19 = null;
    protected byte                 prevTurnCharType     = 0;
    protected String               prevExternalID       = null;
    protected String               prevDocNumber        = null;

    ComparedResultSet(IConnectorSQL runQuery, ConnectionSide comparedSide, MergeParams mergeParams, Logger logger) {
        this.runQuery = runQuery;
        this.comparedSide = comparedSide;
        this.mergeParams = mergeParams;
        this.logger = logger;

        this.query = getSQLQueryText(comparedSide);

        if (query == null || query.isEmpty()) {
            logger.error("Query not found = " + comparedSide);
        } else
            logger.info("sideQuery = " + comparedSide + " <" + query + ">");
    }

    private boolean queryExecute() {
        try {
            resultSet = runQuery.getResultSet(query);
        } catch (SQLException e) {
            logger.error("Error execute query.", e);
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
                runQuery.getConnectorProperty("ProductVersion") + "(" + runQuery.getCurrentDB() + ")");
        logger.info(finishMessage + ((timeProfiler.getTimeInterval()) / 100) / 10.0 + " сек.");
    }

    protected long getInDateTime() {
        return 0;
    }

    protected long getDocIDInner() {
        try {
            return resultSet.getLong("DOCID");
        } catch (SQLException e) {
            logger.error("Error resultSet getDocID", e);
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
            logger.error("Error resultSet getString", e);
            return null;
        }
    }

    protected Long getLongAmountResult(String columnName) throws SQLException {
        return resultSet.getLong(columnName);
    }

    protected String getKeyValue(String key) throws Exception {
        String result = runQuery.getAlternativeTypeValue(resultSet, key); // depends on DBMS
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

    abstract protected DifferenceType compareFetchedValue(ComparedResultSet other);

    protected DifferenceType compareAmountValue(ComparedResultSet other) {
        if (amount != other.amount)
            return DifferenceType.NOT_EQUAL_AMOUNT;
        else
            return DifferenceType.EQUALS;
    }

    protected int compareFetchKeyDoc(ComparedResultSet other) throws Exception {
        if (!externalID.equals(other.externalID))
            return compareString1251(externalID, other.externalID);
        if (externalID.isEmpty())
            return compareString1251(docNumber, other.docNumber);
        return 0;
    }

    protected DifferenceType checkUniqueKey() {
        return DifferenceType.EQUALS;
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

    protected Object[] getQueryParams() {
        return new Object[] { getStrDate112(mergeParams.getCalcDate()) };
    }

    private String getSQLQueryText(final ConnectionSide comparedSide) {

        String templateQuery = null;
        try {
            InputStream inputStream = getResource(getQueryFileName(comparedSide), SQL_DIRECTORY);
            templateQuery = getResourceAsString(inputStream, WIN_CHARSET);
        } catch (IOException e) {
            logger.error("Error get sql query", e);
        }

        if (templateQuery == null || templateQuery.isEmpty()) {
            return null;
        }

        return String.format(templateQuery, getQueryParams());
    }

    private DifferenceType innerFetchAndCheckKey() throws Exception {
        mainCriterion = getStringResult("MAINCRITERION");
        secondCriterion = getStringResult("SECONDCRITERION");

        getFetchedValues();

        final DifferenceType result = firstFetch ? DifferenceType.EQUALS : checkUniqueKey();

        savePreviousValues();
        return result;
    }

    DifferenceType nextWithCheckKey() throws Exception {
        try {
            isFetched = resultSet.next();
            if (isFetched)
                return innerFetchAndCheckKey();
            else
                return DifferenceType.EQUALS;
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
            logger.error("Error resultSet.close()", e);
        }
    }
}
