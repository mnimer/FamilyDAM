module.exports = angular.module('dashboard.user.usermanager', [])
	.controller('UserManagerController', require('./controllers/UserManagerController'))
    .service('userService', require('./services/UserService'))
    .config(['$stateProvider', '$urlRouterProvider', function($stateProvider, $urlRouterProvider)
    {
        $stateProvider.state('user-manager', {
            url:'/user/manager',
            templateUrl: "modules/user/usermanager/usermanager.tpl.html",
            controller: "UserManagerController"
        });

    }]);
