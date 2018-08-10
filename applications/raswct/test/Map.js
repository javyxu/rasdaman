/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

/**
 * Testing Suite for Map widgets
 * @see Rj.widget.Map
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

buster.testCase("Rj.widget.Map tests", {
  setUp:function () {
    $("body").append("<div id='map-widget'></div>");
  },

  "Map widget ok":function () {
    var WMS_SERVICE_URL = "http://212.201.49.173:8080/rasogc/rasogc";

    var map = new Rj.widget.Map('#map-widget', {
      projection : "EPSG:32633",
      maxExtent : new OpenLayers.Bounds(489750,7988500,492750,7990000),
      tileSize : new OpenLayers.Size(500, 500),
      numZoomLevel : 4
    });

    var layerHakoon = new Rj.util.MapLayer("Hakoon Drive", WMS_SERVICE_URL, {
        layers: 'Hakoon_Dive_1',
        styles: 'standard',
        format : "image/png",
        transparent: "true",
        version : "1.1.0",
        exceptions : 'application/vnd.ogc.se_xml',
        customdem : 'minLevel,maxLevel,T'
      },
      {
        isBaseLayer: false,
        transitionEffect : 'resize'
      });


    var layerHakoon2 = new Rj.util.MapLayer("Hakoon Drive 2", WMS_SERVICE_URL, {
        layers: 'Hakoon_Dive_2',
        styles: 'standard',
        format : "image/png",
        transparent: "true",
        version : "1.1.0",
        exceptions : 'application/vnd.ogc.se_xml',
        customdem : 'minLevel,maxLevel,T'
      },
      {
        isBaseLayer: false,
        transitionEffect : 'resize'
      });

    var layerHakoon2b = new Rj.util.MapLayer("Hakoon Drive 2b", WMS_SERVICE_URL, {
        layers: 'Hakoon_Dive_2b',
        styles: 'standard',
        format : "image/png",
        transparent: "true",
        version : "1.1.0",
        exceptions : 'application/vnd.ogc.se_xml',
        customdem : 'minLevel,maxLevel,T'
      },
      {
        isBaseLayer: false,
        transitionEffect : 'resize'
      });

    var HakoonBathymetryLayer = new Rj.util.MapLayer("Hakon_Bathymetry", WMS_SERVICE_URL, {
      layers: 'Hakon_Bathymetry',
      styles: 'colored',
      format : "image/png",
      version : "1.1.0",
      exceptions : 'application/vnd.ogc.se_xml',
      customdem : 'minLevel,maxLevel,T'
    },{
      transitionEffect : 'resize'
    });
    map.addLayers([HakoonBathymetryLayer, layerHakoon, layerHakoon2, layerHakoon2b]);
    buster.assert(true);
  },

  tearDown:function () {
    //$("#text-widget").remove();
  }
})



