#!/bin/bash
# This file is part of rasdaman community.
#
# Rasdaman community is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# Rasdaman community is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.    See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with rasdaman community.    If not, see <http://www.gnu.org/licenses/>.
#
# Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014, 2015 Peter Baumann /
# rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.
#
# SYNOPSIS
#    test.sh
# Description
#    Check if SECORE can handle multiple requests in short time
#
################################################################################
PROG=$( basename $0 )

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ] ; do SOURCE="$(readlink "$SOURCE")"; done
SCRIPT_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

echo $SCRIPT_DIR
. "$SCRIPT_DIR"/../../../util/common.sh

# array to store process ids
pids=""

OUTPUT_FOLDER="output"
mkdir "$OUTPUT_FOLDER"

log "Test SECORE queries's response time..."
for i in {1..100}
do
   #echo "test $i time..."
   wget -q "$SECORE_URL" -O "$OUTPUT_FOLDER/secore$i.txt" &
   # add current process pid to an array, then later it can finish the script when all processes are stopped.
   pids="$pids $!"    
done

wait $pids


result=true
fileContent=""
log "All queries finished. Checking the result files."
for f in "output/*"; do
   fileContent=`cat $f`
   if [[ "$fileContent" != *"ellipsoid"* ]]; then
      result=false
      break
   fi
done

# clean the output file
rm -rf "$OUTPUT_FOLDER"
log "Done."

# check the test result
if [[ $result == true ]]; then
    check_result "ok" "ok" "SECORE responses in accepted time."
else 
    check_result "ok" "failed" "SECORE return exception: $fileContent"
fi

# print summary from util/common.sh
print_summary
exit $RC
