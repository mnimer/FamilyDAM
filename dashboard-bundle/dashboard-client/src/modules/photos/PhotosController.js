/**
 * Controller for the all customers screen
 * @param $scope
 * @param $location
 * @param customerModel
 * @param productModel
 * @constructor
 */
var PhotosController = function($scope, $location) {

    $scope.layout = "grid";
    /**
     * Invoked on startup, like a constructor.
     */
    $scope.$on('$viewContentLoaded', function() {
        console.log("Photo controller loaded");
    });

    $scope.$on('$stateChangeStart',
        function(evt, toState, toParams, fromState, fromParams){
            // We can prevent this state from completing
            console.log("toState=" +toState);
            if( toState == "photos"){
                    evt.preventDefault();
            }
    });

    $scope.$on('$stateNotFound',
        function(event, unfoundState, fromState, fromParams){
            console.log(unfoundState.to); // "lazy.state"
            console.log(unfoundState.toParams); // {a:1, b:2}
            console.log(unfoundState.options); // {inherit:false} + default options
        });

    $scope.$on('$stateChangeError',
        function(event, toState, toParams, fromState, fromParams, error){
            console.log(toState);
            console.log(toParams);
            console.log(fromState);
            console.log(fromParams);
            console.log(error);
        });


    var init = function(){

    };
    init();
};

PhotosController.$inject = ['$scope',  '$location'];
module.exports = PhotosController;