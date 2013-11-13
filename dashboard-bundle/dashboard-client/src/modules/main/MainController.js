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
var MainController = function ($scope, $window, $location, $state, $stateParams) {

    $scope.$state = $state;
    $scope.$stateParams = $stateParams;

    $scope.ModeFullScreen = false;
    $scope.activePath = "/";

    $scope.toggleFullScreenMode = function()
    {
        $scope.ModeFullScreen = !$scope.ModeFullScreen;
    };


    /**
     * Invoked on startup, like a constructor.
     */
    var init = function () {
        //customersService.getCustomer(cloverConfig.customerID, getCustomerCallback, errorCallback);

    };
    init();
};

MainController.$inject = ['$scope', '$window', '$location', '$state', '$stateParams'];
module.exports = MainController;