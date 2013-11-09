module.exports = angular.module('dashboard.user.usermanager', [])
	.controller('UserManagerController', require('./UserManagerController'))
    .config(['$stateProvider', '$urlRouterProvider', function($stateProvider, $urlRouterProvider)
    {
        $stateProvider.state('user-manager', {
            url:'/user/manager',
            templateUrl: "modules/user/usermanager/usermanager.tpl.html",
            controller: "UserManagerController"
        });

    }]);
