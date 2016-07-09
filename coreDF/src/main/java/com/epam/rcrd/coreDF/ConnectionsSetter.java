package com.epam.rcrd.coreDF;

import java.util.EnumSet;

import org.apache.log4j.Logger;

import com.epam.common.igLib.CustomLogger;
import com.epam.common.igLib.LibFormats;
import com.epam.rcrd.coreDF.IMergerStarterCore.TypeReconciliation;

import static com.epam.rcrd.coreDF.AliasPropertiesList.*;

// get mediator: StartDF.getConnectionsSetter
final class ConnectionsSetter implements IConnectionsSetter {

    private static final Logger logger = CustomLogger.getDefaultLogger();
    
    private final String[]         aliasList;
    private final ConnectionsStats connectionProperties;

    ConnectionsSetter() {
        connectionProperties = new ConnectionsStats();
        aliasList = getAliasPropertiesList();
    }

    @Override
    public String[] getAliasList() {
        return aliasList;
    }

    private String getAliasProperty(final String aliasName, final String param) throws Exception {
        return getAliasPropertiesByAliasName(aliasName).getProperty(param);
    }

    @Override
    public String getAliasLogin(String aliasName) throws Exception {
        return getAliasProperty(aliasName, "DataBaseUser");
    }

    @Override
    public boolean isCryptedPass(String aliasName) throws Exception {
        return !getAliasProperty(aliasName, "DataBaseEncryptPassword").isEmpty();

    }

    private boolean checkSetConnInner(NumberConnection numberConnection, String aliasName, String login,
            String password, boolean isScheduler) throws Exception {
        String cryptPass = getAliasProperty(aliasName, "DataBaseEncryptPassword");
        if (!cryptPass.isEmpty())
            if (isScheduler)
                password = decryptSchedulerPassword(cryptPass);
            else
                password = decryptPassword(cryptPass);
        return connectionProperties.checkSetConnection(numberConnection, login, password, aliasName);
    }

    @Override
    public boolean checkSetConnection(NumberConnection numberConnection, String aliasName, String login, String password)
            throws Exception {
        boolean result = checkSetConnInner(numberConnection, aliasName, login, password, false);
        if (result)
            logger.info(connectionProperties.getConnectionOptions(numberConnection));
        return result;
    }

    /*
        @Override
        public boolean checkSetConnection(NumberConnection numberConnection, String aliasName) throws Exception {
            return checkSetConnInner(numberConnection, aliasName, getAliasLogin(aliasName), "", true);        
        } */

    @Override
    public boolean checkBothConnections() throws Exception {
        return connectionProperties.checkBothConnections();
    }

    /*
        @Override
        public void setConnections(String aliasNameFirst, String aliasNameSecond) throws Exception {
            checkSetConnection(NumberConnection.FIRST_CONNECTION, aliasNameFirst);
            checkSetConnection(NumberConnection.SECOND_CONNECTION, aliasNameSecond);
        }
    */

    /*
    @Override
    public String getConnectionOptions(NumberConnection numberConnection) {
        return connectionProperties.getConnectionOptions(numberConnection);
    } */

    @Override
    public IMergerStarterExtension getNewMerger(TypeReconciliation type) throws Exception {
        if (checkBothConnections()) {
            return new MergerMediator(connectionProperties.getIOnePairRecTypeByType(type), connectionProperties);
        }
        return null;
    }

    @Override
    public EnumSet<TypeReconciliation> getTypes() {
        return connectionProperties.getTypes();
    }

    // call static method className.methodName(param1) with String param1 and return String 
    private static String getExtSting(String className, String methodName, String param1) throws Exception {
        return Class.forName(className).getDeclaredMethod(methodName, new Class<?>[] { String.class }).invoke(null, param1).toString();
    }

    private static String decryptPassword(String encryptPassword, String methodName) {
        String result = null;
        try {
            result = getExtSting(ConnectionsSetter.class.getCanonicalName() + "Inner", methodName, encryptPassword);
        } catch (Exception exception) {
            logger.info("Decrypt pass not success", exception);
        }
        return (result != null) ? result : LibFormats.EMPTY_STRING;
    }
    
    private static String decryptPassword(String encryptPassword) {
        return decryptPassword(encryptPassword, "decryptPassword");
    }

    private static String decryptSchedulerPassword(String encryptPassword) {
        return decryptPassword(encryptPassword, "decryptSchedulerPassword");
    }
}
