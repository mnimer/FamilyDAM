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

var FolderNameModalCntrl = function ($scope, $rootScope,  $modalInstance, fileService, currentPath) {

    $scope.formModel = {};
    $scope.currentPath = currentPath;
    $scope.fileService = fileService;

    $scope.createFolder = function ()
    {
        $scope.fileService.createFolder($scope.currentPath, $scope.formModel.path).then(function()
        {
            $rootScope.$broadcast('refresh');
            $modalInstance.close();
        }, function(err){
            //todo error handler
        });

    };

    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };
};

FolderNameModalCntrl.$inject = ['$scope', '$rootScope', '$modalInstance', 'fileService', 'currentPath'];
module.exports = FolderNameModalCntrl;