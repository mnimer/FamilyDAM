/*
 * This file is part of FamilyDAM Project.
 *
 *     The FamilyDAM Project is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     The FamilyDAM Project is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with the FamilyDAM Project.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Module definition for the Add To WaitList popup.  This class will also define the url path that triggers this screen.
 */
module.exports = angular.module('FamilyDAM.FileUploadModal', ['$scope', '$modalInstance'])
    .controller('fileUploadModalController', function ($scope, $modalInstance)
    {
        $scope.close = function ()
        {
            $modalInstance.dismiss('cancel');
        };
    });

