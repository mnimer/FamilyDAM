module.exports = angular.module('dashboard.login', ['ui.bootstrap'])
    .service('loginService', require('./services/LoginService'))
    .controller('LoginController', require('./controllers/LoginController'))
    .config(['$stateProvider', '$urlRouterProvider', function($stateProvider, $urlRouterProvider)
    {
        $stateProvider.state('login', {
            url:'/login',
            templateUrl: "modules/login/login.tpl.html",
            controller: "LoginController"
        });

    }]);

