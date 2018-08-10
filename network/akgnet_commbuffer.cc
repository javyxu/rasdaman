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
/**
 * SOURCE: akgnet_commbuffer.cc
 *
 * MODULE: akg network
 * CLASS:  CommBuffer
 *
 * COMMENTS:
 *
 */

#include "config.h"
#include <akgnet_commbuffer.hh>
#include <string>
#include <assert.h>
#include <logging.hh>

akg::CommBuffer::CommBuffer() noexcept
{
    data     = NULL;
    buffSize = 0;
    maxBuffSize = 0;
    fillSize = 0;
    sendSize = 0;
    allocated = false;
}

akg::CommBuffer::CommBuffer(int size) noexcept
{
    assert(size > 0);
    data = NULL;
    maxBuffSize = 0;
    allocate(size);
}

akg::CommBuffer::CommBuffer(void* externalBuffer, int totalSize, int dataSize) noexcept
{
    data = NULL;
    maxBuffSize = 0;
    takeOver(externalBuffer, totalSize, dataSize);
}

akg::CommBuffer::~CommBuffer() noexcept
{
    if (data != NULL)
    {
        delete[] data;
        data = NULL;
    }
}

bool  akg::CommBuffer::allocate(int size) noexcept
{
    assert(size > 0);

    if (data != NULL && maxBuffSize >= size)
    {
        // already allocated enough buffer, nothing to do
    }
    else
    {
        freeBuffer();
        if (data)
        {
            delete[] data;
            data = NULL;
        }
        data = new char[size];
        maxBuffSize = size;
    }

    buffSize = size;
    allocated = true;
    return true;
}

void akg::CommBuffer::freeBuffer() noexcept
{
    // optimize -- buffer is only freed in the destructor -- DM 2014-feb-03
//    if(allocated == true && data != NULL) delete[] data;
//    data     = NULL;
    buffSize = 0;
    fillSize = 0;
    sendSize = 0;
    allocated = false;
}

void akg::CommBuffer::takeOver(void* externalBuffer, int totalSize, int dataSize) noexcept
{
    assert(externalBuffer != 0);
    assert(totalSize > 0);
    assert(dataSize >= 0);
    assert(totalSize >= dataSize);

    freeBuffer();
    if (data)
    {
        delete[] data;
        data = NULL;
    }
    data     = static_cast<char*>(externalBuffer);
    buffSize = totalSize;
    maxBuffSize = buffSize;
    fillSize = dataSize;
}

bool akg::CommBuffer::resize(int newSize) noexcept
{
    assert(data != 0);

    // we can't make the buffer smaller by truncating inside data!
    if (newSize < fillSize)
    {
        return false;
    }

    char* newData = new char[newSize];
    memcpy(newData, data, static_cast<size_t>(fillSize));
    if (allocated == true)
    {
        delete[] data;
        data = NULL;
    }

    data      = newData;
    buffSize  = newSize;
    maxBuffSize = newSize;
    allocated = true;
    return true;
}

void* akg::CommBuffer::getData() noexcept
{
    return data;
}
int   akg::CommBuffer::getDataSize() noexcept
{
    return fillSize;
}
int   akg::CommBuffer::getBufferSize() noexcept
{
    return buffSize;
}
int   akg::CommBuffer::getSendedSize() noexcept
{
    return sendSize;
}
int   akg::CommBuffer::getNotFilledSize() noexcept
{
    return buffSize - fillSize;
}
int   akg::CommBuffer::getNotSendedSize() noexcept
{
    return fillSize - sendSize;
}
bool  akg::CommBuffer::isAllocated() noexcept
{
    return allocated;
}

int akg::CommBuffer::read(FileDescriptor& socket) noexcept
{
    int rasp = socket.read(data + fillSize, buffSize - fillSize);

    if (rasp >= 0)
    {
        fillSize += rasp;
    }

    return rasp;
}

int akg::CommBuffer::read(const void* externalBuffer, int size) noexcept
{
    assert(externalBuffer != 0);
    assert(size >= 0);

    int cpSize = size < (buffSize - fillSize) ? size : (buffSize - fillSize);

    memcpy(data + fillSize, externalBuffer, static_cast<size_t>(cpSize));
    fillSize += cpSize;

    return cpSize;
}

int akg::CommBuffer::reserve(int size) noexcept
{
    assert(size >= 0);

    int cpSize = size < (buffSize - fillSize) ? size : (buffSize - fillSize);

    fillSize += cpSize;

    return cpSize;
}

int akg::CommBuffer::write(FileDescriptor& socket) noexcept
{
    LDEBUG << "CommBuffer write fillSize=" << fillSize << " sendSize=" << sendSize;
    int rasp = socket.write(data + sendSize, fillSize - sendSize);

    if (rasp >= 0)
    {
        sendSize += rasp;
    }

    return rasp;
}

int akg::CommBuffer::write(void* externalBuffer, int size) noexcept
{
    assert(externalBuffer != 0);
    assert(size >= 0);

    int cpSize = size < (fillSize - sendSize) ? size : (fillSize - sendSize);

    memcpy(externalBuffer, data + sendSize, static_cast<size_t>(cpSize));
    sendSize += cpSize;

    return cpSize;
}

void akg::CommBuffer::clearToRead() noexcept
{
    LDEBUG << "CommBuffer clearToRead";
    fillSize = 0;
    sendSize = 0;
}
void akg::CommBuffer::clearToWrite() noexcept
{
    LDEBUG << "CommBuffer clearToWrite";
    sendSize = 0;
}

