package com.epam.common.igLib;

import java.sql.ResultSet;
import java.sql.SQLException;

import static com.epam.common.igLib.LibFiles.*;
import static com.epam.common.igLib.IConnectorSQL.*;

public class ConnectorSQLFactory {

    protected ConnectorSQLFactory() {
    }

    private static final ConnectorSQLFactory instance = new ConnectorSQLFactory();

    public static ConnectorSQLFactory getInstance() {
        return instance;
    }

    private static final String DEFAULT_APP_NAME = "igLibDefault";

    private IConnectorSQL getConnectorByAliasInner(IConnectorPropertyByAlias aliasProperties, String appName,
            final boolean checkParams) throws Exception {

        String serverSQL = aliasProperties.getProperty("SQLServer");
        switch (SQLServerType.getByString(serverSQL)) {
            case SYBASE_ASE:
                return new ConnectorASE(aliasProperties, appName, checkParams);
            case ORACLE:
                return new ConnectorOracle(aliasProperties, appName, checkParams);
            case PROFILE:
                return new ConnectorSanchez(aliasProperties, appName, checkParams);
            default:
                throw new Exception("ServerType <" + serverSQL + "> not supported.");
        }
    }

    public IConnectorSQL getConnectorByAlias(IConnectorPropertyByAlias aliasProperties) throws Exception {
        return getConnectorByAlias(aliasProperties, null, false);
    }

    public IConnectorSQL getConnectorByAlias(IConnectorPropertyByAlias aliasProperties, String appName,
            final boolean checkParams) throws Exception {
        if (appName == null || appName.isEmpty())
            appName = DEFAULT_APP_NAME;

        return getConnectorByAliasInner(aliasProperties, appName, checkParams);
    }

    private static class ConnectorASE extends ConnectorSQL {

        ConnectorASE(IConnectorPropertyByAlias aliasProperties, String appName, final boolean checkParams)
                throws Exception {
            super(aliasProperties, appName, checkParams);
        }

        @Override
        protected String getJDBCClassName() {
            return "com.sybase.jdbc3.jdbc.SybDriver";
        }

        @Override
        protected String getMainURL() {
            return "jdbc:sybase:Tds:";
        }

        @Override
        protected void throwDetailMessage(SQLException exception, String user, String details) throws Exception {
            if (exception.getSQLState().equals("JZ00L"))
                throw new Exception("Login/pasword faild. Login <" + user + ">");
            else
                super.throwDetailMessage(exception, user, details);
        }

        @Override
        protected String getTuneQuery() {
            String nameDB = aliasProp.getProperty("DataBaseName");
            if (nameDB == null || nameDB.isEmpty())
                return "";
            if (LibFormats.hasDelimiter(nameDB))
                return "";
            return "use " + nameDB + " set nocount on set forceplan on";
        }

        @Override
        protected String getCheckQuery() {
            return "select 'Test' as TEST";
        }

        @Override
        public String getAlternativeTypeValue(ResultSet resultSet, String key) throws Exception {
            return new String(resultSet.getBytes(key), WIN_CHARSET);
        }

        @Override
        protected String checkCurrentDB() throws Exception {
            String currentDB = super.checkCurrentDB();
            if (currentDB == null || !currentDB.equalsIgnoreCase(aliasProp.getProperty("DataBaseName")))
                throw new Exception("CURRENTDB <" + currentDB + "> not equal param DataBaseName <"
                        + aliasProp.getProperty("DataBaseName") + ">");
            return currentDB;
        }

        @Override
        protected String getQueryCurrentDateTime() {
            return "select getdate() as CURRENTDATETIME";
        }

        @Override
        protected void initEnvironmentProperties() throws SQLException {
            super.initEnvironmentProperties();
            unloadPropertyFromSQL("select db_name() as CURRENTDB, @@servername as CURRENTSERVER,"
                    + " suser_name() as CURRENTUSER, left(@@version, 34) as CURRENTSERVERVERSION,"
                    + " convert(varchar, @@Spid) as CURRENTSPID, host_name() as CLIENTHOSTNAME",
                    ParameterNecessity.MANDATORY);
        }

        @Override
        protected SQLServerType getSQLServerType() {
            return SQLServerType.SYBASE_ASE;
        }

    }

    private static class ConnectorOracle extends ConnectorSQL {

        ConnectorOracle(IConnectorPropertyByAlias aliasProperties, String appName, final boolean checkParams)
                throws Exception {
            super(aliasProperties, appName, checkParams);
        }

        @Override
        protected String getJDBCClassName() {
            return "oracle.jdbc.driver.OracleDriver";
        }

        @Override
        protected String getMainURL() {
            return "jdbc:oracle:thin:@";
        }

        @Override
        protected void throwDetailMessage(SQLException exception, String user, String details) throws Exception {
            if (exception.getErrorCode() == 1017)
                throw new Exception("Login/pasword faild. Login <" + user + ">");
            else
                super.throwDetailMessage(exception, user, details);
        }

        @Override
        protected String getTuneQuery() {
            return "ALTER SESSION SET NLS_SORT=BINARY";
        }

        @Override
        protected String getCheckQuery() {
            return "select 'Test' as TEST from dual";
        }

        @Override
        protected String getQueryCurrentDateTime() {
            return "select sysdate as CURRENTDATETIME from dual";
        }

        @Override
        protected void initEnvironmentProperties() throws SQLException {
            super.initEnvironmentProperties();
            unloadPropertyFromSQL("select global_name as CURRENTDB from global_name", ParameterNecessity.MANDATORY);
            unloadPropertyFromSQL(
                    "select SERVICE_NAME as CURRENTSERVICENAME, s.Machine as CLIENTHOSTNAME from v$session s, v$mystat m where m.SID = s.SID and rownum = 1",
                    ParameterNecessity.MANDATORY);
            unloadPropertyFromSQL("select host_name as CURRENTSERVER from v$instance", ParameterNecessity.OPTIONAL);
            unloadPropertyFromSQL("select user as CURRENTUSER from dual", ParameterNecessity.OPTIONAL);
            unloadPropertyFromSQL("select BANNER as CURRENTSERVERVERSION from v$version where rownum = 1",
                    ParameterNecessity.OPTIONAL);
            unloadPropertyFromSQL("select SID as CURRENTSPID from v$mystat where rownum = 1",
                    ParameterNecessity.OPTIONAL);
            unloadPropertyFromSQL("select 'UNKNOWN' as CURRENTPRODUCTVERSION from dual", ParameterNecessity.OPTIONAL);
        }

        @Override
        protected SQLServerType getSQLServerType() {
            return SQLServerType.ORACLE;
        }

        @Override
        protected String getDelimiterWithDB() throws Exception {
            String dataBaseName = aliasProp.getProperty("DataBaseName");
            if (dataBaseName.isEmpty())
                throw new Exception("Не задан параметр БД в файле <" + aliasProp.getAliasName());

            if (aliasProp.getProperty("HostSQLServer").substring(0, 1).equals("/"))
                return "/" + dataBaseName;
            else
                return ":" + dataBaseName;
        }

    }

    private static class ConnectorSanchez extends ConnectorSQL {

        ConnectorSanchez(IConnectorPropertyByAlias aliasProperties, String appName, final boolean checkParams)
                throws Exception {
            super(aliasProperties, appName, checkParams);
            throw new Exception("Not supported");
        }

        @Override
        protected String getJDBCClassName() {
            return "sanchez.jdbc.driver.ScDriver";
        }

        @Override
        protected String getMainURL() {
            // "protocol=jdbc:sanchez/database=fpstd.rccf.ru:8000:SCA$IBS/locale=RU:RU/fileencoding=Cp1251/timeOut=2/transType=MTM/rowPrefetch=30/signOnType=1"
            // jdbc:sanchez/database=localhost:161:SDA$IRS
            return "jdbc:sanchez/database=";
        }

        @Override
        protected SQLServerType getSQLServerType() {
            return SQLServerType.PROFILE;
        }

        @Override
        protected String getCheckQuery() {
            return "write " + '\"' + "Check" + '\"' + ",!";
        }

    }
}
