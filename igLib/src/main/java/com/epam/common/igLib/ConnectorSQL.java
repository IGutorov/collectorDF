package com.epam.common.igLib;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.DriverManager;
import java.util.Properties;

abstract class ConnectorSQL implements IConnectorSQL {

    protected enum ParameterNecessity {
        MANDATORY, OPTIONAL;
    }

    private static final String               DEFAULT_APP_NAME      = "igLibDefault";

    private Statement                         currentStatement;
    private Connection                        connection;

    protected final IConnectorPropertyByAlias aliasProp;
    private final String                      applicationName;
    private final String                      connectionURL;

    protected final Properties                environmentProperties = new Properties();

    ConnectorSQL(IConnectorPropertyByAlias aliasProperties, String appName, final boolean checkParams) throws Exception {
        this.aliasProp = aliasProperties;
        if (appName == null || appName.isEmpty())
            appName = DEFAULT_APP_NAME;

        applicationName = appName;
        connectionURL = getConnectURL();
        openConnection();
        if (checkParams)
            checkCurrentDB();
    }

    abstract protected String getJDBCClassName();

    abstract protected String getMainURL();

    abstract protected SQLServerType getSQLServerType();

    protected String getDelimiterWithDB() throws Exception {
        return "";
    }

    protected abstract String getCheckQuery();

    private void execCheckQuery(String connectionDetail) throws Exception {
        ResultSet resultSet = null;
        try {
            resultSet = getResultSet(getCheckQuery());
            if (!resultSet.next())
                throw new Exception("Нет соединения с сервером  БД. Файл <" + aliasProp.getAliasName() + ">"
                        + " connectionURL = " + connectionURL + " connectionProp = " + connectionDetail);
        } catch (SQLException e) {
            throw new Exception("Нет соединенния с сервером БД. Файл <" + aliasProp.getAliasName() + ">"
                    + " connectionURL = " + connectionURL + " connectionProp = " + connectionDetail, e);
        } finally {
            if (resultSet != null)
                resultSet.close();
            closeStatement();
        }
    }

    private String getConnectURL() throws Exception {
        StringBuilder result = new StringBuilder();
        result.append(getMainURL()).append(aliasProp.getProperty("HostSQLServer"));
        result.append(":").append(aliasProp.getProperty("SQLServerPort")).append(getDelimiterWithDB());
        return result.toString();
    }

    protected void throwDetailMessage(SQLException exception, String user, String details) throws Exception {
        throw new Exception("Не удалось соединиться с сервером БД. Файл <" + aliasProp.getAliasName() + ">"
                + " connectionURL = " + connectionURL + " connectionProp = " + details + " JDBC Class = "
                + getJDBCClassName(), exception);
    }

    protected String getTuneQuery() {
        return "";
    }

    private void execTuneQuery() throws Exception {
        String query = getTuneQuery();
        if (query.isEmpty())
            return;

        Statement tuneStatement = null;
        try {
            tuneStatement = connection.createStatement();
            tuneStatement.execute(query);
        } catch (SQLException e) {
            throw new SQLException("Не удалось подключиться к БД:" + aliasProp.getProperty("DataBaseName")
                    + " на сервере = " + aliasProp.getProperty("HostSQLServer"), e);
        } finally {
            if (tuneStatement != null)
                tuneStatement.close();
        }
    }

    private void getConnection() throws Exception {

        Properties connectionProp = new Properties(); // свойства коннекта
        connectionProp.setProperty("applicationname", applicationName);
        connectionProp.setProperty("user", aliasProp.getProperty("DataBaseUser")); // SQL-login
        connectionProp.setProperty("password", aliasProp.getProperty("DataBasePassword"));
        try {
            connection = DriverManager.getConnection(connectionURL, connectionProp);
        } catch (SQLException exception) {
            connectionProp.setProperty("password", "hidden");
            throwDetailMessage(exception, connectionProp.getProperty("user"), connectionProp.toString());
        }
        // forget pass & check exec
        connectionProp.setProperty("password", "hidden");

        execCheckQuery(connectionProp.toString());

        execTuneQuery();
    }

    protected void initEnvironmentProperties() throws SQLException {
        environmentProperties.clear();
        environmentProperties.put("JRE.version", System.getProperty("java.runtime.version"));
    }

    protected String checkCurrentDB() throws Exception {
        String currentDB = environmentProperties.getProperty("CURRENTDB");
        if (currentDB == null || currentDB.isEmpty())
            throw new Exception("CURRENTDB not defined");
        return currentDB;
    }

    private void openConnection() throws Exception {
        try {
            Class.forName(getJDBCClassName());
        } catch (ClassNotFoundException exception) {
            throw new ClassNotFoundException("Не удалось загрузить jdbc драйвер " + getJDBCClassName(), exception);
        }

        getConnection();
        initEnvironmentProperties();
    }

    protected void unloadPropertyFromSQL(final String query, ParameterNecessity necessity) throws SQLException {
        String columnName = null;
        ResultSet resultSet = null;

        try {
            resultSet = getResultSet(query);
            if (resultSet.next()) {
                ResultSetMetaData metaData = resultSet.getMetaData();

                int columnCount = metaData.getColumnCount();
                for (int i = 0; i < columnCount; i++) {
                    columnName = metaData.getColumnName(i + 1);
                    environmentProperties.put(columnName, resultSet.getString(columnName));
                }
            }
        } catch (SQLException exception) {
            if (necessity == ParameterNecessity.MANDATORY)
                throw new SQLException("Не удалось определить параметр [" + columnName + "] соединения с сервером "
                        + aliasProp.getAliasName(), exception);
        } finally {
            if (resultSet != null)
                resultSet.close();
            closeStatement();
        }
    }

    @Override
    public ResultSet getResultSet(String query) throws SQLException {
        closeStatement();
        currentStatement = connection.createStatement();
        return currentStatement.executeQuery(query);
    }

    @Override
    public void closeStatement() throws SQLException {
        if (currentStatement != null)
            currentStatement.close();
        currentStatement = null;
    }

    @Override
    public void closeConnection() throws SQLException {
        if (connection != null)
            connection.close();
    }

    @Override
    public void unloadPropertyFromSQL(String query, SQLServerType serverType) throws SQLException {
        if (serverType == getSQLServerType())
            unloadPropertyFromSQL(query, ParameterNecessity.MANDATORY);
    }

    @Override
    public void setExtensionProperty(String propertyName, String value) {
        if (propertyName != null && value != null && !propertyName.isEmpty())
            environmentProperties.put(propertyName, value);
    }

    @Override
    public String getAlternativeTypeValue(ResultSet resultSet, String key) throws Exception {
        return resultSet.getString(key);
    }


    @Override
    public void execQuery(String query) throws SQLException {
        closeStatement();
        currentStatement = connection.createStatement();
        currentStatement.execute(query);
        closeStatement();
    }

    @Override
    public String getConnectorProperty(String param) {
        if ("DataBasePassword".equalsIgnoreCase(param))
            return "";
        String result = aliasProp.getProperty(param);
        if (result == null || result.isEmpty())
            result = environmentProperties.getProperty(param);
        return result;
    }

    @Override
    public String getCurrentDB() {
        return environmentProperties.getProperty("CURRENTDB");
    }

    @Override
    public String getCurrentServer() {
        return environmentProperties.getProperty("CURRENTSERVER");
    }

    @Override
    public String getConnectionOptions() {
        return environmentProperties.toString() + "; Server host <" + aliasProp.getProperty("HostSQLServer") + ">";
    }

    @Override
    public String getCurrentSPID() {
        return environmentProperties.getProperty("CURRENTSPID");
    }

    @Override
    public String getProductVersion() {
        return aliasProp.getProperty("DataBaseProduct") + " " + aliasProp.getProperty("ProductVersion");
    }

    protected String getQueryCurrentDateTime() {
        return "";
    }

    @Override
    public long getUTCServerTime() throws SQLException {
        closeStatement();
        String query = getQueryCurrentDateTime();
        if (query.isEmpty())
            return 0;

        long result = 0;
        ResultSet resultSet = null;
        try {
            resultSet = getResultSet(query);
            if (resultSet.next())
                result = resultSet.getTimestamp("CURRENTDATETIME").getTime() / 1000;
        } finally {
            if (resultSet != null)
                resultSet.close();
            closeStatement();
        }
        return result;
    }

}
