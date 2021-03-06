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
    .directive('renderer', require('./directives/renderer'))
    .directive('folderRow', require('./directives/folderRow'))
    .directive('fileRow', require('./directives/fileRow'))
    .directive('imageRow', require('./directives/imageRow'))
    .directive('musicRow', require('./directives/musicRow'))
    .directive('videoRow', require('./directives/videoRow'))
    .directive('preview', require('./directives/preview'))
    .config(['$stateProvider', '$urlRouterProvider', function ($stateProvider, $urlRouterProvider)
    {
        $stateProvider.state('files', {
            url:"/files",
            views:{
                "":{
                    templateUrl: "modules/files/files.list.tpl.html",
                    controller: "FileListController"
                },
                'upload@files':{
                    templateUrl: "modules/files/views/file-upload.tpl.html"
                },
                'preview@files': {
                    templateUrl: "modules/files/views/file-preview.tpl.html",
                    controller: "PreviewController"
                }
            }
        })
        .state('photos:files', {
            url:"/photos/files",
            data:{
                currentPath: "/content/dam/photos",
                filterByTypes:["fd:image"],
                showPhotoGrid: true
            },
            views:{
                "":{
                    templateUrl: "modules/files/files.list.tpl.html",
                    controller: "FileListController"
                },
                'upload@photos:files':{
                    templateUrl: "modules/files/views/file-upload.tpl.html"
                },
                'preview@photos:files': {
                    templateUrl: "modules/files/views/file-preview.tpl.html",
                    controller: "PreviewController"
                }
            }
        })
        .state('music:files', {
            url:"/music/files",
            data:{
                currentPath: "/content/dam/music",
                filterByTypes:["fd:music"]
            },
            views:{
                "":{
                    templateUrl: "modules/files/files.list.tpl.html",
                    controller: "FileListController"
                },
                'upload@music:files':{
                    templateUrl: "modules/files/views/file-upload.tpl.html"
                },
                'preview@music:files': {
                    templateUrl: "modules/files/views/file-preview.tpl.html",
                    controller: "PreviewController"
                }
            }
        })
        .state('movies:files', {
            url:"/movies/files",
            data:{
                currentPath: "/content/dam/movies",
                filterByTypes:["fd:music"]
            },
            views:{
                "":{
                    templateUrl: "modules/files/files.list.tpl.html",
                    controller: "FileListController"
                },
                'upload@movies:files':{
                    templateUrl: "modules/files/views/file-upload.tpl.html"
                },
                'preview@movies:files': {
                    templateUrl: "modules/files/views/file-preview.tpl.html",
                    controller: "PreviewController"
                }
            }
        });

    }]);
