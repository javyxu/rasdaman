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
# Copyright 2003 - 2015 Peter Baumann / rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.
#

PROG=$(basename $0)

FILESTORAGE_TILES_SUBDIR="TILES"
FILESTORAGE_TILES_PER_DIR="16384"
FILESTORAGE_DIRS_PER_DIR="16384"

getNestedPath()
{
  local -r blobId="$1"
  local -r dir2Index=$(echo "scale=0; $blobId / $FILESTORAGE_TILES_PER_DIR" | bc)
  local -r dir1Index=$(echo "scale=0; $dir2Index / $FILESTORAGE_DIRS_PER_DIR" | bc)
  local -r nestedPath="$FILESTORAGE_TILES_SUBDIR/$dir1Index/$dir2Index"
  echo "$nestedPath"
}

[ -n "$RASDATA" ] || error "please export RASDATA to point to the rasdaman data directory"
[ -d "$RASDATA" ] || error "rasdaman data directory not found: $RASDATA"
[ -w "$RASDATA" -a -x "$RASDATA" ] || error "no write permissions to the rasdaman data directory: $RASDATA"

pushd "$RASDATA" > /dev/null

mkdir -p "$FILESTORAGE_TILES_SUBDIR"

echo
log "moving rasdaman blob files in $RASDATA to subdirectories of $FILESTORAGE_TILES_SUBDIR (this may take awhile)..."

for f in *; do
  [ -f "$f" ] || continue

  # check if the file is really a blob in RASBASE ($SQLITE is defined in update_db.sh)
  actualBlob=$($SQLITE "select BlobId from RAS_TILES where BlobId = $f" 2> /dev/null)

  if [ -n "$actualBlob" ]; then

    newPath=$(getNestedPath "$f")
    mkdir -p "$newPath"
    logn "  moving blob file $RASDATA/$f to $RASDATA/$newPath... "
    mv "$f" "$newPath"
    feedback

  fi

done

log "done."

popd > /dev/null
