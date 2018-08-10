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
#include "tilecache.hh"

#include <algorithm>
#include <set>
#include <logging.hh>

using namespace std;

CacheType TileCache::cache;
CacheLRU TileCache::lru;

long TileCache::cacheLimit = 0L;
long TileCache::cacheSize = 0L;

void TileCache::insert(KeyType key, ValueType value)
{
    if (key == 0)
    {
        // invalid key
        return;
    }

    CacheValue* tileToCache = value;
    if (contains(key))
    {
        CacheValue* tile = cache[key];
        if (tile == NULL)
        {
            LERROR << "Error: cached NULL value!";
            remove(key);
        }
        else
        {
            // update referencing tiles
            tileToCache->addReferencingTiles(tile->getReferencingTiles());

            cacheSize -= tile->getSize();
            removeValue(tile);
            if (tile->getData() == tileToCache->getData())
            {
                delete tile;
                tile = NULL;
            }
            cache.erase(key);
            if (tile)
            {
                delete tile;
                tile = NULL;
            }
        }
        LDEBUG << "already inserted";
    }

    cache.insert(CachePairType(key, tileToCache));
    updateValue(tileToCache);
    cacheSize += tileToCache->getSize();
    readjustCache();

}

ValueType TileCache::get(KeyType key)
{
    CacheValue* ret = NULL;
    if (contains(key))
    {
        ret = cache[key];
        if (ret != NULL)
        {
            updateValue(ret);
        }
    }
    else
    {
        LDEBUG << "key not found";
    }
    return ret;
}

bool TileCache::contains(KeyType key)
{
    bool ret = cache.find(key) != cache.end();
    LDEBUG << "TileCache::contains( " << key << " = " << ret << " )";
    return ret;
}

ValueType TileCache::remove(KeyType key)
{
    CacheValue* ret = NULL;
    if (contains(key))
    {
        ret = cache[key];
        if (ret != NULL)
        {
            cacheSize -= ret->getSize();
            removeValue(ret);
            BLOBTile::writeCachedToDb(ret);
        }
        cache.erase(key);
    }
    else
    {
        LDEBUG << "key not found";
    }
    return ret;
}

void TileCache::removeKey(KeyType key)
{
    CacheValue* ret = NULL;
    if (contains(key))
    {
        ret = cache[key];
        if (ret != NULL)
        {
            cacheSize -= ret->getSize();
            removeValue(ret);
            delete ret;
            ret = NULL;
        }
        cache.erase(key);
    }
    else
    {
        LDEBUG << "key not found";
    }
}
void TileCache::clear()
{
    typedef CacheType::iterator it_type;
    for (it_type it = cache.begin(); it != cache.end(); it++)
    {
        LDEBUG << "TileCache::clear() - removing key " << it->first;
        CacheValue* value = it->second;
        BLOBTile::writeCachedToDb(value);
    }
    cache.clear();
    lru.clear();
    cacheSize = 0;
}

void TileCache::readjustCache()
{
    if (cacheSize > cacheLimit)
    {
        long count = 0;
        LDEBUG << "freeing up space from cache...";

        if (cacheSize > cacheLimit && lru.size() > 0)
        {
            CacheLRU::reverse_iterator it;
            for (it = lru.rbegin(); it != lru.rend(); it++)
            {
                CacheValue* value = *it;
                if (value->getReferencingTiles().empty())
                {
                    remove(value->getOId().getCounter());
                    ++count;
                    if (cacheSize <= cacheLimit || lru.empty())
                    {
                        break;
                    }
                }
            }
        }
        LDEBUG << "removed " << count << " blobs from cache.";
    }
}

void TileCache::updateValue(ValueType value)
{
    CacheLRU::iterator pos = std::find(lru.begin(), lru.end(), value);
    if (pos != lru.end())
    {
        LDEBUG << "moving to beginning of LRU list.";
        lru.splice(lru.begin(), lru, pos);
    }
    else
    {
        LDEBUG << "inserting at beginning of LRU list.";
        lru.insert(lru.begin(), value);
    }
}

void TileCache::removeValue(ValueType value)
{
    CacheLRU::iterator pos = std::find(lru.begin(), lru.end(), value);
    if (pos != lru.end())
    {
        lru.erase(pos);
    }
}

void TileCache::insert(OId& key, ValueType value)
{
    insert(OID_KEY(key), value);
}

ValueType TileCache::get(OId& key)
{
    return get(OID_KEY(key));
}

bool TileCache::contains(OId& key)
{
    return contains(OID_KEY(key));
}

ValueType TileCache::remove(OId& key)
{
    return remove(OID_KEY(key));
}

void TileCache::removeKey(OId& key)
{
    removeKey(OID_KEY(key));
}