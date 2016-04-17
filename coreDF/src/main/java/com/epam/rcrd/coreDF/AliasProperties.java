package com.epam.rcrd.coreDF;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static com.epam.common.igLib.LibFilesNew.*;
import static com.epam.common.igLib.LibFormatsNew.*;

final class AliasProperties {

    private static final String SERVERS_DIRECTORY = "servers";

    private static final String EXTENSION_XML     = "xml";
    private final String        aliasName;
    private final Properties    properties;

    private String              login             = "";
    private String              password          = "";

    AliasProperties(String aliasName, InputStream inputStream) throws Exception {
        if (inputStream == null)
            throw new Exception("Не заданы параметры соединения с БД.");
        this.aliasName = aliasName;
        this.properties = new Properties();
        this.properties.loadFromXML(inputStream);
    }

    static String[] getAliasPropertiesList() throws IOException {
        String[] innerStringArray = getStringsFromResource("servers/servers.list");
        String[] outerStringArray = getOuterResourcesListByExt(SERVERS_DIRECTORY, EXTENSION_XML);
        return concatDisticnctArrayString(innerStringArray, outerStringArray);
    }

    static AliasProperties getAliasPropertiesByAliasName(String aliasName) throws Exception {
        if (aliasName == null || aliasName.isEmpty())
            throw new Exception("Не выбрана System");
        return new AliasProperties(aliasName,
                getResource(SERVERS_DIRECTORY + "/" + aliasName + "." + EXTENSION_XML /* , SERVERS_DIRECTORY */));
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
            throw new Exception("Не задан login!");
        else
            this.login = login;
    }

    void setPassword(final String password) throws Exception {
        this.password = "";
        if (password == null || password.isEmpty())
            throw new Exception("Не задан пароль!");
        else
            this.password = password;
    }

    @Override
    public String toString() {
        return "AliasProperties [aliasName=" + aliasName + ", propertiesFromFile=" + properties + ", login=" + login
                + ", password=" + password + "]";
    }
}
