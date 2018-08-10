/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU  General Public License for more details.
 *
 * You should have received a copy of the GNU  General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package org.rasdaman.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.rasdaman.exceptions.RasdamanException;
import petascope.util.IOUtil;
import petascope.util.StringUtil;
import petascope.util.ras.RasUtil;

/**
 * Configuration Manager class: a single entry point for all server settings.
 * Implements the singleton design pattern.
 *
 * Note (AB): Although this class implements the singleton pattern, it offers
 * public static members that come pre-initialized, allowing use of values that
 * are not provided by the configuration file without getting an instance. These
 * should be made private and only the get method on the unique instance allowed
 *
 * @author Andrei Aiordachioaie
 * @author Dimitar Misev
 */
public class ConfigManager {

    private static final Logger log = LoggerFactory.getLogger(ConfigManager.class);
    /* Singleton instance */
    private static ConfigManager instance;
    private static Properties props;

    // For all exceptions, default language is english
    public static final String LANGUAGE = "en";

    /* **** Default endpoint for controllers to handle services **** */
    public static final String MIGRATION = "migration";
    public static final String OWS = "ows";
    public static final String WCPS = "wcps";
    public static final String RASQL = "rasql";
    public static final String OWS_ADMIN = OWS + "/" + "admin";
    public static final String ADMIN = "admin";
    public static final String GET_COVERAGE_EXTENTS = "GetCoveragesExtents";
    
    /* **** Default DMBS for petascope is Postgresql **** */
    public static final String DEFAULT_DMBS = "postgresql";
    // to check if petascopedb (version 9.5+) exists in DBMS
    public static final String PETASCOPEDB_TABLE_EXIST = "coverage";

    /* ***** Services version configuration ***** */
    // CRS resolver
    public static final String SECORE_VERSION = "0.1.0";
    // OGC services info    
    public static final String CIS_VERSION = "1.1";
    public static final String WCST_VERSION = "2.0.0";
    public static final String WCPS_VERSION = "1.0.0";
    public static final String WCS_VERSIONS = "2.0.1";
    public static final String RASQL_SERVLET_VERSION = "1.0.0";
    public static final String WMS_VERSIONS = "1.3.0";

    /* **** Petascope configuration **** */
    // confdir parameter name
    public static final String PETASCOPE_PROPERTIES_FILE = "petascope.properties";    
    // The default database name of petascope
    public static final String PETASCOPE_DB = "petascopedb";

    // e.g: /rasdaman
    public static String PETASCOPE_APPLICATION_CONTEXT_PATH;
    // In case of using proxy, this value needs to be changed
    public static String PETASCOPE_ENDPOINT_URL;
    // configuration for database connection
    public static String PETASCOPE_DATASOURCE_URL;
    public static String PETASCOPE_DATASOURCE_USERNAME;
    public static String PETASCOPE_DATASOURCE_PASSWORD;
    // path to JDBC driver jar file if user doesn't use postgresql
    public static String PETASCOPE_DATASOURCE_JDBC_JAR_PATH;
    // simple user name for admin to update the OWS Service metadata
    public static String PETASCOPE_ADMIN_USERNAME;
    public static String PETASCOPE_ADMIN_PASSWORD;

    // For old Petascopedb to migrate (source data source)
    public static String POSTGRESQL_DATASOURCE_DRIVER = "org.postgresql.Driver";
    public static String SOURCE_DATASOURCE_URL;
    public static String SOURCE_DATASOURCE_USERNAME;
    public static String SOURCE_DATASOURCE_PASSWORD;
    // path to JDBC driver jar file if user doesn't use postgresql
    public static String SOURCE_DATASOURCE_JDBC_JAR_PATH;

    // XML validation schema control setting POST, SOAP request validation for WCS request
    public static boolean XML_VALIDATION = false;
    // Only used when testing OGC CITE
    public static boolean OGC_CITE_OUTPUT_OPTIMIZATION = false;
    
    public static String DEFAULT_DIR_TMP = "/tmp";
    // Tomcat user will create this folder
    public static String DEFAULT_PETASCOPE_DIR_TMP = "/tmp/rasdaman_petascope";
    /* ***** Petascope Uploaded files configuration ***** */    
    // Any files are posted to controllers will store here and be removed when request is done
    public static String UPLOADED_FILE_DIR_TMP = DEFAULT_PETASCOPE_DIR_TMP + "/" + "upload";    
    // Any uploaded file to server to process will have this prefix (e.g on server: rasdaman.uploadedfile.datetime)
    public static String UPLOAD_FILE_PREFIX = "rasdaman.";
    
    public static String WCST_TMP_DIR = "/tmp/rasdaman_wcst_import/";

    /* ***** rasdaman configuration ***** */
    public static String RASDAMAN_SERVER = "localhost";
    public static String RASDAMAN_PORT = "7001";
    public static String RASDAMAN_URL = "http://" + RASDAMAN_SERVER + ":" + RASDAMAN_PORT;
    public static String RASDAMAN_DATABASE = "RASBASE";
    public static String RASDAMAN_USER = "rasguest";
    public static String RASDAMAN_PASS = "rasguest";
    public static String RASDAMAN_ADMIN_USER = "rasadmin";
    public static String RASDAMAN_ADMIN_PASS = "rasadmin";
    public static String RASDAMAN_VERSION = "";
    public static String RASDAMAN_BIN_PATH = "";
    // Retry settings when opening a connection to rasdaman server. Ernesto Rodriguez <ernesto4160@gmail.com>
    // Maximum number of re-connect attempts
    public static String RASDAMAN_RETRY_ATTEMPTS = "5";
    // Time in seconds between each re-connect attempt
    public static String RASDAMAN_RETRY_TIMEOUT = "10";

    /* ***** SECORE configuration ***** */
    public static List<String> SECORE_URLS;
    public static final int CRSRESOLVER_CONN_TIMEOUT = 10000;
    public static final int CRSRESOLVER_READ_TIMEOUT = 10000;

    /* ***** WCST configuration ***** */
    // disable write operations (WCST)
    public static boolean DISABLE_WRITE_OPERATIONS = false;

    /* ***** WMS configuration ***** */
    public static long WMS_MAXIMUM_CACHE_SIZE = 100000000; // 100 MB

    // properties's keys in petascope.properties file
    /* ***** Petascope configuration ***** */
    private static final String KEY_PETASCOPE_DATASOURCE_URL = "spring.datasource.url";
    private static final String KEY_PETASCOPE_DATASOURCE_USERNAME = "spring.datasource.username";
    private static final String KEY_PETASCOPE_DATASOURCE_PASSWORD = "spring.datasource.password";
    // If user doesn't use default postgresql (e.g: H2 database), then user needs to provide a corresponding JDBC driver (h2-jdbc.jar) manually
    public static final String KEY_PETASCOPE_DATASOURCE_JDBC_JAR_PATH = "spring.datasource.jdbc_jar_path";
    private static final String KEY_PETASCOPE_SERVLET_URL = "petascope_servlet_url";
    private static final String KEY_APPLICATION_NAME = "server.contextPath";

    // For old Petascopedb to migrate (source datasource)
    private static final String KEY_SOURCE_DATASOURCE_URL = "metadata_url";
    private static final String KEY_SOURCE_DATASOURCE_USERNAME = "metadata_user";
    private static final String KEY_SOURCE_DATASOURCE_PASSWORD = "metadata_pass";
    // If user doesn't use default postgresql (e.g: H2 database), then user needs to provide a corresponding JDBC driver (h2-jdbc.jar) manually
    public static final String KEY_SOURCE_DATASOURCE_JDBC_JAR_PATH = "metadata_jdbc_jar_path";

    // For simple admin login to update OWS Service metadata
    private static final String KEY_PETASCOPE_ADMIN_USERNAME = "petascope_admin_user";
    private static final String KEY_PETASCOPE_ADMIN_PASSWORD = "petascope_admin_pass";

    /* ***** Rasdaman configuration ***** */
    private static final String KEY_RASDAMAN_DATABASE = "rasdaman_database";
    private static final String KEY_RASDAMAN_URL = "rasdaman_url";
    private static final String KEY_RASDAMAN_USER = "rasdaman_user";
    private static final String KEY_RASDAMAN_PASS = "rasdaman_pass";
    private static final String KEY_RASDAMAN_ADMIN_USER = "rasdaman_admin_user";
    private static final String KEY_RASDAMAN_ADMIN_PASS = "rasdaman_admin_pass";
    private static final String KEY_RASDAMAN_RETRY_TIMEOUT = "rasdaman_retry_timeout";
    private static final String KEY_RASDAMAN_RETRY_ATTEMPTS = "rasdaman_retry_attempts";
    private static final String KEY_RASDAMAN_BIN_PATH = "rasdaman_bin_path";
    
    /* ***** Petascope uploaded file configuration ***** */
    private static final String KEY_UPLOADED_FILE_DIR_TMP = "uploaded_files_dir_tmp";

    /* ***** SECORE configuration ***** */
    private static final String KEY_SECORE_URLS = "secore_urls";

    /* ***** WCS configuration ***** */
    // validate XML POST input request with XML Schema (not set to true when OGC CITE testing)
    private static final String KEY_XML_VALIDATION = "xml_validation";
    // Only used for OGC CITE test as it will optimize output from WCS to bypass some test cases (xml_validation must set to false).
    private static final String KEY_OGC_CITE_OUTPUT_OPTIMIZATION = "ogc_cite_output_optimization";

    /* ***** WCST configuration ***** */
    private static final String KEY_DISABLE_WRITE_OPERATIONS = "disable_write_operations";

    /* ***** LOG4J configuration ***** */
    // from petascope.properties used for log4j
    private static final String KEY_LOG_FILE_PATH = "log4j.appender.rollingFile.File";

    /**
     * Initialize all the keys, values of petascope.properties
     *
     * @param confDir
     * @throws petascope.rasdaman.exceptions.RasdamanException
     * @para
     */
    public static void init(String confDir) throws RasdamanException, PetascopeException {
        if (instance == null) {
            instance = new ConfigManager(confDir);
        }
    }

    /**
     * Returns the instance of the ConfigManager. If no such instance exists, it
     * creates one with the specified settings file.
     *
     * @param confDir Path to the settings file
     * @return instance of the ConfigManager class
     * @throws RasdamanException
     */
    public static ConfigManager getInstance(String confDir) throws PetascopeException {
        if (instance == null) {
            throw new PetascopeException(ExceptionCode.InternalComponentError, "Could not intialize ConfigManager object.");
        }
        return instance;
    }

    /**
     * Private constructor. Use <i>getInstance()</i>.
     *
     * @param confDir Path to the properties directory
     * @throws RasdamanException
     */
    private ConfigManager(String confDir) throws RasdamanException, PetascopeException {

        if (confDir == null) {
            StringBuilder msg = new StringBuilder();
            msg.append("Your petascope.properties file is missing the configuration dir parameter.\n");
            throw new IllegalArgumentException(msg.toString());
        }

        if (!(new File(confDir)).isDirectory()) {
            String msg = "Configuration directory not found.";
            throw new IllegalArgumentException(msg);
        }

        confDir = IOUtil.wrapDir(confDir);
        log.debug("Configuration dir: " + confDir);

        // load logging configuration from setting files first to print the general metadata for services with log.info
        try {
            PropertyConfigurator.configure(confDir + PETASCOPE_PROPERTIES_FILE);            
        } catch (Exception ex) {
            System.err.println("Error loading logger configuration: " + confDir + PETASCOPE_PROPERTIES_FILE + ", error mesage: " + ex.getMessage());
            BasicConfigurator.configure();
        }

        // load petascope configuration
        props = new Properties();
        try {
            InputStream is = new FileInputStream(new File(confDir + PETASCOPE_PROPERTIES_FILE));
            props.load(is);
            this.initSettings();
        } catch (IOException e) {
            log.error("Failed loading the settings file " + confDir + PETASCOPE_PROPERTIES_FILE, e);
            throw new RuntimeException("Failed loading the settings file " + confDir + PETASCOPE_PROPERTIES_FILE, e);
        }

        String logFilePath = props.getProperty(KEY_LOG_FILE_PATH);

        // there is log file path in petascope.properties
        if (logFilePath != null) {
            File f = new File(logFilePath);
            // If the log file path is configured as absolute path, we check the write permision of Tomcat username on this file.
            if (f.isAbsolute()) {
                if (!f.canWrite()) {
                    log.warn("Cannot write to the petascope log file defined in petascope.properties: " + logFilePath + ".\n"
                            + "Please make sure the path specified by " + KEY_LOG_FILE_PATH + " in petascope.properties is"
                            + " a location where the system user running Tomcat has write access."
                            + " Otherwise, the petascope log can only be found in the Tomcat log (usually catalina.out).");
                }
            } else {
                // log file path is relative, we don't know where directory user want to set the log file, so user will need to see the log in catalina.out
                log.warn(KEY_LOG_FILE_PATH + " is set to relative path: " + logFilePath + " in petascope.properties; it is recommended to set it to an absolute path."
                        + " In any case, the petascope log can be found in the Tomcat log (usually catalina.out).");
            }
        }
        
        try {
            File wcstTmpDir = new File(ConfigManager.WCST_TMP_DIR);
            FileUtils.forceMkdir(wcstTmpDir);
            wcstTmpDir.setReadable(true, false);
        } catch (IOException ex) {
            throw new PetascopeException(ExceptionCode.RuntimeError,
                        "Cannot create WCST temp directory at '" + ConfigManager.WCST_TMP_DIR + "'. Reason: " + ex.getMessage() + ".");
        }
    }

    /**
     * Return a setting value from the settings file
     *
     * @param key Key of the setting
     * @return String value, or the empty string in case the key does not exist
     */
    private String get(String key) {
        String result = "";
        if (props.containsKey(key)) {
            result = props.getProperty(key);
        }
        return result;
    }

    /**
     * Overwrite defaults settings with user-defined values in
     * petascope.properties
     *
     * @throws RasdamanException
     */
    private void initSettings() throws RasdamanException {

        /* ***** Petascope configuration ***** */
        PETASCOPE_ENDPOINT_URL = get(KEY_PETASCOPE_SERVLET_URL);
        PETASCOPE_APPLICATION_CONTEXT_PATH = get(KEY_APPLICATION_NAME);

        PETASCOPE_DATASOURCE_URL = get(KEY_PETASCOPE_DATASOURCE_URL);
        PETASCOPE_DATASOURCE_USERNAME = get(KEY_PETASCOPE_DATASOURCE_USERNAME);
        PETASCOPE_DATASOURCE_PASSWORD = get(KEY_PETASCOPE_DATASOURCE_PASSWORD);        
        PETASCOPE_DATASOURCE_JDBC_JAR_PATH = get(KEY_PETASCOPE_DATASOURCE_JDBC_JAR_PATH);

        // For legacy Petascopedb
        SOURCE_DATASOURCE_URL = get(KEY_SOURCE_DATASOURCE_URL);
        SOURCE_DATASOURCE_USERNAME = get(KEY_SOURCE_DATASOURCE_USERNAME);
        SOURCE_DATASOURCE_PASSWORD = get(KEY_SOURCE_DATASOURCE_PASSWORD);
        SOURCE_DATASOURCE_JDBC_JAR_PATH = get(KEY_SOURCE_DATASOURCE_JDBC_JAR_PATH);

        // For simple admin user to update OWS Service metadata
        PETASCOPE_ADMIN_USERNAME = get(KEY_PETASCOPE_ADMIN_USERNAME);
        PETASCOPE_ADMIN_PASSWORD = get(KEY_PETASCOPE_ADMIN_PASSWORD);

        /* ***** Rasdaman configuration ***** */
        RASDAMAN_DATABASE = get(KEY_RASDAMAN_DATABASE);
        RASDAMAN_URL = get(KEY_RASDAMAN_URL);
        RASDAMAN_USER = get(KEY_RASDAMAN_USER);
        RASDAMAN_PASS = get(KEY_RASDAMAN_PASS);
        RASDAMAN_ADMIN_USER = get(KEY_RASDAMAN_ADMIN_USER);
        RASDAMAN_ADMIN_PASS = get(KEY_RASDAMAN_ADMIN_PASS);
        RASDAMAN_RETRY_TIMEOUT = get(KEY_RASDAMAN_RETRY_TIMEOUT);
        RASDAMAN_RETRY_ATTEMPTS = get(KEY_RASDAMAN_RETRY_ATTEMPTS);
        RASDAMAN_BIN_PATH = get(KEY_RASDAMAN_BIN_PATH);        

        /* ***** SECORE configuration ***** */
        SECORE_URLS = StringUtil.csv2list(get(KEY_SECORE_URLS));
        if (SECORE_URLS.isEmpty()) {
            log.error("Failed loading secore urls from petascope.properties");
            throw new RuntimeException("Failed loading secore urls from petascope.properties");
        }

        /* ***** WCS configuration ***** */
        // XML-encoded request schema validation for input request in XML POST
        XML_VALIDATION = Boolean.parseBoolean(get(KEY_XML_VALIDATION));
        // Only used when testing OGC CITE (with xml_validation is set to false)
        OGC_CITE_OUTPUT_OPTIMIZATION = Boolean.parseBoolean(get(KEY_OGC_CITE_OUTPUT_OPTIMIZATION));

        /* ***** WCST configuration ***** */
        // Disable write operations
        DISABLE_WRITE_OPERATIONS = Boolean.parseBoolean(get(KEY_DISABLE_WRITE_OPERATIONS));

        /* ***** Petascope uploaded files directory configuration ***** */
        UPLOADED_FILE_DIR_TMP = get(KEY_UPLOADED_FILE_DIR_TMP);
        if (!StringUtils.isEmpty(UPLOADED_FILE_DIR_TMP)) {
            // try to create this folder (e.g: /tmp/rasdaman/petascope/upload)
            File file = new File(UPLOADED_FILE_DIR_TMP);
            try {
                file.mkdirs();
            } catch (Exception ex) {
                // Cannot create uploaded files folder configured in properties file, fallback to /tmp
                UPLOADED_FILE_DIR_TMP = DEFAULT_DIR_TMP;
                log.warn("Cannot create folder for this key '" + KEY_UPLOADED_FILE_DIR_TMP + "' from properties file. Reason '" + ex + "'."
                        + "Uploaded files will be set to " + DEFAULT_DIR_TMP + " folder.'");
            }
        } else {
            // no configuration for this key in properties file, set it to default /tmp folder
            UPLOADED_FILE_DIR_TMP = DEFAULT_DIR_TMP;
        }
        
        // Get rasdaman version from RasQL (see #546)
        try {
            RASDAMAN_VERSION = RasUtil.getRasdamanVersion(); 
        } catch (RasdamanException ex) {
            // cannot connect to rasdaman server, set default version
            RASDAMAN_VERSION = "9.5";
        }
        

        log.info("------------------------------------");

        log.info("-- PETASCOPE");
        log.info("Version: " + RASDAMAN_VERSION);
        log.info("DB URL: " + PETASCOPE_DATASOURCE_URL);
        log.info("DB User: " + PETASCOPE_DATASOURCE_USERNAME);
        log.info("");

        log.info("-- RASDAMAN");
        log.info("Version: " + RASDAMAN_VERSION);
        log.info("URL: " + RASDAMAN_URL);
        log.info("DB: " + RASDAMAN_DATABASE);
        log.info("User: " + RASDAMAN_USER);
        log.info("");

        log.info("-- SECORE");
        log.info("Version: " + SECORE_VERSION);
        log.info("URL: " + SECORE_URLS);
        log.info("");

        log.info("-- OGC STANDARDS");
        log.info("CIS: " + CIS_VERSION);
        log.info("WCS: " + WCS_VERSIONS);
        log.info("WCS-T: " + WCST_VERSION);
        log.info("WCPS: " + WCPS_VERSION);
        log.info("WMS: " + WMS_VERSIONS);

        log.info("------------------------------------");
    }
}

