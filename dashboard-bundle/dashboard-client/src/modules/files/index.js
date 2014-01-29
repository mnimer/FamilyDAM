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
    .controller('ImagePreviewController', require('./controllers/ImagePreviewController'))
    .controller('MusicPreviewController', require('./controllers/MusicPreviewController'))
    .directive('renderer', require('./directives/renderer'))
    .directive('folderRow', require('./directives/folderRow'))
    .directive('fileRow', require('./directives/fileRow'))
    .directive('imageRow', require('./directives/imageRow'))
    .directive('musicRow', require('./directives/musicRow'))
    .directive('imagePreview', require('./directives/imagePreview'))
    .directive('musicPlayer', require('./directives/musicPlayer'))
    .service('fileService', require('./services/FileService'))
    .config(['$stateProvider', '$urlRouterProvider', function ($stateProvider, $urlRouterProvider)
    {
        $stateProvider.state('files', {
            templateUrl: "modules/files/files.list.tpl.html",
            controller: "FileListController"
        })
            .state('files.upload', {
                url: '/files/upload',
                templateUrl: "modules/files/views/file-upload.tpl.html"
            })
            .state('files.image:preview', {
                url: '/files/preview/image?path',
                templateUrl: "modules/files/views/file-preview-image.tpl.html"
            })
            .state('files.music:preview', {
                url: '/files/preview/music?path',
                templateUrl: "modules/files/views/file-preview-music.tpl.html"
            });

    }]);
