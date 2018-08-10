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
 * Testing Suite for Text widgets
 * @see Rj.widget.Text
 * @author Alex Dumitru <m.dumitru@jacobs-university.de>
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @version 3.0.0
 */

buster.testCase("Rj.util.DataSeries tests", {
  setUp:function () {
  },

  "DataSeries ok":function () {
    var fDataSeries = new Rj.util.DataSeries([[1,2],[3,4]]);
    var sDataSeries = new Rj.util.DataSeries([1,2,3,4]);
    fDataSeries.addListener("myapp", "namechanged", function(name){
      console.log("new name is: ", name);
    });
    fDataSeries.addListener("myapp", "colorchanged", function(color){
      console.log("new color is: ", color);
    });
    fDataSeries.setColor("blue");
    fDataSeries.setName("gogu");
    console.log(fDataSeries, sDataSeries);
    buster.assert(true);
  },

  tearDown:function () {
  }
})



