package com.epam.rcrd.coreDF;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static com.epam.rcrd.coreDF.PackageConsts.*;
import static com.epam.common.igLib.LibFilesNew.*;

final class SummaryResult implements IGetResultSet {

    private static class PairCriteria implements Comparable<PairCriteria> {
        private final String mainCriterion;
        private final String secondCriterion;

        private PairCriteria(String mainCriterion, String secondCriterion) {
            this.mainCriterion = (mainCriterion == null) ? "" : mainCriterion;
            this.secondCriterion = (secondCriterion == null) ? "" : secondCriterion;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((mainCriterion == null) ? 0 : mainCriterion.hashCode());
            result = prime * result + ((secondCriterion == null) ? 0 : secondCriterion.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            PairCriteria other = (PairCriteria) obj;
            if (mainCriterion == null) {
                if (other.mainCriterion != null)
                    return false;
            } else if (!mainCriterion.equals(other.mainCriterion))
                return false;
            if (secondCriterion == null) {
                if (other.secondCriterion != null)
                    return false;
            } else if (!secondCriterion.equals(other.secondCriterion))
                return false;
            return true;
        }

        @Override
        public int compareTo(PairCriteria other) {
            int mainCompare = (mainCriterion.compareTo(other.mainCriterion));
            return (mainCompare == 0 ? (secondCriterion.compareTo(other.secondCriterion)) : mainCompare);
        }

        @Override
        public String toString() {
            return "PairCriteria [mainCriterion=" + mainCriterion + ", secondCriterion=" + secondCriterion + "]";
        }
    }

    // linked ?? default sort ??
    // private final Map<PairCriteria, LineSummaryResult> mainTab = new LinkedHashMap<PairCriteria, LineSummaryResult>(16, 0.75f, true);
    private final Map<PairCriteria, LineSummaryResult> mainTab = new HashMap<PairCriteria, LineSummaryResult>();
    
    private static final IParamPutGet[] EMPTY_SUMMARYLIST = new IParamPutGet[] {};

    private IParamPutGet[] resultArray = EMPTY_SUMMARYLIST;

    void addResult(final String mainCriterion, final String secondCriterion, final ConnectionSide comparedSide,
            final int count, final long amount) {
        getElemet(mainCriterion, secondCriterion).addValue(comparedSide, count, amount);
    }

    void addResultErrCount(final String mainCriterion, final String secondCriterion) {
        getElemet(mainCriterion, secondCriterion).notEqualsCountAdd();
    }

    private LineSummaryResult getElemet(final String mainCriterion, String secondCriterion) {
        resultArray = EMPTY_SUMMARYLIST;
        PairCriteria key = new PairCriteria(mainCriterion, secondCriterion);
        LineSummaryResult result = mainTab.get(key);
        if (result == null) {
            result = new LineSummaryResult(mainCriterion, secondCriterion);
            mainTab.put(key, result);
        }
        return result;
    }

    private IParamPutGet[] buildResultArray() {
        if (EMPTY_SUMMARYLIST.equals(resultArray) && !mainTab.isEmpty()) {
            int rowCount = mainTab.size();
            resultArray = new IParamPutGet[rowCount];
            int i = 0;
            for (LineSummaryResult curr: mainTab.values()) {
                resultArray[i] = curr;
                if (i++ >= rowCount)
                    break;
            }
        }
        return resultArray;
    }

    @Override
    public IParamPutGet[] getResultArray() {
        return buildResultArray();
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (Entry<PairCriteria, LineSummaryResult> curr : mainTab.entrySet()) {
            result.append("PairCriteria = ").append(curr.getKey().toString()).append(LINE_SEPARATOR);
            result.append("LineSummaryResult = ").append(curr.getValue().toString()).append(LINE_SEPARATOR);
        }
        return result.toString();
    }
}
