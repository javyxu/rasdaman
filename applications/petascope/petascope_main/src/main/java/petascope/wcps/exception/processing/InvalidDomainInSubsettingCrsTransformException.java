package petascope.wcps.exception.processing;

import petascope.exceptions.WCPSException;
import petascope.exceptions.ExceptionCode;

/**
 * General error for invalid outputCrs projection
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
public class InvalidDomainInSubsettingCrsTransformException  extends WCPSException {

    /**
     * Constructor for the class
     *
     * @param axisName
     * @param subsettingCrs
     * @param errorMessage
     */
    public InvalidDomainInSubsettingCrsTransformException(String axisName, String subsettingCrs, String errorMessage) {
        super(ExceptionCode.WcpsError, ERROR_TEMPLATE.replace("$axisName", axisName)
              .replace("$subsettingCrs", subsettingCrs)
              .replace("$errorMessage", errorMessage));
    }

    private static final String ERROR_TEMPLATE = "Invalid domain on axis '$axisName' to transform with subsettingCrs '$subsettingCrs', '$errorMessage'.";

}