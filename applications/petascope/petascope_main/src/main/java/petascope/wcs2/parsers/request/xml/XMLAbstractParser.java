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
package petascope.wcs2.parsers.request.xml;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
import org.rasdaman.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import petascope.core.KVPSymbols;
import petascope.core.XMLSymbols;
import static petascope.core.XMLSymbols.ATT_SERVICE;
import static petascope.core.XMLSymbols.ATT_VERSION;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.WCSException;
import petascope.util.XMLUtil;
import static petascope.util.XMLUtil.WCS_SCHEMA_URL;

/**
 * Interface for all WCS POST XML request parsers
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public abstract class XMLAbstractParser {

    private static Logger log = LoggerFactory.getLogger(XMLAbstractParser.class);

    protected Map<String, String[]> kvpParameters = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    // load WCS Schema when initializing PetascopeInterafce.
    protected static Schema schema;

    /**
     * Parse a XML request body to map of keys, values
     *
     * @param requestBody
     * @return
     * @throws petascope.exceptions.WCSException
     */
    abstract protected Map<String, String[]> parse(String requestBody) throws WCSException, PetascopeException;

    /**
     * Check if version from request body is supported
     * @param version
     * @throws WCSException 
     */
    protected void validateRequestVersion(String version) throws WCSException {
        if (!version.equals(ConfigManager.WCS_VERSIONS)) {
            throw new WCSException(ExceptionCode.InvalidRequest, "WCS only supports version: " + ConfigManager.WCS_VERSIONS + ", given: " + version + ".");
        }
    }

    /**
     * This feature takes time (~1 minute) to load the necessary schema, then it
     * can validate the request body in XML. Only used when xml_validation=true
     * in petascope.properties (not set to true when testing OGC CITE).
     *
     * @param requestBody
     * @throws petascope.exceptions.WCSException
     */
    protected void validateXMLRequestBody(String requestBody) throws WCSException {
        if (ConfigManager.XML_VALIDATION) {
            // create XML validator
            Source requestStream = new StreamSource(new StringReader(requestBody));
            Validator validator = schema.newValidator();

            // validate
            try {
                validator.validate(requestStream);
            } catch (SAXException e) {
                throw new WCSException(ExceptionCode.XmlNotValid, "The structure of the provided input is not valid.", e);
            } catch (NullPointerException e) {
                throw new WCSException(ExceptionCode.InvalidRequest, "The received XML document is empty.", e);
            } catch (IOException e) {
                throw new WCSException(ExceptionCode.WcsError, "A fatal error ocurred while validating the input schema.", e);
            }
        }
    }

    /**
     * When xml_validation=true in petascope.properties then caching WCS schema
     * from opengis to validate the input XML request.
     *
     * @throws WCSException
     */
    public static void loadWcsSchema() throws WCSException {
        if (ConfigManager.XML_VALIDATION) {
            log.debug("Loading XML schema definition from " + WCS_SCHEMA_URL + "...");

            URL schemaURL = null;
            try {
                schemaURL = new URL(WCS_SCHEMA_URL);
            } catch (MalformedURLException ex) {
                log.error("Cannot load WCS 2.0.1 schema from resource file: " + WCS_SCHEMA_URL);
                throw new WCSException(ExceptionCode.IOConnectionError, "Cannot load WCS 2.0.1 schema from: " + WCS_SCHEMA_URL, ex);
            }

            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            try {
                schema = schemaFactory.newSchema(schemaURL);
            } catch (SAXException ex) {
                log.error("Cannot build WCS 2.0.1 schema object.");
                throw new WCSException(ExceptionCode.IOConnectionError, "Cannot build WCS 2.0.1 schema object.", ex);
            }
        }
    }
    
    /**
     * Parse a WCS requestBody in XML to a XML document with root element
     *
     * @param input
     * @return
     * @throws WCSException
     */
    protected Element parseInput(String input) throws WCSException, PetascopeException {
        try {
            Document doc = XMLUtil.buildDocument(null, input);
            Element rootElement = doc.getRootElement();

            // Validate the request which must contain the serviceType and the version
            String service = rootElement.getAttributeValue(ATT_SERVICE);
            if (service == null) {
                throw new WCSException(ExceptionCode.InvalidRequest, "Missing attribute '" + ATT_SERVICE + "' from request body.");
            }
            // Only WCS GetCapabilities has acceptVersions as element instead of attribute
            String rootElementName = rootElement.getLocalName();
            String version = "";
            if (!rootElementName.equals(XMLSymbols.LABEL_GET_CAPABILITIES)) {
                version = rootElement.getAttributeValue(ATT_VERSION);
                if (version == null) {
                    throw new WCSException(ExceptionCode.InvalidRequest, "Missing attribute '" + ATT_VERSION + "' in request body.");
                }
            } else {
                Element acceptVersionElement = rootElement.getFirstChildElement(XMLSymbols.OWS_LABEL_ACCEPT_VERSIONS, XMLSymbols.NAMESPACE_OWS);
                if (acceptVersionElement == null) {
                    throw new WCSException(ExceptionCode.InvalidRequest, "Missing element '" + XMLSymbols.OWS_LABEL_ACCEPT_VERSIONS + "' in WCS '" + XMLSymbols.LABEL_GET_CAPABILITIES + "' request.");
                }
                Element versionElement = acceptVersionElement.getFirstChildElement(XMLSymbols.OWS_LABEL_VERSION, XMLSymbols.NAMESPACE_OWS);
                if (versionElement == null) {
                    throw new WCSException(ExceptionCode.InvalidRequest, "Missing element '" + XMLSymbols.OWS_LABEL_VERSION + "' in WCS '" + XMLSymbols.LABEL_GET_CAPABILITIES + "' request.");
                }
                // current only support WCS 2.0.1
                version = versionElement.getValue();
            }

            // Then check the service and accepted version
            if (service.equals(KVPSymbols.WCS_SERVICE)) {
                if (!version.matches(KVPSymbols.WCS_VERSION_PATTERN)) {
                    throw new WCSException(ExceptionCode.VersionNegotiationFailed, "WCS version '" + version + "' is not supported.");
                }
            } else if (service.equals(KVPSymbols.WCPS_SERVICE)) {
                if (!version.matches(KVPSymbols.WCPS_VERSION_PATTERN)) {
                    throw new WCSException(ExceptionCode.VersionNegotiationFailed, "WCPS version '" + version + "' is not supported.");
                }
            } else {
                // neither WCS nor WCPS so not support
                throw new WCSException(ExceptionCode.InvalidRequest, "Request service '" + service + "' is not supported.");
            }

            return rootElement;
        } catch (ParsingException ex) {
            throw new WCSException(ExceptionCode.XmlNotValid.locator(
                    "line: " + ex.getLineNumber() + ", column:" + ex.getColumnNumber()),
                    ex.getMessage(), ex);
        } catch (IOException | WCSException ex) {
            throw new WCSException(ExceptionCode.XmlNotValid, ex.getMessage(), ex);
        }
    }
}
