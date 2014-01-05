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

module.exports = angular.module('dashboard.photos', ['ui.bootstrap'])
    .controller('PhotoGridController', require('./controllers/PhotoGridController'))
    .controller('PhotoDetailsController', require('./controllers/PhotoDetailsController'))
    .controller('FileListController', require('../files/controllers/FileListController'))
	.service('photoService', require('./services/PhotoService'))
	.config(['$stateProvider', '$urlRouterProvider', function($stateProvider, $urlRouterProvider)
        {
            $stateProvider.state('photos', {
                    url:'/photos',
                    templateUrl: "modules/photos/photos.grid.tpl.html",
                    controller: "PhotoGridController"
                }).state('photos.details', {
                    url:'/:id/details',
                    templateUrl: "modules/photos/photos.details.tpl.html",
                    controller: "PhotoDetailsController"
                }).state('photo-files', {
                    url: '/photos/files',
                    templateUrl: "modules/files/files.list.tpl.html",
                    controller: "FileListController",
                    data:{
                        currentPath: "/content/dam/photos",
                        showPhotoGrid: true
                    },
                    resolve:{
                        currentPath: function(){
                            return {value:"/content/dam/photos"};
                        },
                        showPhotoGrid: function(){ return {value:true}; }
                    }
                });
        }]);


/**
 *
 */
