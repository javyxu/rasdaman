#!/bin/bash
#
# This file is part of rasdaman community.
#
# Rasdaman community is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# Rasdaman community is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
#
# Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann /
# rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.

. ../../../util/common.sh # load prepare_xml_file util
out="$1"
oracle="$2"

prepare_xml_file "$out"

# replace OID with 0 \(\) for group match
sed -r 's/(oid\(c\)=[0-9]*)/oid\(c\)=0/' "$out" > "$out".tmp

sort "$out".tmp > "$out".tmp2
sort "$oracle" > "$oracle".tmp2

# diff
diff -b "$out".tmp2 "$oracle".tmp2 > /dev/null 2>&1
rc=$?

# remove out file
rm -f "$out".tmp* "$oracle".tmp*

exit $rc
