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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.util.CrsUtil;

/**
 * This class holds the GetCoverage response data.
 *
 * @author <a href="mailto:d.misev@jacobs-university.de">Dimitar Misev</a>
 */
public class LegacyGetCoverageMetadata {

    private static final Logger log = LoggerFactory.getLogger(LegacyGetCoverageMetadata.class);

    private LegacyCoverageMetadata metadata;
    private String coverageId;
    private String coverageType;
    private String gridType;
    private String gridId;
    private Integer gridDimension;
    private List<RangeField> rangeFields;
    private String crs;     // dynamically adapt to coverage slicing
    private String outputCrs; // dynamically adapt based on (WCS: outputCrs or WCPS: crsTransform($COVERAGE, {2D geo-referenced CRS}, {})
    private String subsettingCrs; // dynmaically adapt based on (WCS: subsettingCrs or WCPS, e.g: Lat:"http://.../4326(0:20))
    private LegacyBbox bbox;
    // <gml:axisLabels> : grid (rasdaman) order
    private String gridAxisLabels;
    // <gml:GridEnvelope> : grid (rasdaman) order
    private String low;     // Grid lower bound (px)
    private String high;    // Grid upper bound (px)
    // <gml:Envelope> + origin and offset vectors : CRS order
    private String domLow;  // Geodomain lower bound (can differ from input request if not fitting with points sample space)
    private String domHigh; // Geodomain upper bound ('' '' )
    // GeoTiff/JP2000/NetCDF/etc encoding: LONG first = rasdaman order (common GIS practice)
    // http://www.remotesensing.org/geotiff/faq.html?What%20is%20the%20purpose%20of%20GeoTIFF%20format%20for%20satellite%20data#AxisOrder
    private String gisDomLow;  // Domain request lower bound (always easting first)
    private String gisDomHigh; // Domain request upper bound (always easting first)

    /**
     * Map of axis labels and their scale factors.
     * Only axes that are explicitly scaled in a WCS request appear here.
     */
    private Map<String, BigDecimal> axisScaleFactors;

//    public GetCoverageMetadata(GetCoverageRequest request, DbMetadataSource meta) throws SecoreException, WCSException {
//        coverageId = request.getCoverageId();
//        gridAxisLabels = "";
//        low = high = "";
//        domLow = domHigh = "";
//        gisDomLow = gisDomHigh = "";
//        gridType = gridId = "";
//
//        if (!meta.existsCoverageName(coverageId)) {
//            throw new Exception("One of the identifiers passed does not match with any of the coverages offered by this server");
//        }
//        metadata = WcsUtil.getMetadata(meta, coverageId) ;
//
//        coverageType = metadata.getCoverageType();
//
//        // Analyze the grid components and their values
//        Iterator<DomainElement> dit = metadata.getDomainIterator();
//        Iterator<CellDomainElement> cdit = metadata.getCellDomainIterator();
//        while (dit.hasNext() && cdit.hasNext()) {
//            DomainElement dom = dit.next();
//            CellDomainElement cell = cdit.next();
//            gridAxisLabels += dom.getLabel() + " ";
//            low  += cell.getLo() + " ";
//            high += cell.getHi() + " ";
//            gisDomLow  += WcsUtil.fitToSampleSpace(dom.getMinValue(), dom, false, coverageType) + " ";
//            gisDomHigh += WcsUtil.fitToSampleSpace(dom.getMaxValue(), dom, true, coverageType) + " ";
//        }
//
//        // Loop through CRS axes
//        crs = CrsUtil.CrsUri.createCompound(metadata.getCrsUris());
//        try {
//            for (String axisLabel : CrsUtil.getAxesLabels(metadata.getCrsUris())) {
//                DomainElement dom = metadata.getDomainByName(axisLabel);
//                domLow  += WcsUtil.fitToSampleSpace(dom.getMinValue(), dom, false, coverageType) + " ";
//                domHigh += WcsUtil.fitToSampleSpace(dom.getMaxValue(), dom, true, coverageType) + " ";
//            }
//        } catch (Exception pEx) {
//            throw pEx;
//        }
//
//        gridType = coverageType.replace("Coverage", "");
//        if (coverageType.equals(XMLSymbols.LABEL_REFERENCEABLE_GRID_COVERAGE)) {
//            gridType = XMLSymbols.LABEL_RGBV; // No other grid implementations supported currently
//        }
//        gridDimension = metadata.getDimension();
//        gridId = coverageId + "-grid";
//
//        rangeFields = new ArrayList<RangeField>();
//        Iterator<RangeElement> rit = metadata.getRangeIterator();
//        Iterator<AbstractSimpleComponent> sweIt = metadata.getSweComponentsIterator();
//        while (rit.hasNext()) {
//            RangeElement range = rit.next();
//            rangeFields.add(new RangeField(metadata, range, sweIt.next()));
//        }
//        bbox = metadata.getBbox();
//        axisScaleFactors = new HashMap<String, BigDecimal>(gridDimension);
//    }

    public String getGridAxisLabels() {
        return gridAxisLabels.trim();
    }

    public String getCoverageId() {
        return coverageId;
    }

    public String getCoverageType() {
        return coverageType;
    }

    public Integer getGridDimension() {
        return gridDimension;
    }

    public String getGridId() {
        return gridId;
    }

    public String getGridType() {
        return gridType;
    }
    
    
    /**
     * Get an origin of Axis (e.g: lon:positive, lat:negative and time in datetime string format)
     * @param coverageMetadata
     * @param domainElement
     * @return
     * @throws Exception 
     */
    public static String getOriginByAxisName(LegacyCoverageMetadata coverageMetadata,
                                             LegacyDomainElement domainElement) throws Exception {
        String origin = null;
        Boolean positiveDirection = domainElement.isPositiveForwards();
        
        if (domainElement.timeAxis()) {
            origin = LegacyWcsUtil.getReferencedPointValue(domainElement.getMinValue(), domainElement);                        
        } else {
            // Grid origin is in the centre of a sample space (eg pixel centre): get half-pixel value (+ or - signed)
            BigDecimal border = (positiveDirection) ?
                                domainElement.getMinValue() :
                                domainElement.getMaxValue();
            BigDecimal sspaceShift = LegacyWcsUtil.getSampleSpaceShift(domainElement.getDirectionalResolution(), 
                                                                 domainElement.isIrregular(),
                                                                 domainElement.getUom(), 
                                                                 coverageMetadata.getCoverageType());
            // Now apply the shift to the border values in domLow/domHigh
            origin = border.subtract(sspaceShift).toPlainString();
        }

        return origin;            
    }
    /**
     * Returns the origin of the grid coverage.
     * Depending on the direction of an axis, with respect to the associated CRS dimension,
     * the origin can be lower or upper bound of a domain element.
     * For instance, usually rasdaman puts grid origin in the UL corner of an imported geoimage.
     *
     * From GML 3.2.1:
     * When a grid point is used to represent a sample space (e.g. image pixel), the grid point
     * represents the center of the sample space (see ISO 19123:2005, 8.2.2).
     *
     * @return The white-space separated list of coordinates of the grid origin (CRS order!) of the requested [subsetted] coverage.
     * @throws SecoreException
     * @throws WCSException
     */
    public String getGridOrigin(String axisName) throws Exception {
        // The origin of the grid (as is in rasdaman) is not always domLow:
        // the _direction_ of each dimension needs to be taken into account.
        List<String> domLowValues = new ArrayList(Arrays.asList(domLow.trim().split(" ")));
        List<String> domHighValues = new ArrayList(Arrays.asList(domHigh.trim().split(" ")));
        List<String> gridAxisLabelsList = new ArrayList(Arrays.asList(gridAxisLabels.trim().split(" ")));

        // Use TreeMap to store grid origin coordinates and order by CRS axis order (key)
        // No need to use GetCoverage CRS, I can use full set of URIs since order is preserved even if there are slicings.
        Map<Integer, String> gridOrigin = new TreeMap<Integer, String>();
        // local
        BigDecimal border;
        BigDecimal sspaceShift;

        try {
            List<String> crsAxisLabels = CrsUtil.getAxesLabels(CrsUtil.CrsUri.decomposeUri(crs)); // full list of CRS axis label

            // Assumption : grid axis label = CRS axis label (aligned axes)
            int i = 0;
            for (String crsAxisLabel : crsAxisLabels) {
                if (gridAxisLabelsList.contains(crsAxisLabel)) {
                    LegacyDomainElement domEl       = metadata.getDomainByName(crsAxisLabel);
                    Integer gridAxisOrder     = gridAxisLabelsList.indexOf(crsAxisLabel);
                    Boolean positiveDirection = domEl.isPositiveForwards();
                    Integer crsAxisOrder      = CrsUtil.getCrsAxisOrder(metadata.getCrsUris(), crsAxisLabel);
                    BigDecimal origin;
                    log.debug("Grid axis n." + gridAxisOrder + " (" + crsAxisLabel + ") is parallel to CRS axis n." + crsAxisOrder + ".");
                    // NOTE: if axis is time then the origin needs to be converted to ISO date time.
                    if (domEl.timeAxis()) {
                        String timeOrigin = LegacyWcsUtil.getReferencedPointValue(domEl.getMinValue(), domEl);
                        gridOrigin.put(crsAxisOrder, timeOrigin);

                    } else {
                        // Grid origin is in the centre of a sample space (eg pixel centre): get half-pixel value (+ or - signed)
                        border = (positiveDirection) ?
                                BigDecimal.valueOf(Double.parseDouble(domLowValues.get(i))) :
                                BigDecimal.valueOf(Double.parseDouble(domHighValues.get(i)));
                        sspaceShift = LegacyWcsUtil.getSampleSpaceShift(domEl.getDirectionalResolution(), domEl.isIrregular(), domEl.getUom(), metadata.getCoverageType());
                        // Now apply the shift to the border values in domLow/domHigh
                        origin = border.subtract(sspaceShift);
                        if (origin.compareTo(BigDecimal.ZERO) == 0) {
                            // Java bug: http://bugs.java.com/bugdatabase/view_bug.do?bug_id=6480539
                            // 0.0 is not stripped to 0
                            gridOrigin.put(crsAxisOrder, BigDecimal.ZERO.toPlainString());
                        } else {
                            gridOrigin.put(crsAxisOrder, origin.stripTrailingZeros().toPlainString());
                        }
                    }
                    i++;
                } // else: sliced CRS axis: skip
            }
        } catch (Exception ex) {
            log.error("error while calculating GetCoverage grid origin: " + ex.getMessage());
            throw new Exception(ex);
        }

        return LegacyListUtil.printList(new ArrayList<String>(gridOrigin.values()), " ");
    }

    public String getHigh() {
        return high.trim();
    }

    public String getLow() {
        return low.trim();
    }

    public String getDomHigh() {
        return domHigh.trim();
    }

    public String getDomLow() {
        return domLow.trim();
    }

    public String getGisDomHigh() {
        return gisDomHigh.trim();
    }

    public String getGisDomLow() {
        return gisDomLow.trim();
    }

    public List<RangeField> getRangeFields() {
        return rangeFields;
    }

    public LegacyCoverageMetadata getMetadata() {
        return metadata;
    }

    public LegacyBbox getBbox() {
        return bbox;
    }

    public String getCrs() {
        return crs;
    }

    public String getOutputCrs() {
        return outputCrs;
    }

    public String getSubsettingCrs() {
        return subsettingCrs;
    }

    /**
     * Get the scaling factor applied to a grid axis.
     * If the specified axis does not contain scaling info for this axis,
     * a value of 1 (no scaling) is returned.
     * @param axis
     * @return The scaling factor requested by the user on "axis", 1 otherwise.
     */
    public BigDecimal getScalingFactor(String axis) {
        return (null == axisScaleFactors.get(axis)) ? BigDecimal.ONE : axisScaleFactors.get(axis);
    }

    public void setAxisLabels(String axisLabels) {
        this.gridAxisLabels = axisLabels;
        setGridDimension(axisLabels.isEmpty() ? 0 : axisLabels.split(" +").length);
    }

    // Update pixel bounds of the grid (upon trimming and slicing)
    public void setHigh(String high) {
        this.high = high;
    }
    public void setLow(String low) {
        this.low = low;
    }

    // Update bounds of coverage (upon trimming and slicing)
    public void setDomHigh(String high) {
        domHigh = high;
    }
    public void setDomLow(String low) {
        domLow = low;
    }

    // Update bounds of coverage (upon trimming and slicing) forcing easting first (= rasdaman grid order)
    public void setGisDomHigh(String high) {
        gisDomHigh = high;
    }
    public void setGisDomLow(String low) {
        gisDomLow = low;
    }

    public void setGridDimension(Integer gridDimension) {
        this.gridDimension = gridDimension;
    }

    /**
     * Trace the scaling applied along a grid axis.
     * This is necessary for GML handlers to scale grid offset-vectors.
     * @param axis
     * @param scaleFactor
     */
    public void setScalingFactor(String axis, BigDecimal scaleFactor) {
        axisScaleFactors.put(axis, scaleFactor);
    }

    /**
     * Updates the CRS of a coverage.
     * It can change when slices are requested, which cut the CRS space.
     * Put here the trigger to update the coverage type dynamically, since it
     * can also change upon slicings.
     * Currently this trigger is commented out since, while Grid->RectifiedGrid->ReferenceableGrid
     * is conceptually a chain of extension to grid geometries, that is not reflected in
     * the schema definitions where gml:Grid has two separate disjoint extensions.
     * Thus dynamically change from Referenceable to Rectified grids can be an unsafe operation.
     * Disable this now: future investigations are needed.
     * @param newUri
     */
    public void setCrs(String newUri) {
        crs = newUri;
        // Changing CRS means 1+ slices have been requested: trigger an update of coverage type
        //updateCoverageType();
    }

    /**
     * Update the outputCrs of a coverage
     * In can change when user want to transform from nativeCRS (e.g: 4326) to (3857)
     * with WCS (outputCrs="http://...crs/EPSG/0/3857) or WCPS (crsTransform(c, {Lat:".../3857"(1110:11120), Long:".../3857"(1223:32323)
     * @param newUri
     */
    public void setOutputCrs(String newUri) {
        outputCrs = newUri;
    }

    /**
     * Update the subsettingCrs of a coverage
     * with WCS (subsettingCrs="http://..../0/3857") or WCPS (Lat:"http://.../3857"(1230:2330))
     * It will transform the subset(e.g: 1230:2330) with the input CRS (e.g: 3857) without using the native CRS (e.g: 4326)
     * @param newUri
     */
    public void setSubsettingCrs(String newUri) {
        subsettingCrs = newUri;
    }

    /**
     * Method for updating the range type of a coverage.
     * This happens when using methods from the WCS RangeSubsetting extension.
     * @param rangeFieldNames
     * @see "https://portal.opengeospatial.org/files/12-040" "OGC 12-040"
     */
    public void setRangeFields(List<String> rangeFieldNames) {
        rangeFields = new ArrayList<RangeField>();
        Iterator<LegacyRangeElement> rit = metadata.getRangeIterator();
        Iterator<LegacyAbstractSimpleComponent> sweIt = metadata.getSweComponentsIterator();
        while (rit.hasNext()) {
            LegacyRangeElement range = rit.next();
            if (rangeFieldNames.contains(range.getName())) {
                rangeFields.add(new RangeField(metadata, range, sweIt.next()));
            } // otherwise this range element has been sliced out
        }
    }
    /**
     * Check if the *output* coverage is an irregular grid.
     * @return True if at least one of the non-sliced axes are irregular.
     */
    public boolean hasIrregularAxis() {
        List<String> labels = new ArrayList<String>();
        labels.addAll(Arrays.asList(gridAxisLabels.split(" +")));

        for (String axisLabel : labels) {
            LegacyDomainElement domEl = metadata.getDomainByName(axisLabel);
            if (domEl.isIrregular()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Internal trigger to update coverage type.
     * Mainly it recognizes when a referenceable grid is sliced on its 1+
     * irregular axes, so that it becomes a (simpler) rectified grid.
     */
    protected void updateCoverageType() {
        if (LegacyWcsUtil.isGrid(getCoverageType())) {
            if (LegacyWcsUtil.isRectifiedGrid(
                        getCoverageType(),
                        getMetadata().getDomainsByNames(Arrays.asList(getGridAxisLabels().split(" "))))) {
                this.coverageType = LegacyXMLSymbols.LABEL_RECTIFIED_GRID_COVERAGE;
            } else {
                this.coverageType = LegacyXMLSymbols.LABEL_REFERENCEABLE_GRID_COVERAGE;
            }
        }
    }

    public class RangeField {

        private final String fieldName;
        private final LegacyAbstractSimpleComponent sweComponent;

        public RangeField(LegacyCoverageMetadata cov, LegacyRangeElement range, LegacyAbstractSimpleComponent component) {
            fieldName = range.getName();
            sweComponent = component;
        }

        public String getFieldName() {
            String ret = fieldName;
            if (fieldName == null) {
                ret = "field";
            } else if (!fieldName.matches("^[a-zA-Z]+.*")) {
                // field name has to start with a letter
                ret = "field" + fieldName;
            }
            return ret;
        }

        public LegacyAbstractSimpleComponent getSWEComponent() {
            return sweComponent;
        }
    }
}