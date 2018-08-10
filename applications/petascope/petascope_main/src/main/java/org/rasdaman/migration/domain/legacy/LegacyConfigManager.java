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
package org.rasdaman.migration.domain.legacy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;
import org.rasdaman.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.rasdaman.migration.domain.legacy.LegacyIOUtil;
import org.rasdaman.migration.domain.legacy.LegacyStringUtil;
import org.rasdaman.migration.domain.legacy.LegacyXMLUtil;

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
public class LegacyConfigManager {

    private static final Logger log = LoggerFactory.getLogger(LegacyConfigManager.class);
    // from petascope.properties used for log4j
    private static final String LOG_FILE_PATH = "log4j.appender.rollingFile.File";

    public final static String PETASCOPE_LANGUAGE = "en";
    /* If the value no given in petascope.properties, this URL gets initialized
     * automatically when the first request is received.
     * Its value is used in the Capabilities response */
    public static String PETASCOPE_SERVLET_URL = "";

    /*
     * settings.properties
     */

    // petascope metadata (stored in postgres)
    public static String METADATA_DRIVER = "org.postgresql.Driver";
    public static final String METADATA_SQLITE_DRIVER = "org.sqlite.JDBC";
    public static final String METADATA_HSQLDB_DRIVER = "org.hsqldb.jdbcDriver";
    public static String METADATA_URL = "jdbc:postgresql://localhost:5432/petascopedb";
    public static String METADATA_USER = "petauser";
    public static String METADATA_PASS = "petapasswd";
    // user updates service provider, service identification (user can change later in petascope.properties)
    public static String PETASCOPE_ADMIN_USER = "petauser";
    public static String PETASCOPE_ADMIN_PASS = "petapasswd";
    public static boolean METADATA_SQLITE = false;
    public static boolean METADATA_HSQLDB = false;

    // rasdaman connection settings
    public static String RASDAMAN_SERVER = "localhost";
    public static String RASDAMAN_PORT = "7001";
    public static String RASDAMAN_URL = "http://" + RASDAMAN_SERVER + ":" + RASDAMAN_PORT;
    public static String RASDAMAN_DATABASE = "RASBASE";
    public static String RASDAMAN_USER = "rasguest";
    public static String RASDAMAN_PASS = "rasguest";
    public static String RASDAMAN_ADMIN_USER = "rasadmin";
    public static String RASDAMAN_ADMIN_PASS = "rasadmin";
    public static String RASDAMAN_VERSION = "v9.0.0beta1";
    public static String RASDAMAN_BIN_PATH = "";

    // XML validation schema control setting (SOAP request validation)
    public static boolean XML_VALIDATION = false;
    
    // Only used when testing OGC CITE
    public static boolean OGC_CITE_OUTPUT_OPTIMIZATION = false;

    //Retry settings when opening a connection to rasdaman server. Ernesto Rodriguez <ernesto4160@gmail.com>
    //Time in seconds between each re-connect attempt
    public static String RASDAMAN_RETRY_TIMEOUT = "5";
    //Maximum number of re-connect attempts
    public static String RASDAMAN_RETRY_ATTEMPTS = "3";

    // OGC services info
    public static String WCST_LANGUAGE  = "en";
    public static String WCST_VERSION = "1.1.4";
    public static String WCPS_LANGUAGE = "en";
    public static String WCPS_VERSION = "1.0.0";
    public static String WPS_LANGUAGE = "en";
    public static String WPS_VERSION = "1.0.0";
    public static String WCS_DEFAULT_LANGUAGE = "en";
    public static String WCS_DEFAULT_VERSION = "2.0.1";
    public static String WCS_LANGUAGES = "en";
    public static String WCS_VERSIONS = "1.1.2," + WCS_DEFAULT_VERSION;
    public static String WMS_LANGUAGES = "en";
    public static String WMS_VERSIONS = "1.0.0,1.1.0";  // (!) Keep consistent with WmsRequest.java
    public static String RASDAMAN_LANGUAGE = "en";

    // Coverage summary tuning patameters
    public static Boolean BBOX_IN_COVSUMMARY = true;
    public static Boolean DESCRIPTION_IN_COVSUMMARY = true;
    public static Boolean METADATA_IN_COVSUMMARY = true;

    // rasql servlet upload file for decode()
    public static String RASQL_SERVLET_UPLOAD_DIR = "/tmp/rasql_servlet_upload";

    // depends on ccip_version in the petascope settings, ccip_version=true
    // will make this flag true.
    public static boolean CCIP_HACK = false;

    // SECORE connection settings
    public static List<String> SECORE_URLS = ConfigManager.SECORE_URLS;
    public static List<String> SECORE_VERSIONS = Arrays.asList(new String[] {"0.1.0"});
    // SECORE keyword used in PS_CRS table to be replaces with the first configured resolver
    public static final String SECORE_URL_KEYWORD = "%SECORE_URL%";
    // [!] Must match with what manually inserted in petascopedb (mind also global_const.sql URLs)

    /* WPS variables*/
    public static URI WPS_GET_CAPABILITIES_URI;
    public static URI WPS_DESCRIBE_PROCESS_URI;

    /* WCS-T Settings. Overridden by user-preferences in <code>settings.properties</code> */
    public static String WCST_DEFAULT_DATATYPE = "unsigned char";

    /* CRS RESOLVERS' timeouts (milliseconds) */
    public static final int CRSRESOLVER_CONN_TIMEOUT = 2000;
    public static final int CRSRESOLVER_READ_TIMEOUT = 10000;

    /* Singleton instance */
    private static LegacyConfigManager instance;
    private static Properties props;

    // confdir parameter name
    public static final String CONF_DIR = "confDir";
    public static final String CONF_DIR_DEFAULT = "@confdir@";
    public static final String SETTINGS_FILE = "petascope.properties";

    // disable write operations (WCST)
    public static boolean DISABLE_WRITE_OPERATIONS = false;

    // keys
    public static final String KEY_BBOX_IN_COVSUMMARY = "bbox_in_covsummary";
    public static final String KEY_DESCRIPTION_IN_COVSUMMARY = "description_in_covsummary";
    public static final String KEY_METADATA_IN_COVSUMMARY = "metadata_in_covsummary";    
    public static final String KEY_RASDAMAN_DATABASE = "rasdaman_database";
    public static final String KEY_RASDAMAN_URL = "rasdaman_url";
    public static final String KEY_RASDAMAN_USER = "rasdaman_user";
    public static final String KEY_RASDAMAN_PASS = "rasdaman_pass";
    public static final String KEY_RASDAMAN_ADMIN_USER = "rasdaman_admin_user";
    public static final String KEY_RASDAMAN_ADMIN_PASS = "rasdaman_admin_pass";
    public static final String KEY_RASDAMAN_VERSION = "rasdaman_version";
    public static final String KEY_METADATA_DRIVER = "metadata_driver";
    public static final String KEY_METADATA_URL = "metadata_url";
    public static final String KEY_METADATA_USER = "metadata_user";
    public static final String KEY_METADATA_PASS = "metadata_pass";
    public static final String KEY_RASQL_SERVLET_UPLOAD_PATH = "rasql_servlet_upload_path";
    // users edit service provider, service identification
    public static final String KEY_PETASCOPE_ADMIN_USER = "petascope_admin_user";
    public static final String KEY_PETASCOPE_ADMIN_PASS = "petascope_admin_pass";
    public static final String KEY_RASDAMAN_RETRY_TIMEOUT = "rasdaman_retry_timeout";
    public static final String KEY_RASDAMAN_RETRY_ATTEMPTS = "rasdaman_retry_attempts";
    public static final String KEY_CCIP_VERSION = "ccip_version";
    public static final String KEY_WCST_DEFAULT_DATATYPE = "default_datatype";
    public static final String KEY_SECORE_URLS = "secore_urls";
    public static final String KEY_SECORE_VERSIONS = "secore_versions";
    // validate SOAP input request with XML Schema
    public static final String KEY_XML_VALIDATION = "xml_validation";
    // Only used for OGC CITE test as it will optimize output from WCS to bypass some test cases (xml_validation must set to false).
    public static final String KEY_OGC_CITE_OUTPUT_OPTIMIZATION = "ogc_cite_output_optimization";
    public static final String KEY_PETASCOPE_SERVLET_URL = "petascope_servlet_url";
    public static final String KEY_RASDAMAN_BIN_PATH = "rasdaman_bin_path";
    public static final String KEY_DISABLE_WRITE_OPERATIONS = "disable_write_operations";

    public static final String TEMPLATES_PATH = "../templates/";
    public static final String GETCAPABILITIES_XML = "GetCapabilities.xml";
    public static final String DESCRIBEPROCESS_XML = "DescribeProcess.xml";

    /**
     * Private constructor. Use <i>getInstance()</i>.
     *
     * @param confDir Path to the settings directory
     * @throws RasdamanException
     */
    private LegacyConfigManager(String confDir) throws Exception {

        if (confDir == null) {
            StringBuilder msg = new StringBuilder();
            msg.append("Your web.xml file is missing the configuration dir parameter.\n");
            msg.append("Please add the following in your $CATALINA_HOME/webapps/petascope/WEB-INF/web.xml:\n");
            msg.append("<context-param>\n");
            msg.append("    <description>Directory containing the configuration files</description>\n");
            msg.append("    <param-name>confDir</param-name>\n");
            msg.append("    <param-value>/path/to/petascope/configuration/files</param-value>\n");
            msg.append("</context-param>\n");

            System.err.println(msg.toString());
            throw new IllegalArgumentException(msg.toString());
        }

        if (confDir.equals(CONF_DIR_DEFAULT)) {
            String msg = "Please set a valid path in your $CATALINA_HOME/webapps/petascope/WEB-INF/web.xml for the confDir parameter.";
            System.err.println(msg);
            throw new IllegalArgumentException(msg);
        }

        if (!(new File(confDir)).isDirectory()) {
            String msg = "Configuration directory not found, please update the confDir in your $CATALINA_HOME/webapps/petascope/WEB-INF/web.xml";
            System.err.println(msg);
            throw new IllegalArgumentException(msg);
        }

        confDir = LegacyIOUtil.wrapDir(confDir);
        // moved configuration files from the war file to a directory specified in web.xml -- DM 2012-jul-09
        log.debug("Configuration dir: " + confDir);

        // init XML parser
        LegacyXMLUtil.init();

        // load petascope configuration
        props = new Properties();
        try {
            InputStream is = new FileInputStream(new File(confDir + SETTINGS_FILE));
            if (is != null) {
                props.load(is);
            }
            initSettings();
        } catch (IOException e) {
            log.error("Failed loading the settings file " + confDir + SETTINGS_FILE, e);
            throw new RuntimeException("Failed loading the settings file " + confDir + SETTINGS_FILE, e);
        }

        // load logging configuration
        try {
            PropertyConfigurator.configure(confDir + SETTINGS_FILE);
        } catch (Exception ex) {
            System.err.println("Error loading logger configuration: " + confDir + SETTINGS_FILE);
            ex.printStackTrace();
            BasicConfigurator.configure();
        }

        String logFilePath = props.getProperty(LOG_FILE_PATH);

        // there is log file path in petascope.properties
        if (logFilePath != null) {
            File f = new File(logFilePath);
            // If the log file path is configured as absolute path, we check the write permision of Tomcat username on this file.
            if (f.isAbsolute()) {
                if (!f.canWrite()) {
                    log.warn("Cannot write to the petascope log file defined in petascope.properties: "  + logFilePath + ".\n"
                             + "Please make sure the path specified by " + LOG_FILE_PATH + " in petascope.properties is"
                             + " a location where the system user running Tomcat has write access."
                             + " Otherwise, the petascope log can only be found in the Tomcat log (usually catalina.out).");
                }
            } else {
                // log file path is relative, we don't know where directory user want to set the log file, so user will need to see the log in catalina.out
                log.warn(LOG_FILE_PATH + " is set to relative path: " + logFilePath + " in petascope.properties; it is recommended to set it to an absolute path."
                         + " In any case, the petascope log can be found in the Tomcat log (usually catalina.out).");
            }
        }
    }

    /**
     * Returns the instance of the ConfigManager. If no such instance exists,
     * it creates one with the specified settings file.
     *
     * @param confDir Path to the settings file
     * @return instance of the ConfigManager class
     * @throws RasdamanException
     */
    public static LegacyConfigManager getInstance(String confDir) throws Exception {
        if (instance == null) {
            instance = new LegacyConfigManager(confDir);
        }
        return instance;
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
     * Overwrite defaults settings with user-defined values in petascope.properties
     * @throws RasdamanException
     */
    private void initSettings() throws Exception {

        // connections
        RASDAMAN_DATABASE       = get(KEY_RASDAMAN_DATABASE);
        RASDAMAN_URL            = get(KEY_RASDAMAN_URL);
        RASDAMAN_USER           = get(KEY_RASDAMAN_USER);
        RASDAMAN_PASS           = get(KEY_RASDAMAN_PASS);
        RASDAMAN_ADMIN_USER     = get(KEY_RASDAMAN_ADMIN_USER);
        RASDAMAN_ADMIN_PASS     = get(KEY_RASDAMAN_ADMIN_PASS);
        RASDAMAN_BIN_PATH       = get(KEY_RASDAMAN_BIN_PATH);
        METADATA_DRIVER         = get(KEY_METADATA_DRIVER);
        if (METADATA_SQLITE_DRIVER.equals(METADATA_DRIVER)) {
            METADATA_SQLITE = true;
        } else if (METADATA_HSQLDB_DRIVER.equals(METADATA_DRIVER)) {
            METADATA_HSQLDB = true;
        }
        METADATA_URL = fileToHsqlConnectionUri(get(KEY_METADATA_URL));
        METADATA_USER           = get(KEY_METADATA_USER);
        METADATA_PASS           = get(KEY_METADATA_PASS);
        PETASCOPE_ADMIN_USER    = get(KEY_PETASCOPE_ADMIN_USER);
        PETASCOPE_ADMIN_PASS    = get(KEY_PETASCOPE_ADMIN_PASS);

        RASDAMAN_RETRY_TIMEOUT  = get(KEY_RASDAMAN_RETRY_TIMEOUT);
        RASDAMAN_RETRY_ATTEMPTS = get(KEY_RASDAMAN_RETRY_ATTEMPTS);
        PETASCOPE_SERVLET_URL   = get(KEY_PETASCOPE_SERVLET_URL);

        // fat/thin coverage summaries in capability
        BBOX_IN_COVSUMMARY        = Boolean.parseBoolean(get(KEY_BBOX_IN_COVSUMMARY));
        DESCRIPTION_IN_COVSUMMARY = Boolean.parseBoolean(get(KEY_DESCRIPTION_IN_COVSUMMARY));
        METADATA_IN_COVSUMMARY    = Boolean.parseBoolean(get(KEY_METADATA_IN_COVSUMMARY));
        
        // XML-encoded request schema validation for input request in XML POST
        XML_VALIDATION            = Boolean.parseBoolean(get(KEY_XML_VALIDATION));
        
        // Only used when testing OGC CITE (with xml_validation is set to false)
        OGC_CITE_OUTPUT_OPTIMIZATION = Boolean.parseBoolean(get(KEY_OGC_CITE_OUTPUT_OPTIMIZATION));

        // Disable write operations
        DISABLE_WRITE_OPERATIONS = Boolean.parseBoolean(get(KEY_DISABLE_WRITE_OPERATIONS));

        // CCIP hack
        CCIP_HACK = Boolean.parseBoolean(get(KEY_CCIP_VERSION));

        // Get rasdaman version from RasQL (see #546)
        RASDAMAN_VERSION = "9.4";

        // SECORE
        SECORE_URLS     = LegacyStringUtil.csv2list(get(KEY_SECORE_URLS));
        if (SECORE_URLS.isEmpty()) {
            log.error("Failed loading secore urls from petascope.properties");
            throw new RuntimeException("Failed loading secore urls from petascope.properties");
        }
        SECORE_VERSIONS = LegacyStringUtil.csv2list(get(KEY_SECORE_VERSIONS));
        // check that a version is assigned to every URI, set the last version to the orphan URIs otherwise
        // NOTE: throwing an exception for a missing version is too harsh.
        if (SECORE_VERSIONS.size() < SECORE_URLS.size()) {
            String lastVersion = SECORE_VERSIONS.get(SECORE_VERSIONS.size() - 1);
            SECORE_VERSIONS.addAll(LegacyStringUtil.repeat(lastVersion, SECORE_URLS.size() - SECORE_VERSIONS.size()));
        }

        //WPS 1.0.0 describeprocess and getcapabilities documents
        /*try {
            WPS_GET_CAPABILITIES_URI = WpsServer.class.getResource(TEMPLATES_PATH + GETCAPABILITIES_XML).toURI();
            WPS_DESCRIBE_PROCESS_URI = WpsServer.class.getResource(TEMPLATES_PATH + DESCRIBEPROCESS_XML).toURI();
        } catch (Exception e) {
            log.warn("Could not find WPS GetCapabilities and DescribeProcess Documents");
        }*/

        /* User preferences override default values for WCS-T */
        String tmp = get(KEY_WCST_DEFAULT_DATATYPE);
        if (tmp.length() > 0) {
            WCST_DEFAULT_DATATYPE = tmp;
        }

        // rasql servlet upload path for decode()
        RASQL_SERVLET_UPLOAD_DIR = get(KEY_RASQL_SERVLET_UPLOAD_PATH);

        log.info("------------------------------------");
        log.info("       *** PETASCOPE ***      ");
        log.info("Petascope Version: " + RASDAMAN_VERSION);
        log.info("Metadata Driver  : " + METADATA_DRIVER);
        log.info("Metadata URL     : " + METADATA_URL);
        log.info("Metadata Username: " + METADATA_USER);
        log.info("");
        log.info("       *** RASDAMAN ***       ");
        log.info("Rasdaman URL        : " + RASDAMAN_URL);
        log.info("Rasdaman DB         : " + RASDAMAN_DATABASE);
        log.info("Rasdaman user       : " + RASDAMAN_USER);
        log.info("Rasdaman version    : " + RASDAMAN_VERSION);
        log.info("Rasdaman admin user : " + RASDAMAN_ADMIN_USER);
        log.info("");
        log.info("       *** SECORE ***       ");
        log.info("SECORE URL       : " + SECORE_URLS);
        log.info("SECORE version   : " + SECORE_VERSIONS);
        log.info("");
        log.info("       *** WCS-T ***       ");
        log.info("WCS-T Language: " + WCST_LANGUAGE);
        log.info("WCS-T Version : " + WCST_VERSION);
        log.info("WCS-T Default Datatype: " + WCST_DEFAULT_DATATYPE);
        log.info("");
        log.info("       *** WCPS ***       ");
        log.info("WCPS Language : " + WCPS_LANGUAGE);
        log.info("WCPS Version  : " + WCPS_VERSION);
        log.info("");
        log.info("       *** WCS ***       ");
        log.info("WCS Languages : " + WCS_LANGUAGES);
        log.info("WCS Versions  : " + WCS_VERSIONS);
        log.info("");
        log.info("       *** WPS ***       ");
        log.info("WPS Language  : " + WPS_LANGUAGE);
        log.info("WPS Version   : " + WPS_VERSION);
        log.info("WPS GetCapabilities template: " + WPS_GET_CAPABILITIES_URI);
        log.info("WPS DescribeProcess template: " + WPS_DESCRIBE_PROCESS_URI);
        log.info("");
        log.info("       *** WMS ***       ");
        log.info("WMS Languages : " + WMS_LANGUAGES);
        log.info("WMS Versions  : " + WMS_VERSIONS);
        log.info("");
        log.info("------------------------------------");
    }

    /**
     * Change an HSQLDB connection URI pointing to a local file database to
     * point to a localhost hsql URI.
     *
     * @param fileUri the file URL
     * @return if it's not matched to be an HSQLDB file URI the original
     * argument is returned, otherwise jdbc:hsqldb:hsql://localhost/dbName is
     * returned, where dbName is the last part of the fileUri
     */
    public static String fileToHsqlConnectionUri(String fileUri) {
        if (fileUri != null && fileUri.startsWith("jdbc:hsqldb:file:")) {
            String[] parts = fileUri.split("/");
            String dbName = parts[parts.length - 1];
            fileUri = "jdbc:hsqldb:hsql://localhost/" + dbName;
        }
        return fileUri;
    }
}
