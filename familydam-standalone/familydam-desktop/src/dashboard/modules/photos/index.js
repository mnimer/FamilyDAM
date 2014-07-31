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

/**

 .state('photo:files:old', {
                url: '/photos/files',
                templateUrl: "modules/files/files.list.tpl.html",
                controller: "FileListController",
                data:{
                    currentPath: "/content/dam/photos",
                    showPhotoGrid: true
                }
            })
 **/

/**
 *
 */


module.exports = angular.module('dashboard.photos', ['ui.bootstrap'])


    .controller('PhotoGridController', require('./controllers/PhotoGridController'))
    .controller('PhotosGridKeywordsController', require('./controllers/PhotoGridKeywordsController'))
    .controller('PhotoDetailsController', require('./controllers/PhotoDetailsController'))
    .controller('PhotoEditController', require('./controllers/PhotoEditController'))
    .controller('PhotoMetadataController', require('./controllers/PhotoMetadataController'))
    .controller('PhotoVersionController', require('./controllers/PhotoVersionController'))

    .directive('dateFilter', require('./directives/dateFilter/index'))
    .directive('locationFilter', require('./directives/locationFilter/index'))
    .directive('tagCloudFilter', require('./directives/tagCloudFilter/index'))
    .directive('keywords', require('./directives/keywords/index'))
    .directive('locationMap', require('./directives/locationMap/index'))
    .directive('exif', require('./directives/exif/index'))
    //.directive('columnSlider', require('./directives/columnSlider'))

    .config(['$stateProvider', function($stateProvider)
    {
        $stateProvider
            .state('photos:grid',
            {
                url:'/photos/grid',
                templateUrl: "modules/photos/photos.grid.tpl.html",
                controller: "PhotoGridController"
            })
            .state('photos:details',
            {
                url:'/:id/details',
                templateUrl: "modules/photos/photos.details.tpl.html",
                controller: "PhotoDetailsController"
            });
    }]);
