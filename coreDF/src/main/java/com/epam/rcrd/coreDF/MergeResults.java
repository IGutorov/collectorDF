package com.epam.rcrd.coreDF;

import static com.epam.rcrd.coreDF.PackageConsts.*;

import com.epam.common.igLib.Money100;
import com.epam.rcrd.coreDF.CompareSystem.IOnePairRecType;

abstract class MergeResults {

    private final TableModelMerger      dataModel;
    protected final String              masterProductName;
    protected final String              generalProductName;
    private final DivergenceDetailArray divDetailArray;
    private final SummaryResult         summaryResult;
    private final String                mainHeader;
    protected final ComparedResultSet   leftCompared;
    protected final ComparedResultSet   rightCompared;

    MergeResults(IOnePairRecType pairType, TableModelMerger dataModel, ComparedResultSet leftCompared,
            ComparedResultSet rightCompared, String mainHeader) {
        this.dataModel = dataModel;
        this.masterProductName = pairType.getMasterProduct().getShortName();
        this.generalProductName = pairType.getGeneralProduct().getShortName();
        this.leftCompared = leftCompared;
        this.rightCompared = rightCompared;
        this.mainHeader = mainHeader;
        divDetailArray = new DivergenceDetailArray(pairType.getLegendDataTypes());
        summaryResult = new SummaryResult();
    }

    String getResultHTML() {
        String result = String.format(dataModel.getCaptionLineHTML(), mainHeader);
        result += dataModel.getLegendHTML();
        result += dataModel.getDataHTML();
        result += dataModel.getTailLineHTML();
        return result;
    }

    IGetResultSet getDivDetailArray() {
        return divDetailArray;
    }

    IGetResultSet getSummaryResult() {
        return summaryResult;
    }

    void addSummaryResult(ComparedResultSet curr, int Count) {
        summaryResult.addResult(curr.mainCriterion, curr.secondCriterion, curr.comparedSide, Count, curr.amount);
    }

    void addSummaryOneError(String mainCriterion, String secondCriterion) {
        summaryResult.addResultErrCount(mainCriterion, secondCriterion);
    }

    void saveDiff(DifferenceType diffType) {

        ComparedResultSet existsCompared = (diffType.onlyMaster()) ? rightCompared : leftCompared;

        IParamPutGet divParam = divDetailArray.getNewDivergenceDetail();

        divParam.put("diffType", getStrDiffType(diffType));
        divParam.put("turnCharType", existsCompared.secondCriterion);
        divParam.put("batchBrief", existsCompared.getTurnParam("BATCHBRIEF"));
        divParam.put("externalID", existsCompared.externalID);
        divParam.put("docNumber", existsCompared.docNumber);
        divParam.put("turnAmount", new Money100(existsCompared.amount));
        divParam.put("docID", existsCompared.getDocID());
        divParam.put("resourceNumber", existsCompared.getResNumber());
        divParam.put("debResNumber", existsCompared.getResNumber(TURN_CHARTYPE_DEBET));
        divParam.put("creResNumber", existsCompared.getResNumber(TURN_CHARTYPE_CREDIT));
        divParam.put("detailMesssage", getDetailMessage(diffType, divParam));

        long leftAmount = getAmount(leftCompared, diffType.onlyMaster());
        long rightAmount = getAmount(rightCompared, diffType.onlyGeneral());
        long docAmount = (leftAmount == 0) ? rightAmount : leftAmount; // ?? multifund ?? // long docAmount = amount;

        divParam.put("docAmount", new Money100(docAmount));
        divParam.put("deltaAmountDoc", new Money100(leftAmount - rightAmount));
        divParam.put("deltaAmount", new Money100(leftAmount - rightAmount));
        divParam.put("restGL", new Money100(leftAmount));
        divParam.put("restMaster", new Money100(rightAmount));
        divParam.put("deltaRest", new Money100(leftAmount - rightAmount));

        divDetailArray.add(divParam);
    }

    private long getAmount(final ComparedResultSet resultSet, final boolean ignored) {
        return ignored ? 0 : resultSet.amount;
    }

    protected String getDetailDocParams(ComparedResultSet resultSet) {
        return "Номер документа [" + resultSet.docNumber + "], ID документа [" + resultSet.getDocID()
                + "], корреспондирующий счёт [" + resultSet.getStringResult("SECONDRESNUMBER")
                + "], пользователь(login) создавший документ [" + resultSet.getStringResult("USERBRIEF") + "], пачка ["
                + resultSet.getStringResult("BATCHBRIEF") + "], назначение платежа ["
                + resultSet.getStringResult("DOCCOMMENT") + "], финансовая операция документа ["
                + resultSet.getStringResult("FOBRIEF") + "]";
    }

    protected String getDetailMessage(DifferenceType diffType, final IParamPutGet params) {
        return null;
    }

    abstract protected String getStrDiffType(DifferenceType diffType);

    protected String getDocDetailMessage(final String objName, final String param1, final String param2) {
        return "В " + generalProductName + " " + objName + " [" + param1 + "], " + objName + " в " + masterProductName
                + " [" + param2 + "]." + " пользователь(login) создавший документ в " + generalProductName + " ["
                + leftCompared.getStringResult("USERBRIEF") + "], пачка [" + leftCompared.getStringResult("BATCHBRIEF")
                + "], назначение платежа [" + leftCompared.getStringResult("DOCCOMMENT")
                + "], финансовая операция документа [" + leftCompared.getStringResult("FOBRIEF") + "]";
    }

}
