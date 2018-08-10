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
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with rasdaman community. If not, see <http://www.gnu.org/licenses/>.
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
#	Command-line utility for testing rasdaman.
#	1)creating collection
#	2)insert MDD into TEST_COLLECTION
#	3)update the MDD
#	4)delete MDD
#	5)drop TEST_COLLECTION
#
# PRECONDITIONS
# 	1)Postgres Server must be running
# 	2)Rasdaman Server must be running
# 	3)database RASBASE must exists
# 	4)rasql utility must be fully running
# Usage: ./test.sh
#
# CHANGE HISTORY
#    2009-Sep-16   J.Yu    created
#    2010-July-04  J.Yu    add precondition


# Variables
PROG=`basename $0`

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ] ; do SOURCE="$(readlink "$SOURCE")"; done
SCRIPT_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

. "$SCRIPT_DIR"/../../util/common.sh

TEST_COLLECTION="test_tmp"
TMP_COLLECTION="test_tmp_select_into"
TEST_TARGET=test_up_tgt
TEST_SOURCE=test_up_src

# ------------------------------------------------------------------------------
# test dependencies
#
#check_postgres
check_rasdaman

# check data types
check_type GreySet


# ------------------------------------------------------------------------------
# drop test collection if they already exists
#
logn "test initialization..."
drop_colls $TEST_COLLECTION
feedback

# ------------------------------------------------------------------------------
# start test
#
create_coll $TEST_COLLECTION GreySet


logn "inserting MDD into collection... "
$RASQL --quiet -q "insert into $TEST_COLLECTION values marray x in [0:255, 0:210] values 1c"
check

# ------------------------------------------------------------------------------

logn "updating MDD from collection... "
$RASQL --quiet -q "update $TEST_COLLECTION as a set a assign a[0:179,0:54] + 1c"
check

# ------------------------------------------------------------------------------

logn "testing SELECT INTO a new collection... "
$RASQL --quiet -q "select c / 2 into $TMP_COLLECTION from $TEST_COLLECTION as c"
check

sdom1=`$RASQL -q "select sdom(c) from $TMP_COLLECTION as c" --out string`
sdom2=`$RASQL -q "select sdom(c) from $TEST_COLLECTION as c" --out string`
check_result "$sdom1" "$sdom2" "testing select into"

# ------------------------------------------------------------------------------

# insert another object, so we test deleting all objects from one collection
$RASQL --quiet -q "select c / 2 into $TMP_COLLECTION from $TEST_COLLECTION as c"

logn "delete all MDDs from a collection... "
$RASQL --quiet -q "delete from $TMP_COLLECTION"
check

sdom=$($RASQL --quiet -q "select sdom(c) from $TMP_COLLECTION as c" --out string)
check_result "" "$sdom" "deleting all objects from a collection"

# ------------------------------------------------------------------------------

mdd_type=NullValueArrayTest2
set_type=NullValueSetTest2

# check data types and insert if not available
check_user_type $set_type
if [ $? -ne 0 ]; then
    $RASQL --quiet -q "create type $mdd_type as char mdarray [ x, y ]" > /dev/null | tee -a $LOG
    $RASQL --quiet -q "create type $set_type as set ( $mdd_type null values [1] )" > /dev/null | tee -a $LOG
fi

TEST_NULL=test_null
TEST_NULL_INTO=test_null_into
drop_colls $TEST_NULL
drop_colls $TEST_NULL_INTO
create_coll $TEST_NULL $set_type

$RASQL --quiet -q "insert into $TEST_NULL values marray x in [0:3,0:3] values (char)(x[0] + x[1] + 1)"
$RASQL --quiet -q "select c - 2c into $TEST_NULL_INTO from $TEST_NULL as c"
result=$($RASQL -q "select add_cells(c) from $TEST_NULL_INTO as c" --out string | grep 'Result ' | awk '{ print $4 }')
exp_result="34"
check_result $exp_result $result "select into with null value transfer"

drop_colls $TEST_NULL
drop_colls $TEST_NULL_INTO
drop_types $set_type $mdd_type

# ------------------------------------------------------------------------------

logn "dropping collection $TMP_COLLECTION... "
$RASQL --quiet -q "drop collection $TMP_COLLECTION"
check

# ------------------------------------------------------------------------------

logn "deleting MDD from collection... "
$RASQL --quiet -q "delete from $TEST_COLLECTION as a where all_cells(a>0)"
check

# ------------------------------------------------------------------------------

logn "dropping collection $TEST_COLLECTION... "
$RASQL --quiet -q "drop collection $TEST_COLLECTION"
check

# ------------------------------------------------------------------------------
# test if rasdaman throws an error when importing array with wrong type
# ------------------------------------------------------------------------------

TEST_COLLECTION=test_rgb_wrong
create_coll $TEST_COLLECTION GreySet

$RASQL --quiet -q "insert into $TEST_COLLECTION values decode(\$1)" -f $SCRIPT_DIR/testdata/rgb.png 2>&1 | grep -F -q "959"
check_result 0 $? "inserting a wrong type... "

drop_colls $TEST_COLLECTION

# ------------------------------------------------------------------------------

TEST_COLLECTION=test_insert
drop_colls $TEST_COLLECTION
logn "create collection $TEST_COLLECTION... "
$RASQL --quiet -q "create collection $TEST_COLLECTION GreySet"
check

logn "dropping collection $TEST_COLLECTION... "
$RASQL --quiet -q "drop collection $TEST_COLLECTION"
check

TEST_COLLECTION=test_select
drop_colls $TEST_COLLECTION
logn "create collection $TEST_COLLECTION... "
$RASQL --quiet -q "create collection $TEST_COLLECTION GreySet"
check

logn "dropping collection $TEST_COLLECTION... "
$RASQL --quiet -q "drop collection $TEST_COLLECTION"
check

# ------------------------------------------------------------------------------
# test updates with null values
# ------------------------------------------------------------------------------

log ""
log "testing updates with null values attached to source array (char)"

TEST_COLLECTION=test_update_nulls_greyset
drop_colls $TEST_COLLECTION
logn "  create collection $TEST_COLLECTION... "
$RASQL --quiet -q "create collection $TEST_COLLECTION GreySet"
check

logn "  inserting testdata... "
$RASQL --quiet -q "insert into $TEST_COLLECTION values <[0:1,0:1] 0c, 1c; 2c, 3c>"
check

res=$($RASQL -q "select add_cells(c) from $TEST_COLLECTION as c" --out string | grep Result | awk '{ print $4; }')
check_result "6" "$res" "  checking testdata"

logn "  updating testdata with null values... "
$RASQL --quiet -q "update $TEST_COLLECTION as c set c[0:1,0:1] assign <[0:1,0:1] 6c, 0c; 1c, 7c> null values [6:7]"
check

# current array: <[0:1,0:1] 0c, 0c; 1c, 3c>
res=$($RASQL -q "select add_cells(c) from $TEST_COLLECTION as c" --out string | grep Result | awk '{ print $4; }')
check_result "4" "$res" "  checking updated testdata"

logn "  dropping collection $TEST_COLLECTION... "
$RASQL --quiet -q "drop collection $TEST_COLLECTION"
check


log ""
log "testing updates with null values attached to source array (rgb)"

TEST_COLLECTION=test_update_nulls_rgb
drop_colls $TEST_COLLECTION
logn "  create collection $TEST_COLLECTION... "
$RASQL --quiet -q "create collection $TEST_COLLECTION RGBSet"
check

logn "  inserting testdata... "
$RASQL --quiet -q "insert into $TEST_COLLECTION values <[0:1,0:1] {1c, 1c, 1c}, {0c, 1c, 2c}; {1c, 2c, 3c}, {2c, 2c, 2c}>"
check

res=$($RASQL -q "select add_cells(c) from $TEST_COLLECTION as c" --out string | grep Result | sed 's/.*: //')
check_result "{ 4, 6, 8 }" "$res" "  checking testdata"

logn "  updating testdata with null values... "
$RASQL --quiet -q "update $TEST_COLLECTION as c set c[0:1,0:1] assign <[0:1,0:1] {4c, 5c, 6c}, {1c, 1c, 1c}; {2c, 2c, 2c}, {0c, 12c, 6c}> null values [1:2]"
check

# current array: <[0:1,0:1] {4c, 5c, 6c}, {0c, 1c, 2c}; {1c, 2c, 3c}, {0c, 12c, 6c}>
res=$($RASQL -q "select add_cells(c) from $TEST_COLLECTION as c" --out string | grep Result | sed 's/.*: //')
check_result "{ 5, 20, 17 }" "$res" "  checking updated testdata"

logn "  dropping collection $TEST_COLLECTION... "
$RASQL --quiet -q "drop collection $TEST_COLLECTION"
check

# ------------------------------------------------------------------------------

TEST_COLLECTION=test123123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789
drop_colls $TEST_COLLECTION

$RASQL --quiet -q "create collection $TEST_COLLECTION GreySet" 2>&1 | grep -F -q "974"
check_result 0 $? "create collection with name longer than 200 characters... "

$RASQL --quiet -q "drop collection $TEST_COLLECTION" 2>&1 | grep -F -q "1013"
check_result 0 $? "dropping collection with name longer than 200 characters... "

# ------------------------------------------------------------------------------
# tests for updating with the FROM clause

#drop collections, if they exist
drop_colls $TEST_TARGET $TEST_SOURCE

#create collections
create_coll $TEST_TARGET GreySet
create_coll $TEST_SOURCE GreySet
$RASQL --quiet -q "insert into $TEST_TARGET values marray x in [0:299, 0:299] values 0c"
$RASQL --quiet -q "insert into $TEST_SOURCE values marray x in [0:299, 0:299] values 1c"

logn "testing UPDATE FROM at a single point on two distinct collections... "
$RASQL --quiet -q "UPDATE $TEST_TARGET AS t SET t[0:0,0:0] ASSIGN s[0:0,0:0] FROM $TEST_SOURCE as s"
result=$($RASQL -q "select c[0,0] from $TEST_TARGET as c" --out string | grep 'Result' | awk '{ print $4 }')
exp_result="1"
check_result $exp_result $result 

logn "testing UPDATE ... FROM on non-overlapping domains from a single collection... "
$RASQL --quiet -q "UPDATE $TEST_TARGET AS a SET a[0:9,0:9] ASSIGN shift(b[0:9,10:19], [0,-10])+1c FROM $TEST_TARGET AS b"
result=$($RASQL -q "select c[5,5] from $TEST_TARGET as c" --out string | grep 'Result' | awk '{ print $4 }')
exp_result="1"
check_result $exp_result $result 

logn "testing UPDATE ... FROM ... WHERE on non-overlapping domains from a single collection... "
$RASQL --quiet -q "UPDATE $TEST_TARGET AS a SET a[100:109,100:109] ASSIGN shift(b[100:109,110:119], [0,-10])+2c FROM $TEST_TARGET AS b WHERE ( 0 = 0 )"
result=$($RASQL -q "select c[105,105] from $TEST_TARGET as c" --out string | grep 'Result' | awk '{ print $4 }')
exp_result="2"
check_result $exp_result $result 

logn "testing UPDATE ... FROM on overlapping domains from a single collection... "
$RASQL --quiet -q "UPDATE $TEST_TARGET AS a SET a[0:9,20:29] ASSIGN b[0:9,20:29]+3c FROM $TEST_TARGET AS b"
result=$($RASQL -q "select c[5,25] from $TEST_TARGET as c" --out string | grep 'Result' | awk '{ print $4 }')
exp_result="3"
check_result $exp_result $result 

logn "testing UPDATE ... FROM ... WHERE on overlapping domains from a single collection... "
$RASQL --quiet -q "UPDATE $TEST_TARGET AS a SET a[100:109,120:129] ASSIGN b[100:109,120:129]+4c FROM $TEST_TARGET AS b WHERE ( 0 = 0 )"
result=$($RASQL -q "select c[105,125] from $TEST_TARGET as c" --out string | grep 'Result' | awk '{ print $4 }')
exp_result="4"
check_result $exp_result $result 

logn "testing UPDATE ... FROM on non-overlapping grid domains from two distinct collections... "
$RASQL --quiet -q "UPDATE $TEST_TARGET AS a SET a[10:19,0:9] ASSIGN shift(b[10:19,10:19], [0,-10]) +7c FROM $TEST_SOURCE AS b"
result=$($RASQL -q "select c[15,5] from $TEST_TARGET as c" --out string | grep 'Result' | awk '{ print $4 }')
exp_result="8"
check_result $exp_result $result 

logn "testing UPDATE ... FROM ... WHERE on non-overlapping grid domains from two distinct collections... "
$RASQL --quiet -q "UPDATE $TEST_TARGET AS a SET a[110:119,100:109] ASSIGN shift(b[110:119,110:119], [0,-10]) +8c FROM $TEST_SOURCE AS b WHERE ( 0 = 0 )"
result=$($RASQL -q "select c[115,105] from $TEST_TARGET as c" --out string | grep 'Result' | awk '{ print $4 }')
exp_result="9"
check_result $exp_result $result 

logn "testing UPDATE ... FROM on overlapping grid domains from two distinct collections... "
$RASQL --quiet -q "UPDATE $TEST_TARGET AS a SET a[10:19,10:19] ASSIGN b[10:19,10:19] +9c FROM $TEST_SOURCE AS b"
result=$($RASQL -q "select c[15,15] from $TEST_TARGET as c" --out string | grep 'Result' | awk '{ print $4 }')
exp_result="10"
check_result $exp_result $result 

logn "testing UPDATE ... FROM ... WHERE on overlapping grid domains from two distinct collections... "
$RASQL --quiet -q "UPDATE $TEST_TARGET AS a SET a[110:119,110:119] ASSIGN b[110:119,110:119] +10c FROM $TEST_SOURCE AS b WHERE ( 0 = 0 )"
result=$($RASQL -q "select c[115,115] from $TEST_TARGET as c" --out string | grep 'Result' | awk '{ print $4 }')
exp_result="11"
check_result $exp_result $result 

logn "testing UPDATE ... FROM on two distinct collections, where the collection described in the FROM clause appears in the SET clause... "
$RASQL --quiet -q "UPDATE $TEST_TARGET AS a SET b[10:19,10:19] ASSIGN b[10:19,10:19] +11c FROM $TEST_SOURCE AS b"
result=$($RASQL -q "select c[15,15] from $TEST_SOURCE as c" --out string | grep 'Result' | awk '{ print $4 }')
exp_result="12"
check_result $exp_result $result 

logn "testing UPDATE ... FROM ... WHERE on two distinct collections, where the collection described in the FROM clause appears in the SET clause... "
$RASQL --quiet -q "UPDATE $TEST_TARGET AS a SET b[120:129,110:119] ASSIGN b[120:129,110:119] +12c FROM $TEST_SOURCE AS b WHERE ( 0 = 0 )"
result=$($RASQL -q "select c[125,115] from $TEST_SOURCE as c" --out string | grep 'Result' | awk '{ print $4 }')
exp_result="13"
check_result $exp_result $result 

logn "testing UPDATE ... FROM ... WHERE on two distinct collections, with the WHERE clause a function of the FROM clause... "
$RASQL --quiet -q "UPDATE $TEST_TARGET AS a SET a[130:139,110:119] ASSIGN b[130:139,110:119] +13c FROM $TEST_SOURCE AS b WHERE some_cells( b = 1 )"
result=$($RASQL -q "select c[135,115] from $TEST_TARGET as c" --out string | grep 'Result' | awk '{ print $4 }')
exp_result="14"
check_result $exp_result $result 

logn "testing UPDATE ... FROM ... WHERE on two distinct collections, with the WHERE clause a function of the UPDATE clause... "
$RASQL --quiet -q "UPDATE $TEST_TARGET AS a SET a[140:149,110:119] ASSIGN b[140:149,110:119] +14c FROM $TEST_SOURCE AS b WHERE some_cells( a = 0 )"
result=$($RASQL -q "select c[145,115] from $TEST_TARGET as c" --out string | grep 'Result' | awk '{ print $4 }')
exp_result="15"
check_result $exp_result $result 

logn "testing UPDATE ... FROM ... WHERE on two distinct collections, with the WHERE clause a function of both the FROM clause and the UPDATE clause... "
$RASQL --quiet -q "UPDATE $TEST_TARGET AS a SET a[150:159,110:119] ASSIGN b[150:159,110:119] +15c FROM $TEST_SOURCE AS b WHERE some_cells( a = b )"
result=$($RASQL -q "select c[155,115] from $TEST_TARGET as c" --out string | grep 'Result' | awk '{ print $4 }')
exp_result="16"
check_result $exp_result $result 

logn "testing UPDATE ... FROM ... WHERE on two distinct collections, with the WHERE clause returning FALSE... "
$RASQL --quiet -q "UPDATE $TEST_TARGET AS a SET a[200:205,200:205] ASSIGN b[200:205,200:205] FROM $TEST_SOURCE as b WHERE some_cells( b = 0 )"
result=$($RASQL -q "select c[205,205] from $TEST_TARGET as c" --out string | grep 'Result' | awk '{ print $4 }')
exp_result="0"
check_result $exp_result $result

logn "testing UPDATE ... FROM where the FROM clause consists of a list of two collections... "
$RASQL --quiet -q "UPDATE $TEST_TARGET AS a SET a[210:215,210:215] ASSIGN b[210:215,210:215] + c[210:215,210:215] FROM $TEST_SOURCE as b, $TEST_SOURCE as c"
result=$($RASQL -q "select c[212,212] from $TEST_TARGET as c" --out string | grep 'Result' | awk '{ print $4 }')
exp_result="2"
check_result $exp_result $result

#drop data so test can be rerun safely
drop_colls $TEST_TARGET $TEST_SOURCE
# end of tests for performing an UPDATE with the FROM clause

# ------------------------------------------------------------------------------
# test summary
#
print_summary
exit $RC
