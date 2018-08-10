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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import petascope.wcps.exception.processing.InvalidIntervalNumberFormat;
import petascope.wcps.metadata.model.NumericTrimming;
import petascope.wcps.metadata.model.Subset;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.subset_axis.model.WcpsSubsetDimension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import petascope.core.AxisTypes;
import petascope.util.BigDecimalUtil;
import petascope.util.CrsUtil;
import petascope.util.TimeUtil;
import petascope.core.XMLSymbols;
import petascope.wcps.exception.processing.InvalidSlicingException;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.metadata.model.NumericSlicing;
import petascope.wcps.metadata.model.NumericSubset;
import petascope.wcps.metadata.model.RegularAxis;
import petascope.wcps.subset_axis.model.WcpsSliceSubsetDimension;
import petascope.wcps.subset_axis.model.WcpsTrimSubsetDimension;
import petascope.core.service.CrsComputerService;

import static petascope.util.WCPSConstants.MSG_STAR;

/**
 * This class has the purpose of translating subsets coming from the users into
 * numerical subsets usable by wcps.
 *
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Service
public class SubsetParsingService {

    public SubsetParsingService() {

    }

    /**
     * Get a list of subset dimensions which have numeric / timestamp lower and upper bounds.
     * e.g: Lat(0), Lat(0:0), ansi("1950-01-01") but not Lat($p), Lat(p), Lat(avg(c)).
     *
     * @param subsetDimensions
     * @return
     */
    public List<WcpsSubsetDimension> getTerminalSubsetDimensions(List<WcpsSubsetDimension> subsetDimensions) {
        List<WcpsSubsetDimension> terminalSubsetDimensions = new ArrayList<>();

        for (WcpsSubsetDimension subsetDimension : subsetDimensions) {
            if (isSubsetTerminal(subsetDimension)) {
                terminalSubsetDimensions.add(subsetDimension);
            }
        }

        return terminalSubsetDimensions;
    }

    /**
     * Get a list of subset dimensions which are non numeric.
     * e.g: Lat($p), Lat(p), Lat(avg(c)) but not Lat(0), Lat(0:0).
     *
     * @param subsetDimensions
     * @return
     */
    public List<WcpsSubsetDimension> getExpressionSubsetDimensions(List<WcpsSubsetDimension> subsetDimensions) {
        List<WcpsSubsetDimension> nonTerminalSubsetDimensions = new ArrayList<>();

        for (WcpsSubsetDimension subsetDimension : subsetDimensions) {
            if (!isSubsetTerminal(subsetDimension)) {
                nonTerminalSubsetDimensions.add(subsetDimension);
            }
        }

        return nonTerminalSubsetDimensions;
    }

    /**
     * Checks if a subset is has only terminal symbols as bounds (number, timestamp or star).
     *
     * @param
     * @return
     */
    private boolean isSubsetTerminal(WcpsSubsetDimension subset) {
        //temporal
        if (subset.isTemporal()) {
            return true;
        }
        //non-temporal, check bounds
        if (subset instanceof WcpsTrimSubsetDimension) {
            // trim subset dimension
            String lowerBound = ((WcpsTrimSubsetDimension) subset).getLowerBound();
            String upperBound = ((WcpsTrimSubsetDimension) subset).getUpperBound();
            return isBoundTerminal(lowerBound) && isBoundTerminal(upperBound);
        } else {
            // slice subset dimension
            String bound = ((WcpsSliceSubsetDimension) subset).getBound();
            return isBoundTerminal(bound);
        }

    }

    /**
     * Checks if a subset bound is a terminal symbol (number or *).
     *
     * @param bound
     * @return
     */
    private boolean isBoundTerminal(String bound) {
        if (NumberUtils.isNumber(bound) || TimeUtil.isValidTimestamp(bound) || bound.equals(MSG_STAR)) {
            return true;
        }
        return false;
    }

    /**
     * Used in slicing,trimming expression then convert list of subsetDimension
     * to subset
     *
     * @param dimensions
     * @param metadata
     * @param isScaleExtend if subsets are used in scale/extend, we need to
     *                      check it specially
     * @return
     */
    public List<Subset> convertToNumericSubsets(List<WcpsSubsetDimension> dimensions, WcpsCoverageMetadata metadata, boolean isScaleExtend) {
        List<Subset> result = new ArrayList();
        for (WcpsSubsetDimension subsetDimension : dimensions) {
            result.add(this.convertToNumericSubset(subsetDimension, metadata, isScaleExtend));
        }

        return result;
    }

    /**
     * Used in axis iterator to convert list of subsetDimension to subset
     *
     * @param dimensions
     * @return
     */
    public List<Subset> convertToRawNumericSubsets(List<WcpsSubsetDimension> dimensions) {
        List<Subset> result = new ArrayList();
        for (WcpsSubsetDimension subsetDimension : dimensions) {
            result.add(convertToRawNumericSubset(subsetDimension));
        }
        return result;
    }

    /**
     * Used in axis iterator
     *
     * @param dimension
     * @return
     */
    public Subset convertToRawNumericSubset(WcpsSubsetDimension dimension) {
        String axisName = dimension.getAxisName();
        String crs = dimension.getCrs();
        BigDecimal lowerBound = BigDecimal.ZERO;
        BigDecimal upperBound = BigDecimal.ZERO;

        NumericSubset numericSubset = null;
        //try to parse numbers
        try {
            if (dimension instanceof WcpsTrimSubsetDimension) {
                lowerBound = new BigDecimal(((WcpsTrimSubsetDimension) dimension).getLowerBound());
                upperBound = new BigDecimal(((WcpsTrimSubsetDimension) dimension).getUpperBound());

                numericSubset = new NumericTrimming(lowerBound, upperBound);
            } else {
                lowerBound = new BigDecimal(((WcpsSliceSubsetDimension) dimension).getBound());
                numericSubset = new NumericSlicing(lowerBound);
            }
        } catch (NumberFormatException ex) {
            throw new InvalidIntervalNumberFormat(lowerBound.toPlainString(), upperBound.toPlainString(), ex);
        }
        return new Subset(numericSubset, crs, axisName);
    }

    /**
     * Supports * and time in the subset.
     *
     * @param dimension
     * @param metadata
     * @param isScaleExtend if subsetDimension is used to scale or extends will
     *                      need to be checked specially
     * @return
     */
    private Subset convertToNumericSubset(WcpsSubsetDimension dimension, WcpsCoverageMetadata metadata, boolean isScaleExtend) {

        // This needs to be added transform() if dimension has crs which is different with native axis from coverage
        String axisName = dimension.getAxisName();
        String sourceCrs = dimension.getCrs();

        Axis axis = metadata.getAxisByName(axisName);

        // Normally subsettingCrs will be null or empty (e.g: Lat(20:30) not Lat:"http://.../4269(20:30)")
        // then it is nativeCrs of axis
        if (sourceCrs == null || sourceCrs.equals("")) {
            sourceCrs = axis.getNativeCrsUri();
        }

        BigDecimal lowerBound = null;
        BigDecimal upperBound = null;

        NumericSubset numericSubset = null;

        //try to parse numbers
        if (dimension instanceof WcpsTrimSubsetDimension) {
            // convert each slicing point of trimming subset to numeric
            // NOTE: it cannot parse expression in the axis interval (e.g: Lat(1 + 1:2 + avg(c)))
            lowerBound = convertPointToBigDecimal(true, true, axis, ((WcpsTrimSubsetDimension) dimension).getLowerBound());
            upperBound = convertPointToBigDecimal(true, false, axis, ((WcpsTrimSubsetDimension) dimension).getUpperBound());

            numericSubset = new NumericTrimming(lowerBound, upperBound);
        } else {
            // NOTE: it cannot parse expression in the axis interval (e.g: Lat(1 + 1))
            lowerBound = convertPointToBigDecimal(false, true, axis, ((WcpsSliceSubsetDimension) dimension).getBound());
            numericSubset = new NumericSlicing(lowerBound);
        }

        return new Subset(numericSubset, sourceCrs, axisName);
    }

    /**
     * Find the nearest geo coordinate which attach to a grid cell coordinate
     * for the input geo coordinate. e.g: 0 - 30 - 60 (geo coordinates), then
     * input: 42 in geo coordinate will be shifted to 30 in geo coordinate.
     * NOTE: we don't need to fit to sample space if coverage is GridCoverage
     * and axis is CRS:1 OR axis type is not X, Y
     *
     * @param subsets  e.g: c[Lat(0), Long(20:30)]
     * @param metadata
     */
    public void fitToSampleSpaceRegularAxes(List<Subset> subsets, WcpsCoverageMetadata metadata) {
        for (Axis axis : metadata.getAxes()) {
            for (Subset subset : subsets) {
                // Only fit the axis if subset of axis is specified
                if (axis.getLabel().equals(subset.getAxisName())) {
                    String crs = axis.getNativeCrsUri();
                    // Just don't fit to sample space when axis type is not geo-reference (e.g: time coefficient will be wrong value)
                    // NOTE: Not support to fit on irregular axis
                    if (axis instanceof RegularAxis) {
                        if (axis.getAxisType().equals(AxisTypes.X_AXIS) || axis.getAxisType().equals(AxisTypes.Y_AXIS)) {
                            if (!CrsUtil.isGridCrs(crs) && !CrsUtil.isIndexCrs(crs) && !metadata.getCoverageType().equals(XMLSymbols.LABEL_GRID_COVERAGE)) {
                                // Depend on subset on axis to fit correctly
                                if (axis.getGeoBounds() instanceof NumericTrimming) {
                                    this.fitToSampleSpaceTrimming(axis);
                                } else {
                                    this.fitToSampleSpaceSlicing(axis);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Fit the slicing (geo, grid) bound on axis to the origin of correct pixel
     *
     * @param axis
     */
    private void fitToSampleSpaceSlicing(Axis axis) {
        BigDecimal geoBound = ((NumericSlicing) axis.getGeoBounds()).getBound();
        BigDecimal resolution = axis.getResolution();
        BigDecimal geoOrigin = axis.getOrigin().subtract(BigDecimalUtil.divide(resolution, new BigDecimal(2)));

        // grid bound is the floor of ( (geoBound - origin) / resolution )
        // e.g: original geo axis is: 0 --- 30 ---- 60 ---- 90 then slice on 31 will return geoBound: 30
        BigDecimal gridBound = (BigDecimalUtil.divide(geoBound.subtract(geoOrigin), resolution)).setScale(0, BigDecimal.ROUND_FLOOR);
        geoBound = geoOrigin.add(gridBound.multiply(resolution));

        // after fitting, set the correct bounds to axis
        axis.setGeoBounds(new NumericSlicing(geoBound));
        axis.setGridBounds(new NumericSlicing(gridBound));
    }

    /**
     * Fit the trmimming (geo, grid) bound on axis to the origin of correct
     * pixel
     *
     * @param axis
     */
    private void fitToSampleSpaceTrimming(Axis axis) {
        BigDecimal geoLowerBound = ((NumericTrimming) axis.getGeoBounds()).getLowerLimit();
        BigDecimal geoUpperBound = ((NumericTrimming) axis.getGeoBounds()).getUpperLimit();
        BigDecimal gridLowerBound = null;
        BigDecimal gridUpperBound = null;
        
        BigDecimal gridOriginalOrigin = axis.getOriginalGridBounds().getLowerLimit();

        BigDecimal resolution = axis.getResolution();
        BigDecimal geoOriginalOrigin = axis.getOriginalOrigin().subtract(BigDecimalUtil.divide(resolution, new BigDecimal(2)));

        // positive axis (origin is lower than minGeo bound)
        if (resolution.compareTo(BigDecimal.ZERO) > 0) {
            // grid lower bound is the floor of ( (geo lower Bound - origin) / resolution )
            // e.g: original geo axis is: (ORIGIN) 0 --- 30 ---- 60 ---- 90 then lower trim on 31 will return geoBound: 30
            BigDecimal tmpGridLowerBound = BigDecimalUtil.divide(geoLowerBound.subtract(geoOriginalOrigin), resolution);
            tmpGridLowerBound = tmpGridLowerBound.add(gridOriginalOrigin);
            tmpGridLowerBound = CrsComputerService.shiftToNearestGridPointWCPS(tmpGridLowerBound);

            gridLowerBound = tmpGridLowerBound.setScale(0, BigDecimal.ROUND_FLOOR);
            geoLowerBound = geoOriginalOrigin.add((gridOriginalOrigin.abs().add(gridLowerBound)).multiply(resolution));

            // grid upper bound is the ceiling of ( (geo upper Bound - origin) / resolution )
            // e.g: original geo axis is: (ORIGIN) 0--- 30 ---- 60 ---- 90 then upper trim on 31 will return geoBound: 60
            BigDecimal tmpGridUpperBound = BigDecimalUtil.divide(geoUpperBound.subtract(geoOriginalOrigin), resolution);
            tmpGridUpperBound = tmpGridUpperBound.add(gridOriginalOrigin);
            tmpGridUpperBound = CrsComputerService.shiftToNearestGridPointWCPS(tmpGridUpperBound);

            gridUpperBound = tmpGridUpperBound.setScale(0, BigDecimal.ROUND_CEILING).subtract(BigDecimal.ONE);
            geoUpperBound = geoOriginalOrigin.add(((gridOriginalOrigin.abs().add(gridUpperBound)).add(BigDecimal.ONE)).multiply(resolution));
        } else {
            // negative axis (origin is larger than maxGeo bound)

            // grid lower bound is the floor of ( (geo upper Bound - origin) / resolution )
            // e.g: original geo axis is: 0 --- 30 ---- 60 ---- 90 (ORIGIN) then upper trim on 31 will return geoBound: 60
            BigDecimal tmpGridLowerBound = BigDecimalUtil.divide(geoUpperBound.subtract(geoOriginalOrigin), resolution);
            tmpGridLowerBound = tmpGridLowerBound.add(gridOriginalOrigin);
            tmpGridLowerBound = CrsComputerService.shiftToNearestGridPointWCPS(tmpGridLowerBound);

            gridLowerBound = tmpGridLowerBound.setScale(0, BigDecimal.ROUND_FLOOR);
            geoUpperBound = geoOriginalOrigin.add((gridOriginalOrigin.abs().add(gridLowerBound)).multiply(resolution));

            // grid lower bound is the ceiling of ( (geo upper Bound - origin) / resolution )
            // e.g: original geo axis is: 0 --- 30 ---- 60 ---- 90 (ORIGIN) then lower trim on 31 will return geoBound: 30
            BigDecimal tmpGridUpperBound = BigDecimalUtil.divide(geoLowerBound.subtract(geoOriginalOrigin), resolution);
            tmpGridUpperBound = tmpGridUpperBound.add(gridOriginalOrigin);
            tmpGridUpperBound = CrsComputerService.shiftToNearestGridPointWCPS(tmpGridUpperBound);

            gridUpperBound = tmpGridUpperBound.setScale(0, BigDecimal.ROUND_CEILING).subtract(BigDecimal.ONE);
            geoLowerBound = geoOriginalOrigin.add(((gridOriginalOrigin.abs().add(gridUpperBound)).add(BigDecimal.ONE)).multiply(resolution));
        }

        // this happens when trim lower and upper before fitting have same value (e.g: Lat(20:20)),
        // after fitting upper will reduced by 1 resolution (e.g: Lat(20,19)) then need to set it back to same value.        
        if (geoUpperBound.compareTo(geoLowerBound) < 0) {
            geoUpperBound = geoLowerBound;
        }
        if (gridUpperBound.compareTo(gridLowerBound) < 0) {
            gridLowerBound = gridUpperBound;
        }

        // after fitting, set the correct bounds to axis
        axis.setGeoBounds(new NumericTrimming(geoLowerBound, geoUpperBound));
        axis.setGridBounds(new NumericTrimming(gridLowerBound, gridUpperBound));
    }

    /**
     * Try to parse a slicing point (from a slicing subset or low/high of
     * trimming subset) to numeric
     *
     * @param isTrimming    check if subset is trimming
     * @param isLowerPoint  check if point is in lower or upper subset
     * @param axisName      axis name
     * @param point         the value of slicing point (can be numeric, date time or
     *                      string (throw exception if cannot parse))
     * @param isScaleExtend is used to check whether should fit the input
     *                      subsets to coverage bounding box or not (scale / extend intervals can be
     *                      larger than coverage bounding box).
     * @return
     */
    private BigDecimal convertPointToBigDecimal(boolean isTrimming, boolean isLowerPoint, Axis axis, String point) {
        BigDecimal result = null;
        if (point.equals(MSG_STAR)) {
            if (isTrimming) {
                if (isLowerPoint) {
                    result = ((NumericTrimming) axis.getGeoBounds()).getLowerLimit();
                } else {
                    result = ((NumericTrimming) axis.getGeoBounds()).getUpperLimit();
                }
            } else {
                // is slicing, throw exception does not support (Lat(*))
                throw new InvalidSlicingException(axis.getLabel(), point);
            }
        } else if (numericPoint(point)) {
            // Grid Coverage, axis with "CRS:1"
            result = new BigDecimal(point);
        } else if (TimeUtil.isValidTimestamp(point)) {
            // Convert date time to numeric
            if (axis instanceof RegularAxis) {
                // regular axis
                result = TimeConversionService.getTimeInGridPointForRegularAxis(axis, point);
            } else {
                // irregular axis
                result = TimeConversionService.getTimeInGridPointForIrregularAxis(axis, point);
            }
        } else {
            // throw exception when cannot parse a slicing subset point (e.g: Lat(1 + 1) or Lat(a))
            throw new InvalidSlicingException(axis.getLabel(), point);
        }

        return result;
    }

    /**
     * Check a slicing point is numeric
     *
     * @param point
     * @return
     */
    private boolean numericPoint(String point) {
        try {
            BigDecimal bigDecimal = new BigDecimal(point);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }
}
