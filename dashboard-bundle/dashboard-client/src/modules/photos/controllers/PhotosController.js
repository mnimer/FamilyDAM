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
    $scope.showUploadSidebar = false;


    $scope.refresh = function()
    {
        //$scope.$emit("refresh");
        $scope.$broadcast("refreshData");
    };


    $scope.toggleUpload = function()
    {
        var b = !$scope.showUploadSidebar;
        //show or hide the sidebar div, based on toggle
        $scope.showSidebar = b;
        $scope.showUploadSidebar = b;
    };

    $scope.isUploadVisible = function()
    {
        return $scope.showUploadSidebar;
    };

    $scope.openCreateFolder = function()
    {
        var modalInstance = $modal.open({
            templateUrl: 'FolderNameModal',
            controller: 'FolderNameModalCntrl',
            resolve:{
                photoService: function(){
                    return photoService;
                },
                currentPath: function(){
                    if( $scope.currentPath == "/" )
                    {
                            return "/photos";
                    }else{
                        return $scope.currentPath;
                    }
                }
            }
        });
    };




    /**
     * Parse a path into it's tokens for a valid breadcrumb array
     */
    $scope.selectFolder = function(path)
    {
        // this is called from child view, so we'll update the parent scope to get the breadcrumb binding
        $scope.$broadcast("pathChange", path);

        $scope.currentPath = path;

        var breadcrumb = [];
        var nodes = path.split("/");
        var lastPath = "";
        for (var indx in nodes)
        {
            var obj = nodes[indx];
            if( obj.length > 1 )
            {
                lastPath = lastPath +"/" +obj;
                breadcrumb.push({name:obj, path:lastPath});
            }
        }
        $scope.breadcrumb = breadcrumb;
    };



    $scope.$on("FullScreenToggle", function(event, boolean)
    {
        $scope.showSidebar = boolean;
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