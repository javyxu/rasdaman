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
 *
 *
 * COMMENTS:
 *   uses embedded SQL
 *
 *****************************************************************************/

#include "config.h"
#include "debug-srv.hh"
#include "sqlglobals.h"

#include "oidif.hh"
#include "sqlerror.hh"
#include "adminif.hh"
#include "sqlitewrapper.hh"
#include <logging.hh>

void
OId::initialize()
{
    loadedOk = false;

    SQLiteQuery query("SELECT NextValue FROM RAS_COUNTERS ORDER BY CounterId");
    long long nextoid;
    unsigned int i = 1;
    while (query.nextRow() && i < OId::maxCounter)
    {
        nextoid = query.nextColumnLong();
        LDEBUG << "-> nextoid=" << nextoid;
        *counterIds[i] = nextoid;
        LTRACE << "read " << counterNames[i] << " " << *counterIds[i];
        ++i;
    }
    query.finalize();

    loadedOk = true;
}

void
OId::deinitialize()
{
    if (AdminIf::isReadOnlyTA())
    {
        LTRACE << "do nothing is read only";
    }
    else
    {
        if (AdminIf::isAborted())
        {
            LTRACE << "do nothing is aborted";
        }

        else if (loadedOk == false)
        {
            LTRACE << "avoiding to write uninitialized counters into DB";
        }

        else
        {
            for (unsigned int i = 1; i < maxCounter; i++)
            {
                SQLiteQuery::executeWithParams("UPDATE RAS_COUNTERS SET NextValue = %lld WHERE CounterName = '%s'",
                                               *counterIds[i], counterNames[i]);
            }
        }
    }

    loadedOk = false;
}
