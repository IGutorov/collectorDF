package com.epam.rcrd.coreDF;

import java.util.EnumSet;

import com.epam.rcrd.coreDF.IMergerStarterCore.TypeReconciliation;

public interface IConnectionsSetter extends IConnectionsCore {

    // 1. aliases
    String[] getAliasList();
    String getAliasLogin(String aliasName) throws Exception;
    boolean isCryptedPass(String aliasName) throws Exception;

    // 2. check first and second
    boolean checkSetConnection(NumberConnection numberConnection, String aliasName, String login, String password)
            throws Exception;

    // 3. check both (IConnectionsCore)

    // 4. available reconciliation types
    EnumSet<TypeReconciliation> getTypes();

    // 5. get starter (IConnectionsCore)
}
