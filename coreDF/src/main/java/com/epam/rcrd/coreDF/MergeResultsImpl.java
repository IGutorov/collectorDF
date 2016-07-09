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
                    return "� " + generalProductName + " ��� �������.";
                case NOT_EXISTS_MASTER:
                    return "� " + masterProductName + " ��� �������.";
                case NOT_EQUAL_AMOUNT:
                    return "������� �� �����";
                default:
                    return "����������� ������ � ������ �������� ??";
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
                    return "[�����],[ExternalID] � [ID] ��������� � " + generalProductName + " ["
                            + params.getObject("docNumber") + "], [" + params.getObject("externalID") + "] ["
                            + params.getObject("docID") + "].";
                case DUBLICATE_DOC_MASTER:
                    return "[�����],[ExternalID] � [ID] ��������� � " + masterProductName + " ["
                            + params.getObject("docNumber") + "], [" + params.getObject("externalID") + "] ["
                            + params.getObject("docID") + "].";
                case NOT_EQUAL_AMOUNT:
                    return "[�����] � [ID] ��������� � " + generalProductName + " ["
                            + longToStrWithComma(leftCompared.amount) + "] [" + leftCompared.getDocID()
                            + "]; [�����] � [ID] ��������� � " + masterProductName + " ["
                            + longToStrWithComma(rightCompared.amount) + "] [" + rightCompared.getDocID() + "].";
                case NOT_EQUAL_DOCNUMBER:
                    return "[�����] � [ID] ��������� � " + generalProductName + " [" + leftCompared.docNumber + "] ["
                            + leftCompared.getDocID() + "]; [�����] � [ID] ��������� � " + masterProductName + " ["
                            + rightCompared.docNumber + "] [" + rightCompared.getDocID() + "].";
                default:
                    return "???";
            }
        }

        @Override
        protected String getStrDiffType(DifferenceType diffType) {
            switch (diffType) {
                case NOT_EXISTS_GL:
                    return "� " + generalProductName + " ��� �������";
                case NOT_EXISTS_MASTER:
                    return "� " + masterProductName + " ��� �������";
                case DUBLICATE_DOC_GL:
                    return "C�������� ������� ����� � ���������� � " + generalProductName + ".";
                case DUBLICATE_DOC_MASTER:
                    return "C�������� ������� ����� � ���������� � " + masterProductName;
                case NOT_EQUAL_AMOUNT:
                    return "����� � ���������� �� �����";
                case NOT_EQUAL_DOCNUMBER:
                    return "�� ��������� ����� � ����������";
                default:
                    return "����������� ������ � ������ �������� ??";
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
                    return "[�����],[ExternalID] � [ID] ��������� � " + masterProductName + " ["
                            + params.getObject("docNumber") + "], [" + params.getObject("externalID") + "] ["
                            + params.getObject("docID") + "], ���������� �������� ��������� ["
                            + rightCompared.getStringResult("FOBRIEF") + "].";
                case NOT_EXISTS_MASTER:
                case DUBLICATE_DOC_GL:
                    return "[�����],[ExternalID] � [ID] ��������� � " + generalProductName + " ["
                            + params.getObject("docNumber") + "], [" + params.getObject("externalID") + "] ["
                            + params.getObject("docID") + "], ���������� �������� ��������� ["
                            + leftCompared.getStringResult("FOBRIEF") + "].";
                case NOT_EQUAL_ACCOUNT_DEB:
                    return getDocDetailMessage("����� �����", leftCompared.accountDeb, rightCompared.accountDeb);
                case NOT_EQUAL_ACCOUNT_CRE:
                    return getDocDetailMessage("����� �����", leftCompared.accountCre, rightCompared.accountCre);
                case NOT_EQUAL_AMOUNT_DEB:
                    return getDocDetailMessage("����� ���������", new Money100(leftCompared.amountDeb).toString(),
                            new Money100(rightCompared.amountDeb).toString());
                case NOT_EQUAL_AMOUNT_CRE:
                    return getDocDetailMessage("����� �����", new Money100(leftCompared.amountCre).toString(),
                            new Money100(rightCompared.amountCre).toString());
                default:
                    return "??";
            }
        }

        @Override
        protected String getStrDiffType(DifferenceType diffType) {
            switch (diffType) {
                case NOT_EXISTS_GL:
                    return "� " + generalProductName + " ��� ���������";
                case NOT_EXISTS_MASTER:
                    return "� " + masterProductName + " ��� ���������";
                case DUBLICATE_DOC_GL:
                    return "C�������� ������� ����� � ���������� � " + generalProductName + ".";
                case DUBLICATE_DOC_MASTER:
                    return "C�������� ������� ����� � ���������� � " + masterProductName;
                case NOT_EQUAL_ACCOUNT_DEB:
                    return "����� �� ������ �� �����";
                case NOT_EQUAL_ACCOUNT_CRE:
                    return "����� �� ������� �� �����";
                case NOT_EQUAL_AMOUNT_DEB:
                    return "����� �� ������ �� �����";
                case NOT_EQUAL_AMOUNT_CRE:
                    return "����� �� ������� �� �����";
                default:
                    return "����������� ������ � ������ ���������� ??";
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
