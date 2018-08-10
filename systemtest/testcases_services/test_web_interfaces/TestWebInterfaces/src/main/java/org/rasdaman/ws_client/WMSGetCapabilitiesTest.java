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
  *  Copyright 2003 - 2017 Peter Baumann / rasdaman GmbH.
  * 
  *  For more information please see <http://www.rasdaman.org>
  *  or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package org.rasdaman.ws_client;

import java.io.IOException;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;

/**
 * Class to test wcs_client, tab WMS/GetCapabilities
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class WMSGetCapabilitiesTest extends WSAbstractSectionWebPageTest {

    private static final Logger log = Logger.getLogger(WMSGetCapabilitiesTest.class);

    public WMSGetCapabilitiesTest() {
        super();
        this.sectionName = "wms_get_capabilities";
    }

    @Override
    public void runTest(WebDriver webDriver) throws InterruptedException, IOException {
        webDriver.navigate().to(this.testURL);
        log.info("*** Testing test cases on Web URL '" + testURL + "', section '" + this.sectionName + "'. ***");

        String testCaseName;

        // Switch to iframe to parse the web element
        webDriver.switchTo().frame(0);
        
        // Focus on the second parent tab (WMS)
        this.clickOnElement(webDriver, "/html/body/div/div/div/div/ul/li[2]/a");
        
        // Click on get capabilities button
        testCaseName = this.getSectionTestCaseName("click_on_GetCapabilities_button");
        log.info("Testing change current tab to GetCapabilities...");
        this.runTestByClickingOnElement(webDriver, testCaseName, "/html/body/div/div/div/div/div/div[2]/div/ul/div/div/ul/li[1]/a");
        Thread.sleep(1000);
        /* 
        As the returned coverages from Petascope are produced from a Map, so they can show different coverageIds in second page.
        // Click on paging button        
        testCaseName = this.getSectionTestCaseName("click_on_Paging_button");
        log.info("Testing click on another paging button...");
        this.runTestByClickingOnElement(webDriver, testCaseName, "/html/body/div/div/div/div/div/div[1]/div/div/div/div[2]/uib-accordion/div/div[1]/div[2]/div/table/tfoot/tr/td/div/nav/ul/li[2]/a"); 
        */

        // Click on GML server Capabilities document dropdown button
        testCaseName = this.getSectionTestCaseName("click_on_ServiceIdentification_dropdown_button");
        log.info("Testing click on Service identification dropdown button...");
        this.runTestByClickingOnElement(webDriver, testCaseName, "/html/body/div/div/div/div/div/div[2]/div/ul/div/div/div/div[1]/div/div/div/div[2]/uib-accordion/div/div[5]/div[1]/h4/a/span/i");
        
        // Search the coverage by id
        // NOTE: this one lists only one coverage and has no paging button, so must put it at the last test case
        testCaseName = this.getSectionTestCaseName("search_layer_by_name");
        log.info("Testing search layer by name textbox...");
        this.runTestByAddingTextToTextBox(webDriver, testCaseName, "test_wms_4326", "/html/body/div/div/div/div/div/div[2]/div/ul/div/div/div/div[1]/div/div/div/div[2]/uib-accordion/div/div[1]/div[2]/div/table/thead/tr[2]/td/input");
        
        /* // Click on the search result (only one result) to move to next tab
        testCaseName = this.getSectionTestCaseName("click_on_a_search_result");
        log.info("Testing click on a found result of searching by layer name...");
        this.clickOnElement(webDriver, "/html/body/div/div/div/div/div/div[2]/div/ul/div/div/div/div[1]/div/div/div/div[2]/uib-accordion/div/div[1]/div[2]/div/table/tbody/tr[1]/td[1]/a");
        Thread.sleep(1000);
        this.runTestByNonElementEvent(webDriver, testCaseName);
	// random output, sometime is short page, sometime is long page
	*/
    }
}
