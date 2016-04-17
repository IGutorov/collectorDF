package com.epam.rcrd.coreDF;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ReconciliationConnectionImpl {

    private static final String PRODUCT_DIASOFT = "5NT(e)";

    private static final String SQL_SYBASE_ASE = "Adaptive Server Enterprise";
    private static final String SQL_ORACLE = "Oracle Database";
    private static final String SERVER_PROFILE = "Sanchez Profile";

    private static class ReconciliationConnectionASE extends ReconciliationConnection {
        private ReconciliationConnectionASE(AliasProperties aliasProperties, String appName) {
            super(aliasProperties, appName);
        }

        @Override
        public String getKeyValue(ResultSet resultSet, String key) throws Exception {
            return new String(resultSet.getBytes(key), "Cp1251");
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
            return "use " + aliasProp.getProperty("DataBaseName") + " set nocount on set forceplan on";
        }

        @Override
        protected String getCheckQuery() {
            return "select 'Test' as TEST";
        }

        @Override
        protected void checkCurrentDBInner(String currentDB) throws Exception {
            if (currentDB == null || !currentDB.equalsIgnoreCase(aliasProp.getProperty("DataBaseName")))
                throw new Exception("CURRENTDB <" + currentDB + "> not equal param DataBaseName <"
                        + aliasProp.getProperty("DataBaseName") + ">");
        }

        @Override
        protected String getQueryCurrentDateTime() {
            return "select getdate() as CURRENTDATETIME";
        }

        @Override
        protected void getSQLProperties() throws SQLException {
            super.getSQLProperties();
            getSQLProperties(
                    "select db_name() as CURRENTDB, @@servername as CURRENTSERVER, suser_name() as CURRENTUSER,"
                            + " left(@@version, 34) as CURRENTSERVERVERSION, convert(varchar, @@Spid) as CURRENTSPID, host_name() as CLIENTHOSTNAME",
                    true);

            getSQLProperties(
                    "select rtrim(Product) + ' ' + case when left(Version, 1) <> '3' then '3.' + rtrim(Version) "
                            + "else rtrim(Version) + '.'  + convert(varchar, Build) end as CURRENTPRODUCTVERSION from tVersion");
            getSQLProperties("select convert(varchar, DateClosed, 104) as lastClosedDate"
                    + " from tBranchBalance where BalanceID = 2140 and InstitutionID = 2000 and AccountingType = 1");
        }

        @Override
        String getConnectionOptions() throws SQLException {
            if (aliasProp.getProperty("DataBaseProduct").equals(PRODUCT_DIASOFT))
                SQLProperties.put("connectionOptions", getASEOptions());
            return super.getConnectionOptions(); 
        }

        private String getASEOptions() throws SQLException {
            StringBuilder result = new StringBuilder();
            ResultSet resultSet = null;
            try {
                resultSet = getResultSet(
                        "select right(convert(varchar, IntToHex(convert(tinyint, substring(@@options, PropVal + 1, 1)))), 2)"
                                + " as cOptions from tProperty where PropType = 7 and PropVal < 9");
                while (resultSet.next())
                    result.append(resultSet.getString("cOptions"));
            } finally {
                if (resultSet != null)
                    resultSet.close();
                closeStatement();
            }

            return result.toString();
        }

    }

    private static class ReconciliationConnectionOracle extends ReconciliationConnection {
        private ReconciliationConnectionOracle(AliasProperties aliasProperties, String appName) {
            super(aliasProperties, appName);
        }

        @Override
        public String getKeyValue(ResultSet resultSet, String key) throws Exception {
            return resultSet.getString(key);
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
            return "select sysdate as CURRENTDATETIME from dual"; //  ???? check
        }

        @Override
        protected void getSQLProperties() throws SQLException {
            super.getSQLProperties();
            getSQLProperties("select global_name as CURRENTDB from global_name", true);
            getSQLProperties(
                    "select SERVICE_NAME as CURRENTSERVICENAME, s.Machine as CLIENTHOSTNAME from v$session s, v$mystat m where m.SID = s.SID and rownum = 1",
                    true);
            getSQLProperties("select host_name as CURRENTSERVER from v$instance");
            getSQLProperties("select user as CURRENTUSER from dual");
            getSQLProperties("select BANNER as CURRENTSERVERVERSION from v$version where rownum = 1");
            getSQLProperties("select SID as CURRENTSPID from v$mystat where rownum = 1");
            getSQLProperties("select 'UNKNOWN' as CURRENTPRODUCTVERSION from dual");
            getSQLProperties("select 'PE R69' as CURRENTPRODUCTVERSION from all_tables a"
                    + " where a.TABLE_NAME = 'NF_PAYMENT' and a.OWNER = 'NF_PAYMENT'");
            getSQLProperties("select 'PGMR jerboa 1.0' as CURRENTPRODUCTVERSION from all_tables a"
                    + " where a.TABLE_NAME = 'ZT_DIAS_PROFILE' and a.OWNER = 'PGMR'"); // PGMR: Profile & GECIF Messages Registry (jerboa)
        }

        @Override
        protected String getDelimiterWithDB() throws Exception {
            String dataBaseName = aliasProp.getProperty("DataBaseName");
            if (dataBaseName.isEmpty())
                throw new Exception("Не задан параметр БД в файле <" + aliasProp.getFileName());

            if (aliasProp.getProperty("HostSQLServer").substring(0, 1).equals("/"))
                return "/" + dataBaseName;
            else
                return ":" + dataBaseName;
        }
    }

    private static class ReconciliationConnectionSanchez extends ReconciliationConnection {
        private ReconciliationConnectionSanchez(AliasProperties aliasProperties, String appName) {
            super(aliasProperties, appName);
        }

        @Override
        public String getKeyValue(ResultSet resultSet, String key) throws Exception {
            return null;
        }

        @Override
        protected String getJDBCClassName() {
            // GT_M, // Greystone Technology M (MUMPS)
            return "sanchez.jdbc.driver.ScDriver";
        }

        @Override
        protected String getMainURL() {
            return "jdbc:sanchez/database=";
            // "protocol=jdbc:sanchez/database=fptst.rccf.ru:3105:SCA$IBS/locale=RU:RU/fileencoding=Cp1251/timeOut=2/transType=MTM/rowPrefetch=30/signOnType=1"
            // jdbc:sanchez/database=localhost:161:SCA$IBS
        }

        @Override
        protected String getCheckQuery() {
            return "write " + '\"' + "Check" + '\"' + ",!";
        }
        // 
    }

    static ReconciliationConnection getReconciliationConnection(AliasProperties aliasProperties, String appName)
            throws Exception {

        if (SQL_SYBASE_ASE.equals(aliasProperties.getProperty("SQLServer")))
            return new ReconciliationConnectionASE(aliasProperties, appName);
        else if (SQL_ORACLE.equals(aliasProperties.getProperty("SQLServer")))
            return new ReconciliationConnectionOracle(aliasProperties, appName);
        else if (SERVER_PROFILE.equals(aliasProperties.getProperty("SQLServer")))
            return new ReconciliationConnectionSanchez(aliasProperties, appName);
        else
            throw new Exception("Неверно задан параметр(SQLServer) сервера БД в файле <" + aliasProperties.getFileName()
                    + ">. Значение = " + aliasProperties.getProperty("SQLServer"));

    }
}
