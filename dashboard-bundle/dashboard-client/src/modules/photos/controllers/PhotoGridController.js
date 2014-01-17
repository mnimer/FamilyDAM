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


    var groupByProperty = "created";
    $scope.assets = {};
    // bootstrap columns for thumbnails
    $scope.responsiveColumns = 3;
    $scope.responsiveColumnLabel = "col-xs-12 col-sm-3";

    $scope.$on("photo:grid:columns", function(data, args){
        $scope.responsiveColumns = args;
        $scope.responsiveColumnLabel = "col-xs-12 col-sm-" +args;
        //$scope.$apply();
    });


    var _filterLimit = 50;
    var _filterOffset = 1;
    var _filterPath = "";
    var _filterTags = "";
    var _filterDateFrom = "";
    var _filterDateTo = "";

    $scope.$on("filter:location:path", function(event, val){
        if( val !== undefined && val !== "")
        {
            _filterPath = val;
            refreshSearch();
        }
    });

    $scope.$on("filter:date:from", function(event, val){
        if( val !== undefined && val !== "")
        {
            _filterDateFrom = val.getTime();
            refreshSearch();
        }
    });

    $scope.$on("filter:date:to", function(event, val){
        if( val !== undefined && val !== "")
        {
            _filterDateTo = val.getTime();
            refreshSearch();
        }
    });

    $scope.$on("filter:tags", function(event, val){
        if( val !== undefined && val !== "")
        {
            _filterTags = val;
            refreshSearch();
        }
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



    $scope.$on('$viewContentLoaded', function(event, toState, toParams, fromState, fromParams)
    {
        if( $rootScope.user == null )
        {
            event.preventDefault();
            // transitionTo() promise will be rejected with
            // a 'transition prevented' error
        }
    });



    var groupData = function (results)
    {
        var assets = {};
        for (var item in results.data)
        {
            var dt = results.data[item][groupByProperty];
            dt = new Date(Date.parse(dt));
            var dtTitle = dt.getFullYear() + "-" + (dt.getMonth() + 1) + "-" + dt.getDate();
            if (assets[dtTitle] === undefined)
            {
                assets[dtTitle] = {};
                assets[dtTitle].title = moment(dt).format('MMMM Do, YYYY');
                assets[dtTitle].data = [];
            }

            var heights = [150,250,300,350,425];
            results.data[item].height = heights[getRandomInt(1,4)];
            assets[dtTitle].data.push(results.data[item]);
        }
        return assets;
    };


    var getRandomInt = function (min, max)
    {
        return Math.floor(Math.random() * (max - min + 1)) + min;
    };


    var refreshSearch = function()
    {
        photoService.search(_filterLimit, _filterOffset, _filterPath, _filterDateFrom, _filterDateTo, _filterTags).then(searchCallback);
    };

    /**
     * Process the search results by grouping them, and checking for the hateoas links.
     * @param _data
     */
    var searchCallback = function (_data)
    {
        $scope.assets = groupData(_data);
        //$scope.$apply();
        // hateoas links
        if( _data.links !== undefined )
        {
            if( _data.links.self !== undefined )
            {
                $scope.self = _data.links.self;
            }
            if( _data.links.next !== undefined )
            {
                $scope.next = _data.links.next;
            }
            if( _data.links.prev !== undefined )
            {
                $scope.prev = _data.links.prev;
            }
        }
    };


    var init = function ()
    {
        $scope.$on('refreshData', refreshGrid);

        if ($rootScope.user == null)
        {
            $state.go("login");
            return;
            // transitionTo() promise will be rejected with
            // a 'transition prevented' error
        }
        var request = photoService.search(_filterLimit, _filterOffset, _filterPath, _filterDateFrom, _filterDateTo, _filterTags).then(searchCallback);

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