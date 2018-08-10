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
// -*-C++-*- (for Emacs)

/*************************************************************
 *
 *
 * PURPOSE:
 *   The DoubleType class is the superclass for all classes
 *   describing the type of a cell
 *
 *
 * COMMENTS:
 *
 ************************************************************/

#ifndef _DOUBLETYPE_HH_
#define _DOUBLETYPE_HH_

#include <iostream>
#include "realtype.hh"
#include "catalogmgr/ops.hh"

class OId;

//@ManMemo: Module: {\bf relcatalogif}.

/*@Doc:
DoubleType is the base type used for 32bit integer cell
values. The value of a Double is stored in four chars.
*/

/**
  * \ingroup Relcatalogifs
  */
class DoubleType : public RealType
{
public:
    DoubleType();
    /*@Doc:
    default constructor, sets type name to "Double".
    */

    DoubleType(const OId& id);
    /*@Doc:
    */

    DoubleType(const DoubleType& old);
    /*@Doc:
    copy constructor.
    */

    DoubleType& operator=(const DoubleType& old);
    /*@Doc:
    assignment operator.
    */

    virtual ~DoubleType();
    /*@Doc:
    virtual destructor.
    */

    virtual void printCell(ostream& stream, const char* cell) const;
    /*@Doc:
    */

    virtual double* convertToCDouble(const char* cell, double* value) const;
    /*@Doc:
    */

    virtual char* makeFromCDouble(char* cell, const double* value) const;
    /*@Doc:
    */

    static const char* Name;

protected:

    virtual void readFromDb();
    /*@Doc:
    initializes the attributes of this type.
    there is no database activity.  this is hard coded.
    */
};

#endif
