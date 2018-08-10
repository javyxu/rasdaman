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
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;

/**
 * Class to test wcs_client, tab WCS/ProcessCoverage
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class WCSProcessCoverageTest extends WSAbstractSectionWebPageTest {

    private static final Logger log = Logger.getLogger(WCSProcessCoverageTest.class);

    public WCSProcessCoverageTest() {
        super();
        this.sectionName = "wcs_process_coverage";
    }

    @Override
    public void runTest(WebDriver webDriver) throws InterruptedException, IOException {
        webDriver.navigate().to(this.testURL);
        log.info("*** Testing test cases on Web URL '" + testURL + "', section '" + this.sectionName + "'. ***");
        
        // Switch to iframe to parse the web element
        webDriver.switchTo().frame(0);
        
        String testCaseName;
        Select dropdown;
        
        // First, change to tab ProcessCoverage
        testCaseName = this.getSectionTestCaseName("change_to_process_coverage_tab");        
        log.info("Testing change current tab to ProcessCoverages...");
        this.runTestByClickingOnElement(webDriver, testCaseName, "/html/body/div/div/div/div/div/div[1]/div/ul/div/div/ul/li[4]/a");
        
        String selectDropDownXPath = "/html/body/div/div/div/div/div/div[1]/div/ul/div/div/div/div[4]/div/div/div/div[3]/div/div/select";
        String executeButtonXPath = "/html/body/div/div/div/div/div/div[1]/div/ul/div/div/div/div[4]/div/div/div/div[2]/div[2]/button";
        
        // No encoding
        testCaseName = this.getSectionTestCaseName("no_encoding");
        dropdown = new Select(webDriver.findElement(By.xpath(selectDropDownXPath)));
        log.info("Testing a WCPS query without encoding...");
        dropdown.selectByVisibleText("No encoding");        
        this.runTestByClickingOnElement(webDriver, testCaseName, executeButtonXPath);
        
        // Encode 2D as PNG with widget
        testCaseName = this.getSectionTestCaseName("encode_2d_png_widget");
        dropdown = new Select(webDriver.findElement(By.xpath(selectDropDownXPath)));
        log.info("Testing a WCPS query with encoding as PNG and image widget...");
        dropdown.selectByVisibleText("Encode 2D as png with image widget");        
        this.runTestByClickingOnElement(webDriver, testCaseName, executeButtonXPath);
        
        // Encode 1D as JSON with widget
        testCaseName = this.getSectionTestCaseName("encode_1d_json_widget");
        log.info("Testing a WCPS query with encoding as JSON and diagram widget...");
        dropdown = new Select(webDriver.findElement(By.xpath(selectDropDownXPath)));
        dropdown.selectByVisibleText("Encode 1D as json with diagram widget");        
        this.runTestByClickingOnElement(webDriver, testCaseName, executeButtonXPath);
        
        // Encode 2D as gml
        testCaseName = this.getSectionTestCaseName("encode_2d_gml");
        log.info("Testing a WCPS query with encoding as GML...");
        dropdown = new Select(webDriver.findElement(By.xpath(selectDropDownXPath)));
        dropdown.selectByVisibleText("Encode 2D as gml");                
        this.runTestByClickingOnElement(webDriver, testCaseName, executeButtonXPath);
        
    }
}
