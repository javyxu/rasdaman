#ifndef _QTPROJECT__
#define _QTPROJECT__
/*************************************************************
 *
 * HEADER: qtproject.hh
 *
 * MODULE: qlparser
 * CLASS:  QtProject
 *
 * PURPOSE:
 *
 * CHANGE HISTORY (append further entries):
 * when         who                what
 * ----------------------------------------------------------
 * 2010-01-31   Aiordachioaie      created
 *
 * COMMENTS:
 *
 * Copyright (C) 2010 Dr. Peter Baumann
 *
 ************************************************************/

#include "conversion/gdalincludes.hh"

#include "raslib/error.hh"
#include "qlparser/qtmdd.hh"
#include "qlparser/qtunaryoperation.hh"
#include "tilemgr/tile.hh"
#include "raslib/type.hh"

#ifdef HAVE_GDAL
class GDALDataset;
#endif

/**************************************************************
 *
 *
 * COMMENTS:
 *
 ************************************************************/

//@ManMemo: Module: {\bf qlparser}

/*@Doc:

  The class represents a (coordinate system) projection operation.

*/

class QtProject : public QtUnaryOperation
{
public:
    /// constructor getting the mdd operand and the other projection parameters
    QtProject(QtOperation* mddOp, const char* bounds, const char* crsIn, const char* crsOut);

    QtProject(QtOperation* mddOp, const char* crsIn, const char* crsOut);


    ~QtProject();

    /// method for evaluating the node
    QtData* evaluate(QtDataList* inputList);

    /// test if the edge to the parent node is of type mdd or atomic
    virtual QtAreaType getAreaType();

    const QtTypeElement& checkType(QtTypeTuple* typeTuple);

    /// debugging method
    virtual void printTree(int tab, std::ostream& s = std::cout, QtChildType mode = QT_ALL_NODES);

    /// method for evaluating the reprojection with a given operand
    QtData* evaluateMDD(QtMDD* mdd);

    /// getters for the geo bounding box
    float getMinX() const;
    float getMinY() const;
    float getMaxX() const;
    float getMaxY() const;
    char* getTargetCrs() const;

    /// optimizing load access
    void optimizeLoad(QtTrimList* trimList);

    /// method for identification of nodes
    virtual QtNodeType getNodeType() const;

private:

#ifdef HAVE_GDAL

    // Conversion methods between rasdaman and GDAL
    GDALDataset* convertTileToDataset(Tile* sourceTile, int nBands, r_Type* bandType);
    Tile* convertDatasetToTile(GDALDataset* gdalResult, int nBands, Tile* sourceTile, r_Type* bandType);

    void saveDatasetToFile(GDALDataset* ds, const char* filename, const char* driverName);

    // Perform reprojection with the help of GDAL library
    GDALDataset* performGdalReprojection(GDALDataset* gdalSource);

    // For checking the "bounds" input string
    void parseNumbers(const char* str);
    float parseOneNumber(char* str);

    // Expands a CRS definition (e.g. "EPSG:4326" etc) into its Well-Known-Text representation
    bool setCrsWKT(const char* srsin, char*& wkt);

    void setBounds(GDALDataset* dataset);

    void testCrsTransformation(const char* in, const char* out);
#endif


    /// attributes for identification of nodes
    QtOperation* mddOp;
    float xmin, ymin, xmax, ymax;
    char* wktCrsIn, *wktCrsOut;
    
    std::string initialBounds;
    std::string initialCrsIn;
    std::string initialCrsOut;

    /// attribute for identification of nodes
    static const QtNodeType nodeType;

};
#endif  // _QTPROJECT_

