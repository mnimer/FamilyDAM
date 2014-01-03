/*
 * This file is part of FamilyCloud Project.
 *
 *     The FamilyCloud Project is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     The FamilyCloud Project is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with the FamilyCloud Project.  If not, see <http://www.gnu.org/licenses/>.
 */

var PhotosController = function($scope, $rootScope, $location, $modal, $state, photoService) {

    $scope.layout = "grid";
    $scope.assets = [];
    // part of the path we hide in the breadrum
    $scope.basePath = "/content/dam";
    // path to show in breadcrumb
    $scope.currentPath = "/";
    $scope.breadcrumb = [];
    $scope.showSidebar = false;
    $scope.showImageDetailsSidebar = false;


    $scope.refresh = function()
    {
        //$scope.$emit("refresh");
        $scope.$broadcast("refreshData");
    };


    $scope.$on("MODE_CHANGE", function(event, mode){

        if( $scope.mode != mode )
        {
            $scope.mode = mode;
            if( mode == "COLLECTION")
            {
                $scope.showSidebar = false;
                $scope.showImageDetailsSidebar = false;
            }
        }
    });

    $scope.$on("FullScreenToggle", function(event, boolean)
    {
        $scope.showSidebar = boolean;
        $scope.showUploadSidebar = false;
    });


    $scope.$on("IMAGE_SELECTED", function(event, data){
        $scope.selectedNode = data;
        $scope.showSidebar = true;
        $scope.showImageDetailsSidebar = true;

        if( data['fc:metadata'] !== undefined )
        {
            $scope.uuid = data['jcr:uuid'];
            $scope.keywords = data['fc:metadata']['Iptc']['Keywords']['value'];
        }

    });


    $scope.$on('$viewContentLoaded', function(event, toState, toParams, fromState, fromParams)
    {
        if( $rootScope.user == null )
        {
            event.preventDefault();
            // transitionTo() promise will be rejected with
            // a 'transition prevented' error
        }
    });


    var init = function()
    {
        if( $rootScope.user == null )
        {
            $state.go("login");
            return;
        }
    };
    init();

    /**
     * Utility functions
     */
    $scope.safeApply = function(fn) {
        var phase = this.$root.$$phase;
        if(phase == '$apply' || phase == '$digest') {
            if(fn && (typeof(fn) === 'function')) {
                fn();
            }
        } else {
            this.$apply(fn);
        }
    };

};

PhotosController.$inject = ['$scope', '$rootScope', '$location', '$modal', '$state', 'photoService'];
module.exports = PhotosController;