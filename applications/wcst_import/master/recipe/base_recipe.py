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

from abc import ABCMeta, abstractmethod
import os
from config_manager import ConfigManager

from master.error.validate_exception import RecipeValidationException
from session import Session
from util.coverage_util import CoverageUtil
from util.log import log


class BaseRecipe:
    """
    This class represents an abstract
    """
    __metaclass__ = ABCMeta

    def __init__(self, session):
        """
        Initializes the recipe
        :param Session session: the session for the import tun
        """
        self.session = session

    def validate(self):
        """
        Validates the session and recipe parameters in order to ensure a correct run
        Recipes are encouraged to override this method and add further validation functionality
        based on their parameters
        """
        self.validate_base()

    @abstractmethod
    def describe(self):
        """
        This methods is called before insert or update is run. You should override the method and add any comments
        regarding the operations that you will perform via log.info to inform the user. You should explicitly state
        the information that you deduced (e.g. timestamps for a timeseries) so that the consequences are clear.
        """
        cov = CoverageUtil(self.session.get_coverage_id())
        operation_type = "UPDATE" if cov.exists() else "INSERT"
        log.info("The recipe has been validated and is ready to run.")
        log.info("\033[1mRecipe:\x1b[0m " + self.session.get_recipe()['name'])
        log.info("\033[1mCoverage:\x1b[0m " + self.session.get_coverage_id())
        log.info("\033[1mWCS Service:\x1b[0m " + ConfigManager.wcs_service)
        log.info("\033[1mOperation:\x1b[0m " + operation_type)
        log.info("\033[1mSubset Correction:\x1b[0m " + str(ConfigManager.subset_correction))
        log.info("\033[1mMocked:\x1b[0m " + str(ConfigManager.mock))
        if ConfigManager.track_files:
            log.info("\033[1mTrack files:\x1b[0m " + str(ConfigManager.track_files))
        if ConfigManager.skip:
            log.info("\033[1mSkip:\x1b[0m " + str(ConfigManager.skip))
        if ConfigManager.retry:
            log.info("\033[1mRetries:\x1b[0m " + str(ConfigManager.retries))
        if ConfigManager.slice_restriction is not None:
            log.info("\033[1mSlice Restriction:\x1b[0m " + str(ConfigManager.slice_restriction))
        pass

    @abstractmethod
    def status(self):
        """
        This method is called continuously to find out the status of the recipe. Use it to print information only
        when necessary and always return a tuple of form (numberOfItemsProcessed, numberOfTotalItems)
        :rtype (int, int)
        """
        pass

    @abstractmethod
    def ingest(self):
        """
        This method is called when the ingestion process is ready to be started. In thise method the developer should
        call the importer with the correct slices and start importing the given files
        """
        pass

    def run(self):
        """
        Runs the recipe
        """
        self.ingest()

    def validate_base(self, ignore_no_files=False):
        """
        Validates the configuration and the input files
        :param bool ignore_no_files: if the extending recipe does not work with files, set this to true to skip
        the validation check for no files
        """
        if self.session.get_wcs_service() is None or self.session.get_wcs_service() == "":
            raise RecipeValidationException("No valid wcs endpoint provided")
        if self.session.get_crs_resolver() is None or self.session.get_crs_resolver() == "":
            raise RecipeValidationException("No valid crs resolver provided")
        if self.session.get_coverage_id() is None or self.session.get_coverage_id() == "":
            raise RecipeValidationException("No valid coverage id provided")
        if ConfigManager.tmp_directory is None or (not os.access(ConfigManager.tmp_directory, os.W_OK)):
            raise RecipeValidationException("No valid tmp directory provided")
        if len(self.session.get_files()) == 0 and not ignore_no_files:
            raise RecipeValidationException("No files provided. Check that the paths you provided are correct.")
        for file in self.session.get_files():
            if not os.access(file.get_filepath(), os.R_OK):
                raise RecipeValidationException("File on path " + file.get_filepath() + " is not accessible")

    @staticmethod
    @abstractmethod
    def get_name():
        pass
