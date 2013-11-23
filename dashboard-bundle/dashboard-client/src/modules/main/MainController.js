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

/**
 * Controller for the main application screen that controls the header and menu.
 * @param $scope
 * @param $window
 * @param $route
 * @param $location
 * @param cloverConfig
 * @param $dialog
 * @constructor
 */
var MainController = function ($scope, $rootScope, $window, $location, $state, $stateParams) {

    // logged in user
    $rootScope.user = null;
    $rootScope.defaultView = "photos.grid";

    $scope.$state = $state;
    $scope.$stateParams = $stateParams;
    $scope.activePath = "/";
    $scope.fullScreenMode = true;
    $scope.showNavigation = false;

    $scope.toggleFullScreenMode = function()
    {
        $scope.fullScreenMode = !$scope.fullScreenMode;
        // broadcast up and down the stack
        $scope.$emit("FullScreenToggle", $scope.FullScreenMode);
        $scope.$broadcast("FullScreenToggle", $scope.fullScreenMode);
    };


    $scope.$on('$stateChangeError', function(event, toState, toParams, fromState, fromParams, error)
    {
        $rootScope.user = null;
        $state.go("login");
    });


    $scope.$on("FullScreenToggle", function(event, boolean)
    {
        $scope.showNavigation = boolean;
    });

    /**
     * Invoked on startup, like a constructor.
     */
    var init = function () {

        //customersService.getCustomer(cloverConfig.customerID, getCustomerCallback, errorCallback);

    };
    init();
};

MainController.$inject = ['$scope', '$rootScope', '$window', '$location', '$state', '$stateParams'];
module.exports = MainController;