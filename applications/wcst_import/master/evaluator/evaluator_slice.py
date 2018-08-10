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
from util.file_obj import File
from util.gdal_util import GDALGmlUtil
from util.import_util import import_pygrib, import_netcdf4


class EvaluatorSlice:
    def __init__(self):
        """
        Description of an evaluator slice. An evaluator slice is the data container on which a wcst expression can
        be evaluated upon. Such slices can be of several types, and each evaluator knows how to handle it internally
        """
        pass


class FileEvaluatorSlice(EvaluatorSlice):
    def __init__(self, file):
        """
        Initializes a file slice
        :param File file: the file associated of slice
        """
        EvaluatorSlice.__init__(self)
        self.file = file

    def get_file(self):
        """
        Returns the file of this slice
        :return:
        """
        return self.file


class GDALEvaluatorSlice(FileEvaluatorSlice):
    def __init__(self, gdal_file):
        """
        Returns a gdal backed slice
        :param gdal_file: the path to the gdal file
        """
        FileEvaluatorSlice.__init__(self, gdal_file)

    def get_dataset(self):
        """
        Returns the dataset of the file
        NOTE: gdal cannot open too many files (1989 files with error too many file opens)
        when getting dataset object and store inside list, so only open dataset when it is needed.
        :rtype: gdal Dataset
        """
        if isinstance(self.get_file(), GDALGmlUtil):
            return self.get_file()
        else:
            return GDALGmlUtil(self.get_file().filepath)


class GribMessageEvaluatorSlice(FileEvaluatorSlice):
    def __init__(self, grib_message, container_file, direct_positions=None):
        """
        A grib backed slice
        :param pygrib.gribmessage grib_message: the grib message of grib file which is used to evaluate variables
        :param File container_file: the file that contains the message
        :param list[values] direct_positions: the list of evaluated values (only used with directPositions for irregular axis)
               i.e: when all the messages were evaluated, now it can get the list of values [min,....max] and do some
               addition calculation if it is necessary
        """
        pygrib = import_pygrib()

        FileEvaluatorSlice.__init__(self, container_file)
        # if no grib_message is passed, we use the first grib message from the grib file to evaluate
        # e.g: global variables which does not change from message, grib:marsType, grib:marsClass
        if grib_message is None:
            dataset = pygrib.open(container_file.get_filepath())
            grib_message = dataset.message(1)
        self.grib_message = grib_message
        self.direct_positions = direct_positions

    def get_grib_message(self):
        """
        Returns the grib grib_message
        :rtype: pygrib.gribmessage
        """
        return self.grib_message

    def get_direct_positions(self):
        """
        Return the evaluated values for irregular axis in all evaluated messages of grib file
        :return list[values]:
        """
        return self.direct_positions


class NetcdfEvaluatorSlice(FileEvaluatorSlice):
    def __init__(self, netcdf_file):
        """
        Returns a netcdf backed slice
        :param netcdf_file: the path to the netcdf file
        """
        FileEvaluatorSlice.__init__(self, netcdf_file)

    def get_dataset(self):
        """
        Returns the dataset ofthe file
        :rtype: netCDF4.Dataset
        """
        netCDF4 = import_netcdf4()
        return netCDF4.Dataset(self.get_file().get_filepath(), "r")
