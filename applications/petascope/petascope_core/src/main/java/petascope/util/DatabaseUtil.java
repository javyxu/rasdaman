/*
  *  This file is part of rasdaman community.
  * 
  *  Rasdaman community is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  * 
  *  Rasdaman community is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  *  See the GNU  General Public License for more details.
  * 
  *  You should have received a copy of the GNU  General Public License
  *  along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
  * 
  *  Copyright 2003 - 2014 Peter Baumann / rasdaman GmbH.
  * 
  *  For more information please see <http://www.rasdaman.org>
  *  or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.rasdaman.InitAllConfigurationsApplicationService;
import static org.rasdaman.InitAllConfigurationsApplicationService.POSTGRESQL_NEUTRAL_DATABASE;
import org.rasdaman.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;

/**
 * Utility to get the database connection manually, used to check database
 * exists or create/update database name.
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class DatabaseUtil {

    private static final Logger log = LoggerFactory.getLogger(DatabaseUtil.class);
    
    private static final String LIQUIBASE_LOCK_TABLE = "databasechangeloglock";

    // To rename a database, connect to a default neutral database of Postgresql
    private static final String SOURCE_NEUTRAL_POSTGRESQL_DATASOURCE_URL = ConfigManager.SOURCE_DATASOURCE_URL.substring(0,
            ConfigManager.SOURCE_DATASOURCE_URL.lastIndexOf("/") + 1) + InitAllConfigurationsApplicationService.POSTGRESQL_NEUTRAL_DATABASE;

    /**
     * Close the connection to petascopedb from manual query.
     * @param connection
     * @throws PetascopeException 
     */
    private static void closeDatabaseConnection(Connection connection) throws PetascopeException {
        closeDatabaseConnection(connection, null);
    }
    
    /**
     * Close the connection, statement to petascopedb from the manual query.
     * @param connection
     * @param statement
     * @throws PetascopeException 
     */
    private static void closeDatabaseConnection(Connection connection, Statement statement) throws PetascopeException {
        closeDatabaseConnection(connection, statement, null);
    }
    
    /**
     * Close the connection, statement, resultset to petascopedb from the manual query.
     *
     * @param connection
     */
    private static void closeDatabaseConnection(Connection connection, Statement statement, ResultSet resultSet) throws PetascopeException {
        try {
            if (connection != null) {
                connection.close();
            }
            if (statement != null) {
                statement.close();
            }
            if (resultSet != null) {
                resultSet.close();
            }
        } catch (SQLException ex) {
            throw new PetascopeException(ExceptionCode.InternalSqlError, "Cannot close connection to petascopedb", ex);
        }
    }

    /**
     * Get a database connection to a JDBC connection.
     *
     * @param datasourceURL
     * @param datasourceUserName
     * @param datasourcePassword
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws PetascopeException
     */
    public static Connection getDatabaseConnection(String datasourceURL,
            String datasourceUserName, String datasourcePassword) throws SQLException, ClassNotFoundException, PetascopeException, PetascopeException {
        Connection connection = DriverManager.getConnection(datasourceURL, datasourceUserName, datasourcePassword);

        return connection;
    }

    /**
     * Check if application can connect to the JDBC connection. It can be old
     * database or new database.
     *
     * @param datasourceURL
     * @param datasourceUserName
     * @param datasourcePassword
     * @return
     * @throws java.lang.ClassNotFoundException
     * @throws petascope.exceptions.PetascopeException
     * @throws java.sql.SQLException
     */
    public static boolean checkJDBCConnection(String datasourceURL,
            String datasourceUserName, String datasourcePassword) throws ClassNotFoundException, PetascopeException, SQLException {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(datasourceURL, datasourceUserName, datasourcePassword);
        } finally {
            closeDatabaseConnection(connection);
        }

        return true;
    }
    
    /**
     * Liquibase adds a row in databasechangeloglock to not let other Liquibase process update petascopedb when it is running.
     * However, if the previous process couldn't finish properly, the next process cannot start.
     * So, delete the lock when the next process (e.g: restart tomcat) starts.
     */
    public static void deletelLiquibaseLock(DataSource dataSource) throws PetascopeException {
        Connection connection = null;
        Statement statement = null;
        
        try {
            connection = dataSource.getConnection();
            
            statement = connection.createStatement();
            // just delete the lock before Liquibase starts
            String selectQuery = "delete from " + LIQUIBASE_LOCK_TABLE;
            statement.executeUpdate(selectQuery);
        } catch (SQLException ex) {
            throw new PetascopeException(ExceptionCode.InternalSqlError, 
                    "Cannot delete lock of liquibase for petascope database "
                            + "from table '" + LIQUIBASE_LOCK_TABLE+ "', please do it manually. "
                            + "Reason: '" + ex.getMessage() + "'", ex);
        } finally {
            closeDatabaseConnection(connection);
        }
    }
    
    /**
     * Check if petascopedb is empty just by trying to connect to petascopedb
     * NOTE: don't distinguish it is petascope version 9.4 or 9.5+
     * @param dataSource
     * @return 
     * @throws java.lang.ClassNotFoundException 
     * @throws petascope.exceptions.PetascopeException 
     */
    public static boolean petascopeDatabaseEmpty(DataSource dataSource) throws ClassNotFoundException, PetascopeException {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();        
            DatabaseMetaData dbm = connection.getMetaData();
            
            // for version < 9.5
            if (sourceLegacyPetascopeDatabaseExists()) {
                return false;
            }
            
            // for version 9.5+
            // check if "coverage" table is there to know petascopedb exists
            ResultSet tables = dbm.getTables(null, null, ConfigManager.PETASCOPEDB_TABLE_EXIST, null);
            if (!tables.next()) {
                // Table does not exist, maybe it is H2/HSQL, tables is in upper cases
                tables = dbm.getTables(null, null, ConfigManager.PETASCOPEDB_TABLE_EXIST.toUpperCase(), null);
                if (!tables.next()) {
                    // Table still does not exist, petascopedb is empty
                    return true;
                }
            }
            // Table exists
            return false;
        } catch (SQLException ex) {
            throw new PetascopeException(ExceptionCode.InternalSqlError, 
                    "Cannot check if petascopedb exists in source DMBS to migrate, error '" + ex.getMessage() + "'", ex);
        } finally {
            closeDatabaseConnection(connection);
        }
    }

    /**
     * A simple check if the old petascope is preparing to migrate is prior
     * version 9.5 which uses the legacy CoverageMetadata instead of CIS data
     * model. (NOTE: used for source datasource: meta_* configuration in petascope.properties)
     *
     * @return
     * @throws java.lang.ClassNotFoundException
     * @throws petascope.exceptions.PetascopeException
     */
    public static boolean sourceLegacyPetascopeDatabaseExists() throws ClassNotFoundException, PetascopeException {
        Connection connection = null;
        try {
            connection = getDatabaseConnection(ConfigManager.SOURCE_DATASOURCE_URL,
                    ConfigManager.SOURCE_DATASOURCE_USERNAME, ConfigManager.SOURCE_DATASOURCE_PASSWORD);
            DatabaseMetaData databaseMetaData = connection.getMetaData();

            // Check if the legacy table ps_dbupdates existed
            ResultSet tables = databaseMetaData.getTables(null, null, "ps_dbupdates", null);
            if (tables.next()) {
                return true;
            }
        } catch (SQLException ex) {
            return false;
        } finally {
            closeDatabaseConnection(connection);
        }

        return false;
    }
    
    /**
     * A simple check if the old petascope is preparing to migrate is prior
     * version 9.5 which uses the legacy CoverageMetadata instead of CIS data
     * model. (NOTE: used for target datasource: spring_* configuration in petascope.properties)
     *
     * @return
     * @throws java.lang.ClassNotFoundException
     * @throws petascope.exceptions.PetascopeException
     */
    public static boolean targetLegacyPetascopeDatabaseExists() throws ClassNotFoundException, PetascopeException {
        Connection connection = null;
        try {
            connection = getDatabaseConnection(ConfigManager.PETASCOPE_DATASOURCE_URL,
                    ConfigManager.PETASCOPE_DATASOURCE_USERNAME, ConfigManager.PETASCOPE_DATASOURCE_PASSWORD);
            DatabaseMetaData databaseMetaData = connection.getMetaData();

            // Check if the legacy table ps_dbupdates existed
            ResultSet tables = databaseMetaData.getTables(null, null, "ps_dbupdates", null);
            if (tables.next()) {
                return true;
            }
        } catch (SQLException ex) {
            return false;
        } finally {
            closeDatabaseConnection(connection);
        }

        return false;
    }
    
    /**
     * Check if database by default (e.g: Postgresql) is used.
     */
    public static boolean checkDefaultDatabase() {
        if (ConfigManager.PETASCOPE_DATASOURCE_URL.contains(ConfigManager.DEFAULT_DMBS)) {
            return true;
        }
        return false;
    }

    /**
     * Create the database if it does not exist as Hibernate cannot do it. NOTE:
     * Only support Postgresql.
     *
     * Postgresql does not allow Hibernate to create database as other
     * relational databases so instead of throwing exception, it should create
     * it internally. Postgresql_URL: jdbc:postgresql://HOST/DATABASE_NAME
     *
     * @param databaseName (null: use the configured Spring JDBC URL to create
     * database, non-null: create the database with the input). Only support
     * Postgresql.
     * @throws java.sql.SQLException
     * @throws java.lang.ClassNotFoundException
     * @throws petascope.exceptions.PetascopeException
     */
    public static void createPostgresqlDatabaseIfNotExist(String databaseName) throws SQLException, ClassNotFoundException, PetascopeException {
        // No need to do anything if it is not Postgresql
        if (!checkDefaultDatabase()) {
            throw new PetascopeException(ExceptionCode.NoApplicableCode, "Cannot create database automatically for this JDBC '" + ConfigManager.PETASCOPE_DATASOURCE_URL + "'.");
        }

        Class.forName(ConfigManager.POSTGRESQL_DATASOURCE_DRIVER);
        int lastIndex = ConfigManager.PETASCOPE_DATASOURCE_URL.lastIndexOf("/");

        if (databaseName == null) {
            // Extract the database from the Spring JDBC URL for Postgresql
            databaseName = ConfigManager.PETASCOPE_DATASOURCE_URL.substring(lastIndex + 1, ConfigManager.PETASCOPE_DATASOURCE_URL.length());
        }

        // template1 is a default database for postgresql to create empty database
        String defaultURL = ConfigManager.PETASCOPE_DATASOURCE_URL.substring(0, lastIndex) + "/" + POSTGRESQL_NEUTRAL_DATABASE;

        Connection connection = null;
        Statement statement = null;
        try {            
            connection = DriverManager.getConnection(defaultURL, ConfigManager.PETASCOPE_DATASOURCE_USERNAME, ConfigManager.PETASCOPE_DATASOURCE_PASSWORD);
            // Then try to query from the database
            statement = connection.createStatement();
            String selectQuery = "select count(*) from pg_catalog.pg_database where datname = '" + databaseName + "'";
            String value = "0";
            try (ResultSet resultSet = statement.executeQuery(selectQuery)) {
                while (resultSet.next()) {
                    value = resultSet.getString("count");
                }
            }

            // If result returns zero, so database does not exist, create it
            if (value.equals("0")) {
                String sqlQuery = "CREATE DATABASE " + databaseName + "";
                statement = connection.createStatement();
                statement.executeUpdate(sqlQuery);
                log.info("CREATE DATABASE " + databaseName + " executed successfully.");
            }
        } catch (SQLException ex) {
            throw ex;
        } finally {
            closeDatabaseConnection(connection, statement);
        }
    }

    /**
     * Kill the connections to a database in Postgresql. NOTE: used only for
     * migration from legacy petascopedb prior version 9.5.
     *
     * @param databaseName
     */
    private static void killConnectionsToPostgresqlLegacyDatabase(String databaseName) throws SQLException, ClassNotFoundException, PetascopeException {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            connection = DatabaseUtil.getDatabaseConnection(SOURCE_NEUTRAL_POSTGRESQL_DATASOURCE_URL,
                    ConfigManager.SOURCE_DATASOURCE_USERNAME, ConfigManager.SOURCE_DATASOURCE_PASSWORD);

            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT current_setting('server_version_num') as version_number;");

            String terminateConnectionSQL = "select pg_terminate_backend(PID) from pg_stat_activity where datname='" + databaseName + "';";
            while (resultSet.next()) {
                // postgresql version is older than 9.2.0
                if (resultSet.getLong("version_number") < 90200) {
                    terminateConnectionSQL = terminateConnectionSQL.replace("PID", "procpid");
                } else {
                    terminateConnectionSQL = terminateConnectionSQL.replace("PID", "pid");
                }
            }

            // Send the query to Postgresql to close all the connectiosn to database
            statement.execute(terminateConnectionSQL);
        } finally {
            closeDatabaseConnection(connection, statement, resultSet);            
        }
    }

    /**
     * To migrate, legacy database in postgresql will be renamed to
     * petacopedb_94_backup. When a temp database is created to migrate and
     * after that, it could be renamed to petascopedb.
     *
     * NOTE: used only for migration from legacy petascopedb prior version 9.5.
     *
     * @param fromDatabaseName
     * @param toDatabaseName
     * @throws petascope.exceptions.PetascopeException
     * @throws java.sql.SQLException
     * @throws java.lang.ClassNotFoundException
     */
    public static void renamePostgresqLegacyDatabase(String fromDatabaseName, String toDatabaseName) throws PetascopeException, SQLException, ClassNotFoundException {

        // First, kill all the connections of fromDatabase
        killConnectionsToPostgresqlLegacyDatabase(fromDatabaseName);

        // Then can rename this database to new name        
        Connection connection = null;
        Statement statement = null;
        try {
            connection = DatabaseUtil.getDatabaseConnection(SOURCE_NEUTRAL_POSTGRESQL_DATASOURCE_URL,
                    ConfigManager.SOURCE_DATASOURCE_USERNAME, ConfigManager.SOURCE_DATASOURCE_PASSWORD);

            statement = connection.createStatement();
            statement.execute("ALTER DATABASE " + fromDatabaseName + " RENAME TO " + toDatabaseName);
        } finally {
            closeDatabaseConnection(connection, statement);
        }
    }
}