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
 * Copyright 2003 - 2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package org.rasdaman.migration.domain.legacy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for handling BigDecimals computations and scales.
 * @author <a href="mailto:p.campalani@jacobs-university.de">Piero Campalani</a>
 */
public class LegacyBigDecimalUtil {

    private static final Logger log = LoggerFactory.getLogger(LegacyBigDecimalUtil.class);
    /**
     * Scale of a quotient between two BigDecimals.
     *
     * Java API on BigDecimal:
     *   Immutable, arbitrary-precision signed decimal numbers.
     *   A BigDecimal consists of an arbitrary precision integer unscaled value and a 32-bit integer scale.
     *   If zero or positive, the scale is the number of digits to the right of the decimal point.
     *   If negative, the unscaled value of the number is multiplied by ten to the power of the negation of the scale.
     *   The value of the number represented by the BigDecimal is therefore (unscaledValue × 10-scale).
     */
    private static final int DIVISION_SCALE = 50;

    /**
     * Converts BigDecimal value minimum scale equivalent representation.
     * Java bug #6480539 is taken into account.
     * @param bd
     * @return bd where decimal zeros have been stripped.
     */
    public static BigDecimal stripDecimalZeros(BigDecimal bd) {
        BigDecimal bdOut = bd.stripTrailingZeros();
        try {
            if (bdOut.scale() < 0) {
                bdOut = new BigDecimal(bdOut.longValueExact());
            }
            if (bdOut.compareTo(BigDecimal.ZERO) == 0) {
                bdOut = BigDecimal.ZERO;
            }
        } catch (ArithmeticException ex) {
            log.trace(bd + " exceeds the capacity of long integers: leaving its own representation.");
        }
        return bdOut;
    }

    /**
     * Computes the division between two BigDecimals with the specified max significant figures.
     * Setting a scale cuts the precision of the quotient to that number of significant figures
     * (negligible when high scale value), but avoids dangerous truncations which are dynamically
     * set depending on the scale of divisor and dividend.
     * Setting no RoundingMode on the other hand can throw exception since exact precision is assumed
     * but sometimes cannot be achieved (eg 1/3).
     * @param dividend
     * @param divisor
     * @return Zeros-stripped quotient of dividend/divisor division.
     */
    public static BigDecimal divide(BigDecimal dividend, BigDecimal divisor) {
        BigDecimal quotient = dividend.divide(divisor, DIVISION_SCALE, RoundingMode.UP);
        return LegacyBigDecimalUtil.stripDecimalZeros(quotient);
    }

    /**
     * Computes the multiply between two BigDecimals number
     * @param numberA
     * @param numberB
     * @return
     */
    public static BigDecimal multiple(BigDecimal numberA, BigDecimal numberB) {
        BigDecimal result = numberA.multiply(numberB);
        return LegacyBigDecimalUtil.stripDecimalZeros(result);
    }

    public static int listContains(List<BigDecimal> list, BigDecimal value) {
        int counter = 0;
        for (BigDecimal element : list) {
            if (element.compareTo(value) == 0) {
                return counter;
            }
            counter++;
        }
        return -1;
    }
}
