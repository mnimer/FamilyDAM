module.exports = angular.module('dashboard.photos', [])
	.controller('PhotosController', require('./PhotosController'))
	.config(['$stateProvider', '$urlRouterProvider', function($stateProvider, $urlRouterProvider)
        {
            $stateProvider.state('photos', {
                    url:'/photos',
                    templateUrl: "modules/photos/photos.tpl.html",
                    controller: "PhotosController"
                });

        }]);
