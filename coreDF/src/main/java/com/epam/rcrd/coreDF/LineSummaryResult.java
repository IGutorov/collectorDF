package com.epam.rcrd.coreDF;

import static com.epam.rcrd.coreDF.PackageConsts.*;
import com.epam.common.igLib.Money100;

final class LineSummaryResult implements IParamPutGet {

    private final String mainCriterion;
    private final String secondCriterion;

    private int  leftCount, rightCount, notEqualsCount;
    private long leftAmount, rightAmount;

    LineSummaryResult(String mainCriterion) {
        this(mainCriterion, "");
    }

    LineSummaryResult(final String mainCriterion, String secondCriterion) {
        this.mainCriterion = (mainCriterion == null) ? "" : mainCriterion;
        this.secondCriterion = (secondCriterion == null) ? "" : secondCriterion;
    }

    void notEqualsCountAdd() {
        notEqualsCount++;
    }

    void addValue(final ConnectionSide comparedSide, final int count, final long amount) {
        switch (comparedSide) {
            case LEFT:
                leftCount += count;
                leftAmount += amount;
            break;
            default: // RIGHT
                rightCount += count;
                rightAmount += amount;
            break;
        }
    }

    @Override
    public Object getObject(final String keyData) {
        if (keyData == null || keyData.isEmpty())
            return null;

        /* keyData definded how:
         new com.epam.common.igLib.LegendArrayOfNamedObjects(com.epam.rcrd.coreDF.PackageConsts.SUMM_FIELDS) */
        if ("mainCriterion".equals(keyData)) {
            return mainCriterion;
        }

        if ("secondCriterion".equals(keyData)) {
            return secondCriterion;
        }

        if ("leftCount".equals(keyData))
            return Integer.valueOf(leftCount);

        if ("rightCount".equals(keyData))
            return Integer.valueOf(rightCount);

        if ("notEqualsCount".equals(keyData))
            return Integer.valueOf(notEqualsCount);

        if ("leftAmount".equals(keyData))
            return new Money100(leftAmount);

        if ("rightAmount".equals(keyData))
            return new Money100(rightAmount);

        if ("deltaAmount".equals(keyData))
            return new Money100(leftAmount - rightAmount);

        return ""; // null ??
    }

    @Override
    public void put(String keyData, Object value) {
        // impl on SummaryResult
    }

    @Override
    public String toString() {
        return "LineSummaryResult [mainCriterion=" + mainCriterion + ", secondCriterion=" + secondCriterion
                + ", leftCount=" + leftCount + ", rightCount=" + rightCount + ", notEqualsCount=" + notEqualsCount
                + ", leftAmount=" + leftAmount + ", rightAmount=" + rightAmount + "]";
    }

}
