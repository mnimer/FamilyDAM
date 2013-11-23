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

var PhotosController = function($scope, $rootScope, $location, $modal, $state, photoService)
{

    $scope.$on("pathChange", function(event, path)
    {
        // update list of photos
        $scope.assets = [];
        $scope.photos = photoService.list(path).then(listCallback);
    });



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
     * Callback for file list from JCR Service
     * @param data
     * @param status
     * @param headers
     * @param config
     */
    var listCallback = function(data)
    {
        var contents = [];
        var pos = data.config.url.indexOf(".1.json");
        var pos2 = $scope.basePath.length;
        var basePath = data.config.url.substring(pos2, pos);

        for(var key in data.data)
        {
            var item = data.data[key];
            if( item instanceof Object )
            {
                item.name = key;
                item.path = basePath +"/" +key;

                contents.push(item);
            }
        }

        $scope.assets = contents;
    };


    var init = function()
    {
        $scope.$on('refreshData', refreshGrid);

        if( $rootScope.user == null )
        {
            $state.go("login");
            return;
            // transitionTo() promise will be rejected with
            // a 'transition prevented' error
        }
        $scope.photos = photoService.list('/photos').then(listCallback);
    };
    init();

};

PhotosController.$inject = ['$scope', '$rootScope', '$location', '$modal', '$state', 'photoService'];
module.exports = PhotosController;