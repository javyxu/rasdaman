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
package org.rasdaman.domain.cis;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.io.Serializable;
import javax.persistence.*;
import petascope.util.BigDecimalUtil;

/**
 * CIS 1.1
 * 
 * A rangeSet component containing the range values (“pixels”, “voxels”)
of the coverage (stored as Rasdaman collection and mdds).
 */

@Entity
@Table(name = RasdamanRangeSet.TABLE_NAME)
public class RasdamanRangeSet implements Serializable {
    
    public static final String TABLE_NAME = "rasdaman_range_set";
    public static final String COLUMN_ID = TABLE_NAME + "_id";
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = COLUMN_ID)
    private long id;

    @Column(name = "collection_name")
    private String collectionName;

    @Column(name = "collection_type")
    private String collectionType;

    @Column(name = "oid")
    private Long oid;

    @Column(name= "mdd_type")
    private String mddType;
    
    @Column(name = "tiling")
    private String tiling;
    
    @OneToMany(cascade = CascadeType.ALL, fetch=FetchType.EAGER, orphanRemoval=true)
    @OrderColumn
    @JoinColumn(name = RasdamanRangeSet.COLUMN_ID)
    private List<RasdamanDownscaledCollection> rasdamanDownscaledCollections;

    public RasdamanRangeSet() {
        
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public String getCollectionType() {
        return collectionType;
    }

    public void setCollectionType(String collectionType) {
        this.collectionType = collectionType;
    }

    public Long getOid() {
        return oid;
    }

    public void setOid(Long oid) {
        this.oid = oid;
    }

    public String getMddType() {
        return mddType;
    }

    public void setMddType(String mddType) {
        this.mddType = mddType;
    }

    public String getTiling() {
        return tiling;
    }

    public void setTiling(String tiling) {
        this.tiling = tiling;
    }

    public List<RasdamanDownscaledCollection> getRasdamanDownscaledCollections() {
        // NOTE: it needs to be sorted by level ascending.
        Collections.sort(rasdamanDownscaledCollections);
        return this.rasdamanDownscaledCollections;
    }
    
    public void setRasdamanDownscaledCollections(List<RasdamanDownscaledCollection> rasdamanDownscaledCollections) {
        this.rasdamanDownscaledCollections = rasdamanDownscaledCollections;
    }
    
    /**
     * Get the downscaled collection by scale level.
     */
    public RasdamanDownscaledCollection getRasdamanDownscaledCollectionByScaleLevel(BigDecimal level) {
        for (RasdamanDownscaledCollection rasdamanDownscaledCollection : this.rasdamanDownscaledCollections) {           
            if (rasdamanDownscaledCollection.getLevel().equals(BigDecimalUtil.stripDecimalZeros(level))) {
                return rasdamanDownscaledCollection;
            }
        }
        
        return null;
    }
    
    /**
     * From the list of downscaled collections and input level, 
     * select the ***highest level collection which is lower than**** this input level.
     * e.g: a list of levels [2, 5, 6, 9], input is: 7, then result is collection with level 6.
     */
    public RasdamanDownscaledCollection getRasdamanDownscaledCollectionAsSourceCollection(BigDecimal level) {
        RasdamanDownscaledCollection result = null;
        for (RasdamanDownscaledCollection rasdamanDownscaledCollection : this.getRasdamanDownscaledCollections()) {            
            if (rasdamanDownscaledCollection.getLevel().compareTo(level) > 0) {
                return result;
            } else if (rasdamanDownscaledCollection.getLevel().compareTo(level) < 0) {
                result = rasdamanDownscaledCollection;
            }
        }
        
        return result;
    }
}
