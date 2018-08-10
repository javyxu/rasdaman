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

import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import nu.xom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.WCSException;
import petascope.core.KVPSymbols;
import petascope.core.XMLSymbols;
import petascope.exceptions.PetascopeException;
import petascope.util.XMLUtil;
import petascope.wcs2.parsers.request.xml.service.IXMLProcessCoverageParserService;
import petascope.wcs2.parsers.request.xml.service.XMLProcessCoverageAbstractSyntaxParserService;
import petascope.wcs2.parsers.request.xml.service.XMLProcessCoverageOGCSyntaxParserService;

/**
 * Parse a ProcessCoverage from request body in XML to map of keys, values as
 * KVP request.
 *
 * NOTE: There are 3 types of WCPS POST request (xmlSyntax, abstractSyntax and OGC POST syntax)
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Service
// Create a new instance of this bean for each request (so it will not use the old object with stored data)
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class XMLProcessCoverageParser extends XMLAbstractParser {
    
    private static Logger log = LoggerFactory.getLogger(XMLProcessCoverageParser.class);

    @Resource
    // Spring finds all the subclass of AbstractHandler and injects to the list
    List<IXMLProcessCoverageParserService> parserServices;
    
    @Override
    public Map<String, String[]> parse(String requestBody) throws WCSException, PetascopeException {
        // Only used when xml_validation=true in petascope.properties
        this.validateXMLRequestBody(requestBody);
        
        // NOTE: with request with <abstractSyntax> for ... </abstractSyntax>
        // or <proc:query>for ....</proc:query>
        // the WCPS query inisde the XML elmements, must add "<![CDATA[" + query + "]]>,
        // or it is not valid XML for character like: c > 3, a < 1.
        String openElement = null;
        String closeElement = null;        
        if (requestBody.contains(XMLProcessCoverageAbstractSyntaxParserService.QUERY_OPEN_ELEMENT)) {
            openElement = XMLProcessCoverageAbstractSyntaxParserService.QUERY_OPEN_ELEMENT;
            closeElement = XMLProcessCoverageAbstractSyntaxParserService.QUERY_CLOSE_ELEMENT;
        } else if (requestBody.contains(XMLProcessCoverageOGCSyntaxParserService.QUERY_OPEN_ELEMENT)) {
            openElement = XMLProcessCoverageOGCSyntaxParserService.QUERY_OPEN_ELEMENT;
            closeElement = XMLProcessCoverageOGCSyntaxParserService.QUERY_CLOSE_ELEMENT;
        }
        if (openElement != null) {
            requestBody = XMLUtil.addCdataInsideElement(requestBody, openElement, closeElement);
        }
        
        Element rootElement = parseInput(requestBody);
        // e.g:  <proc:ProcessCoverages service="WCS" version="2.0.1">...</proc:ProcessCoverages>
        String version = rootElement.getAttributeValue(XMLSymbols.ATT_VERSION);
        kvpParameters.put(KVPSymbols.KEY_SERVICE, new String[]{KVPSymbols.WCS_SERVICE});
        kvpParameters.put(KVPSymbols.KEY_VERSION, new String[]{version});
        kvpParameters.put(KVPSymbols.KEY_REQUEST, new String[]{KVPSymbols.VALUE_PROCESS_COVERAGES});

        // Try to parse the WCPS query in abstract syntax from XML elements
        String wcpsQuery = parseWCPSQueryFromXML(requestBody);
        if (wcpsQuery == null) {
            log.error("No XML parser can handle the ProcessCoverages in XML POST request, please check if the XML request is valid.");
            throw new WCSException(ExceptionCode.InvalidRequest, "Invalid XML ProcessCoverages request.");
        } else {
            kvpParameters.put(KVPSymbols.KEY_QUERY, new String[]{wcpsQuery});        
        }
        
        log.debug("Abstract WCPS query parsed from XML POST request body: '" + wcpsQuery + "'.");

        return kvpParameters;
    }
    
    /**
     * Return the WCPS query in abstract syntax from the XML POST request body
     * which is in XML elements
     * @param requestBody
     * @return 
     * @throws petascope.exceptions.WCSException 
     */
    public String parseWCPSQueryFromXML(String requestBody) throws WCSException, PetascopeException {
        Element rootElement = parseInput(requestBody);
        String wcpsQuery = null;
        for (IXMLProcessCoverageParserService parserService : parserServices) {
            if (parserService.canParse(rootElement)) {
                log.debug("Found the XML ProcessCoverage parser: '" + parserService.getClass().getCanonicalName() + "'.");
                wcpsQuery = parserService.parseXMLRequest(rootElement);                
            }
        } 
        
        // NOTE: the output is a WCPS query (e.g: for c .... ), 
        // but because it was enquoted CDATA element for AbstractSyntax and OGCSyntax, so strip it if it exists
        wcpsQuery = XMLUtil.dequoteCDATA(wcpsQuery);
                
        return wcpsQuery;
    }
}
