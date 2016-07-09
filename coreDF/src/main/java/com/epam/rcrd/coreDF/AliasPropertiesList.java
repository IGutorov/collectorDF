package com.epam.rcrd.coreDF;

import static com.epam.common.igLib.LibFiles.*;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.epam.common.igLib.AliasProperties;
import com.epam.common.igLib.CustomLogger;
import com.epam.common.igLib.IConnectorPropertyByAlias;
import com.epam.common.igLib.LibFormats;

class AliasPropertiesList {

    private static final Logger logger = CustomLogger.getDefaultLogger();
    private static final String SERVERS_DIRECTORY = "servers";
    private static final String RESOURCE_WITH_LIST = "servers.list";
    private static final String EXTENSION_XML     = "xml";

    static String[] getAliasPropertiesList() {
        String[] innerStringArray = null;
        try {
            innerStringArray = getStringsFromResource(RESOURCE_WITH_LIST, SERVERS_DIRECTORY);
        } catch (IOException e) {
            logger.error(RESOURCE_WITH_LIST + " not uploaded", e);
        }
        String[] outerStringArray = getOuterResourcesListByExt(SERVERS_DIRECTORY, EXTENSION_XML);
        return LibFormats.concatDisticnctArrayString(innerStringArray, outerStringArray);
    }

    static IConnectorPropertyByAlias getAliasPropertiesByAliasName(String aliasName) throws Exception {
        if (aliasName == null || aliasName.isEmpty())
            throw new Exception("System not selected.");
        return new AliasProperties(aliasName,
                getResource(aliasName + "." + EXTENSION_XML , SERVERS_DIRECTORY));
    }

}
