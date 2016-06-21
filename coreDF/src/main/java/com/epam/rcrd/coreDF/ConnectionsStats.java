package com.epam.rcrd.coreDF;

import java.util.EnumSet;

import com.epam.rcrd.coreDF.CompareSystem.ProductPair;
import com.epam.rcrd.coreDF.CompareSystem.IOnePairRecType;
import com.epam.rcrd.coreDF.IConnectionsCore.NumberConnection;
import com.epam.rcrd.coreDF.IMergerStarter.TypeReconciliation;
import static com.epam.rcrd.coreDF.AliasProperties.*;

final class ConnectionsStats {

    private enum Direction {
        FORWARD, REVERSE;
    }

    private AliasStats      firstStats  = new AliasStats();
    private AliasStats      secondStats = new AliasStats();
    private AliasProperties generalLAliasProperties;
    private AliasProperties masterAliasProperties;
    private ProductPair     pair;
    private String          applicationName;
    private Direction       direction   = Direction.FORWARD;

    EnumSet<TypeReconciliation> getTypes() {
        return CompareSystem.getTypesByPair(pair);
    }

    IOnePairRecType getIOnePairRecTypeByType(TypeReconciliation type) {
        return CompareSystem.getIOnePairRecType(pair, type);
    }

    private static class AliasStats {
        private boolean         aliasChecked;
        private AliasProperties aliasProperties;
        // ?? DBProperties -> return checkRC... 
        private String          serverName;
        private String          nameDB;
        private String          productVersionStrIn;
        private ProductVersion  productVersion;
        private String          connectionOptions;

        private void clear() {
            setStats(null, null, null, null, null);
        }

        private void setStats(final AliasProperties aliasProperties, final ReconciliationConnection rConn)
                throws Exception {
            setStats(aliasProperties, rConn.getCurrentServer(), rConn.getCurrentDB(), rConn.getProductVersion(),
                    rConn.getConnectionOptions());
        }

        private void setStats(AliasProperties aliasProperties, String serverName, String nameDB,
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

        // ??
        // TODO refactoring. Extract getIRunQuery Interface        

        ReconciliationConnection checkConnection = null;

        AliasProperties currentAliasProperties = getAliasPropertiesByAliasName(aliasName);
        
        currentAliasProperties.setLogin(login);
        currentAliasProperties.setPassword(password);
        generalLAliasProperties = null;
        masterAliasProperties = null;

        boolean result = false;
        try {
            // ?? getCurrentServer(), rConn.getCurrentDB(), rConn.getProductVersion(), rConn.getConnectionOptions()
            checkConnection = ReconciliationConnectionImpl.getReconciliationConnection(currentAliasProperties, null);
            result = checkConnection.openConnection();
            if (result) {
                final AliasStats currentAliasStats = getAliasStats(numberConnection);
                currentAliasStats.clear();
                currentAliasStats.setStats(currentAliasProperties, checkConnection); // ??
                if (firstStats.aliasChecked && secondStats.aliasChecked) {
                    result = checkBothConnections();
                    if (!result)
                        currentAliasStats.clear();
                }
            }
        } finally {
            checkConnection.closeConnection();
        }

        return result;
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

    private ReconciliationConnection getNewConnection(final AliasProperties aliasProperties) throws Exception {
        ReconciliationConnection result = null;
        if (aliasProperties != null) {
            result = ReconciliationConnectionImpl.getReconciliationConnection(aliasProperties, applicationName);
            result.openConnection();
        }
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

    ReconciliationConnection getNewGeneralConnection() throws Exception {
        return getNewConnection(generalLAliasProperties);
    }

    ReconciliationConnection getNewMasterConnection() throws Exception {
        return getNewConnection(masterAliasProperties);
    }

    String getMasterProductName() {
        return getMasterStats().productVersion.getShortName();
    }
}
