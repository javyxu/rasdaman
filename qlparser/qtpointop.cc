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
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
*
* Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann /
rasdaman GmbH.
*
* For more information please see <http://www.rasdaman.org>
* or contact Peter Baumann via <baumann@rasdaman.com>.
*/
/*************************************************************
 *
 *
 * PURPOSE:
 *
 *
 * COMMENTS:
 *
 ************************************************************/

static const char rcsid[] = "@(#)qlparser, QtPointOp: $Id: qtpointop.cc,v 1.7 2002/06/05 18:18:17 coman Exp $";

#include "config.h"
#include "raslib/rmdebug.hh"

#include "qlparser/qtpointop.hh"
#include "qlparser/qtdata.hh"
#include "qlparser/qtconst.hh"
#include "qlparser/qtpointdata.hh"
#include "qlparser/qtatomicdata.hh"

#include "catalogmgr/ops.hh"
#include "relcatalogif/type.hh"

#include <logging.hh>

#include <iostream>
#ifndef CPPSTDLIB
#include <ospace/string.h> // STL<ToolKit>
#else
#include <string>
using namespace std;
#endif

const QtNode::QtNodeType QtPointOp::nodeType = QT_POINTOP;

QtPointOp::QtPointOp(QtOperationList *opList)
    : QtNaryOperation(opList)
{
    pt = NULL;
    bool areAllQtConst = true;
    for (auto iter = opList->begin(); iter != opList->end(); iter++)
    {
        areAllQtConst &= ((*iter)->getNodeType() == QT_CONST);
    }

    if (areAllQtConst)
    {
        pt = new r_Point(opList->size());
        size_t i= 0;
        for (auto iter = opList->begin(); iter != opList->end(); iter++, i++)
        {
            QtData* coordPtr  = (dynamic_cast<QtConst*>(*iter))->getDataObj();
            
           (*pt)[i] = (static_cast<QtAtomicData*>(coordPtr))->getSignedValue();
        }
    }
}

QtPointOp::~QtPointOp()
{
    delete pt;
    pt = NULL;
}


QtData *
QtPointOp::evaluate(QtDataList *inputList)
{
    startTimer("QtPointOp");

    QtData *returnValue = NULL;
    QtDataList *operandList = NULL;

    if (getOperands(inputList, operandList))
    {
        vector<QtData *>::iterator dataIter;
        bool goOn = true;

        if (operandList)
        {
            // first check operand types
            for (dataIter = operandList->begin(); dataIter != operandList->end() && goOn; dataIter++)
                if (!((*dataIter)->getDataType() == QT_SHORT || (*dataIter)->getDataType() == QT_USHORT ||
                      (*dataIter)->getDataType() == QT_LONG || (*dataIter)->getDataType() == QT_ULONG ||
                      (*dataIter)->getDataType() == QT_OCTET || (*dataIter)->getDataType() == QT_CHAR))
                {
                    goOn = false;
                    break;
                }

            if (!goOn)
            {
                LFATAL << "Error: QtPointOp::evaluate() - operands of point expression must be of type integer.";

                parseInfo.setErrorNo(410);

                // delete the old operands
                if (operandList)
                {
                    for (dataIter = operandList->begin(); dataIter != operandList->end(); dataIter++)
                        if ((*dataIter))
                        {
                            (*dataIter)->deleteRef();
                        }

                    delete operandList;
                    operandList = NULL;
                }

                throw parseInfo;
            }

            //
            // create a QtPointData object and fill it
            //
            r_Point ptVar(operandList->size());
            r_Nullvalues *nullValues = NULL;

            for (dataIter = operandList->begin(); dataIter != operandList->end(); dataIter++)
                if ((*dataIter)->getDataType() == QT_SHORT ||
                    (*dataIter)->getDataType() == QT_LONG ||
                    (*dataIter)->getDataType() == QT_OCTET)
                {
                    ptVar << (static_cast<QtAtomicData *>(*dataIter))->getSignedValue();
                    nullValues = (static_cast<QtAtomicData *>(*dataIter))->getNullValues();
                }
                else
                {
                    ptVar << (static_cast<QtAtomicData *>(*dataIter))->getUnsignedValue();
                    nullValues = (static_cast<QtAtomicData *>(*dataIter))->getNullValues();
                }
            returnValue = new QtPointData(ptVar);
            returnValue->setNullValues(nullValues);

            // delete the old operands
            if (operandList)
            {
                for (dataIter = operandList->begin(); dataIter != operandList->end(); dataIter++)
                    if ((*dataIter))
                    {
                        (*dataIter)->deleteRef();
                    }

                delete operandList;
                operandList = NULL;
            }
        }
    }

    stopTimer();

    return returnValue;
}

void QtPointOp::printTree(int tab, std::ostream &s, QtChildType mode)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtPointOp Object " << static_cast<int>(getNodeType()) << getEvaluationTime() << std::endl;

    QtNaryOperation::printTree(tab, s, mode);
}

void QtPointOp::printAlgebraicExpression(std::ostream &s)
{
    s << "[";

    QtNaryOperation::printAlgebraicExpression(s);

    s << "]";
}

const QtTypeElement &
QtPointOp::checkType(QtTypeTuple *typeTuple)
{
    dataStreamType.setDataType(QT_TYPE_UNKNOWN);

    QtOperationList::iterator iter;
    bool opTypesValid = true;

    for (iter = operationList->begin(); iter != operationList->end() && opTypesValid; iter++)
    {
        const QtTypeElement &type = (*iter)->checkType(typeTuple);

        // valid types: integers
        if (!(type.getDataType() == QT_SHORT ||
              type.getDataType() == QT_LONG ||
              type.getDataType() == QT_OCTET ||
              type.getDataType() == QT_USHORT ||
              type.getDataType() == QT_ULONG ||
              type.getDataType() == QT_CHAR))
        {
            opTypesValid = false;
            break;
        }
    }

    if (!opTypesValid)
    {
        LFATAL << "Error: QtPointOp::checkType() - operand of point expression must be of type integer.";
        parseInfo.setErrorNo(410);
        throw parseInfo;
    }

    dataStreamType.setDataType(QT_POINT);

    return dataStreamType;
}

