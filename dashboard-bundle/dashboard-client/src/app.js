/**
 * Initial definition of the Angular Application. This class imports (requires) all of the modules for the application.
 */
require('dashboard-templates');

// Define the required modules
var App = angular.module('dashboard', [
    'ngCookies',
    'ngResource',
	'ngDragDrop',
	'ui.router',
	'ui.bootstrap',
	'ui.bootstrap.tpls',
    'dashboard.templates',
    require('./modules/main').name,
    require('./modules/login').name,
    require('./modules/photos').name,
    require('./directives/fileUpload').name,
    require('./modules/user/preferences').name,
    require('./modules/user/usermanager').name])



    .config(['$stateProvider', '$urlRouterProvider', function($stateProvider, $urlRouterProvider)
    {

        // For any unmatched url, redirect to /state1
        $urlRouterProvider.when('', '/login');

        $urlRouterProvider.otherwise("/login");
    }]);

App.$inject = ['ui.router'];




