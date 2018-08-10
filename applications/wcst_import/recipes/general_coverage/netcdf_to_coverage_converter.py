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
import decimal
from lib import arrow
from master.helper.point_pixel_adjuster import PointPixelAdjuster
from master.error.runtime_exception import RuntimeException
from master.evaluator.evaluator_slice import NetcdfEvaluatorSlice
from master.evaluator.sentence_evaluator import SentenceEvaluator
from master.helper.regular_user_axis import RegularUserAxis
from master.helper.user_axis import UserAxis, UserAxisType
from master.helper.user_band import UserBand
from master.importer.axis_subset import AxisSubset
from master.importer.interval import Interval
from master.provider.metadata.coverage_axis import CoverageAxis
from master.provider.metadata.grid_axis import GridAxis
from master.provider.metadata.irregular_axis import IrregularAxis
from master.provider.metadata.regular_axis import RegularAxis
from recipes.general_coverage.abstract_to_coverage_converter import AbstractToCoverageConverter
from util.crs_util import CRSAxis
from util.file_obj import File
from util.gdal_util import GDALGmlUtil
from util.import_util import import_netcdf4
import numpy
numpy.set_printoptions(numpy.inf)


class NetcdfToCoverageConverter(AbstractToCoverageConverter):
    RECIPE_TYPE = "netcdf"

    def __init__(self, recipe_type, sentence_evaluator, coverage_id, bands, files, crs, user_axes, tiling,
                 global_metadata_fields, local_metadata_fields, bands_metadata_fields,
                 axes_metadata_fields,
                 metadata_type,
                 grid_coverage, pixel_is_point):
        """
        Converts a netcdf list of files to a coverage
        :param recipe_type: the type of recipe
        :param SentenceEvaluator sentence_evaluator: the evaluator for wcst sentences
        :param str coverage_id: the id of the coverage
        :param list[UserBand] bands: the name of the coverage band
        :param list[File] files: a list of netcdf files
        :param str crs: the crs of the coverage
        :param list[UserAxis] user_axes: a list with user axes
        :param str tiling: the tiling string to be passed to wcst
        :param dict global_metadata_fields: the global metadata fields
        :param dict local_metadata_fields: the local metadata fields
        :param dict bands_metadata_fields: the bands metadata fields
        :param dict axes_metadata_fields: the axes metadata fields
        :param str metadata_type: the metadata type
        :param boolean grid_coverage: check if user want to import as grid coverage
        :param boolean pixel_is_point: check if netCDF should be adjusted by +/- 0.5 * resolution for each regular axes
        """
        AbstractToCoverageConverter.__init__(self, recipe_type, sentence_evaluator)
        self.sentence_evaluator = sentence_evaluator
        self.coverage_id = coverage_id
        self.bands = bands
        self.files = files
        self.crs = crs
        self.user_axes = user_axes
        self.tiling = tiling
        self.global_metadata_fields = global_metadata_fields
        self.local_metadata_fields = local_metadata_fields
        self.bands_metadata_fields = bands_metadata_fields
        self.axes_metadata_fields = axes_metadata_fields
        self.metadata_type = metadata_type
        self.grid_coverage = grid_coverage
        self.pixel_is_point = pixel_is_point

    def _data_type(self):
        """
        Returns the data type for this netcdf dataset
        :rtype: str
        """
        if len(self.files) < 1:
            raise RuntimeException("No files to import were specified.")
        netCDF4 = import_netcdf4()
        nci = netCDF4.Dataset(self.files[0].get_filepath(), 'r')
        netcdf_data_type = nci.variables[self.bands[0].identifier].dtype.name

        return GDALGmlUtil.data_type_to_gdal_type(netcdf_data_type)

    def _file_band_nil_values(self, index):
        """
        This is used to get the null values (Only 1) from the given band index if one exists when nilValue was not defined
        in ingredient file
        :param integer index: the current band index to get the nilValues
        :rtype: List[RangeTypeNilValue] with only 1 element
        """
        if len(self.files) < 1:
            raise RuntimeException("No netcdf files given for import!")

        netCDF4 = import_netcdf4()
        # NOTE: all files should have same bands's metadata for each file
        nci = netCDF4.Dataset(self.files[0].get_filepath(), 'r')
        try:
            nil_value = nci.variables[self.bands[index].identifier].missing_value
        except AttributeError:
            # if file has no missing_value attribute of variable, then try with _FillValue
            try:
                nil_value = nci.variables[self.bands[index].identifier]._FillValue
            except AttributeError:
                # so variable does not have any null property
                nil_value = None

        if nil_value is None:
            return None
        else:
            return [nil_value]

    def _axis_subset(self, crs_axis, nc_file):
        """
        Returns an axis subset using the given crs axis in the context of the nc file
        :param CRSAxis crs_axis: the crs definition of the axis
        :param File nc_file: the netcdf file
        :rtype AxisSubset
        """
        user_axis = self._user_axis(self._get_user_axis_by_crs_axis_name(crs_axis.label), NetcdfEvaluatorSlice(nc_file))

        # Normally, without pixelIsPoint:true, in the ingredient needs to +/- 0.5 * resolution for each regular axis
        # e.g: resolution for axis E is 10000, then
        # "min": "${netcdf:variable:E:min} - 10000 / 2",
        # "max": "${netcdf:variable:E:max} + 10000 / 2",
        # with pixelIsPoint: true, no need to add these values as the service will do it automatically
        if self.pixel_is_point:
            PointPixelAdjuster.adjust_axis_bounds_to_continuous_space(user_axis, crs_axis)
        else:
            # No adjustment for all regular axes but still need to translate time in datetime to decimal to calculate
            if user_axis.type == UserAxisType.DATE:
                user_axis.interval.low = decimal.Decimal(str(arrow.get(user_axis.interval.low).float_timestamp))
                if user_axis.interval.high:
                    user_axis.interval.high = decimal.Decimal(str(arrow.get(user_axis.interval.high).float_timestamp))
            # if low < high, adjust it
            if user_axis.interval.high is not None and user_axis.interval.low > user_axis.interval.high:
                user_axis.interval.low, user_axis.interval.high = user_axis.interval.high, user_axis.interval.low

        high = user_axis.interval.high if user_axis.interval.high else user_axis.interval.low
        origin = PointPixelAdjuster.get_origin(user_axis, crs_axis)

        if isinstance(user_axis, RegularUserAxis):
            geo_axis = RegularAxis(crs_axis.label, crs_axis.uom, user_axis.interval.low, high, origin, crs_axis)
        else:
            if user_axis.type == UserAxisType.DATE:
                if crs_axis.is_uom_day():
                    coefficients = self._translate_day_date_direct_position_to_coefficients(user_axis.interval.low,
                                                                                            user_axis.directPositions)
                else:
                    coefficients = self._translate_seconds_date_direct_position_to_coefficients(user_axis.interval.low,
                                                                                                user_axis.directPositions)
            else:
                coefficients = self._translate_number_direct_position_to_coefficients(user_axis.interval.low,
                                                                                      user_axis.directPositions)
            geo_axis = IrregularAxis(crs_axis.label, crs_axis.uom, user_axis.interval.low, high, origin, coefficients, crs_axis)

        grid_low = 0
        grid_high = PointPixelAdjuster.get_grid_points(user_axis, crs_axis)

        # NOTE: Grid Coverage uses the direct intervals as in Rasdaman
        if self.grid_coverage is False and grid_high > grid_low:
            grid_high -= 1

        grid_axis = GridAxis(user_axis.order, crs_axis.label, user_axis.resolution, grid_low, grid_high)
        if user_axis.type == UserAxisType.DATE:
            self._translate_decimal_to_datetime(user_axis, geo_axis)

        return AxisSubset(CoverageAxis(geo_axis, grid_axis, user_axis.dataBound),
                          Interval(user_axis.interval.low, user_axis.interval.high))


