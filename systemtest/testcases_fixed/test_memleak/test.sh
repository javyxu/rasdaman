#!/bin/bash
#!/bin/ksh
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
#
# SYNOPSIS
#	test.sh
# Description
#	Command-line utility for testing postgres connection leak
#       if we get "rasdaman error 801: RasManager Error: System overloaded, please try again later" or "rasdaman error 206" when we run a rasql it means memleak.
#
# PRECONDITIONS
# 	1)Postgres Server must be running
# 	2)Rasdaman Server must be running
# 	3)database RASBASE must exists
# 	4)rasql utility must be fully running
# Usage: ./test.sh 
#        
# CHANGE HISTORY
#       2010-10-31     J.Yu       created
#

PROG=`basename $0`

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ] ; do SOURCE="$(readlink "$SOURCE")"; done
SCRIPT_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

. "$SCRIPT_DIR"/../../util/common.sh

# ------------------------------------------------------------------------------
# test dependencies
#
check_postgres
check_rasdaman

# ------------------------------------------------------------------------------
# check leak by rasql 
#
$RASQL -q "select r from RAS_COLLECTIONNAMES as r" --out string 2>&1 | grep --quiet -w -E "rasdaman error 801|rasdaman error 206"
if [ $? -eq 0 ]; then
   log "memleak"
   exit $RC_ERROR
else
   log "memleak test pass"
   exit $RC_OK
fi
  
