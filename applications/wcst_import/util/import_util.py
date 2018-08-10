"""
 *
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
 * Copyright 2003 - 2015 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 *
"""
from master.error.runtime_exception import RuntimeException

"""
  Utilities to import optional dependencies and throw proper exceptions for user to install these missing libraries. 
"""


def import_pygrib():
    """
    Import pygrib which is used for importing GRIB file.
    """
    try:
        import pygrib
    except ImportError:
        raise RuntimeException("Cannot import GRIB data, please install pygrib first (sudo pip install pygrib).")

    return pygrib


def import_netcdf4():
    """
    Import netCDF4 which is used for importing netCDF file.
    """
    try:
        import netCDF4
    except ImportError:
        raise RuntimeException("Cannot import netCDF data, please install netCDF4 first \
                                (yum install netcdf4-python, or apt-get install python-netcdf, or apt-get install python-netcdf4).")

    return netCDF4
