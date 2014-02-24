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


var PhotoDetailsController = function($scope, $rootScope, $state, $window, $stateParams, photoService) {

    $scope.self = "";
    $scope.scaledImage = undefined;
    $scope.node = {};
    $scope.keywords = ['foo', 'bar'];

    $scope.showSidebar = true;

    var init = function()
    {
        if ($rootScope.user == null)
        {
            $state.go("login");
            return;
            // transitionTo() promise will be rejected with
            // a 'transition prevented' error
        }

        $scope.$emit("MODE_CHANGE", "DETAILS");

        var id = $state.params.id;
        if( id !== undefined )
        {
            photoService.getById(id).then(function(data, status, headers, config){

                $scope.node = data.data;
                $scope.self = data.headers.apply()['location'];

                if( data.data['jcr:path'] === undefined )
                {
                    data.data['jcr:path'] = $scope.self;
                }

                var width  = $("body").width();
                $scope.scaledImageUrl = $scope.self +".scale.w:" +width +".png";

                $scope.$emit("IMAGE_SELECTED", $scope.node);
                $scope.$broadcast("IMAGE_SELECTED", $scope.node);

            });
        }
    };
    init();

};

PhotoDetailsController.$inject = ['$scope', '$rootScope', '$state', '$window', '$stateParams', 'photoService'];
module.exports = PhotoDetailsController;