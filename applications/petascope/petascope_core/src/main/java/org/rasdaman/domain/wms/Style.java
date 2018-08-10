/*
  *  This file is part of rasdaman community.
  * 
  *  Rasdaman community is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  * 
  *  Rasdaman community is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  *  See the GNU  General Public License for more details.
  * 
  *  You should have received a copy of the GNU  General Public License
  *  along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
  * 
  *  Copyright 2003 - 2014 Peter Baumann / rasdaman GmbH.
  * 
  *  For more information please see <http://www.rasdaman.org>
  *  or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package org.rasdaman.domain.wms;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import nu.xom.Element;
import org.apache.commons.lang3.StringUtils;
import static org.rasdaman.domain.wms.Layer.TABLE_PREFIX;
import petascope.core.XMLSymbols;
import petascope.util.XMLUtil;

/**
 *
 * Zero or more Styles may be advertised for a Layer or collection of layers
 * using <Style> elements, each of which shall have <Name> and <Title> elements.
 * The style's Name is used in the Map request STYLES parameter. The Title is a
 * human-readable string. If only a single style is available, that style is
 * known as the “default” style and need not be advertised by the server. A
 * <Style> may contain several other elements. <Abstract> provides a narrative
 * description while <LegendURL>
 * contains the location of an image of a map legend appropriate to the
 * enclosing style.
 *
 * A <Format> element in LegendURL indicates the MIME type of the legend image,
 * and the optional attributes width and height state the size of the image in
 * pixels. Servers should provide the width and height attributes if known at
 * the time of processing the GetCapabilities request. The legend image should
 * clearly represent the symbols, lines and colours used in the map portrayal.
 * The legend image should not contain text that duplicates the Title of the
 * layer, because that information is known to the client and may be shown to
 * the user by other means. Style declarations are inherited by child Layers. A
 * child shall not redefine a Style with the same Name as one inherited from a
 * parent. A child may define a new Style with a new Name that is not available
 * for the parent Layer
 *
 * The mandatory STYLES parameter lists the style in which each layer is to be
 * rendered. The value of the STYLES parameter is a comma-separated list of one
 * or more valid style names.
 *
 * STYLES=style_list M Comma-separated list of one rendering style per requested
 * layer.
 *
 * e.g: <Style>
 * <Name>USGS</Name>
 * <Title>USGS Topo Map Style</Title>
 * <Abstract>Features are shown in a style like that used in USGS topographic
 * maps.</Abstract>
 * <!-- A picture of a legend for a Layer in this Style -->
 * <LegendURL width="72" height="72">
 * <Format>image/gif</Format>
 * <OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink"
 * xlink:type="simple"
 * xlink:href="http://www.university.edu/legends/usgs.gif" />
 * </LegendURL>
 *
 * </Style>
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Entity
@Table(name = Style.TABLE_NAME)
public class Style {

    public static final String TABLE_NAME = TABLE_PREFIX + "_style";
    public static final String COLUMN_ID = TABLE_NAME + "_id";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = COLUMN_ID)
    private long id;

    public Style() {

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    // One, mandatory for the GetMap request 
    // (if only has 1 style then request parameter does not need to specify the name, e.g: styles=&...)
    @Column(name = "name")
    private String name;

    @Column(name = "title")
    private String title;

    @Column(name = "style_abstract")
    @Lob
    // NOTE: As this could be long text, so varchar(255) is not enough
    private String styleAbstract;

    // One, optional
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = LegendURL.COLUMN_ID)
    private LegendURL legendURL;

    // NOTE: Rasdaman does not support the StyleSheet for WMS, it uses a RASQL query to create the style for the layers
    // And this style query could be added to style abstract for human readable.
    // NOTE: As this could be long text, so varchar(255) is not enough
    @Deprecated
    @Column(name = "rasqlQueryTransformFragment")
    @Lob
    private String rasqlQueryTransformFragment;
        
    // NOTE: use this WCPS query fragment (subtracted from a full WCPS query (for c in (...) return encode(wcpsQueryFragment, "png",...))
    // to make style for WMS layer, the rasqlTransformFragment is deprecated.
    @Column(name = "wcpsQueryFragment")
    @Lob
    private String wcpsQueryFragment;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStyleAbstract() {
        return styleAbstract;
    }

    public void setStyleAbstract(String styleAbstract) {
        this.styleAbstract = styleAbstract;
    }

    public LegendURL getLegendURL() {
        return legendURL;
    }

    public void setLegendURL(LegendURL legendURL) {
        this.legendURL = legendURL;
    }

    public String getRasqlQueryTransformFragment() {
        return rasqlQueryTransformFragment;
    }

    public void setRasqlQueryTransformFragment(String rasqlQueryTransformFragment) {
        this.rasqlQueryTransformFragment = rasqlQueryTransformFragment;
    }

    public String getWcpsQueryFragment() {
        return wcpsQueryFragment;
    }

    public void setWcpsQueryFragment(String wcpsQueryFragment) {
        this.wcpsQueryFragment = wcpsQueryFragment;
    }

    /**
     * Return the Style XML element representation in string
     *
     * @return
     */
    public String getRepresentation() {
        Element styleElement = new Element(XMLSymbols.LABEL_WMS_STYLE);

        // Name
        Element nameElement = new Element(XMLSymbols.LABEL_WMS_NAME);
        nameElement.appendChild(this.name);
        styleElement.appendChild(nameElement);

        // Title
        Element titleElement = new Element(XMLSymbols.LABEL_WMS_TITLE);
        titleElement.appendChild(this.title);
        styleElement.appendChild(titleElement);

        // Abstract
        Element abstractElement = new Element(XMLSymbols.LABEL_WMS_ABSTRACT);
        
        // NOTE: fragment query is not a valid WMS style, so just add it to abstract for human readble.
        String styleAbstractStr = this.getStyleAbstract();
        if (!StringUtils.isEmpty(this.getWcpsQueryFragment())) {         
            styleAbstractStr += ". WCPS query fragment: " + XMLUtil.enquoteCDATA(this.getWcpsQueryFragment());
        } else if (!StringUtils.isEmpty(this.getRasqlQueryTransformFragment())) {
            // deprecated
            styleAbstractStr += ". Rasql transform fragment: " + XMLUtil.enquoteCDATA(this.getRasqlQueryTransformFragment());
        }
        abstractElement.appendChild(styleAbstractStr);
        styleElement.appendChild(abstractElement);

        if (this.legendURL != null) {
            styleElement.appendChild(this.getLegendURL().getRepresentation());
        }

        return styleElement.toXML();
    }

}
