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
 * Copyright 2003 - 2017 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.exceptions;

import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import petascope.util.ExceptionUtil;
import petascope.util.MIMEUtil;

/**
 * A fallback class to handle the unchecked exception if it cannot be handled in PetascopeController which is in most cases.
 @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@ControllerAdvice
public class ExceptionAdvice {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(ExceptionAdvice.class);

    /**
     * To Overwrite the body of the white page with a custom response in String.
     * NOTE: this one is only used as a fallback for rare cases as exceptions are parsed as exception report in PetascopeController.
     * @param ex
     * @param httpServletResponse
     * @throws IOException 
     */
    @ExceptionHandler(Exception.class)    
    @ResponseBody    
    public void generalExceptionHandler(Exception ex, HttpServletResponse httpServletResponse) throws IOException {        
        OutputStream outputStream = httpServletResponse.getOutputStream();

        httpServletResponse.setContentType(MIMEUtil.MIME_XML);
        log.error("Catched an exception: ", ex);

        ExceptionReport exceptionReport = ExceptionUtil.exceptionToReportString(ex);
        httpServletResponse.setStatus(exceptionReport.getHttpCode());
        IOUtils.write(exceptionReport.getExceptionText(), outputStream);
        IOUtils.closeQuietly(outputStream);
    }
}
