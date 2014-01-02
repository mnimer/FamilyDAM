
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

var PhotosController = function($scope, $rootScope, $location, $modal, $state, photoService, infiniteScroll) {


    var groupByProperty = "fc:created";
    $scope.assets = {};

    $scope.pageGrid = function()
    {
        console.log("infinite scroll triggered");
    };


    var refreshGrid = function()
    {
        $scope.selectFolder($scope.currentPath);
    };



    /**
     * Callback for search query from JCR Service
     * @param data
     * @param status
     * @param headers
     * @param config
     */
    var searchCallback = function(data)
    {
        groupData($scope.assets, data.data.data);
        // hateoas links
        $scope.self = data.data.links.self;
        $scope.next = data.data.links.next;
        $scope.prev = data.data.links.prev;
    };



    var groupData = function(assets, results)
    {
        for( var item in results)
        {
            var dt = results[item][groupByProperty];
            dt = new Date(Date.parse(dt));
            var dtTitle = dt.toLocaleDateString();
            if( assets[dtTitle] === undefined )
            {
                assets[dtTitle] = {};
                assets[dtTitle].title = dtTitle;
                assets[dtTitle].data = [];
            }

            assets[dtTitle].data.push( results[item] );
        }
    };



    var init = function(){

        $scope.$emit("MODE_CHANGE", "COLLECTION");
        $scope.$on('refreshData', refreshGrid);

        if( $rootScope.user == null )
        {
            $state.go("login");
            return;
            // transitionTo() promise will be rejected with
            // a 'transition prevented' error
        }
        var request = photoService.search(100, 1).then( searchCallback );

    };
    init();

};

PhotosController.$inject = ['$scope', '$rootScope', '$location', '$modal', '$state', 'photoService', 'infinite-scroll'];
module.exports = PhotosController;