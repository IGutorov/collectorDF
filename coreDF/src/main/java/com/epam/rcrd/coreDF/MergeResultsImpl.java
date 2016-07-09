package com.epam.rcrd.coreDF;

import static com.epam.common.igLib.LibFormats.*;
import static com.epam.rcrd.coreDF.PackageConsts.*;

import com.epam.common.igLib.Money100;
import com.epam.rcrd.coreDF.CompareSystem.IOnePairRecType;
import com.epam.rcrd.coreDF.PackageConsts.IParamPutGet;

class MergeResultsImpl {

    private static class MergeResultsRest extends MergeResults {
        private MergeResultsRest(IOnePairRecType pairType, TableModelMerger dataModel, ComparedResultSet leftCompared,
                ComparedResultSet rightCompared, String mainHeader) {
            super(pairType, dataModel, leftCompared, rightCompared, mainHeader);
        }

        @Override
        protected String getStrDiffType(DifferenceType diffType) {
            switch (diffType) {
                case NOT_EXISTS_GL:
                    return "В " + generalProductName + " нет остатка.";
                case NOT_EXISTS_MASTER:
                    return "В " + masterProductName + " нет остатка.";
                case NOT_EQUAL_AMOUNT:
                    return "Остатки не равны";
                default:
                    return "Неизвестная ошибка в сверке остатков ??";
            }
        }
    }

    private static class MergeResultsTurn extends MergeResults {
        private MergeResultsTurn(IOnePairRecType pairType, TableModelMerger dataModel, ComparedResultSet leftCompared,
                ComparedResultSet rightCompared, String mainHeader) {
            super(pairType, dataModel, leftCompared, rightCompared, mainHeader);
        }

        @Override
        protected String getDetailMessage(DifferenceType diffType, IParamPutGet params) {
            switch (diffType) {
                case NOT_EXISTS_GL:
                    return getDetailDocParams(rightCompared);
                case NOT_EXISTS_MASTER:
                    return getDetailDocParams(leftCompared);
                case DUBLICATE_DOC_GL:
                    return "[Номер],[ExternalID] и [ID] документа в " + generalProductName + " ["
                            + params.getObject("docNumber") + "], [" + params.getObject("externalID") + "] ["
                            + params.getObject("docID") + "].";
                case DUBLICATE_DOC_MASTER:
                    return "[Номер],[ExternalID] и [ID] документа в " + masterProductName + " ["
                            + params.getObject("docNumber") + "], [" + params.getObject("externalID") + "] ["
                            + params.getObject("docID") + "].";
                case NOT_EQUAL_AMOUNT:
                    return "[Сумма] и [ID] документа в " + generalProductName + " ["
                            + longToStrWithComma(leftCompared.amount) + "] [" + leftCompared.getDocID()
                            + "]; [сумма] и [ID] документа в " + masterProductName + " ["
                            + longToStrWithComma(rightCompared.amount) + "] [" + rightCompared.getDocID() + "].";
                case NOT_EQUAL_DOCNUMBER:
                    return "[Номер] и [ID] документа в " + generalProductName + " [" + leftCompared.docNumber + "] ["
                            + leftCompared.getDocID() + "]; [номер] и [ID] документа в " + masterProductName + " ["
                            + rightCompared.docNumber + "] [" + rightCompared.getDocID() + "].";
                default:
                    return "???";
            }
        }

        @Override
        protected String getStrDiffType(DifferenceType diffType) {
            switch (diffType) {
                case NOT_EXISTS_GL:
                    return "В " + generalProductName + " нет оборота";
                case NOT_EXISTS_MASTER:
                    return "В " + masterProductName + " нет оборота";
                case DUBLICATE_DOC_GL:
                    return "Cовпадает внешний номер у документов в " + generalProductName + ".";
                case DUBLICATE_DOC_MASTER:
                    return "Cовпадает внешний номер у документов в " + masterProductName;
                case NOT_EQUAL_AMOUNT:
                    return "Суммы в документах не равны";
                case NOT_EQUAL_DOCNUMBER:
                    return "Не совпадает номер у документов";
                default:
                    return "Неизвестная ошибка в сверке оборотов ??";
            }
        }
    }

    private static class MergeResultsDoc extends MergeResults {
        private MergeResultsDoc(IOnePairRecType pairType, TableModelMerger dataModel, ComparedResultSet leftCompared,
                ComparedResultSet rightCompared, String mainHeader) {
            super(pairType, dataModel, leftCompared, rightCompared, mainHeader);
        }

        @Override
        protected String getDetailMessage(DifferenceType diffType, final IParamPutGet params) {
            switch (diffType) {
                case NOT_EXISTS_GL:
                case DUBLICATE_DOC_MASTER:
                    return "[Номер],[ExternalID] и [ID] документа в " + masterProductName + " ["
                            + params.getObject("docNumber") + "], [" + params.getObject("externalID") + "] ["
                            + params.getObject("docID") + "], финансовая операция документа ["
                            + rightCompared.getStringResult("FOBRIEF") + "].";
                case NOT_EXISTS_MASTER:
                case DUBLICATE_DOC_GL:
                    return "[Номер],[ExternalID] и [ID] документа в " + generalProductName + " ["
                            + params.getObject("docNumber") + "], [" + params.getObject("externalID") + "] ["
                            + params.getObject("docID") + "], финансовая операция документа ["
                            + leftCompared.getStringResult("FOBRIEF") + "].";
                case NOT_EQUAL_ACCOUNT_DEB:
                    return getDocDetailMessage("номер счёта", leftCompared.accountDeb, rightCompared.accountDeb);
                case NOT_EQUAL_ACCOUNT_CRE:
                    return getDocDetailMessage("номер счёта", leftCompared.accountCre, rightCompared.accountCre);
                case NOT_EQUAL_AMOUNT_DEB:
                    return getDocDetailMessage("сумма документа", new Money100(leftCompared.amountDeb).toString(),
                            new Money100(rightCompared.amountDeb).toString());
                case NOT_EQUAL_AMOUNT_CRE:
                    return getDocDetailMessage("номер счёта", new Money100(leftCompared.amountCre).toString(),
                            new Money100(rightCompared.amountCre).toString());
                default:
                    return "??";
            }
        }

        @Override
        protected String getStrDiffType(DifferenceType diffType) {
            switch (diffType) {
                case NOT_EXISTS_GL:
                    return "В " + generalProductName + " нет документа";
                case NOT_EXISTS_MASTER:
                    return "В " + masterProductName + " нет документа";
                case DUBLICATE_DOC_GL:
                    return "Cовпадает внешний номер у документов в " + generalProductName + ".";
                case DUBLICATE_DOC_MASTER:
                    return "Cовпадает внешний номер у документов в " + masterProductName;
                case NOT_EQUAL_ACCOUNT_DEB:
                    return "Счета по дебету не равны";
                case NOT_EQUAL_ACCOUNT_CRE:
                    return "Счета по кредиту не равны";
                case NOT_EQUAL_AMOUNT_DEB:
                    return "Суммы по дебету не равны";
                case NOT_EQUAL_AMOUNT_CRE:
                    return "Суммы по кредиту не равны";
                default:
                    return "Неизвестная ошибка в сверке документов ??";
            }
        }
    }

    static MergeResults getMergeResults(IOnePairRecType pairType, TableModelMerger dataModel, ComparedResultSet leftCompared,
            ComparedResultSet rightCompared, String mainHeader) {
        switch (pairType.getType()) {
            case AccountBalance:
                return new MergeResultsRest(pairType, dataModel, leftCompared, rightCompared, mainHeader);
            case Turns:
                return new MergeResultsTurn(pairType, dataModel, leftCompared, rightCompared, mainHeader);
            case Documents:
                return new MergeResultsDoc(pairType, dataModel, leftCompared, rightCompared, mainHeader);
            // case AccountStatement: return null;
            default:
                return null;

        }
    }
}
