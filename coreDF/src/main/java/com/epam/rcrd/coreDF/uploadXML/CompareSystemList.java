package com.epam.rcrd.coreDF.uploadXML;

import java.util.Arrays;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.epam.rcrd.coreDF.IMergerStarterCore.TypeReconciliation;

@XmlRootElement(name = "CompareSystem")
public class CompareSystemList {

    public static class OnePairSystem {
        private String             generalSystem;
        private String             masterSystem;
        private String             typeRec;
        private TypeReconciliation type;
        private String             generalSQL;
        private String             masterSQL;
        private String             legendName;
        private String             summaryLegendName;
        private String             classicLegendName;
        private String             hiddenParams;

        @XmlElement(name = "GeneralSystem")
        void setGeneralSystem(String generalSystem) {
            this.generalSystem = generalSystem;
        }

        @XmlElement(name = "MasterSystem")
        void setMasterSystem(String masterSystem) {
            this.masterSystem = masterSystem;
        }

        @XmlElement(name = "TypeReconciliation")
        void setTypeRec(String typeRec) {
            this.typeRec = typeRec;
            type = TypeReconciliation.valueOf(typeRec);
        }

        @XmlElement(name = "GeneralSQL")
        void setGeneralSQL(String generalSQL) {
            this.generalSQL = generalSQL;
        }

        @XmlElement(name = "MasterSQL")
        void setMasterSQL(String masterSQL) {
            this.masterSQL = masterSQL;
        }

        @XmlElement(name = "LegendName")
        void setLegendName(String legendName) {
            this.legendName = legendName;
        }

        @XmlElement(name = "SummaryLegendName")
        void setSummaryLegendName(String summaryLegendName) {
            this.summaryLegendName = summaryLegendName;
        }

        @XmlElement(name = "ClassicLegendName")
        void setClassicLegendName(String classicLegendName) {
            this.classicLegendName = classicLegendName;
        }

        @XmlElement(name = "HiddenParams")
        void setHiddenParams(String hiddenParams) {
            this.hiddenParams = hiddenParams;
        }

        public OnePairSystem() {
        }

        public String getClassicLegendName() {
            return classicLegendName;
        }

        public String getMasterSystem() {
            return masterSystem;
        }

        public String getGeneralSQL() {
            return generalSQL;
        }

        public String getSummaryLegendName() {
            return summaryLegendName;
        }

        public String getLegendName() {
            return legendName;
        }

        public String getMasterSQL() {
            return masterSQL;
        }

        String getTypeRec() {
            return typeRec;
        }

        public TypeReconciliation getType() {
            return type;
        }

        public String getGeneralSystem() {
            return generalSystem;
        }

        public String getHiddenParams() {
            return hiddenParams;
        }

        @Override
        public String toString() {
            return "OnePairSystem [generalSystem=" + generalSystem + ", masterSystem=" + masterSystem + ", typeRec="
                    + typeRec + ", type=" + type + ", generalSQL=" + generalSQL + ", masterSQL=" + masterSQL
                    + ", legendName=" + legendName + ", summaryLegendName=" + summaryLegendName + ", classicLegendName="
                    + classicLegendName + ", hiddenParams=" + hiddenParams + "]";
        }
    }

    private OnePairSystem[] list;

    @XmlElement(name = "OnePair")
    public void setColumns(OnePairSystem[] in) {
        list = in;
    }

    public CompareSystemList() {
    }

    
    @Override
    public String toString() {
        return "CompareSystemList [list=" + Arrays.toString(list) + "]";
    }

    public OnePairSystem[] getList() throws Exception {
        if (list == null)
            throw new Exception("ProductVersionList not initialized.");
        return list;
    }
}
