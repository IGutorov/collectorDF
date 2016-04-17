package com.epam.rcrd.coreDF;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static com.epam.common.igLib.LibFilesNew.*;
import static com.epam.common.igLib.LibFormats.*;
import com.epam.rcrd.coreDF.ProductVersionList.UploadProductVersion;

class ProductVersion {

    private static boolean                     isInitialize;

    private static Map<String, ProductVersion> productList = new HashMap<String, ProductVersion>();

    private final String                       systemName;
    private final String                       shortName;
    private final String                       identificationName;

    private ProductVersion(String identificationName, String systemName, String shortName) {
        this.systemName = systemName;
        this.shortName = shortName;
        this.identificationName = identificationName;
        productList.put(identificationName, this);
    }

    @Override
    public String toString() {
        return "NewProductVersion [systemName=" + systemName + ", shortName=" + shortName + ", identificationName="
                + identificationName + "]";
    }

    private static final String PRODUCTION_RESOURCENAME = "Productions.xml";

    static void init() throws Exception {
        if (isInitialize)
            return;
        productList = new HashMap<String, ProductVersion>();
        ProductVersionList prods = (ProductVersionList) convertXMLToObject(getInnerResource(PRODUCTION_RESOURCENAME),
                null, ProductVersionList.class);
        for (UploadProductVersion curr : prods.getList())
            new ProductVersion(curr.getIdentificationName(), curr.getSystemName(), curr.getShortName());
        isInitialize = true;
    }

    String getSystemName() {
        return systemName;
    }

    String getShortName() {
        return shortName;
    }

    String getID() {
        return identificationName;
    }

    static ProductVersion getBySystemName(String systemName) {
        for (Entry<String, ProductVersion> curr : productList.entrySet())
            if (curr.getValue().systemName.equals(systemName))
                return curr.getValue();
        return null;
    }

    static ProductVersion getByName(String key) {
        return productList.get(key);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((identificationName == null) ? 0 : identificationName.hashCode());
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
        ProductVersion other = (ProductVersion) obj;
        if (identificationName == null) {
            if (other.identificationName != null)
                return false;
        } else if (!identificationName.equals(other.identificationName))
            return false;
        return true;
    }

    static String getList() {
        StringBuilder res = new StringBuilder();
        for (Entry<String, ProductVersion> curr : productList.entrySet())
            res.append("key <").append(curr.getKey()).append("> value <").append(curr.getValue()).append(">")
                    .append(LINE_SEPARATOR);
        if (res.length() == 0)
            res.append("list is empty");
        return res.toString();
    }
}
