module.exports = angular.module('dashboard.user.preferences', [])
	.controller('PreferencesController', require('./PreferencesController'))
    .config(['$stateProvider', '$urlRouterProvider', function($stateProvider, $urlRouterProvider)
    {
        $stateProvider.state('user', {
            url:'/user/preferences',
            templateUrl: "modules/user/preferences/preferences.tpl.html",
            controller: "PreferencesController"
        });

    }]);
