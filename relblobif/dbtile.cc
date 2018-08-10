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
#include "config.h"
#include "mymalloc/mymalloc.h"
#include "dbtile.hh"
#include "reladminif/externs.h"
#include "reladminif/sqlerror.hh"
#include "raslib/error.hh"
#include "reladminif/objectbroker.hh"
#include "blobtile.hh"
#include "inlinetile.hh"
#include "reladminif/dbref.hh"
#include <logging.hh>

#include "unistd.h"
#include <iostream>
#include <cstring>
#include <vector>



r_Data_Format
DBTile::getDataFormat() const
{
    LTRACE << "getDataFormat() const " << myOId << " " << dataFormat;
    return dataFormat;
}

r_Data_Format
DBTile::getCurrentFormat() const
{
    return currentFormat;
}

void
DBTile::setCurrentFormat(const r_Data_Format& dataformat) const
{
    currentFormat = dataformat;
}

void
DBTile::setDataFormat(const r_Data_Format& dataformat)
{
    dataFormat = dataformat;
    setModified();
}

r_Bytes
DBTile::getMemorySize() const
{
    return size * sizeof(char) + sizeof(char*) + sizeof(r_Data_Format) + DBObject::getMemorySize() + sizeof(r_Bytes);
}

void
DBTile::setCells(char* newCells)
{
    if (cells != newCells)
    {
        cells = newCells;
        ownCells = true;
        setModified();
    }
}

void
DBTile::setNoModificationData(char* newCells) const
{
    if (cells != newCells)
    {
        if (cells != NULL && ownCells)
        {
            LDEBUG << "DBTile::setNoModificationData() freeing blob cells";
            free(cells);
            // cells = NULL;    // added PB 2005-jan-10
        }
        cells = newCells;
        ownCells = true;
    }
}

void
DBTile::setNoModificationSize(r_Bytes newSize) const
{
    size = newSize;
}

char*
DBTile::getCells()
{
    LTRACE << "getCells() " << myOId;
    setModified();
    return cells;
}

const char*
DBTile::getCells() const
{
    LTRACE << "getCells() const " << myOId;
    return cells;
}

char
DBTile::getCell(r_Bytes index) const
{
    LTRACE << "getCell(" << index << ") const " << myOId;
    return getCells()[index];
}

r_Bytes
DBTile::getSize() const
{
    LTRACE << "getSize() const " << myOId << " " << size;
    return size;
}

void
DBTile::setCell(r_Bytes index, char newCell)
{
    LTRACE << "setCell(" << index << ", " << (int)newCell << ") " << myOId;
    setModified();
    getCells()[index] = newCell;
}

DBTile::DBTile(r_Data_Format dataformat)
    :   DBObject(),
        size(0),
        cells(NULL),
        dataFormat(dataformat),
        currentFormat(r_Array)
{
    LTRACE << "DBTile(" << dataFormat << ")";
    objecttype = OId::INVALID;
    ownCells = true;
}

DBTile::DBTile(r_Bytes newSize, char c, r_Data_Format dataformat)
    :   DBObject(),
        size(newSize),
        cells(NULL),
        dataFormat(dataformat),
        currentFormat(r_Array)
{
    LDEBUG << "DBTile::DBTile() allocating " << newSize << " bytes for blob cells, previous ptr was " << (long) cells;
    cells = static_cast<char*>(mymalloc(newSize * sizeof(char)));
    objecttype = OId::INVALID;
    memset(cells, c, size);
    ownCells = true;
}

DBTile::DBTile(r_Bytes newSize, r_Bytes patSize, const char* pat, r_Data_Format dataformat)
    :   DBObject(),
        size(newSize),
        cells(NULL),
        dataFormat(dataformat),
        currentFormat(r_Array)
{
    objecttype = OId::INVALID;

    LDEBUG << "DBTile::DBTile() allocating " << newSize << " bytes for blob cells, previous ptr was " << (long) cells;
    cells = static_cast<char*>(mymalloc(newSize * sizeof(char)));

    r_Bytes i = 0;
    r_Bytes j = 0;

    if (patSize >= size)
    {
        // fill cells with pattern
        for (j = 0; j < size; j++)
        {
            cells[j] = pat[j];
        }
    }
    else
    {
        // fill cells with repeated pattern
        for (i = 0; i < size; i += patSize)
        {
            for (j = 0; j < patSize; j++)
            {
                cells[(i + j)] = pat[j];
            }
        }
        // pad end with 0
        if (i != size)
        {
            // no padding necessary
            i -= patSize;
            for (; i < size; i++)
            {
                cells[i] = 0;
            }
        }
        else
        {
            // fill cells with 0
            for (i = 0; i < size; i++)
            {
                cells[i] = 0;
            }
        }
    }
    ownCells = true;

}

DBTile::DBTile(r_Bytes newSize, const char* newCells, r_Data_Format dataformat)
    :   DBObject(),
        size(newSize),
        cells(0),
        dataFormat(dataformat),
        currentFormat(r_Array)
{
    LDEBUG << "DBTile::DBTile() allocating " << newSize << " bytes for blob cells, previous ptr was " << (long) cells;

    cells = static_cast<char*>(mymalloc(size * sizeof(char)));
    objecttype = OId::INVALID;
    memcpy(cells, newCells, newSize);
    ownCells = true;
}

DBTile::DBTile(r_Bytes newSize, bool takeOwnershipOfNewCells, char* newCells, r_Data_Format dataformat)
    :   DBObject(),
        size(newSize),
        cells(0),
        dataFormat(dataformat),
        currentFormat(r_Array)
{
    if (takeOwnershipOfNewCells)
    {
        LDEBUG << "creating DBTile of size " << newSize << " bytes " << " without copying the given data " << newCells << ".";
        cells = newCells;
    }
    else
    {
        LDEBUG << "creating DBTile of size " << newSize << " bytes " << " with copying the given data " << newCells << ".";
        cells = static_cast<char*>(mymalloc(size * sizeof(char)));
        memcpy(cells, newCells, newSize);
    }
    ownCells = true;
    objecttype = OId::INVALID;
}

DBTile::DBTile(const OId& id)
    :   DBObject(id),
        size(0),
        cells(NULL),
        currentFormat(r_Array)
{
    LTRACE << "DBTile(" << id << ")";
    ownCells = true;
}

DBTile::~DBTile()
{
    if (!ownCells)
    {
        return;
    }

    if (cells)
    {
        if (TileCache::cacheLimit > 0)
        {
            if (!TileCache::contains(myOId))
            {
                LDEBUG << "DBTile::~DBTile() freeing blob cells";
                free(cells);
                cells = NULL;
            }
            else
            {
                CacheValue* value = TileCache::get(myOId);
                value->removeReferencingTile(this);
            }
        }
        else
        {
            LDEBUG << "DBTile::~DBTile() freeing blob cells";
            free(cells);
            cells = NULL;
        }
    }
}

void
DBTile::resize(r_Bytes newSize)
{
    LTRACE << "resize(" << newSize << ") " << myOId;
    if (size != newSize)
    {
        setModified();
        if (cells && ownCells)
        {
            LDEBUG << "freeing blob cells";
            free(cells);
            cells = NULL;
        }
        LDEBUG << "allocating " << newSize << " bytes for blob cells.";
        cells = static_cast<char*>(mymalloc(newSize * sizeof(char)));
        if (cells == NULL)
        {
            LERROR << "failed allocating " << newSize << " bytes of memory for tile.";
            throw new r_Error(r_Error::r_Error_MemoryAllocation);
        }
        size = newSize;
    }
}

void
DBTile::printStatus(unsigned int level, std::ostream& stream) const
{
    DBObject::printStatus(level, stream);
    stream << " r_Data_Format " << dataFormat << " size " << size << " ";
#ifdef DEBUG
    for (int a = 0; a < size; a++)
    {
        stream << " " << (int)(cells[a]);
    }
    stream << endl;
#endif
}

std::ostream&
operator << (std::ostream& stream, DBTile& b)
{
    stream << "\tDBTile at " << &b << endl;
    stream << "\t\tOId\t\t:" << b.myOId << endl;
    stream << "\t\tId\t\t:" << b.myOId.getCounter() << endl;
    stream << "\t\tSize\t\t:" << b.size << endl;
    stream << "\t\tModified\t:" << static_cast<int>(b._isModified) << endl;
    stream << "\t\tCells\t\t:";
#ifdef DEBUG
    for (int a = 0; a < b.size; a++)
    {
        stream << " " << (int)(b.cells[a]);
    }
    stream << endl;
#endif
    return stream;
}

