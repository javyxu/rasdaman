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

from master.evaluator.evaluator_slice import EvaluatorSlice
from master.evaluator.sentence_evaluator import SentenceEvaluator
from master.extra_metadata.extra_metadata import ExtraMetadata
from master.extra_metadata.extra_metadata_ingredient_information import ExtraMetadataIngredientInformation
from master.extra_metadata.extra_metadata_slice import ExtraMetadataSlice


class ExtraMetadataEntry:
    def __init__(self, evalutor_slice, slice_subset):
        """
        Defines an input element for the extra metadata collector
        :param EvaluatorSlice evalutor_slice: the slice on which sentences will be evaluated
        :param list[ExtraMetadataSliceSubset] slice_subset: the spatio-temporal position of the slice
        """
        self.slice_subset = slice_subset
        self.evalutor_slice = evalutor_slice


class ExtraMetadataCollector:
    def __init__(self, evaluator, extra_metadata_info, metadata_entries):
        """
        The metadata collector provides functionality for extracting metadata according to a user defined metadata description
        from a dataset of slices
        :param SentenceEvaluator evaluator : a list of expression evaluators that should be used to parse the given expression
        :param ExtraMetadataIngredientInformation extra_metadata_info: the extra metadata information that the user wants to harvest
        :param list[ExtraMetadataEntry] metadata_entries: a list of slices from which the evaluator can extract the information request in extra_metadata_info
        :return:
        """
        self.evaluator = evaluator
        self.extra_metadata_info = extra_metadata_info
        self.metadata_entries = metadata_entries

    def collect(self):
        """
        Collects the metadata supplied in the constructor
        :rtype: ExtraMetadata
        """
        global_meta = {}
        for key, value in self.extra_metadata_info.global_attributes.items():
            # if value is empty (e.g: metadata "time_of_coverage": "") then should not evaluate this value
            # output of extra metadata should be string in any cases
            if str(value) != "":
                global_meta[key] = str(self.evaluator.evaluate(value, self.metadata_entries[0].evalutor_slice))
            else:
                global_meta[key] = str(value)

        meta_slices = []
        # if we have local metadata go ahead
        if len(self.extra_metadata_info.slice_attributes.items()) > 0:
            for metadata_entry in self.metadata_entries:
                meta_values = {}
                for meta_key, sentence in self.extra_metadata_info.slice_attributes.items():
                    # output of extra metadata should be string in any cases
                    meta_values[meta_key] = str(self.evaluator.evaluate(sentence, metadata_entry.evalutor_slice))
                meta_slices.append(ExtraMetadataSlice(metadata_entry.slice_subset, meta_values))

        # NOTE: this bands's metadata is added to gmlcov:metadata not swe:field
        bands_meta = {}
        # band_attributes is a dict of keys, values
        for band, band_attributes in self.extra_metadata_info.bands_attributes.items():
            bands_meta[band] = {}
            for key, value in band_attributes.items():
                # if value is empty (e.g: metadata "time_of_coverage": "") then should not evaluate this value
                # output of extra metadata should be string in any cases
                if str(value) != "":
                    bands_meta[band][key] = str(self.evaluator.evaluate(value, self.metadata_entries[0].evalutor_slice))
                else:
                    bands_meta[band][key] = str(value)

        # Axes metadata (dimension's metadata)
        axes_meta = {}
        # axes_attributes is a dict of keys, values
        for axis, axis_attributes in self.extra_metadata_info.axes_attributes.items():
            axes_meta[axis] = {}
            if type(axis_attributes) is dict:
                for key, value in axis_attributes.items():
                    # if value is empty (e.g: metadata "time_of_coverage": "") then should not evaluate this value
                    # output of extra metadata should be string in any cases
                    if str(value) != "":
                        axes_meta[axis][key] = str(self.evaluator.evaluate(value, self.metadata_entries[0].evalutor_slice))
                    else:
                        axes_meta[axis][key] = str(value)
            else:
                # It should be a string (e.g: ${netcdf:variable:lat:metadata}) and need to be evaluated
                axes_meta[axis] = self.evaluator.evaluate(axis_attributes, self.metadata_entries[0].evalutor_slice)

        return ExtraMetadata(global_meta, meta_slices, bands_meta, axes_meta)
