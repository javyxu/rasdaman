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
package petascope.wcps.metadata.service;

import petascope.util.BigDecimalUtil;
import petascope.wcps.metadata.model.ParsedSubset;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;
import petascope.core.Pair;
import petascope.wcps.metadata.model.IrregularAxis;
import petascope.core.service.CrsComputerService;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.WCPSException;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.metadata.model.RegularAxis;

/**
 * Translate the coordinates from geo bound to grid bound for trimming/slicing and vice versa if using CRS:1 in trimming/slicing
 * i.e: Lat(0:20) ->
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Service
public class CoordinateTranslationService {
    
    /**
     * Translate a geo subset on an axis to grid subset accordingly.
     * e.g: Lat(0:20) -> c[10:15]
     */
    public ParsedSubset<Long> geoToGridSpatialDomain(Axis axis, ParsedSubset<BigDecimal> parsedGeoSubset) throws PetascopeException {
        ParsedSubset<Long> parsedGridSubset;
        if (axis instanceof RegularAxis) {
            parsedGridSubset = this.geoToGridForRegularAxis(parsedGeoSubset, axis.getGeoBounds().getLowerLimit(),
                                                            axis.getGeoBounds().getUpperLimit(), axis.getResolution(), axis.getGridBounds().getLowerLimit());
        } else {
            parsedGridSubset = this.geoToGridForIrregularAxes(parsedGeoSubset, axis.getResolution(), axis.getGridBounds().getLowerLimit(), 
                                                            axis.getGridBounds().getUpperLimit(), axis.getGeoBounds().getLowerLimit(), (IrregularAxis)axis);
        }
        
        return parsedGridSubset;
    }
    
    
    /**
     * Computes the pixel indices for a subset on a regular axis.
     *
     * @param numericSubset:     the geo subset to be converted to pixel indices.
     * @param geoDomainMin:      the geo minimum on the axis.
     * @param geoDomainMax:      the geo maximum on the axis.
     * @param resolution:        the signed cell width (negative if the axis is linear negative)
     * @param gridDomainMin:     the grid coordinate of the first pixel of the axis
     * @return the pair of grid coordinates corresponding to the given geo subset.
     */
    public ParsedSubset<Long> geoToGridForRegularAxis(ParsedSubset<BigDecimal> numericSubset, BigDecimal geoDomainMin,
        BigDecimal geoDomainMax, BigDecimal resolution, BigDecimal gridDomainMin) {
        boolean zeroIsMin = resolution.compareTo(BigDecimal.ZERO) > 0;

        BigDecimal returnLowerLimit, returnUpperLimit;
        if (zeroIsMin) {
            // closed interval on the lower limit, open on the upper limit - use floor and ceil - 1 repsectively
            // e.g: Long(0:20) -> c[0:50]
            BigDecimal lowerLimit = BigDecimalUtil.divide(numericSubset.getLowerLimit().subtract(geoDomainMin), resolution);
            lowerLimit = CrsComputerService.shiftToNearestGridPointWCPS(lowerLimit);
            returnLowerLimit = lowerLimit.setScale(0, RoundingMode.FLOOR).add(gridDomainMin);
            
            BigDecimal upperLimit = BigDecimalUtil.divide(numericSubset.getUpperLimit().subtract(geoDomainMin), resolution);            
            upperLimit = CrsComputerService.shiftToNearestGridPointWCPS(upperLimit);
            returnUpperLimit = upperLimit.setScale(0, RoundingMode.CEILING).subtract(BigDecimal.ONE).add(gridDomainMin);

        } else {
            // Linear negative axis (eg northing of georeferenced images)
            // First coordHi, so that left-hand index is the lower one
            // e.g: axis with 4 pixels in rasdaman, geo limits are 80 and 0, res = -20.
            // ras:    0   1   2   3
            //        --- --- --- ---
            // geo:  80  60  40  20  0
            // user subset 58: count how many resolution-sized interval are between 80 and 58 (1.1), and floor it to get 1
            BigDecimal lowerLimit = BigDecimalUtil.divide(numericSubset.getUpperLimit().subtract(geoDomainMax), resolution);
            lowerLimit = CrsComputerService.shiftToNearestGridPointWCPS(lowerLimit);
            returnLowerLimit = lowerLimit.setScale(0, RoundingMode.FLOOR).add(gridDomainMin);
            
            BigDecimal upperLimit = BigDecimalUtil.divide(numericSubset.getLowerLimit().subtract(geoDomainMax), resolution);
            upperLimit = CrsComputerService.shiftToNearestGridPointWCPS(upperLimit);
            returnUpperLimit = upperLimit.setScale(0, RoundingMode.CEILING).subtract(BigDecimal.ONE).add(gridDomainMin);
        }
        
        //because we use ceil - 1, when values are close (less than 1 resolution dif), the upper will be pushed below the lower            
        if (returnUpperLimit.add(BigDecimal.ONE).equals(returnLowerLimit)) {
            if (returnUpperLimit.compareTo(gridDomainMin) < 0) {
                returnUpperLimit = gridDomainMin;
            }
            returnLowerLimit = returnUpperLimit;
            
        }            
        
        return new ParsedSubset(returnLowerLimit.longValue(), returnUpperLimit.longValue());
    }

    /**
     * Translate the  grid subset with grid CRS (i.e: CRS:1) to geo subset
     * e.g: Long:"CRS:1"(0:50) -> Long(0.5:20.5)
     * NOTE: no rounding for geo bounds as they should be not integer values
     * @param numericSubset
     * @param gridDomainMin
     * @param gridDomainMax
     * @param resolution
     * @param geoDomainMin
     * @return 
     */
    public ParsedSubset<BigDecimal> gridToGeoForRegularAxis(ParsedSubset<BigDecimal> numericSubset, BigDecimal gridDomainMin,
            BigDecimal gridDomainMax, BigDecimal resolution, BigDecimal geoDomainMin) {
        boolean zeroIsMin = resolution.compareTo(BigDecimal.ZERO) > 0;
        BigDecimal returnLowerLimit, returnUpperLimit;
        if (zeroIsMin) {
            // e.g: Long:"CRS:1"(0:50) -> Long(0.5:20.5)
            returnLowerLimit = BigDecimalUtil.multiple(numericSubset.getLowerLimit().subtract(gridDomainMin), resolution)
                               .add(geoDomainMin);
            returnUpperLimit = BigDecimalUtil.multiple(numericSubset.getUpperLimit().subtract(gridDomainMin), resolution)
                               .add(geoDomainMin);

            // because we use ceil - 1, when values are close (less than 1 resolution dif), the upper will be pushed below the lower
            if (returnUpperLimit.compareTo(returnLowerLimit) < 0) {
                returnUpperLimit = returnLowerLimit;
            }
            // NOTE: the if a slice equals the upper bound of a coverage, out[0]=pxHi+1 but still it is a valid subset.
            if ((gridDomainMax.compareTo(gridDomainMin) != 0) && numericSubset.getLowerLimit().equals(numericSubset.getUpperLimit()) && numericSubset.getUpperLimit().equals(gridDomainMax)) {
                returnLowerLimit = returnLowerLimit.subtract(BigDecimal.ONE);
                returnUpperLimit = returnLowerLimit;
            }
        } else {
            // Linear negative axis (eg northing of georeferenced images)
            // First coordHi, so that left-hand index is the lower one
            // e.g: Lat:"CRS:"(0:50) -> Lat(0.23:20.23)
            // (input grid - total pixels) / resolution + geoDomain, NOTE: total pixels + 1 (e.g: 0:710 then max is not: 0 but 711)
            returnLowerLimit = BigDecimalUtil.multiple(numericSubset.getUpperLimit().subtract(gridDomainMax.add(BigDecimal.ONE)), resolution)
                               .add(geoDomainMin);
            returnUpperLimit = BigDecimalUtil.multiple(numericSubset.getLowerLimit().subtract(gridDomainMax.add(BigDecimal.ONE)), resolution)
                               .add(geoDomainMin);

            if (returnUpperLimit.compareTo(returnLowerLimit) < 0) {
                returnUpperLimit = returnLowerLimit;
            }
        }
        return new ParsedSubset(returnLowerLimit, returnUpperLimit);
    }

    /**
     * Returns the translated subset if the coverage has an irregular axis
     * This needs to be further refactored: the correct coefficients must be added in the WcpsCoverageMetadata object when a subset is done
     * on it, and the min and max coefficients should be passed to this method.
     *
     * @param numericSubset    the subset to be translated
     * @param scalarResolution
     * @param gridDomainMin
     * @param gridDomainMax
     * @param geoDomainMin
     * @param irregularAxis
     * @return     
     */
    public ParsedSubset<Long> geoToGridForIrregularAxes(
        ParsedSubset<BigDecimal> numericSubset, BigDecimal scalarResolution, BigDecimal gridDomainMin,
        BigDecimal gridDomainMax, BigDecimal geoDomainMin, IrregularAxis irregularAxis) throws PetascopeException {

        // e.g: t(148654) in irr_cube_2
        BigDecimal lowerCoefficient = ((numericSubset.getLowerLimit()).subtract(geoDomainMin)).divide(scalarResolution);
        BigDecimal upperCoefficient = ((numericSubset.getUpperLimit()).subtract(geoDomainMin)).divide(scalarResolution);
        
        // Return the grid indices of the lower and upper coefficients in an irregular axis
        Pair<Long, Long> indices = irregularAxis.getGridIndices(lowerCoefficient, upperCoefficient);

        return new ParsedSubset(indices.fst, indices.snd);
    }
}
