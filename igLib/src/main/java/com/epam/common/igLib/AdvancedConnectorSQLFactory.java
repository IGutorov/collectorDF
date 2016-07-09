package com.epam.common.igLib;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.epam.common.igLib.IConnectorSQL.SQLServerType;

public class AdvancedConnectorSQLFactory extends ConnectorSQLFactory {

    private static final String PRODUCT_DIASOFT = "5NT(e)";

    protected AdvancedConnectorSQLFactory() {
        super();
    }

    private static final AdvancedConnectorSQLFactory instance = new AdvancedConnectorSQLFactory();

    public static AdvancedConnectorSQLFactory getInstance() {
        return instance;
    }

    @Override
    public IConnectorSQL getConnectorByAlias(IConnectorPropertyByAlias aliasProperties) throws Exception {
        return getConnectorByAlias(aliasProperties, null, false);
    }

    public IConnectorSQL getConnectorByAlias(IConnectorPropertyByAlias aliasProperties, String appName, final boolean checkParams)
            throws Exception {
        IConnectorSQL result = super.getConnectorByAlias(aliasProperties, appName, checkParams);
        advancedTune(result);

        if (checkParams) {
            String currentVersion = result.getConnectorProperty("CURRENTPRODUCTVERSION");
            if (currentVersion == null || currentVersion.isEmpty())
                throw new Exception("Не определена версия продукта");
            if (!currentVersion.equals(result.getProductVersion()))
                throw new Exception("Не совпадает версия продукта. Подключение к <" + currentVersion
                        + ">, в конфигурационном файле <" + result.getProductVersion() + ">.");
        }

        return result;
    }

    private boolean isDiasoft(String productVersion) {
        if (productVersion == null)
            return false;
        return PRODUCT_DIASOFT.equalsIgnoreCase(productVersion.substring(0, PRODUCT_DIASOFT.length()));
    }

    private String getASEOptions(IConnectorSQL connectorSQL) throws SQLException {
        StringBuilder result = new StringBuilder();
        ResultSet resultSet = null;
        try {
            resultSet = connectorSQL.getResultSet(
                    "select right(convert(varchar, IntToHex(convert(tinyint, substring(@@options, PropVal + 1, 1)))), 2)"
                            + " as cOptions from tProperty where PropType = 7 and PropVal < 9");
            while (resultSet.next())
                result.append(resultSet.getString("cOptions"));
        } finally {
            if (resultSet != null)
                resultSet.close();
            connectorSQL.closeStatement();
        }

        return result.toString();
    }

    private void advancedTune(IConnectorSQL connectorSQL) throws Exception {

        connectorSQL.unloadPropertyFromSQL("select 'PE R69' as CURRENTPRODUCTVERSION from all_tables a"
                + " where a.TABLE_NAME = 'NF_PAYMENT' and a.OWNER = 'NF_PAYMENT'", SQLServerType.ORACLE);
        connectorSQL.unloadPropertyFromSQL("select 'PGMR jerboa 1.0' as CURRENTPRODUCTVERSION from all_tables a"
                + " where a.TABLE_NAME = 'ZT_DIAS_PROFILE' and a.OWNER = 'PGMR'", SQLServerType.ORACLE);

        if (isDiasoft(connectorSQL.getProductVersion())) {

            connectorSQL.unloadPropertyFromSQL("select rtrim(Product) + ' ' +"
                    + " case when left(Version, 1) <> '3' then '3.' + rtrim(Version)"
                    + " else rtrim(Version) + '.'  + convert(varchar, Build) end as CURRENTPRODUCTVERSION"
                    + " from tVersion", SQLServerType.SYBASE_ASE);

            connectorSQL.unloadPropertyFromSQL("select convert(varchar, DateClosed, 104) as lastClosedDate"
                    + " from tBranchBalance where BalanceID = 2140 and InstitutionID = 2000 and AccountingType = 1",
                    SQLServerType.SYBASE_ASE);

            connectorSQL.setExtensionProperty("connectionOptions", getASEOptions(connectorSQL));
        }
    }
}
