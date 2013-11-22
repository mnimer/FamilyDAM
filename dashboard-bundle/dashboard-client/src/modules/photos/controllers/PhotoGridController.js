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

    /*************
     * Event Listeners
     **************/


    var refreshGrid = function()
    {
        if( $scope.currentPath == "/" )
        {
            $scope.selectFolder("/photos");
        }else{
            $scope.selectFolder($scope.currentPath);
        }
    };

    /**
     * Callback for search query from JCR Service
     * @param data
     * @param status
     * @param headers
     * @param config
     */
    var searchCallback = function(data, status, headers, config)
    {
        $scope.assets = data.data.data;
        // hateoas links
        $scope.self = data.data.links.self;
        $scope.next = data.data.links.next;
        $scope.prev = data.data.links.prev;
    };



    var init = function(){

        $scope.$on('refreshData', refreshGrid);

        if( $rootScope.user == null )
        {
            $state.go("login");
            return;
            // transitionTo() promise will be rejected with
            // a 'transition prevented' error
        }
        var request = photoService.search(25, 1);
        request.then( searchCallback );

    };
    init();

};

PhotosController.$inject = ['$scope', '$rootScope', '$location', '$modal', '$state', 'photoService'];
module.exports = PhotosController;