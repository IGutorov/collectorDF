package com.epam.rcrd.coreDF;

import java.io.InputStream;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import static com.epam.common.igLib.LibFiles.*;
import static com.epam.common.igLib.LibFormats.*;

import com.epam.common.igLib.CustomLogger;
import com.epam.rcrd.coreDF.CompareSystemList.OnePairSystem;
import com.epam.rcrd.coreDF.IMergerStarter.TypeReconciliation;

class CompareSystem {

    private static final Logger logger = CustomLogger.getDefaultLogger();

    interface IOnePairRecType {
        String getGeneralSQL();
        String getMasterSQL();
        String getLegendName();
        String getSummaryLegendName();
        String getClassicLegendName();
        String getHiddenParams();
        TypeReconciliation getType();
        ProductVersion getGeneralProduct();
        ProductVersion getMasterProduct();
    }
    
    static class ProductPair {

        private static Set<ProductPair> allPairs = new HashSet<ProductPair>();

        private final ProductVersion generalProduct;
        private final ProductVersion masterProduct;

        private ProductPair(ProductVersion generalProduct, ProductVersion masterProduct) {
            this.generalProduct = generalProduct;
            this.masterProduct = masterProduct;
        }

        private static ProductPair getPairInner(ProductVersion generalProduct, ProductVersion masterProduct,
                boolean checkInverse) throws Exception {
            if (generalProduct == null || masterProduct == null)
                throw new Exception("Empty product 4 pair");
            ProductPair find = null;
            for (ProductPair curr : allPairs) {
                if (curr.generalProduct.equals(generalProduct) && curr.masterProduct.equals(masterProduct))
                    find = curr;
                else if (checkInverse && curr.generalProduct.equals(masterProduct) && curr.masterProduct.equals(generalProduct))
                    throw new Exception("Wrong pair");
            }
            return find;
        }

        private static ProductPair addPair(ProductVersion generalProduct, ProductVersion masterProduct)
                throws Exception {
            ProductPair find = getPairInner(generalProduct, masterProduct, true);
            if (find == null) {
                find = new ProductPair(generalProduct, masterProduct);
                allPairs.add(find);
            }
            return find;
        }

        @Override
        public String toString() {
            return "ProductPair [generalProduct=" + generalProduct + ", masterProduct=" + masterProduct + "]";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((generalProduct == null) ? 0 : generalProduct.hashCode());
            result = prime * result + ((masterProduct == null) ? 0 : masterProduct.hashCode());
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
            ProductPair other = (ProductPair) obj;
            if (generalProduct == null) {
                if (other.generalProduct != null)
                    return false;
            } else if (!generalProduct.equals(other.generalProduct))
                return false;
            if (masterProduct == null) {
                if (other.masterProduct != null)
                    return false;
            } else if (!masterProduct.equals(other.masterProduct))
                return false;
            return true;
        }
    }

    private static class OnePairStats {
        private final String generalSQL;
        private final String masterSQL;
        private final String legendName;
        private final String summaryLegendName;
        private final String classicLegendName;
        private final String hiddenParams;

        private OnePairStats(String generalSQL, String masterSQL, String legendName, String summaryLegendName,
                String classicLegendName, String hiddenParams) {
            this.generalSQL = generalSQL;
            this.masterSQL = masterSQL;
            this.legendName = legendName;
            this.summaryLegendName = summaryLegendName;
            this.classicLegendName = classicLegendName;
            this.hiddenParams = hiddenParams;
        }

        @Override
        public String toString() {
            return "OnePairStats [generalSQL=" + generalSQL + ", masterSQL=" + masterSQL + ", legendName=" + legendName
                    + ", summaryLegendName=" + summaryLegendName + ", classicLegendName=" + classicLegendName
                    + ", hiddenParams=" + hiddenParams + "]";
        }
    }

    private static class OnePairRecType implements IOnePairRecType {

        private static Map<OnePairRecType, OnePairStats> hMap = new HashMap<OnePairRecType, OnePairStats>();

        private final ProductPair        prodPair;
        private final TypeReconciliation type;
        private final OnePairStats       pairStats;
        private final int                hash;

        private OnePairRecType(OnePairSystem added) throws Exception {
            ProductVersion generalProduct = ProductVersion.getBySystemName(added.getGeneralSystem());
            ProductVersion masterProduct = ProductVersion.getBySystemName(added.getMasterSystem());
            prodPair = ProductPair.addPair(generalProduct, masterProduct);
            type = added.getType();
            if (type == null)
                throw new Exception("recType not defined");
            deleteDublicate();
            hash = 6 + generalProduct.getSystemName().hashCode() + masterProduct.getSystemName().hashCode()
                    + type.hashCode();
            pairStats = new OnePairStats(added.getGeneralSQL(), added.getMasterSQL(), added.getLegendName(),
                    added.getSummaryLegendName(), added.getClassicLegendName(), added.getHiddenParams());
            hMap.put(this, pairStats);
        }
        
        private void deleteDublicate(){
            for(OnePairRecType curr: hMap.keySet())
                if (curr.equals(this))
                    hMap.remove(curr);
        }

        @Override
        public String getGeneralSQL() {
            return pairStats.generalSQL;
        }

        @Override
        public String getMasterSQL() {
            return pairStats.masterSQL;
        }

        @Override
        public String getLegendName() {
            return pairStats.legendName;
        }

        @Override
        public String getSummaryLegendName() {
            return pairStats.summaryLegendName;
        }

        @Override
        public String getClassicLegendName() {
            return pairStats.classicLegendName;
        }

        @Override
        public String getHiddenParams() {
            return pairStats.hiddenParams;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            OnePairRecType other = (OnePairRecType) obj;
            if (prodPair == null) {
                if (other.prodPair != null)
                    return false;
            } else if (!prodPair.equals(other.prodPair))
                return false;
            if (type != other.type)
                return false;
            return true;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        private static EnumSet<TypeReconciliation> getTypesByPair(ProductPair pair) {
            if (pair == null)
                return null;
            EnumSet<TypeReconciliation> result = EnumSet.noneOf(TypeReconciliation.class);
            for (OnePairRecType curr : hMap.keySet())
                if (pair.equals(curr.prodPair))
                    result.add(curr.type);
            return result;
        }
        
        private static IOnePairRecType getIOnePairRecType(ProductPair pair, TypeReconciliation type) {
            if (pair == null || type == null)
                return null;
          IOnePairRecType result = null;
          for (OnePairRecType curr : hMap.keySet())
              if (pair.equals(curr.prodPair) && type == curr.type)
                  result = curr;
          return result;
        }

        @Override
        public TypeReconciliation getType() {
            return type;
        }

        @Override
        public ProductVersion getGeneralProduct() {
            return prodPair.generalProduct;
        }

        @Override
        public ProductVersion getMasterProduct() {
            return prodPair.masterProduct;
        }

        @Override
        public String toString() {
            return "OnePairRecType [prodPair=" + prodPair + ", type=" + type + ", pairStats=" + pairStats + "]";
        }
    }

    private CompareSystem() {
    }

    private static final String PAIRS_RESOURCENAME = "PairsProduction.xml";
    
    static EnumSet<TypeReconciliation> getTypesByPair(ProductPair pair) {
        return OnePairRecType.getTypesByPair(pair);
    }

    static IOnePairRecType getIOnePairRecType(ProductPair pair, TypeReconciliation type) {
        return OnePairRecType.getIOnePairRecType(pair, type);
    }
    
    static ProductPair getPair(ProductVersion firstProduct, ProductVersion secondProduct) throws Exception {
        return ProductPair.getPairInner(firstProduct, secondProduct, false);
    }

    private static boolean isInitialize = false;

    private static void addPairsSystem(InputStream XML) throws Exception {
        CompareSystemList prods = (CompareSystemList) convertXMLToObject(XML, null, CompareSystemList.class);
        for (OnePairSystem curr : prods.getList())
            new OnePairRecType(curr);
    }

    static void reolad() throws Exception {
        isInitialize = false;
        init();
    }

    static void init() {
        if (isInitialize)
            return;

        try {
            addPairsSystem(getInnerResource(PAIRS_RESOURCENAME));
        } catch (Exception e) {
            logger.error("inner CompareSystemList not upload", e);
        }

        try {
            addPairsSystem(getOuterResource(PAIRS_RESOURCENAME));
        } catch (Exception e) {            
            logger.debug("outer CompareSystemList not upload", e);
        }
        
        isInitialize = true;
    }
}
