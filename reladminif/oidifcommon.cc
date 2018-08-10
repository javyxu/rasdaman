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
// This is -*- C++ -*-
/*****************************************************************************
 *
 *
 * PURPOSE:
 *  code common to all database interface implementations
 *
 *
 * COMMENTS:
 *   uses embedded SQL
 *
 *****************************************************************************/
#include "config.h"
#include <string.h>
#include "oidif.hh"
#include "adminif.hh"
#include "databaseif.hh"
#include <stdlib.h>
#include "externs.h"
#include "sqlerror.hh"
#include "raslib/error.hh"
#include <logging.hh>

#ifdef RMANBENCHMARK
RMTimer OId::oidAlloc("OId", "allocateOId");
#endif

#ifdef RMANBENCHMARK
RMTimer OId::oidResolve("OId", "resolveOId");
#endif

long long OId::ID_MULTIPLIER = 512;

OId::OIdCounter OId::nextMDDOID = 0;

OId::OIdCounter OId::nextMDDCOLLOID = 0;

OId::OIdCounter OId::nextMDDTYPEOID = 0;

OId::OIdCounter OId::nextMDDBASETYPEOID = 0;

OId::OIdCounter OId::nextMDDDIMTYPEOID = 0;

OId::OIdCounter OId::nextMDDDOMTYPEOID = 0;

OId::OIdCounter OId::nextSTRUCTTYPEOID = 0;

OId::OIdCounter OId::nextSETTYPEOID = 0;

OId::OIdCounter OId::nextBLOBOID = 0;

OId::OIdCounter OId::nextDBMINTERVALOID = 0;

OId::OIdCounter OId::nextSTORAGEOID = 0;

OId::OIdCounter OId::nextMDDHIERIXOID = 0;

OId::OIdCounter OId::nextATOMICTYPEOID = 0;

OId::OIdCounter OId::nextMDDRCIXOID = 0;

OId::OIdCounter OId::nextUDFOID = 0;

OId::OIdCounter OId::nextUDFPACKAGEOID = 0;

OId::OIdCounter OId::nextFILETILEOID = 0;

OId::OIdCounter OId::nextDBNULLVALUESOID = 0;

unsigned int
OId::maxCounter = 22;

const char*
OId::counterNames[] = { "INVALID",
                        "MDDOID",
                        "MDDCOLLOID",
                        "MDDTYPEOID",
                        "MDDBASETYPEOID",
                        "MDDDIMTYPEOID",
                        "MDDDOMTYPEOID",
                        "STRUCTTYPEOID",
                        "SETTYPEOID",
                        "BLOBOID",
                        "DBMINTERVALOID",
                        "STORAGEOID",
                        "MDDHIERIXOID",
                        "INLINEINDEXOID",
                        "INLINETILEOID",
                        "INNEROID",
                        "ATOMICTYPEOID",
                        "UDFOID",
                        "UDFPACKAGEOID",
                        "MDDRCIXOID",
                        "FILETILEOID",
                        "DBNULLVALUESOID"
                      };

OId::OIdCounter*
OId::counterIds[] =
{
    NULL,
    &nextMDDOID,
    &nextMDDCOLLOID,
    &nextMDDTYPEOID,
    &nextMDDBASETYPEOID,
    &nextMDDDIMTYPEOID,
    &nextMDDDOMTYPEOID,
    &nextSTRUCTTYPEOID,
    &nextSETTYPEOID,
    &nextBLOBOID,
    &nextDBMINTERVALOID,
    &nextSTORAGEOID,
    &nextMDDHIERIXOID,
    &nextMDDHIERIXOID,
    &nextBLOBOID,
    &nextBLOBOID,
    &nextATOMICTYPEOID,
    &nextUDFOID,
    &nextUDFPACKAGEOID,
    &nextMDDRCIXOID,
    &nextFILETILEOID,
    &nextDBNULLVALUESOID
};

bool OId::loadedOk = false;

void
OId::allocateOId(OId& id, OIdType type, OIdCounter howMany)
{
    if (howMany == 0)
    {
        LFATAL << "OId::allocateOId(" << id << ", " << type << ", " << howMany << ") allocation of zero oids not supported";
        throw r_Error(r_Error::r_Error_CreatingOIdFailed);
    }
    if (type == INVALID || type == INNEROID || type == ATOMICTYPEOID || static_cast<unsigned int>(type) > maxCounter)
    {
        LFATAL << "OIDs of the specified type (" << type << " cannot be allocated.";
        throw r_Error(r_Error::r_Error_CreatingOIdFailed);
    }
    else
    {
        id.oid = *counterIds[type];
        *counterIds[type] = *counterIds[type] + howMany;
    }
    id.oidtype = type;
}

OId::OId(const OId& oldOId)
{
    oid = oldOId.oid;
    oidtype = oldOId.oidtype;
}

OId::OId()
{
    oidtype = INVALID;
    oid = 0;
}

OId::OId(OIdPrimitive newId)
{
    oid = newId / OId::ID_MULTIPLIER;
    oidtype = static_cast<OId::OIdType>(newId - static_cast<OIdPrimitive>(static_cast<OIdPrimitive>(oid) * OId::ID_MULTIPLIER));
}

OId::OId(OIdCounter newId, OIdType type)
{
    oidtype = type;
    oid = newId;
}

OId::OIdType
OId::getType() const
{
    //LTRACE << "getType() " << oidtype;
    return oidtype;
}

void
OId::print_status(ostream& s) const
{
    s << this;
}

OId::OIdCounter
OId::getCounter() const
{
//    LTRACE << "getCounter() " << oid;
    return oid;
}

OId::operator long long() const
{
//    LTRACE << "operator() " << oid;
    return oid * OId::ID_MULTIPLIER + oidtype;
}

bool
OId::operator!= (const OId& one) const
{
//    LTRACE << "operator!=(" << one << ") " << *this;
    return !(OId::operator==(one));
}

bool
OId::operator== (const OId& one) const
{
    bool retval = false;
    if (oidtype == one.oidtype)
    {
        retval = (oid == one.oid);
    }
    else
    {
        retval = false;
    }
//    LTRACE << "operator==(" << one << ") " << *this << " retval=" << retval;
    return retval;
}

OId&
OId::operator=(const OId& old)
{
//    LTRACE << "operator=(" << old << ") "<< *this;
    if (this != &old)
    {
        oid = old.oid;
        oidtype = old.oidtype;
    }
    return *this;
}

bool
OId::operator<(const OId& old) const
{
    bool retval = false;
    if (oidtype == old.oidtype)
    {
        retval = (oid < old.oid);
    }
    else
    {
        retval = (oidtype < old.oidtype);
    }
//    LTRACE << "operator<(" << old << ") " << *this << " retval=" << retval;
    return retval;
}

bool
OId::operator>(const OId& old) const
{
    bool retval = false;
    if (oidtype == old.oidtype)
    {
        retval = (oid > old.oid);
    }
    else
    {
        retval = (oidtype > old.oidtype);
    }
//    LTRACE << "operator>(" << old << ") " << *this << " retval=" << retval;
    return retval;
}

bool
OId::operator<=(const OId& old) const
{
    bool retval = false;
    if (oidtype == old.oidtype)
    {
        retval = (oid <= old.oid);
    }
    else
    {
        retval = (oidtype < old.oidtype);
    }
//    LTRACE << "operator<=(" << old << ") " << *this << " retval=" << retval;
    return retval;
}

bool
OId::operator>=(const OId& old) const
{
    bool retval = false;
    if (oidtype == old.oidtype)
    {
        retval = (oid >= old.oid);
    }
    else
    {
        retval = (oidtype > old.oidtype);
    }
//    LTRACE << "operator>=(" << old << ") " << *this << " retval=" << retval;
    return retval;
}

bool
operator== (const long long one, const OId& two)
{
//    LTRACE << "operator==(" << one << "," << two << ")";
    bool retval = false;
    // see conversion operator above
    if ((static_cast<long long>(two)) == one)
    {
        retval = true;
    }
    return retval;
}

bool
operator== (const OId& two, const long long one)
{
//    LTRACE << "operator==(" << two << "," << one << ")";
    bool retval = false;
    // see conversion operator above
    if ((static_cast<long long>(two)) == one)
    {
        retval = true;
    }
    return retval;
}

std::ostream&
operator<<(std::ostream& s, const OId& d)
{
    s << "OId(" << d.getCounter() << ":" << d.getType() << ")";
    return s;
}

std::ostream&
operator<<(std::ostream& s, OId::OIdType d)
{
    switch (d)
    {
    case OId::INVALID:
        s << "INVALID";
        break;
    case OId::MDDOID:
        s << "MDDOID";
        break;
    case OId::MDDCOLLOID:
        s << "MDDCOLLOID";
        break;
    case OId::MDDTYPEOID:
        s << "MDDTYPEOID";
        break;
    case OId::MDDBASETYPEOID:
        s << "MDDBASETYPEOID";
        break;
    case OId::MDDDIMTYPEOID:
        s << "MDDDIMTYPEOID";
        break;
    case OId::MDDDOMTYPEOID:
        s << "MDDDOMTYPEOID";
        break;
    case OId::STRUCTTYPEOID:
        s << "STRUCTTYPEOID";
        break;
    case OId::SETTYPEOID:
        s << "SETTYPEOID";
        break;
    case OId::BLOBOID:
        s << "BLOBOID";
        break;
    case OId::DBMINTERVALOID:
        s << "DBMINTERVALOID";
        break;
    case OId::DBNULLVALUESOID:
        s << "DBNULLVALUESOID";
        break;
    case OId::STORAGEOID:
        s << "STORAGEOID";
        break;
    case OId::MDDHIERIXOID:
        s << "MDDHIERIXOID";
        break;
    case OId::DBTCINDEXOID:
        s << "DBTCINDEXOID";
        break;
    case OId::INLINETILEOID:
        s << "INLINETILEOID";
        break;
    case OId::INNEROID:
        s << "INNEROIDOID";
        break;
    case OId::ATOMICTYPEOID:
        s << "ATOMICTYPEOID";
        break;
    case OId::UDFOID:
        s << "UDFOID";
        break;
    case OId::UDFPACKAGEOID:
        s << "UDFPACKAGEOID";
        break;
    case OId::MDDRCIXOID:
        s << "MDDRCIXOID";
        break;
    case OId::FILETILEOID:
        s << "FILETILEOID";
        break;
    default:
        s << "UNKNOWN: " << static_cast<int>(d);
        break;
    }
    return s;
}


