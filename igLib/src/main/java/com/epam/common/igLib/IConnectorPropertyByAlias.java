package com.epam.common.igLib;

public interface IConnectorPropertyByAlias {

    void setLogin(String login) throws Exception;
    void setPassword(String password) throws Exception;
    
    String getAliasName();
    String getProperty(String param);

}
