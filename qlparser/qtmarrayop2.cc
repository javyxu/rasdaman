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

static const char rcsid[] = "@(#)qlparser, QtMarrayOp2: $Header: /home/rasdev/CVS-repository/rasdaman/qlparser/qtmarrayop2.cc,v 1.17 2003/12/27 20:51:28 rasdev Exp $";

#include "config.h"
#include "raslib/rmdebug.hh"

#include "qlparser/qtmarrayop2.hh"
#include "qlparser/qtdata.hh"
#include "qlparser/qtmdd.hh"
#include "qlparser/qtpointdata.hh"
#include "qlparser/qtmintervaldata.hh"
#include "qlparser/qtmintervalop.hh"

#include "mddmgr/mddobj.hh"
#include "qlparser/querytree.hh"

#include "catalogmgr/typefactory.hh"
#include "relcatalogif/basetype.hh"
#include "relcatalogif/longtype.hh"

#include "catalogmgr/algebraops.hh"

#include <logging.hh>

#include <iostream>
#ifndef CPPSTDLIB
#include <ospace/string.h> // STL<ToolKit>
#else
#include <string>
using namespace std;
#endif

extern QueryTree* parseQueryTree;

QtMarrayOp2::QtMarrayOp2(mddIntervalListType*& aList, QtOperation*& cellExp)
    :  iterators(*aList), qtOperation(cellExp)
{
}

QtMarrayOp2::~QtMarrayOp2()
{
}

bool QtMarrayOp2::concatenateIntervals()
{
    QtData* data = NULL;

    // check for validity
    bool eflag = true;
    mddIntervalListType::const_iterator ii;
    QtNode::QtOperationList::const_iterator j;
    QtNode::QtOperationList* ddd = NULL;
    QtOperation* op1 = NULL;
    QtOperation* op2 = NULL;

    for (ii = iterators.begin(); ii != iterators.end() ; ii++)
    {
        ddd = (static_cast<QtMintervalOp*>(ii->tree))->getInputs();
        for (j = ddd->begin(); j != ddd->end(); j++)
        {
            if (static_cast<QtNode*>(*j))
            {
                if (((static_cast<QtNode*>(*j))->getNodeType() == QtNode::QT_INTERVALOP))
                {
                    op1 = (static_cast<QtBinaryOperation*>(*j))->getInput1();
                    if ((static_cast<QtNode*>(op1))->getNodeType() != QtNode::QT_CONST)
                    {
                        eflag = false;
                    }
                    op2 = (static_cast<QtBinaryOperation*>(*j))->getInput2();
                    if ((static_cast<QtNode*>(op2))->getNodeType() != QtNode::QT_CONST)
                    {
                        eflag = false;
                    }
                }
                else if ((static_cast<QtNode*>(*j))->getNodeType() != QtNode::QT_CONST)
                {
                    eflag = false;
                }
            }
        }
    }
    LTRACE << "Validity check completed.";
    if (eflag)
    {
        LTRACE << "eflag is true!";
        // compute total dimension
        LTRACE << "QtMarrayOp2: Dimensions: ";
        greatDimension = 0;
        mddIntervalListType::const_iterator i;
        for (i = iterators.begin(); i != iterators.end() ; i++)
        {
            // evaluate intervals
            data = (i->tree)->evaluate(0);
            r_Dimension dimension = (static_cast<QtMintervalData*>(data))->getMintervalData().dimension();
            LTRACE << dimension << " | ";
            greatDimension += dimension;
        }
        LTRACE << " Total " << greatDimension;

        // concatenate the data of the intervals into one big interval
        LTRACE << "QtMarray2: Domains: ";
        greatDomain = r_Minterval(greatDimension);
        for (i = iterators.begin(); i != iterators.end() ; i++)
        {
            // evaluate intervals
            data = i->tree->evaluate(0);
            r_Minterval domain    = (static_cast<QtMintervalData*>(data))->getMintervalData();
            r_Dimension dimension = domain.dimension();
            r_Dimension jj;
            for (jj = 0; jj != dimension; jj++)
            {
                r_Sinterval part = domain[jj];
                greatDomain << part;
            }
            LTRACE << domain << " | ";
        }
        LTRACE << " Total: " << greatDomain;

        // concatenate the identifier names to one big identifier name
        LTRACE << "QtMarray2: Iterators: ";
        greatIterator = string("");
        string iname("");
        for (i = iterators.begin(); i != iterators.end() ; i++)
        {
            // get iterator name
            iname = string(i->variable);
            greatIterator = greatIterator + string(" ") + string(i->variable);
            LTRACE << i->variable << " | ";
        }
    }

    return eflag;
}



void QtMarrayOp2::rewriteVars()
{
    if (!oldMarray)
    {
        LTRACE << "concatenateIteratorNames: ";
        mddIntervalListType::const_iterator i;
        // concatenate the identifier names to one big identifier name
        LTRACE << "QtMarray2: Iterators: ";
        greatIterator = string("");
        string iname("");
        for (i = iterators.begin(); i != iterators.end() ; i++)
        {
            // get iterator name
            iname = string(i->variable);
            greatIterator = greatIterator + string(" ") + string(i->variable);
            LTRACE << i->variable << " | ";
        }
        LTRACE << " Total: " << greatIterator;
    }
    traverse(qtOperation);
}

void QtMarrayOp2::traverse(QtOperation*& node)
{

    if (node != 0)
    {
        QtOperation* temp = NULL;


        if ((static_cast<QtNode*>(node))->getNodeType() == QtNode::QT_DOMAIN_OPERATION)
        {
            // syntax: dinput [ dmiop ]

            // traverse dmiop
            QtOperation* dmiop  = (static_cast<QtDomainOperation*>(node))->getMintervalOp();
            temp = static_cast<QtOperation*>(dmiop);
            traverse(temp);
            dmiop = temp;
            (static_cast<QtDomainOperation*>(node))->setMintervalOp(dmiop);

            // if dinput == QtVariable then rewrite it
            QtOperation* dinput = (static_cast<QtDomainOperation*>(node))->getInput();

            // if not a variable, then recurse
            if ((static_cast<QtNode*>(dinput))->getNodeType() != QtNode::QT_MDD_VAR)
            {
                // traverse dinput
                temp = static_cast<QtOperation*>(dinput);
                traverse(temp);
                dinput = temp;
                (static_cast<QtDomainOperation*>(node))->setInput(dinput);

                // get childs and traverse them
                QtNode::QtNodeList* childList = node->getChilds(QtNode::QT_DIRECT_CHILDS);
                for (QtNode::QtNodeList::iterator iter = childList->begin(); iter != childList->end(); iter++)
                {
                    temp = static_cast<QtOperation*>(*iter);
                    traverse(temp);
                    *iter = temp;
                };
                delete childList;
                childList = NULL;
            };
        }
        else
        {

            if (node->getNodeType() == QtNode::QT_MDD_VAR)
            {
                if (QueryTree::symtab.lookupSymbol((static_cast<QtVariable*>(node))->getIteratorName()))
                {
                    if (!oldMarray)
                    {
                        LTRACE << "marray2 var identifier:" << ((QtVariable*)node)->getIteratorName() << " replacing with " << greatIterator;
                        (static_cast<QtVariable*>(node))->setIteratorName(greatIterator);
                    };
                    // replace with var[0]
                    QtDomainOperation* dop = new QtDomainOperation(new QtConst(new QtAtomicData(0, sizeof(r_Long))) /*new QtPointOp( lop )*/);
                    dop->setInput(static_cast<QtVariable*>(node));
                    node = dop;
                    parseQueryTree->addDomainObject(dop);
                };
            }
            else
            {

                // traverse inputs
                switch ((static_cast<QtNode*>(node))->getNodeType())
                {
                /*
                      // with no input
                      case QtNode::QT_UNDEFINED_NODE:
                      case QtNode::QT_MDD_ACCESS:
                      case QtNode::QT_OPERATION_ITERATOR:
                      case QtNode::QT_SELECTION_ITERATOR:
                      case QtNode::QT_JOIN_ITERATOR:
                      case QtNode::QT_UPDATE:
                      case QtNode::QT_INSERT:
                      case QtNode::QT_DELETE:
                      case QtNode::QT_COMMAND:
                      case QtNode::QT_PYRAMID:
                      case QtNode::QT_MDD_VAR:
                      case QtNode::QT_CONST:
                      case QtNode::QT_MDD_STREAM:
                */
                // from Unary
                case QtNode::QT_TANH:
                case QtNode::QT_TAN:
                case QtNode::QT_SQRT:
                case QtNode::QT_SINH:
                case QtNode::QT_SIN:
                case QtNode::QT_NOT:
                case QtNode::QT_LOG:
                case QtNode::QT_LN:
                case QtNode::QT_EXP:
                case QtNode::QT_DOT:
                case QtNode::QT_COSH:
                case QtNode::QT_COS:
                case QtNode::QT_ARCTAN:
                case QtNode::QT_ARCSIN:
                case QtNode::QT_ARCCOS:
                case QtNode::QT_ABS:
                case QtNode::QT_REALPART:
                case QtNode::QT_IMAGINARPART:
                case QtNode::QT_CAST:
                case QtNode::QT_SDOM:
                case QtNode::QT_OID:
                case QtNode::QT_LO:
                case QtNode::QT_HI:
//    case QtNode::QT_DOMAIN_OPERATION:
                case QtNode::QT_CONVERSION:
                case QtNode::QT_SOME:
                case QtNode::QT_MINCELLS:
                case QtNode::QT_MAXCELLS:
                case QtNode::QT_COUNTCELLS:
                case QtNode::QT_AVGCELLS:
                case QtNode::QT_ALL:
                case QtNode::QT_ADDCELLS:
                case QtNode::QT_CSE_ROOT:
                {
                    QtOperation* uinput = (static_cast<QtUnaryOperation*>(node))->getInput();

                    temp = static_cast<QtOperation*>(uinput);
                    traverse(temp);
                    uinput = temp;

                    (static_cast<QtUnaryOperation*>(node))->setInput(uinput);
                };
                break;

                // from Binary
                case QtNode::QT_SHIFT:
                case QtNode::QT_SCALE:
                case QtNode::QT_MARRAYOP:
                case QtNode::QT_INTERVALOP:
                case QtNode::QT_CONDENSEOP:
                case QtNode::QT_XOR:
                case QtNode::QT_PLUS:
                case QtNode::QT_MAX_BINARY:
                case QtNode::QT_MIN_BINARY:
                case QtNode::QT_OR:
                case QtNode::QT_NOT_EQUAL:
                case QtNode::QT_MULT:
                case QtNode::QT_MINUS:
                case QtNode::QT_LESS_EQUAL:
                case QtNode::QT_LESS:
                case QtNode::QT_IS:
                case QtNode::QT_EQUAL:
                case QtNode::QT_DIV:
                case QtNode::QT_INTDIV:
                case QtNode::QT_MOD:
                case QtNode::QT_AND:
                case QtNode::QT_OVERLAY:
                case QtNode::QT_BIT:
                {
                    QtOperation* binput1 = (static_cast<QtBinaryOperation*>(node))->getInput1();
                    QtOperation* binput2 = (static_cast<QtBinaryOperation*>(node))->getInput2();

                    QtOperation* temp1 = 0;
                    QtOperation* temp2 = 0;
                    temp1 = static_cast<QtOperation*>(binput1);
                    temp2 = static_cast<QtOperation*>(binput2);
                    traverse(temp1);
                    traverse(temp2);
                    binput1 = temp1;
                    binput2 = temp2;

                    (static_cast<QtBinaryOperation*>(node))->setInput1(binput1);
                    (static_cast<QtBinaryOperation*>(node))->setInput2(binput2);
                };
                break;

                // from Nary
                case QtNode::QT_POINTOP:
                case QtNode::QT_MINTERVALOP:
                case QtNode::QT_RANGE_CONSTRUCTOR:
                case QtNode::QT_CONCAT:
                {
                    QtNode::QtOperationList* ninput = (static_cast<QtNaryOperation*>(node))->getInputs();

                    for (QtNode::QtOperationList::iterator iter = ninput->begin(); iter != ninput->end(); iter++)
                    {
                        temp = static_cast<QtOperation*>(*iter);
                        traverse(temp);
                        *iter = temp;
                    };

                    (static_cast<QtNaryOperation*>(node))->setInputs(ninput);
                };
                break;
                default:
                {
                    // do nothing
                };
                break;
                };

                // get childs and traverse them
                QtNode::QtNodeList* childList = node->getChilds(QtNode::QT_DIRECT_CHILDS);
                for (QtNode::QtNodeList::iterator iter = childList->begin(); iter != childList->end(); iter++)
                {
                    temp = static_cast<QtOperation*>(*iter);
                    traverse(temp);
                    *iter = temp;
                };
                delete childList;
                childList = NULL;
            };
        };
    };
}




