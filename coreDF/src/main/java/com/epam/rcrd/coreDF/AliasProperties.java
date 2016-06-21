package com.epam.rcrd.coreDF;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.epam.common.igLib.CustomLogger;

import static com.epam.common.igLib.LibFiles.*;
import static com.epam.common.igLib.LibFormats.*;

final class AliasProperties {

    private static final Logger logger = CustomLogger.getDefaultLogger();

    private static final String SERVERS_DIRECTORY = "servers";
    private static final String RESOURCE_WITH_LIST = "servers.list";

    private static final String EXTENSION_XML     = "xml";
    private final String        aliasName;
    private final Properties    properties;

    private String              login             = "";
    private String              password          = "";

    AliasProperties(String aliasName, InputStream inputStream) throws Exception {
        if (inputStream == null)
            throw new Exception("Params DB not defined");
        this.aliasName = aliasName;
        this.properties = new Properties();
        this.properties.loadFromXML(inputStream);
    }

    static String[] getAliasPropertiesList() {
        String[] innerStringArray = null;
        try {
            innerStringArray = getStringsFromResource(RESOURCE_WITH_LIST, SERVERS_DIRECTORY);
        } catch (IOException e) {
            logger.error(RESOURCE_WITH_LIST + " not uploaded", e);
        }
        String[] outerStringArray = getOuterResourcesListByExt(SERVERS_DIRECTORY, EXTENSION_XML);
        return concatDisticnctArrayString(innerStringArray, outerStringArray);
    }

    static AliasProperties getAliasPropertiesByAliasName(String aliasName) throws Exception {
        if (aliasName == null || aliasName.isEmpty())
            throw new Exception("System not selected.");
        return new AliasProperties(aliasName,
                getResource(aliasName + "." + EXTENSION_XML , SERVERS_DIRECTORY));
    }

    String getFileName() {
        return aliasName;
    }

    String getProperty(final String param) {
        if (param == null || properties == null)
            return "";
        if ("DataBaseUser".equals(param) && !login.isEmpty())
            return login;
        if ("DataBasePassword".equals(param) && !password.isEmpty())
            return password;

        String result = properties.getProperty(param);
        if (result == null)
            return "";
        else
            return result;
    }

    void setLogin(final String login) throws Exception {
        this.login = null;
        if (login == null || login.isEmpty())
            throw new Exception("Login is empty");
        else
            this.login = login;
    }

    void setPassword(final String password) throws Exception {
        this.password = "";
        if (password == null || password.isEmpty())
            throw new Exception("Password is empty");
        else
            this.password = password;
    }

    @Override
    public String toString() {
        return "AliasProperties [aliasName=" + aliasName + ", propertiesFromFile=" + properties + ", login=" + login
                + ", password=" + password + "]";
    }
}
