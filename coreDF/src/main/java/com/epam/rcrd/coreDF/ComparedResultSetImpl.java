package com.epam.rcrd.coreDF;

import static com.epam.common.igLib.LibFormats.*;
import static com.epam.rcrd.coreDF.PackageConsts.*;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.epam.rcrd.coreDF.Merger.IRunQuery;
import com.epam.rcrd.coreDF.PackageConsts.ConnectionSide;

class ComparedResultSetImpl {

    private static class ComparedResultSetRest extends ComparedResultSet {

        private ComparedResultSetRest(IRunQuery runQuery, ConnectionSide comparedSide, MergeParams mergeParams,
                Logger logger) {
            super(runQuery, comparedSide, mergeParams, logger);
        }

        @Override
        protected String getResNumber() {
            return addBalLetter(getStringResult("RESOURCENUMBER20"));
        }

        @Override
        protected String[] getQueryParams() {
            return new String[] { getStrDate112(mergeParams.getCalcDate()), String.valueOf(mergeParams.isBalanceA()) };
        }

        @Override
        protected void getFetchedValues() throws Exception {
            // key
            resourceNumber19 = getKeyValue("COMPARERESOURCENUMBER19");
            // values
            amount = getLongAmountResult("AMOUNT");
        }

        @Override
        protected int compareFetchKey(ComparedResultSet other) throws Exception {
            if (resourceNumber19.equals(other.resourceNumber19))
                return 0;
            else
                return compareString1251(resourceNumber19, other.resourceNumber19);
        }

        @Override
        protected int compareFetchedValue(ComparedResultSet other) {
            return compareAmountValue(other);
        }
    }

    private static class ComparedResultSetTurn extends ComparedResultSet {
        private ComparedResultSetTurn(IRunQuery runQuery, ConnectionSide comparedSide, MergeParams mergeParams,
                Logger logger) {
            super(runQuery, comparedSide, mergeParams, logger);
        }

        @Override
        protected String getResNumber() {
            return addBalLetter(getStringResult("RESOURCENUMBER20"));
        }

        @Override
        protected String getResNumber(byte turnCharType) {
            if (this.turnCharType == turnCharType)
                return getResNumber();
            else
                return addBalLetter(getStringResult("SECONDRESNUMBER"));
        }

        @Override
        protected String getDocID() {
            return String.valueOf(getDocIDInner());
        }

        @Override
        protected String getTurnParam(final String param) {
            return getStringResult(param);
        }

        @Override
        protected void getFetchedValues() throws Exception {
            // key
            turnCharType = resultSet.getByte("TURNCHARTYPE");
            resourceNumber19 = getKeyValue("COMPARERESOURCENUMBER19");
            externalID = getKeyValue("EXTERNALID");
            docNumber = getKeyValue("DOCNUMBER");
            // values
            amount = getLongAmountResult("AMOUNT");
        }

        @Override
        protected int compareFetchKey(ComparedResultSet other) throws Exception {
            if (turnCharType != other.turnCharType)
                return (int) (turnCharType) - (int) (other.turnCharType);

            if (!resourceNumber19.equals(other.resourceNumber19))
                return compareString1251(resourceNumber19, other.resourceNumber19);

            return compareFetchKeyDoc(other);
        }

        @Override
        protected int compareFetchedValue(ComparedResultSet other) {
            int result = compareAmountValue(other);;
            if (result == EQUALS) {
                // проверку на равенство номеров документов пока ?? отключаем
                // (из-за частых случаев перенумерации в 3.5.9)
                // if (!docNumber.equals(other.docNumber))
                //     return NOT_EQUAL_DOCNUMBER;
            }
            return result;
        }

        @Override
        protected int checkUniqueKey() {
            if ((prevResourceNumber19.equals(resourceNumber19)) && (prevExternalID.equals(externalID))
                    && (prevTurnCharType == turnCharType)) {
                if (!externalID.isEmpty() || prevDocNumber.equals(docNumber)) {
                    if (comparedSide == ConnectionSide.LEFT)
                        return DUBLICATE_DOC_GL;
                    if (comparedSide == ConnectionSide.RIGHT)
                        return DUBLICATE_DOC_MASTER;
                }
            }
            return EQUALS;
        }

        @Override
        protected void savePreviousValues() {
            prevResourceNumber19 = resourceNumber19;
            prevTurnCharType = turnCharType;
            prevExternalID = externalID;
            prevDocNumber = docNumber;
        }
    }

    private static class ComparedResultSetDoc extends ComparedResultSet {
        private ComparedResultSetDoc(IRunQuery runQuery, ConnectionSide comparedSide, MergeParams mergeParams,
                Logger logger) {
            super(runQuery, comparedSide, mergeParams, logger);
        }

        @Override
        protected String getResNumber() {
            return "(" + accountDeb + ")-(" + accountCre + ")";
        }

        @Override
        protected String getDocID() {
            return String.valueOf(getDocIDInner());
        }

        @Override
        protected Object[] getQueryParams() {
            if (mergeParams.checkHiddenParam("typeDoc"))
                return super.getQueryParams();
            else
                return new Object[] { getStrDate112(mergeParams.getCalcDate()), mergeParams.getTransportTypeDoc() };
        }

        @Override
        protected void getFetchedValues() throws Exception {
            // key
            externalID = getKeyValue("EXTERNALID");
            docNumber = getKeyValue("DOCNUMBER");

            // required values
            amountDeb = getLongAmountResult("AMOUNTDEB");
            amountCre = getLongAmountResult("AMOUNTCRE");
            accountDeb = getStringResult("ACCOUNTDEB");
            accountCre = getStringResult("ACCOUNTCRE");
            inDateTime = resultSet.getDate("INDATETIME");

            amount = accountDeb.isEmpty() ? amountCre : amountDeb; // ?? ј если multifund и в "другом"(comparedSide) resultSet наоборот ??
        }

        @Override
        protected int compareFetchKey(ComparedResultSet other) throws Exception {
            return compareFetchKeyDoc(other);
        }

        @Override
        protected int compareFetchedValue(ComparedResultSet other) {
            if (!accountDeb.isEmpty() && !other.accountDeb.isEmpty()) {
                if (!accountDeb.equals(other.accountDeb))
                    return NOT_EQUAL_ACCOUNT_DEB;
                if (amountDeb != other.amountDeb)
                    return NOT_EQUAL_AMOUNT_DEB;
            }

            if (!accountCre.isEmpty() && !other.accountCre.isEmpty()) {
                if (!accountCre.equals(other.accountCre))
                    return NOT_EQUAL_ACCOUNT_CRE;
                if (amountCre != other.amountCre)
                    return NOT_EQUAL_AMOUNT_CRE;
            }

            return EQUALS;
        }

        @Override
        protected int checkUniqueKey() {
            // check unique key (PaymentID/SAK/Number)
            if (prevExternalID.equals(externalID))
                if (!externalID.isEmpty() || prevDocNumber.equals(docNumber)) {
                    if (comparedSide == ConnectionSide.LEFT)
                        return DUBLICATE_DOC_GL;
                    if (comparedSide == ConnectionSide.RIGHT)
                        return DUBLICATE_DOC_MASTER;
                }
            return EQUALS;
        }

        @Override
        protected void savePreviousValues() {
            prevExternalID = externalID;
            prevDocNumber = docNumber;
        }

        @Override
        protected long getInDateTime() {
            try {
                return resultSet.getTimestamp("INDATETIME").getTime() / 1000;
            } catch (SQLException e) {
                logger.error("Error getInDateTime", e);                
                return 0;
            }
        }
    }

    @SuppressWarnings("unused") // not yet
    private static class ComparedResultSetAccount extends ComparedResultSet {
        private ComparedResultSetAccount(IRunQuery runQuery, ConnectionSide comparedSide, MergeParams mergeParams,
                Logger logger) {
            super(runQuery, comparedSide, mergeParams, logger);
        }

        @Override
        protected String[] getQueryParams() {
            return new String[] { getStrDate112(mergeParams.getDateStart()), getStrDate112(mergeParams.getDateEnd()),
                    mergeParams.getAccount() };
        }
        @Override
        protected void getFetchedValues() throws Exception {
        }

        @Override
        protected int compareFetchKey(ComparedResultSet other) throws Exception {
            return 0;
        }

        @Override
        protected int compareFetchedValue(ComparedResultSet other) {
            return 0;
        }
    }

    static ComparedResultSet getComparedResultSet(IRunQuery runQuery, ConnectionSide comparedSide,
            MergeParams mergeParams, Logger logger) {
        switch (mergeParams.getType()) {
            case AccountBalance:
                return new ComparedResultSetRest(runQuery, comparedSide, mergeParams, logger);
            case Turns:
                return new ComparedResultSetTurn(runQuery, comparedSide, mergeParams, logger);
            case Documents:
                return new ComparedResultSetDoc(runQuery, comparedSide, mergeParams, logger);
            // case AccountStatement: return new ComparedResultSetAccount(runQuery, comparedSide, mergeParams, logger);
            default:
                return null;

        }
    }
}
