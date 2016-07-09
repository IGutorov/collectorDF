package com.epam.common.igLib;

import java.io.InputStream;
import java.util.Properties;

public class AliasProperties implements IConnectorPropertyByAlias {

    private final String        aliasName;
    private final Properties    properties;

    private String              login             = "";
    private String              password          = "";

    public AliasProperties(String aliasName, Properties properties) {
        this.aliasName = aliasName;
        this.properties = properties;
    }

    public AliasProperties(String aliasName, InputStream inputStream) throws Exception {
        this(aliasName, getPropertiesByStream(inputStream));
    }

    private static Properties getPropertiesByStream(InputStream inputStream) throws Exception {
        if (inputStream == null)
            throw new Exception("Params DB not defined");
        Properties result = new Properties();
        result.loadFromXML(inputStream);
        return result;
    }
    
    @Override
    public String getAliasName() {
        return aliasName;
    }

    @Override
    public String getProperty(String param) {
        if (param == null || properties == null)
            return "";
        if ("DataBaseUser".equals(param) && !login.isEmpty())
            return login;
        if ("DataBasePassword".equals(param) && !password.isEmpty())
            return password;

        String result = properties.getProperty(param);
        return (result == null) ? "" : result;
    }

    @Override
    public void setLogin(final String login) throws Exception {
        this.login = null;
        if (login == null || login.isEmpty())
            throw new Exception("Login is empty");
        else
            this.login = login;
    }

    @Override
    public void setPassword(final String password) throws Exception {
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
