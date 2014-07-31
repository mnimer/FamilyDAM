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

module.exports = angular.module('dashboard.files', ['ui.bootstrap'])
    .controller('FolderNameModalCntrl', require('./controllers/FolderNameModalCntrl'))
    .controller('FileListController', require('./controllers/FileListController'))
    .controller('PreviewController', require('./controllers/PreviewController'))
    .directive('renderer', require('./directives/renderer/index'))
    .directive('folderRow', require('./directives/folderRow/index'))
    .directive('fileRow', require('./directives/fileRow/index'))
    .directive('imageRow', require('./directives/imageRow/index'))
    .directive('musicRow', require('./directives/musicRow/index'))
    .directive('videoRow', require('./directives/videoRow/index'))
    .directive('preview', require('./directives/preview/index'))
    .config(['$stateProvider', '$urlRouterProvider', function ($stateProvider, $urlRouterProvider)
    {
        $stateProvider.state('files', {
            url:"/files",
            templateUrl: "modules/files/files.list.tpl.html",
            controller: "FileListController"
        })
        .state('photos:files', {
            url:"/photos/files",
            templateUrl: "modules/files/files.list.tpl.html",
            controller: "FileListController",
            data:{
                currentPath: "/content/dam/photos",
                filterByTypes:["fd:image"],
                showPhotoGrid: true
            }

        })
        .state('music:files', {
            url:"/music/files",
            templateUrl: "modules/files/files.list.tpl.html",
            controller: "FileListController",
            data:{
                currentPath: "/content/dam/music",
                filterByTypes:["fd:music"]
            }
        })
        .state('movies:files', {
            url:"/movies/files",
            templateUrl: "modules/files/files.list.tpl.html",
            controller: "FileListController",
            data:{
                currentPath: "/content/dam/movies",
                filterByTypes:["fd:music"]
            }
        });

    }]);
