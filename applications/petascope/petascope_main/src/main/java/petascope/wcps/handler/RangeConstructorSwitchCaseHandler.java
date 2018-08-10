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
package petascope.wcps.handler;

import org.apache.commons.lang3.StringUtils;
import petascope.wcps.result.WcpsResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.rasdaman.domain.cis.NilValue;
import org.springframework.stereotype.Service;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.metadata.model.RangeField;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;

/**
 * Translation class for the range constructor expressions which is used inside
 * switch-case expression  <code>
 * switch
 * case c > 1000 return """{red: 107; green:17; blue:68}"""
 * default return {red: 150; green:103; blue:14}
 * </code> returns  <code>
 * ((107) * {1c,0c,0c,0c} + (17) * {0c,1c,0c,0c} + (68) * {0c,0c,1c})
 * </code>
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.de">Bang Pham Huu</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
@Service
public class RangeConstructorSwitchCaseHandler extends AbstractOperatorHandler {

    public WcpsResult handle(Map<String, WcpsResult> fieldStructure) {
        List<String> translatedFields = new ArrayList();
        // {red: 100, green: 100, blue: 20}
        int i = 0;
        int maxRange = fieldStructure.size();

        List<RangeField> rangeFields = new ArrayList<>();
        for (Map.Entry<String, WcpsResult> entry : fieldStructure.entrySet()) {
            String scalarValue = entry.getValue().getRasql();
            String scalarRange = getScalarRange(i, maxRange);

            String result = TEMPLATE.replace("$scalarValue", scalarValue)
                    .replace("$scalarRange", scalarRange);
            translatedFields.add(result);

            // we create range field for the coverage metadata
            RangeField rangeField = new RangeField(RangeField.DATA_TYPE, entry.getKey(), "", new ArrayList<NilValue>(),
                    RangeField.UOM_CODE, "", null);
            rangeFields.add(rangeField);
            i++;
        }

        List<NilValue> nilValues = new ArrayList<>();

        //for now no metadata is forwarded, but it can be constructed from the fields (we need this to set extrametadata with netcdf)
        WcpsCoverageMetadata metadata = new WcpsCoverageMetadata(null, null, null, new ArrayList<Axis>(), "", rangeFields, nilValues, "");
        String rasql = StringUtils.join(translatedFields, " + ");
        return new WcpsResult(metadata, rasql);
    }

    /**
     * Create range constant (e.g: {1c,0c,0c,0c} according to the index
     *
     * @param index
     * @param maxRange
     * @return
     */
    private String getScalarRange(int index, int maxRange) {
        List<String> tmp = new ArrayList<String>();
        for (int i = 0; i < maxRange; i++) {
            if (i == index) {
                tmp.add("1c");
            } else {
                tmp.add("0c");
            }
        }
        String result = StringUtils.join(tmp, ",");
        return result;
    }

    // e.g: (107) * {1c,0c,0c,0c}
    private final String TEMPLATE = "($scalarValue) * { $scalarRange }";
}
