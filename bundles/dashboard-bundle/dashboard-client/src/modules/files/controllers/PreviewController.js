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

var ImagePreviewController = function ($scope, $rootScope, $state, $stateParams)
{
    $scope.$on("image:preview", function(event, data){
        $scope.node = data.node;
    });


    $scope.$on("music:preview", function(event, data){
        $scope.node = data.node;
    });


    $scope.$on("video:preview", function(event, data){
        $scope.node = data.node;
    });


    var isMixinType = function(node, type)
    {
        if( node === undefined ) return false;
        var mixins = node['jcr:mixinTypes'];
        return mixins.indexOf(type) > -1;
    };

    var init = function ()
    {
    };
    init();

};

ImagePreviewController.$inject = ['$scope', '$rootScope', '$state', '$stateParams'];
module.exports = ImagePreviewController;