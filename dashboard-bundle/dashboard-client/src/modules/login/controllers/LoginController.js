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
                        $state.go("home");
                        // after login, turn off full screen


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

    // reset the logged in user
    $rootScope.user = null;
    $scope.$broadcast("FullScreenToggle", true);
};

LoginController.$inject = ['$scope', '$rootScope', '$location', '$state', 'loginService'];
module.exports = LoginController;