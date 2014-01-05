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

var PhotosController = function ($scope, $rootScope, $location, $modal, $state, photoService)
{
    $scope.layout = "grid";

    // part of the path we hide in the breadrumb
    var rootPath = "/content/dam/photos";
    $scope.currentPath = rootPath;

    $scope.breadcrumb = [];
    $scope.showSidebar = true;
    $scope.showFilterSidebar = true;
    $scope.showImageDetailsSidebar = false;


    var groupByProperty = "fc:created";
    $scope.assets = {};
    // bootstrap columns for thumbnails
    $scope.responsiveColumns = 3;
    $scope.responsiveColumnLabel = "col-xs-12 col-sm-3";

    $scope.$on("photo:grid:columns", function(data, args){
        $scope.responsiveColumns = args;
        $scope.responsiveColumnLabel = "col-xs-12 col-sm-" +args;
        $scope.$apply();
    });


    $scope.getColumnLabel = function(columns)
    {
        return "col-sm-" +$scope.responsiveColumns;
    };


    $scope.pageGrid = function ()
    {
        photoService.invokeLink($scope.next).then(searchCallback);
    };


    var refreshGrid = function ()
    {
        $scope.selectFolder($scope.currentPath);
    };



    $scope.$on("MODE_CHANGE", function(event, mode){

        if( $scope.mode != mode )
        {
            $scope.mode = mode;
            if( mode == "COLLECTION")
            {
                $scope.showSidebar = true;
                $scope.showFilterSidebar = true;
                $scope.showImageDetailsSidebar = false;
            }
        }
    });


    $scope.$on("IMAGE_SELECTED", function(event, data){
        $scope.selectedNode = data;
        $scope.showSidebar = true;
        $scope.showFilterSidebar = false;
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



    var groupData = function (assets, results)
    {
        for (var item in results)
        {
            var dt = results[item][groupByProperty];
            dt = new Date(Date.parse(dt));
            var dtTitle = dt.getFullYear() + "-" + (dt.getMonth() + 1) + "-" + dt.getDate();
            if (assets[dtTitle] === undefined)
            {
                assets[dtTitle] = {};
                assets[dtTitle].title = dtTitle;
                assets[dtTitle].data = [];
            }

            var heights = [150,250,300,350,425];
            results[item].height = heights[getRandomInt(1,4)];
            assets[dtTitle].data.push(results[item]);
        }
    };


    var getRandomInt = function (min, max)
    {
        return Math.floor(Math.random() * (max - min + 1)) + min;
    };


    var searchCallback = function (data)
    {
        groupData($scope.assets, data.data.data);
        // hateoas links
        $scope.self = data.data.links.self;
        $scope.next = data.data.links.next;
        $scope.prev = data.data.links.prev;
    };


    var init = function ()
    {

        $scope.$emit("MODE_CHANGE", "COLLECTION");
        $scope.$on('refreshData', refreshGrid);

        if ($rootScope.user == null)
        {
            $state.go("login");
            return;
            // transitionTo() promise will be rejected with
            // a 'transition prevented' error
        }
        var request = photoService.search(50, 1).then(searchCallback);

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