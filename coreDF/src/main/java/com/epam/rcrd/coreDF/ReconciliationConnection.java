package com.epam.rcrd.coreDF;

import java.sql.Connection;
// import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Properties;
import com.epam.rcrd.coreDF.Merger.IRunQuery;

abstract class ReconciliationConnection implements IRunQuery {

    private static final String DEFAULT_APP_NAME = "DFCheckConn";

    private final String          applicationName;

    protected final AliasProperties aliasProp;
    protected String     connectionURL;

    private Connection connection;
    private Statement  currentStatement;
    private boolean    isOpen;
    protected Properties SQLProperties;

    boolean isOpen() {
        return isOpen;
    }

    // ?? 4 trace
    void execQuery(String query) throws SQLException {
        currentStatement = connection.createStatement();
        currentStatement.execute(query);
    }

    @Override
    public ResultSet getResultSet(String query) throws SQLException {
        currentStatement = connection.createStatement();
        return currentStatement.executeQuery(query);
    }

    @Override
    public void closeStatement() throws SQLException {
        if (currentStatement != null)
            currentStatement.close();
        currentStatement = null;
    }

    // ?? 
    @Override
    public String getInterfaceProductName() {
        return aliasProp.getProperty("ProductVersion") + "(" + getCurrentDB() + ")";
    }

    @Override
    public abstract String getKeyValue(ResultSet resultSet, String key) throws Exception;

    ReconciliationConnection(AliasProperties aliasProperties, String appName) {
        aliasProp = aliasProperties;
        if (appName == null || appName.isEmpty())
            applicationName = DEFAULT_APP_NAME;
        else
            applicationName = appName;
    }

    @Override
    public void closeConnection() throws SQLException {
        isOpen = false;
        if (connection != null && !connection.isClosed())
            connection.close();
    }

    protected void getSQLProperties(final String query) throws SQLException {
        getSQLProperties(query, false);
    }

    protected void getSQLProperties(final String query, final boolean critical) throws SQLException {
        String columnName = null;
        ResultSet resultSet = null;

        try {
            resultSet = getResultSet(query);
            if (resultSet.next()) {
                ResultSetMetaData metaData = resultSet.getMetaData();

                int columnCount = metaData.getColumnCount();
                for (int i = 0; i < columnCount; i++) {
                    columnName = metaData.getColumnName(i + 1);
                    SQLProperties.put(columnName, resultSet.getString(columnName));
                }
            }
        } catch (SQLException exception) {
            if (critical)
                throw new SQLException("Не удалось определить параметр [" + columnName + "] соединения с сервером "
                        + aliasProp.getFileName(), exception);
        } finally {
            if (resultSet != null)
                resultSet.close();
            closeStatement();
        }
    }

    protected void getSQLProperties() throws SQLException {
        SQLProperties = new Properties();
        SQLProperties.put("JRE.version", System.getProperty("java.runtime.version"));
    }

    protected String getDelimiterWithDB() throws Exception {
        return "";
    }
    
    abstract protected String getMainURL();

    private String getConnectURL() throws Exception {
        StringBuilder result = new StringBuilder();
        result.append(getMainURL()).append(aliasProp.getProperty("HostSQLServer"));
        result.append(":").append(aliasProp.getProperty("SQLServerPort")).append(getDelimiterWithDB());
        return result.toString();
    }

    protected abstract String getCheckQuery();

    private void execCheckQuery(final String connectionDetail) throws Exception {
        ResultSet resultSet = null;
        try {
            resultSet = getResultSet(getCheckQuery());
            if (!resultSet.next())
                throw new Exception("Нет соединения с сервером  БД. Файл <" + aliasProp.getFileName() + ">"
                        + " connectionURL = " + connectionURL + " connectionProp = " + connectionDetail);
        } catch (SQLException e) {
            throw new Exception("Нет соединенния с сервером БД. Файл <" + aliasProp.getFileName() + ">"
                    + " connectionURL = " + connectionURL + " connectionProp = " + connectionDetail, e);
        } finally {
            if (resultSet != null)
                resultSet.close();
            closeStatement();
        }
    }

    abstract protected String getJDBCClassName();

    boolean openConnection() throws Exception {
        isOpen = false;
        try {
            Class.forName(getJDBCClassName());
        } catch (ClassNotFoundException exception) {
            throw new ClassNotFoundException("Не удалось загрузить jdbc драйвер " + getJDBCClassName(), exception);
        }

        getConnection();
        getSQLProperties();
        checkCurrentDB();
        isOpen = checkProductVersion();

        if (!isOpen)
            closeConnection();
        return isOpen;
    }

    protected void throwDetailMessage(SQLException exception, String user, String details) throws Exception {
        throw new Exception("Не удалось соединиться с сервером БД. Файл <" + aliasProp.getFileName() + ">"
                + " connectionURL = " + connectionURL + " connectionProp = " + details + " JDBC Class = " + getJDBCClassName(), exception);
    }
    
    private void getConnection() throws Exception {
        connectionURL = getConnectURL();

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

        Statement tuneStatement = null;
        if (!getTuneQuery().isEmpty())
            try {
                tuneStatement = connection.createStatement(); 
                tuneStatement.execute(getTuneQuery());
            } catch (SQLException e) {
                throw new SQLException("Не удалось подключиться к БД:" + aliasProp.getProperty("DataBaseName")
                        + " на сервере = " + aliasProp.getProperty("HostSQLServer"), e);
            } finally {
                tuneStatement.close();
            }
        
    }

    protected String getTuneQuery() {
        return "";
    }

    private boolean checkProductVersion() throws Exception {
        String currentProductVersion = SQLProperties.getProperty("CURRENTPRODUCTVERSION");

        if (currentProductVersion == null || currentProductVersion.isEmpty()) {
            throw new Exception("Не определена версия продукта на базе данных "
                    + SQLProperties.getProperty("CURRENTSERVER") + "/" + SQLProperties.getProperty("CURRENTDB"));
        } else if (!currentProductVersion.equals(getProductVersion())) {
            throw new Exception(
                    "Не совпадает версия продукта. На базе данных " + SQLProperties.getProperty("CURRENTSERVER") + "/"
                            + SQLProperties.getProperty("CURRENTDB") + " установлена <"
                            + SQLProperties.getProperty("CURRENTPRODUCTVERSION") + ">  в конфигурационном файле "
                            + aliasProp.getFileName() + " указана версия <" + getProductVersion() + ">.");
        }
        return true;
    }

    protected void checkCurrentDBInner(String currentDB) throws Exception {
    }

    private void checkCurrentDB() throws Exception {
        String currentDB = SQLProperties.getProperty("CURRENTDB");
        if (currentDB == null || currentDB.isEmpty())
            throw new Exception("CURRENTDB not defined");
        checkCurrentDBInner(currentDB);
    }

    protected String getQueryCurrentDateTime() {
        return "";
    }

    long getUTCServerTime() throws SQLException {
        long result = 0;
        ResultSet resultSet = null;
        String query = getQueryCurrentDateTime(); 
        if (!query.isEmpty())
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

    Date getGLServerTime() throws SQLException {
        Date resultDate = new Date();
        ResultSet resultSet = null;
        String query = getQueryCurrentDateTime();
        if (!query.isEmpty())
        try {
            resultSet = getResultSet(query);
            if (resultSet.next())
                resultDate = resultSet.getTime("CURRENTDATETIME");
        } finally {
            if (resultSet != null)
                resultSet.close();
            closeStatement();
        }
        return resultDate;
    }

    String getCurrentDB() {
        return SQLProperties.getProperty("CURRENTDB");
    }

    String getCurrentServer() {
        return SQLProperties.getProperty("CURRENTSERVER");
    }

    String getCurrentSPID() {
        return SQLProperties.getProperty("CURRENTSPID");
    }

    String getConnectionOptions() throws SQLException {

        return SQLProperties.toString() + "; Server host <" + aliasProp.getProperty("HostSQLServer") + ">";
    }

    String getProductVersion() {
        return aliasProp.getProperty("DataBaseProduct") + " " + aliasProp.getProperty("ProductVersion");
    }

}
