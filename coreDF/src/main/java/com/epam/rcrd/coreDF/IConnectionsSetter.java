package com.epam.rcrd.coreDF;

import java.util.EnumSet;

import com.epam.rcrd.coreDF.IMergerStarter.TypeReconciliation;

public interface IConnectionsSetter extends IConnectionsCore {
    String[] getAliasList();

    String getAliasLogin(String aliasName) throws Exception;

    boolean isCryptedPass(String aliasName) throws Exception;

    boolean checkSetConnection(NumberConnection numberConnection, String aliasName, String login, String password)
            throws Exception;

    //    boolean checkSetConnection(NumberConnection numberConnection, String aliasName) throws Exception;

    String getConnectionOptions(NumberConnection numberConnection);

    void setCurrentMergePage(Object identObj);

    EnumSet<TypeReconciliation> getTypes();
}
