module.exports = angular.module('dashboard.photos', ['ui.bootstrap'])
    .controller('FolderNameModalCntrl', require('./controllers/FolderNameModalCntrl'))
    .controller('PhotosController', require('./controllers/PhotosController'))
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
                    templateUrl: "modules/photos/photos.grid.tpl.html"

                }).state('photos.list', {
                    url:'/list',
                    templateUrl: function (stateParams){
                        return 'modules/photos/photos.list.tpl.html';
                    }
                });

        }]);
