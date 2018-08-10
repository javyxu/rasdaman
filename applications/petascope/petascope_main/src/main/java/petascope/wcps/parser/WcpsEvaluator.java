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
package petascope.wcps.parser;

import petascope.wcps.metadata.service.RangeFieldService;
import petascope.wcps.metadata.service.CrsUtility;
import petascope.wcps.metadata.service.AxisIteratorAliasRegistry;
import petascope.wcps.handler.SubsetExpressionHandler;
import petascope.wcps.handler.BinaryScalarExpressionHandler;
import petascope.wcps.handler.CoverageVariableNameHandler;
import petascope.wcps.handler.ImageCrsDomainExpressionByDimensionExpressionHandler;
import petascope.wcps.handler.ForClauseHandler;
import petascope.wcps.handler.ComplexNumberConstantHandler;
import petascope.wcps.handler.ImageCrsDomainExpressionHandler;
import petascope.wcps.handler.WcsScaleExpressionByScaleAxesHandler;
import petascope.wcps.handler.ImageCrsExpressionHandler;
import petascope.wcps.handler.WcpsQueryHandler;
import petascope.wcps.handler.UnaryBooleanExpressionHandler;
import petascope.wcps.handler.BooleanNumericalComparisonScalarHandler;
import petascope.wcps.handler.ExtendExpressionHandler;
import petascope.wcps.handler.CoverageConstructorHandler;
import petascope.wcps.handler.CoverageCrsSetHandler;
import petascope.wcps.handler.RangeConstructorHandler;
import petascope.wcps.handler.UnaryPowerExpressionHandler;
import petascope.wcps.handler.RangeSubsettingHandler;
import petascope.wcps.handler.EncodeCoverageHandler;
import petascope.wcps.handler.ReturnClauseHandler;
import petascope.wcps.handler.GeneralCondenserHandler;
import petascope.wcps.handler.WcsScaleExpressionByScaleSizeHandler;
import petascope.wcps.handler.DomainExpressionHandler;
import petascope.wcps.handler.ReduceExpressionHandler;
import petascope.wcps.handler.CrsTransformHandler;
import petascope.wcps.handler.WcsScaleExpressionByFactorHandler;
import petascope.wcps.handler.ParenthesesCoverageExpressionHandler;
import petascope.wcps.handler.ScaleExpressionByDimensionIntervalsHandler;
import petascope.wcps.handler.StringScalarHandler;
import petascope.wcps.handler.UnaryArithmeticExpressionHandler;
import petascope.wcps.handler.SwitchCaseRangeConstructorExpression;
import petascope.wcps.handler.CoverageConstantHandler;
import petascope.wcps.handler.BooleanUnaryScalarExpressionHandler;
import petascope.wcps.handler.NanScalarHandler;
import petascope.wcps.handler.ForClauseListHandler;
import petascope.wcps.handler.CastExpressionHandler;
import petascope.wcps.handler.SwitchCaseScalarValueExpression;
import petascope.wcps.handler.CoverageIdentifierHandler;
import petascope.wcps.handler.ScaleExpressionByImageCrsDomainHandler;
import petascope.wcps.handler.WcsScaleExpressionByScaleExtentHandler;
import petascope.wcps.handler.RealNumberConstantHandler;
import petascope.wcps.handler.WhereClauseHandler;
import petascope.wcps.handler.BooleanConstantHandler;
import petascope.wcps.handler.BooleanSwitchCaseCoverageExpressionHandler;
import petascope.wcps.handler.RangeConstructorSwitchCaseHandler;
import petascope.wcps.handler.BinaryCoverageExpressionHandler;
import petascope.wcps.handler.ExtendExpressionByImageCrsDomainHandler;
import petascope.wcps.exception.processing.InvalidSubsettingException;
import petascope.wcps.exception.processing.DuplcateRangeNameException;
import petascope.wcps.exception.processing.CoverageMetadataException;
import petascope.wcps.exception.processing.InvalidAxisNameException;
import petascope.wcps.subset_axis.model.WcpsSubsetDimension;
import petascope.wcps.subset_axis.model.DimensionIntervalList;
import petascope.wcps.subset_axis.model.WcpsTrimSubsetDimension;
import petascope.wcps.subset_axis.model.WcpsSliceSubsetDimension;
import petascope.wcps.subset_axis.model.IntervalExpression;
import petascope.wcps.subset_axis.model.AxisIterator;
import petascope.wcps.subset_axis.model.AxisSpec;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.math.BigDecimal;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.StringUtils;
import petascope.wcps.metadata.model.ParsedSubset;
import petascope.wcps.result.VisitorResult;

import static petascope.wcs2.parsers.subsets.SlicingSubsetDimension.ASTERISK;

import java.util.*;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCPSException;
import petascope.util.CrsUtil;
import petascope.util.ListUtil;
import petascope.util.StringUtil;
import petascope.wcps.exception.processing.Coverage0DMetadataNullException;
import petascope.wcps.exception.processing.InvalidWKTClippingException;
import petascope.wcps.handler.ClipExpressionHandler;
import petascope.wcps.handler.CoverageIsNullHandler;
import petascope.wcps.metadata.model.RangeField;
import petascope.wcps.result.WcpsMetadataResult;
import petascope.wcps.result.WcpsResult;
import petascope.wcps.subset_axis.model.AbstractWKTShape;
import petascope.wcps.subset_axis.model.WcpsScaleDimensionIntevalList;
import petascope.wcps.subset_axis.model.AbstractWcpsScaleDimension;
import petascope.wcps.subset_axis.model.WKTCompoundPoint;
import petascope.wcps.subset_axis.model.WKTCompoundPoints;
import petascope.wcps.subset_axis.model.WKTLineString;
import petascope.wcps.subset_axis.model.WKTMultipolygon;
import petascope.wcps.subset_axis.model.WKTPolygon;
import petascope.wcps.subset_axis.model.WcpsSliceScaleDimension;
import petascope.wcps.subset_axis.model.WcpsTrimScaleDimension;

/**
 * Class that implements the parsing rules described in wcps.g4
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
@Service
public class WcpsEvaluator extends wcpsBaseVisitor<VisitorResult> {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(WcpsEvaluator.class);
    @Autowired private
    AxisIteratorAliasRegistry axisIteratorAliasRegistry;
    
    // Class handlers
    @Autowired private
    WcpsQueryHandler wcpsQueryHandler;
    @Autowired private
    ForClauseHandler forClauseHandler;
    @Autowired private
    ForClauseListHandler forClauseListHandler;
    @Autowired private
    ReturnClauseHandler returnClauseHandler;
    @Autowired private
    EncodeCoverageHandler encodeCoverageHandler;
    
    @Autowired private
    ClipExpressionHandler clipExpressionHandler;
    @Autowired private
    CrsTransformHandler crsTransformHandler;
    @Autowired private
    CoverageVariableNameHandler coverageVariableNameHandler;
    @Autowired private
    BinaryCoverageExpressionHandler binaryCoverageExpressionHandler;
    @Autowired private
    BooleanConstantHandler booleanConstantHandler;
    @Autowired private
    BinaryScalarExpressionHandler binaryScalarExpressionHandler;
    @Autowired private
    CoverageConstructorHandler coverageConstructorHandler;
    @Autowired private
    UnaryArithmeticExpressionHandler unaryArithmeticExpressionHandler;
    @Autowired private
    UnaryPowerExpressionHandler unaryPowerExpressionHandler;
    @Autowired private
    UnaryBooleanExpressionHandler unaryBooleanExpressionHandler;
    
    @Autowired private
    CastExpressionHandler castExpressionHandler;
    @Autowired private
    RangeSubsettingHandler rangeSubsettingHandler;
    @Autowired private
    RealNumberConstantHandler realNumberConstantHandler;
    @Autowired private
    NanScalarHandler nanScalarHandler;
    @Autowired private
    ParenthesesCoverageExpressionHandler parenthesesCoverageExpressionHandler;
    
    @Autowired private
    WhereClauseHandler whereClauseHandler;
    @Autowired private
    BooleanUnaryScalarExpressionHandler booleanUnaryScalarExpressionHandler;
    @Autowired private
    BooleanNumericalComparisonScalarHandler booleanNumericalComparisonScalarHandler;
    @Autowired private
    ReduceExpressionHandler reduceExpressionHandler;
    @Autowired private
    BooleanSwitchCaseCoverageExpressionHandler booleanSwitchCaseCoverageExpressionHandler;
    @Autowired private
    ComplexNumberConstantHandler complexNumberConstantHandler;
    
    @Autowired private
    GeneralCondenserHandler generalCondenserHandler;    
    @Autowired private
    SubsetExpressionHandler subsetExpressionHandler;
    @Autowired private
    CoverageConstantHandler coverageConstantHandler;
    @Autowired private
    ExtendExpressionHandler extendExpressionHandler;
    @Autowired private
    ExtendExpressionByImageCrsDomainHandler extendExpressionByDomainIntervalsHandler;
    @Autowired private
    RangeConstructorHandler rangeConstructorHandler;
    @Autowired private
    RangeConstructorSwitchCaseHandler rangeConstructorSwitchCaseHandler;
    @Autowired private
    StringScalarHandler stringScalarHandler;
    @Autowired private
    CoverageIdentifierHandler coverageIdentifierHandler;
    
    @Autowired private
    CoverageCrsSetHandler coverageCrsSetHandler;
    // Scale Extension
    // WCPS standard
    @Autowired private
    ScaleExpressionByDimensionIntervalsHandler scaleExpressionByDimensionIntervalsHandler;
    @Autowired private
    ScaleExpressionByImageCrsDomainHandler scaleExpressionByImageCrsDomainHandler;
    // Made up to handle WCS -> WCPS scale
    @Autowired private
    WcsScaleExpressionByFactorHandler scaleExpressionByFactorHandler;
    @Autowired private
    WcsScaleExpressionByScaleAxesHandler scaleExpressionByScaleAxesHandler;
    @Autowired private
    WcsScaleExpressionByScaleSizeHandler scaleExpressionByScaleSizeHandler;
    @Autowired private
    WcsScaleExpressionByScaleExtentHandler scaleExpressionByScaleExtentHandler;
    @Autowired private
    ImageCrsExpressionHandler imageCrsExpressionHandler;
    @Autowired private
    ImageCrsDomainExpressionHandler imageCrsDomainExpressionHandler;
    @Autowired private
    ImageCrsDomainExpressionByDimensionExpressionHandler imageCrsDomainExpressionByDimensionExpressionHandler;
    @Autowired private
    DomainExpressionHandler domainExpressionHandler;
    @Autowired private
    SwitchCaseRangeConstructorExpression switchCaseRangeConstructorExpression;
    @Autowired private
    SwitchCaseScalarValueExpression switchCaseScalarValueExpression;
    @Autowired private
    CoverageIsNullHandler coverageIsNullHandler;
    
    /**
     * Class constructor. This object is created for each incoming Wcps query.
     */
    public WcpsEvaluator() {
        super();        
    }

    // VISITOR HANDLERS
    /* --------------------- Visit each nodes then parse ------------------ */
    @Override
    public VisitorResult visitWcpsQueryLabel(@NotNull wcpsParser.WcpsQueryLabelContext ctx) { 
        
        WcpsResult forClauseList = (WcpsResult) visit(ctx.forClauseList());
        //only visit the where clause if it exists
        WcpsResult whereClause = null;
        if (ctx.whereClause() != null) {
            whereClause = (WcpsResult) visit(ctx.whereClause());
        }
        VisitorResult returnClause = visit(ctx.returnClause());
        VisitorResult result = null;
        if (returnClause instanceof WcpsResult) {
            result = wcpsQueryHandler.handle(forClauseList, whereClause, (WcpsResult) returnClause);
        } else {
            result = (WcpsMetadataResult) returnClause;
        }        

        return result;
    }

    @Override
    public VisitorResult visitForClauseLabel(@NotNull wcpsParser.ForClauseLabelContext ctx) {
        List<TerminalNode> coverageNames = ctx.COVERAGE_VARIABLE_NAME();
        List<String> coverageNamesStr = new ArrayList<>();

        for (int i = 0; i < coverageNames.size(); i++) {
            coverageNamesStr.add(coverageNames.get(i).getText());
        }

        WcpsResult result = forClauseHandler.handle(ctx.coverageVariableName().getText(), coverageNamesStr);
        return result;
    }

    @Override
    public VisitorResult visitForClauseListLabel(@NotNull wcpsParser.ForClauseListLabelContext ctx) {
        List<WcpsResult> forClauses = new ArrayList();
        for (wcpsParser.ForClauseContext currentClause : ctx.forClause()) {
            forClauses.add((WcpsResult) visit(currentClause));
        }
        
        WcpsResult result = forClauseListHandler.handle(forClauses);
        return result;
    }

    @Override
    public VisitorResult visitReturnClauseLabel(@NotNull wcpsParser.ReturnClauseLabelContext ctx) {
        VisitorResult processingExpr = visit(ctx.processingExpression());
        VisitorResult result = null;
        if (processingExpr instanceof WcpsResult) {
            result = returnClauseHandler.handle((WcpsResult) processingExpr);
        } else if (processingExpr instanceof WcpsMetadataResult) {
            //if metadata just pass it up
            result = (WcpsMetadataResult) processingExpr;
        }

        return result;
    }

    @Override
    public VisitorResult visitEncodedCoverageExpressionLabel(@NotNull wcpsParser.EncodedCoverageExpressionLabelContext ctx) {
        WcpsResult coverageExpression = (WcpsResult) visit(ctx.coverageExpression());
        // e.g: tiff
        String formatType = StringUtil.stripQuotes(ctx.STRING_LITERAL().getText());
        // NOTE: extraParam here can be:
        // + Old style: e.g: "nodata=0"
        // + JSON style: e.g: "{\"nodata\": [0]}" -> {"nodata": [0]}
        String extraParams = "";
        if (ctx.extra_params() != null) {
            extraParams = StringUtil.stripQuotes(ctx.extra_params().getText()).replace("\\", "");
        }

        WcpsResult result = null;
        try {
            result = encodeCoverageHandler.handle(coverageExpression, formatType, extraParams);
        } catch (PetascopeException | JsonProcessingException ex) {
            String errorMessage = "Error processing encode() operator expression. Reason: " + ex.getMessage() + ".";
            throw new WCPSException(errorMessage, ex);
        }
        
        // Cannot convert object to JSON
        return result;
    }
    
    @Override
    public VisitorResult visitWktPointsLabel(@NotNull wcpsParser.WktPointsLabelContext ctx) {
        // Handle wktPoints (coordinates inside WKT): constant (constant)*) (COMMA constant (constant)*)*
        // e.g: 20 30 "2017-01-01T02:35:50", 30 40 "2017-01-05T02:35:50", 50 60 "2017-01-07T02:35:50" 
        int numberOfCompoundPoints = ctx.constant().size();
        int numberOfDimensions = numberOfCompoundPoints / (ctx.COMMA().size() + 1);
        int count = 0;
        List<String> listTmp = new ArrayList<>();
        String point = "";
        // NOTE: there is a huge bottle neck if using normal for loop with counter i in ANTLR4
        // (e.g: with 100 000 elements, it can take minutes to just iterate) with foreach it takes ms.
        for (wcpsParser.ConstantContext constant : ctx.constant()) {
            String pointTmp = constant.getText();
            point = point + " " + pointTmp;
            if (count < numberOfDimensions - 1) {                
                count++;
            } else {
                listTmp.add(point.trim());
                point = "";
                count = 0;
            }            
        }   
        
        WKTCompoundPoint wktPoint = new WKTCompoundPoint(ListUtil.join(listTmp, ","), numberOfDimensions);
        return wktPoint;
    }
    
    @Override
    public VisitorResult visitWKTPointElementListLabel(@NotNull wcpsParser.WKTPointElementListLabelContext ctx) {
        // Handle LEFT_PARENTHESIS wktPoints RIGHT_PARENTHESIS (COMMA LEFT_PARENTHESIS wktPoints RIGHT_PARENTHESIS)*
        // e.g: (20 30, 40 50), (40 60, 70 80) are considered as 2 WKTCompoundPoints (1 is 20 30 40 50, 2 is 40 60 70 80)
        List<WKTCompoundPoint> points = new ArrayList<>();
        for (wcpsParser.WktPointsContext elem : ctx.wktPoints()) {            
            points.add((WKTCompoundPoint)visit(elem));
        }
        
        WKTCompoundPoints wktCompoundPointList = new WKTCompoundPoints(points);
        return wktCompoundPointList;
    }
    
    @Override
    public VisitorResult visitWKTLineStringLabel(@NotNull wcpsParser.WKTLineStringLabelContext ctx) {
        // Handle LINESTRING wktPointElementList
        // e.g: LineString(20 30, 40 50)
        WKTCompoundPoints wktCompoundPoints = (WKTCompoundPoints) visit(ctx.wktPointElementList());         
        List<WKTCompoundPoints> wktCompoundPointsList = new ArrayList<>();
        wktCompoundPointsList.add(wktCompoundPoints);
        
        WKTLineString wktLineString = new WKTLineString(wktCompoundPointsList);        
        return wktLineString;
    }
    
    @Override
    public VisitorResult visitWKTPolygonLabel(@NotNull wcpsParser.WKTPolygonLabelContext ctx) {
        // Handle POLYGON LEFT_PARENTHESIS wktPointElementList RIGHT_PARENTHESIS
        // e.g: POLYGON((20 30, 40 50), (60 70, 70 80))
        WKTCompoundPoints wktCompoundPoints = (WKTCompoundPoints) visit(ctx.wktPointElementList());        
        List<WKTCompoundPoints> wktCompoundPointsList = new ArrayList<>();
        wktCompoundPointsList.add(wktCompoundPoints);
        
        WKTPolygon wktPolygon = new WKTPolygon(wktCompoundPointsList);        
        return wktPolygon;
    }
    
    @Override
    public VisitorResult visitWKTMultipolygonLabel(@NotNull wcpsParser.WKTMultipolygonLabelContext ctx) {
        // Handle MULTIPOLYGON LEFT_PARENTHESIS 
        //                         LEFT_PARENTHESIS wktPointElementList RIGHT_PARENTHESIS
        //                         (COMMA LEFT_PARENTHESIS wktPointElementList RIGHT_PARENTHESIS)* 
        //                     RIGHT_PARENTHESIS
        // e.g: Multipolygon( ((20 30, 40 50, 60 70)), ((20 30, 40 50), (60 70, 80 90)) )
        List<WKTCompoundPoints> wktPolygonCompoundPointList = new ArrayList();
        for (wcpsParser.WktPointElementListContext elem : ctx.wktPointElementList()) {            
            wktPolygonCompoundPointList.add((WKTCompoundPoints)visit(elem));
        }
        
        WKTMultipolygon wktMultipolygon = new WKTMultipolygon(wktPolygonCompoundPointList);        
        return wktMultipolygon;
    }
    
    
    @Override
    public VisitorResult visitClipExpressionLabel(@NotNull wcpsParser.ClipExpressionLabelContext ctx) { 
        // Handle clipExpression: CLIP LEFT_PARENTHESIS coverageExpression COMMA (wktPolygon | wktLineString) (COMMA crsName)? RIGHT_PARENTHESIS
        // e.g: clip(c[i(0:20), j(0:20)], Polygon((0 10, 20 20, 20 10, 0 10)), "http://opengis.net/def/CRS/EPSG/3857")
        WcpsResult coverageExpression = (WcpsResult) visit(ctx.coverageExpression());
        AbstractWKTShape wktShape;
        if (ctx.wktPolygon() != null) {
            wktShape = (WKTPolygon) visit(ctx.wktPolygon());
        } else if (ctx.wktLineString() != null) {
            wktShape = (WKTLineString) visit(ctx.wktLineString());
        } else if (ctx.wktMultipolygon() != null) {
            wktShape = (WKTMultipolygon) visit(ctx.wktMultipolygon());
        } else {
            throw new InvalidWKTClippingException("Input WKT for clip() operator is not supported."); 
        }
        
        int numberOfDimensions = wktShape.getWktCompoundPointsList().get(0).getNumberOfDimensions();
        if (coverageExpression.getMetadata() == null) {
            throw new Coverage0DMetadataNullException(ClipExpressionHandler.OPERATOR);
        }
        int coverageDimensions = coverageExpression.getMetadata().getAxes().size();
        if (numberOfDimensions != coverageDimensions) {
            throw new InvalidWKTClippingException("Number of dimensions in WKT '" + numberOfDimensions + "' is different from coverage's '" + coverageDimensions + "'.");
        }
        // NOTE: This one is optional parameter, if specified, XY coordinates in WKT will be translated from this CRS to coverage's native CRS for XY axes.
        String wktCRS = null;
        if (ctx.crsName() != null) {
            wktCRS = ctx.crsName().getText().replace("\"", "");
        }        
        WcpsResult result = null;
        try {
            result = clipExpressionHandler.handle(coverageExpression, wktShape, wktCRS);
        } catch (PetascopeException ex) {
            String errorMessage = "Error processing clip() operator expression. Reason: '" + ex.getExceptionText() + "'.";
            throw new WCPSException(ex.getExceptionCode(), errorMessage, ex);
        }
        
        return result;
    }

    @Override
    public WcpsResult visitCrsTransformExpressionLabel(@NotNull wcpsParser.CrsTransformExpressionLabelContext ctx) {
        // Handle crsTransform($COVERAGE_EXPRESSION, {$DOMAIN_CRS_2D}, {$INTERPOLATION})
        // e.g: crsTransform(c, {Lat:"www.opengis.net/def/crs/EPSG/0/4327", Long:"www.opengis.net/def/crs/EPSG/0/4327"}, {}
        WcpsResult coverageExpression = (WcpsResult) visit(ctx.coverageExpression());
        HashMap<String, String> axisCrss = new LinkedHashMap<>();

        // { Axis_CRS_1 , Axis_CRS_2 } (e.g: Lat:"http://localhost:8080/def/crs/EPSG/0/4326")
        wcpsParser.DimensionCrsElementLabelContext crsX = (wcpsParser.DimensionCrsElementLabelContext) ctx.dimensionCrsList().getChild(1);
        axisCrss.put(crsX.axisName().getText(), crsX.crsName().getText().replace("\"", ""));

        wcpsParser.DimensionCrsElementLabelContext crsY = (wcpsParser.DimensionCrsElementLabelContext) ctx.dimensionCrsList().getChild(3);
        axisCrss.put(crsY.axisName().getText(), crsY.crsName().getText().replace("\"", ""));

        // Store the interpolation objects (rangeName, method -> nodata values)
        HashMap<String, HashMap<String, String>> rangeInterpolations = new LinkedHashMap<>();

        // get interpolation parameters
        if (ctx.fieldInterpolationList().fieldInterpolationListElement().size() > 0) {
            // Iterate the interpolation list to get the range (band name) and its parameters (if it is available)
            for (wcpsParser.FieldInterpolationListElementContext element : ctx.fieldInterpolationList().fieldInterpolationListElement()) {

                // e.g: b1(A, B)
                String rangeName = element.getChild(0).getText();
                wcpsParser.InterpolationMethodContext intMethodObj = (wcpsParser.InterpolationMethodContext) element.getChild(2);
                // e.g: A = "near"
                String interpolationMethod = intMethodObj.getChild(0).getText().replace("\"", "");
                // e.g: B = "1,2,3"
                String nullValues = intMethodObj.getChild(2).getText().replace("\"", "");

                // e.g: "near" -> "1,2,3"
                HashMap<String, String> map = new LinkedHashMap<>();
                map.put(interpolationMethod, nullValues);

                rangeInterpolations.put(rangeName, map);
            }
        }

        
        WcpsResult result = null;
        try {
            result = crsTransformHandler.handle(coverageExpression, axisCrss, rangeInterpolations);
        } catch (PetascopeException | SecoreException ex) {
            String errorMessage = "Error processing crsTransform() operator expression. Reason: " + ex.getMessage();
            throw new WCPSException(errorMessage, ex);
        }
        return result;

    }

    @Override
    public VisitorResult visitCoverageVariableNameLabel(@NotNull wcpsParser.CoverageVariableNameLabelContext ctx) {
        // Identifier, e.g: $c or c
        // NOTE: axisIterator and coverage variable name can be the same syntax (e.g: $c, $px)
        String coverageVariable = ctx.COVERAGE_VARIABLE_NAME().getText();
        WcpsResult result = null;
        
        try {
            result = coverageVariableNameHandler.handle(coverageVariable);
        } catch (PetascopeException | SecoreException ex) {
            throw new CoverageMetadataException(ex);
        }
        
        return result;
    }

    @Override
    public VisitorResult visitCoverageExpressionLogicLabel(@NotNull wcpsParser.CoverageExpressionLogicLabelContext ctx) {
        // coverageExpression booleanOperator coverageExpression  (e.g: (c + 1) and (c + 1))
        WcpsResult leftCoverageExpr = (WcpsResult) visit(ctx.coverageExpression(0));
        String operand = ctx.booleanOperator().getText();
        WcpsResult rightCoverageExpr = (WcpsResult) visit(ctx.coverageExpression(1));

        WcpsResult result = binaryCoverageExpressionHandler.handle(leftCoverageExpr, operand, rightCoverageExpr);
        return result;
    }

    @Override
    public VisitorResult visitCoverageExpressionArithmeticLabel(@NotNull wcpsParser.CoverageExpressionArithmeticLabelContext ctx) {
        // coverageExpression (+, -, *, /) coverageExpression (e.g: c + 5 or 2 - 3) as BinarycoverageExpression
        WcpsResult leftCoverageExpression = (WcpsResult) visit(ctx.coverageExpression(0));
        String operand = ctx.coverageArithmeticOperator().getText();
        WcpsResult rightCoverageExpression = (WcpsResult) visit(ctx.coverageExpression(1));

        WcpsResult result = binaryCoverageExpressionHandler.handle(leftCoverageExpression, operand,
                                                                   rightCoverageExpression);
        return result;
    }

    @Override
    public VisitorResult visitCoverageExpressionOverlayLabel(@NotNull wcpsParser.CoverageExpressionOverlayLabelContext ctx) {
        // coverageExpression OVERLAY coverageExpression (e.g: c overlay d)
        // invert the order of the operators since WCPS overlay order is the opposite of the one in rasql
        WcpsResult leftCoverageExpression = (WcpsResult) visit(ctx.coverageExpression(1));
        String overlay = ctx.OVERLAY().getText();
        WcpsResult rightCoverageExpression = (WcpsResult) visit(ctx.coverageExpression(0));

        WcpsResult result = binaryCoverageExpressionHandler.handle(leftCoverageExpression, overlay, rightCoverageExpression);
        return result;
    }

    @Override
    public VisitorResult visitBooleanConstant(@NotNull wcpsParser.BooleanConstantContext ctx) {
        // TRUE | FALSE (e.g: true or false)
        
        WcpsResult result = booleanConstantHandler.handle(ctx.getText());
        return result;
    }

    @Override
    public VisitorResult visitBooleanStringComparisonScalar(@NotNull wcpsParser.BooleanStringComparisonScalarContext ctx) {
        // stringScalarExpression stringOperator stringScalarExpression  (e.g: c = d) or (c != 2))
        String leftScalarStr = ctx.stringScalarExpression(0).getText();
        String operand = ctx.stringOperator().getText();
        String rightScalarStr = ctx.stringScalarExpression(1).getText();

        WcpsResult result = binaryScalarExpressionHandler.handle(leftScalarStr, operand, rightScalarStr);
        return result;
    }

    @Override
    public VisitorResult visitCoverageConstructorExpressionLabel(@NotNull wcpsParser.CoverageConstructorExpressionLabelContext ctx) {
        // COVERAGE IDENTIFIER  OVER axisIterator (COMMA axisIterator)* VALUES coverageExpression
        // e.g: coverage cov over $px x(0:20), $px y(0:20) values avg(c)
        ArrayList<AxisIterator> axisIterators = new ArrayList<>();

        String coverageName = ctx.COVERAGE_VARIABLE_NAME().getText();

        String rasqlAliasName = "";
        String aliasName = "";
        int count = 0;

        // to build the IndexCRS for axis iterator
        int numberOfAxis = axisIterators.size();
        String crsUri = CrsUtil.OPENGIS_INDEX_ND_PATTERN.replace("%d", String.valueOf(numberOfAxis));

        for (wcpsParser.AxisIteratorContext i : ctx.axisIterator()) {
            AxisIterator axisIterator = (AxisIterator) visit(i);
            aliasName = axisIterator.getAliasName();
            if (rasqlAliasName.isEmpty()) {
                rasqlAliasName = aliasName.replace(WcpsSubsetDimension.AXIS_ITERATOR_DOLLAR_SIGN, "");
            }
            axisIterator.getSubsetDimension().setCrs(crsUri);
            axisIterator.setRasqlAliasName(rasqlAliasName);
            axisIterator.setAxisIteratorOrder(count);

            // Add the axis iterator to the axis iterators alias registry
            axisIteratorAliasRegistry.addAxisIteratorAliasMapping(aliasName, axisIterator);
            axisIterators.add(axisIterator);
            // the order of axis iterator in the coverage
            count++;
        }

        WcpsResult valuesExpr = (WcpsResult) visit(ctx.coverageExpression());

        WcpsResult wcpsResult = coverageConstructorHandler.handle(coverageName, axisIterators, valuesExpr);
        return wcpsResult;
    }

    @Override
    public VisitorResult visitUnaryCoverageArithmeticExpressionLabel(@NotNull wcpsParser.UnaryCoverageArithmeticExpressionLabelContext ctx) {
        // unaryArithmeticExpressionOperator  LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS
        // e.g: sqrt(c)
        String operator = ctx.unaryArithmeticExpressionOperator().getText();
        WcpsResult coverageExpr = (WcpsResult) visit(ctx.coverageExpression());

        WcpsResult result = unaryArithmeticExpressionHandler.handle(operator, coverageExpr);
        return result;
    }

    @Override
    public VisitorResult visitTrigonometricExpressionLabel(@NotNull wcpsParser.TrigonometricExpressionLabelContext ctx) {
        // trigonometricOperator LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS
        // e.g: sin(c), cos(c), tan(c)
        String operator = ctx.trigonometricOperator().getText();
        WcpsResult coverageExpr = (WcpsResult) visit(ctx.coverageExpression());

        WcpsResult result = unaryArithmeticExpressionHandler.handle(operator, coverageExpr);
        return result;
    }

    @Override
    public VisitorResult visitExponentialExpressionLabel(@NotNull wcpsParser.ExponentialExpressionLabelContext ctx) {
        // exponentialExpressionOperator LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS
        // e.g: exp(c), log(c), ln(c)
        String operand = ctx.exponentialExpressionOperator().getText();
        WcpsResult coverageExpr = (WcpsResult) visit(ctx.coverageExpression());

        WcpsResult result = unaryArithmeticExpressionHandler.handle(operand, coverageExpr);
        return result;
    }

    @Override
    public VisitorResult visitUnaryPowerExpressionLabel(@NotNull wcpsParser.UnaryPowerExpressionLabelContext ctx) {
        // POWER LEFT_PARENTHESIS coverageExpression COMMA numericalScalarExpression RIGHT_PARENTHESIS
        // e.g: pow(c, -0.5) or pow(c, avg(c))
        WcpsResult coverageExpr = (WcpsResult) visit(ctx.coverageExpression());
        WcpsResult scalarExpr = (WcpsResult) visit(ctx.numericalScalarExpression());

        WcpsResult result = unaryPowerExpressionHandler.handle(coverageExpr, scalarExpr);
        return result;
    }

    @Override
    public VisitorResult visitNotUnaryBooleanExpressionLabel(@NotNull wcpsParser.NotUnaryBooleanExpressionLabelContext ctx) {
        // NOT LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS
        // e.g: not(c)
        WcpsResult coverageExpr = (WcpsResult) visit(ctx.coverageExpression());

        WcpsResult result = unaryBooleanExpressionHandler.handle(coverageExpr, null);
        return result;
    }

    @Override
    public VisitorResult visitBitUnaryBooleanExpressionLabel(@NotNull wcpsParser.BitUnaryBooleanExpressionLabelContext ctx) {
        // BIT LEFT_PARENTHESIS coverageExpression COMMA numericalScalarExpression RIGHT_PARENTHESIS
        // e.g: big(c, 2)
        WcpsResult coverageExpr = (WcpsResult) visit(ctx.coverageExpression());
        WcpsResult scalarExpr = (WcpsResult) visit(ctx.numericalScalarExpression());

        WcpsResult result = unaryBooleanExpressionHandler.handle(coverageExpr, scalarExpr);
        return result;
    }

    @Override
    public VisitorResult visitCastExpressionLabel(@NotNull wcpsParser.CastExpressionLabelContext ctx) {
        // LEFT_PARENTHESIS rangeType RIGHT_PARENTHESIS coverageExpression
        // e.g: (char)(c + 5)
        String castType = StringUtils.join(ctx.rangeType().COVERAGE_VARIABLE_NAME(), " ");
        WcpsResult coverageExpr = (WcpsResult) visit(ctx.coverageExpression());

        WcpsResult result = castExpressionHandler.handle(castType, coverageExpr);
        return result;
    }

    @Override
    public VisitorResult visitCoverageExpressionRangeSubsettingLabel(@NotNull wcpsParser.CoverageExpressionRangeSubsettingLabelContext ctx) {
        // coverageExpression DOT fieldName  (e.g: c.red or (c + 1).red)
        WcpsResult coverageExpression = (WcpsResult) visit(ctx.coverageExpression());
        String fieldName = ctx.fieldName().getText();

        WcpsResult result = rangeSubsettingHandler.handle(fieldName, coverageExpression);
        return result;
    }

    @Override
    public VisitorResult visitNumericalRealNumberExpressionLabel(@NotNull wcpsParser.NumericalRealNumberExpressionLabelContext ctx) {
        // REAL_NUMBER_CONSTANT
        // e.g: 2, 3
        WcpsResult result = realNumberConstantHandler.handle(ctx.getText());
        return result;
    }

    @Override
    public VisitorResult visitNumericalNanNumberExpressionLabel(@NotNull wcpsParser.NumericalNanNumberExpressionLabelContext ctx) {
        // NAN_NUMBER_CONSTANT
        // e.g: c = nan
        WcpsResult result = nanScalarHandler.handle(ctx.getText());
        return result;
    }

    @Override
    public VisitorResult visitCoverageExpressionCoverageLabel(@NotNull wcpsParser.CoverageExpressionCoverageLabelContext ctx) {
        // LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS
        // e.g: used when a coverageExpression is surrounded by ().
        WcpsResult coverageExpression = (WcpsResult) visit(ctx.coverageExpression());

        WcpsResult result = parenthesesCoverageExpressionHandler.handle(coverageExpression);
        return result;
    }

    @Override
    public VisitorResult visitWhereClauseLabel(@NotNull wcpsParser.WhereClauseLabelContext ctx) {
        // WHERE (LEFT_PARENTHESIS)? booleanScalarExpression (RIGHT_PARENTHESIS)?
        // e.g: where (c > 2)
        WcpsResult coverageExpr = (WcpsResult) visit(ctx.booleanScalarExpression());

        WcpsResult result = whereClauseHandler.handle(coverageExpr);
        return result;
    }

    @Override
    public VisitorResult visitBooleanUnaryScalarLabel(@NotNull wcpsParser.BooleanUnaryScalarLabelContext ctx) {
        // booleanUnaryOperator LEFT_PARENTHESIS? booleanScalarExpression RIGHT_PARENTHESIS?
        // e.g: not(1 > 2)
        String operator = ctx.booleanUnaryOperator().getText();
        WcpsResult coverageExpr = (WcpsResult) visit(ctx.booleanScalarExpression());

        WcpsResult result = booleanUnaryScalarExpressionHandler.handle(operator, coverageExpr);
        return result;
    }

    @Override
    public VisitorResult visitBooleanNumericalComparisonScalarLabel(@NotNull wcpsParser.BooleanNumericalComparisonScalarLabelContext ctx) {
        // numericalScalarExpression numericalComparissonOperator numericalScalarExpression
        // e.g: 1 >= avg(c) or (avg(c) = 2)
        WcpsResult leftCoverageExpr = (WcpsResult) visit(ctx.numericalScalarExpression(0));
        WcpsResult rightCoverageExpr = (WcpsResult) visit(ctx.numericalScalarExpression(1));
        String operator = ctx.numericalComparissonOperator().getText();

        WcpsResult result = booleanNumericalComparisonScalarHandler.handle(leftCoverageExpr, rightCoverageExpr, operator);
        return result;
    }

    @Override
    public VisitorResult visitReduceBooleanExpressionLabel(@NotNull wcpsParser.ReduceBooleanExpressionLabelContext ctx) {
        // reduceBooleanExpressionOperator LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS
        // e.g: some(c), all(c)
        String operator = ctx.reduceBooleanExpressionOperator().getText();
        WcpsResult coverageExpr = (WcpsResult) visit(ctx.coverageExpression());

        WcpsResult result = reduceExpressionHandler.handle(operator, coverageExpr);
        return result;
    }

    @Override
    public VisitorResult visitCoverageExpressionComparissonLabel(@NotNull wcpsParser.CoverageExpressionComparissonLabelContext ctx) {
        // coverageExpression numericalComparissonOperator coverageExpression
        // e.g: ( (c + 1) > (c - 1) )
        WcpsResult leftCoverageExpr = (WcpsResult) visit(ctx.coverageExpression(0));
        String operand = ctx.numericalComparissonOperator().getText();
        WcpsResult rightCoverageExpr = (WcpsResult) visit(ctx.coverageExpression(1));

        WcpsResult result = binaryCoverageExpressionHandler.handle(leftCoverageExpr, operand, rightCoverageExpr);
        return result;
    }

    @Override
    public VisitorResult visitBooleanBinaryScalarLabel(@NotNull wcpsParser.BooleanBinaryScalarLabelContext ctx) {
        // booleanScalarExpression booleanOperator booleanScalarExpression
        // Only use if both sides are scalar (e.g: (avg (c) > 5) or ( 3 > 2)
        WcpsResult leftCoverageExpr = (WcpsResult) visit(ctx.booleanScalarExpression(0));
        String operand = ctx.booleanOperator().getText();
        WcpsResult rightCoverageExpr = (WcpsResult) visit(ctx.booleanScalarExpression(1));

        WcpsResult result = binaryCoverageExpressionHandler.handle(leftCoverageExpr, operand, rightCoverageExpr);
        return result;
    }

    @Override
    public VisitorResult visitBooleanSwitchCaseCoverageExpression(@NotNull wcpsParser.BooleanSwitchCaseCoverageExpressionContext ctx) {
        // coverageExpression numericalComparissonOperator coverageExpression
        // NOTE: used in switch case (e.g: switch case c > 100 or c < 100 or c > c or c = c)
        WcpsResult leftCoverageExpr = (WcpsResult) visit(ctx.coverageExpression().get(0));
        String operand = ctx.numericalComparissonOperator().getText();
        WcpsResult rightCoverageExpr = (WcpsResult) visit(ctx.coverageExpression().get(1));

        WcpsResult result = booleanSwitchCaseCoverageExpressionHandler.handle(leftCoverageExpr, operand, rightCoverageExpr);
        return result;
    }

    @Override
    public VisitorResult visitNumericalUnaryScalarExpressionLabel(@NotNull wcpsParser.NumericalUnaryScalarExpressionLabelContext ctx) {
        // numericalUnaryOperation LEFT_PARENTHESIS numericalScalarExpression RIGHT_PARENTHESIS
        // e.g: abs(avg(c)), sqrt(avg(c + 1))
        String operator = ctx.numericalUnaryOperation().getText();
        WcpsResult coverageExpr = (WcpsResult) visit(ctx.numericalScalarExpression());

        WcpsResult result = unaryArithmeticExpressionHandler.handle(operator, coverageExpr);
        return result;
    }

    @Override
    public VisitorResult visitNumericalTrigonometricScalarExpressionLabel(@NotNull wcpsParser.NumericalTrigonometricScalarExpressionLabelContext ctx) {
        // trigonometricOperator LEFT_PARENTHESIS numericalScalarExpression RIGHT_PARENTHESIS
        // e.g: sin(avg(5))
        String operator = ctx.trigonometricOperator().getText();
        WcpsResult coverageExpr = (WcpsResult) visit(ctx.numericalScalarExpression());

        WcpsResult result = unaryArithmeticExpressionHandler.handle(operator, coverageExpr);
        return result;
    }

    @Override
    public VisitorResult visitNumericalBinaryScalarExpressionLabel(@NotNull wcpsParser.NumericalBinaryScalarExpressionLabelContext ctx) {
        // numericalScalarExpression numericalOperator numericalScalarExpression
        // e.g: avg(c) + 2, 5 + 3
        WcpsResult leftCoverageExpr = (WcpsResult) visit(ctx.numericalScalarExpression(0));
        String operator = ctx.numericalOperator().getText();
        WcpsResult rightCoverageExpr = (WcpsResult) visit(ctx.numericalScalarExpression(1));

        WcpsResult result = binaryCoverageExpressionHandler.handle(leftCoverageExpr, operator, rightCoverageExpr);
        return result;
    }

    @Override
    public VisitorResult visitComplexNumberConstantLabel(@NotNull wcpsParser.ComplexNumberConstantLabelContext ctx) {
        //  LEFT_PARENTHESIS REAL_NUMBER_CONSTANT COMMA REAL_NUMBER_CONSTANT RIGHT_PARENTHESIS
        // e.g: (2,5) = 2 + 5i
        String realNumberStr = ctx.REAL_NUMBER_CONSTANT(0).getText();
        String imagineNumberStr = ctx.REAL_NUMBER_CONSTANT(1).getText();

        WcpsResult result = complexNumberConstantHandler.handle(realNumberStr, imagineNumberStr);
        return result;
    }

    @Override
    public VisitorResult visitReduceNumericalExpressionLabel(@NotNull wcpsParser.ReduceNumericalExpressionLabelContext ctx) {
        // reduceNumericalExpressionOperator LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS
        // e.g: count(c + 3), min(c), max(c - 2)
        String operator = ctx.reduceNumericalExpressionOperator().getText();
        WcpsResult coverageExpr = (WcpsResult) visit(ctx.coverageExpression());

        WcpsResult result = reduceExpressionHandler.handle(operator, coverageExpr);
        return result;
    }

    @Override
    public VisitorResult visitGeneralCondenseExpressionLabel(@NotNull wcpsParser.GeneralCondenseExpressionLabelContext ctx) {
        //   CONDENSE condenseExpressionOperator
        //   OVER axisIterator (COMMA axisIterator)*
        //  (whereClause)?
        //   USING coverageExpression
        // e.g: condense + over $px x(0:100), $py y(0:100) where ( max(c) < 100 ) using c[i($x),j($y)]

        String operator = ctx.condenseExpressionOperator().getText();

        ArrayList<AxisIterator> axisIterators = new ArrayList<>();
        String rasqlAliasName = "";
        String aliasName = "";
        int count = 0;

        // to build the IndexCRS for axis iterator
        int numberOfAxis = axisIterators.size();
        String crsUri = CrsUtil.OPENGIS_INDEX_ND_PATTERN.replace("%d", String.valueOf(numberOfAxis));

        for (wcpsParser.AxisIteratorContext i : ctx.axisIterator()) {
            AxisIterator axisIterator = (AxisIterator) visit(i);
            aliasName = axisIterator.getAliasName();
            if (rasqlAliasName.isEmpty()) {
                rasqlAliasName = aliasName.replace(WcpsSubsetDimension.AXIS_ITERATOR_DOLLAR_SIGN, "");
            }
            axisIterator.getSubsetDimension().setCrs(crsUri);
            axisIterator.setRasqlAliasName(rasqlAliasName);
            axisIterator.setAxisIteratorOrder(count);

            // Add the axis iterator to the axis iterators alias registry
            axisIteratorAliasRegistry.addAxisIteratorAliasMapping(aliasName, axisIterator);
            axisIterators.add(axisIterator);
            // the order of axis iterator in the coverage
            count++;
        }

        WcpsResult whereClause = null;
        if (ctx.whereClause() != null) {
            whereClause = (WcpsResult) visit(ctx.whereClause());
        }

        WcpsResult usingExpr = (WcpsResult) visit(ctx.coverageExpression());

        WcpsResult result = generalCondenserHandler.handle(operator, axisIterators, whereClause, usingExpr);
        return result;
    }

    @Override
    public VisitorResult visitCoverageExpressionShorthandTrimLabel(@NotNull wcpsParser.CoverageExpressionShorthandTrimLabelContext ctx) {
        //  coverageExpression LEFT_BRACKET dimensionIntervalList RIGHT_BRACKET
        // e.g: c[Lat(0:20)] - Trim
        DimensionIntervalList dimensionIntList = (DimensionIntervalList) visit(ctx.dimensionIntervalList());
        WcpsResult coverageExpr = (WcpsResult) visit(ctx.coverageExpression());

        WcpsResult wcpsResult = null;
        try {
            wcpsResult = subsetExpressionHandler.handle(coverageExpr, dimensionIntList);
        } catch (PetascopeException ex) {
            // It cannot fetch the coefficient for the regular axis
            String errorMessage = "Error processing shorthand trim() operator expression. Reason: " + ex.getExceptionText() + ".";
            throw new WCPSException(errorMessage, ex);
        }

        return wcpsResult;
    }

    @Override
    public VisitorResult visitCoverageExpressionTrimCoverageLabel(@NotNull wcpsParser.CoverageExpressionTrimCoverageLabelContext ctx) {
        // TRIM LEFT_PARENTHESIS coverageExpression COMMA LEFT_BRACE dimensionIntervalList RIGHT_BRACE RIGHT_PARENTHESIS
        // e.g: trim(c, {Lat(0:20)})
        DimensionIntervalList dimensionIntList = (DimensionIntervalList) visit(ctx.dimensionIntervalList());
        WcpsResult coverageExpr = (WcpsResult) visit(ctx.coverageExpression());

        WcpsResult wcpsResult = null;
        try {
            wcpsResult = subsetExpressionHandler.handle(coverageExpr, dimensionIntList);
        } catch (PetascopeException ex) {
            // It cannot fetch the coefficient for the regular axis
            String errorMessage = "Error processing trim() operator expression. Reason: " + ex.getExceptionText() + ".";
            throw new WCPSException(errorMessage, ex);
        }

        return wcpsResult;
    }

    @Override
    public VisitorResult visitCoverageExpressionShorthandSliceLabel(@NotNull wcpsParser.CoverageExpressionShorthandSliceLabelContext ctx) {
        // coverageExpression LEFT_BRACKET dimensionPointList RIGHT_BRACKET
        // e.g: c[Lat(0)]
        WcpsResult coverageExpr = (WcpsResult) visit(ctx.coverageExpression());
        DimensionIntervalList dimensionIntervalList = (DimensionIntervalList) visit(ctx.dimensionPointList());

        WcpsResult wcpsResult = null;
        try {
            wcpsResult = subsetExpressionHandler.handle(coverageExpr, dimensionIntervalList);
        } catch (PetascopeException ex) {
            String errorMessage = "Error processing shorthand slice() operator expression. Reason: " + ex.getExceptionText() + ".";
            throw new WCPSException(errorMessage, ex);
        }

        return wcpsResult;
    }

    @Override
    public VisitorResult visitCoverageExpressionSliceLabel(@NotNull wcpsParser.CoverageExpressionSliceLabelContext ctx) {
        // SLICE  LEFT_PARENTHESIS  coverageExpression  COMMA   LEFT_BRACE    dimensionPointList    RIGHT_BRACE   RIGHT_PARENTHESIS
        // e.g: slice(c, Lat(0))
        WcpsResult coverageExpr = (WcpsResult) visit(ctx.coverageExpression());
        DimensionIntervalList dimensionIntervalList = (DimensionIntervalList) visit(ctx.dimensionPointList());

        WcpsResult wcpsResult = null;

        try {
            wcpsResult = subsetExpressionHandler.handle(coverageExpr, dimensionIntervalList);
        } catch (PetascopeException ex) {
            // It cannot fetch the coefficient for the regular axis
            String errorMessage = "Error processing slice() operator expression. Reason: " + ex.getExceptionText() + ".";
            throw new WCPSException(errorMessage, ex);
        }

        return wcpsResult;
    }

    @Override
    public VisitorResult visitCoverageConstantExpressionLabel(@NotNull wcpsParser.CoverageConstantExpressionLabelContext ctx) {
        // COVERAGE IDENTIFIER
        // OVER axisIterator (COMMA axisIterator)*
        // VALUE LIST LOWER_THAN   constant (SEMICOLON constant)*   GREATER_THAN
        // e.g: coverage cov over $px x(0:20), $py(0:30) values list<-1,0,1,2,2>
        String identifier = ctx.COVERAGE_VARIABLE_NAME().getText();

        ArrayList<AxisIterator> axisIterators = new ArrayList<>();
        ArrayList<String> constants = new ArrayList<>();

        // to build the IndexCRS for axis iterator
        int numberOfAxis = axisIterators.size();
        String crsUri = CrsUtil.OPENGIS_INDEX_ND_PATTERN.replace("%d", String.valueOf(numberOfAxis));

        //parse the axis specifications
        for (wcpsParser.AxisIteratorContext i : ctx.axisIterator()) {
            AxisIterator axisIterator = (AxisIterator) visit(i);
            axisIterator.getSubsetDimension().setCrs(crsUri);
            axisIterators.add(axisIterator);
        }

        //parse the constants (e.g: <-1,....1>)
        for (wcpsParser.ConstantContext i : ctx.constant()) {
            constants.add(i.getText());
        }

        WcpsResult result = coverageConstantHandler.handle(identifier, axisIterators, constants);
        return result;
    }

    @Override
    public VisitorResult visitCoverageExpressionExtendLabel(@NotNull wcpsParser.CoverageExpressionExtendLabelContext ctx) {
        // EXTEND LEFT_PARENTHESIS coverageExpression COMMA LEFT_BRACE dimensionIntervalList RIGHT_BRACE RIGHT_PARENTHESIS
        // extend($c, {intervalList})
        // e.g: extend(c[t(0)], {Lat:"CRS:1"(0:200), Long:"CRS:1"(0:300)}
        WcpsResult coverageExpr = (WcpsResult) visit(ctx.coverageExpression());
        DimensionIntervalList dimensionIntervalList = (DimensionIntervalList) visit(ctx.dimensionIntervalList());

        WcpsResult wcpsResult = null;

        try {
            wcpsResult = extendExpressionHandler.handle(coverageExpr, dimensionIntervalList);
        } catch (PetascopeException ex) {
            String errorMessage = "Error processing extend() operator expression. Reason: " + ex.getExceptionText() + ".";
            throw new WCPSException(errorMessage, ex);
        }

        return wcpsResult;
    }

    @Override
    public VisitorResult visitCoverageExpressionExtendByDomainIntervalsLabel(@NotNull wcpsParser.CoverageExpressionExtendByDomainIntervalsLabelContext ctx) {
        // EXTEND LEFT_PARENTHESIS coverageExpression COMMA LEFT_BRACE domainIntervals RIGHT_BRACE RIGHT_PARENTHESIS
        // extend($c, {domain() or imageCrsdomain()})
        // e.g: extend(c[t(0)], { imageCrsdomain(Lat:"CRS:1"(0:200), Long:"CRS:1"(0:300)) })
        // NOTE: imageCrsdomain() or domain() will return metadata value not Rasql
        WcpsMetadataResult wcpsMetadataResult = (WcpsMetadataResult) visit(ctx.domainIntervals());

        String domainIntervalsRasql = wcpsMetadataResult.getResult().replace("(", "").replace(")", "");
        WcpsResult coverageExpr = (WcpsResult) visit(ctx.coverageExpression());

        WcpsResult result = null;
        try {
            result = extendExpressionByDomainIntervalsHandler.handle(coverageExpr, wcpsMetadataResult, domainIntervalsRasql);
        } catch (PetascopeException ex) {
            String errorMessage = "Error processing extend() operator on coverage expression. Reason: " + ex.getMessage();
            log.error(errorMessage, ex);
            throw new WCPSException(ExceptionCode.RuntimeError, errorMessage);
        }
        return result;
    }

    @Override
    public VisitorResult visitRangeConstructorExpressionLabel(@NotNull wcpsParser.RangeConstructorExpressionLabelContext ctx) {
        // LEFT_BRACE  (fieldName COLON coverageExpression) (SEMICOLON fieldName COLON coverageExpression)* RIGHT_BRACE
        // NOT used in switch case
        // e.g: {red: c.0, green: c.1, blue: c.2}
        Map<String, WcpsResult> rangeConstructor = new LinkedHashMap();
        // this share same metadata between each range element (e.g: {red:, green:,...}
        for (int i = 0; i < ctx.fieldName().size(); i++) {
            String rangeName = ctx.fieldName().get(i).getText();
            if (rangeConstructor.get(rangeName) != null) {
                throw new DuplcateRangeNameException(rangeName);
            }
            // NOTE: if rangeName already existed, it is invalid (e.g: {red: ..., green:,... red: ...}
            // this is a coverage expression
            WcpsResult wcpsResult = (WcpsResult) visit(ctx.coverageExpression().get(i));
            rangeConstructor.put(rangeName, wcpsResult);
        }

        WcpsResult result = rangeConstructorHandler.handle(rangeConstructor);
        return result;
    }

    @Override
    public VisitorResult visitRangeConstructorSwitchCaseExpressionLabel(@NotNull wcpsParser.RangeConstructorSwitchCaseExpressionLabelContext ctx) {
        // LEFT_BRACE  (fieldName COLON coverageExpression) (SEMICOLON fieldName COLON coverageExpression)*  RIGHT_BRACE
        // USED in switch case
        // e.g: {red: 15, green: 12, blue: 13} is used in switch case
        Map<String, WcpsResult> rangeConstructor = new LinkedHashMap();
        for (int i = 0; i < ctx.fieldName().size(); i++) {
            // this is a scalar value
            WcpsResult wcpsResult = (WcpsResult) visit(ctx.coverageExpression().get(i));
            rangeConstructor.put(ctx.fieldName().get(i).getText(), wcpsResult);
        }

        WcpsResult result = rangeConstructorSwitchCaseHandler.handle(rangeConstructor);
        return result;
    }

    @Override
    public VisitorResult visitScalarValueCoverageExpressionLabel(@NotNull wcpsParser.ScalarValueCoverageExpressionLabelContext ctx) {
        // scalarValueCoverageExpression: (LEFT_PARENTHESIS)?  coverageExpression (RIGHT_PARENTHESIS)?
        // e.g: for $c in (mr) return (c[i(0), j(0)] = 25 + 30 - 50)
        WcpsResult coverageExpr = (WcpsResult) visit(ctx.coverageExpression());

        WcpsResult result = coverageExpr;
        return result;
    }

    @Override
    public VisitorResult visitStringScalarExpressionLabel(@NotNull wcpsParser.StringScalarExpressionLabelContext ctx) {
        // STRING_LITERAL
        // e.g: 1, c, x, y
        String str = ctx.STRING_LITERAL().getText();

        WcpsResult result = stringScalarHandler.handle(str);
        return result;
    }

    @Override
    public VisitorResult visitCoverageIdentifierExpressionLabel(@NotNull wcpsParser.CoverageIdentifierExpressionLabelContext ctx) {
        // IDENTIFIER LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS
        // identifier(), e.g: identifier($c) -> mr
        WcpsResult coverageExpr = (WcpsResult) visit(ctx.coverageExpression());

        WcpsMetadataResult result = coverageIdentifierHandler.handle(coverageExpr);
        return result;
    }

    @Override
    public VisitorResult visitCoverageCrsSetExpressionLabel(@NotNull wcpsParser.CoverageCrsSetExpressionLabelContext ctx) {
        // CRSSET LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS
        // crsSet(), e.g: crsSet($c) -> Lat:"http://...4326",  "CRS:1",
        //                              Long:"http://...4326", "CRS:1"

        WcpsResult coverageExpr = (WcpsResult) visit(ctx.coverageExpression());

        WcpsMetadataResult result = coverageCrsSetHandler.handle(coverageExpr);
        return result;
    }

    @Override
    public VisitorResult visitStarExpressionLabel(@NotNull wcpsParser.StarExpressionLabelContext ctx) {
        // MULTIPLICATION
        // e.g: c[Lat(*)]
        WcpsResult result = stringScalarHandler.handle("\"" + ASTERISK + "\"");
        return result;
    }
    
    @Override
    public VisitorResult visitCoverageExpressionScaleByFactorLabel(@NotNull wcpsParser.CoverageExpressionScaleByFactorLabelContext ctx) {
        // SCALE LEFT_PARENTHESIS
        //        coverageExpression COMMA number
        // RIGHT_PARENTHESIS
        // e.g: scale(c[t(0)], 2.5) with c is 3D coverage which means 2D output will be 
        // downscaled to 2.5 by each dimension (e.g: grid pixel is: 100 then the result is 100 / 2.5)
        WcpsResult coverageExpr = (WcpsResult) visit(ctx.coverageExpression());
        String factorNumber = ctx.number().getText();        
        
        WcpsResult wcpsResult = scaleExpressionByFactorHandler.handle(coverageExpr, new BigDecimal(factorNumber));        
        return wcpsResult;
    }
    
    @Override
    public VisitorResult visitCoverageExpressionScaleByAxesLabel(@NotNull wcpsParser.CoverageExpressionScaleByAxesLabelContext ctx) {
        // SCALE_AXES LEFT_PARENTHESIS
        //        coverageExpression COMMA scaleDimensionIntervalList
        // RIGHT_PARENTHESIS
        // e.g: scaleaxes(c[t(0)], [Lat(2.5), Long(2.5)]) with c is 3D coverage which means 2D output will be 
        // downscaled to 2.5 by each dimension (e.g: grid pixel is: 100 then the result is 100 / 2.5)
        WcpsResult coverageExpr = (WcpsResult) visit(ctx.coverageExpression());
        WcpsScaleDimensionIntevalList scaleAxesDimensionList = (WcpsScaleDimensionIntevalList) visit(ctx.scaleDimensionIntervalList());
        
        WcpsResult wcpsResult = scaleExpressionByScaleAxesHandler.handle(coverageExpr, scaleAxesDimensionList);        
        return wcpsResult;
    }
    
    @Override
    public VisitorResult visitCoverageExpressionScaleBySizeLabel(@NotNull wcpsParser.CoverageExpressionScaleBySizeLabelContext ctx) {
        // SCALE_SIZE LEFT_PARENTHESIS
        //        coverageExpression COMMA scaleDimensionIntervalList
        // RIGHT_PARENTHESIS
        // e.g: scalesize(c[t(0)], [Lat(25), Long(25)]) with c is 3D coverage which means 2D output will have grid domain: 0:24, 0:24 (25 pixesl for each dimension)
        WcpsResult coverageExpr = (WcpsResult) visit(ctx.coverageExpression());
        WcpsScaleDimensionIntevalList scaleDimensionIntervalList = (WcpsScaleDimensionIntevalList) visit(ctx.scaleDimensionIntervalList());
        
        WcpsResult wcpsResult = scaleExpressionByScaleSizeHandler.handle(coverageExpr, scaleDimensionIntervalList);        
        return wcpsResult;
    }
    
    @Override
    public VisitorResult visitCoverageExpressionScaleByExtentLabel(@NotNull wcpsParser.CoverageExpressionScaleByExtentLabelContext ctx) {
        // SCALE_EXTENT LEFT_PARENTHESIS
        //        coverageExpression COMMA scaleDimensionIntervalList
        // RIGHT_PARENTHESIS
        // e.g: scaleextent(c[t(0)], [Lat(25:30), Long(25:30)]) with c is 3D coverage which means 2D output will have grid domain: 25:30, 25:30 (6 pixesl for each dimension)
        WcpsResult coverageExpr = (WcpsResult) visit(ctx.coverageExpression());
        WcpsScaleDimensionIntevalList scaleAxesDimensionList = (WcpsScaleDimensionIntevalList) visit(ctx.scaleDimensionIntervalList());
        
        WcpsResult wcpsResult = scaleExpressionByScaleExtentHandler.handle(coverageExpr, scaleAxesDimensionList);        
        return wcpsResult;
    }

    @Override
    public VisitorResult visitCoverageExpressionScaleByDimensionIntervalsLabel(@NotNull wcpsParser.CoverageExpressionScaleByDimensionIntervalsLabelContext ctx) {
        // SCALE LEFT_PARENTHESIS
        //          coverageExpression COMMA LEFT_BRACE dimensionIntervalList RIGHT_BRACE (COMMA fieldInterpolationList)*
        //       RIGHT_PARENTHESIS
        // scale($c, {intervalList})
        // e.g: scale(c[t(0)], {Lat:"CRS:1"(0:200), Long:"CRS:1"(0:300)}

        WcpsResult coverageExpr = (WcpsResult) visit(ctx.coverageExpression());
        DimensionIntervalList dimensionIntervalList = (DimensionIntervalList) visit(ctx.dimensionIntervalList());

        WcpsResult wcpsResult = null;

        try {
            wcpsResult = scaleExpressionByDimensionIntervalsHandler.handle(coverageExpr, dimensionIntervalList);
        } catch (PetascopeException ex) {
            String errorMessage = "Error processing scale() operator expression. Reason: " + ex.getExceptionText() + ".";
            throw new WCPSException(errorMessage, ex);
        }

        return wcpsResult;
    }

    @Override
    public VisitorResult visitCoverageExpressionScaleByImageCrsDomainLabel(@NotNull wcpsParser.CoverageExpressionScaleByImageCrsDomainLabelContext ctx) {
        // SCALE LEFT_PARENTHESIS
        //        coverageExpression COMMA LEFT_BRACE domainIntervals RIGHT_BRACE
        // RIGHT_PARENTHESIS
        // scale($c, { imageCrsDomain() or domain() }) - domain() can only be 1D
        // e.g: scale(c[t(0)], { imageCrsdomain(Lat:"CRS:1"(0:200), Long:"CRS:1"(0:300)) })
        // NOTE: imageCrsdomain() or domain() will return metadata value not Rasql
        WcpsMetadataResult wcpsMetadataResult = (WcpsMetadataResult) visit(ctx.domainIntervals());
        String domainIntervalsRasql = wcpsMetadataResult.getResult().replace("(", "").replace(")", "");
        WcpsResult coverageExpr = (WcpsResult) visit(ctx.coverageExpression());

        WcpsResult result = scaleExpressionByImageCrsDomainHandler.handle(coverageExpr, wcpsMetadataResult, domainIntervalsRasql);
        return result;
    }

    @Override
    public VisitorResult visitImageCrsExpressionLabel(@NotNull wcpsParser.ImageCrsExpressionLabelContext ctx) {
        // IMAGECRS LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS
        // imageCrs() - return coverage's grid axis
        // e.g: for c in (mr) return imageCrs(c) (imageCrs is the grid CRS of coverage)
        // return: CRS:1
        WcpsResult coverageExpr = (WcpsResult) visit(ctx.coverageExpression());

        WcpsMetadataResult result = imageCrsExpressionHandler.handle(coverageExpr);
        return result;
    }

    @Override
    public VisitorResult visitImageCrsDomainExpressionLabel(@NotNull wcpsParser.ImageCrsDomainExpressionLabelContext ctx) {
        // IMAGECRSDOMAIN LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS
        // imageCrsDomain($c) - can be 2D, 3D,.. depend on coverageExpression
        // e.g: c[t(0), Lat(0:20), Long(0:30)] is 2D
        // return (0:5,0:100,0:231), used with scale, extend (scale(, { imageCrsdomain() })
        WcpsResult coverageExpr = (WcpsResult) visit(ctx.coverageExpression());

        WcpsMetadataResult result = imageCrsDomainExpressionHandler.handle(coverageExpr);
        return result;
    }

    @Override
    public VisitorResult visitImageCrsDomainByDimensionExpressionLabel(@NotNull wcpsParser.ImageCrsDomainByDimensionExpressionLabelContext ctx) {
        // IMAGECRSDOMAIN LEFT_PARENTHESIS coverageExpression COMMA axisName RIGHT_PARENTHESIS
        // imageCrsDomain($c, axisName) - 1D
        // return (0:5), used with axis iterator ($px x ( imageCrsdomain() ))
        WcpsResult coverageExpr = (WcpsResult) visit(ctx.coverageExpression());
        String axisName = ctx.axisName().getText();

        WcpsMetadataResult result = imageCrsDomainExpressionByDimensionExpressionHandler.handle(coverageExpr, axisName);
        return result;
    }

    @Override
    public VisitorResult visitDomainExpressionLabel(@NotNull wcpsParser.DomainExpressionLabelContext ctx) {
        // IMAGECRSDOMAIN LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS
        // domain() - 1D
        // e.g: for c in (mean_summer_airtemp) return domain(c[Lat(0:20)], Lat, "http://.../4326")
        // return: (0:20) as domain of inpurt coverage in Lat is 0:20
        WcpsResult coverageExpr = (WcpsResult) visit(ctx.coverageExpression());
        String axisName = ctx.axisName().getText();
        // NOTE: need to strip bounding quotes of crs (e.g: ""http://.../4326"")
        String crsName = CrsUtility.stripBoundingQuotes(ctx.crsName().getText());

        WcpsMetadataResult result = domainExpressionHandler.handle(coverageExpr, axisName, crsName);
        return result;
    }

    @Override
    public VisitorResult visitSwitchCaseRangeConstructorExpressionLabel(@NotNull wcpsParser.SwitchCaseRangeConstructorExpressionLabelContext ctx) {
        // switch case which returns range constructor
        // e.g: for c in (mr) return encode(
        //        switch case c > 1000 return {red: 107; green:17; blue:68}
        //               default return {red: 150; green:103; blue:14}
        //        , "png")

        List<WcpsResult> booleanResults = new ArrayList<>();
        List<WcpsResult> rangeResults = new ArrayList<>();

        List<RangeField> firstRangeFields = new ArrayList<>();

        // cases return
        for (int i = 0; i < ctx.CASE().size(); i++) {
            // Handle each rangeConstructor (case)
            WcpsResult wcpsBooleanExpressionResult = (WcpsResult) visit(ctx.booleanSwitchCaseCombinedExpression().get(i));
            WcpsResult wcpsRangeConstructorResult = (WcpsResult) visit(ctx.rangeConstructorSwitchCaseExpression().get(i));

            List<RangeField> rangeFields = wcpsRangeConstructorResult.getMetadata().getRangeFields();

            if (firstRangeFields.isEmpty()) {
                firstRangeFields.addAll(rangeFields);
            } else {
                // validate range fields list
                RangeFieldService.validateRangeFields(firstRangeFields, rangeFields);
            }

            booleanResults.add(wcpsBooleanExpressionResult);
            rangeResults.add(wcpsRangeConstructorResult);
        }

        // default return also returns a range constructor (cases size = ranges size - 1)
        int casesSize = ctx.CASE().size();
        WcpsResult wcpsDefaultRangeConstructorResult = (WcpsResult) visit(ctx.rangeConstructorSwitchCaseExpression().get(casesSize));
        List<RangeField> rangeFields = wcpsDefaultRangeConstructorResult.getMetadata().getRangeFields();
        // check if the next case expression has the same band names and band numbers
        RangeFieldService.validateRangeFields(firstRangeFields, rangeFields);
        rangeResults.add(wcpsDefaultRangeConstructorResult);

        WcpsResult result = switchCaseRangeConstructorExpression.handle(booleanResults, rangeResults);
        return result;
    }

    @Override
    public VisitorResult visitSwitchCaseScalarValueExpressionLabel(@NotNull wcpsParser.SwitchCaseScalarValueExpressionLabelContext ctx) {
        // switch case which returns scalar value (mostly is numerical)
        // e.g: e.g: for c in (mr) return encode(
        //               switch case c > 10 and c < 20 return (char)5
        //               default return 2
        //           ,"csv")
        List<WcpsResult> booleanResults = new ArrayList<>();
        List<WcpsResult> scalarResults = new ArrayList<>();

        for (int i = 0; i < ctx.CASE().size(); i++) {
            // Handle each rangeConstructor (case)
            WcpsResult wcpsBooleanExpressionResult = (WcpsResult) visit(ctx.booleanSwitchCaseCombinedExpression().get(i));
            WcpsResult wcpsScalarValueResult = (WcpsResult) visit(ctx.scalarValueCoverageExpression().get(i));

            booleanResults.add(wcpsBooleanExpressionResult);
            scalarResults.add(wcpsScalarValueResult);
        }

        // default return also returns a range constructor (cases size = ranges size - 1)
        int casesSize = ctx.CASE().size();
        WcpsResult wcpsRangeConstructorResult = (WcpsResult) visit(ctx.scalarValueCoverageExpression().get(casesSize));
        scalarResults.add(wcpsRangeConstructorResult);

        WcpsResult result = switchCaseScalarValueExpression.handle(booleanResults, scalarResults);
        return result;
    }
    
    @Override
    public VisitorResult visitCoverageIsNullExpression(@NotNull wcpsParser.CoverageIsNullExpressionContext ctx) {
        // coverageExpression IS NULL
        // e.g: encode(c is null, "csv") then if c's nodata = 0, then all the pixels of c with 0 values will return true, others return false.
        WcpsResult coverageExpression = (WcpsResult)visit(ctx.coverageExpression());
        boolean isNull = true;        
        if (ctx.NOT() != null) {
             isNull = false;
        }
        WcpsResult result = coverageIsNullHandler.handle(coverageExpression, isNull);        
        return result;
    }  

    // PARAMETERS
    /* ----------- Parameters objects for nodes ----------- */
    @Override
    public VisitorResult visitDimensionPointElementLabel(@NotNull wcpsParser.DimensionPointElementLabelContext ctx) {
        // axisName (COLON crsName)? LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS
        // e.g: i(5) - Slicing point
        String axisName = ctx.axisName().getText();
        String crs = null;
        if (ctx.crsName() != null) {
            crs = ctx.crsName().getText().replace("\"", "");
        }
        String bound = ((WcpsResult)visit(ctx.coverageExpression())).getRasql();
        WcpsSliceSubsetDimension sliceSubsetDimension = new WcpsSliceSubsetDimension(axisName, crs, bound);
        if(ctx.coverageExpression().getText().startsWith("\"")){
            sliceSubsetDimension.setTemporal(true);
        }
        return sliceSubsetDimension;
    }

    @Override
    public VisitorResult visitDimensionPointListLabel(@NotNull wcpsParser.DimensionPointListLabelContext ctx) {
        // dimensionPointElement (COMMA dimensionPointElement)*
        // e.g: i(0), j(0) - List of Slicing points
        List<WcpsSubsetDimension> intervalList = new ArrayList<>(ctx.dimensionPointElement().size());
        for (wcpsParser.DimensionPointElementContext elem : ctx.dimensionPointElement()) {
            intervalList.add((WcpsSubsetDimension) visit(elem));
        }

        DimensionIntervalList dimensionIntervalList = new DimensionIntervalList(intervalList);
        return dimensionIntervalList;
    }
    
    @Override
    public VisitorResult visitSliceScaleDimensionIntervalElementLabel(@NotNull wcpsParser.SliceScaleDimensionIntervalElementLabelContext ctx) {
        // axisName LEFT_PARENTHESIS   number   RIGHT_PARENTHESIS
        // e.g: i(0.5) and is used for scaleaxes, scalesize expression, e.g: scale(c, [i(0.5)])
        AbstractWcpsScaleDimension scaleAxesDimension = new WcpsSliceScaleDimension(ctx.axisName().getText(), ctx.number().getText());
        
        return scaleAxesDimension;
    }
    
    @Override
    public VisitorResult visitTrimScaleDimensionIntervalElementLabel(@NotNull wcpsParser.TrimScaleDimensionIntervalElementLabelContext ctx) {
        // axisName LEFT_PARENTHESIS   number   RIGHT_PARENTHESIS
        // e.g: i(20:30) and is used for scaleextent expression, e.g: scale(c, [i(20:30)])
        AbstractWcpsScaleDimension scaleAxesDimension = new WcpsTrimScaleDimension(ctx.axisName().getText(), 
                                            ctx.number().get(0).getText(),
                                            ctx.number().get(1).getText());
        
        return scaleAxesDimension;
    }
    
    @Override
    public VisitorResult visitScaleDimensionIntervalListLabel(@NotNull wcpsParser.ScaleDimensionIntervalListLabelContext ctx) {
        // scaleDimensionIntervalElement (COMMA scaleDimensionIntervalElement)* 
        // e.g: [i(0.5),j(0.5)]        
        List<AbstractWcpsScaleDimension> intervalList = new ArrayList<>();
        for (wcpsParser.ScaleDimensionIntervalElementContext elem : ctx.scaleDimensionIntervalElement()) {
            intervalList.add((AbstractWcpsScaleDimension) visit(elem));
        }
        WcpsScaleDimensionIntevalList scaleAxesDimensionList = new WcpsScaleDimensionIntevalList(intervalList);
        
        return scaleAxesDimensionList;
    }
    
    @Override
    public VisitorResult visitSliceDimensionIntervalElementLabel(@NotNull wcpsParser.SliceDimensionIntervalElementLabelContext ctx) {
        // axisName (COLON crsName)?  LEFT_PARENTHESIS   coverageExpression   RIGHT_PARENTHESIS
        // e.g: i(0)
        String bound = ((WcpsResult)visit(ctx.coverageExpression())).getRasql();
        String crs = ctx.crsName() == null ? "" : ctx.crsName().getText().replace("\"", "");

        WcpsSliceSubsetDimension sliceSubsetDimension = null;
  
        sliceSubsetDimension = new WcpsSliceSubsetDimension(ctx.axisName().getText(), crs, bound);
        if(ctx.coverageExpression().getText().startsWith("\"")){
            sliceSubsetDimension.setTemporal(true);
        }

        return sliceSubsetDimension;
    }

    @Override
    public VisitorResult visitTrimDimensionIntervalElementLabel(@NotNull wcpsParser.TrimDimensionIntervalElementLabelContext ctx) {
        // axisName (COLON crsName)? LEFT_PARENTHESIS  coverageExpression   COLON coverageExpression    RIGHT_PARENTHESIS
        // e.g: i:"CRS:1"(2:3)
        try {
            String rawLowerBound = ((WcpsResult)visit(ctx.coverageExpression(0))).getRasql();
            String rawUpperBound = ((WcpsResult)visit(ctx.coverageExpression(1))).getRasql();
            String crs = null;
            if (ctx.crsName() != null) {
                crs = ctx.crsName().getText().replace("\"", "");
            }
            if (ctx.axisName() == null) {
                throw new InvalidAxisNameException("No axis given");
            }
            String axisName = ctx.axisName().getText();

            WcpsTrimSubsetDimension trimSubsetDimension = new WcpsTrimSubsetDimension(axisName, crs, rawLowerBound, rawUpperBound);

            if(ctx.coverageExpression(0).getText().startsWith("\"") || ctx.coverageExpression(1).getText().startsWith("\"")){
                trimSubsetDimension.setTemporal(true);
            }

            return trimSubsetDimension;
        } catch (NumberFormatException ex) {
            throw new InvalidSubsettingException(ctx.axisName().getText(), new ParsedSubset(ctx.coverageExpression(0).getText(), ctx.coverageExpression(1).getText()), ex);
        }
    }

    @Override
    public VisitorResult visitDimensionIntervalListLabel(@NotNull wcpsParser.DimensionIntervalListLabelContext ctx) {
        // dimensionIntervalElement (COMMA dimensionIntervalElement)*
        // e.g: c[i(0:20),j(0:30)]
        List<WcpsSubsetDimension> intervalList = new ArrayList<>();
        for (wcpsParser.DimensionIntervalElementContext elem : ctx.dimensionIntervalElement()) {
            intervalList.add((WcpsSubsetDimension) visit(elem));
        }
        DimensionIntervalList dimensionIntervalList = new DimensionIntervalList(intervalList);
        return dimensionIntervalList;
    }

    @Override
    public VisitorResult visitIntervalExpressionLabel(@NotNull wcpsParser.IntervalExpressionLabelContext ctx) {
        // scalarExpression COLON scalarExpression  (e.g: 5:10)
        String lowIntervalStr = ctx.scalarExpression(0).getText();
        String highIntervalStr = ctx.scalarExpression(1).getText();

        IntervalExpression intervalExpression = new IntervalExpression(lowIntervalStr, highIntervalStr);
        return intervalExpression;
    }

    @Override
    public VisitorResult visitAxisSpecLabel(@NotNull wcpsParser.AxisSpecLabelContext ctx) {
        // dimensionIntervalElement (e.g: i(0:20) or j:"CRS:1"(0:30))
        WcpsSubsetDimension subsetDimension = (WcpsSubsetDimension) visit(ctx.dimensionIntervalElement());

        AxisSpec axisSpec = new AxisSpec(subsetDimension);
        return axisSpec;
    }

    @Override
    public VisitorResult visitAxisIteratorLabel(@NotNull wcpsParser.AxisIteratorLabelContext ctx) {
        // coverageVariableName dimensionIntervalElement (e.g: $px x(Lat(0:20)) )
        WcpsSubsetDimension subsetDimension = (WcpsSubsetDimension) visit(ctx.dimensionIntervalElement());
        String coverageVariableName = ctx.coverageVariableName().getText();

        AxisIterator axisIterator = new AxisIterator(coverageVariableName, subsetDimension);
        return axisIterator;
    }

    @Override
    public VisitorResult visitAxisIteratorDomainIntervalsLabel(@NotNull wcpsParser.AxisIteratorDomainIntervalsLabelContext ctx) {
        // coverageVariableName axisName LEFT_PARENTHESIS  domainIntervals RIGHT_PARENTHESIS
        // e.g: $px x (imageCrsdomain(c[Lat(0:20)]), Lat)
        // e.g: $px x (imageCrsdomain(c[Long(0)], Lat[(0:20)]))
        // e.g: $px x (domain(c[Lat(0:20)], Lat, "http://.../4326"))
        // return x in (50:80)

        WcpsMetadataResult wcpsMetadataResult = (WcpsMetadataResult) visit(ctx.domainIntervals());
        String rasqlInterval = wcpsMetadataResult.getResult();
        // remove the () from (50:80) and extract lower and upper grid bounds
        String[] gridBounds = rasqlInterval.substring(1, rasqlInterval.length() - 1).split(":");

        // NOTE: it expects that "domainIntervals" will only return 1 trimming domain in this case (so only 1D)
        String coverageVariableName = ctx.coverageVariableName().getText();
        WcpsSubsetDimension trimSubsetDimension = new WcpsTrimSubsetDimension(AxisIterator.AXIS_NAME_DEAULT, AxisIterator.CRS_DEFAULT,
                gridBounds[0], gridBounds[1]);

        AxisIterator axisIterator = new AxisIterator(coverageVariableName, trimSubsetDimension);
        return axisIterator;
    }
}
