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
    $rootScope.defaultView = "photos.list";
    $rootScope.ModeFullScreen = false;

    $scope.$state = $state;
    $scope.$stateParams = $stateParams;
    $scope.activePath = "/";
    $scope.ModeFullScreen = $rootScope.ModeFullScreen;

    $scope.toggleFullScreenMode = function()
    {
        $rootScope.ModeFullScreen = !$rootScope.ModeFullScreen;
        $scope.ModeFullScreen = $rootScope.ModeFullScreen;
    };


    $scope.$on('$stateChangeError', function(event, toState, toParams, fromState, fromParams, error)
    {
        $state.go("login");
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