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
 *
 */


module.exports = angular.module('dashboard.movies', ['ui.bootstrap'])
    .controller('FileListController', require('../files/controllers/FileListController'))
    .config(['$stateProvider', '$urlRouterProvider', function($stateProvider, $urlRouterProvider)
    {
        $stateProvider.state('movies:files', {
                url: '/movies/files',
                templateUrl: "modules/files/files.list.tpl.html",
                controller: "FileListController",
                data:{
                    currentPath: "/content/dam/movies",
                    showPhotoGrid: true
                }
            })
            .state('movies:files.upload', {
                url: '/movies/files/upload',
                templateUrl: "modules/files/views/file-upload.tpl.html"
            });
    }]);
