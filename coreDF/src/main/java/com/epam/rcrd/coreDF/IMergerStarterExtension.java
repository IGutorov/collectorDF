package com.epam.rcrd.coreDF;

import org.apache.log4j.Logger;

public interface IMergerStarterExtension extends IMergerStarterCore{

    Logger getLogger();
    
    boolean isAvailableXls();

    void showXls();

    boolean checkHiddenParam(String paramName);

    Integer[] getColumnSizes(TableDesign tableDesign);
}
