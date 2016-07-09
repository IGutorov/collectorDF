package com.epam.rcrd.coreDF.uploadXML;

import java.util.Arrays;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ListOfVersion")
public class ProductVersionList {

    public static class UploadProductVersion {
        private String systemName;
        private String identificationName;
        private String shortName;

        @XmlElement(name = "SystemName")
        public void setSystemName(String in) {
            systemName = in;
        }

        @XmlElement(name = "IdentificationName")
        public void setIdentificationName(String in) {
            identificationName = in;
        }

        @XmlElement(name = "ShortName")
        public void setShortName(String in) {
            shortName = in;
        }

        public UploadProductVersion() {
        }

        public String getSystemName() {
            return systemName;
        }

        public String getIdentificationName() {
            return identificationName;
        }

        public String getShortName() {
            return shortName;
        }

        @Override
        public String toString() {
            return "UploadProductVersion [systemName=" + systemName + ", identificationName=" + identificationName
                    + ", shortName=" + shortName + "]";
        }
    }

    private UploadProductVersion[] list;

    @XmlElement(name = "ProductVersion")
    public void setColumns(UploadProductVersion[] in) {
        list = in;
    }

    public ProductVersionList() {
    }

    public UploadProductVersion[] getList() throws Exception {
        if (list == null)
            throw new Exception("ProductVersionList not initialized.");
        return list;
    }

    @Override
    public String toString() {
        return "ProductVersionList [list=" + Arrays.toString(list) + "]";
    }
}
