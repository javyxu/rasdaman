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
# Copyright 2003-2016 Peter Baumann /
# rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.
#
###################################################################
if (RMANRASNET)
    add_executable(rasnetserver_test EXCLUDE_FROM_ALL
            clientquerystreamedresulttest.cc main_test.cc)

    find_package(Threads REQUIRED)
    target_link_libraries(rasnetserver_test
            rasnetserver
            ${GMOCK_LIBRARY}
            ${CMAKE_THREAD_LIBS_INIT})

    add_test(rasnetserver_testrunner rasnetserver_test)
    add_dependencies(unit-check rasnetserver_test)
endif ()

