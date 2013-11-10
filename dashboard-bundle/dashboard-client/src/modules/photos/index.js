module.exports = angular.module('dashboard.photos', [])
	.controller('PhotosController', require('./PhotosController'))
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
                    controller: "PhotosController"

                }).state('photos.list', {
                    url:'/list',
                    templateUrl: function (stateParams){
                        return 'modules/photos/photos.list.tpl.html';
                    },
                    controller: "PhotosController"
                });

        }]);
