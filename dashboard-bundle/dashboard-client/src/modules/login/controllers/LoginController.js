var LoginController = function($scope, $rootScope, $location, $state, loginService)
{
    $scope.loginForm = {username:"admin", password:"admin"}; //todo:remove the hard coded login values
    $rootScope.ModeFullScreen = false;

    $scope.login = function()
    {
        var loginQ = loginService.login($scope.loginForm.username, $scope.loginForm.password);
        loginQ.then(
            function(data, status, headers, config)
            {
                $rootScope.username = $scope.loginForm.username;

                var getUserQ = loginService.getUser($scope.loginForm.username);
                getUserQ.then(
                    function(data)
                    {
                        $rootScope.user = data;
                        $rootScope.ModeFullScreen = true;
                        $state.go($rootScope.defaultView);
                    }, function(reason) {
                        $scope.message = reason;
                    }
                );
            }, function(response){
                // todo: error handler
                $scope.message = response.data;
            }
        );
    };
};

LoginController.$inject = ['$scope', '$rootScope', '$location', '$state', 'loginService'];
module.exports = LoginController;