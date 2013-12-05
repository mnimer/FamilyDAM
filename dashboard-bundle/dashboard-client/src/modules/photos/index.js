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

module.exports = angular.module('dashboard.photos', ['ui.bootstrap'])
    .controller('FolderNameModalCntrl', require('./controllers/FolderNameModalCntrl'))
    .controller('PhotosController', require('./controllers/PhotosController'))
    .controller('PhotoListController', require('./controllers/PhotoListController'))
    .controller('PhotoGridController', require('./controllers/PhotoGridController'))
	.service('photoService', require('./services/PhotoService'))
	.config(['$stateProvider', '$urlRouterProvider', function($stateProvider, $urlRouterProvider)
        {
            $stateProvider.state('photos', {
                    url:'/photos',
                    abstract:true,
                    templateUrl: "modules/photos/photos.tpl.html",
                    controller: "PhotosController"
                }).state('photos.grid', {
                    url:'',
                    templateUrl: "modules/photos/photos.grid.tpl.html",
                    controller: "PhotoGridController"
                }).state('photos.list', {
                    url:'/list',
                    templateUrl: function (stateParams){
                        return 'modules/photos/photos.list.tpl.html';
                    },
                    controller: "PhotoListController"
                });

        }]);
