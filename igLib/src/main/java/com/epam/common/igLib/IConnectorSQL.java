package com.epam.common.igLib;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface IConnectorSQL {

    public enum SQLServerType {
        SYBASE_ASE {
            @Override
            public String getIdentificationString() {
                return "Adaptive Server Enterprise";
            }
        },
        ORACLE {
            @Override
            public String getIdentificationString() {
                return "Oracle Database";
            }
        },
        PROFILE { // Profile FIS, GT_M/Greystone Technology M (MUMPS)
            @Override
            public String getIdentificationString() {
                return "Sanchez Profile";
            }
        };
        
        public abstract String getIdentificationString();        

        public static SQLServerType getByString(String param) throws Exception {
            for (SQLServerType curr : SQLServerType.values())
                if (curr.getIdentificationString().equalsIgnoreCase(param))
                    return curr;
            throw new Exception("ServerType <" + param + "> not defined.");
        }
    }

    ResultSet getResultSet(String query) throws SQLException;

    void closeStatement() throws SQLException;

    void closeConnection() throws SQLException;

    String getAlternativeTypeValue(ResultSet resultSet, String key) throws Exception;

    void execQuery(String query) throws SQLException;

    void unloadPropertyFromSQL(String query, SQLServerType serverType) throws SQLException;

    // universal properties
    String getConnectorProperty(String param);

    // specific properties
    String getCurrentDB();

    String getCurrentServer();

    String getConnectionOptions();

    String getCurrentSPID(); // pre

    String getProductVersion();

    // when there are no open SQL statement
    long getUTCServerTime() throws SQLException;

    void setExtensionProperty(String propertyName, String value);    

    // extended
    // String getInterfaceProductName();

}
