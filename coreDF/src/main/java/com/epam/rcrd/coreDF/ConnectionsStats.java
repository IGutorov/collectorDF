package com.epam.rcrd.coreDF;

import java.util.EnumSet;

import com.epam.common.igLib.AdvancedConnectorSQLFactory;
import com.epam.common.igLib.ConnectorSQLFactory;
import com.epam.common.igLib.IConnectorPropertyByAlias;
import com.epam.common.igLib.IConnectorSQL;
import com.epam.rcrd.coreDF.CompareSystem.IOnePairRecType;
import com.epam.rcrd.coreDF.CompareSystem.ProductPair;
import com.epam.rcrd.coreDF.IConnectionsCore.NumberConnection;
import com.epam.rcrd.coreDF.IMergerStarterCore.TypeReconciliation;

import static com.epam.rcrd.coreDF.AliasPropertiesList.*;

final class ConnectionsStats {

    private enum Direction {
        FORWARD, REVERSE;
    }

    private static final String       DEFAULT_CHECK_APP_NAME = "DFCheckConn";

    private AliasStats                firstStats             = new AliasStats();
    private AliasStats                secondStats            = new AliasStats();
    private IConnectorPropertyByAlias generalLAliasProperties;
    private IConnectorPropertyByAlias masterAliasProperties;
    private ProductPair               pair;
    private String                    applicationName;
    private Direction                 direction              = Direction.FORWARD;

    EnumSet<TypeReconciliation> getTypes() {
        return CompareSystem.getTypesByPair(pair);
    }

    IOnePairRecType getIOnePairRecTypeByType(TypeReconciliation type) {
        return CompareSystem.getIOnePairRecType(pair, type);
    }

    private static class AliasStats {
        private boolean                   aliasChecked;
        private IConnectorPropertyByAlias aliasProperties;
        // ?? DBProperties -> return checkRC... 
        private String                    serverName;
        private String                    nameDB;
        private String                    productVersionStrIn;
        private ProductVersion            productVersion;
        private String                    connectionOptions;

        private void clear() {
            setStats(null, null, null, null, null);
        }

        private void setStats(IConnectorPropertyByAlias aliasProperties, IConnectorSQL rConn) {
            setStats(aliasProperties, rConn.getCurrentServer(), rConn.getCurrentDB(), rConn.getProductVersion(),
                    rConn.getConnectionOptions());
        }

        private void setStats(IConnectorPropertyByAlias aliasProperties, String serverName, String nameDB,
                String productVersionStrIn, String connectionOptions) {
            this.aliasProperties = aliasProperties;
            this.serverName = serverName;
            this.nameDB = nameDB;
            this.productVersionStrIn = productVersionStrIn;
            productVersion = ProductVersion.getByName(productVersionStrIn);
            this.connectionOptions = connectionOptions;
            aliasChecked = (aliasProperties != null);
        }

        private boolean equalProduct(ProductVersion other) {
            if (other == null)
                return false;
            return other.equals(productVersion);
        }
    }

    ConnectionsStats() {
        applicationName = PackageProperties.getProperty("Application.SQLname");
        ProductVersion.init();
        CompareSystem.init();
    }

    private AliasStats getAliasStats(final NumberConnection numberConnection) {
        return ((numberConnection == NumberConnection.FIRST_CONNECTION) ? firstStats : secondStats);
    }

    String getConnectionOptions(final NumberConnection numberConnection) {
        return getAliasStats(numberConnection).connectionOptions;
    }

    boolean checkSetConnection(final NumberConnection numberConnection, final String login, final String password,
            String aliasName) throws Exception {

        IConnectorSQL checkConnection = null;

        IConnectorPropertyByAlias currentAliasProperties = getAliasPropertiesByAliasName(aliasName);

        currentAliasProperties.setLogin(login);
        currentAliasProperties.setPassword(password);
        generalLAliasProperties = null;
        masterAliasProperties = null;

        try {
            checkConnection = getConnectorByAliasFasade(currentAliasProperties, DEFAULT_CHECK_APP_NAME, true);
            AliasStats currentAliasStats = getAliasStats(numberConnection);
            currentAliasStats.clear();
            currentAliasStats.setStats(currentAliasProperties, checkConnection);
            if (firstStats.aliasChecked && secondStats.aliasChecked) {
                if (!checkBothConnections()) {
                    currentAliasStats.clear();
                    return false;
                }
            }
        } finally {
            if (checkConnection != null)
                checkConnection.closeConnection();
        }
        return true;
    }

    boolean checkBothConnections() throws Exception {
        if (!firstStats.aliasChecked)
            return false;
        if (!secondStats.aliasChecked)
            return false;

        if (firstStats.serverName.equals(secondStats.serverName) && firstStats.nameDB.equals(secondStats.nameDB))
            throw new Exception("Error. DB equals !!.");

        if (firstStats.equalProduct(secondStats.productVersion))
            throw new Exception("Systems must be different.");

        direction = Direction.FORWARD;
        pair = CompareSystem.getPair(firstStats.productVersion, secondStats.productVersion);
        if (pair == null) {
            direction = Direction.REVERSE;
            pair = CompareSystem.getPair(secondStats.productVersion, firstStats.productVersion);
            if (pair == null)
                throw new Exception("Not supported Systems: " + firstStats.productVersionStrIn + " - "
                        + secondStats.productVersionStrIn);
        }

        generalLAliasProperties = getGeneralStats().aliasProperties;
        masterAliasProperties = getMasterStats().aliasProperties;

        return true;
    }

    private ConnectorSQLFactory getConnectorSQLFactory() {
        return AdvancedConnectorSQLFactory.getInstance();
    }
    
    private IConnectorSQL getConnectorByAliasFasade(IConnectorPropertyByAlias aliasProperties, String appName,
            final boolean checkParams) throws Exception {
        return getConnectorSQLFactory().getConnectorByAlias(aliasProperties, appName, checkParams);
    }

    private IConnectorSQL getNewConnection(IConnectorPropertyByAlias aliasProperties) throws Exception {
        IConnectorSQL result = null;
        if (aliasProperties != null)
            result = getConnectorByAliasFasade(aliasProperties, applicationName, true);

        return result;
    }

    private AliasStats getGeneralStats() {
        switch (direction) {
            case REVERSE:
                return secondStats;
            default:
                return firstStats;
        }
    }

    private AliasStats getMasterStats() {
        switch (direction) {
            case REVERSE:
                return firstStats;
            default:
                return secondStats;
        }
    }

    IConnectorSQL getNewGeneralConnection() throws Exception {
        return getNewConnection(generalLAliasProperties);
    }

    IConnectorSQL getNewMasterConnection() throws Exception {
        return getNewConnection(masterAliasProperties);
    }

    String getMasterProductName() {
        return getMasterStats().productVersion.getShortName();
    }
}
